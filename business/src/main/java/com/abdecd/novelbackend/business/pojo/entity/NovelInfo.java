package com.abdecd.novelbackend.business.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("novel_info")
public class NovelInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String author;
    private String cover;
    private String description;
}
