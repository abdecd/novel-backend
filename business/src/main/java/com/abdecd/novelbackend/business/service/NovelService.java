package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.aspect.UseFileService;
import com.abdecd.novelbackend.business.exceptionhandler.BaseException;
import com.abdecd.novelbackend.business.common.util.SpringContextUtil;
import com.abdecd.novelbackend.business.mapper.NovelAndTagsMapper;
import com.abdecd.novelbackend.business.mapper.NovelInfoMapper;
import com.abdecd.novelbackend.business.mapper.NovelTagsMapper;
import com.abdecd.novelbackend.business.pojo.dto.novel.AddNovelInfoDTOWithUrl;
import com.abdecd.novelbackend.business.pojo.dto.novel.UpdateNovelInfoDTOWithUrl;
import com.abdecd.novelbackend.business.pojo.dto.novel.volume.DeleteNovelVolumeDTO;
import com.abdecd.novelbackend.business.pojo.entity.NovelAndTags;
import com.abdecd.novelbackend.business.pojo.entity.NovelInfo;
import com.abdecd.novelbackend.business.pojo.entity.NovelTags;
import com.abdecd.novelbackend.business.pojo.entity.NovelVolume;
import com.abdecd.novelbackend.business.pojo.vo.novel.NovelInfoVO;
import com.abdecd.novelbackend.business.pojo.vo.novel.contents.ContentsVO;
import com.abdecd.novelbackend.business.service.search.SearchService;
import com.abdecd.novelbackend.common.result.PageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
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
    private TagService tagService;
    @Autowired
    private NovelAndTagsMapper novelAndTagsMapper;
    @Autowired
    private NovelTagsMapper novelTagsMapper;
    @Autowired
    private SearchService searchService;
    private static final Executor saveToESExecutor =
            Executors.newVirtualThreadPerTaskExecutor();

    @Cacheable(value = "novelInfoVO", key = "#nid", unless = "#result == null")
    public NovelInfoVO getNovelInfoVO(int nid) {
        var novelInfo = novelInfoMapper.selectById(nid);
        if (novelInfo == null) return null;

        var tagIds = tagService.getTagIdsByNovelId(nid);
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
            @CacheEvict(value = "novelInfoVO", key = "#updateNovelInfoDTOWithUrl.id"),
            @CacheEvict(value = "getTagIdsByNovelId", key = "#updateNovelInfoDTOWithUrl.id"),
            @CacheEvict(value = "getHotTagIds#32", allEntries = true)
    })
    @Transactional
    @UseFileService(
            value = "cover",
            param = UpdateNovelInfoDTOWithUrl.class,
            folder = "'/novel_data/' + #updateNovelInfoDTOWithUrl.id"
    )
    public void updateNovelInfo(UpdateNovelInfoDTOWithUrl updateNovelInfoDTOWithUrl) {
        var novelInfo = novelInfoMapper.selectById(updateNovelInfoDTOWithUrl.getId());
        if (novelInfo == null) throw new BaseException("更新失败");

        // 更新小说
        var oldCover = updateNovelInfoDTOWithUrl.getCover() == null ? null : novelInfo.getCover();
        BeanUtils.copyProperties(updateNovelInfoDTOWithUrl, novelInfo);
        novelInfoMapper.updateById(novelInfo);
        // 更新tags 必须有才更
        if (updateNovelInfoDTOWithUrl.getTagIds() != null && updateNovelInfoDTOWithUrl.getTagIds().length != 0) {
            novelAndTagsMapper.delete(new LambdaQueryWrapper<NovelAndTags>()
                    .eq(NovelAndTags::getNovelId, updateNovelInfoDTOWithUrl.getId())
            );
            for (var tagId : updateNovelInfoDTOWithUrl.getTagIds()) {
                novelAndTagsMapper.insert(new NovelAndTags()
                        .setNovelId(novelInfo.getId())
                        .setTagId(tagId)
                );
            }
        }
        if (oldCover != null && !oldCover.equals(updateNovelInfoDTOWithUrl.getCover())) {
            fileService.deleteFile(oldCover);
        }
        saveNovelToElasticSearch(novelInfo);
    }

    @Caching(evict = {
            @CacheEvict(value = "getNovelIdsByTagId", allEntries = true),
            @CacheEvict(value = "novelInfoVO", key = "#result"),
            @CacheEvict(value = "getNovelIds", allEntries = true)
    })
    @Transactional
    public Integer addNovelInfo(AddNovelInfoDTOWithUrl addNovelInfoDTOWithUrl) {
        // 插入小说
        var novelInfo = new NovelInfo();
        BeanUtils.copyProperties(addNovelInfoDTOWithUrl, novelInfo);
        novelInfoMapper.insert(novelInfo);
        // 插入tags
        for (var tagId : addNovelInfoDTOWithUrl.getTagIds()) {
            novelAndTagsMapper.insert(new NovelAndTags()
                    .setNovelId(novelInfo.getId())
                    .setTagId(tagId)
            );
        }
        // 更新图片
        var novelService = SpringContextUtil.getBean(NovelService.class);
        var updateNovelInfoDTOWithUrl = new UpdateNovelInfoDTOWithUrl();
        updateNovelInfoDTOWithUrl.setId(novelInfo.getId());
        updateNovelInfoDTOWithUrl.setCover(novelInfo.getCover());
        novelService.updateNovelInfo(updateNovelInfoDTOWithUrl);

        saveNovelToElasticSearch(novelInfo);
        return novelInfo.getId();
    }

    private void saveNovelToElasticSearch(NovelInfo novelInfo) {
        var novelService = SpringContextUtil.getBean(NovelService.class);
        saveToESExecutor.execute(() -> {
            try {
                // 等小说缓存清掉
                Thread.sleep(1000); // todo 可能有更好的实现
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                searchService.saveSearchNovelEntity(novelService.getNovelInfoVO(novelInfo.getId()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Transactional
    public void deleteNovelInfo(Integer id) {
        var novelInfo = novelInfoMapper.selectById(id);
        if (novelInfo == null) return;
        var novelService = SpringContextUtil.getBean(NovelService.class);
        novelService.deleteNovelInfoReally(novelInfo);
        try {
            searchService.deleteSearchNovelEntity(id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "getNovelIdsByTagId", allEntries = true),
            @CacheEvict(value = "novelInfoVO", key = "#novelInfo.id"),
            @CacheEvict(value = "getNovelIds", allEntries = true),
            @CacheEvict(value = "getTagIdsByNovelId", key = "#novelInfo.id"),
            @CacheEvict(value = "novelRankList#32", allEntries = true),
            @CacheEvict(value = "novelRankListByTagName#32", allEntries = true),
            @CacheEvict(value = "getHotTagIds#32", allEntries = true)
    })
    @Transactional
    public void deleteNovelInfoReally(NovelInfo novelInfo) {
        fileService.deleteFile(novelInfo.getCover());
        // 章节内容手动删除
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
        if (novelVolume.isEmpty()) return null;
        var contentsVO = new ContentsVO();
        for (var novelVolumeItem : novelVolume) {
            var vNum = novelVolumeItem.getVolumeNumber();
            var novelChapter = novelChapterService.listNovelChapter(nid, vNum);
            contentsVO.put(novelVolumeItem.getVolumeNumber() + "::" + novelVolumeItem.getTitle(), novelChapter);
        }
        return contentsVO;
    }

    public List<NovelInfoVO> getRelatedList(Integer nid, Integer num) {
        var tagIds = tagService.getTagIdsByNovelId(nid);
        if (tagIds.isEmpty()) return new ArrayList<>();

        List<Integer> maxList = new ArrayList<>();
        var maxTagId = -1;
        for (Integer tagId : tagIds) {
            var tmpList = tagService.getNovelIdsByTagId(tagId);
            if (maxList.size() < tmpList.size()) {
                maxList = tmpList;
                maxTagId = tagId;
            }
        }
        if (maxList.size() <= num) return maxList.stream().parallel()
                .map(this::getNovelInfoVO)
                .toList();
        List<Integer> novelIdsList = new ArrayList<>(maxList);
        for (var tagId : tagIds) {
            if (tagId == maxTagId) continue;
            if (Math.random() > 0.5) continue;
            var tmp = tagService.getNovelIdsByTagId(tagId);
            var willBeNew = novelIdsList.stream().parallel().filter(tmp::contains).toList();
            if (willBeNew.size() < num) continue;
            novelIdsList = willBeNew;
        }
        List<Integer> novelIds = new ArrayList<>(novelIdsList);
        novelIds.remove(nid);
        Collections.shuffle(novelIds);

        if (novelIds.size() >= num) novelIds = novelIds.subList(0, num);
        return novelIds
                .stream().parallel()
                .map(this::getNovelInfoVO)
                .toList();
    }

    @Cacheable(value = "getAvailableTags") // 不会过期
    public List<NovelTags> getAvailableTags() {
        return novelTagsMapper.selectList(new LambdaQueryWrapper<NovelTags>()
                .orderByAsc(NovelTags::getId));
    }

    public PageVO<NovelInfoVO> getNovelInfoVOByTagIds(int[] tagIds, Integer page, Integer pageSize) {
        List<Integer> novelIdsList = new ArrayList<>(tagService.getNovelIdsByTagId(tagIds[0]));
        for (int i = 1; i < tagIds.length; i++) {
            var tmp = tagService.getNovelIdsByTagId(tagIds[i]);
            novelIdsList = novelIdsList.stream().parallel().filter(tmp::contains).toList();
        }
        var novelService = SpringContextUtil.getBean(NovelService.class);
        return new PageVO<NovelInfoVO>()
                .setTotal(novelIdsList.size())
                .setRecords(novelIdsList.subList(Math.max(0, (page - 1) * pageSize), Math.min(novelIdsList.size(), page * pageSize)).stream().parallel()
                        .map(novelService::getNovelInfoVO)
                        .toList()
                );
    }

    public List<NovelTags> searchTags(String tagName) {
        var novelService = SpringContextUtil.getBean(NovelService.class);
        var tags = novelService.getAvailableTags();
        List<NovelTags> result = new ArrayList<>();
        for (var tag : tags) {
            if (tag.getTagName().contains(tagName)) {
                result.add(tag);
            }
        }
        return result;
    }
}
