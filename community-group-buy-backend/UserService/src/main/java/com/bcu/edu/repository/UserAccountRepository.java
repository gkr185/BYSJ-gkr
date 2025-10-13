package com.bcu.edu.repository;

import com.bcu.edu.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户账户Repository
 */
@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    /**
     * 根据用户ID查找账户
     */
    Optional<UserAccount> findByUserId(Long userId);

    /**
     * 检查用户是否已有账户
     */
    boolean existsByUserId(Long userId);

    /**
     * 删除用户账户
     */
    void deleteByUserId(Long userId);
}

