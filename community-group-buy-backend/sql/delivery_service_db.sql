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

 Date: 15/11/2025 10:41:52
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
  `route_strategy` tinyint NULL DEFAULT 0 COMMENT '路径策略（0-最短时间；1-最短距离；2-避开拥堵）',
  `estimated_duration` int NULL DEFAULT NULL COMMENT '预估配送时间（分钟）',
  `actual_start_time` datetime NULL DEFAULT NULL COMMENT '实际开始配送时间',
  `route_display_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '前端地图展示数据',
  `algorithm_used` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'dijkstra' COMMENT '使用的算法（dijkstra/gaode）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delivery_mode` tinyint NOT NULL DEFAULT 1 COMMENT '发货方式: 1-团长团点 2-用户地址',
  `warehouse_id` bigint NULL DEFAULT NULL COMMENT '起点仓库ID',
  `end_warehouse_id` bigint NULL DEFAULT NULL COMMENT '终点仓库ID（可选）',
  `waypoint_count` int NOT NULL DEFAULT 0 COMMENT '途经点数量',
  `order_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联订单ID列表（JSON数组）',
  `waypoints_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '途经点详细信息（JSON数组）',
  `created_by` bigint NULL DEFAULT NULL COMMENT '创建人ID（管理员）',
  PRIMARY KEY (`delivery_id`) USING BTREE,
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE,
  INDEX `idx_dispatch_group`(`dispatch_group` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_algorithm_used`(`algorithm_used` ASC) USING BTREE,
  INDEX `idx_route_strategy`(`route_strategy` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '配送单表 - 支持Dijkstra算法和高德地图API双引擎路径规划' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of delivery
-- ----------------------------
INSERT INTO `delivery` VALUES (8, 2, 'DG20251114007', NULL, NULL, '116.397617,39.909095;116.313174,39.983697;116.240159,40.046474;116.813180,40.162197', 70737.05, 0, 0, 142, NULL, '{\"algorithm\":\"dijkstra\",\"routeType\":\"straightLine\",\"coordinates\":[\"116.397617,39.909095\",\"116.313174,39.983697\",\"116.240159,40.046474\",\"116.813180,40.162197\"],\"waypoints\":4}', 'dijkstra', '2025-11-14 22:09:56', '2025-11-14 22:14:53', 1, NULL, NULL, 0, '', NULL, NULL);

-- ----------------------------
-- Table structure for warehouse_config
-- ----------------------------
DROP TABLE IF EXISTS `warehouse_config`;
CREATE TABLE `warehouse_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '仓库ID',
  `warehouse_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库名称',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '仓库地址',
  `longitude` decimal(10, 6) NOT NULL COMMENT '经度',
  `latitude` decimal(10, 6) NOT NULL COMMENT '纬度',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认仓库（0-否；1-是）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-禁用；1-启用）',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_warehouse_name`(`warehouse_name` ASC) USING BTREE,
  INDEX `idx_is_default`(`is_default` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '仓库配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of warehouse_config
-- ----------------------------
INSERT INTO `warehouse_config` VALUES (1, '中心仓库', '北京市东城区东华门街道天安门', 116.397617, 39.909095, 1, 1, '管理员', '18519249185', '系统默认仓库，用于配送路径规划起点', '2025-11-13 22:11:09', '2025-11-14 18:50:12');

SET FOREIGN_KEY_CHECKS = 1;
