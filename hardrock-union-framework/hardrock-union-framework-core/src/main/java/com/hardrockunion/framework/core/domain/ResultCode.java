package com.hardrockunion.framework.core.domain;

public enum ResultCode {

    SUCCESS("0", "success"),
    BAD_REQUEST("400", "request is invalid"),
    UNAUTHORIZED("401", "unauthorized"),
    FORBIDDEN("403", "forbidden"),
    NOT_FOUND("404", "resource not found"),
    SYSTEM_ERROR("500", "system error");

    private final String code;
    private final String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
