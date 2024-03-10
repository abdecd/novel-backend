package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.aspect.UseFileService;
import com.abdecd.novelbackend.business.mapper.NovelAndTagsMapper;
import com.abdecd.novelbackend.business.mapper.NovelInfoMapper;
import com.abdecd.novelbackend.business.mapper.NovelTagsMapper;
import com.abdecd.novelbackend.business.pojo.dto.novel.AddNovelInfoDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.UpdateNovelInfoDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.volume.DeleteNovelVolumeDTO;
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

import java.util.*;

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
        if (novelInfo == null) return null;

        var tagIds = readerService.getTagIdsByNovelId(nid);
        List<NovelTags> tags = new ArrayList<>();
        if (tagIds != null) tags = novelTagsMapper.selectBatchIds(tagIds);

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
        // 更新tags 必须有才更
        if (updateNovelInfoDTO.getTagIds() == null || updateNovelInfoDTO.getTagIds().length == 0) return;
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
            @CacheEvict(value = "novelInfoVO", key = "#result"),
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

    @Transactional
    public void deleteNovelInfo(Integer id) {
        var novelInfo = novelInfoMapper.selectById(id);
        if (novelInfo == null) return;
        deleteNovelInfoReally(novelInfo);
    }

    @Caching(evict = {
            @CacheEvict(value = "getNovelIdsByTagId", allEntries = true),
            @CacheEvict(value = "novelInfoVO", key = "#novelInfo.id"),
            @CacheEvict(value = "getNovelIds"),
            @CacheEvict(value = "getTagIdsByNovelId", key = "#novelInfo.id"),
            @CacheEvict(value = "novelRankList", allEntries = true),
            @CacheEvict(value = "novelRankListByTagName", allEntries = true)
    })
    @Transactional
    public void deleteNovelInfoReally(NovelInfo novelInfo) {
        fileService.deleteImg(novelInfo.getCover());
        // 章节内容没有外键约束, 手动删除
        var volumeList = novelVolumeService.listNovelVolume(novelInfo.getId());
        for (var volume : volumeList) {
            var volumeNumber = volume.getVolumeNumber();
            novelVolumeService.deleteNovelVolume(new DeleteNovelVolumeDTO()
                    .setNovelId(novelInfo.getId())
                    .setVolumeNumber(volumeNumber)
            );
        }
        novelInfoMapper.deleteById(novelInfo.getId());
    }

    public ContentsVO getContents(Integer nid) {
        List<NovelVolume> novelVolume = novelVolumeService.listNovelVolume(nid);
        var contentsVO = new ContentsVO();
        for (var novelVolumeItem : novelVolume) {
            var vNum = novelVolumeItem.getVolumeNumber();
            var novelChapter = novelChapterService.listNovelChapter(nid, vNum);
            contentsVO.put(novelVolumeItem.getVolumeNumber() + "::" + novelVolumeItem.getTitle(), novelChapter);
        }
        return contentsVO;
    }

    public List<NovelInfoVO> getRelatedList(Integer nid) {
        var tagIds = readerService.getTagIdsByNovelId(nid);
        if (tagIds.isEmpty()) return new ArrayList<>();

        List<Integer> novelIdsList = new ArrayList<>(readerService.getNovelIdsByTagId(tagIds.getFirst()));
        for (int i = 1; i < tagIds.size(); i++) {
            var tmp = readerService.getNovelIdsByTagId(tagIds.get(i));
            novelIdsList = novelIdsList.stream().parallel().filter(tmp::contains).toList();
        }
        List<Integer> novelIds = new ArrayList<>(novelIdsList);
        novelIds.remove((Object) nid);
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
