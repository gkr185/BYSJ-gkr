/*
 社区团购系统 - 团长服务数据库
 
 微服务: LeaderService (端口: 8068)
 数据库名称: leader_service_db
 数据库版本: MySQL 8.0.36
 字符集: utf8mb4
 排序规则: utf8mb4_0900_ai_ci
 
 包含表数: 4张
 - community (社区表)
 - community_application (社区申请表)
 - group_leader_store (团长团点表)
 - commission_record (佣金记录表)
 
 创建日期: 2025-10-29
 说明: 从 community_group_buy v3.0 拆分而来
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 创建数据库
-- ============================================
DROP DATABASE IF EXISTS `leader_service_db`;
CREATE DATABASE `leader_service_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `leader_service_db`;

-- ============================================
-- 1. 社区表
-- ============================================
DROP TABLE IF EXISTS `community`;
CREATE TABLE `community` (
  `community_id` bigint NOT NULL AUTO_INCREMENT COMMENT '社区ID',
  `community_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '社区名称',
  `province` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
  `city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
  `district` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区/县',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  `longitude` decimal(10, 6) NOT NULL COMMENT '社区中心经度',
  `latitude` decimal(10, 6) NOT NULL COMMENT '社区中心纬度',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-禁用；1-启用）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`community_id`) USING BTREE,
  INDEX `idx_location`(`province` ASC, `city` ASC, `district` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社区表（v3.0新增）' ROW_FORMAT = Dynamic;

-- 导入原有数据
INSERT INTO `community` VALUES (1, '中关村社区', '北京市', '北京市', '海淀区', '中关村大街1号', 116.240252, 40.046427, 1, '2025-10-28 00:00:00');
INSERT INTO `community` VALUES (2, '望京社区', '北京市', '北京市', '朝阳区', '望京街道', 116.470302, 39.991957, 1, '2025-10-28 00:00:00');

-- ============================================
-- 2. 社区申请表
-- ============================================
DROP TABLE IF EXISTS `community_application`;
CREATE TABLE `community_application` (
  `application_id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID（团长申请人，跨库关联）',
  `community_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '社区名称',
  `province` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
  `city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
  `district` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区/县',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度（管理员审核时填写）',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度（管理员审核时填写）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '申请说明',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态（0-待审核；1-已通过；2-已拒绝）',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拒绝原因',
  `approved_community_id` bigint NULL DEFAULT NULL COMMENT '审核通过后生成的社区ID',
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '审核人ID（跨库关联）',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`application_id`) USING BTREE,
  INDEX `idx_applicant_id`(`applicant_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_approved_community_id`(`approved_community_id` ASC) USING BTREE,
  INDEX `idx_auditor_id`(`auditor_id` ASC) USING BTREE,
  CONSTRAINT `fk_app_community` FOREIGN KEY (`approved_community_id`) REFERENCES `community` (`community_id`) ON DELETE SET NULL ON UPDATE CASCADE
  -- ❌ 删除跨库外键: CONSTRAINT `fk_app_applicant` (关联 user_service_db.sys_user)
  -- ❌ 删除跨库外键: CONSTRAINT `fk_app_auditor` (关联 user_service_db.sys_user)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社区申请表（v3.0新增）' ROW_FORMAT = Dynamic;

-- 暂无数据

-- ============================================
-- 3. 团长团点表
-- ============================================
DROP TABLE IF EXISTS `group_leader_store`;
CREATE TABLE `group_leader_store` (
  `store_id` bigint NOT NULL AUTO_INCREMENT COMMENT '团点ID',
  `leader_id` bigint NOT NULL COMMENT '关联团长用户ID（role=2，跨库关联）',
  `store_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '团点名称',
  `province` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所在省份',
  `city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所在城市',
  `district` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所在区/县',
  `detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  `longitude` decimal(10, 6) NOT NULL COMMENT '团点经度（路径计算起点）',
  `latitude` decimal(10, 6) NOT NULL COMMENT '团点纬度（路径计算起点）',
  `community_id` bigint NULL DEFAULT NULL COMMENT '归属社区ID（v3.0新增）',
  `max_delivery_range` int NOT NULL DEFAULT 3000 COMMENT '最大配送范围（米，分单逻辑依据）',
  `audit_status` tinyint NOT NULL DEFAULT 0 COMMENT '资质审核状态（0-待审核；1-通过；2-拒绝）',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `commission_rate` decimal(5, 2) NOT NULL DEFAULT 5.00 COMMENT '佣金比例（%）',
  PRIMARY KEY (`store_id`) USING BTREE,
  UNIQUE INDEX `uk_leader_id`(`leader_id` ASC) USING BTREE,
  INDEX `idx_community_id`(`community_id` ASC) USING BTREE,
  INDEX `idx_audit_status`(`audit_status` ASC) USING BTREE,
  CONSTRAINT `fk_store_community` FOREIGN KEY (`community_id`) REFERENCES `community` (`community_id`) ON DELETE SET NULL ON UPDATE CASCADE
  -- ❌ 删除跨库外键: CONSTRAINT `fk_store_leader` (关联 user_service_db.sys_user)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储团长信息及团点地理数据，支持分单与配送' ROW_FORMAT = DYNAMIC;

-- 暂无数据

-- ============================================
-- 4. 佣金记录表
-- ============================================
DROP TABLE IF EXISTS `commission_record`;
CREATE TABLE `commission_record` (
  `commission_id` bigint NOT NULL AUTO_INCREMENT COMMENT '佣金ID',
  `leader_id` bigint NOT NULL COMMENT '关联团长ID（跨库关联）',
  `order_id` bigint NOT NULL COMMENT '关联订单ID（跨库关联）',
  `amount` decimal(10, 2) NOT NULL COMMENT '佣金金额（订单实付×佣金比例）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-未结算；1-已结算）',
  `settle_time` datetime NULL DEFAULT NULL COMMENT '结算时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间（订单支付后）',
  PRIMARY KEY (`commission_id`) USING BTREE,
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
  -- ❌ 删除跨库外键: CONSTRAINT `fk_commission_leader` (关联 user_service_db.sys_user)
  -- ❌ 删除跨库外键: CONSTRAINT `fk_commission_order` (关联 order_service_db.order_main)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '记录团长佣金计算与结算明细' ROW_FORMAT = DYNAMIC;

-- 暂无数据

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 初始化完成提示
-- ============================================
SELECT '==================================================' AS '';
SELECT '团长服务数据库 (leader_service_db) 创建完成！' AS '状态';
SELECT '==================================================' AS '';
SELECT '总表数: 4张' AS '统计';
SELECT '已导入数据: 社区2个（中关村社区、望京社区）' AS '数据';
SELECT '物理外键: 2个（单库内）' AS '外键';
SELECT '跨库关联: leader_id, applicant_id, auditor_id, order_id（应用层校验）' AS '提醒';
SELECT '==================================================' AS '';

