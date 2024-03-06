package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.pojo.dto.reader.UpdateReaderDetailDTO;
import com.abdecd.novelbackend.business.pojo.vo.reader.ReaderDetailVO;
import com.abdecd.novelbackend.business.service.ReaderService;
import com.abdecd.novelbackend.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "读者接口")
@RestController
@RequestMapping("reader")
public class ReaderController {
    @Autowired
    private ReaderService readerService;

    @Operation(summary = "获取用户信息")
    @GetMapping("")
    public Result<ReaderDetailVO> getReaderDetail(
            @NotNull @Schema(description = "用户id") Integer uid
    ) {
        var reader = readerService.getReaderDetail(uid);
        var readerVO = new ReaderDetailVO();
        if (reader != null) BeanUtils.copyProperties(reader, readerVO);
        else readerVO = null;
        return Result.success(readerVO);
    }

    @Operation(summary = "修改用户信息")
    @PostMapping("update")
    public Result<String> updateReaderDetail(UpdateReaderDetailDTO updateReaderDetailDTO) {
        readerService.updateReaderDetail(updateReaderDetailDTO);
        return Result.success();
    }
}
