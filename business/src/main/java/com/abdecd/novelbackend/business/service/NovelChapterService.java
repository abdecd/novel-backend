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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NovelChapterService {
    @Autowired
    private NovelChapterMapper novelChapterMapper;
    @Autowired
    private NovelContentMapper novelContentMapper;

    @Cacheable(value = "novelChapterList", key = "#nid + ':' + #vNum")
    public List<NovelChapter> listNovelChapter(Integer nid, Integer vNum) {
        return novelChapterMapper.selectList(new LambdaQueryWrapper<NovelChapter>()
                .eq(NovelChapter::getNovelId, nid)
                .eq(NovelChapter::getVolumeNumber, vNum)
        );
    }

    public NovelChapterVO getNovelChapter(Integer nid, Integer vNum, Integer cNum) {
        return novelChapterMapper.getNovelChapterVO(nid, vNum, cNum);
    }

    @CacheEvict(value = "novelChapterList", key = "#addNovelChapterDTO.novelId + ':' + #addNovelChapterDTO.volumeNumber")
    public long addNovelChapter(AddNovelChapterDTO addNovelChapterDTO) {
        var novelChapter = new NovelChapter();
        BeanUtils.copyProperties(addNovelChapterDTO, novelChapter);
        novelChapterMapper.insert(novelChapter);
        return novelChapter.getId();
    }

    @CacheEvict(value = "novelChapterList", key = "#updateNovelChapterDTO.novelId + ':' + #updateNovelChapterDTO.volumeNumber")
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
        BeanUtils.copyProperties(updateNovelChapterDTO, novelChapter);
        novelChapterMapper.updateById(novelChapter);
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

    @CacheEvict(value = "novelChapterList", key = "#deleteNovelChapterDTO.novelId + ':' + #deleteNovelChapterDTO.volumeNumber")
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
