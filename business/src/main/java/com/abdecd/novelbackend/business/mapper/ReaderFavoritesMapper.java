package com.abdecd.novelbackend.business.mapper;

import com.abdecd.novelbackend.business.pojo.entity.ReaderFavorites;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderFavoritesVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReaderFavoritesMapper extends BaseMapper<ReaderFavorites> {
    List<ReaderFavoritesVO> listReaderFavoritesVO(Integer userId, Integer startId, Integer pageSize);

    void insertBatch(Integer userId, Integer[] novelIds);
}
