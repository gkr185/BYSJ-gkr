package com.bcu.edu.repository;

import com.bcu.edu.entity.UserFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户反馈Repository
 */
@Repository
public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {

    /**
     * 查询用户的所有反馈
     */
    List<UserFeedback> findByUserId(Long userId);

    /**
     * 分页查询用户的反馈
     */
    Page<UserFeedback> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据状态查询反馈列表
     */
    List<UserFeedback> findByStatus(Integer status);

    /**
     * 分页查询指定状态的反馈
     */
    Page<UserFeedback> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据类型查询反馈
     */
    List<UserFeedback> findByType(Integer type);

    /**
     * 统计用户反馈数量
     */
    long countByUserId(Long userId);

    /**
     * 统计指定状态的反馈数量
     */
    long countByStatus(Integer status);

    /**
     * 统计指定类型的反馈数量
     */
    long countByType(Integer type);
}

