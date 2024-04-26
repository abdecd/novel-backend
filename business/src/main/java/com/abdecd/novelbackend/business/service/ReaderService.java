package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.aspect.UseFileService;
import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.business.common.util.SpringContextUtil;
import com.abdecd.novelbackend.business.mapper.ReaderDetailMapper;
import com.abdecd.novelbackend.business.mapper.ReaderHistoryMapper;
import com.abdecd.novelbackend.business.pojo.dto.reader.UpdateReaderDetailDTOWithUrl;
import com.abdecd.novelbackend.business.pojo.entity.ReaderDetail;
import com.abdecd.novelbackend.business.pojo.entity.ReaderHistory;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderFavoritesVO;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderHistoryVO;
import com.abdecd.novelbackend.business.service.lib.RateLimiter;
import com.abdecd.novelbackend.business.service.lib.ReaderHistorySaver;
import com.abdecd.novelbackend.common.constant.MessageConstant;
import com.abdecd.novelbackend.common.constant.RedisConstant;
import com.abdecd.novelbackend.common.constant.StatusConstant;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.tokenlogin.common.context.UserContext;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class ReaderService {
    @Autowired
    private ReaderDetailMapper readerDetailMapper;
    @Autowired
    private ReaderHistoryMapper readerHistoryMapper;
    @Autowired
    private RedisTemplate<String, ReaderHistoryVO> redisTemplate;
    @Autowired
    private RedisTemplate<String, LocalDateTime> redisTemplateForTime;
    @Autowired
    private StringRedisTemplate redisTemplateForInt;
    @Autowired
    private ReaderHistorySaver readerHistorySaver;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RateLimiter rateLimiter;

    public ReaderDetail getReaderDetail(Integer uid) {
        return readerDetailMapper.selectById(uid);
    }

    @UseFileService(value = "avatar", param = UpdateReaderDetailDTOWithUrl.class)
    public void updateReaderDetail(UpdateReaderDetailDTOWithUrl updateReaderDetailDTOWithUrl) {
        var readerDetail = new ReaderDetail();
        BeanUtils.copyProperties(updateReaderDetailDTOWithUrl, readerDetail);
        readerDetail.setUserId(UserContext.getUserId());
        readerDetailMapper.updateById(readerDetail);
    }

    // 以下注释内容为使用数据库的实现，现优化为使用redis的实现
//    public PageVO<ReaderFavoritesVO> pageReaderFavoritesVO(Integer uid, Integer page, Integer pageSize) {
//        var readerService = SpringContextUtil.getBean(ReaderService.class);
//        var novelService = SpringContextUtil.getBean(NovelService.class);
//        var list = readerService.listReaderFavoritesVO(uid);
//        var total = list.size();
//        list = list.subList(Math.max(0, (page - 1) * pageSize), Math.min(list.size(), page * pageSize));
//        var resultList = list.stream().parallel()
//                .peek(vo -> {
//                    var novelInfoVO = novelService.getNovelInfoVO(vo.getNovelId());
//                    var recordId = vo.getId();
//                    BeanUtils.copyProperties(novelInfoVO, vo);
//                    vo.setNovelId(novelInfoVO.getId());
//                    vo.setId(recordId);
//                })
//                .toList();
//        return new PageVO<>(total, resultList);
//    }

//    @Cacheable(value = "listReaderFavoritesVO", key = "#userId")
//    public List<ReaderFavoritesVO> listReaderFavoritesVO(Integer userId) {
//        return readerFavoritesMapper.listReaderFavoritesVO(userId);
//    }

//    @CacheEvict(value = "listReaderFavoritesVO", key = "#userId")
//    public void addReaderFavorites(Integer userId, int[] novelIdsRaw) {
//        var novelIds = Arrays.stream(novelIdsRaw).boxed().toArray(Integer[]::new);
//        var count = readerFavoritesMapper.selectCount(new LambdaQueryWrapper<ReaderFavorites>()
//                .eq(ReaderFavorites::getUserId, userId)
//                .in(ReaderFavorites::getNovelId, (Object[]) novelIds)
//        );
//        if (count > 0) throw new BaseException(MessageConstant.FAVORITES_EXIST);
//        readerFavoritesMapper.insertBatch(userId, novelIds);
//    }

//    @CacheEvict(value = "listReaderFavoritesVO", key = "#userId")
//    public void deleteReaderFavorites(Integer userId, int[] novelIdsRaw) {
//        var novelIds = Arrays.stream(novelIdsRaw).boxed().toArray(Integer[]::new);
//        readerFavoritesMapper.delete(new LambdaQueryWrapper<ReaderFavorites>()
//                .eq(ReaderFavorites::getUserId, userId)
//                .in(ReaderFavorites::getNovelId, (Object[]) novelIds)
//        );
//    }

    public PageVO<ReaderFavoritesVO> pageReaderFavoritesVO(Integer uid, Integer page, Integer pageSize) {
        var list = listReaderFavoritesVO(uid);
        var total = list.size();
        list = list.subList(Math.max(0, (page - 1) * pageSize), Math.min(list.size(), page * pageSize));
        return new PageVO<>(total, list);
    }

    public List<ReaderFavoritesVO> listReaderFavoritesVO(Integer userId) {
        var list = redisTemplateForInt.opsForList().range(RedisConstant.READER_FAVORITES + userId, 0, RedisConstant.READER_FAVORITES_SIZE);
        if (list == null) return new ArrayList<>();
        var novelService = SpringContextUtil.getBean(NovelService.class);
        return list.stream().parallel()
                .map(idStr -> {
                    var id = Integer.parseInt(idStr);
                    var vo = new ReaderFavoritesVO();
                    BeanUtils.copyProperties(novelService.getNovelInfoVO(id), vo);
                    vo.setNovelId(id);
                    return vo;
                })
                .toList();
    }

    /**
     * 最多添加 RedisConstant.READER_FAVORITES_SIZE 个收藏
     */
    public void addReaderFavorites(Integer userId, int[] novelIdsRaw) {
        var novelIds = Arrays.stream(novelIdsRaw).boxed().toArray(Integer[]::new);
        // 如果小说不存在
        var novelService = SpringContextUtil.getBean(NovelService.class);
        var allNovelIds = novelService.getNovelIds();
        for (var novelId : novelIds) {
            if (!allNovelIds.contains(novelId))
                throw new BaseException(MessageConstant.NOVEL_NOT_EXIST);
        }
        // 如果超过最大收藏数
        var list = redisTemplateForInt.opsForList().range(RedisConstant.READER_FAVORITES + userId, 0, RedisConstant.READER_FAVORITES_SIZE);
        var count = list == null ? 0L : list.size();
        if (novelIds.length + count > RedisConstant.READER_FAVORITES_SIZE)
            throw new BaseException(MessageConstant.FAVORITES_EXCEED_LIMIT);
        var needAdd = Arrays.stream(novelIds)
                .map(Object::toString)
                .filter(idStr -> list == null || !list.contains(idStr))
                .toArray(String[]::new);
        if (needAdd.length == 0) throw new BaseException(MessageConstant.FAVORITES_EXIST);
        redisTemplateForInt.opsForList().rightPushAll(RedisConstant.READER_FAVORITES + userId, needAdd);
    }

    public void deleteReaderFavorites(Integer userId, int[] novelIdsRaw) {
        for (int novelId : novelIdsRaw) {
            redisTemplateForInt.opsForList().remove(RedisConstant.READER_FAVORITES + userId, 0,novelId + "");
        }
    }

    public void saveReaderHistory(Integer userId, Integer novelId, Integer volumeNumber, Integer chapterNumber) {
        // 插入记录
        var newRecord = new ReaderHistory()
                .setUserId(userId)
                .setNovelId(novelId)
                .setVolumeNumber(volumeNumber)
                .setChapterNumber(chapterNumber)
                .setStatus(StatusConstant.ENABLE)
                .setTimestamp(LocalDateTime.now());
        if (!rateLimiter.isRateLimited(
                RedisConstant.READER_HISTORY_RECORD_LIMIT + userId,
                5,
                1,
                TimeUnit.SECONDS
        )) readerHistorySaver.addReaderHistory(newRecord);
        // 更新缓存
        addReaderHistoryCache(userId, newRecord);
        // 记录更新时间戳
        redisTemplateForTime.opsForValue().set(RedisConstant.READER_HISTORY_TIMESTAMP + userId, LocalDateTime.now());
    }

    /**
     * 最多返回 RedisConstant.READER_HISTORY_SIZE 条
     */
    public PageVO<ReaderHistoryVO> listReaderHistoryVO(Integer uid, Integer page, Integer pageSize) {
        List<ReaderHistoryVO> list = getReaderHistoryCache(uid);
        return new PageVO<>(list.size(), list.subList(Math.max(0, (page - 1) * pageSize), Math.min(list.size(), page * pageSize)));
    }

    /**
     * 默认小说章节数量不超过 RedisConstant.READER_HISTORY_SIZE
     */
    public List<ReaderHistoryVO> listReaderHistoryByNovel(Integer userId, Integer novelId, Integer page, Integer pageSize) {
        List<ReaderHistoryVO> list;
        if (page == 1 && pageSize == 1) {
            // 直接去缓存拿最新的
            list = redisTemplate.opsForList().range(RedisConstant.READER_HISTORY + userId, 0, RedisConstant.READER_HISTORY_SIZE);
            if (list != null) list = list.stream().filter(obj -> Objects.equals(obj.getNovelId(), novelId)).toList();
        } else {
            list = readerHistoryMapper.listReaderHistoryByNovel(
                    userId,
                    novelId,
                    LocalDateTime.now(),
                    RedisConstant.READER_HISTORY_SIZE,
                    StatusConstant.ENABLE
            );
        }
        if (list == null) return new ArrayList<>();
        return list.subList(
                Math.max(0, (page - 1) * pageSize),
                Math.min(list.size(), page * pageSize)
        );
    }

    public void deleteReaderHistory(Integer userId, int[] novelIdsRaw) {
        var novelIds = Arrays.stream(novelIdsRaw).boxed().toArray(Integer[]::new);
        readerHistoryMapper.update(new LambdaUpdateWrapper<ReaderHistory>()
                .eq(ReaderHistory::getUserId, userId)
                .in(ReaderHistory::getNovelId, (Object[]) novelIds)
                .set(ReaderHistory::getStatus, StatusConstant.DISABLE)
        );
        // 删除缓存
        removeReaderHistoryCache(userId, novelIds);
        redisTemplateForTime.opsForValue().set(RedisConstant.READER_HISTORY_TIMESTAMP + userId, LocalDateTime.now());
    }

    private void addReaderHistoryCache(Integer userId, ReaderHistory newRecord) {
        var lock = redissonClient.getLock(RedisConstant.READER_HISTORY + userId + ":lock");
        lock.lock();
        try {
            // 移除旧的记录
            List<ReaderHistoryVO> list = redisTemplate.opsForList().range(RedisConstant.READER_HISTORY + userId, 0, RedisConstant.READER_HISTORY_SIZE);
            if (list != null) {
                for (var readerHistoryVO : list) {
                    if (Objects.equals(readerHistoryVO.getNovelId(), newRecord.getNovelId())) {
                        redisTemplate.opsForList().remove(RedisConstant.READER_HISTORY + userId, 0, readerHistoryVO);
                    }
                }
            }
            // 生成完整的记录
            var readerHistoryVO = new ReaderHistoryVO();
            readerHistoryVO.setNovelId(newRecord.getNovelId())
                    .setVolumeNumber(newRecord.getVolumeNumber())
                    .setChapterNumber(newRecord.getChapterNumber())
                    .setTimestamp(newRecord.getTimestamp());
            var novelService = SpringContextUtil.getBean(NovelService.class);
            var novelInfoVO = novelService.getNovelInfoVO(newRecord.getNovelId());
            readerHistoryVO.setNovelTitle(novelInfoVO.getTitle())
                    .setAuthor(novelInfoVO.getAuthor())
                    .setCover(novelInfoVO.getCover());
            var contents = novelService.getContents(newRecord.getNovelId());
            boolean isFound = false;
            for (var entry : contents.entrySet()) {
                if (Integer.parseInt(entry.getKey().substring(0, entry.getKey().indexOf("::"))) == newRecord.getVolumeNumber()) {
                    for (var chapter : entry.getValue()) {
                        if (Objects.equals(chapter.getChapterNumber(), newRecord.getChapterNumber())) {
                            readerHistoryVO.setChapterTitle(chapter.getTitle());
                            isFound = true;
                            break;
                        }
                    }
                    if (isFound) break;
                }
            }
            // 添加到缓存
            redisTemplate.opsForList().leftPush(
                    RedisConstant.READER_HISTORY + userId,
                    readerHistoryVO
            );
            redisTemplate.opsForList().trim(RedisConstant.READER_HISTORY + userId, 0, RedisConstant.READER_HISTORY_SIZE);
        } finally {
            lock.unlock();
        }
    }

    public List<ReaderHistoryVO> getReaderHistoryCache(Integer uid) {
        List<ReaderHistoryVO> list = redisTemplate.opsForList().range(RedisConstant.READER_HISTORY + uid, 0, RedisConstant.READER_HISTORY_SIZE);
        if (list == null) list = new ArrayList<>();
        return list;
    }

    private void removeReaderHistoryCache(Integer userId, Integer[] novelIds) {
        var lock = redissonClient.getLock(RedisConstant.READER_HISTORY + userId + ":lock");
        lock.lock();
        try {
            List<ReaderHistoryVO> list = redisTemplate.opsForList().range(RedisConstant.READER_HISTORY + userId, 0, RedisConstant.READER_HISTORY_SIZE);
            var novelIdsList = Arrays.asList(novelIds);

            if (list != null) {
                for (var readerHistoryVO : list) {
                    if (novelIdsList.contains(readerHistoryVO.getNovelId())) {
                        redisTemplate.opsForList().remove(RedisConstant.READER_HISTORY + userId, 0, readerHistoryVO);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
