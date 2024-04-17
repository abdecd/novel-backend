package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.business.mapper.ReaderDetailMapper;
import com.abdecd.novelbackend.business.pojo.dto.user.LoginByEmailDTO;
import com.abdecd.novelbackend.business.pojo.dto.user.LoginDTO;
import com.abdecd.novelbackend.business.pojo.dto.user.ResetPwdDTO;
import com.abdecd.novelbackend.business.pojo.dto.user.SignUpDTO;
import com.abdecd.novelbackend.business.pojo.entity.ReaderDetail;
import com.abdecd.novelbackend.common.constant.MessageConstant;
import com.abdecd.novelbackend.common.constant.StatusConstant;
import com.abdecd.tokenlogin.mapper.UserMapper;
import com.abdecd.tokenlogin.pojo.entity.User;
import com.abdecd.tokenlogin.service.UserBaseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Slf4j
public class UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    UserBaseService userBaseService;
    @Autowired
    CommonService commonService;
    @Autowired
    ReaderDetailMapper readerDetailMapper;

    public User login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        User user = null;
        try {
            user = userBaseService.loginById(
                    Integer.parseInt(username),
                    password,
                    BaseException.class,
                    MessageConstant.LOGIN_PASSWORD_ERROR,
                    MessageConstant.ACCOUNT_LOCKED
            );
        } catch (NumberFormatException ignored) {}
        if (user == null) {
            user = userBaseService.login(
                    User::getEmail,
                    username,
                    password,
                    BaseException.class,
                    MessageConstant.LOGIN_PASSWORD_ERROR,
                    MessageConstant.ACCOUNT_LOCKED
            );
        }
        return user;
    }

    public User loginByEmail(LoginByEmailDTO loginByEmailDTO) {
        // 验证邮箱
        commonService.verifyEmail(loginByEmailDTO.getEmail(), loginByEmailDTO.getVerifyCode());
        // 登录
        return userBaseService.forceLogin(User::getEmail, loginByEmailDTO.getEmail());
    }

    public String generateUserToken(@Nonnull User user) {
        return userBaseService.generateUserToken(user);
    }

    @Transactional
    public User signup(SignUpDTO signUpDTO) {
        // 验证邮箱
        commonService.verifyEmail(signUpDTO.getEmail(), signUpDTO.getVerifyCode());
        // 注册
        var user = userBaseService.signup(signUpDTO.getPassword(), "1", BaseException.class);
        if (user == null) throw new BaseException(MessageConstant.SIGNUP_FAILED);
        userMapper.updateById(user.setEmail(signUpDTO.getEmail()));
        readerDetailMapper.insert(new ReaderDetail()
                .setUserId(user.getId())
                .setNickname(signUpDTO.getNickname())
                .setAvatar("")
                .setSignature("")
        );
        return user;
    }

    public void forgetPassword(ResetPwdDTO resetPwdDTO) {
        // 验证邮箱
        commonService.verifyEmail(resetPwdDTO.getEmail(), resetPwdDTO.getVerifyCode());
        // 重置密码
        var user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, resetPwdDTO.getEmail())
        );
        userBaseService.forgetPassword(user.getId(), resetPwdDTO.getNewPassword(), BaseException.class);
    }

    public String refreshUserToken() {
        return userBaseService.refreshUserToken();
    }

    public void deleteAccount(Integer userId, String verifyCode) {
        var user = userMapper.selectById(userId);
        if (Objects.equals(user.getStatus(), StatusConstant.DISABLE))
            throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
        // 验证邮箱
        commonService.verifyEmail(user.getEmail(), verifyCode);
        // 删除账号
        userBaseService.deleteAccount(user);
    }
}
