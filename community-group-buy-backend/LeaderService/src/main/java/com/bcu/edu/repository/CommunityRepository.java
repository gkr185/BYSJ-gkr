package com.bcu.edu.repository;

import com.bcu.edu.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 社区Repository
 *
 * @author 耿康瑞
 * @date 2025-10-30
 */
@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {

    /**
     * 根据社区名称查询
     */
    Optional<Community> findByName(String name);

    /**
     * 根据状态查询所有社区
     * @param status 状态：0-待审核 1-正常运营 2-已关闭
     */
    List<Community> findByStatus(Integer status);

    /**
     * 查询所有正常运营的社区
     */
    @Query("SELECT c FROM Community c WHERE c.status = 1 ORDER BY c.createdAt DESC")
    List<Community> findAllActiveCommunities();

    /**
     * 根据经纬度范围查询社区（用于距离计算优化）
     * 先用矩形范围过滤，再用Haversine公式精确计算
     * 
     * @param minLat 最小纬度
     * @param maxLat 最大纬度
     * @param minLon 最小经度
     * @param maxLon 最大经度
     */
    @Query("SELECT c FROM Community c WHERE " +
            "c.status = 1 AND " +
            "c.latitude BETWEEN :minLat AND :maxLat AND " +
            "c.longitude BETWEEN :minLon AND :maxLon")
    List<Community> findByCoordinateRange(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLon") BigDecimal minLon,
            @Param("maxLon") BigDecimal maxLon
    );

    /**
     * 检查社区名称是否已存在
     */
    boolean existsByName(String name);
}

