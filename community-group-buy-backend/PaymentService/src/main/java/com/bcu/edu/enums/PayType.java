package com.bcu.edu.enums;

import lombok.Getter;

/**
 * 支付类型枚举
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Getter
public enum PayType {
    
    WECHAT(1, "微信支付"),
    ALIPAY(2, "支付宝支付"),
    BALANCE(3, "余额支付");
    
    private final Integer code;
    private final String description;
    
    PayType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public static PayType fromCode(Integer code) {
        for (PayType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的支付类型: " + code);
    }
}

