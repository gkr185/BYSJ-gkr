package com.bcu.edu.service;

import com.bcu.edu.common.enums.ResultCode;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.common.utils.SecurityUtil;
import com.bcu.edu.dto.request.AddressRequest;
import com.bcu.edu.dto.response.AddressResponse;
import com.bcu.edu.entity.UserAddress;
import com.bcu.edu.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 地址服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserAddressRepository addressRepository;

    /**
     * 新增地址
     */
    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setReceiver(request.getReceiver());
        address.setPhone(request.getPhone()); // 不加密
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        address.setLongitude(request.getLongitude());
        address.setLatitude(request.getLatitude());
        address.setIsDefault(request.getIsDefault());

        // 如果设置为默认地址，先取消其他默认地址
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            addressRepository.clearDefaultAddress(userId);
        }

        address = addressRepository.save(address);

        log.info("新增地址成功: userId={}, addressId={}", userId, address.getAddressId());

        return AddressResponse.fromEntity(address);
    }

    /**
     * 更新地址
     */
    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "地址不存在"));

        // 验证地址所有权
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        address.setReceiver(request.getReceiver());
        address.setPhone(request.getPhone()); // 不加密
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetail(request.getDetail());
        address.setLongitude(request.getLongitude());
        address.setLatitude(request.getLatitude());
        address.setIsDefault(request.getIsDefault());

        // 如果设置为默认地址，先取消其他默认地址
        if (request.getIsDefault() != null && request.getIsDefault() == 1) {
            addressRepository.clearDefaultAddress(userId);
        }

        address = addressRepository.save(address);

        log.info("更新地址成功: userId={}, addressId={}", userId, addressId);

        return AddressResponse.fromEntity(address);
    }

    /**
     * 删除地址
     */
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "地址不存在"));

        // 验证地址所有权
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        addressRepository.delete(address);

        log.info("删除地址成功: userId={}, addressId={}", userId, addressId);
    }

    /**
     * 获取用户的所有地址
     */
    public List<AddressResponse> getUserAddresses(Long userId) {
        List<UserAddress> addresses = addressRepository.findByUserId(userId);
        return addresses.stream()
                .map(AddressResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户的默认地址
     */
    public AddressResponse getDefaultAddress(Long userId) {
        UserAddress address = addressRepository.findByUserIdAndIsDefault(userId, 1)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "未设置默认地址"));
        return AddressResponse.fromEntity(address);
    }

    /**
     * 设置默认地址
     */
    @Transactional
    public AddressResponse setDefaultAddress(Long userId, Long addressId) {
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "地址不存在"));

        // 验证地址所有权
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 取消其他默认地址
        addressRepository.clearDefaultAddress(userId);

        // 设置为默认
        address.setIsDefault(1);
        address = addressRepository.save(address);

        log.info("设置默认地址成功: userId={}, addressId={}", userId, addressId);

        return AddressResponse.fromEntity(address);
    }

    /**
     * 获取地址详情
     */
    public AddressResponse getAddressById(Long userId, Long addressId) {
        UserAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "地址不存在"));

        // 验证地址所有权
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        return AddressResponse.fromEntity(address);
    }
}

