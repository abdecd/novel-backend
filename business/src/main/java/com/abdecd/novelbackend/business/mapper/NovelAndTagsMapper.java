package com.abdecd.novelbackend.business.mapper;

import com.abdecd.novelbackend.business.pojo.entity.NovelInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NovelAndTagsMapper {
    List<NovelInfo> getRelatedList(Integer novelId);
}
