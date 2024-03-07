package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.pojo.entity.NovelInfo;
import com.abdecd.novelbackend.business.service.NovelService;
import com.abdecd.novelbackend.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "搜索接口")
@RestController
@RequestMapping("search")
public class SearchController {
    @Autowired
    private NovelService novelService;

    @Operation(summary = "搜索书籍", description = "模糊匹配")
    @GetMapping("book")
    public Result<List<NovelInfo>> searchBook(
            @NotNull @Schema(description = "书名") String title,
            @Schema(description = "起始小说id") Long startId,
            @NotNull @Schema(description = "每页数量") Integer pageSize
    ) {
        return Result.success(novelService.searchNovelInfoByTitle(title, startId, pageSize));
    }

    @Operation(summary = "搜索作者对应的书籍", description = "精确搜索")
    @GetMapping("author")
    public Result<List<NovelInfo>> searchBookByAuthor(
            @NotNull @Schema(description = "作者名") String author,
            @Schema(description = "起始小说id") Long startId,
            @NotNull @Schema(description = "每页数量") Integer pageSize
    ) {
        return Result.success(novelService.searchNovelInfoByAuthor(author, startId, pageSize));
    }
}
