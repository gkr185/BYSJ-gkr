package com.bcu.edu.common.constant;

/**
 * 系统常量类
 *
 * @author 耿康瑞
 * @date 2025-10-12
 */
public class Constants {

    /**
     * 用户角色常量
     */
    public static class UserRole {
        /** 普通用户 */
        public static final Integer USER = 1;
        /** 团长 */
        public static final Integer LEADER = 2;
        /** 系统管理员 */
        public static final Integer ADMIN = 3;
    }

    /**
     * 用户状态常量
     */
    public static class UserStatus {
        /** 禁用 */
        public static final Integer DISABLED = 0;
        /** 正常 */
        public static final Integer NORMAL = 1;
    }

    /**
     * 审核状态常量
     */
    public static class AuditStatus {
        /** 待审核 */
        public static final Integer PENDING = 0;
        /** 审核通过 */
        public static final Integer APPROVED = 1;
        /** 审核拒绝 */
        public static final Integer REJECTED = 2;
    }

    /**
     * 商品状态常量
     */
    public static class ProductStatus {
        /** 下架 */
        public static final Integer OFFLINE = 0;
        /** 上架 */
        public static final Integer ONLINE = 1;
    }

    /**
     * 拼团活动状态常量
     */
    public static class GroupBuyStatus {
        /** 未开始 */
        public static final Integer NOT_STARTED = 0;
        /** 进行中 */
        public static final Integer IN_PROGRESS = 1;
        /** 已结束 */
        public static final Integer ENDED = 2;
        /** 异常 */
        public static final Integer ABNORMAL = 3;
    }

    /**
     * 拼团参与状态常量
     */
    public static class GroupJoinStatus {
        /** 已退出 */
        public static final Integer QUIT = 0;
        /** 参与中 */
        public static final Integer JOINING = 1;
        /** 成团 */
        public static final Integer SUCCESS = 2;
    }

    /**
     * 订单状态常量
     */
    public static class OrderStatus {
        /** 待支付 */
        public static final Integer PENDING_PAYMENT = 0;
        /** 待发货 */
        public static final Integer PENDING_DELIVERY = 1;
        /** 配送中 */
        public static final Integer IN_DELIVERY = 2;
        /** 已送达 */
        public static final Integer DELIVERED = 3;
        /** 已取消 */
        public static final Integer CANCELLED = 4;
        /** 退款中 */
        public static final Integer REFUNDING = 5;
        /** 已退款 */
        public static final Integer REFUNDED = 6;
    }

    /**
     * 支付状态常量
     */
    public static class PayStatus {
        /** 未支付 */
        public static final Integer UNPAID = 0;
        /** 已支付 */
        public static final Integer PAID = 1;
    }

    /**
     * 支付类型常量
     */
    public static class PayType {
        /** 微信支付 */
        public static final Integer WECHAT = 1;
        /** 支付宝支付 */
        public static final Integer ALIPAY = 2;
        /** 余额支付 */
        public static final Integer BALANCE = 3;
    }

    /**
     * 配送状态常量
     */
    public static class DeliveryStatus {
        /** 待分配 */
        public static final Integer PENDING = 0;
        /** 配送中 */
        public static final Integer IN_PROGRESS = 1;
        /** 已完成 */
        public static final Integer COMPLETED = 2;
    }

    /**
     * 佣金状态常量
     */
    public static class CommissionStatus {
        /** 未结算 */
        public static final Integer UNSETTLED = 0;
        /** 已结算 */
        public static final Integer SETTLED = 1;
    }

    /**
     * 反馈类型常量
     */
    public static class FeedbackType {
        /** 功能问题 */
        public static final Integer FUNCTION = 1;
        /** 商品问题 */
        public static final Integer PRODUCT = 2;
        /** 配送问题 */
        public static final Integer DELIVERY = 3;
        /** 支付问题 */
        public static final Integer PAYMENT = 4;
        /** 其他 */
        public static final Integer OTHER = 5;
    }

    /**
     * 反馈状态常量
     */
    public static class FeedbackStatus {
        /** 待处理 */
        public static final Integer PENDING = 0;
        /** 处理中 */
        public static final Integer PROCESSING = 1;
        /** 已解决 */
        public static final Integer RESOLVED = 2;
        /** 已关闭 */
        public static final Integer CLOSED = 3;
    }

    /**
     * 默认值常量
     */
    public static class Default {
        /** 默认地址 */
        public static final Integer IS_DEFAULT = 1;
        /** 非默认地址 */
        public static final Integer NOT_DEFAULT = 0;
        
        /** 拼团发起者 */
        public static final Integer IS_LAUNCHER = 1;
        /** 非拼团发起者 */
        public static final Integer NOT_LAUNCHER = 0;
    }

    /**
     * 分页常量
     */
    public static class Page {
        /** 默认页码 */
        public static final Integer DEFAULT_PAGE = 1;
        /** 默认每页大小 */
        public static final Integer DEFAULT_SIZE = 10;
        /** 最大每页大小 */
        public static final Integer MAX_SIZE = 100;
    }

    /**
     * 时间常量（毫秒）
     */
    public static class Time {
        /** 订单超时时间（30分钟） */
        public static final Long ORDER_TIMEOUT = 30 * 60 * 1000L;
        /** Token过期时间（7天） */
        public static final Long TOKEN_EXPIRE = 7 * 24 * 60 * 60 * 1000L;
        /** 验证码过期时间（5分钟） */
        public static final Long CODE_EXPIRE = 5 * 60 * 1000L;
    }

    /**
     * 缓存Key前缀
     */
    public static class CacheKey {
        /** 用户信息缓存前缀 */
        public static final String USER_INFO = "user:info:";
        /** 商品信息缓存前缀 */
        public static final String PRODUCT_INFO = "product:info:";
        /** 验证码缓存前缀 */
        public static final String VERIFY_CODE = "verify:code:";
        /** Token黑名单前缀 */
        public static final String TOKEN_BLACKLIST = "token:blacklist:";
    }

    /**
     * 正则表达式
     */
    public static class Regex {
        /** 手机号正则 */
        public static final String PHONE = "^1[3-9]\\d{9}$";
        /** 邮箱正则 */
        public static final String EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        /** 密码正则（8-20位，包含字母和数字） */
        public static final String PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$";
    }

    /**
     * 默认配置
     */
    public static class Config {
        /** 默认团长佣金比例（%） */
        public static final Double DEFAULT_COMMISSION_RATE = 5.0;
        /** 默认配送范围（米） */
        public static final Integer DEFAULT_DELIVERY_RANGE = 3000;
        /** 默认拼团人数 */
        public static final Integer DEFAULT_GROUP_NUM = 2;
    }

    /**
     * 文件上传配置
     */
    public static class Upload {
        /** 文件上传根目录 */
        public static final String ROOT_PATH = "/uploads/";
        /** 图片允许的格式 */
        public static final String[] IMAGE_TYPES = {"jpg", "jpeg", "png", "gif", "webp"};
        /** 单个文件最大大小（5MB） */
        public static final Long MAX_FILE_SIZE = 5 * 1024 * 1024L;
        /** 图片最大宽度（像素） */
        public static final Integer MAX_IMAGE_WIDTH = 2000;
        /** 图片最大高度（像素） */
        public static final Integer MAX_IMAGE_HEIGHT = 2000;
    }
}

