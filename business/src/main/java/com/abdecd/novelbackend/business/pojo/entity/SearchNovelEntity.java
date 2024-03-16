package com.abdecd.novelbackend.business.pojo.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchNovelEntity {
    private Integer id;
    private String title;
    private String author;
    private String description;
    private List<String> tags;
    private List<String> suggestion;

    public void refreshSuggestion() {
        suggestion = new ArrayList<>();
        suggestion.add(title);
        suggestion.add(author);
    }
}
