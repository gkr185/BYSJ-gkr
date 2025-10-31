package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拼团活动实体
 * 
 * <p>表名: group_buy
 * <p>描述: 拼团活动模板，管理员创建，定义拼团规则
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Entity
@Table(name = "group_buy")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuy {
    
    /**
     * 活动ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;
    
    /**
     * 关联商品ID（跨库关联到product_service_db.product）
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    /**
     * 拼团价
     */
    @Column(name = "group_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal groupPrice;
    
    /**
     * 成团人数（2-10人）
     */
    @Column(name = "required_num", nullable = false)
    private Integer requiredNum;
    
    /**
     * 最大人数限制（可为null，表示无限制）
     */
    @Column(name = "max_num")
    private Integer maxNum;
    
    /**
     * 活动开始时间
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    /**
     * 活动结束时间
     */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    /**
     * 活动状态
     * 0-未开始；1-进行中；2-已结束；3-异常
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;
    
    /**
     * 活动二维码URL
     */
    @Column(name = "qrcode_url", length = 255)
    private String qrcodeUrl;
    
    /**
     * 活动专属链接
     */
    @Column(name = "link", unique = true, length = 255)
    private String link;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (status == null) {
            status = 1;
        }
    }
}

