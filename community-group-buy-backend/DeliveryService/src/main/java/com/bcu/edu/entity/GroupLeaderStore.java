package com.bcu.edu.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团长团点实体（简化版，用于Feign调用接收）
 * 
 * <p>完整定义在LeaderService中
 * <p>添加@JsonIgnoreProperties忽略未知字段（如reviewerId、reviewedAt等）
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupLeaderStore {

    private Long storeId;
    private Long leaderId;
    private String leaderName;
    private String leaderPhone;
    private Long communityId;
    private String communityName;
    private String storeName;
    private String province;
    private String city;
    private String district;
    private String address;
    private String detailAddress;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Integer maxDeliveryRange;
    private String description;
    private BigDecimal commissionRate;
    private BigDecimal totalCommission;
    private Integer status;
    
    // 以下字段用于JSON反序列化，但不影响配送业务
    private Long reviewerId;
    private String reviewComment;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

