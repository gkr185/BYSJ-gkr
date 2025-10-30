/*
 Navicat Premium Data Transfer

 Source Server         : javaEE22软本3
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : localhost:3306
 Source Schema         : delivery_service_db

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 30/10/2025 18:18:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for delivery
-- ----------------------------
DROP TABLE IF EXISTS `delivery`;
CREATE TABLE `delivery`  (
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
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储配送路径与分单结果（支撑Dijkstra算法）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of delivery
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
