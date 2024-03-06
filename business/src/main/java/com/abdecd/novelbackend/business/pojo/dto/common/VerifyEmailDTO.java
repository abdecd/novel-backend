package com.abdecd.novelbackend.business.pojo.dto.common;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailDTO {
    @Email
    @NotBlank
    private String email;
}
