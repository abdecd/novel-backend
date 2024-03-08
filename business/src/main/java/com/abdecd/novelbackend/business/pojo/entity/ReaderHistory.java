package com.abdecd.novelbackend.business.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@TableName("reader_history")
public class ReaderHistory {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer novelId;
    private Integer volumeNumber;
    private Integer chapterNumber;
    private Byte status;
    private LocalDateTime timestamp;
}
