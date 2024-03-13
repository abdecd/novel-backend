package com.abdecd.novelbackend.business.pojo.dto.novel;

import lombok.Data;

@Data
public class AddNovelInfoDTOWithUrl {
    String title;

    String author;

    String cover;

    String description;

    int[] tagIds;
}
