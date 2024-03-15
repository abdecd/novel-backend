package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.business.pojo.vo.novel.NovelInfoVO;
import com.abdecd.novelbackend.business.service.ElasticSearchService;
import com.abdecd.novelbackend.business.service.NovelExtService;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.novelbackend.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Tag(name = "搜索接口")
@RestController
@RequestMapping("search")
public class SearchController {
    @Autowired
    private NovelExtService novelExtService;
    @Autowired
    private ElasticSearchService elasticSearchService;

    @Operation(summary = "搜索", description = "聚合搜索")
    @GetMapping("")
    public Result<PageVO<NovelInfoVO>> search(
            @NotBlank @Schema(description = "关键字") String q,
            @NotNull @Schema(description = "页码") Integer page,
            @NotNull @Schema(description = "每页数量") Integer pageSize
    ) {
        try {
            return Result.success(elasticSearchService.searchNovel(q, page, pageSize));
        } catch (IOException e) {
            throw new BaseException("搜索失败");
        }
    }

    @Operation(summary = "获得搜索建议")
    @GetMapping("suggestion")
    public Result<List<String>> getSuggestion(
            @NotBlank @Schema(description = "关键字") String q,
            @NotNull @Schema(description = "数量") @Min(1) @Max(16) Integer num
    ) {
        try {
            return Result.success(elasticSearchService.getSearchSuggestions(q, num));
        } catch (IOException e) {
            throw new BaseException("搜索失败");
        }
    }

    @Operation(summary = "搜索书籍", description = "模糊匹配")
    @GetMapping("book")
    public Result<PageVO<NovelInfoVO>> searchBook(
            @NotNull @Schema(description = "书名") String title,
            @NotNull @Schema(description = "页码") Integer page,
            @NotNull @Schema(description = "每页数量") Integer pageSize
    ) {
        return Result.success(novelExtService.searchNovelInfoByTitle(title, page, pageSize));
    }

    @Operation(summary = "搜索作者对应的书籍", description = "模糊匹配")
    @GetMapping("author")
    public Result<PageVO<NovelInfoVO>> searchBookByAuthor(
            @NotNull @Schema(description = "作者名") String author,
            @NotNull @Schema(description = "页码") Integer page,
            @NotNull @Schema(description = "每页数量") Integer pageSize
    ) {
        return Result.success(novelExtService.searchNovelInfoByAuthor(author, page, pageSize));
    }
}
