package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.exceptionhandler.BaseException;
import com.abdecd.novelbackend.business.pojo.dto.novel.volume.AddNovelVolumeDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.volume.DeleteNovelVolumeDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.volume.UpdateNovelVolumeDTO;
import com.abdecd.novelbackend.business.pojo.entity.NovelVolume;
import com.abdecd.novelbackend.business.service.NovelVolumeService;
import com.abdecd.novelbackend.common.result.Result;
import com.abdecd.tokenlogin.aspect.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Tag(name = "小说卷接口")
@RestController
@RequestMapping("/novel/volume")
public class NovelVolumeController {
    @Autowired
    private NovelVolumeService novelVolumeService;

    @Operation(summary = "获取小说卷列表")
    @GetMapping("list")
    public Result<List<NovelVolume>> listNovelVolume(
            @NotNull @Schema(description = "小说id") Integer nid
    ) {
        return Result.success(novelVolumeService.listNovelVolume(nid));
    }

    @Operation(summary = "获取小说卷")
    @GetMapping("")
    public Result<NovelVolume> getNovelVolume(
            @NotNull @Schema(description = "小说id") Integer nid,
            @NotNull @Schema(description = "卷num") Integer vNum
    ) {
        return Result.success(novelVolumeService.getNovelVolume(nid, vNum));
    }

    @Operation(summary = "新增小说卷")
    @RequirePermission(value = "99", exception = BaseException.class)
    @PostMapping("add")
    public Result<String> addNovelVolume(@RequestBody @Valid AddNovelVolumeDTO addNovelVolumeDTO) {
        novelVolumeService.addNovelVolume(addNovelVolumeDTO);
        return Result.success();
    }

    @Operation(summary = "修改小说卷")
    @RequirePermission(value = "99", exception = BaseException.class)
    @PostMapping("update")
    public Result<String> updateNovelVolume(@RequestBody @Valid UpdateNovelVolumeDTO updateNovelVolumeDTO) {
        novelVolumeService.updateNovelVolume(updateNovelVolumeDTO);
        return Result.success();
    }

    @Async
    @Operation(summary = "删除小说卷")
    @RequirePermission(value = "99", exception = BaseException.class)
    @PostMapping("delete")
    public CompletableFuture<Result<String>> deleteNovelVolume(@RequestBody @Valid DeleteNovelVolumeDTO deleteNovelVolumeDTO) {
        novelVolumeService.deleteNovelVolume(deleteNovelVolumeDTO);
        return CompletableFuture.completedFuture(Result.success());
    }
}
