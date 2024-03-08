package com.abdecd.novelbackend.business.mapper;

import com.abdecd.novelbackend.business.pojo.entity.ReaderHistory;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderHistoryVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReaderHistoryMapper extends BaseMapper<ReaderHistory> {
    List<ReaderHistoryVO> listReaderHistoryVO(Integer userId, Integer startId, Integer pageSize, Byte enableStatus);
}
