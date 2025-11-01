package com.bcu.edu.enums;

import lombok.Getter;

/**
 * 支付状态枚举
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Getter
public enum PayStatus {
    
    FAILED(0, "失败"),
    SUCCESS(1, "成功");
    
    private final Integer code;
    private final String description;
    
    PayStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public static PayStatus fromCode(Integer code) {
        for (PayStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的支付状态: " + code);
    }
}

