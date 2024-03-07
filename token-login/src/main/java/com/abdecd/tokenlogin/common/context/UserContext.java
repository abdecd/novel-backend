package com.abdecd.tokenlogin.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;

public class UserContext {
    private static final ThreadLocal<UserContextHolder> userContext = TransmittableThreadLocal.withInitial(UserContextHolder::new);

    public static void setUserId(Integer userId) {
        userContext.get().setUserId(userId);
    }

    public static Integer getUserId() {
        return userContext.get().getUserId();
    }

    public static void setPermission(Byte permission) {
        userContext.get().setPermission(permission);
    }

    public static Byte getPermission() {
        return userContext.get().getPermission();
    }

    public static void clear() {
        userContext.remove();
    }

    public static class UserContextHolder {
        private Integer userId;
        private Byte permission;

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public Byte getPermission() {
            return permission;
        }

        public void setPermission(Byte permission) {
            this.permission = permission;
        }
    }
}
