package com.abdecd.novelbackend.business.pojo.dto.reader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteReaderHistoryDTO {
    @NotBlank
    @Schema(description = "历史记录ids")
    Long[] ids;
}
