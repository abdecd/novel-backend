package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.business.common.util.HttpCacheUtils;
import com.abdecd.novelbackend.business.pojo.dto.novel.chapter.AddNovelChapterDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.chapter.DeleteNovelChapterDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.chapter.UpdateNovelChapterDTO;
import com.abdecd.novelbackend.business.pojo.entity.NovelChapter;
import com.abdecd.novelbackend.business.pojo.vo.novel.chapter.NovelChapterVO;
import com.abdecd.novelbackend.business.service.NovelChapterService;
import com.abdecd.novelbackend.business.service.NovelVolumeService;
import com.abdecd.novelbackend.business.service.ReaderService;
import com.abdecd.novelbackend.common.constant.MessageConstant;
import com.abdecd.novelbackend.common.result.Result;
import com.abdecd.tokenlogin.aspect.RequirePermission;
import com.abdecd.tokenlogin.common.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Tag(name = "小说章节接口")
@RestController
@RequestMapping("/novel/chapter")
public class NovelChapterController {
    @Autowired
    private NovelChapterService novelChapterService;
    @Autowired
    private NovelVolumeService novelVolumeService;
    @Autowired
    private ReaderService readerService;

    @Operation(summary = "获取小说章节列表")
    @GetMapping("list")
    public Result<List<NovelChapter>> listNovelChapter(
            @NotNull @Schema(description = "小说id") Integer nid,
            @NotNull @Schema(description = "卷num") Integer vNum
    ) {
        var list = novelChapterService.listNovelChapter(nid, vNum);
        return Result.success(list);
    }

    @Operation(summary = "获取小说章节", description = "包括文章内容")
    @GetMapping("")
    public Result<NovelChapterVO> getNovelChapter(
            @NotNull @Schema(description = "小说id") Integer nid,
            @NotNull @Schema(description = "卷num") Integer vNum,
            @NotNull @Schema(description = "章num") Integer cNum,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        var currentLocalDateTime = novelChapterService.getNovelChapterVOOnlyTimestamp(nid, vNum, cNum);
        if (currentLocalDateTime == null) return Result.success(null);
        // 更新阅读记录
        if (UserContext.getUserId() != null) readerService.saveReaderHistory(
                UserContext.getUserId(),
                nid,
                vNum,
                cNum
        );
        if (HttpCacheUtils.tryUseCache(request, response, currentLocalDateTime)) return null;

        var novelChapter = novelChapterService.getNovelChapterVO(nid, vNum, cNum);
        return Result.success(novelChapter);
    }

    @Operation(summary = "新增小说章节", description = "不添加小说内容, 成功时返回novelChapterId(String)")
    @RequirePermission(value = 99, exception = BaseException.class)
    @PostMapping("add")
    public Result<String> addNovelChapter(@RequestBody @Valid AddNovelChapterDTO addNovelChapterDTO) {
        if (novelVolumeService.getNovelVolume(addNovelChapterDTO.getNovelId(), addNovelChapterDTO.getVolumeNumber()) == null)
            return Result.error(MessageConstant.NOVEL_VOLUME_NOT_FOUND);
        var novelChapterId = novelChapterService.addNovelChapter(addNovelChapterDTO);
        return Result.success(novelChapterId + "");
    }

    @Async
    @Operation(summary = "修改小说章节")
    @RequirePermission(value = 99, exception = BaseException.class)
    @PostMapping("update")
    public CompletableFuture<Result<String>> updateNovelChapter(@RequestBody @Valid UpdateNovelChapterDTO updateNovelChapterDTO) {
        novelChapterService.updateNovelChapter(updateNovelChapterDTO);
        return CompletableFuture.completedFuture(Result.success());
    }

    @Operation(summary = "删除小说章节")
    @RequirePermission(value = 99, exception = BaseException.class)
    @PostMapping("delete")
    public Result<String> deleteNovelChapter(@RequestBody @Valid DeleteNovelChapterDTO deleteNovelChapterDTO) {
        novelChapterService.deleteNovelChapter(deleteNovelChapterDTO);
        return Result.success();
    }
}
