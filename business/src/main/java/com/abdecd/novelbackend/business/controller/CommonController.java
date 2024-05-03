package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.business.common.util.ImageChecker;
import com.abdecd.novelbackend.business.pojo.dto.common.VerifyEmailDTO;
import com.abdecd.novelbackend.business.pojo.vo.common.CaptchaVO;
import com.abdecd.novelbackend.business.service.CommonService;
import com.abdecd.novelbackend.business.service.FileService;
import com.abdecd.novelbackend.business.service.lib.RateLimiter;
import com.abdecd.novelbackend.common.constant.MessageConstant;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.FileCopyUtils;
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
    @Autowired
    private UserBaseService userBaseService;
    @Autowired
    private RateLimiter rateLimiter;

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
        if (Boolean.TRUE.equals(redisTemplate.hasKey(RedisConstant.LIMIT_VERIFY_EMAIL + request.getHeader("X-Real-IP"))))
            return CompletableFuture.completedFuture(Result.error(MessageConstant.RATE_LIMIT));
        commonService.sendCodeToVerifyEmail(verifyEmailDTO.getEmail());
        // 成功后进行限流
        redisTemplate.opsForValue().set(
                RedisConstant.LIMIT_VERIFY_EMAIL + request.getHeader("X-Real-IP"),
                "true",
                RedisConstant.LIMIT_VERIFY_EMAIL_RESET_TIME,
                TimeUnit.SECONDS
        );
        return CompletableFuture.completedFuture(Result.success());
    }

    @Async
    @Operation(summary = "图片上传")
    @PostMapping("/upload")
    public CompletableFuture<Result<String>> uploadImg(@RequestParam MultipartFile file) {
        try {
            if (file.isEmpty()) return CompletableFuture.completedFuture(Result.error(MessageConstant.IMG_FILE_EMPTY));
            if (!ImageChecker.isImage(file)) throw new BaseException(MessageConstant.IMG_FILE_TYPE_ERROR);
            if (file.getSize() > 1024 * 1024 * 3) throw new BaseException(MessageConstant.IMG_FILE_SIZE_ERROR);

            var key = RedisConstant.LIMIT_UPLOAD_IMG + UserContext.getUserId();
            if (rateLimiter.isRateLimited(
                    key,
                    RedisConstant.LIMIT_UPLOAD_IMG_CNT,
                    RedisConstant.LIMIT_UPLOAD_IMG_RESET_TIME,
                    TimeUnit.DAYS)
            ) return CompletableFuture.completedFuture(Result.error(MessageConstant.IMG_FILE_UPLOAD_LIMIT));
            return CompletableFuture.completedFuture(Result.success(fileService.uploadTmpFile(file)));
        } catch (IOException e) {
            log.warn(MessageConstant.IMG_FILE_UPLOAD_FAIL, e);
            return CompletableFuture.completedFuture(Result.error(MessageConstant.IMG_FILE_UPLOAD_FAIL));
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
            if (inForCheck != null && ImageChecker.isImage(inForCheck)) {
                response.setContentType("image/jpeg");
                response.setHeader("Cache-Control", "public, max-age=31536000");
                FileCopyUtils.copy(in, response.getOutputStream());
            } else throw new BaseException(MessageConstant.IMG_FILE_NOT_FOUND);
            return null;
        } catch (IOException e) {
            log.warn(MessageConstant.IMG_FILE_READ_FAIL, e);
            return CompletableFuture.completedFuture(Result.error(MessageConstant.IMG_FILE_READ_FAIL));
        }
    }

    @Operation(summary = "获得公钥")
    @GetMapping("public-key")
    public Result<String> getPublicKey() {
        return Result.success(userBaseService.getPublicKey());
    }
}
