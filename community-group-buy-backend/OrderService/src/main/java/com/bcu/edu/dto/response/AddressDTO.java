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

    private String receiver;

    private String phone;

    private String province;

    private String city;

    private String district;

    private String detail;

    /**
     * 完整地址
     */
    public String getFullAddress() {
        return province + city + district + detail;
    }
}

