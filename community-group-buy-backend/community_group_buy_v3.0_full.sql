/*
 社区团购系统 - 完整数据库脚本 v3.0
 
 项目名称: 基于Spring Boot的社区团购系统
 数据库名称: community_group_buy
 数据库版本: MySQL 8.0.36
 字符集: utf8mb4
 排序规则: utf8mb4_0900_ai_ci
 
 v3.0 重要变更:
 - 新增社区机制（community表、community_application表）
 - sys_user、group_leader_store、group_buy_team表增加community_id字段
 - 新增group_buy_member表（参团记录）
 - 废弃group_member表和group_buy_join表
 
 创建日期: 2025-10-28
 包含原有数据: 是
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 社区域（v3.0新增）
-- ============================================

-- ----------------------------
-- Table structure for community
-- ----------------------------
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

-- ----------------------------
-- Records of community
-- ----------------------------
INSERT INTO `community` VALUES (1, '中关村社区', '北京市', '北京市', '海淀区', '中关村大街1号', 116.240252, 40.046427, 1, '2025-10-28 00:00:00');
INSERT INTO `community` VALUES (2, '望京社区', '北京市', '北京市', '朝阳区', '望京街道', 116.470302, 39.991957, 1, '2025-10-28 00:00:00');

-- ----------------------------
-- Table structure for community_application
-- ----------------------------
DROP TABLE IF EXISTS `community_application`;
CREATE TABLE `community_application` (
  `application_id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID（团长申请人）',
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
  `auditor_id` bigint NULL DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`application_id`) USING BTREE,
  INDEX `idx_applicant_id`(`applicant_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `fk_app_community`(`approved_community_id` ASC) USING BTREE,
  INDEX `fk_app_auditor`(`auditor_id` ASC) USING BTREE,
  CONSTRAINT `fk_app_applicant` FOREIGN KEY (`applicant_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_app_auditor` FOREIGN KEY (`auditor_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_app_community` FOREIGN KEY (`approved_community_id`) REFERENCES `community` (`community_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '社区申请表（v3.0新增）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of community_application
-- ----------------------------

-- ============================================
-- 2. 用户域
-- ============================================

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
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
  `community_id` bigint NULL DEFAULT NULL COMMENT '归属社区ID（v3.0新增）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-禁用；1-正常）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_wx_openid`(`wx_openid` ASC) USING BTREE,
  INDEX `idx_community_id`(`community_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_community` FOREIGN KEY (`community_id`) REFERENCES `community` (`community_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储所有用户（普通用户/团长/管理员）基础信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user（保留原有数据，community_id自动匹配为1-中关村社区）
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, '1', '96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e', 3, NULL, NULL, NULL, NULL, 1, 1, '2025-10-12 18:38:48', '2025-10-12 18:52:14');
INSERT INTO `sys_user` VALUES (2, '测试用户1', '96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e', 1, '测试用户1', '12312312311', NULL, NULL, 1, 1, '2025-10-12 19:20:44', '2025-10-12 20:53:49');
INSERT INTO `sys_user` VALUES (3, '22', '96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e', 1, '1231', '12312312312', NULL, '', 1, 1, '2025-10-12 20:12:52', '2025-10-12 21:07:01');
INSERT INTO `sys_user` VALUES (4, '33', '96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e', 1, NULL, '12312312333', NULL, NULL, 1, 1, '2025-10-12 21:22:06', NULL);
INSERT INTO `sys_user` VALUES (5, '44', '96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e', 1, '', '12312312344', NULL, NULL, 1, 1, '2025-10-14 23:40:21', '2025-10-14 23:48:59');

-- ----------------------------
-- Table structure for user_address
-- ----------------------------
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

-- ----------------------------
-- Records of user_address
-- ----------------------------
INSERT INTO `user_address` VALUES (1, 3, '1231', '12312312312', '北京市', '北京市', '海淀区', '15-2', 116.240252, 40.046427, 1);

-- ----------------------------
-- Table structure for user_account
-- ----------------------------
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

-- ----------------------------
-- Records of user_account
-- ----------------------------
INSERT INTO `user_account` VALUES (1, 2, 0.00, 0.00, '2025-10-12 19:20:44');
INSERT INTO `user_account` VALUES (2, 3, 0.00, 0.00, '2025-10-12 20:12:52');
INSERT INTO `user_account` VALUES (3, 4, 0.00, 0.00, '2025-10-12 21:22:06');
INSERT INTO `user_account` VALUES (4, 1, 0.00, 0.00, '2025-10-13 20:30:40');
INSERT INTO `user_account` VALUES (5, 5, 0.00, 0.00, '2025-10-14 23:40:21');

-- ----------------------------
-- Table structure for user_feedback
-- ----------------------------
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

-- ----------------------------
-- Records of user_feedback
-- ----------------------------
INSERT INTO `user_feedback` VALUES (1, 4, 1, '123', NULL, 2, '333333', '2025-10-13 20:44:09', '2025-10-12 21:22:43', '2025-10-13 20:44:09');
INSERT INTO `user_feedback` VALUES (2, 3, 2, '333', NULL, 1, '999', '2025-10-13 20:43:57', '2025-10-13 19:58:14', '2025-10-13 20:43:57');

-- ============================================
-- 3. 商品域
-- ============================================

-- ----------------------------
-- Table structure for product_category
-- ----------------------------
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `category_id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类ID（0-顶级分类）',
  `category_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序权重（值越小越靠前）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-禁用；1-启用）',
  PRIMARY KEY (`category_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理商品分类体系，支持多级分类' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product_category
-- ----------------------------

-- ----------------------------
-- Table structure for product
-- ----------------------------
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
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `product_category` (`category_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储商品信息，支持拼团及普通购买' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product
-- ----------------------------

-- ============================================
-- 4. 拼团域（v3.0优化）
-- ============================================

-- ----------------------------
-- Table structure for group_buy
-- ----------------------------
DROP TABLE IF EXISTS `group_buy`;
CREATE TABLE `group_buy` (
  `activity_id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `product_id` bigint NOT NULL COMMENT '关联商品ID',
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
  INDEX `idx_time`(`start_time` ASC, `end_time` ASC) USING BTREE,
  CONSTRAINT `fk_group_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '拼团活动表（活动模板）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of group_buy
-- ----------------------------

-- ----------------------------
-- Table structure for group_buy_team
-- ----------------------------
DROP TABLE IF EXISTS `group_buy_team`;
CREATE TABLE `group_buy_team` (
  `team_id` bigint NOT NULL AUTO_INCREMENT COMMENT '团ID',
  `team_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '团号（用于分享链接，如：T20251027001）',
  `activity_id` bigint NOT NULL COMMENT '关联活动ID',
  `launcher_id` bigint NOT NULL COMMENT '发起人用户ID（v3.0仅团长可发起）',
  `leader_id` bigint NOT NULL COMMENT '归属团长ID（配送团点，v3.0中=launcher_id）',
  `community_id` bigint NULL DEFAULT NULL COMMENT '归属社区ID（v3.0新增）',
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
  CONSTRAINT `fk_team_activity` FOREIGN KEY (`activity_id`) REFERENCES `group_buy` (`activity_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_team_launcher` FOREIGN KEY (`launcher_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_team_leader` FOREIGN KEY (`leader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_team_community` FOREIGN KEY (`community_id`) REFERENCES `community` (`community_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '团实例表（具体的团，v3.0优化）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of group_buy_team
-- ----------------------------

-- ----------------------------
-- Table structure for group_buy_member
-- ----------------------------
DROP TABLE IF EXISTS `group_buy_member`;
CREATE TABLE `group_buy_member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT COMMENT '参团记录ID',
  `team_id` bigint NOT NULL COMMENT '团ID',
  `user_id` bigint NOT NULL COMMENT '参团用户ID',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID',
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
  CONSTRAINT `fk_member_team` FOREIGN KEY (`team_id`) REFERENCES `group_buy_team` (`team_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_member_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_member_order` FOREIGN KEY (`order_id`) REFERENCES `order_main` (`order_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '参团记录表（v2.0新增，v3.0核心）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of group_buy_member
-- ----------------------------

-- ============================================
-- 5. 团长域（v3.0优化）
-- ============================================

-- ----------------------------
-- Table structure for group_leader_store
-- ----------------------------
DROP TABLE IF EXISTS `group_leader_store`;
CREATE TABLE `group_leader_store` (
  `store_id` bigint NOT NULL AUTO_INCREMENT COMMENT '团点ID',
  `leader_id` bigint NOT NULL COMMENT '关联团长用户ID（role=2）',
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
  CONSTRAINT `fk_store_leader` FOREIGN KEY (`leader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_store_community` FOREIGN KEY (`community_id`) REFERENCES `community` (`community_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储团长信息及团点地理数据，支持分单与配送' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of group_leader_store
-- ----------------------------

-- ============================================
-- 6. 订单域
-- ============================================

-- ----------------------------
-- Table structure for shopping_cart
-- ----------------------------
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart` (
  `cart_id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `product_id` bigint NOT NULL COMMENT '关联商品ID',
  `activity_id` bigint NULL DEFAULT NULL COMMENT '关联拼团活动ID（非拼团商品为null）',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `add_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`cart_id`) USING BTREE,
  UNIQUE INDEX `uk_user_product_activity`(`user_id` ASC, `product_id` ASC, `activity_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  CONSTRAINT `fk_cart_activity` FOREIGN KEY (`activity_id`) REFERENCES `group_buy` (`activity_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储用户待购商品，支持拼团/普通商品混存' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of shopping_cart
-- ----------------------------

-- ----------------------------
-- Table structure for order_main
-- ----------------------------
DROP TABLE IF EXISTS `order_main`;
CREATE TABLE `order_main` (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '下单用户ID',
  `leader_id` bigint NOT NULL COMMENT '取货团长ID（关联团点）',
  `order_sn` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号（规则：日期+随机数）',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '商品总金额',
  `discount_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  `pay_amount` decimal(10, 2) NOT NULL COMMENT '实付金额（total - discount）',
  `order_status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态（0-待支付；1-待发货；2-配送中；3-已送达；4-已取消；5-退款中；6-已退款）',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态（0-未支付；1-已支付）',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `receive_address_id` bigint NOT NULL COMMENT '收货地址ID',
  `dispatch_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分单组标识（同批次配送订单）',
  `delivery_id` bigint NULL DEFAULT NULL COMMENT '关联配送单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE INDEX `uk_order_sn`(`order_sn` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE,
  INDEX `idx_delivery_id`(`delivery_id` ASC) USING BTREE,
  INDEX `idx_dispatch_group`(`dispatch_group` ASC) USING BTREE,
  INDEX `fk_order_address`(`receive_address_id` ASC) USING BTREE,
  CONSTRAINT `fk_order_address` FOREIGN KEY (`receive_address_id`) REFERENCES `user_address` (`address_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_order_delivery` FOREIGN KEY (`delivery_id`) REFERENCES `delivery` (`delivery_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_order_leader` FOREIGN KEY (`leader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储订单核心信息，关联用户、团长与配送' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_main
-- ----------------------------

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `product_id` bigint NOT NULL COMMENT '关联商品ID',
  `activity_id` bigint NULL DEFAULT NULL COMMENT '关联拼团活动ID（非拼团为null）',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称（下单时快照）',
  `product_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片（下单时快照）',
  `price` decimal(10, 2) NOT NULL COMMENT '购买单价（拼团价/原价）',
  `quantity` int NOT NULL COMMENT '购买数量',
  `total_price` decimal(10, 2) NOT NULL COMMENT '小计金额（price×quantity）',
  PRIMARY KEY (`item_id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  CONSTRAINT `fk_item_activity` FOREIGN KEY (`activity_id`) REFERENCES `group_buy` (`activity_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `order_main` (`order_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_item_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储订单商品明细（快照设计）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_item
-- ----------------------------

-- ============================================
-- 7. 配送域
-- ============================================

-- ----------------------------
-- Table structure for delivery
-- ----------------------------
DROP TABLE IF EXISTS `delivery`;
CREATE TABLE `delivery` (
  `delivery_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配送单ID',
  `leader_id` bigint NOT NULL COMMENT '负责团长ID',
  `dispatch_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联分单组',
  `start_time` datetime NULL DEFAULT NULL COMMENT '配送开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '配送完成时间',
  `optimal_route` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '最优路径（经纬度序列）',
  `distance` decimal(10, 2) NOT NULL COMMENT '总配送距离（米）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '配送状态（0-待分配；1-配送中；2-已完成）',
  PRIMARY KEY (`delivery_id`) USING BTREE,
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE,
  INDEX `idx_dispatch_group`(`dispatch_group` ASC) USING BTREE,
  CONSTRAINT `fk_delivery_leader` FOREIGN KEY (`leader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储配送路径与分单结果（支撑Dijkstra算法）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of delivery
-- ----------------------------

-- ============================================
-- 8. 支付域
-- ============================================

-- ----------------------------
-- Table structure for payment_record
-- ----------------------------
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record` (
  `pay_id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付记录ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `order_id` bigint NULL DEFAULT NULL COMMENT '关联订单ID（充值时为null）',
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
  CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `order_main` (`order_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_payment_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '记录支付/充值/退款明细，保障交易安全' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of payment_record
-- ----------------------------

-- ----------------------------
-- Table structure for commission_record
-- ----------------------------
DROP TABLE IF EXISTS `commission_record`;
CREATE TABLE `commission_record` (
  `commission_id` bigint NOT NULL AUTO_INCREMENT COMMENT '佣金ID',
  `leader_id` bigint NOT NULL COMMENT '关联团长ID',
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '佣金金额（订单实付×佣金比例）',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0-未结算；1-已结算）',
  `settle_time` datetime NULL DEFAULT NULL COMMENT '结算时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间（订单支付后）',
  PRIMARY KEY (`commission_id`) USING BTREE,
  INDEX `idx_leader_id`(`leader_id` ASC) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  CONSTRAINT `fk_commission_leader` FOREIGN KEY (`leader_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_commission_order` FOREIGN KEY (`order_id`) REFERENCES `order_main` (`order_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '记录团长佣金计算与结算明细' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of commission_record
-- ----------------------------

-- ============================================
-- 9. 系统域
-- ============================================

-- ----------------------------
-- Table structure for sys_operation_log
-- ----------------------------
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

-- ----------------------------
-- Records of sys_operation_log
-- ----------------------------
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
SELECT '社区团购系统 v3.0 数据库创建完成！' AS '状态';
SELECT '==================================================' AS '';
SELECT '总表数: 19张（v3.0新增2张社区表）' AS '统计';
SELECT '已导入原有数据: 用户5个，地址1个，账户5个，反馈2条，日志7条' AS '数据';
SELECT '已创建示例社区: 中关村社区、望京社区' AS '社区';
SELECT '==================================================' AS '';
SELECT '✅ 所有用户已自动关联到中关村社区（community_id=1）' AS '提醒1';
SELECT '⚠️  请根据实际情况调整用户的社区归属' AS '提醒2';
SELECT '📋 建议接下来：创建商品、设置拼团活动、配置团长' AS '下一步';
SELECT '==================================================' AS '';

