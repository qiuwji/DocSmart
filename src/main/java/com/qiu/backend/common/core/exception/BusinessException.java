package com.qiu.backend.common.core.exception;

import com.qiu.backend.common.core.Result.IErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final IErrorCode errorCode;

    private String data;

    public BusinessException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(IErrorCode errorCode, String data) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = data;
    }
}
