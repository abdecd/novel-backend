package com.abdecd.novelbackend.business.pojo.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SignUpDTO {
    @NotBlank
    @Length(min = 3, max = 20)
    private String nickname;

    @NotBlank
    private String password;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Length(min = 6, max = 6)
    @Schema(description = "6位邮箱验证码")
    private String verifyCode;
}
