# PaymentService API文档

**版本**: v1.0.0  
**服务端口**: `8066`  
**基础路径**: `http://localhost:8066`  
**通过网关访问**: `http://localhost:9000`  
**Swagger文档**: `http://localhost:8066/swagger-ui.html`  
**文档日期**: 2025-11-01  
**最后更新**: 2025-11-01

---

## 📋 接口概览

| 模块 | 接口数 | 路径前缀 | 说明 |
|------|--------|---------|------|
| **Feign内部接口** | 1个 | `/api/payment/feign` | 🔴 供OrderService/GroupBuyService调用 |
| 用户端支付接口 | 6个 | `/api/payment` | 支付、充值、查询 |
| **总计** | **7个** | - | - |

---

## 🔴 重要说明

### 核心功能

PaymentService是**支付流转的中枢服务**，负责：
- ✅ 支付发起（余额支付、微信支付、支付宝支付）
- ✅ 支付成功后处理（更新订单、回调GroupBuyService）
- ✅ 退款处理（余额退款、第三方退款）
- ✅ 充值管理（余额充值）
- ✅ 支付查询（支付记录、充值记录）

### 服务间协同流程

#### 支付闭环
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
                    └─ OrderService.batchUpdateStatus() ✅
    ↓
✅ 支付完成！
```

#### 退款闭环
```
拼团失败/订单取消
    ↓
GroupBuyService/OrderService 调用
    ↓
PaymentService.refund()
    ├─ 查询原支付记录
    ├─ 创建退款记录(amount为负数)
    └─ handleBalanceRefund()
        ├─ UserService.recharge() - 余额充值 ✅
        └─ 更新退款记录(pay_status=1)
    ↓
✅ 退款完成！
```

---

## 目录

1. [Feign内部接口](#1-feign内部接口供微服务调用)
2. [用户端支付接口](#2-用户端支付接口)
3. [充值接口](#3-充值接口)
4. [查询接口](#4-查询接口)
5. [通用响应格式](#5-通用响应格式)
6. [错误码说明](#6-错误码说明)
7. [测试用例](#7-测试用例)

---

## 1. Feign内部接口（供微服务调用）

### 1.1 申请退款 ⭐⭐⭐⭐⭐

```http
POST /api/payment/feign/refund
Content-Type: application/json
```

**功能**: 订单取消或拼团失败时退款（最核心接口）

**调用方**: 
- OrderService（订单取消）
- GroupBuyService（拼团失败定时任务）

**使用场景**:
- 用户取消订单
- 拼团失败自动退款
- 订单超时未支付退款

**请求参数**:
```json
{
  "orderId": 1,
  "reason": "拼团失败自动退款"
}
```

**参数说明**:

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| orderId | Long | 是 | 订单ID |
| reason | String | 否 | 退款原因 |

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "退款成功",
  "timestamp": "2025-11-01T16:30:00"
}
```

失败响应：
```json
{
  "code": 500,
  "message": "退款失败: 未找到支付记录",
  "timestamp": "2025-11-01T16:30:00"
}
```

**业务逻辑**:
1. 查询原支付记录（`payment_record` 表）
2. 验证是否已退款（`amount < 0` 表示已退款）
3. 创建退款记录（`amount` 为负数）
4. 根据原支付方式退款：
   - 余额支付：调用 `UserService.recharge()` 增加余额
   - 微信支付：调用微信退款API（TODO）
   - 支付宝支付：调用支付宝退款API（TODO）
5. 更新退款记录状态为成功

**注意事项**:
- 幂等性：重复调用不会重复退款
- 退款记录：`amount` 为负数，`order_id` 与原支付记录相同
- 退款失败：抛出 `BusinessException`，调用方需补偿

**错误码**:
- `500`: 未找到支付记录
- `500`: 订单已退款，无需重复操作
- `500`: 退款失败（UserService调用失败）

---

## 2. 用户端支付接口

### 2.1 创建支付 ⭐⭐⭐⭐⭐

```http
POST /api/payment/create
Content-Type: application/json
X-User-Id: 1  # Gateway解析JWT传递
```

**功能**: 用户下单后创建支付订单（最核心接口）

**认证**: 需要JWT Token（Gateway传递 `X-User-Id`）

**请求参数**:
```json
{
  "orderId": 1,
  "payType": 3,
  "amount": 14.00
}
```

**参数说明**:

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| orderId | Long | 是 | 订单ID |
| payType | Integer | 是 | 支付方式（1-微信；2-支付宝；3-余额） |
| amount | BigDecimal | 是 | 支付金额 |

**响应示例**:

成功响应（余额支付）：
```json
{
  "code": 200,
  "message": "支付成功",
  "data": {
    "payId": 1,
    "payType": 3,
    "payStatus": 1
  },
  "timestamp": "2025-11-01T16:30:00"
}
```

成功响应（微信支付）：
```json
{
  "code": 200,
  "message": "支付订单创建成功",
  "data": {
    "payId": 2,
    "payType": 1,
    "payStatus": 0,
    "wxPayParams": {
      "appId": "wx...",
      "timeStamp": "1698825000",
      "nonceStr": "...",
      "package": "prepay_id=...",
      "signType": "RSA",
      "paySign": "..."
    }
  },
  "timestamp": "2025-11-01T16:30:00"
}
```

失败响应：
```json
{
  "code": 500,
  "message": "余额不足",
  "data": {
    "payId": 3,
    "payType": 3,
    "payStatus": 0,
    "errorMessage": "余额不足"
  },
  "timestamp": "2025-11-01T16:30:00"
}
```

**业务逻辑**:

#### 余额支付流程：
1. 创建支付记录（`pay_status=0`）
2. 调用 `UserService.deduct()` 扣款
3. 扣款成功 → 更新支付记录（`pay_status=1`）
4. 执行支付成功后处理：
   - 调用 `OrderService.updatePayStatus()` 更新订单支付状态
   - 调用 `OrderService.isGroupBuyOrder()` 判断订单类型
   - 如果是拼团订单，调用 `GroupBuyService.paymentCallback()` 触发成团检查
5. 返回支付成功

#### 微信/支付宝支付流程：
1. 创建支付记录（`pay_status=0`）
2. 调用第三方支付API统一下单
3. 返回支付参数给前端
4. 等待支付回调（TODO）

**注意事项**:
- 余额支付：同步返回结果，立即完成
- 微信/支付宝支付：异步支付，需要等待回调
- 支付失败：不抛异常，返回 `payStatus=0` 和错误信息

**错误码**:
- `401`: 未登录
- `500`: 余额不足
- `500`: 不支持的支付方式
- `500`: 支付失败

---

## 3. 充值接口

### 3.1 余额充值 ⭐⭐⭐

```http
POST /api/payment/recharge
Content-Type: application/json
X-User-Id: 1  # Gateway解析JWT传递
```

**功能**: 用户充值余额（简化版本）

**认证**: 需要JWT Token

**说明**: 当前为简化版本，直接增加余额。后续集成微信支付/支付宝支付后，需要先创建支付订单，等待支付回调。

**请求参数**:
```json
{
  "amount": 100.00,
  "payType": 3
}
```

**参数说明**:

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| amount | BigDecimal | 是 | 充值金额 |
| payType | Integer | 是 | 支付方式（1-微信；2-支付宝；3-余额）⚠️当前仅支持3 |

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "充值成功",
  "data": {
    "payId": 10,
    "userId": 1,
    "orderId": null,
    "payType": 3,
    "amount": 100.00,
    "payStatus": 1,
    "transactionId": "RECHARGE_1698825000123",
    "createTime": "2025-11-01T16:30:00"
  },
  "timestamp": "2025-11-01T16:30:00"
}
```

失败响应：
```json
{
  "code": 500,
  "message": "充值失败: 充值金额必须大于0",
  "timestamp": "2025-11-01T16:30:00"
}
```

**业务逻辑**:
1. 创建充值记录（`order_id=null`, `pay_status=0`）
2. 调用 `UserService.recharge()` 增加余额
3. 更新充值记录（`pay_status=1`）
4. 返回充值记录

**注意事项**:
- 充值记录：`order_id` 为 `null`
- `amount` 为正数
- 简化版本：直接增加余额，无需第三方支付

---

## 4. 查询接口

### 4.1 查询支付记录

```http
GET /api/payment/record/{payId}
X-User-Id: 1
```

**功能**: 根据支付记录ID查询详情

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| payId | Long | 是 | 支付记录ID（路径参数） |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "payId": 1,
    "userId": 1,
    "orderId": 1,
    "payType": 3,
    "amount": 14.00,
    "payStatus": 1,
    "transactionId": "BALANCE_1698825000123",
    "encryptSign": "abc123...",
    "createTime": "2025-11-01T16:30:00"
  },
  "timestamp": "2025-11-01T16:30:00"
}
```

---

### 4.2 查询订单支付记录

```http
GET /api/payment/order/{orderId}
X-User-Id: 1
```

**功能**: 根据订单ID查询支付记录

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| orderId | Long | 是 | 订单ID（路径参数） |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "payId": 1,
    "userId": 1,
    "orderId": 1,
    "payType": 3,
    "amount": 14.00,
    "payStatus": 1,
    "transactionId": "BALANCE_1698825000123",
    "createTime": "2025-11-01T16:30:00"
  },
  "timestamp": "2025-11-01T16:30:00"
}
```

---

### 4.3 查询所有支付记录

```http
GET /api/payment/records
X-User-Id: 1
```

**功能**: 查询当前用户的所有支付/充值记录

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "payId": 10,
      "userId": 1,
      "orderId": null,
      "payType": 3,
      "amount": 100.00,
      "payStatus": 1,
      "transactionId": "RECHARGE_1698825000123",
      "createTime": "2025-11-01T16:30:00"
    },
    {
      "payId": 1,
      "userId": 1,
      "orderId": 1,
      "payType": 3,
      "amount": 14.00,
      "payStatus": 1,
      "transactionId": "BALANCE_1698825000456",
      "createTime": "2025-11-01T16:25:00"
    },
    {
      "payId": 2,
      "userId": 1,
      "orderId": 1,
      "payType": 3,
      "amount": -14.00,
      "payStatus": 1,
      "transactionId": "REFUND_1698825000789",
      "createTime": "2025-11-01T16:20:00"
    }
  ],
  "timestamp": "2025-11-01T16:30:00"
}
```

**记录类型说明**:
- `orderId` 为 `null` → 充值记录
- `amount` 为正数 → 支付/充值
- `amount` 为负数 → 退款

---

### 4.4 查询充值记录

```http
GET /api/payment/recharge/records
X-User-Id: 1
```

**功能**: 查询当前用户的充值记录（仅包含 `orderId=null` 的记录）

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "payId": 10,
      "userId": 1,
      "orderId": null,
      "payType": 3,
      "amount": 100.00,
      "payStatus": 1,
      "transactionId": "RECHARGE_1698825000123",
      "createTime": "2025-11-01T16:30:00"
    }
  ],
  "timestamp": "2025-11-01T16:30:00"
}
```

---

## 5. 通用响应格式

### 5.1 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": "2025-11-01T16:30:00"
}
```

### 5.2 失败响应

```json
{
  "code": 500,
  "message": "错误信息",
  "timestamp": "2025-11-01T16:30:00"
}
```

### 5.3 响应字段说明

| 字段 | 类型 | 说明 |
|-----|------|------|
| code | Integer | 状态码（200-成功，其他-失败） |
| message | String | 响应消息 |
| data | Object | 响应数据（失败时可能为null） |
| timestamp | String | 响应时间戳 |

---

## 6. 错误码说明

| 错误码 | 错误信息 | 说明 | 解决方案 |
|-------|---------|------|---------|
| 200 | 操作成功 | 请求成功 | - |
| 401 | 未登录 | JWT Token缺失或失效 | 重新登录获取Token |
| 500 | 未找到支付记录 | 支付记录不存在 | 检查payId/orderId是否正确 |
| 500 | 余额不足 | 用户余额不足以支付 | 先充值再支付 |
| 500 | 订单已退款，无需重复操作 | 重复退款 | 幂等性保护，无需处理 |
| 500 | 不支持的支付方式 | payType不在[1,2,3]范围 | 检查payType参数 |
| 500 | 微信支付暂未开放 | 微信支付功能未实现 | 使用余额支付 |
| 500 | 支付宝支付暂未开放 | 支付宝支付功能未实现 | 使用余额支付 |
| 500 | 充值失败: ... | UserService调用失败 | 检查UserService是否正常 |
| 500 | 退款失败: ... | 退款流程异常 | 查看日志定位问题 |

---

## 7. 测试用例

### 7.1 完整支付流程测试

#### 步骤1：充值
```bash
POST http://localhost:8066/api/payment/recharge
Content-Type: application/json
X-User-Id: 1

{
  "amount": 100.00,
  "payType": 3
}

# 预期结果：
# - 返回充值记录
# - 用户余额增加100.00
```

#### 步骤2：创建支付
```bash
POST http://localhost:8066/api/payment/create
Content-Type: application/json
X-User-Id: 1

{
  "orderId": 1,
  "payType": 3,
  "amount": 14.00
}

# 预期结果：
# - 返回 payStatus=1（支付成功）
# - 用户余额减少14.00
# - 订单 pay_status=1
# - 拼团状态更新为"已支付"
# - 成团后订单状态更新为"待发货"
```

#### 步骤3：查询支付记录
```bash
GET http://localhost:8066/api/payment/order/1
X-User-Id: 1

# 预期结果：
# - 返回订单的支付记录
# - payStatus=1
```

#### 步骤4：申请退款（拼团失败场景）
```bash
POST http://localhost:8066/api/payment/feign/refund
Content-Type: application/json

{
  "orderId": 1,
  "reason": "拼团失败自动退款"
}

# 预期结果：
# - 退款记录创建（amount为负数）
# - 用户余额恢复14.00
# - 订单状态更新为"已退款"
```

---

### 7.2 边界条件测试

#### 测试1：余额不足
```bash
POST http://localhost:8066/api/payment/create
Content-Type: application/json
X-User-Id: 1

{
  "orderId": 2,
  "payType": 3,
  "amount": 1000.00
}

# 预期结果：
# - 返回 payStatus=0
# - errorMessage="余额不足"
# - 用户余额不变
```

#### 测试2：重复退款
```bash
# 第一次退款
POST http://localhost:8066/api/payment/feign/refund
Content-Type: application/json
{"orderId": 1, "reason": "测试"}

# 第二次退款（重复）
POST http://localhost:8066/api/payment/feign/refund
Content-Type: application/json
{"orderId": 1, "reason": "测试"}

# 预期结果：
# - 第二次返回"订单已退款，无需重复操作"
# - 幂等性保护，不会重复退款
```

---

## 8. 数据字典

### 8.1 支付类型（pay_type）

| 值 | 说明 | 状态 |
|---|------|------|
| 1 | 微信支付 | ⏳ 待开发 |
| 2 | 支付宝支付 | ⏳ 待开发 |
| 3 | 余额支付 | ✅ 已实现 |

### 8.2 支付状态（pay_status）

| 值 | 说明 |
|---|------|
| 0 | 失败/待支付 |
| 1 | 成功 |

### 8.3 支付记录类型识别

| 条件 | 类型 |
|------|------|
| `order_id` = null, `amount` > 0 | 充值记录 |
| `order_id` != null, `amount` > 0 | 支付记录 |
| `order_id` != null, `amount` < 0 | 退款记录 |

---

## 9. 安全设计

### 9.1 数据加密

- **支付回调信息**：使用AES加密存储在 `pay_callback_info` 字段
- **数据签名**：使用SHA256+盐值生成签名存储在 `encrypt_sign` 字段
- **防篡改**：每条支付记录都有加密签名

### 9.2 密钥配置

配置文件：`application.yml`

```yaml
security:
  aes:
    key: 1234567890abcdef1234567890abcdef
  sha256:
    salt: community_group_buy_secret_salt_2025
```

⚠️ **生产环境建议**：
- 密钥存储在配置中心或环境变量
- 定期更换密钥
- 使用更强的加密算法

---

## 10. 常见问题（FAQ）

### Q1: 为什么余额支付是同步的，微信支付是异步的？

**A**: 
- 余额支付：内部服务调用，直接扣款，可以立即知道结果
- 微信/支付宝支付：需要调用第三方API，用户在第三方平台完成支付，通过回调通知结果

### Q2: 充值功能为什么是简化版本？

**A**: 
- 当前版本：为了快速测试支付流程，直接增加余额
- 完整版本（TODO）：应该先创建支付订单，用户通过微信/支付宝支付，等待支付回调后再增加余额

### Q3: 退款失败怎么办？

**A**: 
- 退款失败会抛出异常，调用方（GroupBuyService/OrderService）需要补偿
- 建议实现定时任务，检查退款失败的记录，自动重试

### Q4: 支付回调信息为什么要AES加密？

**A**: 
- 支付回调信息可能包含敏感数据（如用户信息、交易流水号）
- AES加密可以防止数据泄露
- SHA256签名可以防止数据篡改

### Q5: 如何测试完整的拼团支付流程？

**A**: 
1. 启动所有服务（UserService, OrderService, GroupBuyService, PaymentService）
2. 用户充值余额
3. 用户参团（会自动创建订单）
4. 用户支付（调用PaymentService）
5. 支付成功后，自动更新订单状态，触发成团检查
6. 成团后，订单状态更新为"待发货"

---

## 11. 开发计划

### v1.0（✅ 已完成）
- ✅ 余额支付
- ✅ 余额退款
- ✅ 余额充值（简化版本）
- ✅ 支付查询
- ✅ AES加密 + SHA256签名

### v1.1（⏳ 计划中）
- [ ] 微信支付集成
- [ ] 微信支付回调处理
- [ ] 微信退款API

### v1.2（⏳ 计划中）
- [ ] 支付宝支付集成
- [ ] 支付宝支付回调处理
- [ ] 支付宝退款API

### v2.0（⏳ 远期规划）
- [ ] 充值功能完整版（需要支付回调）
- [ ] 定时任务补偿机制
- [ ] 支付统计报表
- [ ] 支付流水导出

---

## 12. 相关文档

- [PaymentService README](../../../community-group-buy-backend/PaymentService/README.md)
- [DESIGN_PaymentService_2025-11-01](../一级文档（持续更新）/DESIGN_PaymentService_2025-11-01.md)
- [服务间协同闭环分析_2025-11-01](../一级文档（持续更新）/服务间协同闭环分析_2025-11-01.md)
- [PaymentService_完成报告_2025-11-01](../一级文档（持续更新）/PaymentService_完成报告_2025-11-01.md)

---

**文档版本**: v1.0.0  
**创建日期**: 2025-11-01  
**作者**: 耿康瑞  
**状态**: ✅ 完成

