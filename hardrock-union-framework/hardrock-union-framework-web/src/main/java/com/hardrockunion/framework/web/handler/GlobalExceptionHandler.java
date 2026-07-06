package com.hardrockunion.framework.web.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.hardrockunion.framework.core.domain.Result;
import com.hardrockunion.framework.core.domain.ResultCode;
import com.hardrockunion.framework.core.exception.BusinessException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        return Result.failure(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({
        BindException.class,
        MethodArgumentNotValidException.class,
        HttpMessageNotReadableException.class,
        HttpRequestMethodNotSupportedException.class
    })
    public Result<Void> handleBadRequest(Exception ex) {
        return Result.failure(ResultCode.BAD_REQUEST.getCode(), ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNotFound(NoResourceFoundException ex) {
        return Result.failure(ResultCode.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return Result.failure(ResultCode.SYSTEM_ERROR);
    }
}
