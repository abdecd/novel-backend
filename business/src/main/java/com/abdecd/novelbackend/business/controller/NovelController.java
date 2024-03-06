package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.pojo.dto.novel.AddNovelInfoDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.DeleteNovelInfoDTO;
import com.abdecd.novelbackend.business.pojo.dto.novel.UpdateNovelInfoDTO;
import com.abdecd.novelbackend.business.pojo.vo.novel.NovelInfoVO;
import com.abdecd.novelbackend.business.service.NovelService;
import com.abdecd.novelbackend.common.result.Result;
import com.abdecd.tokenlogin.aspect.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "小说接口")
@RestController
@RequestMapping("novel")
public class NovelController {
    @Autowired
    private NovelService novelService;

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


}
