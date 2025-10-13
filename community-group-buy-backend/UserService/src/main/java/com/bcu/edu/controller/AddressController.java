package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.AddressRequest;
import com.bcu.edu.dto.response.AddressResponse;
import com.bcu.edu.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地址Controller
 */
@Tag(name = "地址管理", description = "用户收货地址管理接口")
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "新增收货地址", description = "为当前用户新增一个收货地址")
    @PostMapping("/add/{userId}")
    public Result<AddressResponse> addAddress(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Valid @RequestBody AddressRequest request) {
        return Result.success(addressService.addAddress(userId, request));
    }

    @Operation(summary = "更新收货地址", description = "更新指定的收货地址信息")
    @PutMapping("/update/{userId}/{addressId}")
    public Result<AddressResponse> updateAddress(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "地址ID") @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        return Result.success(addressService.updateAddress(userId, addressId, request));
    }

    @Operation(summary = "删除收货地址", description = "删除指定的收货地址")
    @DeleteMapping("/delete/{userId}/{addressId}")
    public Result<Void> deleteAddress(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "地址ID") @PathVariable Long addressId) {
        addressService.deleteAddress(userId, addressId);
        return Result.success();
    }

    @Operation(summary = "获取用户所有地址", description = "查询当前用户的所有收货地址")
    @GetMapping("/list/{userId}")
    public Result<List<AddressResponse>> getUserAddresses(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(addressService.getUserAddresses(userId));
    }

    @Operation(summary = "获取默认地址", description = "获取用户的默认收货地址")
    @GetMapping("/default/{userId}")
    public Result<AddressResponse> getDefaultAddress(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(addressService.getDefaultAddress(userId));
    }

    @Operation(summary = "设置默认地址", description = "将指定地址设置为默认收货地址")
    @PutMapping("/default/{userId}/{addressId}")
    public Result<AddressResponse> setDefaultAddress(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "地址ID") @PathVariable Long addressId) {
        return Result.success(addressService.setDefaultAddress(userId, addressId));
    }

    @Operation(summary = "获取地址详情", description = "查询指定地址的详细信息")
    @GetMapping("/detail/{userId}/{addressId}")
    public Result<AddressResponse> getAddressById(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "地址ID") @PathVariable Long addressId) {
        return Result.success(addressService.getAddressById(userId, addressId));
    }
}

