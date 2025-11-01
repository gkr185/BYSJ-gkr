# OrderService - 订单服务

## 📋 服务概述

**服务名称**: order-service  
**端口**: 8065  
**数据库**: order_service_db  
**功能**: 订单管理、购物车管理、订单超时处理  
**版本**: v1.0.0  
**开发日期**: 2025-11-01

---

## 🎯 核心功能

### 1. Feign内部接口（供GroupBuyService调用）🔴 最关键

| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| **创建订单** | POST | `/api/order/feign/create` | ⭐ 参团时创建订单 |
| **批量更新订单状态** | POST | `/api/order/feign/batchUpdateStatus` | ⭐ 成团后批量更新 |
| **更新订单状态** | POST | `/api/order/feign/updateStatus` | 退款时更新状态 |
| **取消订单** | POST | `/api/order/feign/cancel/{orderId}` | 退团时取消订单 |
| **查询订单详情** | GET | `/api/order/feign/{orderId}` | 获取订单信息 |

### 2. 用户端API接口

| 接口 | 方法 | 路径 | 说明 |
|-----|------|------|------|
| 查询我的订单 | GET | `/api/order/my` | 分页查询订单列表 |
| 查询订单详情 | GET | `/api/order/{orderId}` | 订单详细信息 |
| 取消订单 | POST | `/api/order/cancel/{orderId}` | 用户主动取消 |
| 确认收货 | POST | `/api/order/confirm/{orderId}` | 确认收货 |

### 3. 定时任务

- **订单超时取消**: 每5分钟检查一次，自动取消超时未支付订单（默认30分钟）

---

## 🗄️ 数据库设计

### 表结构（3张表）

#### 1. order_main（订单主表）

| 字段 | 类型 | 说明 |
|-----|------|------|
| order_id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| leader_id | BIGINT | 团长ID |
| order_sn | VARCHAR(50) | 订单编号（唯一） |
| total_amount | DECIMAL(10,2) | 商品总金额 |
| discount_amount | DECIMAL(10,2) | 优惠金额 |
| pay_amount | DECIMAL(10,2) | 实付金额 |
| order_status | TINYINT | 订单状态（0-6） |
| pay_status | TINYINT | 支付状态（0-1） |
| receive_address_id | BIGINT | 收货地址ID |

**订单状态枚举**:
- 0: 待支付
- 1: 待发货
- 2: 配送中
- 3: 已送达
- 4: 已取消
- 5: 退款中
- 6: 已退款

#### 2. order_item（订单明细表 - 快照设计）

| 字段 | 类型 | 说明 |
|-----|------|------|
| item_id | BIGINT | 主键 |
| order_id | BIGINT | 订单ID |
| product_id | BIGINT | 商品ID |
| activity_id | BIGINT | 拼团活动ID（可空） |
| product_name | VARCHAR(100) | 商品名称（快照） |
| product_img | VARCHAR(255) | 商品图片（快照） |
| price | DECIMAL(10,2) | 购买单价 |
| quantity | INT | 购买数量 |
| total_price | DECIMAL(10,2) | 小计金额 |

**快照设计说明**:
- `product_name`, `product_img`, `price` 保存下单时的商品信息
- 避免商品信息变更影响历史订单

#### 3. shopping_cart（购物车表）

| 字段 | 类型 | 说明 |
|-----|------|------|
| cart_id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| product_id | BIGINT | 商品ID |
| activity_id | BIGINT | 拼团活动ID（可空） |
| quantity | INT | 数量 |

**唯一索引**: `(user_id, product_id, activity_id)` - 防止重复添加

---

## 🔗 微服务依赖

### OrderService 调用的服务

| 服务 | 接口 | 用途 |
|-----|------|------|
| **UserService** | `/api/user/feign/validate/{userId}` | 验证用户 |
| **UserService** | `/api/user/feign/address/{addressId}` | 获取地址 |
| **ProductService** | `/api/product/feign/{productId}` | 获取商品信息 |
| **ProductService** | `/api/product/feign/deductStock` | 扣减库存 |
| **LeaderService** | `/api/leader/feign/validate/{leaderId}` | 验证团长 |

### 调用 OrderService 的服务

| 服务 | 场景 |
|-----|------|
| **GroupBuyService** | 参团时创建订单、成团后更新订单、退团时取消订单 |
| **PaymentService** | 支付成功后更新订单状态 |

---

## 🚀 启动步骤

### 1. 前置条件

```bash
# 1. 启动Consul
consul agent -dev

# 2. 启动MySQL并导入数据库
mysql -u root -p < sql/order_service_db.sql

# 3. 启动依赖服务
cd UserService && mvn spring-boot:run &
cd ProductService && mvn spring-boot:run &
cd LeaderService && mvn spring-boot:run &
cd gateway-service && mvn spring-boot:run &
```

### 2. 启动OrderService

```bash
cd OrderService
mvn clean spring-boot:run
```

### 3. 验证启动

访问以下地址确认服务正常：

- **Swagger文档**: http://localhost:8065/swagger-ui.html
- **健康检查**: http://localhost:8065/actuator/health
- **Consul控制台**: http://localhost:8500 （查看order-service是否注册）

---

## 🧪 接口测试

### 测试1: 创建订单（Feign接口）

```bash
curl -X POST http://localhost:8065/api/order/feign/create \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 4,
    "leaderId": 3,
    "addressId": 2,
    "productId": 1,
    "productName": "测试商品",
    "productImg": "http://example.com/img.jpg",
    "quantity": 1,
    "price": 14.00,
    "activityId": 1
  }'
```

**预期响应**:
```json
{
  "code": 200,
  "message": "订单创建成功",
  "data": 1,
  "timestamp": "2025-11-01T14:30:00"
}
```

### 测试2: 通过Gateway测试

```bash
# 通过网关创建订单
curl -X POST http://localhost:9000/api/order/feign/create \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{...}'
```

### 测试3: 查询我的订单

```bash
curl -X GET "http://localhost:9000/api/order/my?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📊 订单状态流转

```
创建订单（用户参团）
    ↓
【待支付】(0) ←─────────┐
    │                 │ 超时取消（30分钟）
    │ 支付成功         │
    ↓                 │
【待发货】(1) ←────────┤
    │                 │ 用户取消/退团
    │ 发货             │
    ↓                 │
【配送中】(2)          │
    │                 │
    │ 送达             │
    ↓                 │
【已送达】(3)          │
                      │
【已取消】(4) ←────────┘
【退款中】(5)
【已退款】(6)
```

---

## 🔧 配置说明

### application.yml 关键配置

```yaml
server:
  port: 8065

spring:
  application:
    name: order-service  # ⭐ 小写-连字符命名
  datasource:
    url: jdbc:mysql://localhost:3306/order_service_db
    username: root
    password: 123456

# 订单配置
order:
  expire:
    minutes: 30  # 订单超时时间（分钟）
```

---

## 📝 开发规范

### 1. 实体类设计

- ✅ 严格匹配SQL字段定义
- ✅ 使用JPA审计功能（`@CreatedDate`, `@LastModifiedDate`）
- ✅ 快照设计：订单明细保存商品快照

### 2. 事务管理

```java
@Transactional(rollbackFor = Exception.class)
public Long createOrder(CreateOrderRequest request) {
    // 事务保证：订单主表 + 订单明细原子性
}
```

### 3. 日志规范

```java
@OperationLog(module = "订单管理", operation = "创建订单")
public Long createOrder(CreateOrderRequest request) {
    log.info("开始创建订单: userId={}, productId={}", ...);
    // ...
    log.info("订单创建成功: orderId={}", orderId);
}
```

---

## 🐛 常见问题

### 问题1: GroupBuyService调用OrderService失败（503）

**原因**: OrderService未启动或未注册到Consul  
**解决**:
1. 确认OrderService已启动
2. 检查Consul中是否有`order-service`服务（小写-连字符）
3. 检查服务健康状态

### 问题2: 订单创建失败（用户不存在）

**原因**: Feign调用UserService失败  
**解决**:
1. 确认UserService已启动
2. 检查用户ID是否有效
3. 查看OrderService日志中的Feign调用日志

### 问题3: 定时任务不执行

**原因**: 未启用调度功能  
**解决**: 确认启动类有`@EnableScheduling`注解

---

## 🎯 核心技术亮点

### 1. ⭐⭐⭐⭐⭐ Feign内部接口设计

- 4个核心接口完整支持GroupBuyService
- 异常处理完善（try-catch + 降级）
- 日志记录详细

### 2. ⭐⭐⭐⭐ 快照设计模式

- 订单明细保存下单时的商品快照
- 避免商品信息变更影响历史订单
- 符合电商系统最佳实践

### 3. ⭐⭐⭐⭐ 定时任务设计

- 每5分钟自动检查超时订单
- 异常隔离（单个订单失败不影响其他）
- 可配置超时时间

### 4. ⭐⭐⭐ 集成Common模块

- 统一响应格式（`Result<T>`）
- AOP操作日志（`@OperationLog`）
- 全局异常处理

---

## 📈 后续优化

- [ ] 添加购物车功能完整实现
- [ ] 集成库存扣减逻辑
- [ ] 订单状态机优化
- [ ] 添加订单导出功能
- [ ] 性能优化（批量查询）

---

## 👨‍💻 开发者

**姓名**: 耿康瑞  
**学号**: 20221204229  
**日期**: 2025-11-01

---

**状态**: ✅ OrderService开发完成，已解除GroupBuyService阻塞！

**核心成果**:
- ✅ 4个Feign核心接口实现
- ✅ 订单CRUD完整功能
- ✅ 定时任务自动取消超时订单
- ✅ 集成Gateway路由
- ✅ 严格匹配SQL字段定义
- ✅ 集成Common模块

**下一步**: 启动服务测试GroupBuyService拼团流程！

