package com.abdecd.novelbackend.business.pojo.dto.reader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteReaderHistoryDTO {
    @NotNull
    @Schema(description = "小说ids")
    Integer[] novelIds;
}
