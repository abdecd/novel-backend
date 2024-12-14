package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.exceptionhandler.BaseException;
import com.abdecd.novelbackend.business.mapper.NovelChapterMapper;
import com.abdecd.novelbackend.business.pojo.dto.novel.chapter.AddNovelChapterDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.chapter.DeleteNovelChapterDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.chapter.UpdateNovelChapterDTO;
import com.abdecd.novelbackend.business.pojo.entity.NovelChapter;
import com.abdecd.novelbackend.business.pojo.vo.novel.chapter.NovelChapterVO;
import com.abdecd.novelbackend.business.lib.CacheByFrequency;
import com.abdecd.novelbackend.business.lib.CacheByFrequencyFactory;
import com.abdecd.novelbackend.common.constant.RedisConstant;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NovelChapterService {
    @Autowired
    private NovelChapterMapper novelChapterMapper;
    @Autowired
    private FileService fileService;
    private CacheByFrequency<NovelChapterVO> cacheNovelChapterByFrequency;

    @Autowired
    public void setCacheNovelChapterByFrequency(CacheByFrequencyFactory cacheNovelChapterByFrequencyFactory) {
        this.cacheNovelChapterByFrequency = cacheNovelChapterByFrequencyFactory.create(
                RedisConstant.NOVEL_DAILY_READ,
                100,
                86400
        );
    }

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
        return cacheNovelChapterByFrequency.get(nid + ":" + vNum + ":" + cNum, () -> {
            var entity = novelChapterMapper.getNovelChapter(nid, vNum, cNum);
            var vo = new NovelChapterVO();
            BeanUtils.copyProperties(entity, vo);
            // 获取内容
            try (var in = new BufferedReader(new InputStreamReader(fileService.getFileInSystem("/novel_data"
                    + "/" + entity.getNovelId()
                    + "/" + entity.getVolumeNumber()
                    + "/" + entity.getChapterNumber() + ".txt"
            ), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                in.lines().forEach(line -> sb.append(line).append("\n"));
                vo.setContent(sb.toString());
            } catch (Exception e) {
                throw new BaseException("文章获取出错");
            }
            return vo;
        }, (k) -> k.substring(0, k.indexOf(":")), 120960); // 1.4 天
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
        // 更新novelChapter
        var entity = updateNovelChapterDTO.toEntity();
        entity.setId(novelChapter.getId());
        novelChapterMapper.updateById(entity);
        // 更新novelContent
        if (updateNovelChapterDTO.getContent() != null) {
            try (ByteArrayInputStream inputStream
                         = new ByteArrayInputStream(updateNovelChapterDTO.getContent().getBytes())) {
                fileService.uploadFile(
                        inputStream,
                        "/novel_data"
                                + "/" + entity.getNovelId()
                                + "/" + entity.getVolumeNumber(),
                        entity.getChapterNumber() + ".txt"
                );// todo 更新的时候出现异常 然后文件没了
            } catch (Exception e) {
                throw new BaseException("更新失败");
            }
        }
        // 刷新缓存
        cacheNovelChapterByFrequency.delete(entity.getNovelId() + ":" + entity.getVolumeNumber() + ":" + entity.getChapterNumber());
    }

    @Transactional
    protected void deleteNovelChapterReally(NovelChapter chapter) {
        novelChapterMapper.deleteById(chapter.getId());
        fileService.deleteFileInSystem("/novel_data"
                + "/" + chapter.getNovelId()
                + "/" + chapter.getVolumeNumber()
                + "/" + chapter.getChapterNumber() + ".txt"
        );
        // 刷新缓存
        cacheNovelChapterByFrequency.delete(chapter.getNovelId() + ":" + chapter.getVolumeNumber() + ":" + chapter.getChapterNumber());
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
        deleteNovelChapterReally(novelChapter);
    }

    @Caching(evict = {
            @CacheEvict(value = "novelChapterList", key = "#chapter.novelId + ':' + #chapter.volumeNumber"),
            @CacheEvict(value = "getNovelChapterVOOnlyTimestamp", key = "#chapter.novelId + ':' + #chapter.volumeNumber + ':' + #chapter.chapterNumber")
    })
    @Transactional
    public void deleteNovelChapter(NovelChapter chapter) {
        deleteNovelChapterReally(chapter);
    }
}
