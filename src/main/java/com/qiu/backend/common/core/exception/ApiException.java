package com.qiu.backend.common.core.exception;

import com.qiu.backend.common.core.Result.IErrorCode;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException{

    private final IErrorCode errorCode;

    public ApiException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

}
