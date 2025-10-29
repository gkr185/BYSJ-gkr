/*
 社区团购系统 - 支付服务数据库
 
 微服务: PaymentService (端口: 8066)
 数据库名称: payment_service_db
 数据库版本: MySQL 8.0.36
 字符集: utf8mb4
 排序规则: utf8mb4_0900_ai_ci
 
 包含表数: 1张
 - payment_record (支付记录表)
 
 创建日期: 2025-10-29
 说明: 从 community_group_buy v3.0 拆分而来
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 创建数据库
-- ============================================
DROP DATABASE IF EXISTS `payment_service_db`;
CREATE DATABASE `payment_service_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `payment_service_db`;

-- ============================================
-- 1. 支付记录表
-- ============================================
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record` (
  `pay_id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID（跨库关联）',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID（充值时为null，跨库关联）',
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
  INDEX `idx_pay_status`(`pay_status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
  -- ❌ 删除跨库外键: CONSTRAINT `fk_payment_user` (关联 user_service_db.sys_user)
  -- ❌ 删除跨库外键: CONSTRAINT `fk_payment_order` (关联 order_service_db.order_main)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '记录支付/充值/退款明细，保障交易安全' ROW_FORMAT = DYNAMIC;

-- 暂无数据

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 初始化完成提示
-- ============================================
SELECT '==================================================' AS '';
SELECT '支付服务数据库 (payment_service_db) 创建完成！' AS '状态';
SELECT '==================================================' AS '';
SELECT '总表数: 1张' AS '统计';
SELECT '已导入数据: 暂无（待用户支付）' AS '数据';
SELECT '物理外键: 0个' AS '外键';
SELECT '跨库关联: user_id, order_id（应用层校验）' AS '提醒';
SELECT '安全设计: 支付回调信息AES加密，数据签名SHA256防篡改' AS '说明';
SELECT '==================================================' AS '';

