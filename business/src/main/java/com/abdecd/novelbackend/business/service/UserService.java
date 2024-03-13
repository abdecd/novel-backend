package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.business.mapper.ReaderDetailMapper;
import com.abdecd.novelbackend.business.pojo.dto.user.LoginDTO;
import com.abdecd.novelbackend.business.pojo.dto.user.ResetPwdDTO;
import com.abdecd.novelbackend.business.pojo.dto.user.SignUpDTO;
import com.abdecd.novelbackend.business.pojo.entity.ReaderDetail;
import com.abdecd.novelbackend.common.constant.MessageConstant;
import com.abdecd.tokenlogin.mapper.UserMapper;
import com.abdecd.tokenlogin.pojo.entity.User;
import com.abdecd.tokenlogin.service.UserBaseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    MessageConstant.LOGIN_ACCOUNT_LOCKED
            );
        } catch (NumberFormatException ignored) {}
        if (user == null) {
            user = userBaseService.login(
                    User::getEmail,
                    username,
                    password,
                    BaseException.class,
                    MessageConstant.LOGIN_PASSWORD_ERROR,
                    MessageConstant.LOGIN_ACCOUNT_LOCKED
            );
        }
        return user;
    }

    public String generateUserToken(@Nonnull User user) {
        return userBaseService.generateUserToken(user);
    }

    @Transactional
    public User signup(SignUpDTO signUpDTO) {
        // 验证邮箱
        commonService.verifyEmail(signUpDTO.getEmail(), signUpDTO.getVerifyCode());
        // 注册
        var userId = userBaseService.signup(signUpDTO.getPassword(), (byte) 1, BaseException.class);
        userMapper.updateById(new User()
                .setId(userId)
                .setEmail(signUpDTO.getEmail())
        );
        readerDetailMapper.insert(new ReaderDetail()
                .setUserId(userId)
                .setNickname(signUpDTO.getNickname())
                .setAvatar("")
                .setSignature("")
        );
        return userMapper.selectById(userId);
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
}
