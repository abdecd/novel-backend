package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.business.common.util.ImageChecker;
import com.abdecd.novelbackend.business.pojo.dto.common.VerifyEmailDTO;
import com.abdecd.novelbackend.business.pojo.vo.common.CaptchaVO;
import com.abdecd.novelbackend.business.service.CommonService;
import com.abdecd.novelbackend.business.service.FileService;
import com.abdecd.novelbackend.common.constant.RedisConstant;
import com.abdecd.novelbackend.common.result.Result;
import com.abdecd.tokenlogin.common.context.UserContext;
import com.abdecd.tokenlogin.service.UserBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Objects;
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
    @Autowired
    private UserBaseService userBaseService;

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
    @PostMapping("/verify-email")
    public CompletableFuture<Result<String>> verifyEmail(@RequestBody @Valid VerifyEmailDTO verifyEmailDTO, HttpServletRequest request) {
        // 接口限流
        if (Boolean.TRUE.equals(redisTemplate.hasKey(RedisConstant.LIMIT_VERIFY_EMAIL + request.getRemoteAddr())))
            return CompletableFuture.completedFuture(Result.error("请求过于频繁"));
        commonService.sendCodeToVerifyEmail(verifyEmailDTO.getEmail());
        // 成功后进行限流
        redisTemplate.opsForValue().set(RedisConstant.LIMIT_VERIFY_EMAIL + request.getRemoteAddr(), "true", 60, TimeUnit.SECONDS);
        return CompletableFuture.completedFuture(Result.success());
    }

    @Async
    @Operation(summary = "图片上传")
    @PostMapping("/upload")
    public CompletableFuture<Result<String>> uploadImg(@RequestParam MultipartFile file) {
        try {
            if (file.isEmpty()) return CompletableFuture.completedFuture(Result.error("文件为空"));
            if (!ImageChecker.isImage(file)) throw new BaseException("文件类型不正确");
            if (file.getSize() > 1024 * 1024 * 3) throw new BaseException("文件过大");
            // 300次/天
            var key = RedisConstant.LIMIT_UPLOAD_IMG + UserContext.getUserId();
            RedisAtomicInteger redisAtomicInteger = new RedisAtomicInteger(key, Objects.requireNonNull(redisTemplate.getConnectionFactory()));
            if (redisAtomicInteger.get() == 0) redisAtomicInteger.expire(1, TimeUnit.DAYS);
            if (redisAtomicInteger.incrementAndGet() > 300) return CompletableFuture.completedFuture(Result.error("图片上传次数达到上限"));
            return CompletableFuture.completedFuture(Result.success(fileService.uploadTmpFile(file)));
        } catch (IOException e) {
            log.warn("上传失败", e);
            return CompletableFuture.completedFuture(Result.error("上传失败"));
        }
    }

    @Async
    @Operation(summary = "图片查看")
    @GetMapping("/image/**")
    public CompletableFuture<Result<String>> viewImg(HttpServletRequest request, HttpServletResponse response) {
        var path = request.getRequestURI().substring("/common/image".length());
        try (
                var inForCheck = fileService.getFileInSystem(path);
                var in = fileService.getFileInSystem(path)
        ) {
            response.setContentType("image/jpeg");
            response.setHeader("Cache-Control", "public, max-age=31536000");
            if (ImageChecker.isImage(inForCheck))
                FileCopyUtils.copy(in, response.getOutputStream());
            else throw new BaseException("文件不存在或格式错误");
            return null;
        } catch (IOException e) {
            log.warn("查看失败", e);
            return CompletableFuture.completedFuture(Result.error("查看失败"));
        }
    }

    @Operation(summary = "获得公钥")
    @GetMapping("public-key")
    public Result<String> getPublicKey() {
        return Result.success(userBaseService.getPublicKey());
    }
}
