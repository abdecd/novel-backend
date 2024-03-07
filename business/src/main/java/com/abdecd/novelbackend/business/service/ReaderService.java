package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.mapper.ReaderDetailMapper;
import com.abdecd.novelbackend.business.pojo.dto.reader.UpdateReaderDetailDTO;
import com.abdecd.novelbackend.business.pojo.entity.ReaderDetail;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReaderService {
    @Autowired
    private ReaderDetailMapper readerDetailMapper;
    @Autowired
    private FileService fileService;

    public ReaderDetail getReaderDetail(Integer uid) {
        return readerDetailMapper.selectById(uid);
    }

    public void updateReaderDetail(UpdateReaderDetailDTO updateReaderDetailDTO) {
        // 图片转正
        String img;
        if ((img = updateReaderDetailDTO.getAvatar()) != null) {
            img = fileService.changeTmpImgToStatic(img);
            // 转正成功，换新链接
            if (!img.isEmpty()) updateReaderDetailDTO.setAvatar(img);
            else img = null;
        }
        var readerDetail = new ReaderDetail();
        BeanUtils.copyProperties(updateReaderDetailDTO, readerDetail);
        try {
            readerDetailMapper.updateById(readerDetail);
        } catch (Exception e) {
            if (img != null) fileService.deleteImg(img);
            throw e;
        }
    }
}
