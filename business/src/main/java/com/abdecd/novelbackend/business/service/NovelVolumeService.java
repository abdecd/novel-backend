package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.common.util.SpringContextUtil;
import com.abdecd.novelbackend.business.mapper.NovelVolumeMapper;
import com.abdecd.novelbackend.business.pojo.dto.novel.volume.AddNovelVolumeDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.volume.DeleteNovelVolumeDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.volume.UpdateNovelVolumeDTO;
import com.abdecd.novelbackend.business.pojo.entity.NovelVolume;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class NovelVolumeService {
    @Autowired
    private NovelVolumeMapper novelVolumeMapper;
    @Autowired
    private NovelChapterService novelChapterService;

    @Cacheable(value = "novelVolumeList", key = "#nid", unless="#result.isEmpty()")
    public List<NovelVolume> listNovelVolume(Integer nid) {
        return novelVolumeMapper.selectList(new LambdaQueryWrapper<NovelVolume>()
                .eq(NovelVolume::getNovelId, nid)
        );
    }

    public NovelVolume getNovelVolume(Integer nid, Integer vNum) {
        var novelVolumeService = SpringContextUtil.getBean(NovelVolumeService.class);
        for (NovelVolume novelVolume : novelVolumeService.listNovelVolume(nid)) {
            if (Objects.equals(novelVolume.getVolumeNumber(), vNum)) {
                return novelVolume;
            }
        }
        return null;
    }

    @CacheEvict(value = "novelVolumeList", key = "#addNovelVolumeDTO.novelId")
    public void addNovelVolume(AddNovelVolumeDTO addNovelVolumeDTO) {
        var novelVolume = new NovelVolume();
        BeanUtils.copyProperties(addNovelVolumeDTO, novelVolume);
        novelVolumeMapper.insert(novelVolume);
    }

    @CacheEvict(value = "novelVolumeList", key = "#updateNovelVolumeDTO.novelId")
    public void updateNovelVolume(UpdateNovelVolumeDTO updateNovelVolumeDTO) {
        novelVolumeMapper.update(new LambdaUpdateWrapper<NovelVolume>()
                .eq(NovelVolume::getNovelId, updateNovelVolumeDTO.getNovelId())
                .eq(NovelVolume::getVolumeNumber, updateNovelVolumeDTO.getVolumeNumber())
                .set(NovelVolume::getTitle, updateNovelVolumeDTO.getTitle())
        );
    }

    @CacheEvict(value = "novelVolumeList", key = "#deleteNovelVolumeDTO.novelId")
    @Transactional
    public void deleteNovelVolume(DeleteNovelVolumeDTO deleteNovelVolumeDTO) {
        // 章节内容手动删除
        var chapterList = novelChapterService.listNovelChapter(deleteNovelVolumeDTO.getNovelId(), deleteNovelVolumeDTO.getVolumeNumber());
        for (var chapter : chapterList) {
            novelChapterService.deleteNovelChapter(chapter.getId());
        }
        novelVolumeMapper.delete(new LambdaQueryWrapper<NovelVolume>()
                .eq(NovelVolume::getNovelId, deleteNovelVolumeDTO.getNovelId())
                .eq(NovelVolume::getVolumeNumber, deleteNovelVolumeDTO.getVolumeNumber())
        );
    }
}
