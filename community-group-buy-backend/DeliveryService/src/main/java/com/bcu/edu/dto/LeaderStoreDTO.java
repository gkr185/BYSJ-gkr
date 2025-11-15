package com.bcu.edu.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 团长团点信息DTO（LeaderService返回）
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Data
public class LeaderStoreDTO {

    /**
     * 团点ID
     */
    private Long storeId;

    /**
     * 团长ID
     */
    private Long leaderId;

    /**
     * 团长姓名
     */
    private String leaderName;

    /**
     * 团长手机号
     */
    private String leaderPhone;

    /**
     * 社区ID
     */
    private Long communityId;

    /**
     * 社区名称
     */
    private String communityName;

    /**
     * 团点名称
     */
    private String storeName;

    /**
     * 团点地址
     */
    private String address;

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
    private String detailAddress;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 最大配送范围（米）
     */
    private Integer maxDeliveryRange;

    /**
     * 完整地址（拼接）
     */
    public String getFullAddress() {
        return (province != null ? province : "") +
                (city != null ? city : "") +
                (district != null ? district : "") +
                (detailAddress != null ? detailAddress : "");
    }
}

