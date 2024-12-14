package com.abdecd.novelbackend.common.constant;

public interface RedisConstant {
    String READER_HISTORY = "readerHistory::";
    int READER_HISTORY_SIZE = 1000;
    String READER_HISTORY_RECORD_LIMIT = "readerHistoryRecordLimit:";
    String READER_HISTORY_TIMESTAMP = "timestamp::readerHistory:";
    String READER_FAVORITES = "readerFavorites:";
    int READER_FAVORITES_SIZE = 100;
    int COMMENT_FOR_NOVEL_SIZE = 500;
    String COMMENT_FOR_NOVEL_TIMESTAMP = "timestamp::commentForNovel:";
    String NOVEL_DAILY_READ = "novelDailyRead:";
    String LIMIT_VERIFY_EMAIL = "limitVerifyEmail:";
    int LIMIT_VERIFY_EMAIL_RESET_TIME = 60;
    String LIMIT_UPLOAD_IMG = "limitUploadImg:";
    int LIMIT_UPLOAD_IMG_CNT = 300;
    int LIMIT_UPLOAD_IMG_RESET_TIME = 1; // day
    String LIMIT_IP_RATE = "limitIpRate:";
    int LIMIT_IP_RATE_CNT = 200;
    int LIMIT_IP_RATE_RESET_TIME = 3;
}
