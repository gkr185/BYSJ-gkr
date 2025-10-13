package com.bcu.edu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 地址请求（新增/更新）
 */
@Data
public class AddressRequest {

    @NotBlank(message = "收件人不能为空")
    private String receiver;

    @NotBlank(message = "收件人电话不能为空")
    @Size(min = 11, max = 11, message = "手机号长度必须为11位")
    private String phone;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "区/县不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    @NotNull(message = "经度不能为空")
    private BigDecimal longitude;

    @NotNull(message = "纬度不能为空")
    private BigDecimal latitude;

    private Integer isDefault = 0;
}

