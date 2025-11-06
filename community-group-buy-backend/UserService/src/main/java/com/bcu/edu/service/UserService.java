package com.bcu.edu.service;

import com.bcu.edu.common.enums.ResultCode;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.common.utils.JwtUtil;
import com.bcu.edu.common.utils.SecurityUtil;
import com.bcu.edu.dto.request.UserLoginRequest;
import com.bcu.edu.dto.request.UserRegisterRequest;
import com.bcu.edu.dto.request.UserUpdateRequest;
import com.bcu.edu.dto.response.LoginResponse;
import com.bcu.edu.dto.response.UserInfoResponse;
import com.bcu.edu.entity.SysUser;
import com.bcu.edu.entity.UserAccount;
import com.bcu.edu.repository.SysUserRepository;
import com.bcu.edu.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserRepository userRepository;
    private final UserAccountRepository accountRepository;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    @Transactional
    public UserInfoResponse register(UserRegisterRequest request) {
        // 检查用户名是否存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        // 检查手机号是否存在
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(ResultCode.USER_PHONE_EXISTS);
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(SecurityUtil.sha256(request.getPassword())); // SHA256加密
        user.setPhone(request.getPhone()); // 不加密
        user.setRealName(request.getRealName());
        user.setWxOpenid(request.getWxOpenid());
        user.setRole(request.getRole());
        user.setStatus(SysUser.Status.ENABLED.getCode());

        user = userRepository.save(user);

        // 创建账户
        UserAccount account = new UserAccount();
        account.setUserId(user.getUserId());
        accountRepository.save(account);

        log.info("用户注册成功: userId={}, username={}", user.getUserId(), user.getUsername());

        return UserInfoResponse.fromEntity(user);
    }

    /**
     * 用户登录
     */
    public LoginResponse login(UserLoginRequest request) {
        // 查找用户
        SysUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 验证密码
        String encryptedPassword = SecurityUtil.sha256(request.getPassword());
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "密码错误");
        }

        // 检查账户状态
        if (user.getStatus().equals(SysUser.Status.DISABLED.getCode())) {
            throw new BusinessException(ResultCode.USER_ACCOUNT_DISABLED);
        }

        // 生成JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());

        String token = jwtUtil.generateToken(user.getUsername(), claims);
        Long expiresIn = jwtUtil.getExpiration() / 1000; // 转换为秒

        log.info("用户登录成功: userId={}, username={}", user.getUserId(), user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userInfo(UserInfoResponse.fromEntity(user))
                .build();
    }

    /**
     * 获取用户信息
     */
    public UserInfoResponse getUserInfo(Long userId) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        return UserInfoResponse.fromEntity(user);
    }

    /**
     * 更新用户信息
     */
    @Transactional
    public UserInfoResponse updateUserInfo(Long userId, UserUpdateRequest request) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // 更新密码
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(SecurityUtil.sha256(request.getPassword()));
        }

        // 更新手机号
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            // 检查手机号是否被其他用户使用
            userRepository.findByPhone(request.getPhone()).ifPresent(existUser -> {
                if (!existUser.getUserId().equals(userId)) {
                    throw new BusinessException(ResultCode.USER_PHONE_EXISTS);
                }
            });
            user.setPhone(request.getPhone()); // 不加密
        }

        // 更新其他信息
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getWxOpenid() != null) {
            user.setWxOpenid(request.getWxOpenid());
        }
        // 更新所属社区
        if (request.getCommunityId() != null) {
            user.setCommunityId(request.getCommunityId());
            log.debug("更新用户所属社区: userId={}, communityId={}", userId, request.getCommunityId());
        }

        user = userRepository.save(user);

        log.info("用户信息更新成功: userId={}", userId);

        return UserInfoResponse.fromEntity(user);
    }

    /**
     * 删除用户（逻辑删除 - 禁用账户）
     */
    @Transactional
    public void deleteUser(Long userId) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        user.setStatus(SysUser.Status.DISABLED.getCode());
        userRepository.save(user);

        log.info("用户账户已禁用: userId={}", userId);
    }

    /**
     * 查询用户列表（按角色）
     */
    public List<UserInfoResponse> getUsersByRole(Integer role) {
        List<SysUser> users = userRepository.findByRole(role);
        return users.stream()
                .map(UserInfoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 搜索用户
     * keyword为空时返回所有用户
     */
    public List<UserInfoResponse> searchUsers(String keyword) {
        List<SysUser> users;
        if (keyword == null || keyword.trim().isEmpty()) {
            // 如果keyword为空，返回所有用户
            users = userRepository.findAll();
        } else {
            // 否则按关键词搜索
            users = userRepository.searchByKeyword(keyword);
        }
        return users.stream()
                .map(UserInfoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 更改用户角色（管理员功能）
     */
    @Transactional
    public UserInfoResponse changeUserRole(Long userId, Integer newRole) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        user.setRole(newRole);
        user = userRepository.save(user);

        log.info("用户角色已更改: userId={}, newRole={}", userId, newRole);

        return UserInfoResponse.fromEntity(user);
    }

    /**
     * 启用/禁用用户（管理员功能）
     */
    @Transactional
    public UserInfoResponse changeUserStatus(Long userId, Integer status) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        user.setStatus(status);
        user = userRepository.save(user);

        log.info("用户状态已更改: userId={}, status={}", userId, status);

        return UserInfoResponse.fromEntity(user);
    }

    /**
     * 统计用户数量
     */
    public Map<String, Long> getUserStatistics() {
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("totalUsers", userRepository.count());
        statistics.put("normalUsers", userRepository.countByRole(SysUser.Role.USER.getCode()));
        statistics.put("leaders", userRepository.countByRole(SysUser.Role.LEADER.getCode()));
        statistics.put("admins", userRepository.countByRole(SysUser.Role.ADMIN.getCode()));
        statistics.put("activeUsers", userRepository.countByStatus(SysUser.Status.ENABLED.getCode()));
        statistics.put("disabledUsers", userRepository.countByStatus(SysUser.Status.DISABLED.getCode()));
        return statistics;
    }

    // ========== v3.0新增：社区关联功能 ==========

    /**
     * 关联用户到社区（v3.0新增）
     * @param userId 用户ID
     * @param communityId 社区ID
     * @return 更新后的用户信息
     */
    @Transactional
    public UserInfoResponse associateCommunity(Long userId, Long communityId) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        // TODO: 调用LeaderService验证社区是否存在
        // if (!leaderServiceClient.existsCommunity(communityId)) {
        //     throw new BusinessException("社区不存在");
        // }

        user.setCommunityId(communityId);
        user = userRepository.save(user);

        log.info("用户已关联社区: userId={}, communityId={}", userId, communityId);

        return UserInfoResponse.fromEntity(user);
    }

    /**
     * 查询社区内的所有用户（v3.0新增）
     * @param communityId 社区ID
     * @return 用户列表
     */
    public List<UserInfoResponse> getUsersByCommunity(Long communityId) {
        List<SysUser> users = userRepository.findByCommunityId(communityId);
        return users.stream()
                .map(UserInfoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 查询社区内的团长（v3.0新增）
     * @param communityId 社区ID
     * @return 团长列表
     */
    public List<UserInfoResponse> getLeadersByCommunity(Long communityId) {
        List<SysUser> leaders = userRepository.findByCommunityIdAndRole(
            communityId, 
            SysUser.Role.LEADER.getCode()
        );
        return leaders.stream()
                .map(UserInfoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 统计社区内的用户数量（v3.0新增）
     * @param communityId 社区ID
     * @return 用户数量
     */
    public long countUsersByCommunity(Long communityId) {
        return userRepository.countByCommunityId(communityId);
    }

    /**
     * 更新用户角色（供LeaderService调用）
     * @param userId 用户ID
     * @param role 角色（0-普通用户 1-管理员 2-团长）
     */
    @Transactional
    public void updateUserRole(Long userId, Integer role) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        user.setRole(role);
        userRepository.save(user);

        log.info("[Feign] 用户角色已更新: userId={}, role={}", userId, role);
    }
}

