package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.pojo.dto.reader.DeleteReaderHistoryDTO;
import com.abdecd.novelbackend.business.pojo.dto.reader.ReaderFavoritesDTO;
import com.abdecd.novelbackend.business.pojo.dto.reader.UpdateReaderDetailDTO;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderDetailVO;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderFavoritesVO;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderHistoryVO;
import com.abdecd.novelbackend.business.service.ReaderService;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.novelbackend.common.result.Result;
import com.abdecd.tokenlogin.common.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "读者接口")
@RestController
@RequestMapping("reader")
public class ReaderController {
    @Autowired
    private ReaderService readerService;

    @Operation(summary = "获取用户信息")
    @GetMapping("")
    public Result<ReaderDetailVO> getReaderDetail(
            @Nullable @Schema(description = "用户id") Integer uid
    ) {
        if (uid == null) uid = UserContext.getUserId();
        var reader = readerService.getReaderDetail(uid);
        var readerVO = new ReaderDetailVO();
        if (reader != null) BeanUtils.copyProperties(reader, readerVO);
        else readerVO = null;
        return Result.success(readerVO);
    }

    @Operation(summary = "修改用户信息")
    @PostMapping("update")
    public Result<String> updateReaderDetail(@RequestBody @Valid UpdateReaderDetailDTO updateReaderDetailDTO) {
        readerService.updateReaderDetail(updateReaderDetailDTO);
        return Result.success();
    }

    @Operation(summary = "获取用户收藏列表")
    @GetMapping("favorites")
    public Result<PageVO<ReaderFavoritesVO>> getReaderFavorites(
            @NotNull @Schema(description = "页码") @Min(1) Integer page,
            @NotNull @Schema(description = "每页数量") @Min(0) Integer pageSize
    ) {
        var readerFavorites = readerService.pageReaderFavoritesVO(UserContext.getUserId(), page, pageSize);
        return Result.success(readerFavorites);
    }

    @Operation(summary = "添加用户收藏")
    @PostMapping("favorites/add")
    public Result<String> addReaderFavorites(@RequestBody @Valid ReaderFavoritesDTO readerFavoritesDTO) {
        readerService.addReaderFavorites(UserContext.getUserId(), readerFavoritesDTO.getNovelIds());
        return Result.success();
    }

    @Operation(summary = "删除用户收藏")
    @PostMapping("favorites/delete")
    public Result<String> deleteReaderFavorites(@RequestBody @Valid ReaderFavoritesDTO readerFavoritesDTO) {
        readerService.deleteReaderFavorites(UserContext.getUserId(), readerFavoritesDTO.getNovelIds());
        return Result.success();
    }

    @Operation(summary = "获取用户阅读历史")
    @GetMapping("history")
    public Result<List<ReaderHistoryVO>> getReaderHistory(
            @Nullable @Schema(description = "起始记录id(倒序)") Long startId,
            @NotNull @Schema(description = "每页数量") Integer pageSize
    ) {
        var readerHistory = readerService.listReaderHistoryVO(UserContext.getUserId(), startId, pageSize);
        return Result.success(readerHistory);
    }

    @Operation(summary = "获取用户特定小说阅读记录")
    @GetMapping("history/novel")
    public Result<List<ReaderHistoryVO>> getReaderHistoryByNovel(
            @NotNull @Schema(description = "小说id") Integer novelId,
            @Nullable @Schema(description = "起始记录id(倒序)") Long startId,
            @NotNull @Schema(description = "每页数量") Integer pageSize
    ) {
        var readerHistory = readerService.listReaderHistoryByNovel(UserContext.getUserId(), novelId, startId, pageSize);
        return Result.success(readerHistory);
    }

    @Operation(summary = "删除用户阅读历史")
    @PostMapping("history/delete")
    public Result<String> deleteReaderHistory(@RequestBody @Valid DeleteReaderHistoryDTO deleteReaderHistoryDTO) {
        readerService.deleteReaderHistory(UserContext.getUserId(), deleteReaderHistoryDTO.getIds());
        return Result.success();
    }
}
