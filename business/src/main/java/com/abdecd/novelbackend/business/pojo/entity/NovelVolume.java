package com.abdecd.novelbackend.business.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("novel_volume")
public class NovelVolume {
    @TableId
    private Integer novelId;
    private Integer volumeNumber;
    private String title;
    private String cover;
}
