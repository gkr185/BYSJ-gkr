# OrderService API文档

**版本**: v1.1.0 ⭐ **新增团长订单接口**  
**服务端口**: `8065`  
**基础路径**: `http://localhost:8065`  
**通过网关访问**: `http://localhost:9000`  
**Swagger文档**: `http://localhost:8065/swagger-ui.html`

---

## 📋 接口概览

| 模块 | 接口数 | 路径前缀 | 说明 |
|------|--------|---------|------|
| **Feign内部接口** | 5个 | `/api/order/feign` | 🔴 供GroupBuyService调用 |
| 用户端订单接口 | 4个 | `/api/order` | 订单查询、取消、确认收货 |
| **团长端订单接口** | **2个** | `/api/order/leader` | 🟡 供团长查询订单 ⭐ 新增 |
| **管理端订单接口** | **11个** | `/api/order/admin` | 🟢 供管理员使用 |
| **总计** | **22个** | - | - |

---

## 🔴 重要说明

### Feign内部接口调用流程

```
GroupBuyService 
    ↓ (Feign调用)
OrderService Feign接口 (/api/order/feign/*)
    ↓
OrderService 核心业务逻辑
    ↓
创建/更新/取消订单
```

### 用户端接口调用流程

```
前端 (Vue)
    ↓ (HTTP请求 + JWT Token)
Gateway (端口9000)
    ↓ (JWT验证 + 路由转发)
OrderService 用户端接口 (/api/order/*)
    ↓
返回订单数据
```

---

## 1. Feign内部接口（供微服务调用）

### 1.1 创建订单 ⭐⭐⭐⭐⭐

```http
POST /api/order/feign/create
Content-Type: application/json
```

**功能**: 用户参团时创建订单（最核心接口）

**调用方**: GroupBuyService.joinTeam()

**使用场景**:
- 用户参与拼团活动时
- 团长发起并参与拼团时

**请求参数**:
```json
{
  "userId": 4,
  "leaderId": 3,
  "addressId": 2,
  "productId": 1,
  "productName": "测试商品：牙膏",
  "productImg": "http://localhost:8062/uploads/product/20251031192059_512856.jpg",
  "quantity": 1,
  "price": 14.00,
  "activityId": 1
}
```

**参数说明**:

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| userId | Long | 是 | 用户ID |
| leaderId | Long | 是 | 团长ID |
| addressId | Long | 是 | 收货地址ID |
| productId | Long | 是 | 商品ID |
| productName | String | 是 | 商品名称（快照） |
| productImg | String | 否 | 商品图片URL（快照） |
| quantity | Integer | 是 | 购买数量 |
| price | BigDecimal | 是 | 购买单价（拼团价/原价） |
| activityId | Long | 否 | 拼团活动ID（非拼团为null） |

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "订单创建成功",
  "data": 1,
  "timestamp": "2025-11-01T14:30:00"
}
```

失败响应：
```json
{
  "code": 500,
  "message": "订单创建失败: 用户不存在",
  "data": null,
  "timestamp": "2025-11-01T14:30:00"
}
```

**业务逻辑**:
1. 验证用户是否存在（调用UserService）
2. 验证地址是否存在（调用UserService）
3. 验证团长是否存在（调用LeaderService）
4. 生成订单编号（yyyyMMddHHmmss + 6位随机数）
5. 创建订单主表（order_main）
6. 创建订单明细（order_item，保存商品快照）
7. 返回订单ID

**错误码**:
- 500: 用户不存在
- 500: 收货地址不存在
- 500: 团长不存在
- 500: 数据库操作失败

---

### 1.2 批量更新订单状态 ⭐⭐⭐⭐⭐

```http
POST /api/order/feign/batchUpdateStatus
Content-Type: application/json
```

**功能**: 成团后批量更新所有成员订单状态

**调用方**: GroupBuyService 成团逻辑

**使用场景**:
- 拼团成功后，批量更新所有成员订单状态为"待发货"

**请求参数**:
```json
{
  "orderIds": [1, 2, 3],
  "status": 1
}
```

**URL参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| status | Integer | 是 | 订单状态（1-待发货） |

**Body参数**:

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| orderIds | List<Long> | 是 | 订单ID列表 |

**订单状态枚举**:
- 0: 待支付
- 1: 待发货
- 2: 配送中
- 3: 已送达
- 4: 已取消
- 5: 退款中
- 6: 已退款

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "订单状态更新成功",
  "data": null,
  "timestamp": "2025-11-01T14:35:00"
}
```

失败响应：
```json
{
  "code": 500,
  "message": "订单状态更新失败: 订单不存在",
  "data": null,
  "timestamp": "2025-11-01T14:35:00"
}
```

**业务逻辑**:
1. 根据订单ID列表查询所有订单
2. 批量更新订单状态
3. 更新update_time字段
4. 保存到数据库

---

### 1.3 更新单个订单状态 ⭐⭐⭐⭐

```http
POST /api/order/feign/updateStatus?orderId=1&status=6
```

**功能**: 更新单个订单状态

**调用方**: GroupBuyService 退款逻辑

**使用场景**:
- 退款时更新订单状态为"已退款"（状态码6）

**URL参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| orderId | Long | 是 | 订单ID |
| status | Integer | 是 | 订单状态（6-已退款） |

**响应示例**:

```json
{
  "code": 200,
  "message": "订单状态更新成功",
  "data": null,
  "timestamp": "2025-11-01T14:40:00"
}
```

---

### 1.4 取消订单 ⭐⭐⭐⭐

```http
POST /api/order/feign/cancel/{orderId}
```

**功能**: 取消订单

**调用方**: GroupBuyService 退团逻辑

**使用场景**:
- 用户主动退团时
- 拼团失败时自动取消订单

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| orderId | Long | 是 | 订单ID |

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "订单取消成功",
  "data": null,
  "timestamp": "2025-11-01T14:45:00"
}
```

失败响应：
```json
{
  "code": 500,
  "message": "订单取消失败: 订单已发货，无法取消",
  "data": null,
  "timestamp": "2025-11-01T14:45:00"
}
```

**业务逻辑**:
1. 查询订单
2. 检查订单状态（只有待支付、待发货可取消）
3. 更新订单状态为"已取消"（状态码4）
4. 保存到数据库

**限制条件**:
- 只有待支付（0）和待发货（1）的订单可以取消
- 配送中（2）及以后状态的订单不能取消

---

### 1.5 查询订单详情 ⭐⭐⭐

```http
GET /api/order/feign/{orderId}
```

**功能**: 查询订单详细信息

**调用方**: 其他微服务（需要订单信息时）

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| orderId | Long | 是 | 订单ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": 1,
    "orderSn": "20251101143000123456",
    "totalAmount": 14.00,
    "discountAmount": 0.00,
    "payAmount": 14.00,
    "orderStatus": 0,
    "orderStatusText": "待支付",
    "payStatus": 0,
    "leaderId": 3,
    "leaderName": null,
    "receiveAddressId": 2,
    "receiveAddress": null,
    "items": [
      {
        "itemId": 1,
        "productId": 1,
        "productName": "测试商品：牙膏",
        "productImg": "http://localhost:8062/uploads/product/20251031192059_512856.jpg",
        "price": 14.00,
        "quantity": 1,
        "totalPrice": 14.00,
        "activityId": 1
      }
    ],
    "createTime": "2025-11-01T14:30:00",
    "payTime": null,
    "updateTime": null
  },
  "timestamp": "2025-11-01T14:50:00"
}
```

---

## 2. 用户端订单接口（供前端调用）

### 2.1 查询我的订单

```http
GET /api/order/my?page=0&size=10
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 分页查询当前用户的订单列表

**认证**: 需要JWT Token

**URL参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|------|--------|------|
| page | Integer | 否 | 0 | 页码（从0开始） |
| size | Integer | 否 | 10 | 每页数量 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 15,
    "list": [
      {
        "orderId": 3,
        "orderSn": "20251101150000789012",
        "payAmount": 28.00,
        "orderStatus": 1,
        "orderStatusText": "待发货",
        "payStatus": 1,
        "items": [
          {
            "itemId": 3,
            "productId": 2,
            "productName": "有机蔬菜",
            "productImg": "http://localhost:8062/uploads/product/example.jpg",
            "price": 28.00,
            "quantity": 1,
            "totalPrice": 28.00,
            "activityId": null
          }
        ],
        "createTime": "2025-11-01T15:00:00",
        "payTime": "2025-11-01T15:05:00"
      },
      {
        "orderId": 2,
        "orderSn": "20251101143000456789",
        "payAmount": 14.00,
        "orderStatus": 0,
        "orderStatusText": "待支付",
        "payStatus": 0,
        "items": [
          {
            "itemId": 2,
            "productId": 1,
            "productName": "测试商品：牙膏",
            "productImg": "http://localhost:8062/uploads/product/20251031192059_512856.jpg",
            "price": 14.00,
            "quantity": 1,
            "totalPrice": 14.00,
            "activityId": 1
          }
        ],
        "createTime": "2025-11-01T14:30:00",
        "payTime": null
      }
    ]
  },
  "timestamp": "2025-11-01T15:10:00"
}
```

**数据说明**:
- 订单按创建时间倒序排列（最新的在前）
- 每个订单包含订单商品列表
- 支持分页查询

---

### 2.2 查询订单详情

```http
GET /api/order/{orderId}
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 查询单个订单的详细信息

**认证**: 需要JWT Token

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| orderId | Long | 是 | 订单ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": 1,
    "orderSn": "20251101143000123456",
    "totalAmount": 14.00,
    "discountAmount": 0.00,
    "payAmount": 14.00,
    "orderStatus": 0,
    "orderStatusText": "待支付",
    "payStatus": 0,
    "leaderId": 3,
    "leaderName": null,
    "receiveAddressId": 2,
    "receiveAddress": null,
    "items": [
      {
        "itemId": 1,
        "productId": 1,
        "productName": "测试商品：牙膏",
        "productImg": "http://localhost:8062/uploads/product/20251031192059_512856.jpg",
        "price": 14.00,
        "quantity": 1,
        "totalPrice": 14.00,
        "activityId": 1
      }
    ],
    "createTime": "2025-11-01T14:30:00",
    "payTime": null,
    "updateTime": null
  },
  "timestamp": "2025-11-01T15:15:00"
}
```

**错误码**:
- 404: 订单不存在
- 403: 无权访问该订单（非本人订单）

---

### 2.3 取消订单

```http
POST /api/order/cancel/{orderId}
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 用户主动取消订单

**认证**: 需要JWT Token

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| orderId | Long | 是 | 订单ID |

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "订单已取消",
  "data": null,
  "timestamp": "2025-11-01T15:20:00"
}
```

失败响应：
```json
{
  "code": 500,
  "message": "订单已发货，无法取消",
  "data": null,
  "timestamp": "2025-11-01T15:20:00"
}
```

**限制条件**:
- 只能取消自己的订单
- 只有待支付（0）和待发货（1）的订单可以取消
- 配送中（2）及以后状态的订单不能取消

---

### 2.4 确认收货

```http
POST /api/order/confirm/{orderId}
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 用户确认收货

**认证**: 需要JWT Token

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| orderId | Long | 是 | 订单ID |

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "确认收货成功",
  "data": null,
  "timestamp": "2025-11-01T15:25:00"
}
```

**业务逻辑**:
1. 验证订单归属（只能确认自己的订单）
2. 更新订单状态为"已送达"（状态码3）
3. 保存到数据库

---

## 3. 团长端订单接口（供团长查询） ⭐ **新增（2025-11-13）**

### 3.1 查询团长订单列表 ⭐⭐⭐⭐⭐

```http
GET /api/order/leader/my?leaderId=1&page=0&size=10&orderStatus=1
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 团长查询自己负责的所有订单

**认证**: 需要JWT Token

**URL参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|------|--------|------|
| leaderId | Long | 是 | - | 团长ID |
| page | Integer | 否 | 0 | 页码（从0开始） |
| size | Integer | 否 | 10 | 每页数量 |
| orderStatus | Integer | 否 | null | 订单状态筛选（可选） |

**订单状态说明**:

| 状态码 | 状态名称 | 说明 |
|--------|---------|------|
| 0 | 待支付 | 订单创建，等待支付 |
| 1 | 待发货 | 已支付，等待团长发货 |
| 2 | 配送中 | 团长已发货，正在配送 |
| 3 | 已送达 | 用户已确认收货 |
| 4 | 已取消 | 用户或系统取消订单 |
| 5 | 退款中 | 正在处理退款 |
| 6 | 已退款 | 退款完成 |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 25,
    "list": [
      {
        "orderId": 1001,
        "orderSn": "20251113142530123456",
        "userId": 4,
        "leaderId": 1,
        "totalAmount": 42.00,
        "payAmount": 42.00,
        "orderStatus": 1,
        "orderStatusText": "待发货",
        "payStatus": 1,
        "createTime": "2025-11-13T14:25:30",
        "items": [
          {
            "productId": 1,
            "productName": "牙膏",
            "productImg": "http://...",
            "quantity": 3,
            "price": 14.00,
            "subtotal": 42.00
          }
        ]
      }
    ]
  }
}
```

**业务逻辑**:
1. 根据leaderId查询该团长负责的所有订单
2. 支持按orderStatus筛选（待发货/配送中/已送达）
3. 支持分页查询
4. 返回订单列表（包含订单明细）

**使用场景**:
- 团长查看所有需要发货的订单
- 团长查看配送中的订单
- 团长统计订单数量

---

### 3.2 查询团长订单统计

```http
GET /api/order/leader/summary?leaderId={leaderId}
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 统计团长订单数量和金额

**认证**: 需要JWT Token

**查询参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| leaderId | Long | 是 | 团长ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalCount": 156,
    "todayCount": 8,
    "pendingCount": 12,
    "deliveringCount": 5
  },
  "timestamp": "2025-11-13T15:00:00"
}
```

**业务逻辑**:
1. 统计团长的订单总数
2. 统计今日订单数量（当天0点到现在）
3. 统计待发货订单数
4. 统计配送中订单数

**使用场景**:
- 团长工作台首页显示统计卡片
- 快速了解订单处理情况

---

## 4. 管理端订单接口（供管理员调用）

### 4.1 获取订单列表（分页）⭐⭐⭐⭐⭐

```http
GET /api/order/admin/list?page=0&size=10&status=1&payStatus=1
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**功能**: 管理员分页查询所有订单，支持按订单状态和支付状态过滤

**认证**: 需要管理员JWT Token

**URL参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|------|--------|------|
| page | Integer | 否 | 0 | 页码（从0开始） |
| size | Integer | 否 | 10 | 每页数量 |
| status | Integer | 否 | null | 订单状态（可选） |
| payStatus | Integer | 否 | null | 支付状态（可选） |

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "pageNum": 0,
    "pageSize": 10,
    "total": 50,
    "pages": 5,
    "list": [
      {
        "orderId": 5,
        "orderSn": "20251101160000123456",
        "payAmount": 28.00,
        "orderStatus": 1,
        "orderStatusText": "待发货",
        "payStatus": 1,
        "items": [...],
        "createTime": "2025-11-01T16:00:00",
        "payTime": "2025-11-01T16:05:00"
      }
    ]
  },
  "timestamp": "2025-11-01T16:10:00"
}
```

---

### 3.2 获取订单统计 ⭐⭐⭐⭐⭐

```http
GET /api/order/admin/statistics
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**功能**: 获取各状态订单数量统计、今日订单、销售额等

**认证**: 需要管理员JWT Token

**响应示例**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalOrders": 100,
    "pendingPayment": 10,
    "pendingDelivery": 15,
    "inDelivery": 20,
    "delivered": 50,
    "cancelled": 3,
    "refunding": 1,
    "refunded": 1,
    "todayOrders": 5,
    "todaySales": 500.00,
    "totalSales": 15000.00
  },
  "timestamp": "2025-11-01T16:15:00"
}
```

**数据说明**:

| 字段 | 类型 | 说明 |
|-----|------|------|
| totalOrders | Long | 总订单数 |
| pendingPayment | Long | 待支付订单数（状态0） |
| pendingDelivery | Long | 待发货订单数（状态1） |
| inDelivery | Long | 配送中订单数（状态2） |
| delivered | Long | 已送达订单数（状态3） |
| cancelled | Long | 已取消订单数（状态4） |
| refunding | Long | 退款中订单数（状态5） |
| refunded | Long | 已退款订单数（状态6） |
| todayOrders | Long | 今日订单数 |
| todaySales | BigDecimal | 今日销售额（已支付） |
| totalSales | BigDecimal | 总销售额（已支付） |

---

### 3.3 更新订单状态 ⭐⭐⭐⭐

```http
PUT /api/order/admin/status/{orderId}?status=1
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**功能**: 管理员更新单个订单状态

**认证**: 需要管理员JWT Token

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| orderId | Long | 是 | 订单ID |

**URL参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| status | Integer | 是 | 订单状态（0-6） |

**响应示例**:

```json
{
  "code": 200,
  "message": "订单状态更新成功",
  "data": null,
  "timestamp": "2025-11-01T16:20:00"
}
```

---

### 3.4 批量更新订单状态 ⭐⭐⭐⭐

```http
POST /api/order/admin/batchUpdateStatus?status=1
Authorization: Bearer {ADMIN_JWT_TOKEN}
Content-Type: application/json
```

**功能**: 批量更新多个订单状态

**认证**: 需要管理员JWT Token

**URL参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| status | Integer | 是 | 订单状态（0-6） |

**请求参数**:

```json
[1, 2, 3, 4, 5]
```

**响应示例**:

```json
{
  "code": 200,
  "message": "批量更新成功",
  "data": null,
  "timestamp": "2025-11-01T16:25:00"
}
```

---

### 3.5 按状态查询订单 ⭐⭐⭐⭐

```http
GET /api/order/admin/status/{status}?page=0&size=10
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**功能**: 查询指定状态的订单列表

**认证**: 需要管理员JWT Token

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| status | Integer | 是 | 订单状态（0-6） |

**URL参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|------|--------|------|
| page | Integer | 否 | 0 | 页码（从0开始） |
| size | Integer | 否 | 10 | 每页数量 |

**响应示例**: 同3.1

---

### 3.6 搜索订单 ⭐⭐⭐⭐

```http
GET /api/order/admin/search?keyword=20251101&page=0&size=10
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**功能**: 根据订单号搜索订单

**认证**: 需要管理员JWT Token

**URL参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|------|--------|------|
| keyword | String | 是 | - | 搜索关键词（订单号） |
| page | Integer | 否 | 0 | 页码（从0开始） |
| size | Integer | 否 | 10 | 每页数量 |

**响应示例**: 同3.1

---

### 3.7 导出订单 ⭐⭐⭐

```http
GET /api/order/admin/export?status=1&payStatus=1
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**功能**: 导出订单数据为CSV文件

**认证**: 需要管理员JWT Token

**URL参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| status | Integer | 否 | 订单状态（可选） |
| payStatus | Integer | 否 | 支付状态（可选） |
| startDate | String | 否 | 开始日期（可选，格式：yyyy-MM-dd） |
| endDate | String | 否 | 结束日期（可选，格式：yyyy-MM-dd） |

**响应**: CSV文件流

**文件格式**:
```csv
订单ID,订单编号,用户ID,总金额,订单状态,创建时间
1,20251101143000123456,4,14.00,0,2025-11-01 14:30:00
2,20251101150000789012,5,28.00,1,2025-11-01 15:00:00
```

**注意**: 当前为简化版实现（CSV格式），生产环境建议使用EasyExcel生成Excel文件。

---

### 3.8 获取用户订单 ⭐⭐⭐

```http
GET /api/order/admin/user/{userId}?page=0&size=10
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**功能**: 查询指定用户的订单列表

**认证**: 需要管理员JWT Token

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| userId | Long | 是 | 用户ID |

**URL参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|------|--------|------|
| page | Integer | 否 | 0 | 页码（从0开始） |
| size | Integer | 否 | 10 | 每页数量 |

**响应示例**: 同3.1

---

### 3.9 获取团长订单 ⭐⭐⭐

```http
GET /api/order/admin/leader/{leaderId}?page=0&size=10
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**功能**: 查询指定团长的订单列表

**认证**: 需要管理员JWT Token

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| leaderId | Long | 是 | 团长ID |

**URL参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|-----|------|------|--------|------|
| page | Integer | 否 | 0 | 页码（从0开始） |
| size | Integer | 否 | 10 | 每页数量 |

**响应示例**: 同3.1

---

### 3.10 管理员查询订单详情

```http
GET /api/order/{orderId}
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**功能**: 查询订单详细信息（复用用户端接口）

**详见**: 2.2 查询订单详情

---

### 3.11 管理员取消订单

```http
POST /api/order/cancel/{orderId}
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

**功能**: 取消订单（复用用户端接口）

**详见**: 2.3 取消订单

---

## 4. 数据模型

### 4.1 CreateOrderRequest（创建订单请求）

```json
{
  "userId": "Long - 用户ID",
  "leaderId": "Long - 团长ID",
  "addressId": "Long - 收货地址ID",
  "productId": "Long - 商品ID",
  "productName": "String - 商品名称（快照）",
  "productImg": "String - 商品图片（快照）",
  "quantity": "Integer - 购买数量",
  "price": "BigDecimal - 购买单价",
  "activityId": "Long - 拼团活动ID（可选）"
}
```

### 4.2 OrderDetailVO（订单详情响应）

```json
{
  "orderId": "Long - 订单ID",
  "orderSn": "String - 订单编号",
  "totalAmount": "BigDecimal - 商品总金额",
  "discountAmount": "BigDecimal - 优惠金额",
  "payAmount": "BigDecimal - 实付金额",
  "orderStatus": "Integer - 订单状态",
  "orderStatusText": "String - 订单状态文本",
  "payStatus": "Integer - 支付状态",
  "leaderId": "Long - 团长ID",
  "leaderName": "String - 团长名称",
  "receiveAddressId": "Long - 收货地址ID",
  "receiveAddress": "String - 收货地址详情",
  "items": "List<OrderItemVO> - 订单商品列表",
  "createTime": "LocalDateTime - 创建时间",
  "payTime": "LocalDateTime - 支付时间",
  "updateTime": "LocalDateTime - 更新时间"
}
```

### 4.3 OrderVO（订单列表项）

```json
{
  "orderId": "Long - 订单ID",
  "orderSn": "String - 订单编号",
  "payAmount": "BigDecimal - 实付金额",
  "orderStatus": "Integer - 订单状态",
  "orderStatusText": "String - 订单状态文本",
  "payStatus": "Integer - 支付状态",
  "items": "List<OrderItemVO> - 订单商品列表",
  "createTime": "LocalDateTime - 创建时间",
  "payTime": "LocalDateTime - 支付时间"
}
```

### 4.4 OrderItemVO（订单商品项）

```json
{
  "itemId": "Long - 订单项ID",
  "productId": "Long - 商品ID",
  "productName": "String - 商品名称",
  "productImg": "String - 商品图片",
  "price": "BigDecimal - 购买单价",
  "quantity": "Integer - 购买数量",
  "totalPrice": "BigDecimal - 小计金额",
  "activityId": "Long - 拼团活动ID（可选）"
}
```

### 4.5 OrderStatisticsVO（订单统计数据）

```json
{
  "totalOrders": "Long - 总订单数",
  "pendingPayment": "Long - 待支付订单数",
  "pendingDelivery": "Long - 待发货订单数",
  "inDelivery": "Long - 配送中订单数",
  "delivered": "Long - 已送达订单数",
  "cancelled": "Long - 已取消订单数",
  "refunding": "Long - 退款中订单数",
  "refunded": "Long - 已退款订单数",
  "todayOrders": "Long - 今日订单数",
  "todaySales": "BigDecimal - 今日销售额",
  "totalSales": "BigDecimal - 总销售额"
}
```

---

## 5. 订单状态枚举

### 5.1 订单状态（OrderStatus）

| 状态码 | 状态名称 | 说明 | 可执行操作 |
|--------|---------|------|-----------|
| 0 | 待支付 | 订单已创建，等待支付 | 取消订单、支付 |
| 1 | 待发货 | 已支付，等待商家发货 | 取消订单 |
| 2 | 配送中 | 商品配送中 | 确认收货 |
| 3 | 已送达 | 用户已确认收货 | - |
| 4 | 已取消 | 订单已取消 | - |
| 5 | 退款中 | 退款处理中 | - |
| 6 | 已退款 | 退款完成 | - |

### 5.2 支付状态（PayStatus）

| 状态码 | 状态名称 | 说明 |
|--------|---------|------|
| 0 | 未支付 | 订单未支付 |
| 1 | 已支付 | 订单已支付 |

---

## 6. 订单状态流转图

```
                  创建订单（参团）
                       ↓
              ┌────【待支付】(0)────┐
              │         ↓          │
              │    支付成功         │ 超时取消（30分钟）
              │         ↓          │ 或用户取消
              │    【待发货】(1)────┤
              │         ↓          │
              │      商家发货       │
              │         ↓          │
              │    【配送中】(2)    │
              │         ↓          │
              │    用户确认收货     │
              │         ↓          │
              │    【已送达】(3)    │
              │                    │
              │                    ↓
              └────────────→【已取消】(4)
                           
                           【退款中】(5)
                                ↓
                           【已退款】(6)
```

---

## 7. 错误码说明

### 7.1 通用错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|---------|
| 200 | 成功 | - |
| 400 | 请求参数错误 | 检查请求参数格式 |
| 401 | 未认证 | 需要登录或Token无效 |
| 403 | 无权访问 | 无权操作该资源 |
| 404 | 资源不存在 | 订单不存在 |
| 500 | 服务器内部错误 | 查看服务器日志 |

### 7.2 业务错误码

| 错误信息 | 说明 | 原因 |
|---------|------|------|
| 用户不存在 | 创建订单失败 | UserService验证失败 |
| 收货地址不存在 | 创建订单失败 | 地址ID无效 |
| 团长不存在 | 创建订单失败 | 团长ID无效 |
| 订单不存在 | 查询/操作订单失败 | 订单ID无效 |
| 订单已发货，无法取消 | 取消订单失败 | 订单状态不允许取消 |

---

## 8. 调用示例

### 8.1 Feign客户端调用（Java）

```java
@Service
public class GroupBuyService {
    
    @Autowired
    private OrderServiceClient orderServiceClient;
    
    public void joinTeam(JoinTeamRequest request) {
        // 构建创建订单请求
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setUserId(request.getUserId());
        orderRequest.setLeaderId(team.getLeaderId());
        orderRequest.setAddressId(request.getAddressId());
        orderRequest.setProductId(activity.getProductId());
        orderRequest.setProductName(product.getProductName());
        orderRequest.setProductImg(product.getCoverImg());
        orderRequest.setQuantity(request.getQuantity());
        orderRequest.setPrice(activity.getGroupPrice());
        orderRequest.setActivityId(activity.getActivityId());
        
        // 调用OrderService创建订单
        Result<Long> result = orderServiceClient.createOrder(orderRequest);
        
        if (result.getCode() == 200) {
            Long orderId = result.getData();
            log.info("订单创建成功: orderId={}", orderId);
            // 保存订单ID到拼团成员表
            member.setOrderId(orderId);
        } else {
            throw new BusinessException("订单创建失败: " + result.getMessage());
        }
    }
}
```

### 8.2 前端调用（JavaScript）

```javascript
// 查询我的订单
import request from '@/utils/request'

export function getMyOrders(page, size) {
  return request({
    url: '/api/order/my',
    method: 'get',
    params: { page, size }
  })
}

// 查询订单详情
export function getOrderDetail(orderId) {
  return request({
    url: `/api/order/${orderId}`,
    method: 'get'
  })
}

// 取消订单
export function cancelOrder(orderId) {
  return request({
    url: `/api/order/cancel/${orderId}`,
    method: 'post'
  })
}

// 确认收货
export function confirmReceipt(orderId) {
  return request({
    url: `/api/order/confirm/${orderId}`,
    method: 'post'
  })
}
```

### 8.3 cURL调用示例

```bash
# 1. 创建订单（Feign接口）
curl -X POST http://localhost:8065/api/order/feign/create \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 4,
    "leaderId": 3,
    "addressId": 2,
    "productId": 1,
    "productName": "测试商品：牙膏",
    "productImg": "http://localhost:8062/uploads/product/20251031192059_512856.jpg",
    "quantity": 1,
    "price": 14.00,
    "activityId": 1
  }'

# 2. 查询我的订单（需要Token）
curl -X GET "http://localhost:9000/api/order/my?page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 3. 取消订单（需要Token）
curl -X POST http://localhost:9000/api/order/cancel/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 9. 快照设计说明

### 9.1 为什么需要快照？

**问题场景**:
- 用户下单时商品价格是14元
- 一周后商家调整价格为20元
- 如果订单直接关联商品表，历史订单会显示20元（错误！）

**快照解决方案**:
- 订单明细表保存下单时的商品信息（product_name, product_img, price）
- 即使商品信息变更，历史订单仍显示下单时的信息

### 9.2 快照字段

| 字段 | 说明 | 为什么需要 |
|-----|------|-----------|
| product_name | 商品名称快照 | 商品可能改名 |
| product_img | 商品图片快照 | 图片可能更新 |
| price | 购买单价快照 | 价格可能调整 |

### 9.3 数据库设计

```sql
CREATE TABLE `order_item` (
  `item_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL COMMENT '商品ID（用于统计销量）',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称（下单时快照）',
  `product_img` varchar(255) COMMENT '商品图片（下单时快照）',
  `price` decimal(10, 2) NOT NULL COMMENT '购买单价（拼团价/原价）',
  `quantity` int NOT NULL COMMENT '购买数量',
  `total_price` decimal(10, 2) NOT NULL COMMENT '小计金额',
  PRIMARY KEY (`item_id`)
);
```

---

## 10. 定时任务说明

### 10.1 订单超时取消任务

**功能**: 自动取消超时未支付订单

**执行频率**: 每5分钟执行一次

**超时时间**: 30分钟（可配置）

**Cron表达式**: `0 */5 * * * ?`

**配置文件**:
```yaml
# application.yml
order:
  expire:
    minutes: 30  # 订单超时时间（分钟）
```

**执行逻辑**:
1. 计算过期时间点（当前时间 - 30分钟）
2. 查询超时订单（order_status=0 且 pay_status=0 且 create_time < 过期时间点）
3. 批量取消超时订单
4. 记录执行日志

**日志示例**:
```
2025-11-01 14:00:00 ========== 开始检查超时订单 ==========
2025-11-01 14:00:00 超时时间设置: 30分钟
2025-11-01 14:00:00 过期时间点: 2025-11-01T13:30:00
2025-11-01 14:00:00 找到3条超时订单
2025-11-01 14:00:01 取消超时订单: orderId=1, orderSn=20251101130000123456
2025-11-01 14:00:01 订单20251101130000123456取消成功
2025-11-01 14:00:02 ========== 超时订单处理完成 ==========
2025-11-01 14:00:02 处理结果: 成功3条, 失败0条
```

---

## 11. 性能优化建议

### 11.1 数据库索引

```sql
-- order_main表
CREATE INDEX idx_user_id ON order_main(user_id);
CREATE INDEX idx_leader_id ON order_main(leader_id);
CREATE INDEX idx_order_status ON order_main(order_status);
CREATE INDEX idx_pay_status ON order_main(pay_status);
CREATE UNIQUE INDEX uk_order_sn ON order_main(order_sn);

-- order_item表
CREATE INDEX idx_order_id ON order_item(order_id);
CREATE INDEX idx_product_id ON order_item(product_id);
CREATE INDEX idx_activity_id ON order_item(activity_id);
```

### 11.2 查询优化

- 订单列表查询使用分页（避免一次查询过多数据）
- 批量更新订单状态（减少数据库交互次数）
- 使用JPA审计功能（自动填充create_time和update_time）

### 11.3 Feign超时配置

```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000  # 连接超时5秒
        readTimeout: 5000     # 读取超时5秒
```

---

## 12. 常见问题（FAQ）

### Q1: 为什么创建订单接口返回500？

**A**: 检查以下几点：
1. OrderService是否启动并注册到Consul
2. UserService、ProductService、LeaderService是否正常运行
3. 数据库连接是否正常
4. 请求参数是否正确（userId、addressId、leaderId是否有效）

### Q2: 为什么批量更新订单状态失败？

**A**: 检查：
1. 订单ID列表是否为空
2. 订单是否存在
3. 数据库连接是否正常

### Q3: 如何自定义订单超时时间？

**A**: 修改`application.yml`：
```yaml
order:
  expire:
    minutes: 60  # 改为60分钟
```

### Q4: 订单状态流转规则是什么？

**A**: 
- 待支付(0) → 支付后 → 待发货(1)
- 待发货(1) → 发货后 → 配送中(2)
- 配送中(2) → 确认收货 → 已送达(3)
- 任意状态(0,1) → 取消 → 已取消(4)
- 已支付 → 退款 → 退款中(5) → 已退款(6)

### Q5: 如何通过Gateway访问OrderService？

**A**: 
- 直接访问: `http://localhost:8065/api/order/my`
- 通过Gateway: `http://localhost:9000/api/order/my`（推荐）

### Q6: 管理端接口需要特殊权限吗？

**A**: 
- 是的，所有`/api/order/admin/*`接口都需要管理员JWT Token
- Gateway应该配置管理员角色检查
- 建议在Controller中使用`@PreAuthorize("hasRole('ADMIN')")`注解

### Q7: 为什么导出订单是CSV格式而不是Excel？

**A**: 
- 当前为简化版实现（CSV格式）
- 生产环境建议使用EasyExcel或Apache POI生成Excel文件
- CSV格式优点：实现简单、文件小、Excel可以直接打开

### Q8: 订单统计数据会实时更新吗？

**A**: 
- 是的，当前实时查询数据库
- 如果数据量大，建议使用Redis缓存（5分钟刷新一次）
- 可以使用`@Cacheable`注解实现缓存

---

## 13. 联系方式

**开发者**: 耿康瑞  
**学号**: 20221204229  
**开发日期**: 2025-11-01  
**Swagger文档**: http://localhost:8065/swagger-ui.html

---

## 14. 更新日志

### v1.1.0 (2025-11-13) ⭐⭐⭐⭐⭐

**新增功能**:
- ✅ **新增团长端订单接口（2个）**
  - `GET /api/order/leader/my` - 查询团长订单列表 ⭐⭐⭐⭐⭐
  - `GET /api/order/leader/summary` - 查询团长订单统计 ⭐⭐⭐⭐
- ✅ 支持按订单状态筛选（待发货/配送中/已送达）
- ✅ 支持分页查询
- ✅ 统计今日订单、待发货、配送中订单数

**实现细节**:
- 新增Repository方法（3个）：
  - `findByLeaderIdAndOrderStatusOrderByCreateTimeDesc()` - 按状态查询
  - `countByLeaderIdAndOrderStatus()` - 统计指定状态订单数
  - `countByLeaderIdAndCreateTimeGreaterThanEqual()` - 统计今日订单数
- 新增Service方法（2个）：
  - `OrderService.getLeaderOrders()` - 查询团长订单列表
  - `OrderService.getLeaderOrdersSummary()` - 查询团长订单统计
- 新增Controller接口（2个）：
  - `OrderController.getLeaderOrders()` - 团长订单列表接口
  - `OrderController.getLeaderOrdersSummary()` - 团长订单统计接口

**业务价值**:
- 团长可查看自己负责的所有订单 ✅
- 团长可按状态筛选订单（待发货/配送中） ✅
- 团长可查看订单统计数据 ✅
- 支持团长工作台功能完整闭环 ✅

**使用场景**:
- 团长工作台首页显示订单统计卡片
- 团长查看待发货订单列表
- 团长查看配送中订单列表
- 团长统计订单处理情况

---

### v1.0.0 (2025-11-01)

**初始版本**:
- ✅ Feign内部接口（5个）
- ✅ 用户端订单接口（4个）
- ✅ 管理端订单接口（11个）
- ✅ 订单超时自动取消定时任务
- ✅ 完整的订单CRUD功能

---

**当前版本**: ✅ OrderService v1.1.0  
**状态**: 已完成并部署  
**最后更新**: 2025-11-13  
**Swagger文档**: http://localhost:8065/swagger-ui.html

