-- ==========================================
-- 社区团购系统 - 数据库迁移脚本
-- 版本: v3.0
-- 日期: 2025-10-27
-- 说明: 
--   1. 引入社区机制（用于优先推荐）
--   2. 新增拼团v2.0逻辑（活动-团-成员三层架构）
--   3. 支持跨社区拼团
--   4. 删除固定绑定关系（group_member表）
--   5. 新增社区申请审核机制
-- ==========================================

USE community_group_buy;

-- ==========================================
-- 备份提示
-- ==========================================
-- ⚠️ 执行前请先备份数据库！
-- mysqldump -u root -p community_group_buy > backup_$(date +%Y%m%d_%H%M%S).sql

-- ==========================================
-- Step 1: 创建社区表 (community)
-- ==========================================
CREATE TABLE IF NOT EXISTS `community` (
  `community_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '社区ID',
  `community_name` VARCHAR(100) NOT NULL COMMENT '社区名称',
  `province` VARCHAR(20) NOT NULL COMMENT '省份',
  `city` VARCHAR(20) NOT NULL COMMENT '城市',
  `district` VARCHAR(20) NOT NULL COMMENT '区/县',
  `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `longitude` DECIMAL(10,6) NOT NULL COMMENT '社区中心经度',
  `latitude` DECIMAL(10,6) NOT NULL COMMENT '社区中心纬度',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-禁用；1-启用）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  
  PRIMARY KEY (`community_id`) USING BTREE,
  INDEX `idx_location` (`province`, `city`, `district`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区表';

-- ==========================================
-- Step 2: 创建社区申请表 (community_application) ⭐新增
-- ==========================================
CREATE TABLE IF NOT EXISTS `community_application` (
  `application_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `applicant_id` BIGINT NOT NULL COMMENT '申请人ID（团长申请人）',
  `community_name` VARCHAR(100) NOT NULL COMMENT '社区名称',
  `province` VARCHAR(20) NOT NULL COMMENT '省份',
  `city` VARCHAR(20) NOT NULL COMMENT '城市',
  `district` VARCHAR(20) NOT NULL COMMENT '区/县',
  `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `longitude` DECIMAL(10,6) DEFAULT NULL COMMENT '经度（管理员审核时填写）',
  `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度（管理员审核时填写）',
  `description` TEXT DEFAULT NULL COMMENT '申请说明',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态（0-待审核；1-已通过；2-已拒绝）',
  `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '拒绝原因',
  `approved_community_id` BIGINT DEFAULT NULL COMMENT '审核通过后生成的社区ID',
  `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  
  PRIMARY KEY (`application_id`) USING BTREE,
  INDEX `idx_applicant_id` (`applicant_id`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE,
  INDEX `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区申请表';

-- ==========================================
-- Step 3: 调整用户表 (sys_user) - 增加社区字段
-- ==========================================
ALTER TABLE `sys_user` 
ADD COLUMN IF NOT EXISTS `community_id` BIGINT DEFAULT NULL COMMENT '所属社区ID（自动匹配，非强制）' AFTER `role`;

ALTER TABLE `sys_user` 
ADD INDEX IF NOT EXISTS `idx_community_id` (`community_id`) USING BTREE;

-- ==========================================
-- Step 4: 调整团长团点表 (group_leader_store) - 增加社区字段
-- ==========================================
ALTER TABLE `group_leader_store` 
ADD COLUMN IF NOT EXISTS `community_id` BIGINT DEFAULT NULL COMMENT '归属社区ID' AFTER `leader_id`;

ALTER TABLE `group_leader_store` 
ADD COLUMN IF NOT EXISTS `community_application_id` BIGINT DEFAULT NULL COMMENT '关联的社区申请ID' AFTER `community_id`;

ALTER TABLE `group_leader_store` 
ADD INDEX IF NOT EXISTS `idx_community_id` (`community_id`) USING BTREE;

ALTER TABLE `group_leader_store` 
ADD INDEX IF NOT EXISTS `idx_community_application_id` (`community_application_id`) USING BTREE;

-- ==========================================
-- Step 5: 调整团实例表 (group_buy_team) - 增加社区字段
-- ==========================================
ALTER TABLE `group_buy_team` 
ADD COLUMN IF NOT EXISTS `community_id` BIGINT DEFAULT NULL COMMENT '归属社区ID（团长的社区）' AFTER `leader_id`;

ALTER TABLE `group_buy_team` 
ADD INDEX IF NOT EXISTS `idx_community_id` (`community_id`) USING BTREE;

-- ==========================================
-- Step 6: 添加外键约束
-- ==========================================

-- 用户表外键
ALTER TABLE `sys_user` 
ADD CONSTRAINT `fk_user_community` 
FOREIGN KEY (`community_id`) REFERENCES `community` (`community_id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- 团长表外键
ALTER TABLE `group_leader_store` 
ADD CONSTRAINT `fk_store_community` 
FOREIGN KEY (`community_id`) REFERENCES `community` (`community_id`) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `group_leader_store` 
ADD CONSTRAINT `fk_store_community_app` 
FOREIGN KEY (`community_application_id`) REFERENCES `community_application` (`application_id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- 团实例表外键
ALTER TABLE `group_buy_team` 
ADD CONSTRAINT `fk_team_community` 
FOREIGN KEY (`community_id`) REFERENCES `community` (`community_id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- 社区申请表外键
ALTER TABLE `community_application` 
ADD CONSTRAINT `fk_app_applicant` 
FOREIGN KEY (`applicant_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE `community_application` 
ADD CONSTRAINT `fk_app_community` 
FOREIGN KEY (`approved_community_id`) REFERENCES `community` (`community_id`) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `community_application` 
ADD CONSTRAINT `fk_app_auditor` 
FOREIGN KEY (`auditor_id`) REFERENCES `sys_user` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- ==========================================
-- Step 7: 删除/备份旧表
-- ==========================================

-- 7.1 备份并删除 group_member 表（固定绑定机制废弃）
DROP TABLE IF EXISTS `group_member_backup_20251027`;
RENAME TABLE `group_member` TO `group_member_backup_20251027`;

-- 7.2 备份 group_buy_join 表（将被 group_buy_member 替代）
DROP TABLE IF EXISTS `group_buy_join_backup_20251027`;
RENAME TABLE `group_buy_join` TO `group_buy_join_backup_20251027`;

-- 如果确认不需要旧数据，可执行以下命令删除备份表：
-- DROP TABLE IF EXISTS `group_member_backup_20251027`;
-- DROP TABLE IF EXISTS `group_buy_join_backup_20251027`;

-- ==========================================
-- Step 8: 插入示例社区数据
-- ==========================================
INSERT INTO `community` (`community_name`, `province`, `city`, `district`, `address`, `longitude`, `latitude`, `status`)
VALUES 
('阳光社区', '北京市', '北京市', '朝阳区', '朝阳北路88号', 116.497128, 39.926527, 1),
('幸福家园', '北京市', '北京市', '朝阳区', '建国路168号', 116.457128, 39.916527, 1),
('绿地花园', '北京市', '北京市', '海淀区', '中关村大街1号', 116.307128, 39.976527, 1),
('温馨小区', '北京市', '北京市', '海淀区', '学院路35号', 116.347128, 39.986527, 1),
('和谐家园', '北京市', '北京市', '丰台区', '丰台路100号', 116.287128, 39.856527, 1)
ON DUPLICATE KEY UPDATE `community_name`=`community_name`;

-- ==========================================
-- Step 9: 验证迁移结果
-- ==========================================

SELECT '==========================================';
SELECT '✅ 数据库迁移完成！';
SELECT '==========================================';
SELECT '';
SELECT '📊 变更摘要：';
SELECT '';
SELECT '新增表（2张）：';
SELECT '  ✅ community（社区表）';
SELECT '  ✅ community_application（社区申请表）';
SELECT '';
SELECT '调整表（3张）：';
SELECT '  ✅ sys_user（增加 community_id）';
SELECT '  ✅ group_leader_store（增加 community_id, community_application_id）';
SELECT '  ✅ group_buy_team（增加 community_id）';
SELECT '';
SELECT '备份表（2张）：';
SELECT '  ⚠️ group_member_backup_20251027';
SELECT '  ⚠️ group_buy_join_backup_20251027';
SELECT '';
SELECT '删除表：';
SELECT '  ❌ group_member（固定绑定机制废弃）';
SELECT '  ❌ group_buy_join（被 group_buy_member 替代）';
SELECT '';
SELECT '📋 后续操作：';
SELECT '  1. 管理员为现有团长分配社区';
SELECT '  2. 用户完善地址时自动匹配社区（≤5km）';
SELECT '  3. 团长发起团时自动关联社区';
SELECT '  4. 用户参团优先显示本社区的团';
SELECT '';
SELECT '==========================================';

