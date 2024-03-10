package com.abdecd.novelbackend.business.pojo.dto.reader;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReaderFavoritesDTO {
    @NotBlank
    private Integer[] novelIds;
}
