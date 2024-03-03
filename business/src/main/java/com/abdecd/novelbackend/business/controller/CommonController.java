package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.common.properties.TtlProperties;
import com.abdecd.novelbackend.business.service.CommonService;
import com.abdecd.novelbackend.common.constant.Constant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Tag(name = "通用接口")
@RestController
public class CommonController {
    @Resource
    CommonService commonService;
    @Resource
    TtlProperties ttlProperties;

    @Async
    @Operation(summary = "获取验证码图片")
    @GetMapping("/common/captcha")
    public CompletableFuture<Void> getCaptcha(HttpServletResponse response) throws IOException {
        var pair = commonService.generateCaptcha();
        var cookie = new Cookie(Constant.COOKIE_KEY_VERIFY_CODE_ID, pair.getFirst());
        cookie.setPath("/");
        cookie.setMaxAge(ttlProperties.getCaptchaTtlSeconds());
        response.addCookie(cookie);

        response.setHeader("Cache-Control", "no-store");
        response.setContentType("image/jpeg");
        var responseOutputStream = response.getOutputStream();
        responseOutputStream.write(pair.getSecond());
        responseOutputStream.flush();
        responseOutputStream.close();
        return CompletableFuture.completedFuture(null);
    }
}
