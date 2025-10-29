/*
 社区团购系统 - 商品服务数据库
 
 微服务: ProductService (端口: 8062)
 数据库名称: product_service_db
 数据库版本: MySQL 8.0.36
 字符集: utf8mb4
 排序规则: utf8mb4_0900_ai_ci
 
 包含表数: 2张
 - product_category (商品分类表)
 - product (商品表)
 
 创建日期: 2025-10-29
 说明: 从 community_group_buy v3.0 拆分而来
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 创建数据库
-- ============================================
DROP DATABASE IF EXISTS `product_service_db`;
CREATE DATABASE `product_service_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `product_service_db`;

-- ============================================
-- 1. 商品分类表
-- ============================================
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `category_id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类ID（0-顶级分类）',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序权重（值越小越靠前）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-禁用；1-启用）',
  PRIMARY KEY (`category_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理商品分类体系，支持多级分类' ROW_FORMAT = DYNAMIC;

-- 暂无数据

-- ============================================
-- 2. 商品表
-- ============================================
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
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
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`category_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储商品信息，支持拼团及普通购买' ROW_FORMAT = DYNAMIC;

-- 暂无数据

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 初始化完成提示
-- ============================================
SELECT '==================================================' AS '';
SELECT '商品服务数据库 (product_service_db) 创建完成！' AS '状态';
SELECT '==================================================' AS '';
SELECT '总表数: 2张' AS '统计';
SELECT '已导入数据: 暂无（待管理员添加商品）' AS '数据';
SELECT '物理外键: 1个（单库内）' AS '外键';
SELECT '==================================================' AS '';

