package com.abdecd.novelbackend.business.pojo.dto.reader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateReaderDetailDTO {
    @NotNull
    @Schema(description = "用户id")
    private Integer userId;

    @NotNull
    @Schema(description = "用户昵称")
    private String nickname;

    @NotNull
    @Schema(description = "用户头像")
    private String avatar;

    @NotNull
    @Schema(description = "用户签名")
    private String signature;
}
