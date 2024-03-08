package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.aspect.UseFileService;
import com.abdecd.novelbackend.business.mapper.ReaderDetailMapper;
import com.abdecd.novelbackend.business.mapper.ReaderFavoritesMapper;
import com.abdecd.novelbackend.business.mapper.ReaderHistoryMapper;
import com.abdecd.novelbackend.business.pojo.dto.reader.UpdateReaderDetailDTO;
import com.abdecd.novelbackend.business.pojo.entity.ReaderDetail;
import com.abdecd.novelbackend.business.pojo.entity.ReaderFavorites;
import com.abdecd.novelbackend.business.pojo.entity.ReaderHistory;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderFavoritesVO;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderHistoryVO;
import com.abdecd.novelbackend.common.constant.StatusConstant;
import com.abdecd.tokenlogin.common.context.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReaderService {
    @Autowired
    private ReaderDetailMapper readerDetailMapper;
    @Autowired
    private ReaderFavoritesMapper readerFavoritesMapper;
    @Autowired
    private ReaderHistoryMapper readerHistoryMapper;

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

    public List<ReaderFavoritesVO> listReaderFavoritesVO(Integer uid, Integer startNovelId, Integer pageSize) {
        return readerFavoritesMapper.listReaderFavoritesVO(uid, startNovelId, pageSize);
    }

    public List<ReaderFavoritesVO> addReaderFavorites(Integer userId, Integer[] novelIds) {
        readerFavoritesMapper.insertBatch(userId, novelIds);
        return readerFavoritesMapper.getReaderFavoritesVO(userId, novelIds);
    }

    public void deleteReaderFavorites(Integer userId, Integer[] novelIds) {
        readerFavoritesMapper.delete(new LambdaQueryWrapper<ReaderFavorites>()
                .eq(ReaderFavorites::getUserId, userId)
                .in(ReaderFavorites::getNovelId, (Object) novelIds)
        );
    }

    public void saveReaderHistory(Integer userId, Integer novelId, Integer volumeNumber, Integer chapterNumber) {
        // 先检查是否有该记录
        var readerHistory = readerHistoryMapper.selectOne(new LambdaQueryWrapper<ReaderHistory>()
                .eq(ReaderHistory::getUserId, userId)
                .eq(ReaderHistory::getNovelId, novelId)
        );
        if (readerHistory != null) {
            readerHistoryMapper.updateById(new ReaderHistory()
                    .setId(readerHistory.getId())
                    .setUserId(userId)
                    .setNovelId(novelId)
                    .setVolumeNumber(volumeNumber)
                    .setChapterNumber(chapterNumber)
                    .setStatus(StatusConstant.ENABLE)
            );
        } else {
            readerHistoryMapper.insert(new ReaderHistory()
                    .setUserId(userId)
                    .setNovelId(novelId)
                    .setVolumeNumber(volumeNumber)
                    .setChapterNumber(chapterNumber)
                    .setStatus(StatusConstant.ENABLE)
            );
        }
    }

    public List<ReaderHistoryVO> listReaderHistoryVO(Integer uid, Integer startNovelId, Integer pageSize) {
        return readerHistoryMapper.listReaderHistoryVO(uid, startNovelId, pageSize, StatusConstant.ENABLE);
    }
}
