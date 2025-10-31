-- 修复重复团长记录问题
-- 错误：Duplicate entry '3' for key 'group_leader_store.uk_leader_id'
-- 日期：2025-10-31
-- 说明：清除测试数据，删除重复的团长记录

USE leader_service_db;

-- 1. 查看当前冲突的记录
SELECT 
    store_id,
    leader_id,
    leader_name,
    community_id,
    community_name,
    status,
    created_at
FROM group_leader_store
WHERE leader_id = 3
ORDER BY created_at;

-- 2. 查看社区申请记录
SELECT 
    application_id,
    applicant_id,
    applicant_name,
    community_name,
    status,
    created_community_id,
    created_at
FROM community_application
WHERE applicant_id = 3
ORDER BY created_at;

-- 3. 删除用户3的所有团长记录（保留最新的或根据需要保留）
-- 方案A：删除所有记录，让用户重新申请
DELETE FROM group_leader_store WHERE leader_id = 3;

-- 方案B：只保留最新的记录（如果有多条）
-- DELETE FROM group_leader_store 
-- WHERE leader_id = 3 
-- AND store_id NOT IN (
--     SELECT store_id FROM (
--         SELECT store_id 
--         FROM group_leader_store 
--         WHERE leader_id = 3 
--         ORDER BY created_at DESC 
--         LIMIT 1
--     ) AS t
-- );

-- 4. 同时清除社区申请记录（可选）
-- DELETE FROM community_application WHERE applicant_id = 3;

-- 5. 验证清理结果
SELECT 
    store_id,
    leader_id,
    leader_name,
    community_id,
    community_name,
    status
FROM group_leader_store
WHERE leader_id = 3;

-- 应该返回空结果或只有一条记录

-- 6. 更新UserService中的用户角色（如果需要）
-- 如果删除了所有团长记录，需要将用户角色改回普通用户
-- 这个操作需要在UserService的数据库中执行：
-- USE user_service_db;
-- UPDATE sys_user SET role = 1 WHERE user_id = 3;

