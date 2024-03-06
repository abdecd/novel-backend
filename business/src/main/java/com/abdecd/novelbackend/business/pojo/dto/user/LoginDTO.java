package com.abdecd.novelbackend.business.pojo.dto.user;

import com.abdecd.novelbackend.common.constant.DTOConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class LoginDTO {
    @NotBlank
    @Length(min = DTOConstant.PERSON_NAME_LENGTH_MIN, max = DTOConstant.PERSON_NAME_LENGTH_MAX)
    @Schema(description = "用户id或者email")
    private String username;

    @NotBlank
    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    private String password;

    @NotBlank
    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    @Schema(description = "图像验证码id")
    private String verifyCodeId;

    @NotBlank
    @Length(min = DTOConstant.CAPTCHA_LENGTH, max = DTOConstant.CAPTCHA_LENGTH)
    @Schema(description = "4位图像验证码")
    private String captcha;
}
