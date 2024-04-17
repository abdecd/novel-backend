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
    private Integer id; // 必须
    private String password; // 必须
    private String email;
    private String permission; // 必须
    private Byte status; // 必须

    public static User ofEmpty() {
        return new User()
                .setId(null)
                .setPassword("")
                .setEmail("")
                .setPermission("")
                .setStatus((byte) 0);
    }
}
