package com.bcu.edu.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 带坐标的地址DTO
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Data
public class AddressWithCoordinatesDTO {

    private Long addressId;

    private String receiver;

    private String phone;

    private String province;

    private String city;

    private String district;

    private String detail;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private Integer isDefault;

    /**
     * 获取完整地址
     */
    public String getFullAddress() {
        return province + city + district + detail;
    }

    /**
     * 转换为GeoPoint
     */
    public GeoPoint toGeoPoint() {
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        geoPoint.setAddress(getFullAddress());
        geoPoint.setAddressId(addressId);
        return geoPoint;
    }

    /**
     * 坐标是否完整
     */
    public boolean hasCoordinates() {
        return longitude != null && latitude != null;
    }
}
