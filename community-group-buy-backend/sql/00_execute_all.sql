/*
 社区团购系统 - 微服务数据库批量创建脚本
 
 执行顺序: 按01-07顺序执行
 执行方式: 在MySQL客户端使用 source 命令
 
 创建日期: 2025-10-29
 说明: 从 community_group_buy v3.0 拆分为7个独立数据库
*/

-- ============================================
-- 执行方式1: MySQL命令行
-- ============================================
-- mysql -u root -p
-- source E:/E/BYSJ/community-group-buy-backend/sql/01_user_service_db.sql
-- source E:/E/BYSJ/community-group-buy-backend/sql/02_product_service_db.sql
-- source E:/E/BYSJ/community-group-buy-backend/sql/03_groupbuy_service_db.sql
-- source E:/E/BYSJ/community-group-buy-backend/sql/04_order_service_db.sql
-- source E:/E/BYSJ/community-group-buy-backend/sql/05_payment_service_db.sql
-- source E:/E/BYSJ/community-group-buy-backend/sql/06_delivery_service_db.sql
-- source E:/E/BYSJ/community-group-buy-backend/sql/07_leader_service_db.sql

-- ============================================
-- 执行方式2: Navicat/DBeaver等图形化工具
-- ============================================
-- 1. 打开每个SQL文件
-- 2. 按01-07顺序逐个执行
-- 3. 检查执行结果提示信息

-- ============================================
-- 执行方式3: Windows批处理脚本
-- ============================================
-- 创建 execute_all.bat 文件，内容如下：
-- @echo off
-- echo 正在创建微服务数据库...
-- mysql -u root -p123456 < 01_user_service_db.sql
-- mysql -u root -p123456 < 02_product_service_db.sql
-- mysql -u root -p123456 < 03_groupbuy_service_db.sql
-- mysql -u root -p123456 < 04_order_service_db.sql
-- mysql -u root -p123456 < 05_payment_service_db.sql
-- mysql -u root -p123456 < 06_delivery_service_db.sql
-- mysql -u root -p123456 < 07_leader_service_db.sql
-- echo 所有数据库创建完成！
-- pause

-- ============================================
-- 验证数据库创建结果
-- ============================================
-- SHOW DATABASES;
-- 应显示:
-- - user_service_db
-- - product_service_db
-- - groupbuy_service_db
-- - order_service_db
-- - payment_service_db
-- - delivery_service_db
-- - leader_service_db

-- ============================================
-- 验证数据导入情况
-- ============================================
-- USE user_service_db;
-- SELECT COUNT(*) AS user_count FROM sys_user;  -- 应为 5
-- SELECT COUNT(*) AS address_count FROM user_address;  -- 应为 1
-- SELECT COUNT(*) AS account_count FROM user_account;  -- 应为 5
-- SELECT COUNT(*) AS feedback_count FROM user_feedback;  -- 应为 2
-- SELECT COUNT(*) AS log_count FROM sys_operation_log;  -- 应为 7

-- USE leader_service_db;
-- SELECT COUNT(*) AS community_count FROM community;  -- 应为 2

-- ============================================
-- 查看物理外键约束
-- ============================================
-- USE user_service_db;
-- SELECT 
--   TABLE_NAME,
--   CONSTRAINT_NAME,
--   REFERENCED_TABLE_NAME
-- FROM information_schema.KEY_COLUMN_USAGE
-- WHERE TABLE_SCHEMA = 'user_service_db'
--   AND REFERENCED_TABLE_NAME IS NOT NULL;
-- -- 应显示 4 个外键（单库内）

SELECT '==================================================' AS '';
SELECT '微服务数据库创建指南' AS '说明';
SELECT '==================================================' AS '';
SELECT '请按照以下步骤执行:' AS '';
SELECT '1. 打开MySQL客户端（命令行/Navicat/DBeaver）' AS '';
SELECT '2. 依次执行 01-07 SQL脚本' AS '';
SELECT '3. 检查每个脚本的执行结果提示' AS '';
SELECT '4. 验证数据库数量和表数量' AS '';
SELECT '==================================================' AS '';

