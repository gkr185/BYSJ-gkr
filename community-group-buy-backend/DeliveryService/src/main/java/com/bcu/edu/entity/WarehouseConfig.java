package com.bcu.edu.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仓库配置实体
 * 
 * <p>用于配置配送路径的起点信息
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "warehouse_config")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "仓库配置")
public class WarehouseConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("仓库ID")
    @Schema(description = "仓库ID", example = "1")
    private Long id;

    @Column(name = "warehouse_name", nullable = false, length = 100)
    @Comment("仓库名称")
    @Schema(description = "仓库名称", example = "中心仓库", required = true)
    private String warehouseName;

    @Column(name = "address", nullable = false, length = 255)
    @Comment("仓库地址")
    @Schema(description = "仓库地址", example = "北京市朝阳区示例地址123号", required = true)
    private String address;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 6)
    @Comment("经度")
    @Schema(description = "经度", example = "116.397128", required = true)
    private BigDecimal longitude;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 6)
    @Comment("纬度")
    @Schema(description = "纬度", example = "39.916527", required = true)
    private BigDecimal latitude;

    @Column(name = "is_default", nullable = false)
    @Comment("是否默认仓库（0-否；1-是）")
    @Schema(description = "是否默认仓库", example = "1", required = true)
    private Integer isDefault;

    @Column(name = "status", nullable = false)
    @Comment("状态（0-禁用；1-启用）")
    @Schema(description = "状态", example = "1", required = true)
    private Integer status;

    @Column(name = "contact_person", length = 50)
    @Comment("联系人")
    @Schema(description = "联系人", example = "张三")
    private String contactPerson;

    @Column(name = "contact_phone", length = 20)
    @Comment("联系电话")
    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Column(name = "description", length = 500)
    @Comment("描述")
    @Schema(description = "描述", example = "主要配送仓库，负责市区配送业务")
    private String description;

    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2025-11-13 09:00:00")
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    @Comment("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间", example = "2025-11-13 09:30:00")
    private LocalDateTime updateTime;

    // 便利方法

    /**
     * 是否为默认仓库
     */
    public boolean isDefaultWarehouse() {
        return Integer.valueOf(1).equals(this.isDefault);
    }

    /**
     * 设置为默认仓库
     */
    public void setAsDefault() {
        this.isDefault = 1;
    }

    /**
     * 取消默认仓库
     */
    public void unsetDefault() {
        this.isDefault = 0;
    }

    /**
     * 是否启用
     */
    public boolean isEnabled() {
        return Integer.valueOf(1).equals(this.status);
    }

    /**
     * 启用仓库
     */
    public void enable() {
        this.status = 1;
    }

    /**
     * 禁用仓库
     */
    public void disable() {
        this.status = 0;
    }

    /**
     * 获取坐标字符串（格式：经度,纬度）
     */
    public String getCoordinateString() {
        return longitude + "," + latitude;
    }
}
