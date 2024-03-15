package com.abdecd.novelbackend.business.pojo.vo.novel;

import com.abdecd.novelbackend.business.pojo.entity.NovelTags;
import com.abdecd.novelbackend.business.pojo.entity.SearchNovelEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
public class NovelInfoVO {
    private Integer id;
    private String title;
    private String author;
    private String cover;
    private String description;
    private List<NovelTags> tags;

    public SearchNovelEntity toSearchNovelEntity() {
        var searchNovel = new SearchNovelEntity();
        BeanUtils.copyProperties(this, searchNovel);
        searchNovel.setTags(this.getTags().stream().map(NovelTags::getTagName).toList());
        searchNovel.refreshSuggestion();
        return searchNovel;
    }
}
