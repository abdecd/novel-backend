package com.abdecd.novelbackend.business.pojo.vo.reader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReaderHistoryVO {
    private Long id;
    private Integer novelId;
    private Integer volumeNumber;
    private Integer chapterNumber;
    private LocalDateTime timestamp;
    @Schema(description = "小说标题")
    private String novelTitle;
    private String author;
    private String cover;
    @Schema(description = "文章标题")
    private String chapterTitle;
}
