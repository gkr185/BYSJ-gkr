# PaymentService - 支付服务

## 📋 服务信息

- **服务名称**: payment-service
- **服务端口**: 8066
- **数据库**: payment_service_db（1张表）
- **开发状态**: ✅ v1.0 基础版完成（2025-11-01）
- **开发者**: 耿康瑞

---

## 🎯 服务定位

PaymentService 是**支付流转的中枢服务**，负责所有支付相关的业务逻辑。

### 核心职责

1. **支付发起** - 创建支付订单，调用支付渠道
2. **支付处理** - 处理支付成功/失败，更新支付状态
3. **资金流转** - 余额支付的扣款/充值/冻结/解冻
4. **支付回调** - 接收第三方支付回调，通知业务服务
5. **退款处理** - 处理订单退款，返还资金
6. **支付记录** - 记录所有支付/充值/退款明细

---

## 🏗️ 技术架构

### 依赖服务

```
PaymentService (8066)
    ↓
├─ UserService (8061) - 余额扣款/充值
├─ OrderService (8065) - 更新订单支付状态
└─ GroupBuyService (8063) - 支付回调通知
```

### 技术栈

- **Spring Boot**: 3.2.3
- **Spring Cloud**: 2023.0.0
- **OpenFeign**: 服务间调用
- **Hutool**: 5.8.25（AES、SHA256加密）
- **MySQL**: 8.0.36
- **Consul**: 服务注册发现

---

## 📦 已实现功能（v1.0）

### 1. 支付发起 ✅

- [x] 创建支付接口 (`POST /api/payment/create`)
- [x] 支付类型：余额支付（完整实现）
- [x] 支付类型：微信支付（TODO，返回提示）
- [x] 支付类型：支付宝支付（TODO，返回提示）

### 2. 支付处理 ✅

- [x] 余额支付完整流程
  - 调用UserService扣款
  - 更新支付记录
  - 执行支付成功后处理
- [x] 支付成功后处理
  - 调用OrderService更新订单支付状态
  - 判断是否拼团订单
  - 调用GroupBuyService支付回调（触发成团检查）

### 3. 退款处理 ✅

- [x] 申请退款接口 (`POST /api/payment/feign/refund`)
- [x] 余额退款完整流程
  - 查询原支付记录
  - 创建退款记录（amount为负数）
  - 调用UserService增加余额
- [x] 微信退款（TODO）
- [x] 支付宝退款（TODO）

### 4. 支付查询 ✅

- [x] 查询支付记录 (`GET /api/payment/record/{payId}`)
- [x] 查询订单支付记录 (`GET /api/payment/order/{orderId}`)

### 5. 安全设计 ✅

- [x] 支付回调信息AES加密
- [x] 数据签名SHA256防篡改
- [x] Feign调用异常降级处理

---

## 🔄 核心流程

### 拼团支付完整闭环 ⭐⭐⭐⭐⭐

```
用户发起支付
    ↓
PaymentService.createPayment()
    ├─ 创建支付记录
    └─ 余额支付
        ├─ UserService.deduct() - 余额扣款 ✅
        ├─ 更新支付记录(pay_status=1)
        └─ handleOrderPaymentSuccess()
            ├─ OrderService.updatePayStatus() - 更新订单支付状态 ✅
            ├─ OrderService.isGroupBuyOrder() - 判断订单类型 ✅
            └─ GroupBuyService.paymentCallback() - 支付回调 ✅
                ├─ 更新参团状态(PAID)
                ├─ 团人数+1
                ├─ 检查是否成团
                └─ 成团逻辑
                    ├─ 更新团状态(SUCCESS)
                    └─ OrderService.batchUpdateStatus() - 批量更新订单 ✅
    ↓
✅ 闭环完成！订单进入配送流程
```

### 退款流程 ⭐⭐⭐

```
GroupBuyService定时任务检测过期团
    ↓
PaymentService.refund()
    ├─ 查询原支付记录
    ├─ 创建退款记录(amount为负数)
    └─ 余额退款
        ├─ UserService.recharge() - 余额充值 ✅
        └─ 更新退款记录(pay_status=1)
    ↓
✅ 闭环完成！资金已返还
```

---

## 📡 API接口清单

### 用户端接口（3个）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建支付 | POST | `/api/payment/create` | ⭐最核心接口 |
| 查询支付记录 | GET | `/api/payment/record/{payId}` | 查询详情 |
| 查询订单支付 | GET | `/api/payment/order/{orderId}` | 订单支付记录 |

### Feign内部接口（1个）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 申请退款 | POST | `/api/payment/feign/refund` | ⭐关键接口 |

---

## 🗄️ 数据库设计

### payment_record（支付记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| pay_id | BIGINT | 支付记录ID（主键） |
| user_id | BIGINT | 用户ID（跨库关联） |
| order_id | BIGINT | 订单ID（充值时为null） |
| pay_type | TINYINT | 支付类型（1-微信；2-支付宝；3-余额） |
| amount | DECIMAL(10,2) | 金额（正数-支付/充值；负数-退款） |
| pay_status | TINYINT | 状态（0-失败；1-成功） |
| transaction_id | VARCHAR(100) | 第三方支付流水号 |
| wx_transaction_id | VARCHAR(100) | 微信支付专用流水号 |
| pay_callback_info | TEXT | 支付回调信息（AES加密） |
| encrypt_sign | VARCHAR(255) | 数据加密签名（SHA256） |
| create_time | DATETIME | 创建时间 |

**索引**:
- PRIMARY KEY (`pay_id`)
- INDEX `idx_user_id` (`user_id`)
- INDEX `idx_order_id` (`order_id`)
- INDEX `idx_transaction_id` (`transaction_id`)
- INDEX `idx_pay_status` (`pay_status`)
- INDEX `idx_create_time` (`create_time`)

---

## 🚀 快速启动

### 1. 前置条件

- MySQL已启动，并创建 `payment_service_db` 数据库
- Consul已启动（端口8500）
- UserService已启动（端口8061）
- OrderService已启动（端口8065）
- GroupBuyService已启动（端口8063）

### 2. 启动服务

```bash
cd PaymentService
mvn clean install
mvn spring-boot:run
```

### 3. 验证服务

- **Swagger文档**: http://localhost:8066/swagger-ui.html
- **健康检查**: http://localhost:8066/actuator/health
- **Consul注册**: http://localhost:8500

---

## 📝 测试用例

### 测试1: 余额支付完整流程

**前置条件**:
- 用户余额充足
- 已有待支付订单

**测试步骤**:
```bash
# 1. 创建支付
POST http://localhost:8066/api/payment/create
Content-Type: application/json
X-User-Id: 1

{
  "orderId": 1,
  "payType": 3,
  "amount": 14.00
}

# 2. 验证：用户余额减少
# 3. 验证：订单支付状态更新为1
# 4. 验证：拼团订单触发成团检查
```

**期望结果**:
- ✅ 支付记录 `pay_status=1`
- ✅ 用户余额减少14.00
- ✅ 订单 `pay_status=1`
- ✅ 参团状态更新为"已支付"
- ✅ 成团后订单状态更新为"待发货"

---

### 测试2: 拼团失败退款

**测试步骤**:
```bash
# 1. 申请退款
POST http://localhost:8066/api/payment/feign/refund
Content-Type: application/json

{
  "orderId": 1,
  "reason": "拼团失败自动退款"
}

# 2. 验证：用户余额恢复
# 3. 验证：订单状态更新为"已退款"
```

**期望结果**:
- ✅ 退款记录创建（`amount`为负数）
- ✅ 用户余额恢复
- ✅ 订单状态更新为"已退款"

---

## ⚠️ TODO清单

### 近期（v1.1）

- [ ] 微信支付集成（统一下单API）
- [ ] 微信支付回调处理
- [ ] 微信退款API
- [ ] 充值功能（`POST /api/payment/recharge`）

### 中期（v1.2）

- [ ] 支付宝支付集成
- [ ] 支付宝退款API
- [ ] 支付回调幂等性测试

### 长期（v2.0）

- [ ] 定时任务补偿机制（检查不一致数据）
- [ ] 支付统计报表
- [ ] 支付流水导出

---

## 📊 开发进度

| 功能模块 | 完成度 | 说明 |
|---------|--------|------|
| 项目骨架 | 100% | ✅ 完成 |
| 余额支付 | 100% | ✅ 完成（闭环验证通过） |
| 余额退款 | 100% | ✅ 完成（闭环验证通过） |
| 微信支付 | 0% | ⏳ 待开发 |
| 支付宝支付 | 0% | ⏳ 待开发 |
| 充值功能 | 0% | ⏳ 待开发 |
| **总计** | **60%** | **核心功能已完成** |

---

## 🔗 相关文档

- [PaymentService设计文档](../../docs/社区团购系统/一级文档（持续更新）/DESIGN_PaymentService_2025-11-01.md)
- [服务间协同闭环分析](../../docs/社区团购系统/一级文档（持续更新）/服务间协同闭环分析_2025-11-01.md)
- [数据库设计说明](../../docs/社区团购系统/二级文档（参考）/数据库设计说明文档_v6.0.md)

---

**创建日期**: 2025-11-01  
**版本**: v1.0  
**状态**: ✅ 余额支付闭环完成，可演示

