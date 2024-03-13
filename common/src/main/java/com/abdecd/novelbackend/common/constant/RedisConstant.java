package com.abdecd.novelbackend.common.constant;

public class RedisConstant {
    public static final String READER_HISTORY = "readerHistory::";
    public static final int READER_HISTORY_SIZE = 1000;
    public static final String READER_HISTORY_TIMESTAMP = "timestamp::readerHistory:";
    public static final String READER_HISTORY_A_NOVEL_TIMESTAMP = "timestamp::readerHistoryANovel:";
    public static final int COMMENT_FOR_NOVEL_SIZE = 500;
    public static final String COMMENT_FOR_NOVEL_TIMESTAMP = "timestamp::commentForNovel:";
    public static final String LIMIT_VERIFY_EMAIL = "limitVerifyEmail:";
    public static final String LIMIT_UPLOAD_IMG = "limitUploadImg:";
}
