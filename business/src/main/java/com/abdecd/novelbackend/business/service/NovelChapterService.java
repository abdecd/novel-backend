package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.mapper.NovelChapterMapper;
import com.abdecd.novelbackend.business.mapper.NovelContentMapper;
import com.abdecd.novelbackend.business.pojo.dto.novel.chapter.AddNovelChapterDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.chapter.DeleteNovelChapterDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.chapter.UpdateNovelChapterDTO;
import com.abdecd.novelbackend.business.pojo.entity.NovelChapter;
import com.abdecd.novelbackend.business.pojo.entity.NovelContent;
import com.abdecd.novelbackend.business.pojo.vo.novel.chapter.NovelChapterVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NovelChapterService {
    @Autowired
    private NovelChapterMapper novelChapterMapper;
    @Autowired
    private NovelContentMapper novelContentMapper;

    @Cacheable(value = "novelChapterList", key = "#nid + ':' + #vNum", unless="#result.isEmpty()")
    public List<NovelChapter> listNovelChapter(Integer nid, Integer vNum) {
        return novelChapterMapper.selectList(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovelId, nid)
                .eq(NovelChapter::getVolumeNumber, vNum)
        );
    }

    @Cacheable(value = "getNovelChapterVOOnlyTimestamp", key = "#nid + ':' + #vNum + ':' + #cNum", unless="#result == null")
    public LocalDateTime getNovelChapterVOOnlyTimestamp(Integer nid, Integer vNum, Integer cNum) {
        var novelChapterVO = novelChapterMapper.getNovelChapterVOOnlyTimestamp(nid, vNum, cNum);
        if (novelChapterVO == null) {
            return null;
        } else {
            return novelChapterVO.getTimestamp();
        }
    }

    public NovelChapterVO getNovelChapterVO(Integer nid, Integer vNum, Integer cNum) {
        return novelChapterMapper.getNovelChapterVO(nid, vNum, cNum);
    }

    @CacheEvict(value = "novelChapterList", key = "#addNovelChapterDTO.novelId + ':' + #addNovelChapterDTO.volumeNumber")
    public long addNovelChapter(AddNovelChapterDTO addNovelChapterDTO) {
        var entity = addNovelChapterDTO.toEntity();
        novelChapterMapper.insert(entity);
        return entity.getId();
    }

    @Caching(evict = {
            @CacheEvict(value = "novelChapterList", key = "#updateNovelChapterDTO.novelId + ':' + #updateNovelChapterDTO.volumeNumber"),
            @CacheEvict(value = "getNovelChapterVOOnlyTimestamp", key = "#updateNovelChapterDTO.novelId + ':' + #updateNovelChapterDTO.volumeNumber + ':' + #updateNovelChapterDTO.chapterNumber")
    })
    @Transactional
    public void updateNovelChapter(UpdateNovelChapterDTO updateNovelChapterDTO) {
        var novelChapter = novelChapterMapper.selectOne(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovelId, updateNovelChapterDTO.getNovelId())
                .eq(NovelChapter::getVolumeNumber, updateNovelChapterDTO.getVolumeNumber())
                .eq(NovelChapter::getChapterNumber, updateNovelChapterDTO.getChapterNumber())
        );
        if (novelChapter == null) return;
        var cid = novelChapter.getId();
        // 更新novelChapter
        var entity = updateNovelChapterDTO.toEntity();
        novelChapterMapper.updateById(entity);
        // 更新novelContent
        if (updateNovelChapterDTO.getContent() != null) {
            novelContentMapper.delete(new LambdaQueryWrapper<NovelContent>()
                    .eq(NovelContent::getNovelChapterId, cid)
            );
            novelContentMapper.insert(new NovelContent()
                    .setNovelChapterId(cid)
                    .setContent(updateNovelChapterDTO.getContent())
            );
        }
    }

    @Transactional
    public void deleteNovelChapter(long id) {
        novelChapterMapper.deleteById(id);
        novelContentMapper.deleteById(id);
    }

    @Caching(evict = {
            @CacheEvict(value = "novelChapterList", key = "#deleteNovelChapterDTO.novelId + ':' + #deleteNovelChapterDTO.volumeNumber"),
            @CacheEvict(value = "getNovelChapterVOOnlyTimestamp", key = "#deleteNovelChapterDTO.novelId + ':' + #deleteNovelChapterDTO.volumeNumber + ':' + #deleteNovelChapterDTO.chapterNumber")
    })
    @Transactional
    public void deleteNovelChapter(DeleteNovelChapterDTO deleteNovelChapterDTO) {
        var novelChapter = novelChapterMapper.selectOne(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovelId, deleteNovelChapterDTO.getNovelId())
                .eq(NovelChapter::getVolumeNumber, deleteNovelChapterDTO.getVolumeNumber())
                .eq(NovelChapter::getChapterNumber, deleteNovelChapterDTO.getChapterNumber())
        );
        if (novelChapter == null) return;
        deleteNovelChapter(novelChapter.getId());
    }
}
