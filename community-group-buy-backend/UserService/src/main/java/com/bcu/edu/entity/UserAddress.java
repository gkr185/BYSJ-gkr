package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

/**
 * 用户收货地址表
 * 支持地理路径计算（Dijkstra算法）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_address", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Comment("用户收货地址表")
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    @Comment("地址ID")
    private Long addressId;

    @Column(name = "user_id", nullable = false)
    @Comment("关联用户ID")
    private Long userId;

    @Column(name = "receiver", nullable = false, length = 50)
    @Comment("收件人")
    private String receiver;

    @Column(name = "phone", nullable = false, length = 128)
    @Comment("收件人电话（AES加密存储）")
    private String phone;

    @Column(name = "province", nullable = false, length = 20)
    @Comment("省份")
    private String province;

    @Column(name = "city", nullable = false, length = 20)
    @Comment("城市")
    private String city;

    @Column(name = "district", nullable = false, length = 20)
    @Comment("区/县")
    private String district;

    @Column(name = "detail", nullable = false, length = 255)
    @Comment("详细地址")
    private String detail;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 6)
    @Comment("地址经度（Dijkstra算法输入）")
    private BigDecimal longitude;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 6)
    @Comment("地址纬度（Dijkstra算法输入）")
    private BigDecimal latitude;

    @Column(name = "is_default", nullable = false)
    @Comment("是否默认（0-否；1-是）")
    private Integer isDefault = 0;

    /**
     * 获取完整地址
     */
    public String getFullAddress() {
        return province + city + district + detail;
    }
}

