package com.bcu.edu.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 拼团活动状态枚举
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Getter
@AllArgsConstructor
public enum ActivityStatus {
    
    /**
     * 未开始
     */
    NOT_STARTED(0, "未开始"),
    
    /**
     * 进行中
     */
    ONGOING(1, "进行中"),
    
    /**
     * 已结束
     */
    ENDED(2, "已结束"),
    
    /**
     * 异常
     */
    ABNORMAL(3, "异常");
    
    private final Integer code;
    private final String desc;
    
    /**
     * 根据code获取枚举
     */
    public static ActivityStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ActivityStatus status : ActivityStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}

