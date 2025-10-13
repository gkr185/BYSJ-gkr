package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户账户表
 * 管理用户余额及资金流转
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_account", indexes = {
        @Index(name = "uk_user_id", columnList = "user_id", unique = true)
})
@Comment("用户账户表")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    @Comment("账户ID")
    private Long accountId;

    @Column(name = "user_id", nullable = false, unique = true)
    @Comment("关联用户ID")
    private Long userId;

    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    @Comment("可用余额")
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "freeze_amount", nullable = false, precision = 10, scale = 2)
    @Comment("冻结金额（未结算佣金/退款中）")
    private BigDecimal freezeAmount = BigDecimal.ZERO;

    @Column(name = "update_time", nullable = false)
    @Comment("最后更新时间")
    private LocalDateTime updateTime;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    /**
     * 增加余额
     */
    public void addBalance(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.add(amount);
        }
    }

    /**
     * 减少余额
     */
    public boolean deductBalance(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            if (this.balance.compareTo(amount) >= 0) {
                this.balance = this.balance.subtract(amount);
                return true;
            }
        }
        return false;
    }

    /**
     * 冻结金额
     */
    public boolean freezeAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            if (this.balance.compareTo(amount) >= 0) {
                this.balance = this.balance.subtract(amount);
                this.freezeAmount = this.freezeAmount.add(amount);
                return true;
            }
        }
        return false;
    }

    /**
     * 解冻金额
     */
    public void unfreezeAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.freezeAmount = this.freezeAmount.subtract(amount);
            this.balance = this.balance.add(amount);
        }
    }
}

