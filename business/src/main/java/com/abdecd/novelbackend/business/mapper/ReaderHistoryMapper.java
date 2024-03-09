package com.abdecd.novelbackend.business.mapper;

import com.abdecd.novelbackend.business.pojo.entity.NovelInfo;
import com.abdecd.novelbackend.business.pojo.entity.ReaderHistory;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderHistoryVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReaderHistoryMapper extends BaseMapper<ReaderHistory> {
    List<ReaderHistoryVO> listReaderHistoryVO(Integer userId, Integer startId, Integer pageSize, Byte enableStatus);

    ReaderHistoryVO getReaderHistoryByNovel(Integer userId, Integer novelId);

    /**
     * 获取前100名
     */
    List<NovelInfo> getRankList(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取前100名
     */
    List<NovelInfo> getRankListByTagName(String tagName, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 随机前100名
     */
    List<NovelInfo> getRandomRankList();

    /**
     * 随机前100名
     */
    List<NovelInfo> getRandomRankListByTagName(String tagName);

    List<Integer> getReaderFavoriteTagIds(Integer userId);
}
