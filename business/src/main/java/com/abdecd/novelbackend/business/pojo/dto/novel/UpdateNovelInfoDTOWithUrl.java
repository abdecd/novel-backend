package com.abdecd.novelbackend.business.pojo.dto.novel;

import lombok.Data;

@Data
public class UpdateNovelInfoDTOWithUrl {
    Integer id;

    String title;

    String author;

    String cover;

    String description;

    int[] tagIds;
}
