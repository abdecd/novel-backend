package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.aspect.UseFileService;
import com.abdecd.novelbackend.business.mapper.NovelAndTagsMapper;
import com.abdecd.novelbackend.business.mapper.ReaderDetailMapper;
import com.abdecd.novelbackend.business.mapper.ReaderFavoritesMapper;
import com.abdecd.novelbackend.business.mapper.ReaderHistoryMapper;
import com.abdecd.novelbackend.business.pojo.dto.reader.UpdateReaderDetailDTO;
import com.abdecd.novelbackend.business.pojo.entity.*;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderFavoritesVO;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderHistoryVO;
import com.abdecd.novelbackend.common.constant.StatusConstant;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.tokenlogin.common.context.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    public PageVO<ReaderFavoritesVO> pageReaderFavoritesVO(Integer uid, Integer startNovelId, Integer pageSize) {
        var total = readerFavoritesMapper.selectCount(new LambdaQueryWrapper<ReaderFavorites>()
                .eq(ReaderFavorites::getUserId, uid)
        );
        var list = readerFavoritesMapper.listReaderFavoritesVO(uid, startNovelId, pageSize);
        return new PageVO<>(Math.toIntExact(total), list);
    }

    public List<ReaderFavoritesVO> addReaderFavorites(Integer userId, Integer[] novelIds) {
        readerFavoritesMapper.insertBatch(userId, novelIds);
        return readerFavoritesMapper.getReaderFavoritesVO(userId, novelIds);
    }

    public void deleteReaderFavorites(Integer userId, Integer[] novelIds) {
        readerFavoritesMapper.delete(new LambdaQueryWrapper<ReaderFavorites>()
                .eq(ReaderFavorites::getUserId, userId)
                .in(ReaderFavorites::getNovelId, (Object[]) novelIds)
        );
    }

    public void saveReaderHistory(Integer userId, Integer novelId, Integer volumeNumber, Integer chapterNumber) {
        readerHistoryMapper.insert(new ReaderHistory()
                .setUserId(userId)
                .setNovelId(novelId)
                .setVolumeNumber(volumeNumber)
                .setChapterNumber(chapterNumber)
                .setStatus(StatusConstant.ENABLE)
                .setTimestamp(LocalDateTime.now())
        );
    }

    public List<ReaderHistoryVO> listReaderHistoryVO(Integer uid, Integer startId, Integer pageSize) {
        return readerHistoryMapper.listReaderHistoryVO(uid, startId, pageSize, StatusConstant.ENABLE);
    }

    public ReaderHistoryVO getReaderHistoryByNovel(Integer userId, Integer novelId) {
        return readerHistoryMapper.getReaderHistoryByNovel(userId, novelId);
    }

    public void deleteReaderHistory(Integer userId, Integer[] ids) {
        readerHistoryMapper.update(new LambdaUpdateWrapper<ReaderHistory>()
                .eq(ReaderHistory::getUserId, userId)
                .in(ReaderHistory::getId, (Object[]) ids)
                .set(ReaderHistory::getStatus, StatusConstant.DISABLE)
        );
    }

    @Cacheable(value = "novelRankList#32", key = "#startTime.toString() + ':' + #endTime.toString()")
    public List<NovelInfo> getRankList(LocalDateTime startTime, LocalDateTime endTime) {
        var list = readerHistoryMapper.getRankList(startTime, endTime);
        if (list.isEmpty()) list = readerHistoryMapper.getRandomRankList();
        return list;
    }

    @Cacheable(value = "novelRankListByTagName#32", key = "#tagName + ':' + #startTime.toString() + ':' + #endTime.toString()")
    public List<NovelInfo> getRankListByTagName(String tagName, LocalDateTime startTime, LocalDateTime endTime) {
        var list = readerHistoryMapper.getRankListByTagName(tagName, startTime, endTime);
        if (list.isEmpty()) list = readerHistoryMapper.getRandomRankListByTagName(tagName);
        return list;
    }

    @Cacheable(value = "readerFavoriteTagIds#1", key = "#userId")
    public List<Integer> getReaderFavoriteTagIds(Integer userId) {
        return readerHistoryMapper.getReaderFavoriteTagIds(userId);
    }

    @Cacheable(value = "getNovelIdsByTagId", key = "#tagId")
    public List<Integer> getNovelIdsByTagId(Integer tagId) {
         return new ArrayList<>(novelAndTagsMapper.selectList(new LambdaQueryWrapper<NovelAndTags>()
                 .eq(NovelAndTags::getTagId, tagId)
         ).stream().map(NovelAndTags::getNovelId).toList());
    }

    @Cacheable(value = "getTagIdsByNovelId", key = "#novelId")
    public List<Integer> getTagIdsByNovelId(Integer novelId) {
        return new ArrayList<>(novelAndTagsMapper.selectList(new LambdaQueryWrapper<NovelAndTags>()
                .eq(NovelAndTags::getNovelId, novelId)
        ).stream().map(NovelAndTags::getTagId).toList());
    }
}
