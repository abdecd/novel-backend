package com.abdecd.novelbackend.business.pojo.vo.novel;

import com.abdecd.novelbackend.business.pojo.entity.NovelTags;
import lombok.Data;

import java.util.List;

@Data
public class NovelInfoVO {
    private Integer id;
    private String title;
    private String author;
    private String cover;
    private String description;
    private List<NovelTags> tags;
}
