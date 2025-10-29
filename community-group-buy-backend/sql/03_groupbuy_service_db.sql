/*
 社区团购系统 - 拼团服务数据库
 
 微服务: GroupBuyService (端口: 8063)
 数据库名称: groupbuy_service_db
 数据库版本: MySQL 8.0.36
 字符集: utf8mb4
 排序规则: utf8mb4_0900_ai_ci
 
 包含表数: 3张
 - group_buy (拼团活动表)
 - group_buy_team (团实例表)
 - group_buy_member (参团记录表)
 
 创建日期: 2025-10-29
 说明: 从 community_group_buy v3.0 拆分而来
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 创建数据库
-- ============================================
DROP DATABASE IF EXISTS `groupbuy_service_db`;
CREATE DATABASE `groupbuy_service_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `groupbuy_service_db`;

-- ============================================
-- 1. 拼团活动表
-- ============================================
DROP TABLE IF EXISTS `group_buy`;
CREATE TABLE `group_buy` (
  `activity_id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `product_id` bigint NOT NULL COMMENT '关联商品ID（跨库关联，仅应用层校验）',
  `group_price` decimal(10, 2) NOT NULL COMMENT '拼团价',
  `required_num` int NOT NULL DEFAULT 2 COMMENT '成团人数',
  `max_num` int NULL DEFAULT NULL COMMENT '最大人数限制（可空，无限制）',
  `start_time` datetime NOT NULL COMMENT '活动开始时间',
  `end_time` datetime NOT NULL COMMENT '活动结束时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '活动状态（0未开始/1进行中/2已结束/3异常）',
  `qrcode_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动二维码URL',
  `link` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '活动专属链接',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`activity_id`) USING BTREE,
  UNIQUE INDEX `uk_link`(`link` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_time`(`start_time` ASC, `end_time` ASC) USING BTREE
  -- ❌ 删除跨库外键: CONSTRAINT `fk_group_product` (关联 product_service_db.product)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拼团活动表（活动模板）' ROW_FORMAT = DYNAMIC;

-- 暂无数据

-- ============================================
-- 2. 团实例表
-- ============================================
DROP TABLE IF EXISTS `group_buy_team`;
CREATE TABLE `group_buy_team` (
  `team_id` bigint NOT NULL AUTO_INCREMENT COMMENT '团ID',
  `team_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '团号（用于分享链接，如：T20251027001）',
  `activity_id` bigint NOT NULL COMMENT '关联活动ID',
  `launcher_id` bigint NOT NULL COMMENT '发起人用户ID（v3.0仅团长可发起，跨库关联）',
  `leader_id` bigint NOT NULL COMMENT '归属团长ID（配送团点，v3.0中=launcher_id，跨库关联）',
  `community_id` bigint NULL DEFAULT NULL COMMENT '归属社区ID（v3.0新增，跨库关联）',
  `required_num` int NOT NULL COMMENT '成团人数（从活动复制）',
  `current_num` int NOT NULL DEFAULT 0 COMMENT '当前人数',
  `team_status` tinyint NOT NULL DEFAULT 0 COMMENT '团状态（0拼团中/1已成团/2已失败）',
  `success_time` datetime NULL DEFAULT NULL COMMENT '成团时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间（24小时后）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`team_id`) USING BTREE,
  UNIQUE INDEX `uk_team_no`(`team_no` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  INDEX `idx_launcher_id`(`launcher_id` ASC) USING BTREE,
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE,
  INDEX `idx_community_id`(`community_id` ASC) USING BTREE,
  INDEX `idx_team_status`(`team_status` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_team_activity` FOREIGN KEY (`activity_id`) REFERENCES `group_buy` (`activity_id`) ON DELETE CASCADE ON UPDATE CASCADE
  -- ❌ 删除跨库外键: CONSTRAINT `fk_team_launcher` (关联 user_service_db.sys_user)
  -- ❌ 删除跨库外键: CONSTRAINT `fk_team_leader` (关联 user_service_db.sys_user)
  -- ❌ 删除跨库外键: CONSTRAINT `fk_team_community` (关联 leader_service_db.community)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '团实例表（具体的团，v3.0优化）' ROW_FORMAT = DYNAMIC;

-- 暂无数据

-- ============================================
-- 3. 参团记录表
-- ============================================
DROP TABLE IF EXISTS `group_buy_member`;
CREATE TABLE `group_buy_member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT COMMENT '参团记录ID',
  `team_id` bigint NOT NULL COMMENT '团ID',
  `user_id` bigint NOT NULL COMMENT '参团用户ID（跨库关联）',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID（跨库关联）',
  `is_launcher` tinyint NOT NULL DEFAULT 0 COMMENT '是否发起人（0否/1是）',
  `pay_amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '参团时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0待支付/1已支付/2已成团/3已取消）',
  PRIMARY KEY (`member_id`) USING BTREE,
  UNIQUE INDEX `uk_team_user`(`team_id` ASC, `user_id` ASC) USING BTREE COMMENT '一个用户不能重复参加同一个团',
  INDEX `idx_team_id`(`team_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `fk_member_team` FOREIGN KEY (`team_id`) REFERENCES `group_buy_team` (`team_id`) ON DELETE CASCADE ON UPDATE CASCADE
  -- ❌ 删除跨库外键: CONSTRAINT `fk_member_user` (关联 user_service_db.sys_user)
  -- ❌ 删除跨库外键: CONSTRAINT `fk_member_order` (关联 order_service_db.order_main)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '参团记录表（v2.0新增，v3.0核心）' ROW_FORMAT = DYNAMIC;

-- 暂无数据

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 初始化完成提示
-- ============================================
SELECT '==================================================' AS '';
SELECT '拼团服务数据库 (groupbuy_service_db) 创建完成！' AS '状态';
SELECT '==================================================' AS '';
SELECT '总表数: 3张' AS '统计';
SELECT '已导入数据: 暂无（待团长发起拼团）' AS '数据';
SELECT '物理外键: 2个（单库内）' AS '外键';
SELECT '跨库关联: product_id, launcher_id, leader_id, community_id, user_id, order_id（应用层校验）' AS '提醒';
SELECT '==================================================' AS '';

