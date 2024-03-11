package com.abdecd.novelbackend.business.pojo.dto.reader;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ReaderFavoritesDTO {
    @NotEmpty
    private int[] novelIds;
}
