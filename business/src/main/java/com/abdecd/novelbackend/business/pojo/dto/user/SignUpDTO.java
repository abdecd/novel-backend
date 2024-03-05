package com.abdecd.novelbackend.business.pojo.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignUpDTO {
    @NotBlank
    private String nickname;
    @NotBlank
    private String password;
    @Email
    private String email;
    @NotBlank
    private String verifyCode;
}
