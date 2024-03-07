package com.abdecd.novelbackend.business.pojo.dto.reader;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReaderFavoritesDTO {
    @NotNull
    private Integer[] novelIds;
}
