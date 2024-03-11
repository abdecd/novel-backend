package com.abdecd.novelbackend.business.pojo.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteCommentDTO {
    @NotNull
    @Schema(description = "评论id")
    private Integer id;
}
