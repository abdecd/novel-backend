package com.abdecd.novelbackend.business.pojo.dto.novel.volume;

import com.abdecd.novelbackend.common.constant.DTOConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class AddNovelVolumeDTO {
    @NotNull
    private Integer novelId;

    @NotNull
    private Integer volumeNumber;

    @NotBlank
    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    private String title;

    @NotBlank
    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    private String cover;
}
