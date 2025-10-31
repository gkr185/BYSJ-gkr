package com.bcu.edu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 团状态枚举
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Getter
@AllArgsConstructor
public enum TeamStatus {
    
    /**
     * 拼团中
     */
    JOINING(0, "拼团中"),
    
    /**
     * 已成团
     */
    SUCCESS(1, "已成团"),
    
    /**
     * 已失败
     */
    FAILED(2, "已失败");
    
    private final Integer code;
    private final String desc;
    
    /**
     * 根据code获取枚举
     */
    public static TeamStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TeamStatus status : TeamStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}

