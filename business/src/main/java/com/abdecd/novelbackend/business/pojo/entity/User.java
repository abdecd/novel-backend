package com.abdecd.novelbackend.business.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class User {
    private Integer id;
    private String password;
    private String email;
    private Byte permission;
    private Byte status;
}
