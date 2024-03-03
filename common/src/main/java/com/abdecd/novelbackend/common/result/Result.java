package com.abdecd.novelbackend.common.result;

import lombok.Data;

@Data
public class Result<T> {
    private int code; //编码：1成功，0和其它数字为失败
    private String msg; //错误信息
    private T data; //数据

    public static Result<String> success() {
        Result<String> result = new Result<>();
        result.code = 1;
        result.data = "ok";
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<>();
        result.data = object;
        result.code = 1;
        return result;
    }

    public static Result<String> error(String msg) {
        Result<String> result = new Result<>();
        result.msg = msg;
        result.code = 0;
        return result;
    }
}
