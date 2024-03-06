package com.abdecd.novelbackend.business.pojo.dto.novel.chapter;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeleteNovelChapterDTO {
    @NotNull
    private Integer novelId;

    @NotNull
    private Integer volumeNumber;

    @NotNull
    private Integer chapterNumber;
}
