package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.aspect.UseFileService;
import com.abdecd.novelbackend.business.mapper.NovelInfoMapper;
import com.abdecd.novelbackend.business.pojo.dto.novel.AddNovelInfoDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.UpdateNovelInfoDTO;
import com.abdecd.novelbackend.business.pojo.entity.NovelInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NovelService {
    @Autowired
    private NovelInfoMapper novelInfoMapper;
    @Autowired
    private FileService fileService;

    public NovelInfo getNovelInfo(int nid) {
        return novelInfoMapper.selectById(nid);
    }

    @UseFileService(value = "cover", param = UpdateNovelInfoDTO.class)
    public void updateNovelInfo(UpdateNovelInfoDTO updateNovelInfoDTO) {
        var novelInfo = new NovelInfo();
        BeanUtils.copyProperties(updateNovelInfoDTO, novelInfo);
        novelInfoMapper.updateById(novelInfo);
    }

    @UseFileService(value = "cover", param = AddNovelInfoDTO.class)
    public Integer addNovelInfo(AddNovelInfoDTO addNovelInfoDTO) {
        var novelInfo = new NovelInfo();
        BeanUtils.copyProperties(addNovelInfoDTO, novelInfo);
        novelInfoMapper.insert(novelInfo);
        return novelInfo.getId();
    }

    public void deleteNovelInfo(Integer id) {
        fileService.deleteImg(novelInfoMapper.selectById(id).getCover());
        novelInfoMapper.deleteById(id);
    }
}
