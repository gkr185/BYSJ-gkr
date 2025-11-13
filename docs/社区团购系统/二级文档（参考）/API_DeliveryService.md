# 社区团购系统 - 配送服务API接口文档

**服务名称**: DeliveryService  
**服务端口**: 8067  
**Base URL**: `http://localhost:8067`  
**版本**: v1.0.0  
**文档日期**: 2025-11-13  
**最后更新**: 2025-11-13 22:15 ⭐ 双引擎路径规划系统

---

## 目录

1. [接口概述](#1-接口概述)
2. [认证方式](#2-认证方式)
3. [通用响应格式](#3-通用响应格式)
4. [错误码说明](#4-错误码说明)
5. [配送单管理接口](#5-配送单管理接口)
6. [路径规划接口](#6-路径规划接口)
7. [批量发货接口](#7-批量发货接口)
8. [仓库管理接口](#8-仓库管理接口)
9. [监控接口](#9-监控接口)
10. [Swagger文档](#10-swagger文档)

---

## 1. 接口概述

### 1.1 服务功能

配送服务（DeliveryService）是社区团购系统的智能物流管理服务，负责：

- ✅ **双引擎路径规划**：Dijkstra算法 + 高德地图API智能切换
- ✅ **配送单管理**：完整的配送单CRUD操作和状态管理
- ✅ **批量发货处理**：管理端批量发货和订单状态同步
- ✅ **仓库配置管理**：多仓库支持和默认仓库设置
- ✅ **配送监控**：团长配送任务监控和实时状态查看
- ✅ **地图可视化**：提供前端地图展示所需的路径数据
- ✅ **智能降级**：API失败时自动降级到本地算法

### 1.2 技术栈

- **框架**: Spring Boot 3.2.3
- **数据库**: MySQL 8.0.36 (delivery_service_db)
- **ORM**: Spring Data JPA
- **API文档**: SpringDoc OpenAPI 2.3.0
- **认证**: JWT Token (通过Gateway)
- **服务注册**: Consul
- **路径规划**: Dijkstra算法 + 高德地图API
- **HTTP客户端**: WebClient (Reactive)

### 1.3 核心特性

#### 🚀 双引擎路径规划
- **主引擎**: 高德地图API - 实时路况，精确路径
- **备用引擎**: Dijkstra算法 - 离线计算，降级保障
- **智能切换**: 根据API可用性自动选择最优算法

#### 📊 性能指标
- Dijkstra算法：10个节点≤20ms，30个节点≤100ms
- 高德API调用：≤2秒
- 接口响应时间：≤500ms
- 并发路径规划：支持10个同时请求

---

## 2. 认证方式

### 2.1 JWT认证
配送服务通过API Gateway统一鉴权，使用JWT Token认证：

```http
Authorization: Bearer <JWT_TOKEN>
```

### 2.2 角色权限
- **管理员**: 所有接口访问权限
- **团长**: 只能操作自己负责的配送单
- **普通用户**: 无配送服务访问权限

---

## 3. 通用响应格式

### 3.1 成功响应
```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": "2025-11-13T22:15:30"
}
```

### 3.2 错误响应
```json
{
  "code": 500,
  "message": "错误描述",
  "data": null,
  "timestamp": "2025-11-13T22:15:30"
}
```

---

## 4. 错误码说明

| 状态码 | 含义 | 说明 |
|--------|------|------|
| 200 | SUCCESS | 请求成功 |
| 400 | BAD_REQUEST | 请求参数错误 |
| 401 | UNAUTHORIZED | 未授权访问 |
| 403 | FORBIDDEN | 权限不足 |
| 404 | NOT_FOUND | 资源不存在 |
| 500 | INTERNAL_ERROR | 服务器内部错误 |
| 1001 | BUSINESS_ERROR | 业务逻辑错误 |
| 2001 | DATA_NOT_FOUND | 数据不存在 |

---

## 5. 配送单管理接口

### 5.1 创建配送单

**接口地址**: `POST /api/delivery`

**功能描述**: 创建新的配送单并可选择生成配送路径

**请求参数**:
```json
{
  "dispatchGroup": "DG20251113001",
  "leaderId": 1001,
  "routeStrategy": "shortest-time",
  "warehouseId": 1,
  "generateRoute": true,
  "remark": "批量发货创建"
}
```

**参数说明**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dispatchGroup | String | ✅ | 分单组标识，唯一 |
| leaderId | Long | ✅ | 负责团长ID |
| routeStrategy | String | ❌ | 路径策略：shortest-time/shortest-distance/avoid-congestion |
| warehouseId | Long | ❌ | 仓库ID，默认使用默认仓库 |
| generateRoute | Boolean | ❌ | 是否立即生成路径，默认true |
| remark | String | ❌ | 备注信息 |

**响应示例**:
```json
{
  "code": 200,
  "message": "配送单创建成功",
  "data": {
    "deliveryId": 1,
    "leaderId": 1001,
    "dispatchGroup": "DG20251113001",
    "status": 0,
    "distance": 5280.50,
    "estimatedDuration": 120,
    "algorithmUsed": "gaode",
    "createTime": "2025-11-13T22:15:30"
  }
}
```

### 5.2 查询配送单详情

**接口地址**: `GET /api/delivery/{deliveryId}`

**功能描述**: 根据配送单ID查询详细信息

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| deliveryId | Long | 配送单ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "deliveryId": 1,
    "leaderId": 1001,
    "dispatchGroup": "DG20251113001",
    "startTime": null,
    "endTime": null,
    "optimalRoute": "39.916527,116.397128;39.925963,116.404000",
    "distance": 5280.50,
    "status": 0,
    "routeStrategy": 0,
    "estimatedDuration": 120,
    "algorithmUsed": "gaode",
    "routeDisplayData": "{\"algorithm\":\"gaode\",\"coordinates\":[...]}",
    "createTime": "2025-11-13T22:15:30",
    "updateTime": null
  }
}
```

### 5.3 根据分单组查询配送单

**接口地址**: `GET /api/delivery/dispatch-group/{dispatchGroup}`

**功能描述**: 根据分单组标识查询对应的配送单

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| dispatchGroup | String | 分单组标识 |

### 5.4 查询团长配送单列表

**接口地址**: `GET /api/delivery/leader/{leaderId}`

**功能描述**: 查询指定团长的所有配送单

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| leaderId | Long | 团长ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": [
    {
      "deliveryId": 1,
      "dispatchGroup": "DG20251113001",
      "status": 1,
      "distance": 5280.50,
      "createTime": "2025-11-13T22:15:30"
    }
  ]
}
```

### 5.5 查询团长指定状态的配送单

**接口地址**: `GET /api/delivery/leader/{leaderId}/status/{status}`

**功能描述**: 查询团长指定状态的配送单列表

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| leaderId | Long | 团长ID |
| status | Integer | 配送状态：0-待分配；1-配送中；2-已完成 |

### 5.6 更新配送状态

**接口地址**: `PUT /api/delivery/{deliveryId}/status`

**功能描述**: 更新配送单的状态

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| deliveryId | Long | 配送单ID |

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | ✅ | 新状态：0-待分配；1-配送中；2-已完成 |

### 5.7 开始配送

**接口地址**: `PUT /api/delivery/{deliveryId}/start`

**功能描述**: 将配送状态从待分配更新为配送中

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| deliveryId | Long | 配送单ID |

### 5.8 完成配送

**接口地址**: `PUT /api/delivery/{deliveryId}/complete`

**功能描述**: 将配送状态从配送中更新为已完成

### 5.9 重新生成配送路径

**接口地址**: `PUT /api/delivery/{deliveryId}/regenerate-route`

**功能描述**: 重新计算配送路径（仅限待分配状态）

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| routeStrategy | String | ✅ | 路径策略 |

### 5.10 删除配送单

**接口地址**: `DELETE /api/delivery/{deliveryId}`

**功能描述**: 删除配送单（仅限待分配状态）

### 5.11 获取配送统计信息

**接口地址**: `GET /api/delivery/statistics`

**功能描述**: 获取指定时间范围内的配送统计数据

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| leaderId | Long | ❌ | 团长ID，不传则统计所有团长 |
| startDate | String | ✅ | 开始日期，格式：yyyy-MM-dd |
| endDate | String | ✅ | 结束日期，格式：yyyy-MM-dd |

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "totalDeliveries": 10,
    "pendingCount": 2,
    "deliveringCount": 3,
    "completedCount": 5,
    "averageDistance": 4521.30,
    "averageDuration": 95,
    "dijkstraUsageCount": 3,
    "gaodeUsageCount": 7
  }
}
```

---

## 6. 路径规划接口

### 6.1 规划配送路径

**接口地址**: `POST /api/delivery/route/plan`

**功能描述**: 使用双引擎计算最优配送路径

**请求参数**:
```json
{
  "dispatchGroup": "DG20251113001",
  "leaderId": 1001,
  "origin": {
    "latitude": 39.916527,
    "longitude": 116.397128,
    "address": "中心仓库"
  },
  "waypoints": [
    {
      "latitude": 39.925963,
      "longitude": 116.404000,
      "address": "配送点1",
      "addressId": 1001
    }
  ],
  "routeStrategy": "shortest-time",
  "forceGaodeApi": false,
  "enableDijkstraFallback": true
}
```

**参数说明**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dispatchGroup | String | ✅ | 分单组标识 |
| leaderId | Long | ✅ | 团长ID |
| origin | GeoPoint | ✅ | 起点坐标（仓库地址） |
| waypoints | List<GeoPoint> | ✅ | 配送点列表 |
| routeStrategy | String | ❌ | 路径策略，默认shortest-time |
| forceGaodeApi | Boolean | ❌ | 是否强制使用高德API |
| enableDijkstraFallback | Boolean | ❌ | 是否启用Dijkstra降级 |

**响应示例**:
```json
{
  "code": 200,
  "message": "路径规划成功",
  "data": {
    "success": true,
    "message": "路径规划成功",
    "algorithmUsed": "gaode",
    "routeStrategy": "SHORTEST_TIME",
    "routePath": "39.916527,116.397128;39.925963,116.404000",
    "totalDistance": 5280.50,
    "estimatedDuration": 120,
    "optimizedWaypoints": [
      {
        "sequence": 1,
        "geoPoint": {
          "latitude": 39.925963,
          "longitude": 116.404000,
          "address": "配送点1"
        },
        "estimatedArrivalTime": 15,
        "distanceFromPrevious": 1200.50
      }
    ],
    "mapDisplayData": "{\"algorithm\":\"gaode\",\"coordinates\":[...]}",
    "calculatedAt": "2025-11-13T22:15:30",
    "apiCallInfo": {
      "duration": 150,
      "fromCache": false,
      "responseCode": 200,
      "retryCount": 0
    }
  }
}
```

### 6.2 获取算法引擎状态

**接口地址**: `GET /api/delivery/route/status`

**功能描述**: 检查高德API和Dijkstra算法的可用状态

**响应示例**:
```json
{
  "code": 200,
  "message": "算法状态查询成功",
  "data": {
    "gaodeApiAvailable": true,
    "dijkstraEnabled": true,
    "maxWaypoints": 30,
    "defaultStrategy": "shortest-time"
  }
}
```

### 6.3 测试算法引擎

**接口地址**: `POST /api/delivery/route/test/{algorithm}`

**功能描述**: 使用预设数据测试指定算法的可用性和性能

**路径参数**:
| 参数 | 类型 | 说明 |
|------|------|------|
| algorithm | String | 算法类型：dijkstra/gaode/auto |

**响应示例**:
```json
{
  "code": 200,
  "message": "算法测试成功",
  "data": {
    "success": true,
    "algorithmUsed": "dijkstra",
    "totalDistance": 3250.80,
    "estimatedDuration": 85,
    "apiCallInfo": {
      "duration": 18
    }
  }
}
```

---

## 7. 批量发货接口

### 7.1 批量发货

**接口地址**: `POST /api/delivery/batch/ship`

**功能描述**: 批量处理订单发货，生成配送单和配送路径

**请求参数**:
```json
{
  "orderIds": [1001, 1002, 1003],
  "warehouseId": 1,
  "routeStrategy": "shortest-time",
  "remark": "批量发货操作",
  "operatorId": 1001,
  "operatorName": "管理员"
}
```

**参数说明**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| orderIds | List<Long> | ✅ | 订单ID列表 |
| warehouseId | Long | ❌ | 仓库ID |
| routeStrategy | String | ❌ | 路径策略 |
| remark | String | ❌ | 备注信息 |
| operatorId | Long | ❌ | 操作员ID |
| operatorName | String | ❌ | 操作员姓名 |

**响应示例**:
```json
{
  "code": 200,
  "message": "批量发货处理完成",
  "data": {
    "success": true,
    "message": "批量发货成功",
    "dispatchGroup": "DG20251113001",
    "totalOrders": 3,
    "successOrders": 3,
    "failedOrders": 0,
    "delivery": {
      "deliveryId": 1,
      "distance": 8520.30,
      "estimatedDuration": 180
    },
    "processedAt": "2025-11-13T22:15:30"
  }
}
```

### 7.2 重新发货

**接口地址**: `POST /api/delivery/batch/retry`

**功能描述**: 重新处理批量发货中失败的订单

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| failedOrderIds | List<Long> | ✅ | 失败的订单ID列表 |
| originalDispatchGroup | String | ✅ | 原始分单组 |

### 7.3 取消批量发货

**接口地址**: `POST /api/delivery/batch/cancel`

**功能描述**: 取消已执行的批量发货操作，恢复订单状态

**请求参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| dispatchGroup | String | ✅ | 分单组 |
| reason | String | ✅ | 取消原因 |

---

## 8. 仓库管理接口

### 8.1 获取默认仓库

**接口地址**: `GET /api/delivery/warehouse/default`

**功能描述**: 获取当前设置的默认仓库配置

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1,
    "warehouseName": "中心仓库",
    "address": "北京市朝阳区示例地址123号",
    "longitude": 116.397128,
    "latitude": 39.916527,
    "isDefault": 1,
    "status": 1,
    "contactPerson": "张三",
    "contactPhone": "13800138000",
    "createTime": "2025-11-13T22:15:30"
  }
}
```

### 8.2 获取启用的仓库列表

**接口地址**: `GET /api/delivery/warehouse/enabled`

**功能描述**: 获取所有状态为启用的仓库配置

### 8.3 查询仓库详情

**接口地址**: `GET /api/delivery/warehouse/{id}`

**功能描述**: 根据仓库ID查询详细配置

### 8.4 创建仓库

**接口地址**: `POST /api/delivery/warehouse`

**功能描述**: 创建新的仓库配置

**请求参数**:
```json
{
  "warehouseName": "东区仓库",
  "address": "北京市朝阳区东区地址456号",
  "longitude": 116.450000,
  "latitude": 39.950000,
  "isDefault": 0,
  "contactPerson": "李四",
  "contactPhone": "13900139000",
  "description": "东区配送仓库"
}
```

### 8.5 更新仓库

**接口地址**: `PUT /api/delivery/warehouse/{id}`

**功能描述**: 更新仓库配置信息

### 8.6 删除仓库

**接口地址**: `DELETE /api/delivery/warehouse/{id}`

**功能描述**: 删除仓库配置（不能删除默认仓库）

### 8.7 设置默认仓库

**接口地址**: `PUT /api/delivery/warehouse/{id}/default`

**功能描述**: 将指定仓库设置为默认仓库

### 8.8 切换仓库状态

**接口地址**: `PUT /api/delivery/warehouse/{id}/toggle-status`

**功能描述**: 启用或禁用仓库（不能禁用默认仓库）

### 8.9 获取仓库统计

**接口地址**: `GET /api/delivery/warehouse/statistics`

**功能描述**: 获取仓库配置的统计信息

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "totalCount": 3,
    "enabledCount": 2,
    "disabledCount": 1,
    "hasDefault": true
  }
}
```

---

## 9. 监控接口

### 9.1 健康检查

**接口地址**: `GET /api/delivery/monitor/health`

**功能描述**: 检查配送服务的基础健康状态

**响应示例**:
```json
{
  "code": 200,
  "message": "服务正常运行",
  "data": {
    "service": "DeliveryService",
    "status": "UP",
    "timestamp": "2025-11-13T22:15:30",
    "version": "1.0.0"
  }
}
```

### 9.2 服务状态检查

**接口地址**: `GET /api/delivery/monitor/status`

**功能描述**: 检查配送服务各组件的运行状态

**响应示例**:
```json
{
  "code": 200,
  "message": "状态检查完成",
  "data": {
    "algorithmStatus": {
      "gaodeApiAvailable": true,
      "dijkstraEnabled": true,
      "maxWaypoints": 30,
      "defaultStrategy": "shortest-time"
    },
    "warehouseStatus": {
      "totalCount": 3,
      "enabledCount": 2,
      "hasDefault": true
    },
    "deliveryStatus": {
      "totalDeliveries": 15,
      "completedCount": 8,
      "averageDistance": 4521.30
    },
    "overall": "healthy",
    "timestamp": "2025-11-13T22:15:30"
  }
}
```

### 9.3 版本信息

**接口地址**: `GET /api/delivery/monitor/version`

**功能描述**: 获取配送服务版本和构建信息

**响应示例**:
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "serviceName": "DeliveryService",
    "version": "1.0.0",
    "buildDate": "2025-11-13",
    "features": [
      "Dijkstra算法路径规划",
      "高德地图API集成",
      "双引擎智能切换",
      "批量发货处理",
      "配送状态管理",
      "地图可视化支持"
    ],
    "port": 8067,
    "database": "delivery_service_db"
  }
}
```

---

## 10. Swagger文档

### 10.1 在线文档
**Swagger UI**: http://localhost:8067/swagger-ui.html  
**OpenAPI JSON**: http://localhost:8067/v3/api-docs

### 10.2 Postman导入
可以直接导入OpenAPI文档到Postman进行接口测试。

---

## 附录

### A. 数据模型

#### A.1 配送单状态枚举
```java
public enum DeliveryStatus {
    PENDING(0, "待分配"),
    DELIVERING(1, "配送中"), 
    COMPLETED(2, "已完成")
}
```

#### A.2 路径策略枚举
```java
public enum RouteStrategy {
    SHORTEST_TIME(0, "shortest-time", "最短时间"),
    SHORTEST_DISTANCE(1, "shortest-distance", "最短距离"),
    AVOID_CONGESTION(2, "avoid-congestion", "避开拥堵")
}
```

### B. 配置说明

#### B.1 高德API配置
```yaml
gaode:
  api:
    key: ${GAODE_API_KEY:your_api_key_here}
    base-url: https://restapi.amap.com/v3
    timeout: 5000
    retry-count: 3
```

#### B.2 算法配置
```yaml
delivery:
  route:
    max-waypoints: 30
    default-strategy: shortest-time
    enable-dijkstra-fallback: true
```

---

**文档版本**: v1.0.0  
**最后更新**: 2025-11-13 22:15  
**维护人员**: 耿康瑞 (20221204229)
