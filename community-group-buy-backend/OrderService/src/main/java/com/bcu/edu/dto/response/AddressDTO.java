package com.bcu.edu.dto.response;

import lombok.Data;

/**
 * 地址DTO
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
public class AddressDTO {

    private Long addressId;

    /**
     * 收货人姓名
     */
    private String receiver;

    /**
     * 收货人电话
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
     * 区县
     */
    private String district;

    /**
     * 详细地址
     */
    private String detail;

    /**
     * 经度
     */
    private java.math.BigDecimal longitude;

    /**
     * 纬度
     */
    private java.math.BigDecimal latitude;

    /**
     * 完整地址
     */
    public String getFullAddress() {
        return (province != null ? province : "") + 
               (city != null ? city : "") + 
               (district != null ? district : "") + 
               (detail != null ? detail : "");
    }
}

