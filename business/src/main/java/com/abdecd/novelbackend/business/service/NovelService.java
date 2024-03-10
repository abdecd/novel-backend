package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.aspect.UseFileService;
import com.abdecd.novelbackend.business.mapper.NovelAndTagsMapper;
import com.abdecd.novelbackend.business.mapper.NovelInfoMapper;
import com.abdecd.novelbackend.business.mapper.NovelTagsMapper;
import com.abdecd.novelbackend.business.pojo.dto.novel.AddNovelInfoDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.UpdateNovelInfoDTO;
import com.abdecd.novelbackend.business.pojo.entity.NovelAndTags;
import com.abdecd.novelbackend.business.pojo.entity.NovelInfo;
import com.abdecd.novelbackend.business.pojo.entity.NovelTags;
import com.abdecd.novelbackend.business.pojo.entity.NovelVolume;
import com.abdecd.novelbackend.business.pojo.vo.novel.NovelInfoVO;
import com.abdecd.novelbackend.business.pojo.vo.novel.contents.ContentsVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
public class NovelService {
    @Autowired
    private NovelInfoMapper novelInfoMapper;
    @Autowired
    private NovelVolumeService novelVolumeService;
    @Autowired
    private NovelChapterService novelChapterService;
    @Autowired
    private FileService fileService;
    @Autowired
    private ReaderService readerService;
    @Autowired
    private NovelAndTagsMapper novelAndTagsMapper;
    @Autowired
    private NovelTagsMapper novelTagsMapper;

    @Cacheable(value = "novelInfoVO", key = "#nid")
    public NovelInfoVO getNovelInfoVO(int nid) {
        var novelInfo = novelInfoMapper.selectById(nid);
        var tagIds = readerService.getTagIdsByNovelId(nid);
        var tags = novelTagsMapper.selectBatchIds(tagIds);
        var novelInfoVO = new NovelInfoVO();
        BeanUtils.copyProperties(novelInfo, novelInfoVO);
        novelInfoVO.setTags(tags);
        return novelInfoVO;
    }

    @Cacheable(value = "getNovelIds")
    public List<Integer> getNovelIds() {
        return new ArrayList<>(novelInfoMapper.selectList(new LambdaQueryWrapper<>())
                .stream().map(NovelInfo::getId)
                .toList());
    }

    @Caching(evict = {
            @CacheEvict(value = "getNovelIdsByTagId", allEntries = true),
            @CacheEvict(value = "novelInfoVO", key = "#updateNovelInfoDTO.id"),
            @CacheEvict(value = "getNovelIds"),
            @CacheEvict(value = "getTagIdsByNovelId", key = "#updateNovelInfoDTO.id")
    })
    @Transactional
    @UseFileService(value = "cover", param = UpdateNovelInfoDTO.class)
    public void updateNovelInfo(UpdateNovelInfoDTO updateNovelInfoDTO) {
        // 更新小说
        var novelInfo = new NovelInfo();
        BeanUtils.copyProperties(updateNovelInfoDTO, novelInfo);
        novelInfoMapper.updateById(novelInfo);
        // 更新tags
        if (updateNovelInfoDTO.getTagIds().length == 0) return;
        novelAndTagsMapper.delete(new LambdaQueryWrapper<NovelAndTags>()
                .eq(NovelAndTags::getNovelId, updateNovelInfoDTO.getId())
        );
        for (var tagId : updateNovelInfoDTO.getTagIds()) {
            novelAndTagsMapper.insert(new NovelAndTags()
                    .setNovelId(novelInfo.getId())
                    .setTagId(tagId)
            );
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "getNovelIdsByTagId", allEntries = true),
            @CacheEvict(value = "getNovelIds")
    })
    @Transactional
    @UseFileService(value = "cover", param = AddNovelInfoDTO.class)
    public Integer addNovelInfo(AddNovelInfoDTO addNovelInfoDTO) {
        // 插入小说
        var novelInfo = new NovelInfo();
        BeanUtils.copyProperties(addNovelInfoDTO, novelInfo);
        novelInfoMapper.insert(novelInfo);
        // 插入tags
        for (var tagId : addNovelInfoDTO.getTagIds()) {
            novelAndTagsMapper.insert(new NovelAndTags()
                    .setNovelId(novelInfo.getId())
                    .setTagId(tagId)
            );
        }
        return novelInfo.getId();
    }

    @Caching(evict = {
            @CacheEvict(value = "getNovelIdsByTagId", allEntries = true),
            @CacheEvict(value = "novelInfoVO", key = "#id"),
            @CacheEvict(value = "getNovelIds"),
            @CacheEvict(value = "getTagIdsByNovelId", key = "#id")
    })
    public void deleteNovelInfo(Integer id) {
        fileService.deleteImg(novelInfoMapper.selectById(id).getCover());
        novelInfoMapper.deleteById(id);
    }

    public ContentsVO getContents(Integer nid) {
        List<NovelVolume> novelVolume = novelVolumeService.listNovelVolume(nid);
        var contentsVO = new ContentsVO();
        for (var novelVolumeItem : novelVolume) {
            var vNum = novelVolumeItem.getVolumeNumber();
            var novelChapter = novelChapterService.listNovelChapter(nid, vNum);
            contentsVO.put(novelVolumeItem.getTitle(), novelChapter);
        }
        return contentsVO;
    }

    public List<NovelInfoVO> getRelatedList(Integer nid) {
        var tagIds = readerService.getTagIdsByNovelId(nid);
        HashSet<Integer> novelIdsSet = new HashSet<>();
        for (var tagId : tagIds) {
            novelIdsSet.addAll(readerService.getNovelIdsByTagId(tagId));// todo 重复的 有多个tag符合 应该提高权重
        }
        List<Integer> novelIds = new ArrayList<>(novelIdsSet);
        Collections.shuffle(novelIds);
        if (novelIds.size() >= 3) novelIds = novelIds.subList(0, 3);
        return novelIds
                .stream().parallel()
                .map(this::getNovelInfoVO)
                .toList();
    }

    @Cacheable(value = "getAvailableTags") // 不会过期
    public List<NovelTags> getAvailableTags() {
        return novelTagsMapper.selectList(new LambdaQueryWrapper<>());
    }
}
