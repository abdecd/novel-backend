package com.abdecd.novelbackend.business.pojo.dto.reader;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class DeleteReaderHistoryDTO {
    @NotEmpty
    @Schema(description = "小说ids")
    int[] novelIds;
}
