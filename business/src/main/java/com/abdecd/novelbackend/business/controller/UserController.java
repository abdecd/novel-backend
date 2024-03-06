package com.abdecd.novelbackend.business.controller;

import com.abdecd.novelbackend.business.pojo.dto.user.LoginDTO;
import com.abdecd.novelbackend.business.pojo.dto.user.ResetPwdDTO;
import com.abdecd.novelbackend.business.pojo.dto.user.SignUpDTO;
import com.abdecd.novelbackend.business.service.CommonService;
import com.abdecd.novelbackend.business.service.UserService;
import com.abdecd.novelbackend.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
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

    @Operation(summary = "用户登录", description = "data字段返回用户token")
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid LoginDTO loginDTO) {
        commonService.verifyCaptcha(loginDTO.getVerifyCodeId(), loginDTO.getCaptcha());
        var user = userService.login(loginDTO);
        var token = userService.generateUserToken(user);
        return Result.success(token);
    }

    @Operation(summary = "用户注册", description = "data字段返回用户id")
    @PostMapping("/signup")
    public Result<Integer> signup(@RequestBody @Valid SignUpDTO signUpDTO) {
        return Result.success(userService.signup(signUpDTO));
    }

    @Operation(summary = "忘记密码")
    @PostMapping("/forget-password")
    public Result<String> forgetPassword(@RequestBody @Valid ResetPwdDTO resetPwdDTO) {
        userService.forgetPassword(resetPwdDTO);
        return Result.success();
    }
}
