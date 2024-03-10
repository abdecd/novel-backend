package com.abdecd.novelbackend.business.pojo.dto.user;

import com.abdecd.novelbackend.common.constant.DTOConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class SignUpDTO {
    @NotBlank
    @Length(min = DTOConstant.PERSON_NAME_LENGTH_MIN, max = DTOConstant.PERSON_NAME_LENGTH_MAX)
    private String nickname;

    @NotBlank
    @Length(max = DTOConstant.PASSWORD_LENGTH_MAX)
    private String password;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Length(min = DTOConstant.EMAIL_VERIFY_CODE_LENGTH, max = DTOConstant.EMAIL_VERIFY_CODE_LENGTH)
    @Schema(description = "6位邮箱验证码")
    private String verifyCode;
}
