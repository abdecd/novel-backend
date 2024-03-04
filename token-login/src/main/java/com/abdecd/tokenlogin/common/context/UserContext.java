package com.abdecd.tokenlogin.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;

public class UserContext {
    private static final ThreadLocal<UserContextHolder> userContext = new TransmittableThreadLocal<>();

    public static void setUserId(Long userId) {
        userContext.get().setUserId(userId);
    }

    public static Long getUserId() {
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
        private Long userId;
        private Byte permission;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
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
