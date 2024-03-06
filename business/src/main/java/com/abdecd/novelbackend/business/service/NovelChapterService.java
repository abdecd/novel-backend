package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.mapper.NovelChapterMapper;
import com.abdecd.novelbackend.business.mapper.NovelContentMapper;
import com.abdecd.novelbackend.business.pojo.dto.novel.chapter.AddNovelChapterDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.chapter.DeleteNovelChapterDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.chapter.UpdateNovelChapterDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.chapter.UpdateNovelChapterWithCidDTO;
import com.abdecd.novelbackend.business.pojo.entity.NovelChapter;
import com.abdecd.novelbackend.business.pojo.entity.NovelContent;
import com.abdecd.novelbackend.business.pojo.vo.novel.chapter.NovelChapterVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NovelChapterService {
    @Autowired
    private NovelChapterMapper novelChapterMapper;
    @Autowired
    private NovelContentMapper novelContentMapper;
    public List<NovelChapter> listNovelChapter(Integer nid, Integer vNum) {
        return novelChapterMapper.selectList(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovelId, nid)
                .eq(NovelChapter::getVolumeNumber, vNum)
        );
    }

    public NovelChapterVO getNovelChapter(Integer nid, Integer vNum, Integer cNum) {
        return novelChapterMapper.getNovelChapterVO(nid, vNum, cNum);
    }

    public NovelChapterVO getNovelChapter(long cid) {
        return novelChapterMapper.getNovelChapterVOByCid(cid);
    }

    public long addNovelChapter(AddNovelChapterDTO addNovelChapterDTO) {
        var novelChapter = new NovelChapter();
        BeanUtils.copyProperties(addNovelChapterDTO, novelChapter);
        novelChapterMapper.insert(novelChapter);
        return novelChapter.getId();
    }

    @SuppressWarnings("DuplicatedCode")
    @Transactional
    public long updateNovelChapter(UpdateNovelChapterDTO updateNovelChapterDTO) {
        var novelChapter = novelChapterMapper.selectOne(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovelId, updateNovelChapterDTO.getNovelId())
                .eq(NovelChapter::getVolumeNumber, updateNovelChapterDTO.getVolumeNumber())
                .eq(NovelChapter::getChapterNumber, updateNovelChapterDTO.getChapterNumber())
        );
        var cid = novelChapter.getId();
        // 更新novelChapter
        BeanUtils.copyProperties(updateNovelChapterDTO, novelChapter);
        novelChapterMapper.updateById(novelChapter);
        // 更新novelContent
        novelContentMapper.update(new LambdaUpdateWrapper<NovelContent>()
                .eq(NovelContent::getNovelChapterId, cid)
                .set(NovelContent::getContent, updateNovelChapterDTO.getContent())
        );
        return cid;
    }

    @SuppressWarnings("DuplicatedCode")
    @Transactional
    public long updateNovelChapter(UpdateNovelChapterWithCidDTO updateNovelChapterWithCidDTO) {
        var novelChapter = novelChapterMapper.selectById(updateNovelChapterWithCidDTO.getId());
        var cid = updateNovelChapterWithCidDTO.getId();
        // 更新novelChapter
        BeanUtils.copyProperties(updateNovelChapterWithCidDTO, novelChapter);
        novelChapterMapper.updateById(novelChapter);
        // 更新novelContent
        novelContentMapper.update(new LambdaUpdateWrapper<NovelContent>()
                .eq(NovelContent::getNovelChapterId, cid)
                .set(NovelContent::getContent, updateNovelChapterWithCidDTO.getContent())
        );
        return cid;
    }

    @Transactional
    public void deleteNovelChapter(long id) {
        novelChapterMapper.deleteById(id);
        novelContentMapper.deleteById(id);
    }

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
