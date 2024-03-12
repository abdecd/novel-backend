package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.aspect.UseFileService;
import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.business.common.util.SpringContextUtil;
import com.abdecd.novelbackend.business.mapper.NovelAndTagsMapper;
import com.abdecd.novelbackend.business.mapper.ReaderDetailMapper;
import com.abdecd.novelbackend.business.mapper.ReaderFavoritesMapper;
import com.abdecd.novelbackend.business.mapper.ReaderHistoryMapper;
import com.abdecd.novelbackend.business.pojo.dto.reader.UpdateReaderDetailDTO;
import com.abdecd.novelbackend.business.pojo.entity.NovelAndTags;
import com.abdecd.novelbackend.business.pojo.entity.ReaderDetail;
import com.abdecd.novelbackend.business.pojo.entity.ReaderFavorites;
import com.abdecd.novelbackend.business.pojo.entity.ReaderHistory;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderFavoritesVO;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderHistoryVO;
import com.abdecd.novelbackend.common.constant.MessageConstant;
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
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReaderService {
    @Autowired
    private ReaderDetailMapper readerDetailMapper;
    @Autowired
    private ReaderFavoritesMapper readerFavoritesMapper;
    @Autowired
    private ReaderHistoryMapper readerHistoryMapper;
    @Autowired
    private NovelAndTagsMapper novelAndTagsMapper;
    @Autowired
    private RedisTemplate<String, ReaderHistoryVO> redisTemplate;

    public ReaderDetail getReaderDetail(Integer uid) {
        return readerDetailMapper.selectById(uid);
    }

    @UseFileService(value = "avatar", param = UpdateReaderDetailDTO.class)
    public void updateReaderDetail(UpdateReaderDetailDTO updateReaderDetailDTO) {
        var readerDetail = new ReaderDetail();
        BeanUtils.copyProperties(updateReaderDetailDTO, readerDetail);
        readerDetail.setUserId(UserContext.getUserId());
        readerDetailMapper.updateById(readerDetail);
    }

    public PageVO<ReaderFavoritesVO> pageReaderFavoritesVO(Integer uid, Integer page, Integer pageSize) {
        var readerService = SpringContextUtil.getBean(ReaderService.class);
        var novelService = SpringContextUtil.getBean(NovelService.class);
        var list = readerService.listReaderFavoritesVO(uid);
        var total = list.size();
        list = list.subList(Math.max(0, (page - 1) * pageSize), Math.min(list.size(), page * pageSize));
        var resultList = list.stream().parallel()
                .peek(vo -> {
                    var novelInfoVO = novelService.getNovelInfoVO(vo.getNovelId());
                    var recordId = vo.getId();
                    BeanUtils.copyProperties(novelInfoVO, vo);
                    vo.setNovelId(novelInfoVO.getId());
                    vo.setId(recordId);
                })
                .toList();
        return new PageVO<>(total, resultList);
    }

    @Cacheable(value = "listReaderFavoritesVO", key = "#userId")
    public List<ReaderFavoritesVO> listReaderFavoritesVO(Integer userId) {
        return readerFavoritesMapper.listReaderFavoritesVO(userId);
    }

    @CacheEvict(value = "listReaderFavoritesVO", key = "#userId")
    public void addReaderFavorites(Integer userId, int[] novelIdsRaw) {
        var novelIds = Arrays.stream(novelIdsRaw).boxed().toArray(Integer[]::new);
        var count = readerFavoritesMapper.selectCount(new LambdaQueryWrapper<ReaderFavorites>()
                .eq(ReaderFavorites::getUserId, userId)
                .in(ReaderFavorites::getNovelId, (Object[]) novelIds)
        );
        if (count > 0) throw new BaseException(MessageConstant.FAVORITES_EXIST);
        readerFavoritesMapper.insertBatch(userId, novelIds);
    }

    @CacheEvict(value = "listReaderFavoritesVO", key = "#userId")
    public void deleteReaderFavorites(Integer userId, int[] novelIdsRaw) {
        var novelIds = Arrays.stream(novelIdsRaw).boxed().toArray(Integer[]::new);
        readerFavoritesMapper.delete(new LambdaQueryWrapper<ReaderFavorites>()
                .eq(ReaderFavorites::getUserId, userId)
                .in(ReaderFavorites::getNovelId, (Object[]) novelIds)
        );
    }

    @Transactional
    public void saveReaderHistory(Integer userId, Integer novelId, Integer volumeNumber, Integer chapterNumber) {
        // 删掉同样的旧记录
        readerHistoryMapper.delete(new LambdaQueryWrapper<ReaderHistory>()
                .eq(ReaderHistory::getUserId, userId)
                .eq(ReaderHistory::getNovelId, novelId)
                .eq(ReaderHistory::getVolumeNumber, volumeNumber)
                .eq(ReaderHistory::getChapterNumber, chapterNumber)
        );
        // 插入新记录
        var newRecord = new ReaderHistory()
                .setUserId(userId)
                .setNovelId(novelId)
                .setVolumeNumber(volumeNumber)
                .setChapterNumber(chapterNumber)
                .setStatus(StatusConstant.ENABLE)
                .setTimestamp(LocalDateTime.now());
        readerHistoryMapper.insert(newRecord);
        // 更新redis
        List<ReaderHistoryVO> list = redisTemplate.opsForList().range(RedisConstant.READER_HISTORY + userId, 0, RedisConstant.READER_HISTORY_SIZE);
        redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<Object> execute(@Nonnull RedisOperations operations) throws DataAccessException {
                operations.multi();
                if (list != null) {
                    for (var readerHistoryVO : list) {
                        if (Objects.equals(readerHistoryVO.getNovelId(), novelId)
                                && Objects.equals(readerHistoryVO.getVolumeNumber(), volumeNumber)
                                && Objects.equals(readerHistoryVO.getChapterNumber(), chapterNumber)) {
                            operations.opsForList().remove(RedisConstant.READER_HISTORY + userId, 1, readerHistoryVO);
                            break;
                        }
                    }
                }
                operations.opsForList().leftPush(RedisConstant.READER_HISTORY + userId, readerHistoryMapper.getReaderHistoryVO(newRecord.getId()));
                operations.opsForList().trim(RedisConstant.READER_HISTORY + userId, 0, RedisConstant.READER_HISTORY_SIZE);
                return operations.exec();
            }
        });
    }

    public List<ReaderHistoryVO> listReaderHistoryVO(Integer uid, Long startId, Integer pageSize) {
        List<ReaderHistoryVO> list = redisTemplate.opsForList().range(RedisConstant.READER_HISTORY + uid, 0, RedisConstant.READER_HISTORY_SIZE);
        if (list != null) {
            // 不够就补
            if (list.size() < RedisConstant.READER_HISTORY_SIZE) {
                var willStartId = Optional.of(list).map(List::getLast).map(ReaderHistoryVO::getId).orElse(null);
                List<ReaderHistoryVO> tmpList;
                if (willStartId == null) {
                    tmpList = readerHistoryMapper.listReaderHistoryVO(uid, null, RedisConstant.READER_HISTORY_SIZE, StatusConstant.ENABLE);
                } else {
                    tmpList = readerHistoryMapper.listReaderHistoryVO(uid, willStartId, RedisConstant.READER_HISTORY_SIZE - list.size() + 1, StatusConstant.ENABLE);
                    tmpList.removeFirst();
                }
                if (!tmpList.isEmpty()) redisTemplate.opsForList().rightPushAll(RedisConstant.READER_HISTORY + uid, tmpList);
            }
            var startIndex = 0;
            if (startId != null) {
                for (var i = 0; i < list.size(); i++) {
                    if (Objects.equals(list.get(i).getId(), startId)) {
                        startIndex = i;
                        break;
                    }
                }
                if (startIndex == 0) startIndex = Integer.MAX_VALUE;
            }
            if (startIndex == Integer.MAX_VALUE)
                return readerHistoryMapper.listReaderHistoryVO(uid, startId, pageSize, StatusConstant.ENABLE);
            if (startIndex + pageSize <= list.size()) {
                return list.subList(startIndex, pageSize);
            } else {
                var remainList = readerHistoryMapper.listReaderHistoryVO(uid, list.getLast().getId(), pageSize - list.size() + startIndex + 1, StatusConstant.ENABLE);
                remainList.removeFirst();
                list.addAll(remainList);
                return list;
            }
        }
        return readerHistoryMapper.listReaderHistoryVO(uid, startId, pageSize, StatusConstant.ENABLE);
    }

    public List<ReaderHistoryVO> listReaderHistoryByNovel(Integer userId, Integer novelId, Long startId, Integer pageSize) {
        return readerHistoryMapper.listReaderHistoryByNovel(userId, novelId, startId, pageSize, StatusConstant.ENABLE);
    }

    public void deleteReaderHistory(Integer userId, int[] novelIdsRaw) {
        var novelIds = Arrays.stream(novelIdsRaw).boxed().toArray(Integer[]::new);
        readerHistoryMapper.update(new LambdaUpdateWrapper<ReaderHistory>()
                .eq(ReaderHistory::getUserId, userId)
                .in(ReaderHistory::getNovelId, (Object[]) novelIds)
                .set(ReaderHistory::getStatus, StatusConstant.DISABLE)
        );
        // 更新redis
        List<ReaderHistoryVO> list = redisTemplate.opsForList().range(RedisConstant.READER_HISTORY + userId, 0, RedisConstant.READER_HISTORY_SIZE);
        var novelIdsList = Arrays.asList(novelIds);
        redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<Object> execute(@Nonnull RedisOperations operations) throws DataAccessException {
                operations.multi();
                if (list != null) {
                    for (var readerHistoryVO : list) {
                        if (novelIdsList.contains(readerHistoryVO.getNovelId())) {
                            operations.opsForList().remove(RedisConstant.READER_HISTORY + userId, 0, readerHistoryVO);
                        }
                    }
                }
                return operations.exec();
            }
        });
    }

    @Cacheable(value = "readerFavoriteTagIds#1", key = "#userId")
    public List<Integer> getReaderFavoriteTagIds(Integer userId) {
        return readerHistoryMapper.getReaderFavoriteTagIds(userId);
    }

    @Cacheable(value = "getHotTagIds#32", key = "#startTime.toString() + ':' + #endTime.toString()")
    public List<Integer> getHotTagIds(LocalDateTime startTime, LocalDateTime endTime) {
        return readerHistoryMapper.getHotTagIds(startTime, endTime);
    }

    @Cacheable(value = "getNovelIdsByTagId", key = "#tagId", unless = "#result.isEmpty()")
    public List<Integer> getNovelIdsByTagId(int tagId) {
        return new ArrayList<>(novelAndTagsMapper.selectList(new LambdaQueryWrapper<NovelAndTags>()
                .eq(NovelAndTags::getTagId, tagId)
        ).stream().map(NovelAndTags::getNovelId).toList());
    }

    @Cacheable(value = "getTagIdsByNovelId", key = "#novelId", unless = "#result.isEmpty()")
    public List<Integer> getTagIdsByNovelId(int novelId) {
        return new ArrayList<>(novelAndTagsMapper.selectList(new LambdaQueryWrapper<NovelAndTags>()
                .eq(NovelAndTags::getNovelId, novelId)
        ).stream().map(NovelAndTags::getTagId).toList());
    }
}
