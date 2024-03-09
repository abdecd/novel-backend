package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.mapper.NovelVolumeMapper;
import com.abdecd.novelbackend.business.pojo.dto.novel.volume.AddNovelVolumeDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.volume.DeleteNovelVolumeDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.volume.UpdateNovelVolumeDTO;
import com.abdecd.novelbackend.business.pojo.entity.NovelVolume;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NovelVolumeService {
    @Autowired
    private NovelVolumeMapper novelVolumeMapper;

    @Cacheable(value = "novelVolumeList", key = "#nid")
    public List<NovelVolume> listNovelVolume(Integer nid) {
        return novelVolumeMapper.selectList(new LambdaQueryWrapper<NovelVolume>()
                .eq(NovelVolume::getNovelId, nid)
        );
    }

    public NovelVolume getNovelVolume(Integer nid, Integer vNum) {
        return novelVolumeMapper.selectOne(new LambdaQueryWrapper<NovelVolume>()
                .eq(NovelVolume::getNovelId, nid)
                .eq(NovelVolume::getVolumeNumber, vNum)
        );
    }

    @CacheEvict(value = "novelVolumeList", key = "#addNovelVolumeDTO.novelId")
    public Integer addNovelVolume(AddNovelVolumeDTO addNovelVolumeDTO) {
        var novelVolume = new NovelVolume();
        BeanUtils.copyProperties(addNovelVolumeDTO, novelVolume);
        novelVolumeMapper.insert(novelVolume);
        return novelVolume.getNovelId();
    }

    @CacheEvict(value = "novelVolumeList", key = "#updateNovelVolumeDTO.novelId")
    public void updateNovelVolume(UpdateNovelVolumeDTO updateNovelVolumeDTO) {
        var novelVolume = new NovelVolume();
        BeanUtils.copyProperties(updateNovelVolumeDTO, novelVolume);
        novelVolumeMapper.updateById(novelVolume);
    }

    @CacheEvict(value = "novelVolumeList", key = "#deleteNovelVolumeDTO.novelId")
    public void deleteNovelVolume(DeleteNovelVolumeDTO deleteNovelVolumeDTO) {
        novelVolumeMapper.delete(new LambdaQueryWrapper<NovelVolume>()
                .eq(NovelVolume::getNovelId, deleteNovelVolumeDTO.getNovelId())
                .eq(NovelVolume::getVolumeNumber, deleteNovelVolumeDTO.getVolumeNumber())
        );
    }
}
