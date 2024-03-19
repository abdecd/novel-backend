package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.business.common.util.HttpCacheUtils;
import com.abdecd.novelbackend.business.pojo.dto.novel.*;
import com.abdecd.novelbackend.business.pojo.vo.novel.NovelInfoVO;
import com.abdecd.novelbackend.business.pojo.vo.novel.contents.ContentsVO;
import com.abdecd.novelbackend.business.service.NovelService;
import com.abdecd.novelbackend.common.result.Result;
import com.abdecd.tokenlogin.aspect.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Tag(name = "小说接口")
@RestController
@RequestMapping("novel")
public class NovelController {
    @Autowired
    private NovelService novelService;
    @Autowired
    private CommonController commonController;

    @Operation(summary = "获取小说信息")
    @GetMapping("")
    public Result<NovelInfoVO> getNovelInfo(
            @NotNull @Schema(description = "小说id") Integer nid,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        var novelInfoVO = novelService.getNovelInfoVO(nid);
        if (novelInfoVO != null && HttpCacheUtils.tryUseCache(request, response, novelInfoVO.hashCode())) return null;
        return Result.success(novelInfoVO);
    }

    @Async
    @Operation(summary = "修改小说信息")
    @RequirePermission(value = 99, exception = BaseException.class)
    @PostMapping(value = "update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<Result<String>> updateNovelInfo(@Valid UpdateNovelInfoDTO updateNovelInfoDTO) throws ExecutionException, InterruptedException {
        var tmp = new UpdateNovelInfoDTOWithUrl();
        if (updateNovelInfoDTO.getCover() != null) {
            var coverResult = commonController.uploadImg(updateNovelInfoDTO.getCover());
            if (coverResult.get().getCode() != 200) return coverResult;
            tmp.setCover(coverResult.get().getData());
        }
        BeanUtils.copyProperties(updateNovelInfoDTO, tmp);
        novelService.updateNovelInfo(tmp);
        return CompletableFuture.completedFuture(Result.success());
    }

    @Async
    @Operation(summary = "新增小说", description = "data字段返回小说id")
    @RequirePermission(value = 99, exception = BaseException.class)
    @PostMapping(value = "add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<Result<String>> addNovelInfo(@Valid AddNovelInfoDTO addNovelInfoDTO) throws ExecutionException, InterruptedException {
        var coverResult = commonController.uploadImg(addNovelInfoDTO.getCover());
        if (coverResult.get().getCode() != 200) return coverResult;
        var tmp = new AddNovelInfoDTOWithUrl();
        BeanUtils.copyProperties(addNovelInfoDTO, tmp);
        tmp.setCover(coverResult.get().getData());
        var novelId = novelService.addNovelInfo(tmp);
        return CompletableFuture.completedFuture(Result.success(novelId + ""));
    }

    @Operation(summary = "删除小说")
    @RequirePermission(value = 99, exception = BaseException.class)
    @PostMapping("delete")
    public Result<String> deleteNovelInfo(@RequestBody @Valid DeleteNovelInfoDTO deleteNovelInfoDTO) {
        novelService.deleteNovelInfo(deleteNovelInfoDTO.getId());
        return Result.success();
    }

    @Operation(summary = "获取小说目录")
    @GetMapping("contents")
    public Result<ContentsVO> getContents(
            @NotNull @Schema(description = "小说id") Integer nid,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        var contentsVO = novelService.getContents(nid);
        if (contentsVO == null) return Result.success(null);
        if (HttpCacheUtils.tryUseCache(request, response, contentsVO.hashCode())) return null;
        return Result.success(contentsVO);
    }
}
