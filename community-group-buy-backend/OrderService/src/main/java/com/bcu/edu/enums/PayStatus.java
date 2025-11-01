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

    /**
     * 未支付
     */
    UNPAID(0, "未支付"),

    /**
     * 已支付
     */
    PAID(1, "已支付");

    private final Integer code;
    private final String description;

    PayStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static PayStatus fromCode(Integer code) {
        for (PayStatus status : PayStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid PayStatus code: " + code);
    }
}

