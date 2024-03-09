package com.abdecd.novelbackend.business.mapper;

import com.abdecd.novelbackend.business.pojo.entity.NovelInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NovelInfoMapper extends BaseMapper<NovelInfo> {
    List<NovelInfo> searchNovelInfoByTitle(String title, Long startId, Integer pageSize);

    List<NovelInfo> searchNovelInfoByAuthor(String author, Long startId, Integer pageSize);

    Integer countSearchNovelInfoByTitle(String title);

    Integer countSearchNovelInfoByAuthor(String author);
}
