# DeliveryService API文档

**版本**: v1.0.0  
**服务端口**: `8067`  
**基础路径**: `http://localhost:8067`  
**通过网关访问**: `http://localhost:9000`  
**Swagger文档**: `http://localhost:8067/swagger-ui.html`  
**创建日期**: 2025-11-15

---

## 📋 接口概览

| 模块 | 接口数 | 路径前缀 | 说明 |
|------|--------|---------|------|
| 批量发货 | 1个 | `/api/delivery/batch` | 🔴 核心功能 |
| 配送管理 | 5个 | `/api/delivery` | 🟡 配送单管理 |
| 仓库管理 | 7个 | `/api/delivery/warehouse` | 🟢 仓库配置 |
| 配送统计 | 4个 | `/api/delivery/statistics` | 🟣 数据统计 |
| **总计** | **17个** | - | - |

---

## 1. 批量发货接口（核心功能）⭐⭐⭐⭐⭐

### 1.1 批量发货

```http
POST /api/delivery/batch/ship
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 管理员批量发货，生成配送单并更新订单状态

**请求参数**:
```json
{
  "orderIds": [1001, 1002, 1003],
  "deliveryMode": 1,
  "warehouseId": 1,
  "endWarehouseId": null,
  "routeStrategy": 1,
  "createdBy": 1
}
```

**参数说明**:

| 字段 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| orderIds | List<Long> | 是 | 订单ID列表 |
| deliveryMode | Integer | 是 | 发货方式：1-团长团点；2-用户地址 |
| warehouseId | Long | 是 | 起点仓库ID |
| endWarehouseId | Long | 否 | 终点仓库ID（可选） |
| routeStrategy | Integer | 否 | 路径策略：1-最短距离（默认） |
| createdBy | Long | 否 | 创建人ID（管理员ID） |

**响应示例**:

成功响应：
```json
{
  "code": 200,
  "message": "批量发货成功",
  "data": {
    "deliveryId": 1,
    "dispatchGroup": "SHIP20251115143025123",
    "orderIds": [1001, 1002, 1003],
    "deliveryMode": 1,
    "waypointCount": 3,
    "totalDistance": 15300.50,
    "estimatedDuration": 45,
    "waypoints": [
      {
        "sequence": 0,
        "orderId": 1001,
        "address": "北京市朝阳区建国路88号",
        "longitude": 116.407400,
        "latitude": 39.904200,
        "receiverName": "张团长",
        "receiverPhone": "138****0001",
        "type": "leader_store"
      }
    ],
    "algorithmUsed": "dijkstra",
    "message": "批量发货成功，已生成配送单"
  },
  "timestamp": "2025-11-15T14:30:25"
}
```

失败响应（订单状态不正确）：
```json
{
  "code": 500,
  "message": "以下订单状态不是待发货，无法发货：[1001, 1002]",
  "data": null,
  "timestamp": "2025-11-15T14:30:25"
}
```

失败响应（途经点数量超限）：
```json
{
  "code": 500,
  "message": "途经点数量不能超过30个，当前35个，建议分批发货",
  "data": null,
  "timestamp": "2025-11-15T14:30:25"
}
```

**业务流程**:
```
1. 验证订单状态（必须为待发货）
2. 生成分单组标识
3. 提取途经点坐标（根据发货方式）
4. 调用Dijkstra算法计算最优路径
5. 创建配送单
6. 批量更新订单状态→配送中
7. 返回配送结果
```

---

## 2. 配送管理接口

### 2.1 查询配送单列表

```http
GET /api/delivery/list?status=1&page=0&size=10
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 分页查询配送单，支持按状态筛选

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| status | Integer | 否 | 配送状态：0-待分配；1-配送中；2-已完成 |
| page | Integer | 否 | 页码（从0开始），默认0 |
| size | Integer | 否 | 每页大小，默认10 |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 25,
    "pages": 3,
    "list": [
      {
        "deliveryId": 1,
        "dispatchGroup": "SHIP20251115143025123",
        "deliveryMode": 1,
        "warehouseId": 1,
        "waypointCount": 3,
        "distance": 15300.50,
        "estimatedDuration": 45,
        "status": 1,
        "algorithmUsed": "dijkstra",
        "createTime": "2025-11-15T14:30:25"
      }
    ]
  }
}
```

---

### 2.2 查询配送单详情

```http
GET /api/delivery/{id}
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 查询配送单完整信息，包含订单列表和路径信息

**路径参数**:
- `id`: 配送单ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deliveryId": 1,
    "dispatchGroup": "SHIP20251115143025123",
    "deliveryMode": 1,
    "status": 1,
    "startWarehouse": {
      "id": 1,
      "warehouseName": "中心仓库",
      "address": "北京市朝阳区示例地址",
      "longitude": 116.397128,
      "latitude": 39.916527
    },
    "endWarehouse": null,
    "waypoints": [
      {
        "sequence": 0,
        "orderId": 1001,
        "address": "北京市朝阳区建国路88号",
        "longitude": 116.407400,
        "latitude": 39.904200,
        "receiverName": "张团长",
        "receiverPhone": "138****0001",
        "type": "leader_store"
      }
    ],
    "orders": [
      {
        "orderId": 1001,
        "orderSn": "20251115143000123456",
        "userId": 4,
        "leaderId": 3,
        "orderStatus": 2,
        "payAmount": 28.00
      }
    ],
    "totalDistance": 15300.50,
    "estimatedDuration": 45,
    "algorithmUsed": "dijkstra",
    "routeStrategy": 1,
    "startTime": "2025-11-15T14:30:25",
    "endTime": null,
    "createTime": "2025-11-15T14:30:25"
  }
}
```

---

### 2.3 重新规划路径

```http
POST /api/delivery/{id}/replan
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 重新计算配送路径，更新配送单

**路径参数**:
- `id`: 配送单ID

**响应示例**:
```json
{
  "code": 200,
  "message": "路径重新规划成功",
  "data": {
    "pathSequence": [0, 2, 1, 3],
    "optimalRoute": "39.916527,116.397128;39.904200,116.407400;...",
    "totalDistance": 14800.20,
    "estimatedDuration": 42,
    "algorithmUsed": "dijkstra",
    "segments": [
      {
        "fromIndex": 0,
        "toIndex": 2,
        "distance": 5100.30,
        "duration": 15
      }
    ]
  }
}
```

---

### 2.4 手动完成配送

```http
POST /api/delivery/{id}/complete
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 手动标记配送完成，批量更新订单状态为"已送达"

**路径参数**:
- `id`: 配送单ID

**响应示例**:
```json
{
  "code": 200,
  "message": "配送完成",
  "data": null
}
```

**业务说明**:
- 更新配送单状态：1-配送中 → 2-已完成
- 记录完成时间
- Feign调用OrderService批量更新订单状态：2-配送中 → 3-已送达

---

### 2.5 取消配送

```http
POST /api/delivery/{id}/cancel
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 取消配送单，删除配送记录

**路径参数**:
- `id`: 配送单ID

**响应示例**:
```json
{
  "code": 200,
  "message": "配送已取消",
  "data": null
}
```

---

## 3. 仓库管理接口

### 3.1 查询仓库列表

```http
GET /api/delivery/warehouse/list
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 查询所有仓库（包含禁用的）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "warehouseName": "中心仓库",
      "address": "北京市朝阳区示例地址",
      "longitude": 116.397128,
      "latitude": 39.916527,
      "isDefault": 1,
      "status": 1,
      "contactPerson": "王管理",
      "contactPhone": "138****0000",
      "createTime": "2025-11-15T10:00:00"
    }
  ]
}
```

---

### 3.2 查询启用的仓库

```http
GET /api/delivery/warehouse/active
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 仅查询启用状态的仓库

---

### 3.3 查询默认仓库

```http
GET /api/delivery/warehouse/default
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 获取系统默认仓库配置

---

### 3.4 查询仓库详情

```http
GET /api/delivery/warehouse/{id}
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 根据ID查询仓库详细信息

---

### 3.5 创建仓库

```http
POST /api/delivery/warehouse
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**请求参数**:
```json
{
  "warehouseName": "东区仓库",
  "address": "北京市朝阳区东三环100号",
  "longitude": 116.450000,
  "latitude": 39.920000,
  "isDefault": 0,
  "status": 1,
  "contactPerson": "李主管",
  "contactPhone": "13900000000",
  "description": "东区配送仓库"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "仓库创建成功",
  "data": {
    "id": 2,
    "warehouseName": "东区仓库",
    "createTime": "2025-11-15T14:35:00"
  }
}
```

**业务规则**:
- 仓库名称不能重复
- 如果系统无默认仓库，自动设置为默认
- 如果设置为默认仓库，取消其他默认仓库

---

### 3.6 更新仓库

```http
PUT /api/delivery/warehouse/{id}
Content-Type: application/json
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 修改仓库配置信息

---

### 3.7 删除仓库

```http
DELETE /api/delivery/warehouse/{id}
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 删除仓库（默认仓库不能删除）

**业务规则**:
- 默认仓库不能删除
- 删除前需先设置其他仓库为默认

---

### 3.8 设置默认仓库

```http
PUT /api/delivery/warehouse/{id}/setDefault
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 将指定仓库设置为默认仓库

**业务规则**:
- 禁用的仓库不能设置为默认
- 设置后自动取消其他默认仓库

---

## 4. 配送统计接口

### 4.1 配送总览统计

```http
GET /api/delivery/statistics/overview
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 获取配送总量、距离、效率等统计数据

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalDeliveries": 150,
    "pendingDeliveries": 5,
    "shippingDeliveries": 10,
    "completedDeliveries": 135,
    "totalOrders": 0,
    "totalDistance": 285600.50,
    "totalDuration": 0,
    "averageDistance": 2116.30,
    "averageWaypointCount": 4.5
  }
}
```

---

### 4.2 距离统计

```http
GET /api/delivery/statistics/distance
  ?startTime=2025-11-01 00:00:00
  &endTime=2025-11-15 23:59:59
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 统计指定时间范围内的配送距离

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| startTime | String | 是 | 开始时间（yyyy-MM-dd HH:mm:ss） |
| endTime | String | 是 | 结束时间（yyyy-MM-dd HH:mm:ss） |

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "deliveryCount": 50,
    "totalDistance": 95600.80,
    "averageDistance": 1912.02,
    "maxDistance": 25800.50,
    "minDistance": 500.30
  }
}
```

---

### 4.3 效率统计

```http
GET /api/delivery/statistics/efficiency
  ?startTime=2025-11-01 00:00:00
  &endTime=2025-11-15 23:59:59
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 统计配送完成率、平均时间等效率指标

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "completionRate": 90.00,
    "averageEstimatedDuration": 38,
    "averageWaypointCount": 4.2
  }
}
```

---

### 4.4 团长配送统计

```http
GET /api/delivery/statistics/leader
Authorization: Bearer {JWT_TOKEN}
```

**功能**: 按团长统计配送单数量

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "leaderId": 3,
      "deliveryCount": 25
    },
    {
      "leaderId": 5,
      "deliveryCount": 18
    }
  ]
}
```

---

## 5. 数据结构

### DeliveryEntity（配送单实体）

```java
{
  "deliveryId": 1,              // 配送单ID
  "dispatchGroup": "SHIP...",   // 分单组标识
  "deliveryMode": 1,            // 发货方式
  "warehouseId": 1,             // 起点仓库
  "endWarehouseId": null,       // 终点仓库
  "waypointCount": 3,           // 途经点数量
  "orderIds": "[1001,1002]",    // 订单ID列表(JSON)
  "waypointsData": "[...]",     // 途经点数据(JSON)
  "optimalRoute": "lat,lng;...", // 最优路径
  "distance": 15300.50,         // 总距离(米)
  "estimatedDuration": 45,      // 预估时间(分钟)
  "status": 1,                  // 配送状态
  "routeStrategy": 1,           // 路径策略
  "algorithmUsed": "dijkstra",  // 使用算法
  "routeDisplayData": "{...}",  // 地图展示数据
  "startTime": "2025-11-15...", // 开始时间
  "endTime": null,              // 完成时间
  "actualStartTime": null,      // 实际开始时间
  "createdBy": 1,               // 创建人
  "createTime": "2025-11-15...", // 创建时间
  "updateTime": "2025-11-15..."  // 更新时间
}
```

### WarehouseConfig（仓库配置）

```java
{
  "id": 1,
  "warehouseName": "中心仓库",
  "address": "北京市朝阳区...",
  "longitude": 116.397128,
  "latitude": 39.916527,
  "isDefault": 1,              // 0-否；1-是
  "status": 1,                 // 0-禁用；1-启用
  "contactPerson": "王管理",
  "contactPhone": "13800000000",
  "description": "中心配送仓库",
  "createTime": "2025-11-15...",
  "updateTime": "2025-11-15..."
}
```

---

## 6. 核心算法

### 6.1 Dijkstra算法（TSP贪心求解）

**算法说明**:
- 基于最近邻贪心算法（Nearest Neighbor Algorithm）
- 时间复杂度：O(n²)，n为途经点数量
- 空间复杂度：O(n²)，距离矩阵

**算法步骤**:
```
1. 构建距离矩阵（使用Haversine公式）
2. 从起点开始
3. 每次选择距离当前点最近的未访问点
4. 标记为已访问
5. 重复3-4，直到所有点都被访问
6. 如果有终点，最后访问终点
7. 计算总距离和预估时间
```

**性能指标**:
- 10个途经点：≤20ms
- 30个途经点：≤100ms

**适用场景**:
- 途经点数量：≤30个
- 优化目标：最短距离
- 无外部依赖，稳定可靠

---

### 6.2 Haversine公式（距离计算）

**公式**:
```
a = sin²(Δφ/2) + cos(φ1) * cos(φ2) * sin²(Δλ/2)
c = 2 * atan2(√a, √(1−a))
d = R * c

其中：
- φ 表示纬度（latitude）
- λ 表示经度（longitude）
- R 表示地球半径（6371公里）
```

**精度**: 误差<1%，适合城市配送场景

---

## 7. 业务规则

### 7.1 发货方式

**方式1：团长团点模式**（推荐用于拼团订单）
- 途经点：团长团点坐标
- 去重：同一团长只访问一次
- 适用场景：货物送到团点，团长分发

**方式2：用户地址模式**（推荐用于紧急订单）
- 途经点：用户收货地址坐标
- 去重：同一地址访问一次
- 适用场景：货物直接配送到用户

### 7.2 途经点限制

- Dijkstra算法：建议≤30个点
- 超过限制：提示管理员分批发货
- 去重规则：团长团点模式按团长去重，用户地址模式按地址去重

### 7.3 订单状态流转

```
批量发货前：order_status = 1（待发货）
批量发货后：order_status = 2（配送中）
配送完成后：order_status = 3（已送达）
```

### 7.4 配送状态流转

```
创建配送单：status = 1（配送中）
手动完成：  status = 2（已完成）
```

---

## 8. 错误码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

**常见错误消息**:
- "订单ID列表不能为空"
- "发货方式不能为空"
- "起点仓库ID不能为空"
- "以下订单状态不是待发货，无法发货：[...]"
- "途经点数量不能超过30个，当前35个，建议分批发货"
- "仓库不存在，ID=xxx"
- "默认仓库不能删除，请先设置其他仓库为默认"

---

## 9. 微服务依赖

### DeliveryService调用的服务

| 服务 | 接口 | 用途 |
|-----|------|------|
| **OrderService** | `/api/order/feign/batchQuery` | 批量查询订单信息 |
| **OrderService** | `/api/order/feign/batchUpdateToShipping` | 批量更新订单状态→配送中 |
| **OrderService** | `/api/order/feign/batchUpdateToDelivered` | 批量更新订单状态→已送达 |
| **UserService** | `/api/user/feign/address/{addressId}` | 获取地址坐标 |
| **UserService** | `/api/user/feign/address/batch` | 批量获取地址信息 |
| **LeaderService** | `/api/leader/feign/store/{leaderId}` | 获取团长团点信息 |
| **LeaderService** | `/api/leader/feign/store/batch` | 批量获取团长团点信息 |

### 调用DeliveryService的服务

目前无其他服务调用DeliveryService（配送服务为终端服务）

---

## 10. 后续扩展

### 可选功能（不影响答辩）

1. **高德地图API集成**
   - 实时路况信息
   - 多种路径策略
   - 官方限制≤16个途经点

2. **配送时间窗口**
   - 用户指定配送时间
   - 系统自动安排批次

3. **司机管理**
   - 司机账号管理
   - 司机调度系统

4. **配送异常处理**
   - 用户拒收
   - 重新分配配送

---

## 11. 技术亮点

### 1. ⭐⭐⭐⭐⭐ Dijkstra算法实现
- TSP贪心求解
- Haversine距离计算
- 性能优秀：30个点<100ms

### 2. ⭐⭐⭐⭐ 双发货模式
- 团长团点模式
- 用户地址模式
- 灵活切换

### 3. ⭐⭐⭐⭐ 批量发货闭环
- 订单验证
- 路径规划
- 状态更新
- 完整事务

### 4. ⭐⭐⭐ 配送统计
- 总览统计
- 距离分析
- 效率分析
- 团长统计

---

**Swagger在线文档**: 启动服务后访问 http://localhost:8067/swagger-ui.html 查看完整的API文档。

**创建人**: 耿康瑞  
**学号**: 20221204229  
**创建日期**: 2025-11-15  
**状态**: ✅ DeliveryService开发完成

