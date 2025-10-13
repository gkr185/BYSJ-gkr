package com.bcu.edu.common.exception;

import com.bcu.edu.common.enums.ResultCode;

/**
 * 业务异常类
 * 用于业务逻辑中主动抛出的异常
 *
 * @author 耿康瑞
 * @date 2025-10-12
 */
public class BusinessException extends BaseException {

    private static final long serialVersionUID = 1L;

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Integer code, String message) {
        super(code, message);
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode);
    }

    public BusinessException(ResultCode resultCode, String customMessage) {
        super(resultCode, customMessage);
    }
}

