package com.abdecd.novelbackend.business.pojo.dto.novel.chapter;

import com.abdecd.novelbackend.business.pojo.entity.NovelChapter;
import com.abdecd.novelbackend.common.constant.DTOConstant;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
public class UpdateNovelChapterDTO {
    @NotNull
    private Integer novelId;

    @NotNull
    private Integer volumeNumber;

    @NotNull
    private Integer chapterNumber;

    @Length(max = DTOConstant.STRING_LENGTH_MAX)
    private String title;

    @Length(max = DTOConstant.NOVEL_CONTENT_LENGTH_MAX)
    private String content;

    public NovelChapter toEntity() {
        var novelChapter = new NovelChapter();
        BeanUtils.copyProperties(this, novelChapter);
        novelChapter.setTimestamp(LocalDateTime.now());
        return novelChapter;
    }
}
