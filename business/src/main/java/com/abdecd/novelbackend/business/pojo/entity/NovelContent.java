package com.abdecd.novelbackend.business.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("novel_content")
public class NovelContent {
    @TableId
    private Long novelChapterId;
    private String content;
}
