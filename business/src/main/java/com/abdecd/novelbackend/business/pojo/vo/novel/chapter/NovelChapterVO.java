package com.abdecd.novelbackend.business.pojo.vo.novel.chapter;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NovelChapterVO {
    private Long id;
    private Integer novelId;
    private Integer volumeNumber;
    private Integer chapterNumber;
    private String title;
    private LocalDateTime timestamp;
    private String content;
}
