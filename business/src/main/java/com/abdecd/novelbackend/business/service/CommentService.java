package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.common.util.SpringContextUtil;
import com.abdecd.novelbackend.business.common.util.UnionFind;
import com.abdecd.novelbackend.business.mapper.UserCommentMapper;
import com.abdecd.novelbackend.business.pojo.dto.user.AddCommentDTO;
import com.abdecd.novelbackend.business.pojo.entity.UserComment;
import com.abdecd.novelbackend.business.pojo.vo.user.UserCommentVO;
import com.abdecd.novelbackend.common.constant.StatusConstant;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.tokenlogin.common.context.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

@Service
public class CommentService {
    @Autowired
    private UserCommentMapper userCommentMapper;

    public PageVO<List<UserCommentVO>> getComment(Integer novelId, Long startId, Integer pageSize) {
        var commentService = SpringContextUtil.getBean(CommentService.class);
        var userCommentList = commentService.getCommentsByNovelId(novelId);
        if (userCommentList == null) return new PageVO<>(0, new ArrayList<>());
        var startIndex = 0;
        if (startId != null) for (var i = 0; i < userCommentList.size(); i++) {
            if (Objects.equals(userCommentList.get(i).getFirst().getId(), startId)) {
                startIndex = i;
                break;
            }
        }
        return new PageVO<>(userCommentList.size(), userCommentList.subList(startIndex, Math.min(userCommentList.size(), startIndex + pageSize)));
    }

    @Cacheable(value = "getCommentsByNovelId", key = "#novelId", unless = "#result == null")
    public List<List<UserCommentVO>> getCommentsByNovelId(Integer novelId) {
        var allComments = userCommentMapper.listCommentVOByNovelId(novelId, StatusConstant.ENABLE);
        if (allComments.isEmpty()) return null;
        var unionFind = new UnionFind(Math.toIntExact(allComments.getLast().getId()) + 1);
        for (var userComment : allComments) {
            if (userComment.getToId() == -1) continue;
            unionFind.union(Math.toIntExact(userComment.getId()), Math.toIntExact(userComment.getToId()));
        }
        var result = new LinkedHashMap<Integer, List<UserCommentVO>>();
        for (var userComment : allComments) {
            // 根评论已建立，直接附加
            if (result.containsKey(unionFind.find(Math.toIntExact(userComment.getId())))) {
                result.get(unionFind.find(Math.toIntExact(userComment.getId()))).add(userComment);
            } else {
                // 建立根评论块
                // 第一个一般是根评论（最早）
                // 不是根评论说明根评论删掉了，整块不显示
                if (userComment.getToId() != -1) continue;
                ArrayList<UserCommentVO> list = new ArrayList<>();
                list.add(userComment);
                result.put(unionFind.find(Math.toIntExact(userComment.getId())), list);
            }
        }
        return new ArrayList<>(result.values());
    }

    @CacheEvict(value = "getCommentsByNovelId", key = "#addCommentDTO.novelId")
    public Long addComment(AddCommentDTO addCommentDTO) {
        var userComment = addCommentDTO.toEntity();
        // 检查评论的id是否在这本小说中
        var beCommented = userCommentMapper.selectOne(new LambdaQueryWrapper<UserComment>()
                .eq(UserComment::getId, userComment.getToId())
                .eq(UserComment::getNovelId, userComment.getNovelId())
                .eq(UserComment::getStatus, StatusConstant.ENABLE)
        );
        if (beCommented == null) return (long) -1;
        userCommentMapper.insert(userComment);
        return userComment.getId();
    }

    @CacheEvict(value = "getCommentsByNovelId", key = "#root.target.getNovelIdByCommentId(#id)")
    public void deleteComment(Integer id) {
        userCommentMapper.update(new LambdaUpdateWrapper<UserComment>()
                .eq(UserComment::getId, id)
                .eq(UserComment::getUserId, UserContext.getUserId())
                .set(UserComment::getStatus, StatusConstant.DISABLE)
        );
    }

    public Integer getNovelIdByCommentId(Integer id) {
        return userCommentMapper.selectById(id).getNovelId();
    }
}
