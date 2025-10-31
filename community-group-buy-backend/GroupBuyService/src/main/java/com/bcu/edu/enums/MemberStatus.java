package com.bcu.edu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 参团状态枚举
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Getter
@AllArgsConstructor
public enum MemberStatus {
    
    /**
     * 待支付
     */
    UNPAID(0, "待支付"),
    
    /**
     * 已支付
     */
    PAID(1, "已支付"),
    
    /**
     * 已成团
     */
    SUCCESS(2, "已成团"),
    
    /**
     * 已取消
     */
    CANCELLED(3, "已取消");
    
    private final Integer code;
    private final String desc;
    
    /**
     * 根据code获取枚举
     */
    public static MemberStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MemberStatus status : MemberStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}

