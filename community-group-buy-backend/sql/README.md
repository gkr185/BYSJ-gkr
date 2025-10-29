# 社区团购系统 - 微服务数据库SQL脚本

## 📁 文件列表

| 文件名 | 数据库名称 | 表数量 | 物理外键 | 包含数据 | 说明 |
|--------|-----------|--------|---------|---------|------|
| `01_user_service_db.sql` | user_service_db | 5张 | 4个 | ✅ 用户5个，地址1个，账户5个，反馈2条，日志7条 | 用户服务数据库 |
| `02_product_service_db.sql` | product_service_db | 2张 | 1个 | ❌ 暂无 | 商品服务数据库 |
| `03_groupbuy_service_db.sql` | groupbuy_service_db | 3张 | 2个 | ❌ 暂无 | 拼团服务数据库 |
| `04_order_service_db.sql` | order_service_db | 3张 | 1个 | ❌ 暂无 | 订单服务数据库 |
| `05_payment_service_db.sql` | payment_service_db | 1张 | 0个 | ❌ 暂无 | 支付服务数据库 |
| `06_delivery_service_db.sql` | delivery_service_db | 1张 | 0个 | ❌ 暂无 | 配送服务数据库 |
| `07_leader_service_db.sql` | leader_service_db | 4张 | 2个 | ✅ 社区2个 | 团长服务数据库 |
| `00_execute_all.sql` | - | - | - | - | 批量执行指南 |

**总计**：
- 数据库：7个
- 表数量：19张
- 物理外键：10个（单库内）
- 跨库关联：22个（应用层校验）
- 原有数据：已完整迁移

---

## 🚀 快速开始

### 方式1：MySQL命令行执行（推荐）

```bash
# 1. 登录MySQL
mysql -u root -p

# 2. 依次执行SQL脚本
source E:/E/BYSJ/community-group-buy-backend/sql/01_user_service_db.sql
source E:/E/BYSJ/community-group-buy-backend/sql/02_product_service_db.sql
source E:/E/BYSJ/community-group-buy-backend/sql/03_groupbuy_service_db.sql
source E:/E/BYSJ/community-group-buy-backend/sql/04_order_service_db.sql
source E:/E/BYSJ/community-group-buy-backend/sql/05_payment_service_db.sql
source E:/E/BYSJ/community-group-buy-backend/sql/06_delivery_service_db.sql
source E:/E/BYSJ/community-group-buy-backend/sql/07_leader_service_db.sql

# 3. 验证数据库创建
SHOW DATABASES;
```

### 方式2：Windows批处理脚本

创建 `execute_all.bat` 文件（与SQL文件同目录）：

```bat
@echo off
chcp 65001
echo ====================================
echo 社区团购系统 - 微服务数据库创建
echo ====================================
echo.
echo 正在创建数据库，请稍候...
echo.

cd /d %~dp0

mysql -u root -p123456 < 01_user_service_db.sql
if %errorlevel% neq 0 (
    echo ❌ 用户服务数据库创建失败！
    pause
    exit /b 1
)
echo ✅ 用户服务数据库创建成功

mysql -u root -p123456 < 02_product_service_db.sql
if %errorlevel% neq 0 (
    echo ❌ 商品服务数据库创建失败！
    pause
    exit /b 1
)
echo ✅ 商品服务数据库创建成功

mysql -u root -p123456 < 03_groupbuy_service_db.sql
if %errorlevel% neq 0 (
    echo ❌ 拼团服务数据库创建失败！
    pause
    exit /b 1
)
echo ✅ 拼团服务数据库创建成功

mysql -u root -p123456 < 04_order_service_db.sql
if %errorlevel% neq 0 (
    echo ❌ 订单服务数据库创建失败！
    pause
    exit /b 1
)
echo ✅ 订单服务数据库创建成功

mysql -u root -p123456 < 05_payment_service_db.sql
if %errorlevel% neq 0 (
    echo ❌ 支付服务数据库创建失败！
    pause
    exit /b 1
)
echo ✅ 支付服务数据库创建成功

mysql -u root -p123456 < 06_delivery_service_db.sql
if %errorlevel% neq 0 (
    echo ❌ 配送服务数据库创建失败！
    pause
    exit /b 1
)
echo ✅ 配送服务数据库创建成功

mysql -u root -p123456 < 07_leader_service_db.sql
if %errorlevel% neq 0 (
    echo ❌ 团长服务数据库创建失败！
    pause
    exit /b 1
)
echo ✅ 团长服务数据库创建成功

echo.
echo ====================================
echo ✅ 所有数据库创建完成！
echo ====================================
echo.
echo 已创建7个数据库，共19张表
echo.
pause
```

**注意**：修改批处理脚本中的 `root` 和 `123456` 为您的MySQL用户名和密码。

### 方式3：Navicat/DBeaver图形化工具

1. 打开Navicat或DBeaver
2. 连接到MySQL服务器
3. 依次打开并执行 `01_user_service_db.sql` 至 `07_leader_service_db.sql`
4. 检查每个脚本的执行结果（会显示提示信息）

---

## ✅ 验证数据库创建

### 1. 验证数据库数量

```sql
SHOW DATABASES;
```

**预期结果**：应显示以下7个数据库
- user_service_db
- product_service_db
- groupbuy_service_db
- order_service_db
- payment_service_db
- delivery_service_db
- leader_service_db

### 2. 验证表数量

```sql
SELECT 
    TABLE_SCHEMA AS '数据库',
    COUNT(*) AS '表数量'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA IN (
    'user_service_db', 'product_service_db', 'groupbuy_service_db',
    'order_service_db', 'payment_service_db', 'delivery_service_db',
    'leader_service_db'
)
GROUP BY TABLE_SCHEMA
ORDER BY TABLE_SCHEMA;
```

**预期结果**：
| 数据库 | 表数量 |
|--------|--------|
| user_service_db | 5 |
| product_service_db | 2 |
| groupbuy_service_db | 3 |
| order_service_db | 3 |
| payment_service_db | 1 |
| delivery_service_db | 1 |
| leader_service_db | 4 |

### 3. 验证数据导入

```sql
-- 用户服务数据库
USE user_service_db;
SELECT COUNT(*) AS user_count FROM sys_user;  -- 应为 5
SELECT COUNT(*) AS address_count FROM user_address;  -- 应为 1
SELECT COUNT(*) AS account_count FROM user_account;  -- 应为 5
SELECT COUNT(*) AS feedback_count FROM user_feedback;  -- 应为 2
SELECT COUNT(*) AS log_count FROM sys_operation_log;  -- 应为 7

-- 团长服务数据库
USE leader_service_db;
SELECT COUNT(*) AS community_count FROM community;  -- 应为 2
```

### 4. 验证外键约束

```sql
-- 查看用户服务数据库的外键
SELECT 
  TABLE_NAME AS '表名',
  CONSTRAINT_NAME AS '约束名',
  REFERENCED_TABLE_NAME AS '关联表'
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'user_service_db'
  AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME;
```

**预期结果**：应显示4个外键
| 表名 | 约束名 | 关联表 |
|------|--------|--------|
| sys_operation_log | fk_log_user | sys_user |
| user_account | fk_account_user | sys_user |
| user_address | fk_address_user | sys_user |
| user_feedback | fk_feedback_user | sys_user |

---

## 📋 数据库配置（Spring Boot）

### UserService 配置

**文件**：`UserService/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_service_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: none  # 不自动创建表，使用SQL脚本
    show-sql: true
```

### ProductService 配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/product_service_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

**其他5个服务类似**，只需修改 `url` 中的数据库名称。

---

## ⚠️ 重要说明

### 1. 跨库外键已删除

**原因**：微服务架构下，禁止跨库JOIN和物理外键约束。

**示例**：
```sql
-- ❌ 原SQL中存在，拆分后已删除
CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) 
  REFERENCES `sys_user` (`user_id`)
```

**改为应用层校验**：
```java
// OrderService 创建订单时
UserDTO user = userServiceClient.getUserById(userId);
if (user == null) {
    throw new BusinessException("用户不存在");
}
```

### 2. 数据一致性策略

**跨库关联字段**（仅应用层校验）：
- `sys_user.community_id` → `leader_service_db.community`
- `order_main.user_id` → `user_service_db.sys_user`
- `order_main.receive_address_id` → `user_service_db.user_address`
- `order_item.product_id` → `product_service_db.product`
- `group_buy.product_id` → `product_service_db.product`
- `group_buy_team.launcher_id` → `user_service_db.sys_user`
- `payment_record.order_id` → `order_service_db.order_main`
- 等22个跨库关联...

**需要在代码中实现**：
1. **服务间HTTP调用**：通过OpenFeign调用其他服务验证数据有效性
2. **数据冗余（快照）**：`order_item` 表保存 `product_name`、`product_img`，避免频繁跨服务查询
3. **分布式事务**：使用Saga模式或消息队列（RabbitMQ）保证最终一致性
4. **定期数据修复**：编写定时任务检查孤立数据并修复

### 3. 测试账号信息

```sql
-- 管理员账号
username: 1
password: 1  -- 明文，数据库存储SHA256加密后的值
role: 3

-- 普通用户账号
username: 测试用户1
password: 1
role: 1
```

**密码加密后的值**：`96cae35ce8a9b0244178bf28e4966c2ce1b8385723a96a6b838858cdd6ca0a1e`

---

## 🗂️ 与原单体数据库的对比

| 项目 | 原单体数据库 | 拆分后微服务数据库 |
|------|-------------|------------------|
| 数据库数量 | 1个（community_group_buy） | 7个独立数据库 |
| 表数量 | 19张 | 19张（不变） |
| 物理外键 | 33个 | 10个（仅单库内） |
| 跨库关联 | 无 | 22个（应用层校验） |
| 服务耦合度 | 高（共享数据库） | 低（独立数据库） |
| 部署方式 | 单点部署 | 独立部署 |
| 故障隔离 | 差（全局影响） | 好（局部影响） |

---

## 📝 后续开发建议

### 1. 服务间调用接口（OpenFeign）

**UserService 对外提供的接口**：
```java
@FeignClient(name = "user-service", url = "http://localhost:8061")
public interface UserServiceClient {
    @GetMapping("/api/user/{userId}")
    Result<UserDTO> getUserById(@PathVariable Long userId);
    
    @GetMapping("/api/address/{addressId}")
    Result<AddressDTO> getAddressById(@PathVariable Long addressId);
}
```

### 2. 补偿事务表（每个服务新增）

```sql
CREATE TABLE saga_compensation_log (
  saga_id VARCHAR(50) PRIMARY KEY,
  service_name VARCHAR(50) NOT NULL,
  action VARCHAR(50) NOT NULL,
  compensation_data TEXT,  -- JSON格式，用于回滚
  status TINYINT NOT NULL DEFAULT 0,  -- 0-待补偿/1-已补偿
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT = 'Saga补偿事务日志';
```

### 3. 数据一致性检查脚本

```sql
-- 检查订单表中不存在的user_id（孤立数据）
SELECT o.order_id, o.user_id
FROM order_service_db.order_main o
WHERE NOT EXISTS (
  SELECT 1 FROM user_service_db.sys_user u 
  WHERE u.user_id = o.user_id
);
```

---

## 🔧 常见问题

### Q1: 执行SQL脚本报错 "Unknown database"
**原因**：MySQL版本过低或字符集不支持。  
**解决**：确保MySQL版本 ≥ 8.0.36，字符集为 utf8mb4。

### Q2: 数据导入后用户数量不对
**原因**：脚本未按顺序执行。  
**解决**：按01-07顺序重新执行，或删除数据库后重建。

### Q3: 如何删除所有数据库重新开始？
```sql
DROP DATABASE IF EXISTS user_service_db;
DROP DATABASE IF EXISTS product_service_db;
DROP DATABASE IF EXISTS groupbuy_service_db;
DROP DATABASE IF EXISTS order_service_db;
DROP DATABASE IF EXISTS payment_service_db;
DROP DATABASE IF EXISTS delivery_service_db;
DROP DATABASE IF EXISTS leader_service_db;
```

---

## 📞 技术支持

如有疑问，请参考：
- [数据库微服务拆分对比报告](../docs/社区团购系统/数据库微服务拆分对比报告.md)
- [毕业设计报告_第5章_数据库设计](../docs/社区团购系统/毕业设计报告_第5章_数据库设计（按样例格式）.md)

---

**创建日期**: 2025-10-29  
**版本**: v3.0 微服务拆分版  
**作者**: 22软本3-20221204229-耿康瑞

