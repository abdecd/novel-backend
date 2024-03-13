package com.abdecd.novelbackend.business.pojo.vo.novel;

import com.abdecd.novelbackend.business.pojo.entity.NovelTags;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HotNovelVO {
    private NovelTags tag;
    private List<NovelInfoVO> list;
}
