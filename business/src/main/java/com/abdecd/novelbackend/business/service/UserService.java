package com.abdecd.novelbackend.business.service;

import com.abdecd.novelbackend.business.common.exception.BaseException;
import com.abdecd.novelbackend.business.common.properties.TtlProperties;
import com.abdecd.novelbackend.business.common.util.JwtUtils;
import com.abdecd.novelbackend.business.common.util.PwdUtils;
import com.abdecd.novelbackend.business.mapper.ReaderDetailMapper;
import com.abdecd.novelbackend.business.mapper.UserMapper;
import com.abdecd.novelbackend.business.pojo.dto.user.LoginDTO;
import com.abdecd.novelbackend.business.pojo.dto.user.ResetPwdDTO;
import com.abdecd.novelbackend.business.pojo.dto.user.SignUpDTO;
import com.abdecd.novelbackend.business.pojo.entity.ReaderDetail;
import com.abdecd.novelbackend.business.pojo.entity.User;
import com.abdecd.novelbackend.common.constant.Constant;
import com.abdecd.novelbackend.common.constant.MessageConstant;
import com.abdecd.novelbackend.common.constant.StatusConstant;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    CommonService commonService;
    @Autowired
    ReaderDetailMapper readerDetailMapper;
    @Resource
    TtlProperties ttlProperties;


    public User login(LoginDTO loginDTO) {
        Integer id = loginDTO.getUserId();
        String password = loginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        var user = userMapper.selectById(id);
        log.info("user:{}", user);
        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (user == null) {
            //账号不存在
            throw new BaseException(MessageConstant.LOGIN_ACCOUNT_NOT_FOUND);
        }

        //密码比对
        String hashPwd;
        try {
            hashPwd = PwdUtils.encodePwd(user.getEmail(), password);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        if (!hashPwd.equals(user.getPassword())) {
            //密码错误
            throw new BaseException(MessageConstant.LOGIN_PASSWORD_ERROR);
        }

        if (Objects.equals(user.getStatus(), StatusConstant.DISABLE)) {
            //账号被锁定
            throw new BaseException(MessageConstant.LOGIN_ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return user;
    }

    public String getUserToken(User user) {
        return JwtUtils.getInstance().encodeJWT(ttlProperties.getJwtTtlSeconds(), Map.of(Constant.JWT_ID, user.getId(), Constant.JWT_PERMISSION, user.getPermission()));
    }

    @Transactional
    public int signup(SignUpDTO signUpDTO) {
        // 验证邮箱
        commonService.verifyEmail(signUpDTO.getEmail(), signUpDTO.getVerifyCode());
        // 注册
        try {
            var user = new User()
                    .setEmail(signUpDTO.getEmail())
                    .setPassword(PwdUtils.encodePwd(signUpDTO.getEmail(), signUpDTO.getPassword()))
                    .setPermission((byte) 1)
                    .setStatus((byte) 1);
            userMapper.insert(user);

            int id = user.getId();
            readerDetailMapper.insert(new ReaderDetail()
                    .setUserId(user.getId())
                    .setNickname(signUpDTO.getNickname())
                    .setAvatar("")
                    .setSignature("")
            );
            return id;
        } catch (NoSuchAlgorithmException e) {
            throw new BaseException(MessageConstant.UNKNOWN_ERROR);
        }
    }

    public void resetPassword(ResetPwdDTO resetPwdDTO) {
        // 验证邮箱
        commonService.verifyEmail(resetPwdDTO.getEmail(), resetPwdDTO.getVerifyCode());
        // 重置密码
        try {
            userMapper.update(new LambdaUpdateWrapper<User>()
                    .eq(User::getEmail, resetPwdDTO.getEmail())
                    .set(User::getPassword, PwdUtils.encodePwd(resetPwdDTO.getEmail(), resetPwdDTO.getNewPassword()))
            );
        } catch (NoSuchAlgorithmException e) {
            throw new BaseException(MessageConstant.UNKNOWN_ERROR);
        }
    }
}
