package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.pojo.vo.common.CaptchaVO;
import com.abdecd.novelbackend.business.service.CommonService;
import com.abdecd.novelbackend.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;

@Tag(name = "通用接口")
@RestController
public class CommonController {
    @Resource
    CommonService commonService;

    @Async
    @Operation(summary = "获取验证码图片")
    @GetMapping("/common/captcha")
    public CompletableFuture<Result<CaptchaVO>> getCaptcha() {
        var pair = commonService.generateCaptcha();

        var base64 = "data:image/jpg;base64," + Base64.getEncoder().encodeToString(pair.getSecond());
        var ans = new CaptchaVO()
                .setVerifyCodeId(pair.getFirst())
                .setCaptcha(base64);
        return CompletableFuture.completedFuture(Result.success(ans));
    }
}
