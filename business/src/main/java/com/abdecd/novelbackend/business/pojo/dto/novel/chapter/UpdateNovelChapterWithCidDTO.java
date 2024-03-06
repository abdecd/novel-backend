package com.abdecd.novelbackend.business.pojo.dto.novel.chapter;

import com.abdecd.novelbackend.common.constant.DTOConstant;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UpdateNovelChapterWithCidDTO {
    @NotNull
    private Long id;

    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    private String title;

    @Length(max = DTOConstant.NOVEL_CONTENT_LENGTH_MAX)
    private String content;
}
