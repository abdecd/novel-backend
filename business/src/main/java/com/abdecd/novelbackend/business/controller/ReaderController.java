package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.common.util.HttpCacheUtils;
import com.abdecd.novelbackend.business.pojo.dto.reader.DeleteReaderHistoryDTO;
import com.abdecd.novelbackend.business.pojo.dto.reader.ReaderFavoritesDTO;
import com.abdecd.novelbackend.business.pojo.dto.reader.UpdateReaderDetailDTO;
import com.abdecd.novelbackend.business.pojo.dto.reader.UpdateReaderDetailDTOWithUrl;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderDetailVO;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderFavoritesVO;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderHistoryVO;
import com.abdecd.novelbackend.business.service.ReaderService;
import com.abdecd.novelbackend.common.constant.RedisConstant;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.novelbackend.common.result.Result;
import com.abdecd.tokenlogin.common.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Tag(name = "读者接口")
@RestController
@RequestMapping("reader")
public class ReaderController {
    @Autowired
    private ReaderService readerService;
    @Autowired
    private CommonController commonController;
    @Autowired
    private RedisTemplate<String, LocalDateTime> redisTemplate;

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

    @Async
    @Operation(summary = "修改用户信息")
    @PostMapping(value = "update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<Result<String>> updateReaderDetail(@Valid UpdateReaderDetailDTO updateReaderDetailDTO) throws ExecutionException, InterruptedException {
        var avatarResult = commonController.uploadImg(updateReaderDetailDTO.getAvatar());
        if (avatarResult.get().getCode() != 200) return avatarResult;
        var tmp = new UpdateReaderDetailDTOWithUrl();
        BeanUtils.copyProperties(updateReaderDetailDTO, tmp);
        tmp.setAvatar(avatarResult.get().getData());
        readerService.updateReaderDetail(tmp);
        return CompletableFuture.completedFuture(Result.success());
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

    @Operation(summary = "获取特定id的小说是否被用户收藏")
    @GetMapping("favorites/contains")
    public Result<Boolean> getReaderFavoritesContains(
            @NotNull @Schema(description = "小说id") Integer novelId
    ) {
        var readerFavorites = readerService.listReaderFavoritesVO(UserContext.getUserId());
        for (var readerFavoritesVO : readerFavorites) {
            if (Objects.equals(readerFavoritesVO.getNovelId(), novelId)) return Result.success(true);
        }
        return Result.success(false);
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
    public Result<PageVO<ReaderHistoryVO>> getReaderHistory(
            @NotNull @Schema(description = "页码") Integer page,
            @NotNull @Schema(description = "每页数量") Integer pageSize,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (HttpCacheUtils.tryUseCache(
                request,
                response,
                redisTemplate.opsForValue().get(
                        RedisConstant.READER_HISTORY_TIMESTAMP + UserContext.getUserId()
                ))) return null;
        var readerHistory = readerService.listReaderHistoryVO(UserContext.getUserId(), page, pageSize);
        return Result.success(readerHistory);
    }

    @Operation(summary = "获取用户特定小说阅读记录")
    @GetMapping("history/novel")
    public Result<List<ReaderHistoryVO>> getReaderHistoryByNovel(
            @NotNull @Schema(description = "小说id") Integer novelId,
            @Schema(description = "页码") Integer page,
            @NotNull @Schema(description = "每页数量") Integer pageSize
    ) {
        if (page == null) page = 1;
        var readerHistory = readerService.listReaderHistoryByNovel(UserContext.getUserId(), novelId, page, pageSize);
        return Result.success(readerHistory);
    }

    @Operation(summary = "删除用户阅读历史")
    @PostMapping("history/delete")
    public Result<String> deleteReaderHistory(@RequestBody @Valid DeleteReaderHistoryDTO deleteReaderHistoryDTO) {
        readerService.deleteReaderHistory(UserContext.getUserId(), deleteReaderHistoryDTO.getNovelIds());
        return Result.success();
    }
}
