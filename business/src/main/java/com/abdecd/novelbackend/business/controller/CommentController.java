package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.pojo.dto.user.AddCommentDTO;
import com.abdecd.novelbackend.business.pojo.dto.user.DeleteCommentDTO;
import com.abdecd.novelbackend.business.pojo.vo.user.UserCommentVO;
import com.abdecd.novelbackend.business.service.CommentService;
import com.abdecd.novelbackend.common.result.PageVO;
import com.abdecd.novelbackend.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评论接口")
@RestController
@RequestMapping("/novel/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Operation(summary = "获取评论")
    @GetMapping("")
    public Result<PageVO<List<UserCommentVO>>> getComment(
            @NotNull @Schema(description = "小说id") Integer novelId,
            @Nullable @Schema(description = "起始根评论id") @Min(0) Long startId,
            @NotNull @Schema(description = "评论块数量") @Min(0) Integer pageNum
    ) {
        return Result.success(commentService.getComment(novelId, startId, pageNum));
    }

    @Operation(summary = "添加评论")
    @PostMapping("add")
    public Result<String> addComment(@RequestBody @Valid AddCommentDTO addCommentDTO) {
        var commentId = commentService.addComment(addCommentDTO);
        return Result.success(commentId + "");
    }

    @Operation(summary = "删除评论")
    @PostMapping("delete")
    public Result<String> deleteComment(@RequestBody @Valid DeleteCommentDTO deleteCommentDTO) {
        commentService.deleteComment(deleteCommentDTO.getId());
        return Result.success();
    }
}
