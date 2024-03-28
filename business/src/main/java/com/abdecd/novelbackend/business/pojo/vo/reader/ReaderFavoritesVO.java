package com.abdecd.novelbackend.business.pojo.vo.reader;

import com.abdecd.novelbackend.business.pojo.entity.NovelTags;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ReaderFavoritesVO {
    @Deprecated
    @Schema(description = "收藏id")
    private Integer id;
    private Integer novelId;
    private String title;
    private String author;
    private String cover;
    private String description;
    private List<NovelTags> tags;
}
