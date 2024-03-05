package com.abdecd.novelbackend.business.pojo.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPwdDTO {
    @Email
    private String email;
    @NotBlank
    private String verifyCode;
    @NotBlank
    private String newPassword;
}
