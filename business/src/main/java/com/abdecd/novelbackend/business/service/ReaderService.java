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
import com.abdecd.novelbackend.common.constant.StatusConstant;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.tokenlogin.common.context.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        readerHistoryMapper.insert(new ReaderHistory()
                .setUserId(userId)
                .setNovelId(novelId)
                .setVolumeNumber(volumeNumber)
                .setChapterNumber(chapterNumber)
                .setStatus(StatusConstant.ENABLE)
                .setTimestamp(LocalDateTime.now())
        );
    }

    public List<ReaderHistoryVO> listReaderHistoryVO(Integer uid, Long startId, Integer pageSize) {
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
