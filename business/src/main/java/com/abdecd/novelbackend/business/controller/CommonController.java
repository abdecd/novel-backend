package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.pojo.vo.common.CaptchaVO;
import com.abdecd.novelbackend.business.service.CommonService;
import com.abdecd.novelbackend.business.service.FileService;
import com.abdecd.novelbackend.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Tag(name = "通用接口")
@Slf4j
@RestController
@RequestMapping("common")
public class CommonController {
    @Resource
    CommonService commonService;
    @Autowired
    FileService fileService;
    @Autowired
    StringRedisTemplate redisTemplate;

    @Async
    @Operation(summary = "获取验证码图片")
    @GetMapping("/captcha")
    public CompletableFuture<Result<CaptchaVO>> getCaptcha() {
        var pair = commonService.generateCaptcha();

        var base64 = "data:image/jpg;base64," + Base64.getEncoder().encodeToString(pair.getSecond());
        var ans = new CaptchaVO()
                .setVerifyCodeId(pair.getFirst())
                .setCaptcha(base64);
        return CompletableFuture.completedFuture(Result.success(ans));
    }

    @Async
    @Operation(summary = "邮箱验证")
    @GetMapping("/verify-email")
    public CompletableFuture<Result<String>> verifyEmail(@Email String email, HttpServletRequest request) {
        // 接口限流
        if (Boolean.TRUE.equals(redisTemplate.hasKey("limitVerifyEmail:" + request.getRemoteAddr())))
            return CompletableFuture.completedFuture(Result.error("请求过于频繁"));
        var uuid = commonService.verifyEmail(email);
        // 成功后进行限流
        redisTemplate.opsForValue().set("limitVerifyEmail:" + request.getRemoteAddr(), "true", 60, TimeUnit.SECONDS);
        return CompletableFuture.completedFuture(Result.success(uuid));
    }

    @Async
    @Operation(summary = "图片上传")
    @PostMapping("/upload")
    public CompletableFuture<Result<String>> uploadImg(@RequestParam MultipartFile file) {
        try {
            return CompletableFuture.completedFuture(Result.success(fileService.uploadTmpImg(file)));
        } catch (IOException e) {
            log.warn("上传失败", e);
            return CompletableFuture.completedFuture(Result.error("上传失败"));
        }
    }

    @Async
    @Operation(summary = "图片查看")
    @GetMapping("/image/**")
    public CompletableFuture<Result<String>> viewImg(HttpServletRequest request, HttpServletResponse response) {
        try {
            fileService.viewImg(request.getRequestURI().substring("/common/image".length()), response);
            return null;
        } catch (IOException e) {
            log.warn("查看失败", e);
            return CompletableFuture.completedFuture(Result.error("查看失败"));
        }
    }
}
