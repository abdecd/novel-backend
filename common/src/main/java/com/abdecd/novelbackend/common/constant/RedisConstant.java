package com.abdecd.novelbackend.common.constant;

public class RedisConstant {
    public static final String READER_HISTORY = "readerHistory::";
    public static final int READER_HISTORY_SIZE = 1000;
    public static final String READER_HISTORY_RECORD_LIMIT = "readerHistoryRecordLimit:";
    public static final String READER_HISTORY_TIMESTAMP = "timestamp::readerHistory:";
    public static final String READER_FAVORITES = "readerFavorites:";
    public static final int READER_FAVORITES_SIZE = 100;
    public static final int COMMENT_FOR_NOVEL_SIZE = 500;
    public static final String COMMENT_FOR_NOVEL_TIMESTAMP = "timestamp::commentForNovel:";
    public static final String NOVEL_DAILY_READ = "novelDailyRead:";
    public static final String LIMIT_VERIFY_EMAIL = "limitVerifyEmail:";
    public static final int LIMIT_VERIFY_EMAIL_RESET_TIME = 60;
    public static final String LIMIT_UPLOAD_IMG = "limitUploadImg:";
    public static final int LIMIT_UPLOAD_IMG_CNT = 300;
    public static final int LIMIT_UPLOAD_IMG_RESET_TIME = 1; // day
    public static final String LIMIT_IP_RATE = "limitIpRate:";
    public static final int LIMIT_IP_RATE_CNT = 200;
    public static final int LIMIT_IP_RATE_RESET_TIME = 3;
}
