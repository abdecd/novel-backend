package com.abdecd.tokenlogin.service;

import com.abdecd.tokenlogin.common.constant.Constant;
import com.abdecd.tokenlogin.common.context.UserContext;
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

import java.util.Base64;
import java.util.HashMap;
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
        String hashPwd = "";
        try {
            hashPwd = PwdUtils.encodePwd(user.getId().toString(), password);
        } catch (RuntimeException e) {
            throwException(exceptionClass, e.getMessage());
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
        var claims = new HashMap<String, String>();
        claims.put(Constant.JWT_ID, user.getId().toString());
        claims.put(Constant.JWT_PERMISSION, user.getPermission().toString());
        return JwtUtils.getInstance().encodeJWT(allProperties.getJwtTtlSeconds(), claims);
    }

    public String refreshUserToken() {
        var claims = new HashMap<String, String>();
        claims.put(Constant.JWT_ID, UserContext.getUserId().toString());
        claims.put(Constant.JWT_PERMISSION, UserContext.getPermission().toString());
        return JwtUtils.getInstance().encodeJWT(allProperties.getJwtTtlSeconds(), claims);
    }

    @Transactional
    public int signup(String password, byte permission, Class<? extends RuntimeException> exceptionClass) {
        // 注册
        try {
            var user = User.ofEmpty()
                    .setPassword("")
                    .setPermission(permission)
                    .setStatus((byte) 1);
            userMapper.insert(user);
            var userId = user.getId();
            userMapper.updateById(user
                    .setPassword(PwdUtils.encodePwd(userId.toString(), password))
            );
            return userId;
        } catch (RuntimeException e) {
            throwException(exceptionClass, e.getMessage());
        }
        return -1; // never reach
    }

    /**
     * 重置密码
     */
    public void forgetPassword(Integer id, String newPassword, Class<? extends RuntimeException> exceptionClass) {
        try {
            userMapper.updateById(new User()
                    .setId(id)
                    .setPassword(PwdUtils.encodePwd(id.toString(), newPassword))
            );
        } catch (RuntimeException e) {
            throwException(exceptionClass, e.getMessage());
        }
    }

    /**
     * 返回Base64编码的公钥
     */
    public String getPublicKey() {
        return Base64.getEncoder().encodeToString(PwdUtils.publicKey.getEncoded());
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
