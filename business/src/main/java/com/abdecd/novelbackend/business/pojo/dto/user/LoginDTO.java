package com.abdecd.novelbackend.business.pojo.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginDTO {
    @NotNull
    private Integer userId;

    @NotBlank
    private String password;

    @NotBlank
    private String captcha;
}
