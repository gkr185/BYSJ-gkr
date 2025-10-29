/*
 社区团购系统 - 配送服务数据库
 
 微服务: DeliveryService (端口: 8067)
 数据库名称: delivery_service_db
 数据库版本: MySQL 8.0.36
 字符集: utf8mb4
 排序规则: utf8mb4_0900_ai_ci
 
 包含表数: 1张
 - delivery (配送单表)
 
 创建日期: 2025-10-29
 说明: 从 community_group_buy v3.0 拆分而来
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 创建数据库
-- ============================================
DROP DATABASE IF EXISTS `delivery_service_db`;
CREATE DATABASE `delivery_service_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `delivery_service_db`;

-- ============================================
-- 1. 配送单表（支撑Dijkstra算法）
-- ============================================
DROP TABLE IF EXISTS `delivery`;
CREATE TABLE `delivery` (
  `delivery_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配送单ID',
  `leader_id` bigint NOT NULL COMMENT '负责团长ID（跨库关联）',
  `dispatch_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联分单组',
  `start_time` datetime NULL DEFAULT NULL COMMENT '配送开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '配送完成时间',
  `optimal_route` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '最优路径（经纬度序列，Dijkstra算法计算结果）',
  `distance` decimal(10, 2) NOT NULL COMMENT '总配送距离（米）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '配送状态（0-待分配；1-配送中；2-已完成）',
  PRIMARY KEY (`delivery_id`) USING BTREE,
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE,
  INDEX `idx_dispatch_group`(`dispatch_group` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
  -- ❌ 删除跨库外键: CONSTRAINT `fk_delivery_leader` (关联 user_service_db.sys_user)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储配送路径与分单结果（支撑Dijkstra算法）' ROW_FORMAT = DYNAMIC;

-- 暂无数据

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 初始化完成提示
-- ============================================
SELECT '==================================================' AS '';
SELECT '配送服务数据库 (delivery_service_db) 创建完成！' AS '状态';
SELECT '==================================================' AS '';
SELECT '总表数: 1张' AS '统计';
SELECT '已导入数据: 暂无（待团长生成配送单）' AS '数据';
SELECT '物理外键: 0个' AS '外键';
SELECT '跨库关联: leader_id（应用层校验）' AS '提醒';
SELECT '算法支撑: optimal_route字段存储Dijkstra算法计算的最优路径' AS '说明';
SELECT '==================================================' AS '';

