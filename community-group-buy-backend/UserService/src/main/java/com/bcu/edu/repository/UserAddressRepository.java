package com.bcu.edu.repository;

import com.bcu.edu.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户地址Repository
 */
@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {

    /**
     * 查询用户的所有地址
     */
    List<UserAddress> findByUserId(Long userId);

    /**
     * 查询用户的默认地址
     */
    Optional<UserAddress> findByUserIdAndIsDefault(Long userId, Integer isDefault);

    /**
     * 统计用户地址数量
     */
    long countByUserId(Long userId);

    /**
     * 取消用户的所有默认地址（设置默认地址前调用）
     */
    @Modifying
    @Query("UPDATE UserAddress a SET a.isDefault = 0 WHERE a.userId = :userId")
    void clearDefaultAddress(@Param("userId") Long userId);

    /**
     * 删除用户的所有地址
     */
    void deleteByUserId(Long userId);
}

