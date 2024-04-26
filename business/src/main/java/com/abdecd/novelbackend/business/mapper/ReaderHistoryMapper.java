package com.abdecd.novelbackend.business.mapper;

import com.abdecd.novelbackend.business.pojo.entity.ReaderHistory;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderHistoryVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReaderHistoryMapper extends BaseMapper<ReaderHistory> {

    List<ReaderHistoryVO> listReaderHistoryByNovel(Integer userId, Integer novelId, LocalDateTime minDateTime, LocalDateTime maxDateTime, Integer pageSize, Byte enableStatus);
    default List<ReaderHistoryVO> listReaderHistoryByNovel(Integer userId, Integer novelId, LocalDateTime maxDateTime, Integer pageSize, Byte enableStatus) {
        var minDateTime = maxDateTime == null ? null : maxDateTime.minusMonths(3);
        return listReaderHistoryByNovel(userId, novelId, minDateTime, maxDateTime, pageSize, enableStatus);
    }

    /**
     * 获取前100名
     */
    List<Integer> getRankList(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取前100名
     */
    List<Integer> getRankListByTagName(String tagName, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 随机前100名
     */
    List<Integer> getRandomRankList();

    /**
     * 随机前100名
     */
    List<Integer> getRandomRankListByTagName(String tagName);

    List<Integer> getReaderFavoriteTagIds(Integer userId);

    /**
     * 前5个热门类型
     */
    List<Integer> getHotTagIds(LocalDateTime startTime, LocalDateTime endTime);
}
