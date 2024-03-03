package com.abdecd.novelbackend.business.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("reader_detail")
public class ReaderDetail {
    @TableId
    private Integer userId;
    private String nickname;
    private String avatar;
    private String signature;
}
