package com.abdecd.novelbackend.business.exceptionhandler;

import com.abdecd.novelbackend.common.constant.MessageConstant;
import com.abdecd.novelbackend.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.regex.Pattern;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler
    public Result<String> exceptionHandler(Exception ex) {
        if (ex instanceof ErrorResponse e) {
            return Result.error(e.getStatusCode().value(), e.getBody().getTitle());
        }
        log.error("全局异常捕获：{}", ex.toString());
        return Result.error(500, MessageConstant.UNKNOWN_ERROR);
    }
    @ExceptionHandler
    public Result<String> exceptionHandler(BaseException ex) {
        log.info("业务异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
    @ExceptionHandler
    public Result<String> exceptionHandler(MethodArgumentNotValidException ex) {
        log.info("参数校验失败：{}", ex.toString());
        return Result.error(ex.getAllErrors().getFirst().getDefaultMessage());
    }
    @ExceptionHandler
    public Result<String> exceptionHandler(HandlerMethodValidationException ex) {
        log.info("参数校验失败：{}", ex.toString());
        return Result.error(ex.getAllErrors().getFirst().getDefaultMessage());
    }
    @ExceptionHandler
    public Result<String> exceptionHandler(MethodArgumentTypeMismatchException ex) {
        log.info("参数类型错误：{}", ex.toString());
        return Result.error("参数类型错误");
    }
    @ExceptionHandler
    public Result<String> exceptionHandler(HttpMessageNotReadableException ex) {
        log.info("参数错误：{}", ex.toString());
        return Result.error("参数错误");
    }
    static Pattern dulplicatePattern = Pattern.compile("Duplicate entry '(.*?)' for key '(.*?)'");
    static Pattern fkPattern = Pattern.compile("CONSTRAINT `(.*?)` FOREIGN KEY \\(`(.*?)`\\) REFERENCES `(.*?)` \\(`(.*?)`\\)");
    @ExceptionHandler
    public Result<String> exceptionHandler(DataIntegrityViolationException ex) {
        var msg = ex.getMessage();
        log.info("违反数据库约束：{}", msg);
        var matcher1 = dulplicatePattern.matcher(msg);
        if (matcher1.find()) return Result.error("重复的值：'" + matcher1.group(1)+"'");
        var matcher2 = fkPattern.matcher(msg);
        if (matcher2.find()) return Result.error("字段 '" + matcher2.group(2)+"' 对应的内容不存在");
        return Result.error(MessageConstant.DB_ERROR);
    }
}
