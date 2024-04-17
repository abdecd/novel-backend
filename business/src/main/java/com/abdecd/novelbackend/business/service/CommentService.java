package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.common.util.SpringContextUtil;
import com.abdecd.novelbackend.business.common.util.UnionFind;
import com.abdecd.novelbackend.business.mapper.UserCommentMapper;
import com.abdecd.novelbackend.business.pojo.dto.user.AddCommentDTO;
import com.abdecd.novelbackend.business.pojo.entity.UserComment;
import com.abdecd.novelbackend.business.pojo.vo.user.UserCommentVO;
import com.abdecd.novelbackend.business.pojo.vo.user.UserCommentVOBasic;
import com.abdecd.novelbackend.common.constant.RedisConstant;
import com.abdecd.novelbackend.common.constant.StatusConstant;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.tokenlogin.common.context.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Nonnull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

@Service
public class CommentService {
    @Autowired
    private UserCommentMapper userCommentMapper;
    @Autowired
    private RedisTemplate<String, LocalDateTime> redisTemplate;

    public PageVO<List<UserCommentVO>> getComment(Integer novelId, Integer page, Integer pageSize) {
        var commentService = SpringContextUtil.getBean(CommentService.class);
        var userCommentList = commentService.getCommentsByNovelIdCached(novelId);
        if (userCommentList.isEmpty()) return new PageVO<>(0, new ArrayList<>());
        var cnt = userCommentMapper.countRootCommentByNovelId(novelId, StatusConstant.ENABLE);
        if (page * pageSize > userCommentList.size()) {
            // 超出范围且后面有数据就无缓
            if (cnt > userCommentList.size()) userCommentList = commentService.getCommentsByNovelId(novelId);
        }
        return new PageVO<>(cnt, userCommentList.subList(Math.max(0, (page - 1) * pageSize), Math.min(userCommentList.size(), page * pageSize)));
    }

    /**
     * 最多缓存前 RedisConstant.COMMENT_FOR_NOVEL_SIZE 条
     */
    @Cacheable(value = "getCommentsByNovelId", key = "#novelId", unless = "#result.isEmpty()")
    @Nonnull
    public List<List<UserCommentVO>> getCommentsByNovelIdCached(Integer novelId) {
        var list = getCommentsByNovelId(novelId);
        if (list == null || list.isEmpty()) return new ArrayList<>();
        var maxCnt = RedisConstant.COMMENT_FOR_NOVEL_SIZE;
        var lastIndex = 0;
        for (var userCommentList : list) {
            if ((maxCnt -= userCommentList.size()) >= 0) {
                lastIndex++;
            } else break;
        }
        return new ArrayList<>(list.subList(0, lastIndex));
    }

    public List<List<UserCommentVO>> getCommentsByNovelId(Integer novelId) {
        var allComments = userCommentMapper.listCommentVOByNovelId(novelId, null);
        if (allComments.isEmpty()) return null;

        var unionFind = new UnionFind(Math.toIntExact(allComments.getLast().getId()) + 1);
        for (var userComment : allComments) {
            if (userComment.getToId() == -1) continue;
            unionFind.union(Math.toIntExact(userComment.getId()), Math.toIntExact(userComment.getToId()));
        }

        var result = new LinkedHashMap<Integer, List<UserCommentVOBasic>>();
        for (var userComment : allComments) {
            if (Objects.equals(userComment.getStatus(), StatusConstant.DISABLE)) continue;
            // 根评论已建立，直接附加
            if (result.containsKey(unionFind.find(Math.toIntExact(userComment.getId())))) {
                result.get(unionFind.find(Math.toIntExact(userComment.getId()))).add(userComment);
            } else {
                // 建立根评论块
                // 第一个一般是根评论（最早）
                // 不是根评论说明根评论删掉了，整块不显示
                if (userComment.getToId() != -1) continue;
                ArrayList<UserCommentVOBasic> list = new ArrayList<>();
                list.add(userComment);
                result.put(unionFind.find(Math.toIntExact(userComment.getId())), list);
            }
        }
        return new ArrayList<>(result.values()
                .stream().map(list -> new ArrayList<>(list.stream().map(item -> {
                            var userCommentVO = new UserCommentVO();
                            BeanUtils.copyProperties(item, userCommentVO);
                            return userCommentVO;
                        }).toList())
                ).toList());
    }

    @CacheEvict(value = "getCommentsByNovelId", key = "#addCommentDTO.novelId")
    public Long addComment(AddCommentDTO addCommentDTO) {
        var userComment = addCommentDTO.toEntity();
        // 检查评论的id是否在这本小说中
        if (addCommentDTO.getToId() != -1) {
            var beCommented = userCommentMapper.selectOne(new LambdaQueryWrapper<UserComment>()
                    .eq(UserComment::getId, userComment.getToId())
                    .eq(UserComment::getNovelId, userComment.getNovelId())
                    .eq(UserComment::getStatus, StatusConstant.ENABLE)
            );
            if (beCommented == null) return (long) -1;
        }
        userCommentMapper.insert(userComment);
        redisTemplate.opsForValue().set(RedisConstant.COMMENT_FOR_NOVEL_TIMESTAMP + userComment.getNovelId(), userComment.getTimestamp());
        return userComment.getId();
    }

    @CacheEvict(value = "getCommentsByNovelId", key = "#root.target.getNovelIdByCommentId(#id)")
    public void deleteComment(Integer id) {
        var effectedRows = userCommentMapper.update(new LambdaUpdateWrapper<UserComment>()
                .eq(UserComment::getId, id)
                .eq(UserComment::getUserId, UserContext.getUserId())
                .set(UserComment::getStatus, StatusConstant.DISABLE)
                .set(UserComment::getTimestamp, LocalDateTime.now())
        );
        if (effectedRows != 0) {
            var obj = userCommentMapper.selectOne(new LambdaQueryWrapper<UserComment>()
                    .select(UserComment::getNovelId, UserComment::getTimestamp)
                    .eq(UserComment::getId, id)
            );
            redisTemplate.opsForValue().set(RedisConstant.COMMENT_FOR_NOVEL_TIMESTAMP + obj.getNovelId(), obj.getTimestamp());
        }
    }

    @CacheEvict(value = "getCommentsByNovelId", key = "#root.target.getNovelIdByCommentId(#id)")
    public void deleteCommentByAdmin(Integer id) {
        var effectedRows = userCommentMapper.update(new LambdaUpdateWrapper<UserComment>()
                .eq(UserComment::getId, id)
                .set(UserComment::getStatus, StatusConstant.DISABLE)
                .set(UserComment::getTimestamp, LocalDateTime.now())
        );
        if (effectedRows != 0) {
            var obj = userCommentMapper.selectOne(new LambdaQueryWrapper<UserComment>()
                    .select(UserComment::getNovelId, UserComment::getTimestamp)
                    .eq(UserComment::getId, id)
            );
            redisTemplate.opsForValue().set(RedisConstant.COMMENT_FOR_NOVEL_TIMESTAMP + obj.getNovelId(), obj.getTimestamp());
        }
    }

    public Integer getNovelIdByCommentId(Integer id) {
        var obj = userCommentMapper.selectById(id);
        if (obj == null) return -1;
        return obj.getNovelId();
    }
}
