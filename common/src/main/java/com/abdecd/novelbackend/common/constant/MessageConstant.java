package com.abdecd.novelbackend.common.constant;

public interface MessageConstant {
    String LOGIN_FAIL = "登录失败";
    String LOGIN_PASSWORD_ERROR = "账号或密码错误";
    String ACCOUNT_LOCKED = "账号已被锁定";
    String SIGNUP_FAILED = "注册失败";
    String CAPTCHA_EXPIRED = "验证码已过期，请重新获取";
    String CAPTCHA_ERROR = "验证码错误";
    String EMAIL_SEND_FAIL = "邮件发送失败";
    String UNKNOWN_ERROR = "未知错误";
    String NOVEL_NOT_EXIST = "小说不存在";
    String NOVEL_VOLUME_NOT_FOUND = "对应小说卷不存在";
    String FAVORITES_EXIST = "已经收藏过了";
    String FAVORITES_EXCEED_LIMIT = "收藏数量超过上限";
    String RATE_LIMIT = "请求过于频繁，请稍后再试";
    String IMG_FILE_EMPTY = "文件为空";
    String IMG_FILE_TYPE_ERROR = "文件类型不正确";
    String IMG_FILE_SIZE_ERROR = "文件大小超过限制";
    String IMG_FILE_UPLOAD_FAIL = "图片上传失败";
    String IMG_FILE_UPLOAD_LIMIT = "图片上传次数达到上限";
    String IMG_FILE_NOT_FOUND = "图片不存在或格式错误";
    String IMG_FILE_READ_FAIL = "图片查看失败";
    String DB_ERROR = "数据库操作异常";
}
