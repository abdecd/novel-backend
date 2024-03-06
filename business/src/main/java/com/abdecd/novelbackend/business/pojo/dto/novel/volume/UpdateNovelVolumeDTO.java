package com.abdecd.novelbackend.business.pojo.dto.novel.volume;

import com.abdecd.novelbackend.common.constant.DTOConstant;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdateNovelVolumeDTO {
    @NotNull
    private Integer novelId;

    @NotNull
    private Integer volumeNumber;

    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    private String title;

    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    private String cover;
}
