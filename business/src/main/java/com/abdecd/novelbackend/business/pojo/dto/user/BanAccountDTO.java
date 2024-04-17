package com.abdecd.novelbackend.business.pojo.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BanAccountDTO {
    @NotNull
    private Integer userId;
    @NotNull
    private Byte status;
}
