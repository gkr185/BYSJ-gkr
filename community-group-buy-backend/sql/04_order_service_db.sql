/*
 社区团购系统 - 订单服务数据库
 
 微服务: OrderService (端口: 8065)
 数据库名称: order_service_db
 数据库版本: MySQL 8.0.36
 字符集: utf8mb4
 排序规则: utf8mb4_0900_ai_ci
 
 包含表数: 3张
 - shopping_cart (购物车表)
 - order_main (订单主表)
 - order_item (订单项表)
 
 创建日期: 2025-10-29
 说明: 从 community_group_buy v3.0 拆分而来
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 创建数据库
-- ============================================
DROP DATABASE IF EXISTS `order_service_db`;
CREATE DATABASE `order_service_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `order_service_db`;

-- ============================================
-- 1. 购物车表
-- ============================================
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart` (
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
  -- ❌ 删除跨库外键: CONSTRAINT `fk_cart_user` (关联 user_service_db.sys_user)
  -- ❌ 删除跨库外键: CONSTRAINT `fk_cart_product` (关联 product_service_db.product)
  -- ❌ 删除跨库外键: CONSTRAINT `fk_cart_activity` (关联 groupbuy_service_db.group_buy)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储用户待购商品，支持拼团/普通商品混存' ROW_FORMAT = DYNAMIC;

-- 暂无数据

-- ============================================
-- 2. 订单主表
-- ============================================
DROP TABLE IF EXISTS `order_main`;
CREATE TABLE `order_main` (
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
  -- ❌ 删除跨库外键: CONSTRAINT `fk_order_user` (关联 user_service_db.sys_user)
  -- ❌ 删除跨库外键: CONSTRAINT `fk_order_leader` (关联 user_service_db.sys_user)
  -- ❌ 删除跨库外键: CONSTRAINT `fk_order_address` (关联 user_service_db.user_address)
  -- ❌ 删除跨库外键: CONSTRAINT `fk_order_delivery` (关联 delivery_service_db.delivery)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储订单核心信息，关联用户、团长与配送' ROW_FORMAT = DYNAMIC;

-- 暂无数据

-- ============================================
-- 3. 订单项表（快照设计）
-- ============================================
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
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
  -- ❌ 删除跨库外键: CONSTRAINT `fk_item_product` (关联 product_service_db.product)
  -- ❌ 删除跨库外键: CONSTRAINT `fk_item_activity` (关联 groupbuy_service_db.group_buy)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储订单商品明细（快照设计）' ROW_FORMAT = DYNAMIC;

-- 暂无数据

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 初始化完成提示
-- ============================================
SELECT '==================================================' AS '';
SELECT '订单服务数据库 (order_service_db) 创建完成！' AS '状态';
SELECT '==================================================' AS '';
SELECT '总表数: 3张' AS '统计';
SELECT '已导入数据: 暂无（待用户下单）' AS '数据';
SELECT '物理外键: 1个（单库内）' AS '外键';
SELECT '跨库关联: user_id, leader_id, receive_address_id, delivery_id, product_id, activity_id（应用层校验）' AS '提醒';
SELECT '快照设计: order_item表保存商品名称、图片，避免频繁跨库查询' AS '说明';
SELECT '==================================================' AS '';

