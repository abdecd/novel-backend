package com.abdecd.novelbackend.business.pojo.dto.novel;

import com.abdecd.novelbackend.common.constant.DTOConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class AddNovelInfoDTO {
    @NotBlank
    @Length(min = 1, max = DTOConstant.STRING_LENGTH_MAX)
    @Schema(description = "小说标题")
    String title;

    @NotBlank
    @Length(min = DTOConstant.PERSON_NAME_LENGTH_MIN, max = DTOConstant.PERSON_NAME_LENGTH_MAX)
    @Schema(description = "小说作者")
    String author;

    @NotBlank
    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    @Schema(description = "小说封面")
    String cover;

    @NotBlank
    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    @Schema(description = "小说描述")
    String description;

    @Schema(description = "小说tags")
    Integer[] tagIds;
}
