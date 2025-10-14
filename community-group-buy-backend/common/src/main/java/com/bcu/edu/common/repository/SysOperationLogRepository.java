package com.bcu.edu.common.repository;

import com.bcu.edu.common.entity.SysOperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统操作日志Repository接口
 * 
 * @author AI Assistant
 * @since 2025-10-14
 */
@Repository
public interface SysOperationLogRepository extends JpaRepository<SysOperationLog, Long>, 
                                                     JpaSpecificationExecutor<SysOperationLog> {
    
    /**
     * 根据模块查询操作日志
     * 
     * @param module 操作模块
     * @return 日志列表
     */
    List<SysOperationLog> findByModule(String module);
    
    /**
     * 根据用户ID查询操作日志
     * 
     * @param userId 用户ID
     * @return 日志列表
     */
    List<SysOperationLog> findByUserId(Long userId);
    
    /**
     * 根据操作结果查询日志
     * 
     * @param result 操作结果（SUCCESS/FAIL）
     * @return 日志列表
     */
    List<SysOperationLog> findByResult(String result);
    
    /**
     * 根据时间范围查询日志
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    List<SysOperationLog> findByCreateTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询所有操作模块（去重）
     * 用于前端筛选下拉框
     * 
     * @return 模块列表
     */
    @Query("SELECT DISTINCT s.module FROM SysOperationLog s ORDER BY s.module")
    List<String> findAllModules();
    
    /**
     * 统计某个用户的操作次数
     * 
     * @param userId 用户ID
     * @return 操作次数
     */
    @Query("SELECT COUNT(s) FROM SysOperationLog s WHERE s.userId = ?1")
    Long countByUserId(Long userId);
    
    /**
     * 统计某个模块的操作次数
     * 
     * @param module 操作模块
     * @return 操作次数
     */
    @Query("SELECT COUNT(s) FROM SysOperationLog s WHERE s.module = ?1")
    Long countByModule(String module);
    
    /**
     * 统计失败操作次数
     * 
     * @return 失败次数
     */
    @Query("SELECT COUNT(s) FROM SysOperationLog s WHERE s.result = 'FAIL'")
    Long countFailedOperations();
    
    /**
     * 查询最近N条日志
     * 
     * @param limit 数量限制
     * @return 日志列表
     */
    @Query(value = "SELECT * FROM sys_operation_log ORDER BY create_time DESC LIMIT ?1", nativeQuery = true)
    List<SysOperationLog> findRecentLogs(int limit);
}

