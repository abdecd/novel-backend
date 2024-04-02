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
import com.abdecd.novelbackend.business.service.lib.CacheByFrequency;
import com.abdecd.novelbackend.common.constant.MessageConstant;
import com.abdecd.novelbackend.common.constant.RedisConstant;
import com.abdecd.novelbackend.common.constant.StatusConstant;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.tokenlogin.common.context.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    private RedisTemplate<String, List<ReaderHistoryVO>> redisTemplateHistoryList;
    @Autowired
    private RedisTemplate<String, Integer> redisTemplateForInt;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

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
        var set = redisTemplateForInt.opsForSet().members(RedisConstant.READER_FAVORITES + userId);
        if (set == null) return new ArrayList<>();
        var novelService = SpringContextUtil.getBean(NovelService.class);
        return set.stream().parallel()
                .map(id -> {
                    var vo = new ReaderFavoritesVO();
                    BeanUtils.copyProperties(novelService.getNovelInfoVO(id), vo);
                    vo.setNovelId(id);
                    return vo;
                })
                .toList();
    }

    /**
     * 最多添加 100 个收藏
     */
    public void addReaderFavorites(Integer userId, int[] novelIdsRaw) {
        var novelIds = Arrays.stream(novelIdsRaw).boxed().toArray(Integer[]::new);
        // 进行校验
        var novelService = SpringContextUtil.getBean(NovelService.class);
        var allNovelIds = novelService.getNovelIds();
        for (var novelId : novelIds) {
            if (!allNovelIds.contains(novelId))
                throw new BaseException(MessageConstant.NOVEL_NOT_EXIST);
        }
        var count = redisTemplateForInt.opsForSet().size(RedisConstant.READER_FAVORITES + userId);
        if (count == null) count = 0L;
        if (novelIds.length + count > 100)
            throw new BaseException(MessageConstant.FAVORITES_EXCEED_LIMIT);
        redisTemplateForInt.opsForSet().add(RedisConstant.READER_FAVORITES + userId, novelIds);
    }

    public void deleteReaderFavorites(Integer userId, int[] novelIdsRaw) {
        var novelIds = Arrays.stream(novelIdsRaw).boxed().toArray(Integer[]::new);
        redisTemplateForInt.opsForSet().remove(RedisConstant.READER_FAVORITES + userId, (Object[]) novelIds);
    }

    public void saveReaderHistory(Integer userId, Integer novelId, Integer volumeNumber, Integer chapterNumber) {
        // 获取旧记录
        var oldRecord = readerHistoryMapper.selectOne(new LambdaQueryWrapper<ReaderHistory>()
                .eq(ReaderHistory::getUserId, userId)
                .eq(ReaderHistory::getNovelId, novelId)
                .eq(ReaderHistory::getVolumeNumber, volumeNumber)
                .eq(ReaderHistory::getChapterNumber, chapterNumber)
        );
        // 插入或更新新记录
        var newRecord = new ReaderHistory()
                .setUserId(userId)
                .setNovelId(novelId)
                .setVolumeNumber(volumeNumber)
                .setChapterNumber(chapterNumber)
                .setStatus(StatusConstant.ENABLE)
                .setTimestamp(LocalDateTime.now());
        if (oldRecord != null) {
            newRecord.setId(oldRecord.getId());
            readerHistoryMapper.updateById(newRecord);
        } else {
            readerHistoryMapper.insert(newRecord);
        }
        // 更新缓存
        addReaderHistoryCache(userId, newRecord);
        var cacheHistoryForANovelByFrequency = new CacheByFrequency<>(
                redisTemplateHistoryList,
                stringRedisTemplate,
                redissonClient,
                RedisConstant.READER_HISTORY_A_NOVEL + ":" + userId,
                5,
                86400
        );
        cacheHistoryForANovelByFrequency.delete(novelId + "");// todo 更好的更新方式
        // 记录更新时间戳
        redisTemplateForTime.opsForValue().set(RedisConstant.READER_HISTORY_TIMESTAMP + userId, LocalDateTime.now());
        redisTemplateForTime.opsForValue().set(RedisConstant.READER_HISTORY_A_NOVEL_TIMESTAMP + userId + ':' + novelId, LocalDateTime.now());
    }

    @Deprecated
    public List<ReaderHistoryVO> listReaderHistoryVO(Integer uid, Long startId, Integer pageSize) {
        List<ReaderHistoryVO> list = getReaderHistoryCache(uid);
        // 计算 startIndex
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
            list = list.subList(startIndex, list.size());
            var remainList = readerHistoryMapper.listReaderHistoryVO(uid, list.getLast().getId(), pageSize - list.size() + startIndex + 1, StatusConstant.ENABLE);
            remainList.removeFirst();
            list.addAll(remainList);
            return list;
        }
    }

    /**
     * 最多返回 RedisConstant.READER_HISTORY_SIZE 条
     */
    public PageVO<ReaderHistoryVO> listReaderHistoryVO(Integer uid, Integer page, Integer pageSize) {
        List<ReaderHistoryVO> list = getReaderHistoryCache(uid);
        return new PageVO<>(list.size(), list.subList(Math.max(0, (page - 1) * pageSize), Math.min(list.size(), page * pageSize)));
    }

    /**
     * 默认小说章节数量不超过 1000
     */
    public List<ReaderHistoryVO> listReaderHistoryByNovel(Integer userId, Integer novelId, Integer page, Integer pageSize) {
        var cacheHistoryForANovelByFrequency = new CacheByFrequency<>(
                redisTemplateHistoryList,
                stringRedisTemplate,
                redissonClient,
                RedisConstant.READER_HISTORY_A_NOVEL + ":" + userId,
                5,
                86400
        );
        cacheHistoryForANovelByFrequency.recordFrequency(novelId + "");
        var list = cacheHistoryForANovelByFrequency.get(novelId + "", () -> readerHistoryMapper.listReaderHistoryByNovel(userId, novelId, null, 1000, StatusConstant.ENABLE), null, 172800); // 2 天
        if (list == null) return new ArrayList<>();
        return list.subList(Math.max(0, (page - 1) * pageSize), Math.min(list.size(), page * pageSize));
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
        var cacheHistoryForANovelByFrequency = new CacheByFrequency<>(
                redisTemplateHistoryList,
                stringRedisTemplate,
                redissonClient,
                RedisConstant.READER_HISTORY_A_NOVEL + ":" + userId,
                5,
                86400
        );
        redisTemplateForTime.opsForValue().set(RedisConstant.READER_HISTORY_TIMESTAMP + userId, LocalDateTime.now());
        var novelService = SpringContextUtil.getBean(NovelService.class);
        var allNovelIds = novelService.getNovelIds();
        for (var novelId : novelIds) if (allNovelIds.contains(novelId)) {
            redisTemplateForTime.opsForValue().set(RedisConstant.READER_HISTORY_A_NOVEL_TIMESTAMP + userId + ':' + novelId, LocalDateTime.now());
            cacheHistoryForANovelByFrequency.delete(novelId + "");
        }
    }

    private void addReaderHistoryCache(Integer userId, ReaderHistory newRecord) {
        var lock = redissonClient.getLock(RedisConstant.READER_HISTORY + userId + ":lock");
        lock.lock();
        List<ReaderHistoryVO> list = redisTemplate.opsForList().range(RedisConstant.READER_HISTORY + userId, 0, RedisConstant.READER_HISTORY_SIZE);
        if (list != null) {
            for (var readerHistoryVO : list) {
                if (Objects.equals(readerHistoryVO.getNovelId(), newRecord.getNovelId())) {
                    redisTemplate.opsForList().remove(RedisConstant.READER_HISTORY + userId, 0, readerHistoryVO);
                }
            }
        }
        redisTemplate.opsForList().leftPush(RedisConstant.READER_HISTORY + userId, readerHistoryMapper.getReaderHistoryVO(newRecord.getId()));
        redisTemplate.opsForList().trim(RedisConstant.READER_HISTORY + userId, 0, RedisConstant.READER_HISTORY_SIZE);
        // 如果有效最大数量存在则更新
        if (Boolean.TRUE.equals(redisTemplateForInt.hasKey(RedisConstant.READER_HISTORY_NOW_MAX_CNT + userId)))
            redisTemplateForInt.opsForValue().increment(RedisConstant.READER_HISTORY_NOW_MAX_CNT + userId);
        lock.unlock();
    }

    public List<ReaderHistoryVO> getReaderHistoryCache(Integer uid) {
        List<ReaderHistoryVO> list = redisTemplate.opsForList().range(RedisConstant.READER_HISTORY + uid, 0, RedisConstant.READER_HISTORY_SIZE);
        if (list == null) list = new ArrayList<>();
        var listSize = list.size();
        // 确实不足 RedisConstant.READER_HISTORY_SIZE 就不要去拿了
        var nowMaxCnt = redisTemplateForInt.opsForValue().get(RedisConstant.READER_HISTORY_NOW_MAX_CNT + uid);
        if (nowMaxCnt != null && nowMaxCnt <= RedisConstant.READER_HISTORY_SIZE) return list;
        if (listSize < RedisConstant.READER_HISTORY_SIZE) {
            // 数量不足则补充数据
            var lock = redissonClient.getLock(RedisConstant.READER_HISTORY + uid + ":lock");
            lock.lock();
            List<ReaderHistoryVO> currentList = redisTemplate.opsForList().range(RedisConstant.READER_HISTORY + uid, 0, RedisConstant.READER_HISTORY_SIZE);
            if (currentList != null && currentList.size() != listSize) return currentList;
            try {
                Long willStartId = null;
                if (!list.isEmpty()) willStartId = list.getLast().getId();
                List<ReaderHistoryVO> tmpList;
                if (willStartId == null) {
                    tmpList = readerHistoryMapper.listReaderHistoryVO(uid, null, RedisConstant.READER_HISTORY_SIZE, StatusConstant.ENABLE);
                } else {
                    var novelIdsNot = list.stream().map(ReaderHistoryVO::getNovelId).toArray(Integer[]::new);
                    // 已将重复列表排除，不用多拿一个
                    tmpList = readerHistoryMapper.listReaderHistoryVO(uid, willStartId, RedisConstant.READER_HISTORY_SIZE - list.size(), StatusConstant.ENABLE, novelIdsNot);
                }
                if (!tmpList.isEmpty()) redisTemplate.opsForList().rightPushAll(RedisConstant.READER_HISTORY + uid, tmpList);
                list.addAll(tmpList);
                // 记录当前有效最大数量
                if (list.size() < RedisConstant.READER_HISTORY_SIZE) redisTemplateForInt.opsForValue().set(RedisConstant.READER_HISTORY_NOW_MAX_CNT + uid, list.size());
            } finally {
                lock.unlock();
            }
        }
        return list;
    }

    private void removeReaderHistoryCache(Integer userId, Integer[] novelIds) {
        var lock = redissonClient.getLock(RedisConstant.READER_HISTORY + userId + ":lock");
        lock.lock();
        List<ReaderHistoryVO> list = redisTemplate.opsForList().range(RedisConstant.READER_HISTORY + userId, 0, RedisConstant.READER_HISTORY_SIZE);
        var novelIdsList = Arrays.asList(novelIds);

        if (list != null) {
            for (var readerHistoryVO : list) {
                if (novelIdsList.contains(readerHistoryVO.getNovelId())) {
                    redisTemplate.opsForList().remove(RedisConstant.READER_HISTORY + userId, 0, readerHistoryVO);
                }
            }
        }
        // 如果有效最大数量存在则更新
        if (Boolean.TRUE.equals(redisTemplateForInt.hasKey(RedisConstant.READER_HISTORY_NOW_MAX_CNT + userId)))
            redisTemplateForInt.opsForValue().decrement(RedisConstant.READER_HISTORY_NOW_MAX_CNT + userId);
        lock.unlock();
    }
}
