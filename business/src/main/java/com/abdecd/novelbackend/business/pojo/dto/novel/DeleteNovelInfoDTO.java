package com.abdecd.novelbackend.business.pojo.dto.novel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteNovelInfoDTO {
    @NotNull
    @Schema(description = "小说id")
    Integer id;
}
