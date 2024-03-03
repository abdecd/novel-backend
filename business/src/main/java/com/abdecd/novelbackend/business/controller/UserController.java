package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.pojo.dto.user.LoginDTO;
import com.abdecd.novelbackend.business.service.CommonService;
import com.abdecd.novelbackend.business.service.UserService;
import com.abdecd.novelbackend.common.constant.Constant;
import com.abdecd.novelbackend.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户接口")
@Slf4j
@RestController
@RequestMapping("user")
public class UserController {
    @Resource
    UserService userService;
    @Resource
    CommonService commonService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid LoginDTO loginDTO, HttpServletRequest request) {
        commonService.verifyCaptcha(loginDTO.getCaptcha(), getCookieValue(request, Constant.COOKIE_KEY_VERIFY_CODE_ID));
        var user = userService.login(loginDTO);
        var token = userService.getUserToken(user);
        return Result.success(token);
    }

    public String getCookieValue(HttpServletRequest request, String cookieName) {
        // 获取所有Cookie对象的数组
        var cookies = request.getCookies();
        // 遍历Cookie数组查找指定名称的Cookie
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // 匹配cookie名称
                if (cookie.getName().equals(cookieName)) {
                    // 返回匹配的Cookie值
                    return cookie.getValue();
                }
            }
        }
        // 如果未找到，则返回null或其他默认值
        return null;
    }
}
