package com.bcu.edu.dto.response;

import lombok.Data;

/**
 * 社区信息DTO（Feign响应）
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Data
public class CommunityDTO {
    /**
     * 社区ID
     */
    private Long communityId;
    
    /**
     * 社区名称
     */
    private String communityName;
    
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
    private String address;
}

