package com.bcu.edu.controller;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.UserLoginRequest;
import com.bcu.edu.dto.request.UserRegisterRequest;
import com.bcu.edu.dto.request.UserUpdateRequest;
import com.bcu.edu.dto.response.LoginResponse;
import com.bcu.edu.dto.response.UserInfoResponse;
import com.bcu.edu.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户Controller
 */
@Tag(name = "用户管理", description = "用户注册、登录、信息管理等接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册", description = "新用户注册，支持普通用户和团长注册")
    @OperationLog(value = "用户注册", module = "用户管理", recordParams = false)
    @PostMapping("/register")
    public Result<UserInfoResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT Token")
    @OperationLog(value = "用户登录", module = "认证管理", recordParams = false)
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return Result.success(userService.login(request));
    }

    @Operation(summary = "获取当前用户信息", description = "根据Token获取当前登录用户信息")
    @GetMapping("/info/{userId}")
    public Result<UserInfoResponse> getUserInfo(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(userService.getUserInfo(userId));
    }

    @Operation(summary = "更新用户信息", description = "更新用户基本信息（密码、手机号、头像等）")
    @OperationLog(value = "编辑用户", module = "用户管理")
    @PutMapping("/update/{userId}")
    public Result<UserInfoResponse> updateUserInfo(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        return Result.success(userService.updateUserInfo(userId, request));
    }

    @Operation(summary = "删除用户（禁用）", description = "逻辑删除用户，将用户状态设为禁用")
    @OperationLog(value = "删除用户", module = "用户管理")
    @DeleteMapping("/delete/{userId}")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        userService.deleteUser(userId);
        return Result.success();
    }

    @Operation(summary = "搜索用户", description = "根据关键词搜索用户（用户名或真实姓名），keyword为空时返回所有用户")
    @GetMapping("/search")
    public Result<List<UserInfoResponse>> searchUsers(
            @Parameter(description = "搜索关键词（可选）") @RequestParam(required = false, defaultValue = "") String keyword) {
        return Result.success(userService.searchUsers(keyword));
    }

    @Operation(summary = "按角色查询用户", description = "查询指定角色的所有用户")
    @GetMapping("/role/{role}")
    public Result<List<UserInfoResponse>> getUsersByRole(
            @Parameter(description = "角色（1-普通用户；2-团长；3-管理员）") @PathVariable Integer role) {
        return Result.success(userService.getUsersByRole(role));
    }

    @Operation(summary = "更改用户角色", description = "管理员功能：更改用户角色")
    @OperationLog(value = "修改用户角色", module = "用户管理")
    @PutMapping("/role/{userId}")
    public Result<UserInfoResponse> changeUserRole(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "新角色") @RequestParam Integer role) {
        return Result.success(userService.changeUserRole(userId, role));
    }

    @Operation(summary = "更改用户状态", description = "管理员功能：启用或禁用用户")
    @OperationLog(value = "修改用户状态", module = "用户管理")
    @PutMapping("/status/{userId}")
    public Result<UserInfoResponse> changeUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "状态（0-禁用；1-正常）") @RequestParam Integer status) {
        return Result.success(userService.changeUserStatus(userId, status));
    }

    @Operation(summary = "用户统计", description = "获取用户统计信息（总数、各角色数量等）")
    @GetMapping("/statistics")
    public Result<Map<String, Long>> getUserStatistics() {
        return Result.success(userService.getUserStatistics());
    }

    @Operation(summary = "管理员创建用户", description = "管理员功能：手动创建新用户账号")
    @OperationLog(value = "创建用户", module = "用户管理", recordParams = false)
    @PostMapping("/admin/create")
    public Result<UserInfoResponse> adminCreateUser(@Valid @RequestBody UserRegisterRequest request) {
        return Result.success(userService.register(request));
    }
}

