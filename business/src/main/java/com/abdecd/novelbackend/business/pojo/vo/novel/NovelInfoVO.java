package com.abdecd.novelbackend.business.pojo.vo.novel;

import lombok.Data;

@Data
public class NovelInfoVO {
    private Integer id;
    private String title;
    private String author;
    private String cover;
    private String description;
}
