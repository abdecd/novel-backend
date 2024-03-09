package com.abdecd.novelbackend.business.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("novel_and_tags")
public class NovelAndTags {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer tagId;
    private Integer novelId;
}
