package com.abdecd.novelbackend.business.mapper;

import com.abdecd.novelbackend.business.pojo.entity.NovelChapter;
import com.abdecd.novelbackend.business.pojo.vo.novel.chapter.NovelChapterVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NovelChapterMapper extends BaseMapper<NovelChapter> {
    NovelChapterVO getNovelChapterVO(Integer nid, Integer vNum, Integer cNum);
}
