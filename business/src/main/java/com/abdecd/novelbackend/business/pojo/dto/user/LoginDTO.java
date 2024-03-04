package com.abdecd.novelbackend.business.pojo.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank
    private String username; // 通用

    @NotBlank
    private String password;

    @NotBlank
    private String verifyCodeId;

    @NotBlank
    private String captcha;
}
