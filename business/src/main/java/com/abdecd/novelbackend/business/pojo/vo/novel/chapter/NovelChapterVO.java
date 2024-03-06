package com.abdecd.novelbackend.business.pojo.vo.novel.chapter;

import lombok.Data;

@Data
public class NovelChapterVO {
    private Long id;
    private Integer novelId;
    private Integer volumeNumber;
    private Integer chapterNumber;
    private String title;
    private String content;
}
