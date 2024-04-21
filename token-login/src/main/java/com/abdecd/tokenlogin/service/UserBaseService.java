package com.abdecd.tokenlogin.service;

import com.abdecd.tokenlogin.common.constant.Constant;
import com.abdecd.tokenlogin.common.context.UserContext;
import com.abdecd.tokenlogin.common.property.AllProperties;
import com.abdecd.tokenlogin.common.util.JwtUtils;
import com.abdecd.tokenlogin.common.util.PwdUtils;
import com.abdecd.tokenlogin.mapper.UserMapper;
import com.abdecd.tokenlogin.pojo.entity.User;
import jakarta.annotation.Nonnull;
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
    @Resource
    LoginBlackListManager loginBlackListManager;

    /**
     * 登录
     * @param user 需要是从表里直接查出来的
     * @param password :
     * @param exceptionClass :
     * @param pwdErrMsg :
     * @param accountLockedMsg :
     * @return :
     */
    public User login(
            User user,
            String password,
            Class<? extends RuntimeException> exceptionClass,
            String pwdErrMsg,
            String accountLockedMsg
    ) {
        // 用户不存在
        if (user == null) return null;

        // 密码解密并验证格式
        try {
            password = PwdUtils.getEncryptedPwd(password);
        } catch (RuntimeException e) {
            throwException(exceptionClass, e.getMessage());
        }
        if (!Constant.PASSWORD_PATTERN.matcher(password).find()) {
            throwException(exceptionClass, pwdErrMsg);
        }
        // 密码验证
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

        return user;
    }

    /**
     * 强制登录
     * @param user 需要是从表里直接查出来的
     * @return :
     */
    public User forceLogin(User user) {
        return user;
    }

    public String generateUserToken(@Nonnull User user) {
        var claims = new HashMap<String, String>();
        claims.put(Constant.JWT_ID, user.getId().toString());
        claims.put(Constant.JWT_PERMISSION, user.getPermission());
        return JwtUtils.getInstance().encodeJWT(allProperties.getJwtTtlSeconds(), claims);
    }

    public String refreshUserToken() {
        var claims = new HashMap<String, String>();
        claims.put(Constant.JWT_ID, UserContext.getUserId().toString());
        claims.put(Constant.JWT_PERMISSION, UserContext.getPermission());
        return JwtUtils.getInstance().encodeJWT(allProperties.getJwtTtlSeconds(), claims);
    }

    @Transactional
    public User signup(String password, String permission, Class<? extends RuntimeException> exceptionClass) {
        try {
            // 解密并验证密码格式
            password = PwdUtils.getEncryptedPwd(password);
            if (!Constant.PASSWORD_PATTERN.matcher(password).find()) {
                throwException(exceptionClass, "invalid password");
            }
            // 创建账号
            var user = User.ofEmpty()
                    .setPassword("")
                    .setPermission(permission)
                    .setStatus((byte) 1);
            userMapper.insert(user);
            var userId = user.getId();
            userMapper.updateById(user
                    .setPassword(PwdUtils.encodePwd(userId.toString(), password))
            );
            return user;
        } catch (RuntimeException e) {
            throwException(exceptionClass, e.getMessage());
        }
        return null; // never reach
    }

    /**
     * 重置密码
     */
    public void forgetPassword(Integer id, String newPassword, Class<? extends RuntimeException> exceptionClass) {
        try {
            // 解密并验证密码格式
            newPassword = PwdUtils.getEncryptedPwd(newPassword);
            if (!Constant.PASSWORD_PATTERN.matcher(newPassword).find()) {
                throwException(exceptionClass, "invalid password");
            }
            userMapper.updateById(new User()
                    .setId(id)
                    .setPassword(PwdUtils.encodePwd(id.toString(), newPassword))
            );
        } catch (RuntimeException e) {
            throwException(exceptionClass, e.getMessage());
        }
        forceLogout(id);
    }

    public void forceLogout(Integer userId) {
        loginBlackListManager.forceLogout(userId);
    }

    public void deleteAccount(User user) {
        forceLogout(user.getId());
        userMapper.deleteById(user);
    }
    public void deleteAccount(Integer userId) {
        forceLogout(userId);
        userMapper.deleteById(userId);
    }

    /**
     * 返回Base64编码的公钥 为pem格式去掉开头和结尾的-----BEGIN PUBLIC KEY-----和-----END PUBLIC KEY-----
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
