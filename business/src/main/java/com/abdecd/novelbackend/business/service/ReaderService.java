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

    public ReaderDetail getReaderDetail(Integer uid) {
        return readerDetailMapper.selectById(uid);
    }

    public void updateReaderDetail(UpdateReaderDetailDTO updateReaderDetailDTO) {
        var readerDetail = new ReaderDetail();
        BeanUtils.copyProperties(updateReaderDetailDTO, readerDetail);
        readerDetailMapper.updateById(readerDetail);
    }
}
