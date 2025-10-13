package com.bcu.edu.dto.response;

import com.bcu.edu.entity.UserAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long accountId;
    private Long userId;
    private BigDecimal balance;
    private BigDecimal freezeAmount;
    private BigDecimal totalAmount;
    private LocalDateTime updateTime;

    /**
     * 从实体类转换
     */
    public static AccountResponse fromEntity(UserAccount account) {
        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .userId(account.getUserId())
                .balance(account.getBalance())
                .freezeAmount(account.getFreezeAmount())
                .totalAmount(account.getBalance().add(account.getFreezeAmount()))
                .updateTime(account.getUpdateTime())
                .build();
    }
}

