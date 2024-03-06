package com.abdecd.novelbackend.business.pojo.dto.novel;

import com.abdecd.novelbackend.common.constant.DTOConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdateNovelInfoDTO {
    @NotNull
    @Schema(description = "小说id")
    Integer id;

    @Length(min = 1, max = DTOConstant.STRING_LENGTH_MAX)
    @Schema(description = "小说标题")
    String title;

    @Length(min = DTOConstant.PERSON_NAME_LENGTH_MIN, max = DTOConstant.PERSON_NAME_LENGTH_MAX)
    @Schema(description = "小说作者")
    String author;

    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    @Schema(description = "小说封面")
    String cover;

    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    @Schema(description = "小说描述")
    String description;
}
