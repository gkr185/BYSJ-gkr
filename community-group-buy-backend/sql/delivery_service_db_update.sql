-- ============================================
-- DeliveryService 数据库更新脚本
-- ============================================
-- 功能: 为现有delivery表新增字段和索引
-- 作者: 耿康瑞
-- 日期: 2025-11-15
-- 版本: v1.0
-- ============================================

USE delivery_service_db;

-- ============================================
-- 1. 新增字段到 delivery 表
-- ============================================

-- 发货方式: 1-团长团点 2-用户地址
ALTER TABLE delivery ADD COLUMN delivery_mode TINYINT NOT NULL DEFAULT 1 
COMMENT '发货方式: 1-团长团点 2-用户地址';

-- 起点仓库ID
ALTER TABLE delivery ADD COLUMN warehouse_id BIGINT 
COMMENT '起点仓库ID';

-- 终点仓库ID（可选）
ALTER TABLE delivery ADD COLUMN end_warehouse_id BIGINT 
COMMENT '终点仓库ID（可选）';

-- 途经点数量
ALTER TABLE delivery ADD COLUMN waypoint_count INT NOT NULL DEFAULT 0 
COMMENT '途经点数量';

-- 关联订单ID列表（JSON数组）
ALTER TABLE delivery ADD COLUMN order_ids TEXT NOT NULL 
COMMENT '关联订单ID列表（JSON数组）';

-- 途经点详细信息（JSON数组）
ALTER TABLE delivery ADD COLUMN waypoints_data TEXT 
COMMENT '途经点详细信息（JSON数组）';

-- 创建人ID（管理员）
ALTER TABLE delivery ADD COLUMN created_by BIGINT 
COMMENT '创建人ID（管理员）';

-- 创建时间
ALTER TABLE delivery ADD COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP 
COMMENT '创建时间';

-- 更新时间
ALTER TABLE delivery ADD COLUMN update_time DATETIME ON UPDATE CURRENT_TIMESTAMP 
COMMENT '更新时间';

-- ============================================
-- 2. 新增索引到 delivery 表
-- ============================================

-- 发货方式索引
CREATE INDEX idx_delivery_mode ON delivery(delivery_mode);

-- 仓库ID索引
CREATE INDEX idx_warehouse_id ON delivery(warehouse_id);

-- 创建人索引
CREATE INDEX idx_created_by ON delivery(created_by);

-- 创建时间索引
CREATE INDEX idx_create_time ON delivery(create_time);

-- ============================================
-- 3. 验证更新
-- ============================================

-- 查看delivery表结构
DESC delivery;

-- 查看delivery表索引
SHOW INDEX FROM delivery;

-- ============================================
-- 更新完成！
-- ============================================
-- 新增字段数: 9个
-- 新增索引数: 4个
-- delivery表总字段数: 20个
-- ============================================

