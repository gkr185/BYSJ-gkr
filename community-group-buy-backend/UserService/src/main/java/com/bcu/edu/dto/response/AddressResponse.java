package com.bcu.edu.dto.response;

import com.bcu.edu.entity.UserAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 地址响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private Long addressId;
    private Long userId;
    private String receiver;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private String fullAddress;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer isDefault;

    /**
     * 从实体类转换
     */
    public static AddressResponse fromEntity(UserAddress address) {
        return AddressResponse.builder()
                .addressId(address.getAddressId())
                .userId(address.getUserId())
                .receiver(address.getReceiver())
                .phone(address.getPhone())
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detail(address.getDetail())
                .fullAddress(address.getFullAddress())
                .longitude(address.getLongitude())
                .latitude(address.getLatitude())
                .isDefault(address.getIsDefault())
                .build();
    }
}

