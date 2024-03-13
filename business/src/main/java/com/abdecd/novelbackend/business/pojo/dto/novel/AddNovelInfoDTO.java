package com.abdecd.novelbackend.business.pojo.dto.novel;

import com.abdecd.novelbackend.common.constant.DTOConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

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

    @NotNull
    @Schema(description = "小说封面")
    MultipartFile cover;

    @NotBlank
    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    @Schema(description = "小说描述")
    String description;

    @NotEmpty
    @Schema(description = "小说tags")
    int[] tagIds;
}
