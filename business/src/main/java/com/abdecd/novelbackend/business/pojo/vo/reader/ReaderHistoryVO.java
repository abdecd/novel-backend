package com.abdecd.novelbackend.business.pojo.vo.reader;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Accessors(chain = true)
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
