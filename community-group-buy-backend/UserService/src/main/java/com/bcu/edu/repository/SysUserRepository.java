package com.bcu.edu.repository;

import com.bcu.edu.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * 用户Repository
 */
@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<SysUser> findByUsername(String username);

    /**
     * 根据手机号查找用户
     */
    Optional<SysUser> findByPhone(String phone);

    /**
     * 根据微信OpenID查找用户
     */
    Optional<SysUser> findByWxOpenid(String wxOpenid);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 根据角色查询用户列表
     */
    List<SysUser> findByRole(Integer role);

    /**
     * 根据状态查询用户列表
     */
    List<SysUser> findByStatus(Integer status);

    /**
     * 根据角色和状态查询
     */
    List<SysUser> findByRoleAndStatus(Integer role, Integer status);

    /**
     * 模糊搜索用户（用户名或真实姓名）
     */
    @Query("SELECT u FROM SysUser u WHERE u.username LIKE %:keyword% OR u.realName LIKE %:keyword%")
    List<SysUser> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 统计指定角色的用户数量
     */
    long countByRole(Integer role);

    /**
     * 统计指定状态的用户数量
     */
    long countByStatus(Integer status);

    /**
     * 根据社区ID查询用户列表（v3.0新增）
     */
    List<SysUser> findByCommunityId(Long communityId);

    /**
     * 查询指定社区内的团长（v3.0新增）
     */
    List<SysUser> findByCommunityIdAndRole(Long communityId, Integer role);

    /**
     * 统计社区内的用户数量（v3.0新增）
     */
    long countByCommunityId(Long communityId);
}

