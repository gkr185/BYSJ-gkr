/*
 Navicat Premium Data Transfer

 Source Server         : javaEE22软本3
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : localhost:3306
 Source Schema         : order_service_db

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 30/10/2025 18:18:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `product_id` bigint NOT NULL COMMENT '关联商品ID（跨库关联）',
  `activity_id` bigint NULL DEFAULT NULL COMMENT '关联拼团活动ID（非拼团为null，跨库关联）',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称（下单时快照）',
  `product_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片（下单时快照）',
  `price` decimal(10, 2) NOT NULL COMMENT '购买单价（拼团价/原价）',
  `quantity` int NOT NULL COMMENT '购买数量',
  `total_price` decimal(10, 2) NOT NULL COMMENT '小计金额（price×quantity）',
  PRIMARY KEY (`item_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `order_main` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储订单商品明细（快照设计）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_item
-- ----------------------------

-- ----------------------------
-- Table structure for order_main
-- ----------------------------
DROP TABLE IF EXISTS `order_main`;
CREATE TABLE `order_main`  (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '下单用户ID（跨库关联）',
  `leader_id` bigint NOT NULL COMMENT '取货团长ID（关联团点，跨库关联）',
  `order_sn` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号（规则：日期+随机数）',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '商品总金额',
  `discount_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `pay_amount` decimal(10, 2) NOT NULL COMMENT '实付金额（total - discount）',
  `order_status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态（0-待支付；1-待发货；2-配送中；3-已送达；4-已取消；5-退款中；6-已退款）',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态（0-未支付；1-已支付）',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `receive_address_id` bigint NOT NULL COMMENT '收货地址ID（跨库关联）',
  `dispatch_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分单组标识（同批次配送订单）',
  `delivery_id` bigint NULL DEFAULT NULL COMMENT '关联配送单ID（跨库关联）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE INDEX `uk_order_sn`(`order_sn` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE,
  INDEX `idx_delivery_id`(`delivery_id` ASC) USING BTREE,
  INDEX `idx_dispatch_group`(`dispatch_group` ASC) USING BTREE,
  INDEX `idx_receive_address_id`(`receive_address_id` ASC) USING BTREE,
  INDEX `idx_order_status`(`order_status` ASC) USING BTREE,
  INDEX `idx_pay_status`(`pay_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储订单核心信息，关联用户、团长与配送' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_main
-- ----------------------------

-- ----------------------------
-- Table structure for shopping_cart
-- ----------------------------
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart`  (
  `cart_id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID（跨库关联）',
  `product_id` bigint NOT NULL COMMENT '关联商品ID（跨库关联）',
  `activity_id` bigint NULL DEFAULT NULL COMMENT '关联拼团活动ID（非拼团商品为null，跨库关联）',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `add_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`cart_id`) USING BTREE,
  UNIQUE INDEX `uk_user_product_activity`(`user_id` ASC, `product_id` ASC, `activity_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储用户待购商品，支持拼团/普通商品混存' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of shopping_cart
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
