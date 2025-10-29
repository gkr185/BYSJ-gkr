/*
 社区团购系统 - 用户服务数据库
 
 微服务: UserService (端口: 8061)
 数据库名称: user_service_db
 数据库版本: MySQL 8.0.36
 字符集: utf8mb4
 排序规则: utf8mb4_0900_ai_ci
 
 包含表数: 5张
 - sys_user (用户表)
 - user_address (用户地址表)
 - user_account (用户账户表)
 - user_feedback (用户反馈表)
 - sys_operation_log (系统操作日志表)
 
 创建日期: 2025-10-29
 说明: 从 community_group_buy v3.0 拆分而来
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 创建数据库
-- ============================================
DROP DATABASE IF EXISTS `user_service_db`;
CREATE DATABASE `user_service_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `user_service_db`;

-- ============================================
-- 1. 用户表
-- ============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密密码（SHA256）',
  `role` tinyint NOT NULL COMMENT '角色（1-普通用户；2-团长；3-管理员）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号（加密存储）',
  `wx_openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信OpenID',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `community_id` bigint NULL DEFAULT NULL COMMENT '归属社区ID（跨库关联，仅应用层校验）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-禁用；1-正常）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_wx_openid`(`wx_openid` ASC) USING BTREE,
  INDEX `idx_community_id`(`community_id` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
  -- ❌ 删除跨库外键: CONSTRAINT `fk_user_community` (关联 leader_service_db.community)
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储所有用户（普通用户/团长/管理员）基础信息' ROW_FORMAT = DYNAMIC;

-- 导入原有数据
INSERT INTO `sys_user` VALUES (1, '1', '96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e', 3, NULL, NULL, NULL, NULL, 1, 1, '2025-10-12 18:38:48', '2025-10-12 18:52:14');
INSERT INTO `sys_user` VALUES (2, '测试用户1', '96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e', 1, '测试用户1', '12312312311', NULL, NULL, 1, 1, '2025-10-12 19:20:44', '2025-10-12 20:53:49');
INSERT INTO `sys_user` VALUES (3, '22', '96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e', 1, '1231', '12312312312', NULL, '', 1, 1, '2025-10-12 20:12:52', '2025-10-12 21:07:01');
INSERT INTO `sys_user` VALUES (4, '33', '96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e', 1, NULL, '12312312333', NULL, NULL, 1, 1, '2025-10-12 21:22:06', NULL);
INSERT INTO `sys_user` VALUES (5, '44', '96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e', 1, '', '12312312344', NULL, NULL, 1, 1, '2025-10-14 23:40:21', '2025-10-14 23:48:59');

-- ============================================
-- 2. 用户地址表
-- ============================================
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `address_id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `receiver` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收件人',
  `phone` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收件人电话（加密存储）',
  `province` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省份',
  `city` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '城市',
  `district` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区/县',
  `detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  `longitude` decimal(10, 6) NOT NULL COMMENT '地址经度（Dijkstra算法输入）',
  `latitude` decimal(10, 6) NOT NULL COMMENT '地址纬度（Dijkstra算法输入）',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认（0-否；1-是）',
  PRIMARY KEY (`address_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储用户收货地址，支持地理路径计算' ROW_FORMAT = DYNAMIC;

-- 导入原有数据
INSERT INTO `user_address` VALUES (1, 3, '1231', '12312312312', '北京市', '北京市', '海淀区', '15-2', 116.240252, 40.046427, 1);

-- ============================================
-- 3. 用户账户表
-- ============================================
DROP TABLE IF EXISTS `user_account`;
CREATE TABLE `user_account` (
  `account_id` bigint NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `balance` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '可用余额',
  `freeze_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '冻结金额（未结算佣金/退款中）',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`account_id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_account_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理用户余额及资金流转' ROW_FORMAT = DYNAMIC;

-- 导入原有数据
INSERT INTO `user_account` VALUES (1, 2, 0.00, 0.00, '2025-10-12 19:20:44');
INSERT INTO `user_account` VALUES (2, 3, 0.00, 0.00, '2025-10-12 20:12:52');
INSERT INTO `user_account` VALUES (3, 4, 0.00, 0.00, '2025-10-12 21:22:06');
INSERT INTO `user_account` VALUES (4, 1, 0.00, 0.00, '2025-10-13 20:30:40');
INSERT INTO `user_account` VALUES (5, 5, 0.00, 0.00, '2025-10-14 23:40:21');

-- ============================================
-- 4. 用户反馈表
-- ============================================
DROP TABLE IF EXISTS `user_feedback`;
CREATE TABLE `user_feedback` (
  `feedback_id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '反馈类型（1-功能问题；2-商品问题；3-配送问题；4-支付问题；5-其他）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图片URL（多张用逗号分隔）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态（0-待处理；1-处理中；2-已解决；3-已关闭）',
  `reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '处理意见（管理员回复）',
  `reply_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '反馈提交时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`feedback_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储用户反馈信息，支持管理员处理与回复' ROW_FORMAT = DYNAMIC;

-- 导入原有数据
INSERT INTO `user_feedback` VALUES (1, 4, 1, '123', NULL, 2, '333333', '2025-10-13 20:44:09', '2025-10-12 21:22:43', '2025-10-13 20:44:09');
INSERT INTO `user_feedback` VALUES (2, 3, 2, '333', NULL, 1, '999', '2025-10-13 20:43:57', '2025-10-13 19:58:14', '2025-10-13 20:43:57');

-- ============================================
-- 5. 系统操作日志表
-- ============================================
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作人ID（管理员/团长）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人用户名',
  `operation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作内容',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作模块',
  `method` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '方法名',
  `params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数（JSON）',
  `result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作结果（SUCCESS/FAIL）',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息',
  `duration` int NULL DEFAULT NULL COMMENT '执行时长（毫秒）',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作IP地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_module`(`module` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_result`(`result` ASC) USING BTREE,
  CONSTRAINT `fk_log_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '记录系统操作审计日志，保障安全' ROW_FORMAT = DYNAMIC;

-- 导入原有数据
INSERT INTO `sys_operation_log` VALUES (1, 1, '1', '修改用户状态', '用户管理', 'public com.bcu.edu.common.result.Result com.bcu.edu.controller.UserController.changeUserStatus(java.lang.Long,java.lang.Integer)', '[5,0]', 'SUCCESS', NULL, 26, '0:0:0:0:0:0:0:1', '2025-10-14 23:48:27');
INSERT INTO `sys_operation_log` VALUES (2, 1, '1', '修改用户状态', '用户管理', 'public com.bcu.edu.common.result.Result com.bcu.edu.controller.UserController.changeUserStatus(java.lang.Long,java.lang.Integer)', '[5,1]', 'SUCCESS', NULL, 5, '0:0:0:0:0:0:0:1', '2025-10-14 23:48:59');
INSERT INTO `sys_operation_log` VALUES (3, NULL, 'unknown', '用户登录', '认证管理', 'public com.bcu.edu.common.result.Result com.bcu.edu.controller.UserController.login(com.bcu.edu.dto.request.UserLoginRequest)', NULL, 'SUCCESS', NULL, 409, '0:0:0:0:0:0:0:1', '2025-10-19 18:57:12');
INSERT INTO `sys_operation_log` VALUES (4, NULL, 'unknown', '用户登录', '认证管理', 'public com.bcu.edu.common.result.Result com.bcu.edu.controller.UserController.login(com.bcu.edu.dto.request.UserLoginRequest)', NULL, 'SUCCESS', NULL, 3, '0:0:0:0:0:0:0:1', '2025-10-19 18:57:34');
INSERT INTO `sys_operation_log` VALUES (5, NULL, 'unknown', '用户登录', '认证管理', 'public com.bcu.edu.common.result.Result com.bcu.edu.controller.UserController.login(com.bcu.edu.dto.request.UserLoginRequest)', NULL, 'SUCCESS', NULL, 439, '0:0:0:0:0:0:0:1', '2025-10-27 20:14:57');
INSERT INTO `sys_operation_log` VALUES (6, 3, '22', '用户登录', '认证管理', 'public com.bcu.edu.common.result.Result com.bcu.edu.controller.UserController.login(com.bcu.edu.dto.request.UserLoginRequest)', NULL, 'SUCCESS', NULL, 6, '0:0:0:0:0:0:0:1', '2025-10-27 20:15:14');
INSERT INTO `sys_operation_log` VALUES (7, NULL, 'unknown', '用户登录', '认证管理', 'public com.bcu.edu.common.result.Result com.bcu.edu.controller.UserController.login(com.bcu.edu.dto.request.UserLoginRequest)', NULL, 'SUCCESS', NULL, 241, '0:0:0:0:0:0:0:1', '2025-10-27 20:22:56');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 初始化完成提示
-- ============================================
SELECT '==================================================' AS '';
SELECT '用户服务数据库 (user_service_db) 创建完成！' AS '状态';
SELECT '==================================================' AS '';
SELECT '总表数: 5张' AS '统计';
SELECT '已导入数据: 用户5个，地址1个，账户5个，反馈2条，日志7条' AS '数据';
SELECT '物理外键: 4个（单库内）' AS '外键';
SELECT '跨库关联: community_id（应用层校验）' AS '提醒';
SELECT '==================================================' AS '';

