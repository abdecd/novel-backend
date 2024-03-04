package com.abdecd.tokenlogin.service;

import com.abdecd.tokenlogin.common.constant.Constant;
import com.abdecd.tokenlogin.common.property.AllProperties;
import com.abdecd.tokenlogin.common.util.JwtUtils;
import com.abdecd.tokenlogin.common.util.PwdUtils;
import com.abdecd.tokenlogin.mapper.UserMapper;
import com.abdecd.tokenlogin.pojo.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
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
public class UserBaseService {
    @Autowired
    UserMapper userMapper;
    @Resource
    AllProperties allProperties;

    public User loginById(
            Integer id,
            String password,
            Class<? extends RuntimeException> exceptionClass,
            String pwdErrMsg,
            String accountLockedMsg
    ) {
        //1、根据用户名查询数据库中的数据
        var user = userMapper.selectById(id);
        log.info("user:{}", user);
        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (user == null) {
            //账号不存在
            return null;
        }

        //密码比对
        String hashPwd;
        try {
            hashPwd = PwdUtils.encodePwd(user.getId().toString(), password);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("unknown error");
        }
        if (!hashPwd.equals(user.getPassword())) {
            //密码错误
            throwException(exceptionClass, pwdErrMsg);
        }

        if (Objects.equals(user.getStatus(), Constant.DISABLE)) {
            //账号被锁定
            throwException(exceptionClass, accountLockedMsg);
        }

        //3、返回实体对象
        return user;
    }

    public User login(
            SFunction<User, ?> queryKey,
            Object queryValue,
            String password,
            Class<? extends RuntimeException> exceptionClass,
            String pwdErrMsg,
            String accountLockedMsg
    ) {
        var user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(queryKey, queryValue));
        if (user == null) return null;
        return loginById(user.getId(), password, exceptionClass, pwdErrMsg, accountLockedMsg);
    }

    public String generateUserToken(User user) {
        return JwtUtils.getInstance().encodeJWT(allProperties.getJwtTtlSeconds(), Map.of(Constant.JWT_ID, user.getId(), Constant.JWT_PERMISSION, user.getPermission()));
    }

    @Transactional
    public int signup(String password, byte permission) {
        // 注册
        try {
            var user = new User()
                    .setPermission(permission)
                    .setStatus((byte) 1);
            userMapper.insert(user);
            var userId = user.getId();
            userMapper.updateById(user
                    .setPassword(PwdUtils.encodePwd(userId.toString(), password))
            );
            return userId;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("unknown error");
        }
    }

    public void forgetPassword(Integer id, String newPassword) {
        // 重置密码
        try {
            userMapper.updateById(new User()
                    .setId(id)
                    .setPassword(PwdUtils.encodePwd(id.toString(), newPassword))
            );
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("unknown error");
        }
    }

    private void throwException(Class<? extends RuntimeException> exceptionClass, String errMessage) {
        RuntimeException exception;
        try {
            exception = exceptionClass.getConstructor(String.class).newInstance(errMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw exception;
    }
}
