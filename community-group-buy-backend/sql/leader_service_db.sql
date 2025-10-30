/*
 Navicat Premium Data Transfer

 Source Server         : javaEE22软本3
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : localhost:3306
 Source Schema         : leader_service_db

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 30/10/2025 19:21:12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for commission_record
-- ----------------------------
DROP TABLE IF EXISTS `commission_record`;
CREATE TABLE `commission_record`  (
  `commission_id` bigint NOT NULL AUTO_INCREMENT COMMENT '佣金ID',
  `leader_id` bigint NOT NULL COMMENT '关联团长ID（跨库关联）',
  `leader_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '团长姓名（冗余）',
  `order_id` bigint NOT NULL COMMENT '关联订单ID（跨库关联）',
  `order_amount` decimal(10, 2) NOT NULL COMMENT '订单金额',
  `commission_rate` decimal(5, 2) NOT NULL COMMENT '佣金比例',
  `commission_amount` decimal(10, 2) NOT NULL COMMENT '佣金金额',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态（0-待结算；1-已结算；2-结算失败）',
  `settled_at` datetime NULL DEFAULT NULL COMMENT '结算时间',
  `settlement_batch` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '结算批次号（格式：YYYYMMDD）',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间（订单完成后）',
  PRIMARY KEY (`commission_id`) USING BTREE,
  UNIQUE INDEX `uk_order_id`(`order_id` ASC) USING BTREE COMMENT '一个订单只生成一条佣金记录',
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE COMMENT '团长查询索引',
  INDEX `idx_status`(`status` ASC) USING BTREE COMMENT '状态筛选索引',
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE COMMENT '时间排序索引'
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '记录团长佣金计算与结算明细' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of commission_record
-- ----------------------------

-- ----------------------------
-- Table structure for community
-- ----------------------------
DROP TABLE IF EXISTS `community`;
CREATE TABLE `community`  (
  `community_id` bigint NOT NULL AUTO_INCREMENT COMMENT '社区ID',
  `community_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '社区名称',
  `province` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
  `city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
  `district` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区/县',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  `latitude` decimal(10, 6) NOT NULL COMMENT '社区中心纬度',
  `longitude` decimal(10, 6) NOT NULL COMMENT '社区中心经度',
  `service_radius` int NOT NULL DEFAULT 3000 COMMENT '服务半径（单位：米，默认3000）',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态（0-待审核；1-正常运营；2-已关闭）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社区简介',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`community_id`) USING BTREE,
  INDEX `idx_latitude_longitude`(`latitude` ASC, `longitude` ASC) USING BTREE COMMENT '地理位置查询索引',
  INDEX `idx_status`(`status` ASC) USING BTREE COMMENT '状态筛选索引',
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE COMMENT '时间排序索引'
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社区表（v3.0新增）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of community
-- ----------------------------
INSERT INTO `community` VALUES (1, '中关村社区', '北京市', '北京市', '海淀区', '中关村大街1号', 40.046427, 116.240252, 3000, 1, '中关村社区，科技创新中心', '2025-10-28 00:00:00', '2025-10-30 18:12:30');
INSERT INTO `community` VALUES (2, '望京社区', '北京市', '北京市', '朝阳区', '望京街道', 39.991957, 116.470302, 3000, 1, '望京社区，国际化社区', '2025-10-28 00:00:00', '2025-10-30 18:12:30');

-- ----------------------------
-- Table structure for community_application
-- ----------------------------
DROP TABLE IF EXISTS `community_application`;
CREATE TABLE `community_application`  (
  `application_id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID（团长申请人，跨库关联）',
  `applicant_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请人姓名（冗余）',
  `applicant_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请人手机号（冗余）',
  `community_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '社区名称',
  `province` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
  `city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
  `district` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区/县',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  `latitude` decimal(10, 6) NOT NULL COMMENT '纬度',
  `longitude` decimal(10, 6) NOT NULL COMMENT '经度',
  `service_radius` int NOT NULL DEFAULT 3000 COMMENT '服务半径（单位：米）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社区简介',
  `application_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申请理由',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态（0-待审核；1-审核通过；2-审核拒绝）',
  `reviewer_id` bigint NULL DEFAULT NULL COMMENT '审核人ID（跨库关联）',
  `review_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核意见',
  `reviewed_at` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `created_community_id` bigint NULL DEFAULT NULL COMMENT '创建的社区ID（审核通过后生成）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`application_id`) USING BTREE,
  INDEX `idx_applicant_id`(`applicant_id` ASC) USING BTREE COMMENT '申请人查询索引',
  INDEX `idx_status`(`status` ASC) USING BTREE COMMENT '状态筛选索引',
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE COMMENT '时间排序索引',
  INDEX `fk_app_community`(`created_community_id` ASC) USING BTREE,
  CONSTRAINT `fk_app_community` FOREIGN KEY (`created_community_id`) REFERENCES `community` (`community_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社区申请表（v3.0新增）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of community_application
-- ----------------------------

-- ----------------------------
-- Table structure for group_leader_store
-- ----------------------------
DROP TABLE IF EXISTS `group_leader_store`;
CREATE TABLE `group_leader_store`  (
  `store_id` bigint NOT NULL AUTO_INCREMENT COMMENT '团点ID',
  `leader_id` bigint NOT NULL COMMENT '关联团长用户ID（role=2，跨库关联）',
  `leader_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '团长姓名（冗余）',
  `leader_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '团长手机号（冗余）',
  `community_id` bigint NOT NULL COMMENT '归属社区ID',
  `community_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '社区名称（冗余）',
  `store_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '团点名称',
  `province` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在省份',
  `city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在城市',
  `district` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在区/县',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '团点地址',
  `detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '团点经度（路径计算起点）',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '团点纬度（路径计算起点）',
  `max_delivery_range` int NOT NULL DEFAULT 3000 COMMENT '最大配送范围（米，分单逻辑依据）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '团点简介',
  `commission_rate` decimal(5, 2) NOT NULL DEFAULT 10.00 COMMENT '佣金比例（%）',
  `total_commission` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '累计佣金',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态（0-待审核；1-正常运营；2-已停用）',
  `reviewer_id` bigint NULL DEFAULT NULL COMMENT '审核人ID（跨库关联）',
  `review_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核意见',
  `reviewed_at` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`store_id`) USING BTREE,
  UNIQUE INDEX `uk_leader_id`(`leader_id` ASC) USING BTREE COMMENT '一个团长只能有一个团点',
  INDEX `idx_community_id`(`community_id` ASC) USING BTREE COMMENT '社区查询索引',
  INDEX `idx_status`(`status` ASC) USING BTREE COMMENT '状态筛选索引',
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE COMMENT '时间排序索引',
  CONSTRAINT `fk_store_community` FOREIGN KEY (`community_id`) REFERENCES `community` (`community_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储团长信息及团点地理数据，支持分单与配送' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of group_leader_store
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
