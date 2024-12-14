package com.abdecd.novelbackend.business.exceptionhandler;

public class BaseException extends RuntimeException {
    public BaseException(String msg) {
        super(msg);
    }
}
