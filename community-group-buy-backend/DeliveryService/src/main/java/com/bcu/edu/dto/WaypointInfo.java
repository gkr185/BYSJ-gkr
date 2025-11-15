package com.bcu.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 途经点信息DTO
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaypointInfo {

    /**
     * 序号（路径顺序，从0开始）
     */
    private Integer sequence;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 地址
     */
    private String address;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 收件人姓名
     */
    private String receiverName;

    /**
     * 收件电话
     */
    private String receiverPhone;

    /**
     * 类型
     * leader_store-团长团点；user_address-用户地址
     */
    private String type;
}

