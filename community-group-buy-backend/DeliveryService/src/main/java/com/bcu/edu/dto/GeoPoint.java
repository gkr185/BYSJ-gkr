package com.bcu.edu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 地理坐标点
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "地理坐标点")
public class GeoPoint {

    @Schema(description = "纬度", example = "39.916527", required = true)
    private BigDecimal latitude;

    @Schema(description = "经度", example = "116.397128", required = true)
    private BigDecimal longitude;

    @Schema(description = "地址描述", example = "北京市朝阳区示例地址")
    private String address;

    @Schema(description = "地址ID", example = "1001")
    private Long addressId;

    public GeoPoint(BigDecimal latitude, BigDecimal longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * 获取坐标字符串（高德API格式：经度,纬度）
     */
    public String toCoordinateString() {
        return longitude + "," + latitude;
    }

    /**
     * 从坐标字符串创建GeoPoint
     */
    public static GeoPoint fromCoordinateString(String coordinates) {
        if (coordinates == null || coordinates.trim().isEmpty()) {
            return null;
        }
        String[] parts = coordinates.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("坐标格式错误，应为 '经度,纬度'");
        }
        try {
            BigDecimal longitude = new BigDecimal(parts[0].trim());
            BigDecimal latitude = new BigDecimal(parts[1].trim());
            return new GeoPoint(latitude, longitude);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("坐标数值格式错误: " + coordinates);
        }
    }

    /**
     * 计算与另一个点的直线距离（米）
     * 使用Haversine公式
     */
    public double distanceTo(GeoPoint other) {
        if (other == null) {
            return 0.0;
        }
        
        final double R = 6371000; // 地球半径（米）
        double lat1Rad = Math.toRadians(this.latitude.doubleValue());
        double lat2Rad = Math.toRadians(other.latitude.doubleValue());
        double deltaLat = Math.toRadians(other.latitude.subtract(this.latitude).doubleValue());
        double deltaLng = Math.toRadians(other.longitude.subtract(this.longitude).doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
