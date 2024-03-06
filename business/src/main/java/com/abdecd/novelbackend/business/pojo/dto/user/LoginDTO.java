package com.abdecd.novelbackend.business.pojo.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class LoginDTO {
    @NotBlank
    @Length(min = 3, max = 20)
    @Schema(description = "用户id或者email")
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    @Schema(description = "图像验证码id")
    private String verifyCodeId;

    @NotBlank
    @Length(min = 4, max = 4)
    @Schema(description = "4位图像验证码")
    private String captcha;
}
