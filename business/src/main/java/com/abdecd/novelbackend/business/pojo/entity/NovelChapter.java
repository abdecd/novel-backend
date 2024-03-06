package com.abdecd.novelbackend.business.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("novel_chapter")
public class NovelChapter {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer novelId;
    private Integer volumeNumber;
    private Integer chapterNumber;
    private String title;
}
