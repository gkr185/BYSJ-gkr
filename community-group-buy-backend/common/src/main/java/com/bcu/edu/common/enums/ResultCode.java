package com.bcu.edu.common.enums;

import lombok.Getter;

/**
 * 统一返回结果码枚举
 * 
 * @author 耿康瑞
 * @date 2025-10-12
 */
@Getter
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "请求的资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "数据冲突"),
    
    // 业务错误 1xxx
    BUSINESS_ERROR(1000, "业务处理失败"),
    VALIDATION_ERROR(1001, "参数校验失败"),
    VALIDATE_FAILED(1001, "参数校验失败"), // 别名
    DUPLICATE_ERROR(1002, "数据重复"),
    
    // 用户相关 10xx
    USER_NOT_FOUND(1010, "用户不存在"),
    USER_DISABLED(1011, "用户已被禁用"),
    USER_ACCOUNT_DISABLED(1011, "用户账户已被禁用"), // 别名
    USERNAME_ALREADY_EXISTS(1012, "用户名已存在"),
    USER_ALREADY_EXISTS(1012, "用户名已存在"), // 别名
    PHONE_ALREADY_EXISTS(1013, "手机号已被注册"),
    USER_PHONE_EXISTS(1013, "手机号已被注册"), // 别名
    PASSWORD_ERROR(1014, "密码错误"),
    OLD_PASSWORD_ERROR(1015, "原密码错误"),
    
    // 认证相关 11xx
    TOKEN_INVALID(1100, "Token无效"),
    TOKEN_EXPIRED(1101, "Token已过期"),
    TOKEN_MISSING(1102, "缺少Token"),
    
    // 商品相关 12xx
    PRODUCT_NOT_FOUND(1200, "商品不存在"),
    PRODUCT_STOCK_NOT_ENOUGH(1201, "商品库存不足"),
    PRODUCT_OFFLINE(1202, "商品已下架"),
    CATEGORY_NOT_FOUND(1203, "分类不存在"),
    
    // 拼团相关 13xx
    GROUP_BUY_NOT_FOUND(1300, "拼团活动不存在"),
    GROUP_BUY_NOT_STARTED(1301, "拼团活动未开始"),
    GROUP_BUY_ENDED(1302, "拼团活动已结束"),
    GROUP_BUY_FULL(1303, "拼团人数已满"),
    ALREADY_JOINED_GROUP(1304, "您已参与该拼团"),
    
    // 订单相关 14xx
    ORDER_NOT_FOUND(1400, "订单不存在"),
    ORDER_ALREADY_PAID(1401, "订单已支付"),
    ORDER_ALREADY_CANCELLED(1402, "订单已取消"),
    ORDER_TIMEOUT(1403, "订单已超时"),
    ORDER_STATUS_ERROR(1404, "订单状态错误"),
    
    // 支付相关 15xx
    PAYMENT_FAILED(1500, "支付失败"),
    BALANCE_NOT_ENOUGH(1501, "余额不足"),
    INSUFFICIENT_BALANCE(1501, "余额不足"), // 别名
    REFUND_FAILED(1502, "退款失败"),
    
    // 团长相关 16xx
    LEADER_NOT_FOUND(1600, "团长不存在"),
    LEADER_NOT_APPROVED(1601, "团长资质未审核通过"),
    LEADER_ALREADY_EXISTS(1602, "您已经是团长"),
    
    // 地址相关 17xx
    ADDRESS_NOT_FOUND(1700, "收货地址不存在"),
    ADDRESS_OUT_OF_RANGE(1701, "收货地址超出配送范围"),
    
    // 服务器错误 5xx
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 返回信息
     */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}

