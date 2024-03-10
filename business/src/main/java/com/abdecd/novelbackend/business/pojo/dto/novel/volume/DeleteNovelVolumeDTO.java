package com.abdecd.novelbackend.business.pojo.dto.novel.volume;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeleteNovelVolumeDTO {
    @NotNull
    private Integer novelId;

    @NotNull
    private Integer volumeNumber;
}
