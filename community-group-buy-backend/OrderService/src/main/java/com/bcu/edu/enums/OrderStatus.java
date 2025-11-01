package com.bcu.edu.enums;

import lombok.Getter;

/**
 * 订单状态枚举
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Getter
public enum OrderStatus {

    /**
     * 待支付
     */
    PENDING_PAYMENT(0, "待支付"),

    /**
     * 待发货
     */
    PENDING_DELIVERY(1, "待发货"),

    /**
     * 配送中
     */
    IN_DELIVERY(2, "配送中"),

    /**
     * 已送达
     */
    DELIVERED(3, "已送达"),

    /**
     * 已取消
     */
    CANCELLED(4, "已取消"),

    /**
     * 退款中
     */
    REFUNDING(5, "退款中"),

    /**
     * 已退款
     */
    REFUNDED(6, "已退款");

    private final Integer code;
    private final String description;

    OrderStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据code获取枚举
     */
    public static OrderStatus fromCode(Integer code) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid OrderStatus code: " + code);
    }
}

