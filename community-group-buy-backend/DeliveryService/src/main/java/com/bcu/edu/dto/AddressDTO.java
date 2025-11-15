package com.bcu.edu.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 地址信息DTO（UserService返回）
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Data
public class AddressDTO {

    /**
     * 地址ID
     */
    private Long addressId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收件人
     */
    private String receiver;

    /**
     * 收件电话
     */
    private String phone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区/县
     */
    private String district;

    /**
     * 详细地址
     */
    private String detail;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 完整地址（拼接）
     */
    public String getFullAddress() {
        return (province != null ? province : "") +
                (city != null ? city : "") +
                (district != null ? district : "") +
                (detail != null ? detail : "");
    }
}

