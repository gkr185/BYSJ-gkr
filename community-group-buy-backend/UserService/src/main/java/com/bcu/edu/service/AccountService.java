package com.bcu.edu.service;

import com.bcu.edu.common.enums.ResultCode;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.dto.response.AccountResponse;
import com.bcu.edu.entity.UserAccount;
import com.bcu.edu.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 账户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserAccountRepository accountRepository;

    /**
     * 获取用户账户信息
     * 如果账户不存在，自动创建一个（兼容旧数据）
     */
    @Transactional
    public AccountResponse getAccount(Long userId) {
        UserAccount account = accountRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // 账户不存在，自动创建一个（初始余额为0）
                    log.warn("用户账户不存在，自动创建: userId={}", userId);
                    UserAccount newAccount = new UserAccount();
                    newAccount.setUserId(userId);
                    return accountRepository.save(newAccount);
                });
        return AccountResponse.fromEntity(account);
    }

    /**
     * 充值
     */
    @Transactional
    public AccountResponse recharge(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "充值金额必须大于0");
        }

        UserAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "账户不存在"));

        account.addBalance(amount);
        account = accountRepository.save(account);

        log.info("充值成功: userId={}, amount={}, newBalance={}", userId, amount, account.getBalance());

        return AccountResponse.fromEntity(account);
    }

    /**
     * 扣款（支付订单等）
     */
    @Transactional
    public AccountResponse deduct(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "扣款金额必须大于0");
        }

        UserAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "账户不存在"));

        boolean success = account.deductBalance(amount);
        if (!success) {
            throw new BusinessException(ResultCode.INSUFFICIENT_BALANCE);
        }

        account = accountRepository.save(account);

        log.info("扣款成功: userId={}, amount={}, newBalance={}", userId, amount, account.getBalance());

        return AccountResponse.fromEntity(account);
    }

    /**
     * 冻结金额
     */
    @Transactional
    public AccountResponse freeze(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "冻结金额必须大于0");
        }

        UserAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "账户不存在"));

        boolean success = account.freezeAmount(amount);
        if (!success) {
            throw new BusinessException(ResultCode.INSUFFICIENT_BALANCE);
        }

        account = accountRepository.save(account);

        log.info("冻结金额成功: userId={}, amount={}, freezeAmount={}", userId, amount, account.getFreezeAmount());

        return AccountResponse.fromEntity(account);
    }

    /**
     * 解冻金额
     */
    @Transactional
    public AccountResponse unfreeze(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "解冻金额必须大于0");
        }

        UserAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "账户不存在"));

        if (account.getFreezeAmount().compareTo(amount) < 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "冻结金额不足");
        }

        account.unfreezeAmount(amount);
        account = accountRepository.save(account);

        log.info("解冻金额成功: userId={}, amount={}, newBalance={}", userId, amount, account.getBalance());

        return AccountResponse.fromEntity(account);
    }

    // ========== Feign调用接口（供其他微服务调用） ==========

    /**
     * 扣减余额（带Saga事务ID，供其他服务调用）
     * @param userId 用户ID
     * @param amount 扣减金额
     * @param sagaId Saga事务ID（用于分布式事务追踪）
     */
    @Transactional
    public void deductBalanceForFeign(Long userId, BigDecimal amount, String sagaId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "扣款金额必须大于0");
        }

        UserAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "账户不存在"));

        boolean success = account.deductBalance(amount);
        if (!success) {
            throw new BusinessException(ResultCode.INSUFFICIENT_BALANCE);
        }

        account = accountRepository.save(account);

        log.info("[Saga-{}] 余额扣减成功: userId={}, amount={}, newBalance={}", 
            sagaId, userId, amount, account.getBalance());
    }

    /**
     * 返还余额（带Saga事务ID，用于退款或补偿事务）
     * @param userId 用户ID
     * @param amount 返还金额
     * @param sagaId Saga事务ID
     */
    @Transactional
    public void refundBalanceForFeign(Long userId, BigDecimal amount, String sagaId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "返还金额必须大于0");
        }

        UserAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "账户不存在"));

        account.addBalance(amount);
        account = accountRepository.save(account);

        log.info("[Saga-{}] 余额返还成功: userId={}, amount={}, newBalance={}", 
            sagaId, userId, amount, account.getBalance());
    }

    /**
     * 验证余额是否充足（供其他服务调用）
     * @param userId 用户ID
     * @param amount 需要的金额
     * @return 是否充足
     */
    public boolean checkBalance(Long userId, BigDecimal amount) {
        UserAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "账户不存在"));
        
        return account.getBalance().compareTo(amount) >= 0;
    }

    /**
     * 增加余额（供LeaderService佣金结算调用）
     * @param userId 用户ID（团长ID）
     * @param amount 增加金额（佣金金额）
     * @param remark 备注（结算批次号）
     */
    @Transactional
    public void addBalanceForCommission(Long userId, BigDecimal amount, String remark) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "增加金额必须大于0");
        }

        UserAccount account = accountRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // 账户不存在，自动创建（兼容旧数据）
                    log.warn("用户账户不存在，自动创建: userId={}", userId);
                    UserAccount newAccount = new UserAccount();
                    newAccount.setUserId(userId);
                    return accountRepository.save(newAccount);
                });

        account.addBalance(amount);
        account = accountRepository.save(account);

        log.info("佣金结算-余额增加成功: userId={}, amount={}, newBalance={}, remark={}", 
            userId, amount, account.getBalance(), remark);
    }
}

