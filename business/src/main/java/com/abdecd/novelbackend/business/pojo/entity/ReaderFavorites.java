package com.abdecd.novelbackend.business.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("reader_favorites")
public class ReaderFavorites {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer novelId;
}
