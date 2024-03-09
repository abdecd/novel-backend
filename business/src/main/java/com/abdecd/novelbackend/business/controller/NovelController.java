package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.pojo.dto.novel.AddNovelInfoDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.DeleteNovelInfoDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.UpdateNovelInfoDTO;
import com.abdecd.novelbackend.business.pojo.entity.NovelInfo;
import com.abdecd.novelbackend.business.pojo.vo.novel.NovelInfoVO;
import com.abdecd.novelbackend.business.pojo.vo.novel.contents.ContentsVO;
import com.abdecd.novelbackend.business.service.NovelService;
import com.abdecd.novelbackend.business.service.ReaderService;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.novelbackend.common.result.Result;
import com.abdecd.tokenlogin.aspect.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "小说接口")
@RestController
@RequestMapping("novel")
public class NovelController {
    @Autowired
    private NovelService novelService;
    @Autowired
    private ReaderService readerService;

    @Operation(summary = "获取小说信息")
    @GetMapping("")
    public Result<NovelInfoVO> getNovelInfo(
            @NotNull @Schema(description = "小说id") Integer nid
    ) {
        var novelInfo = novelService.getNovelInfo(nid);
        var novelInfoVO = new NovelInfoVO();
        if (novelInfo != null) BeanUtils.copyProperties(novelInfo, novelInfoVO);
        return Result.success(novelInfoVO);
    }

    @Operation(summary = "修改小说信息")
    @RequirePermission(99)
    @PostMapping("update")
    public Result<String> updateNovelInfo(@RequestBody @Valid UpdateNovelInfoDTO updateNovelInfoDTO) {
        novelService.updateNovelInfo(updateNovelInfoDTO);
        return Result.success();
    }

    @Operation(summary = "新增小说", description = "data字段返回小说id")
    @RequirePermission(99)
    @PostMapping("add")
    public Result<String> addNovelInfo(@RequestBody @Valid AddNovelInfoDTO addNovelInfoDTO) {
        var novelId = novelService.addNovelInfo(addNovelInfoDTO);
        return Result.success(novelId+"");
    }

    @Operation(summary = "删除小说")
    @RequirePermission(99)
    @PostMapping("delete")
    public Result<String> deleteNovelInfo(@RequestBody @Valid DeleteNovelInfoDTO deleteNovelInfoDTO) {
        novelService.deleteNovelInfo(deleteNovelInfoDTO.getId());
        return Result.success();
    }

    @Operation(summary = "获取小说目录")
    @GetMapping("contents")
    public Result<ContentsVO> getContents(
            @NotNull @Schema(description = "小说id") Integer nid
    ) {
        var contentsVO = novelService.getContents(nid);// todo 考虑缓存
        return Result.success(contentsVO);
    }

    @Operation(summary = "获取小说排行榜", description = "最多100本")
    @GetMapping("ranklist")
    public Result<PageVO<NovelInfo>> getRankList(
            @NotNull @Schema(description = "时间类型，day日榜week周榜month月榜") String timeType,
            @Nullable @Schema(description = "小说类型") String tagName,
            @NotNull @Schema(description = "页码") Integer page,
            @NotNull @Schema(description = "每页数量") Integer pageSize
    ) {
        var novelList = novelService.pageRankList(timeType, tagName, page, pageSize);
        return Result.success(novelList);
    }

    @Operation(summary = "获取轮播小说列表", description = "最多5本")
    @GetMapping("carousel")
    public Result<List<NovelInfo>> getCarouselList() {
        var novelPageVO = novelService.pageRankList("month", null, 1, 5);
        return Result.success(novelPageVO.getRecords());
    }

    @Operation(summary = "获取推荐小说列表", description = "最多10本")
    @GetMapping("recommend")
    public Result<List<NovelInfo>> getRecommendList() {
        var novelList = readerService.getRecommendList();
        return Result.success(novelList);
    }

    @Operation(summary = "获取相关小说推荐", description = "最多3本")
    @GetMapping("related")
    public Result<List<NovelInfo>> getRelatedList(
            @NotNull @Schema(description = "小说id") Integer nid
    ) {
        var novelList = novelService.getRelatedList(nid);
        return Result.success(novelList);
    }
}
