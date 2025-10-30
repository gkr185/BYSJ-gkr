package com.bcu.edu.service;

import com.bcu.edu.entity.Community;
import com.bcu.edu.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * 社区服务
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 核心功能：
 * 1. 社区CRUD操作
 * 2. Haversine距离计算算法（根据用户经纬度匹配最近社区）
 * 3. 社区服务范围判断
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityRepository communityRepository;

    /**
     * 地球半径（单位：米）
     * 平均半径：6371000米
     */
    private static final double EARTH_RADIUS_METERS = 6371000.0;

    /**
     * 创建社区
     */
    @Transactional
    public Community createCommunity(Community community) {
        // 检查社区名称是否已存在
        if (communityRepository.existsByName(community.getName())) {
            throw new IllegalArgumentException("社区名称已存在：" + community.getName());
        }

        // 验证经纬度范围
        validateCoordinates(community.getLatitude(), community.getLongitude());

        log.info("创建社区：{}", community.getName());
        return communityRepository.save(community);
    }

    /**
     * 更新社区信息
     */
    @Transactional
    public Community updateCommunity(Long communityId, Community updatedCommunity) {
        Community existing = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("社区不存在：" + communityId));

        // 如果修改了名称，检查新名称是否已存在
        if (!existing.getName().equals(updatedCommunity.getName()) &&
                communityRepository.existsByName(updatedCommunity.getName())) {
            throw new IllegalArgumentException("社区名称已存在：" + updatedCommunity.getName());
        }

        // 验证经纬度
        validateCoordinates(updatedCommunity.getLatitude(), updatedCommunity.getLongitude());

        existing.setName(updatedCommunity.getName());
        existing.setAddress(updatedCommunity.getAddress());
        existing.setProvince(updatedCommunity.getProvince());
        existing.setCity(updatedCommunity.getCity());
        existing.setDistrict(updatedCommunity.getDistrict());
        existing.setLatitude(updatedCommunity.getLatitude());
        existing.setLongitude(updatedCommunity.getLongitude());
        existing.setServiceRadius(updatedCommunity.getServiceRadius());
        existing.setDescription(updatedCommunity.getDescription());

        log.info("更新社区：{}", communityId);
        return communityRepository.save(existing);
    }

    /**
     * 删除社区（软删除，修改状态为2）
     */
    @Transactional
    public void deleteCommunity(Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new IllegalArgumentException("社区不存在：" + communityId));

        community.setStatus(2); // 2-已关闭
        communityRepository.save(community);

        log.info("删除社区：{}", communityId);
    }

    /**
     * 根据ID查询社区
     */
    public Optional<Community> getCommunityById(Long communityId) {
        return communityRepository.findById(communityId);
    }

    /**
     * 查询所有正常运营的社区
     */
    public List<Community> getAllActiveCommunities() {
        return communityRepository.findAllActiveCommunities();
    }

    /**
     * 根据状态查询社区列表
     */
    public List<Community> getCommunitiesByStatus(Integer status) {
        return communityRepository.findByStatus(status);
    }

    /**
     * 【核心算法】根据用户经纬度匹配最近的社区
     * 
     * 算法流程：
     * 1. 粗筛：用矩形范围过滤候选社区（提升性能）
     * 2. 精算：用Haversine公式计算精确距离
     * 3. 排序：返回最近的社区
     * 
     * @param userLatitude 用户纬度
     * @param userLongitude 用户经度
     * @return 最近的社区（如果在服务范围内）
     */
    public Optional<Community> findNearestCommunity(BigDecimal userLatitude, BigDecimal userLongitude) {
        validateCoordinates(userLatitude, userLongitude);

        // 步骤1：计算粗筛范围（假设最大搜索半径10公里）
        double searchRadiusKm = 10.0;
        BigDecimal[] bounds = calculateBounds(userLatitude, userLongitude, searchRadiusKm);

        // 步骤2：查询候选社区（矩形范围过滤）
        List<Community> candidates = communityRepository.findByCoordinateRange(
                bounds[0], // minLat
                bounds[1], // maxLat
                bounds[2], // minLon
                bounds[3]  // maxLon
        );

        if (candidates.isEmpty()) {
            log.warn("用户位置({}, {})周围10公里内无社区", userLatitude, userLongitude);
            return Optional.empty();
        }

        // 步骤3：用Haversine公式计算精确距离，找出最近的社区
        Community nearestCommunity = null;
        double minDistance = Double.MAX_VALUE;

        for (Community community : candidates) {
            double distance = calculateHaversineDistance(
                    userLatitude, userLongitude,
                    community.getLatitude(), community.getLongitude()
            );

            // 检查是否在服务范围内
            if (distance <= community.getServiceRadius() && distance < minDistance) {
                minDistance = distance;
                nearestCommunity = community;
            }
        }

        if (nearestCommunity != null) {
            log.info("用户({}, {})匹配到社区：{}，距离：{}米",
                    userLatitude, userLongitude, nearestCommunity.getName(), (int) minDistance);
        } else {
            log.warn("用户位置({}, {})不在任何社区服务范围内", userLatitude, userLongitude);
        }

        return Optional.ofNullable(nearestCommunity);
    }

    /**
     * 【核心算法】Haversine公式计算两点间球面距离
     * 
     * 公式：
     * a = sin²(Δφ/2) + cos(φ1) * cos(φ2) * sin²(Δλ/2)
     * c = 2 * atan2(√a, √(1−a))
     * d = R * c
     * 
     * 其中：
     * - φ 表示纬度（latitude）
     * - λ 表示经度（longitude）
     * - R 表示地球半径（6371000米）
     * 
     * @param lat1 点1纬度
     * @param lon1 点1经度
     * @param lat2 点2纬度
     * @param lon2 点2经度
     * @return 距离（单位：米）
     */
    private double calculateHaversineDistance(
            BigDecimal lat1, BigDecimal lon1,
            BigDecimal lat2, BigDecimal lon2
    ) {
        // 转换为弧度
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lon1Rad = Math.toRadians(lon1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lon2Rad = Math.toRadians(lon2.doubleValue());

        // 计算差值
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Haversine公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 距离（米）
        return EARTH_RADIUS_METERS * c;
    }

    /**
     * 计算矩形搜索范围（用于数据库粗筛）
     * 
     * 简化算法：
     * - 纬度1度 ≈ 111km
     * - 经度1度 ≈ 111km * cos(纬度)
     * 
     * @param centerLat 中心纬度
     * @param centerLon 中心经度
     * @param radiusKm 搜索半径（公里）
     * @return [minLat, maxLat, minLon, maxLon]
     */
    private BigDecimal[] calculateBounds(BigDecimal centerLat, BigDecimal centerLon, double radiusKm) {
        double latDelta = radiusKm / 111.0; // 纬度偏移量
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(centerLat.doubleValue()))); // 经度偏移量

        BigDecimal minLat = centerLat.subtract(BigDecimal.valueOf(latDelta));
        BigDecimal maxLat = centerLat.add(BigDecimal.valueOf(latDelta));
        BigDecimal minLon = centerLon.subtract(BigDecimal.valueOf(lonDelta));
        BigDecimal maxLon = centerLon.add(BigDecimal.valueOf(lonDelta));

        return new BigDecimal[]{minLat, maxLat, minLon, maxLon};
    }

    /**
     * 验证经纬度范围
     * - 纬度：-90 ~ 90
     * - 经度：-180 ~ 180
     */
    private void validateCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("经纬度不能为空");
        }

        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 ||
                latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new IllegalArgumentException("纬度范围必须在-90到90之间：" + latitude);
        }

        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 ||
                longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new IllegalArgumentException("经度范围必须在-180到180之间：" + longitude);
        }
    }
}

