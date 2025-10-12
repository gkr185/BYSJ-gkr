/*
 Navicat Premium Data Transfer

 Source Server         : 毕业设计
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : localhost:3306
 Source Schema         : community_group_buy

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 12/10/2025 15:02:53
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for commission_record
-- ----------------------------
DROP TABLE IF EXISTS `commission_record`;
CREATE TABLE `commission_record`  (
  `commission_id` bigint NOT NULL AUTO_INCREMENT COMMENT '佣金ID',
  `leader_id` bigint NOT NULL COMMENT '关联团长ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '佣金金额（订单实付×佣金比例）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-未结算；1-已结算）',
  `settle_time` datetime NULL DEFAULT NULL COMMENT '结算时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间（订单支付后）',
  PRIMARY KEY (`commission_id`) USING BTREE,
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  CONSTRAINT `fk_commission_leader` FOREIGN KEY (`leader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_commission_order` FOREIGN KEY (`order_id`) REFERENCES `order_main` (`order_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '记录团长佣金计算与结算明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for delivery
-- ----------------------------
DROP TABLE IF EXISTS `delivery`;
CREATE TABLE `delivery`  (
  `delivery_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配送单ID',
  `leader_id` bigint NOT NULL COMMENT '负责团长ID',
  `dispatch_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联分单组',
  `start_time` datetime NULL DEFAULT NULL COMMENT '配送开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '配送完成时间',
  `optimal_route` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '最优路径（经纬度序列，如：[lat1,lng1;lat2,lng2...]）',
  `distance` decimal(10, 2) NOT NULL COMMENT '总配送距离（米）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '配送状态（0-待分配；1-配送中；2-已完成）',
  PRIMARY KEY (`delivery_id`) USING BTREE,
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE,
  INDEX `idx_dispatch_group`(`dispatch_group` ASC) USING BTREE,
  CONSTRAINT `fk_delivery_leader` FOREIGN KEY (`leader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储配送路径与分单结果（支撑Dijkstra算法）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for group_buy
-- ----------------------------
DROP TABLE IF EXISTS `group_buy`;
CREATE TABLE `group_buy`  (
  `activity_id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `product_id` bigint NOT NULL COMMENT '关联商品ID',
  `leader_id` bigint NULL DEFAULT NULL COMMENT '发起团长ID（可空，系统默认团长）',
  `group_price` decimal(10, 2) NOT NULL COMMENT '拼团价（活动专属）',
  `required_num` int NOT NULL DEFAULT 2 COMMENT '成团人数',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-未开始；1-进行中；2-已结束；3-异常）',
  `qrcode_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拼团二维码URL',
  `link` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拼团专属链接',
  PRIMARY KEY (`activity_id`) USING BTREE,
  UNIQUE INDEX `uk_link`(`link` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE,
  CONSTRAINT `fk_group_leader` FOREIGN KEY (`leader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_group_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储拼团活动核心参数，覆盖全生命周期' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for group_buy_join
-- ----------------------------
DROP TABLE IF EXISTS `group_buy_join`;
CREATE TABLE `group_buy_join`  (
  `join_id` bigint NOT NULL AUTO_INCREMENT COMMENT '参与ID',
  `activity_id` bigint NOT NULL COMMENT '关联活动ID',
  `user_id` bigint NOT NULL COMMENT '参与用户ID',
  `is_launcher` tinyint NOT NULL DEFAULT 0 COMMENT '是否发起者（0-否；1-是）',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-已退出；1-参与中；2-成团）',
  PRIMARY KEY (`join_id`) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_join_activity` FOREIGN KEY (`activity_id`) REFERENCES `group_buy` (`activity_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_join_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '记录用户参与拼团的行为与状态' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for group_leader_store
-- ----------------------------
DROP TABLE IF EXISTS `group_leader_store`;
CREATE TABLE `group_leader_store`  (
  `store_id` bigint NOT NULL AUTO_INCREMENT COMMENT '团点ID',
  `leader_id` bigint NOT NULL COMMENT '关联团长用户ID（role=2）',
  `store_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '团点名称',
  `province` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所在省份',
  `city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所在城市',
  `district` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所在区/县',
  `detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  `longitude` decimal(10, 6) NOT NULL COMMENT '团点经度（路径计算起点）',
  `latitude` decimal(10, 6) NOT NULL COMMENT '团点纬度（路径计算起点）',
  `max_delivery_range` int NOT NULL DEFAULT 3000 COMMENT '最大配送范围（米，分单逻辑依据）',
  `audit_status` tinyint NOT NULL DEFAULT 0 COMMENT '资质审核状态（0-待审核；1-通过；2-拒绝）',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `commission_rate` decimal(5, 2) NOT NULL DEFAULT 5.00 COMMENT '佣金比例（%）',
  PRIMARY KEY (`store_id`) USING BTREE,
  UNIQUE INDEX `uk_leader_id`(`leader_id` ASC) USING BTREE,
  CONSTRAINT `fk_store_leader` FOREIGN KEY (`leader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储团长信息及团点地理数据，支持分单与配送' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for group_member
-- ----------------------------
DROP TABLE IF EXISTS `group_member`;
CREATE TABLE `group_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `leader_id` bigint NOT NULL COMMENT '团长用户ID（role=2）',
  `member_id` bigint NOT NULL COMMENT '团员用户ID（role=1）',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-已退出；1-正常）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_leader_member`(`leader_id` ASC, `member_id` ASC) USING BTREE,
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE,
  INDEX `idx_member_id`(`member_id` ASC) USING BTREE,
  CONSTRAINT `fk_member_leader` FOREIGN KEY (`leader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_member_user` FOREIGN KEY (`member_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理团长与团员的绑定关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `product_id` bigint NOT NULL COMMENT '关联商品ID',
  `activity_id` bigint NULL DEFAULT NULL COMMENT '关联拼团活动ID（非拼团为null）',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称（下单时快照）',
  `product_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片（下单时快照）',
  `price` decimal(10, 2) NOT NULL COMMENT '购买单价（拼团价/原价）',
  `quantity` int NOT NULL COMMENT '购买数量',
  `total_price` decimal(10, 2) NOT NULL COMMENT '小计金额（price×quantity）',
  PRIMARY KEY (`item_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  CONSTRAINT `fk_item_activity` FOREIGN KEY (`activity_id`) REFERENCES `group_buy` (`activity_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `order_main` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储订单商品明细（快照设计）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_main
-- ----------------------------
DROP TABLE IF EXISTS `order_main`;
CREATE TABLE `order_main`  (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '下单用户ID',
  `leader_id` bigint NOT NULL COMMENT '取货团长ID（关联团点）',
  `order_sn` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号（规则：日期+随机数）',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '商品总金额',
  `discount_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `pay_amount` decimal(10, 2) NOT NULL COMMENT '实付金额（total - discount）',
  `order_status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态（0-待支付；1-待发货；2-配送中；3-已送达；4-已取消；5-退款中；6-已退款）',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态（0-未支付；1-已支付）',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `receive_address_id` bigint NOT NULL COMMENT '收货地址ID',
  `dispatch_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分单组标识（同批次配送订单）',
  `delivery_id` bigint NULL DEFAULT NULL COMMENT '关联配送单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE INDEX `uk_order_sn`(`order_sn` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE,
  INDEX `idx_delivery_id`(`delivery_id` ASC) USING BTREE,
  INDEX `idx_dispatch_group`(`dispatch_group` ASC) USING BTREE,
  INDEX `fk_order_address`(`receive_address_id` ASC) USING BTREE,
  CONSTRAINT `fk_order_address` FOREIGN KEY (`receive_address_id`) REFERENCES `user_address` (`address_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_order_delivery` FOREIGN KEY (`delivery_id`) REFERENCES `delivery` (`delivery_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_order_leader` FOREIGN KEY (`leader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储订单核心信息，关联用户、团长与配送' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for payment_record
-- ----------------------------
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record`  (
  `pay_id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID（充值时为null）',
  `pay_type` tinyint NOT NULL COMMENT '支付类型（1-微信；2-支付宝；3-余额）',
  `amount` decimal(10, 2) NOT NULL COMMENT '金额（正数-支付/充值；负数-退款）',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-失败；1-成功）',
  `transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '第三方支付流水号（通用）',
  `wx_transaction_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信支付专用流水号',
  `pay_callback_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '支付回调信息（AES加密存储）',
  `encrypt_sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据加密签名（防篡改，SHA256+盐值）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`pay_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_transaction_id`(`transaction_id` ASC) USING BTREE,
  CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `order_main` (`order_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_payment_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '记录支付/充值/退款明细，保障交易安全' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `product_id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `category_id` bigint NULL DEFAULT NULL COMMENT '关联分类ID',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `cover_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '封面图URL',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品详情（富文本）',
  `price` decimal(10, 2) NOT NULL COMMENT '原价',
  `group_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '拼团参考价（可被活动覆盖）',
  `stock` int NOT NULL DEFAULT 0 COMMENT '库存数量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-下架；1-上架）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`product_id`) USING BTREE,
  INDEX `idx_category_id`(`category_id` ASC) USING BTREE,
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`category_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储商品信息，支持拼团及普通购买' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for product_category
-- ----------------------------
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category`  (
  `category_id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类ID（0-顶级分类）',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序权重（值越小越靠前）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-禁用；1-启用）',
  PRIMARY KEY (`category_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理商品分类体系，支持多级分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for shopping_cart
-- ----------------------------
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart`  (
  `cart_id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `product_id` bigint NOT NULL COMMENT '关联商品ID',
  `activity_id` bigint NULL DEFAULT NULL COMMENT '关联拼团活动ID（非拼团商品为null）',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `add_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`cart_id`) USING BTREE,
  UNIQUE INDEX `uk_user_product_activity`(`user_id` ASC, `product_id` ASC, `activity_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  CONSTRAINT `fk_cart_activity` FOREIGN KEY (`activity_id`) REFERENCES `group_buy` (`activity_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储用户待购商品，支持拼团/普通商品混存' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`  (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作人ID（管理员/团长）',
  `operation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作内容（如“创建拼团活动”“审核团长资质”）',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作模块（如“拼团活动”“订单管理”）',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作IP地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_module`(`module` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_log_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '记录系统操作审计日志，保障安全' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密密码（MD5/SHA256）',
  `role` tinyint NOT NULL COMMENT '角色（1-普通用户；2-团长；3-管理员）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名（用于资质审核）',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号（加密存储）',
  `wx_openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信开放平台ID（关联微信支付/登录）',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-禁用；1-正常）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_wx_openid`(`wx_openid` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储所有用户（普通用户/团长/管理员）基础信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_account
-- ----------------------------
DROP TABLE IF EXISTS `user_account`;
CREATE TABLE `user_account`  (
  `account_id` bigint NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `balance` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '可用余额',
  `freeze_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额（未结算佣金/退款中）',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`account_id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_account_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理用户余额及资金流转' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_address
-- ----------------------------
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address`  (
  `address_id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `receiver` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收件人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收件人电话（加密存储）',
  `province` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
  `city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
  `district` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区/县',
  `detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  `longitude` decimal(10, 6) NOT NULL COMMENT '地址经度（Dijkstra算法输入）',
  `latitude` decimal(10, 6) NOT NULL COMMENT '地址纬度（Dijkstra算法输入）',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认（0-否；1-是）',
  PRIMARY KEY (`address_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储用户收货地址，支持地理路径计算' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
