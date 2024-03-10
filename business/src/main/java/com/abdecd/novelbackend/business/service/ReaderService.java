package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.aspect.UseFileService;
import com.abdecd.novelbackend.business.common.util.SpringContextUtil;
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

    public PageVO<ReaderFavoritesVO> pageReaderFavoritesVO(Integer uid, Integer startId, Integer pageSize) {
        var total = readerFavoritesMapper.selectCount(new LambdaQueryWrapper<ReaderFavorites>()
                .eq(ReaderFavorites::getUserId, uid)
        );
        var list = readerFavoritesMapper.listReaderFavoritesVO(uid, startId, pageSize);// todo 考虑缓存
        var novelService = SpringContextUtil.getBean(NovelService.class);
        var resultList = list.stream().parallel()
                .peek(vo -> {
                    var novelInfoVO = novelService.getNovelInfoVO(vo.getNovelId());
                    var recordId = vo.getId();
                    BeanUtils.copyProperties(novelInfoVO, vo);
                    vo.setNovelId(novelInfoVO.getId());
                    vo.setId(recordId);
                })
                .toList();
        return new PageVO<>(Math.toIntExact(total), resultList);
    }

    public void addReaderFavorites(Integer userId, Integer[] novelIds) {
        readerFavoritesMapper.insertBatch(userId, novelIds);
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

    public List<ReaderHistoryVO> listReaderHistoryVO(Integer uid, Long startId, Integer pageSize) {
        return readerHistoryMapper.listReaderHistoryVO(uid, startId, pageSize, StatusConstant.ENABLE);
    }

    public List<ReaderHistoryVO> listReaderHistoryByNovel(Integer userId, Integer novelId, Long startId, Integer pageSize) {
        return readerHistoryMapper.listReaderHistoryByNovel(userId, novelId, startId, pageSize, StatusConstant.ENABLE);
    }

    public void deleteReaderHistory(Integer userId, Long[] ids) {
        readerHistoryMapper.update(new LambdaUpdateWrapper<ReaderHistory>()
                .eq(ReaderHistory::getUserId, userId)
                .in(ReaderHistory::getId, (Object[]) ids)
                .set(ReaderHistory::getStatus, StatusConstant.DISABLE)
        );
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
