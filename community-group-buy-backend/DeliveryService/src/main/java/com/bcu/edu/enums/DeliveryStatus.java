package com.bcu.edu.enums;

/**
 * 配送状态枚举
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
public enum DeliveryStatus {
    
    /**
     * 0-待分配：配送单已创建，等待团长分配
     */
    PENDING(0, "待分配"),
    
    /**
     * 1-配送中：团长已开始配送
     */
    DELIVERING(1, "配送中"),
    
    /**
     * 2-已完成：配送任务完成
     */
    COMPLETED(2, "已完成");
    
    private final Integer code;
    private final String description;
    
    DeliveryStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举
     */
    public static DeliveryStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DeliveryStatus status : DeliveryStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的配送状态代码: " + code);
    }
}
