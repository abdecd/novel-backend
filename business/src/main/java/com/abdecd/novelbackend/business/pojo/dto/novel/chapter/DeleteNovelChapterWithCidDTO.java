package com.abdecd.novelbackend.business.pojo.dto.novel.chapter;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteNovelChapterWithCidDTO {
    @NotNull
    private Long id;
}
