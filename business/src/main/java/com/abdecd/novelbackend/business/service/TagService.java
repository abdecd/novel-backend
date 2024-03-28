package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.mapper.NovelAndTagsMapper;
import com.abdecd.novelbackend.business.pojo.entity.NovelAndTags;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TagService {
    @Autowired
    private NovelAndTagsMapper novelAndTagsMapper;

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
