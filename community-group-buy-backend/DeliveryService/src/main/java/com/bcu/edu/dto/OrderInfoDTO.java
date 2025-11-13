package com.bcu.edu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单信息DTO
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Data
public class OrderInfoDTO {

    private Long orderId;

    private String orderSn;

    private Long userId;

    private Long leaderId;

    private BigDecimal totalAmount;

    private BigDecimal paymentAmount;

    private Integer orderStatus;

    private Integer payStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    private Long receiveAddressId;

    private String dispatchGroup;

    private Long deliveryId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 冗余字段（用于配送）
    private String receiverName;
    private String receiverPhone;
    private String deliveryAddress;
    
    /**
     * 是否待发货
     */
    public boolean isPendingShipment() {
        return orderStatus != null && orderStatus == 2; // 假设2为待发货状态
    }

    /**
     * 是否已支付
     */
    public boolean isPaid() {
        return payStatus != null && payStatus == 1; // 假设1为已支付状态
    }

    /**
     * 是否可以发货
     */
    public boolean canShip() {
        return isPaid() && isPendingShipment();
    }
}
