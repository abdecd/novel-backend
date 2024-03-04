package com.abdecd.tokenlogin.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String password;
    private String email;
    private Byte permission;
    private Byte status;
}
