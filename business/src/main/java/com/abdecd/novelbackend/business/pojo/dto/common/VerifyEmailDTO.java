package com.abdecd.novelbackend.business.pojo.dto.common;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class VerifyEmailDTO {
    @Email
    private String email;
}
