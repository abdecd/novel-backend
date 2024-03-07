package com.abdecd.novelbackend.business.service;

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

    public void updateNovelInfo(UpdateNovelInfoDTO updateNovelInfoDTO) {
        // 图片转正
        String img;
        if ((img = updateNovelInfoDTO.getCover()) != null) {
            img = fileService.changeTmpImgToStatic(img);
            // 转正成功，换新链接
            if (!img.isEmpty()) updateNovelInfoDTO.setCover(img);
            else img = null;
        }
        var novelInfo = new NovelInfo();
        BeanUtils.copyProperties(updateNovelInfoDTO, novelInfo);
        try {
            novelInfoMapper.updateById(novelInfo);
        } catch (Exception e) {
            if (img != null) fileService.deleteImg(img);
            throw e;
        }
    }

    public Integer addNovelInfo(AddNovelInfoDTO addNovelInfoDTO) {
        // 图片转正
        String img;
        if ((img = addNovelInfoDTO.getCover()) != null) {
            img = fileService.changeTmpImgToStatic(img);
            // 转正成功，换新链接
            if (!img.isEmpty()) addNovelInfoDTO.setCover(img);
            else img = null;
        }
        var novelInfo = new NovelInfo();
        BeanUtils.copyProperties(addNovelInfoDTO, novelInfo);
        try {
            novelInfoMapper.insert(novelInfo);
        } catch (Exception e) {
            if (img != null) fileService.deleteImg(img);
            throw e;
        }
        return novelInfo.getId();
    }

    public void deleteNovelInfo(Integer id) {
        fileService.deleteImg(novelInfoMapper.selectById(id).getCover());
        novelInfoMapper.deleteById(id);
    }
}
