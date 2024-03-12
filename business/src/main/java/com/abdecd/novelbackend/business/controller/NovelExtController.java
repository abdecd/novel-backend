package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.pojo.vo.novel.NovelInfoVO;
import com.abdecd.novelbackend.business.service.NovelExtService;
import com.abdecd.novelbackend.business.service.NovelService;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.novelbackend.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "小说增强接口")
@RestController
@RequestMapping("/novel")
public class NovelExtController {
    @Autowired
    private NovelService novelService;
    @Autowired
    private NovelExtService novelExtService;
    @Operation(summary = "获取小说排行榜", description = "最多100本")
    @GetMapping("ranklist")
    public Result<PageVO<NovelInfoVO>> getRankList(
            @NotNull @Schema(description = "时间类型，day日榜week周榜month月榜") String timeType,
            @Nullable @Schema(description = "小说类型") String tagName,
            @NotNull @Schema(description = "页码") @Min(1) Integer page,
            @NotNull @Schema(description = "每页数量") @Min(0) Integer pageSize
    ) {
        var novelList = novelExtService.pageRankList(timeType, tagName, page, pageSize);
        return Result.success(novelList);
    }

    @Operation(summary = "获取轮播小说列表")
    @GetMapping("carousel")
    public Result<List<NovelInfoVO>> getCarouselList(
            @NotNull @Schema(description = "数量") @Min(1) @Max(20) Integer num
    ) {
        var novelPageVO = novelExtService.pageRankList("month", null, 1, num);
        return Result.success(novelPageVO.getRecords());
    }

    @Operation(summary = "获取推荐小说列表")
    @GetMapping("recommend")
    public Result<List<NovelInfoVO>> getRecommendList(
            @NotNull @Schema(description = "数量") @Min(1) @Max(20) Integer num
    ) {
        var novelList = novelExtService.getRecommendList(num);
        return Result.success(novelList);
    }

    @Operation(summary = "获取相关小说推荐")
    @GetMapping("related")
    public Result<List<NovelInfoVO>> getRelatedList(
            @NotNull @Schema(description = "小说id") Integer nid,
            @NotNull @Schema(description = "数量") @Min(1) @Max(10) Integer num
    ) {
        var novelList = novelService.getRelatedList(nid, num);
        return Result.success(novelList);
    }
}
