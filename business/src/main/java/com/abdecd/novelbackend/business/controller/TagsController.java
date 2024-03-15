package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.common.util.HttpCacheUtils;
import com.abdecd.novelbackend.business.pojo.entity.NovelTags;
import com.abdecd.novelbackend.business.pojo.vo.novel.NovelInfoVO;
import com.abdecd.novelbackend.business.service.NovelExtService;
import com.abdecd.novelbackend.business.service.NovelService;
import com.abdecd.novelbackend.common.constant.DTOConstant;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.novelbackend.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "标签接口")
@RestController
@RequestMapping("novel/tags")
public class TagsController {
    @Autowired
    private NovelService novelService;
    @Autowired
    private NovelExtService novelExtService;

    @Operation(summary = "获取可用tags")
    @GetMapping("available-tags")
    public Result<List<NovelTags>> getAvailableTags(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        var tags = novelService.getAvailableTags(); // 非空
        if (HttpCacheUtils.tryUseCache(request, response, tags.hashCode())) return null;
        return Result.success(tags);
    }

    @Operation(summary = "查找tags")
    @GetMapping("tags")
    public Result<List<NovelTags>> searchTags(
            @NotBlank @Schema(description = "标签名") @Length(min = 1, max = DTOConstant.STRING_LENGTH_MAX) String tagName
    ) {
        var tags = novelService.searchTags(tagName);
        return Result.success(tags);
    }

    @Operation(summary = "获取热门tags")
    @GetMapping("hot-tags")
    public Result<List<NovelTags>> getHotTags() {
        var tags = novelExtService.getHotTags("week");
        return Result.success(tags);
    }

    @Operation(summary = "获取tags对应的小说列表")
    @GetMapping("get-by-tags")
    public Result<PageVO<NovelInfoVO>> getNovelInfoVOByTagIds(
            @NotEmpty int[] tagIds,
            @NotNull @Schema(description = "页码") @Min(1) Integer page,
            @NotNull @Schema(description = "每页数量") @Min(0) Integer pageSize
    ) {
        var result = novelService.getNovelInfoVOByTagIds(tagIds, page, pageSize);
        return Result.success(result);
    }
}
