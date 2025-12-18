# LeaderService API 文档

**版本**：v1.0.0  
**作者**：耿康瑞  
**日期**：2025-10-30  
**服务端口**：8068  
**Swagger文档**：http://localhost:8068/swagger-ui.html

---

## 📋 目录

1. [服务概述](#服务概述)
2. [社区管理 API](#社区管理-api)
3. [社区申请审核 API](#社区申请审核-api)
4. [团长管理 API](#团长管理-api)
5. [佣金管理 API](#佣金管理-api)
6. [Feign内部接口](#feign内部接口)
7. [数据结构](#数据结构)

---

## 服务概述

**LeaderService** 是社区团购系统的团长服务，负责管理社区、团长、佣金等核心业务。

### 核心功能模块

1. **社区管理**：社区的创建、查询、匹配（基于经纬度的Haversine距离计算）
2. **社区申请审核**：用户申请成为新社区的团长，管理员审核后自动创建社区
3. **团长管理**：团长申请、审核、停用，审核通过后自动调用UserService更新用户角色
4. **佣金管理**：佣金生成、计算、结算，每月1号定时自动结算

### 数据库

- **数据库名称**：`leader_service_db`
- **表数量**：4张
  - `community`（社区表）
  - `community_application`（社区申请表）
  - `group_leader_store`（团长团点表）
  - `commission_record`（佣金记录表）

---

## 社区管理 API

### 1. 匹配最近的社区

**【核心接口】**根据用户经纬度，使用Haversine公式计算距离，返回最近的社区。

- **接口**：`GET /api/community/nearest`
- **请求参数**：
  ```json
  {
    "latitude": 39.9042,   // 用户纬度
    "longitude": 116.4074  // 用户经度
  }
  ```
- **响应示例**：
  ```json
  {
    "code": 200,
    "message": "匹配成功",
    "data": {
      "communityId": 1,
      "name": "阳光小区",
      "address": "北京市朝阳区建国路88号",
      "latitude": 39.9042,
      "longitude": 116.4074,
      "serviceRadius": 3000,
      "status": 1,
      "createdAt": "2025-10-30T10:00:00"
    },
    "timestamp": "2025-10-30T12:00:00"
  }
  ```

### 2. 查询所有社区

- **接口**：`GET /api/community/list`
- **响应**：返回所有正常运营的社区列表

### 3. 查询社区详情

- **接口**：`GET /api/community/{communityId}`
- **响应**：返回指定社区的详细信息

### 4. 【管理员】创建社区

- **接口**：`POST /api/admin/community`
- **请求体**：
  ```json
  {
    "name": "幸福家园",
    "address": "北京市朝阳区朝阳路100号",
    "latitude": 39.9042,
    "longitude": 116.4074,
    "serviceRadius": 3000,
    "description": "幸福家园社区"
  }
  ```

### 5. 【管理员】更新社区

- **接口**：`PUT /api/admin/community/{communityId}`

### 6. 【管理员】删除社区

- **接口**：`DELETE /api/admin/community/{communityId}`

---

## 社区申请审核 API

### 1. 提交社区申请

用户申请成为新社区的团长。

- **接口**：`POST /api/community-application`
- **请求体**：
  ```json
  {
    "applicantId": 1,
    "applicantName": "张三",
    "applicantPhone": "13800138000",
    "communityName": "新社区名称",
    "address": "北京市朝阳区...",
    "latitude": 39.9042,
    "longitude": 116.4074,
    "serviceRadius": 3000,
    "description": "社区简介",
    "applicationReason": "我想成为团长"
  }
  ```

### 2. 查询我的申请记录

- **接口**：`GET /api/community-application/my?userId=1`

### 3. 查询申请详情

- **接口**：`GET /api/community-application/{applicationId}`

### 4. 【管理员】审核申请

**【核心接口】**审核通过后自动创建Community + GroupLeaderStore。

- **接口**：`POST /api/community-application/{applicationId}/review`
- **请求参数**：
  ```
  reviewerId: 1
  approved: true
  reviewComment: 审核通过
  ```

### 5. 【管理员】查询待审核申请

- **接口**：`GET /api/community-application/pending`

---

## 团长管理 API

### 1. 提交团长申请

- **接口**：`POST /api/leader/apply`
- **请求体**：
  ```json
  {
    "leaderId": 1,
    "leaderName": "李四",
    "leaderPhone": "13900139000",
    "communityId": 1,
    "storeName": "李四团点",
    "address": "社区1号楼",
    "description": "团点简介"
  }
  ```

### 2. 查询我的团长信息

- **接口**：`GET /api/leader/my?userId=1`

### 3. 查询社区的团长列表

- **接口**：`GET /api/leader/community/{communityId}`

### 4. 查询团长详情

- **接口**：`GET /api/leader/{storeId}`

### 5. 【团长】更新团点信息

- **接口**：`PUT /api/leader/{storeId}`

### 6. 【管理员】审核团长申请

**【核心接口】**审核通过后自动调用UserService更新用户角色为2（团长）。

- **接口**：`POST /api/leader/{storeId}/review`
- **请求参数**：
  ```
  reviewerId: 1
  approved: true
  reviewComment: 审核通过
  ```

### 7. 【管理员】查询待审核申请

- **接口**：`GET /api/leader/pending`

### 8. 【管理员】停用团长

- **接口**：`POST /api/leader/{storeId}/disable`

---

## 佣金管理 API

### 1. 【团长】查询我的佣金记录

- **接口**：`GET /api/commission/my?leaderId=1`

### 2. 【团长】查询佣金统计

- **接口**：`GET /api/commission/my/summary?leaderId=1`
- **响应示例**：
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "pendingCommission": 100.00,   // 待结算佣金
      "settledCommission": 500.00,   // 已结算佣金
      "totalCommission": 600.00      // 累计佣金
    }
  }
  ```

### 3. 【管理员】查询待结算佣金

- **接口**：`GET /api/commission/pending`

### 4. 【管理员】查询已结算佣金

- **接口**：`GET /api/commission/settled`
- **响应示例**：
  ```json
  {
    "code": 200,
    "message": "success",
    "data": [
      {
        "recordId": 1,
        "leaderId": 1,
        "leaderName": "李四",
        "orderId": 100,
        "orderAmount": 100.00,
        "commissionRate": 10.00,
        "commissionAmount": 10.00,
        "status": 1,
        "settledAt": "2025-11-01T02:00:00",
        "settlementBatch": "20251101",
        "createdAt": "2025-10-30T12:00:00"
      }
    ]
  }
  ```

### 5. 【管理员】查询结算批次

- **接口**：`GET /api/commission/batch/{settlementBatch}`

### 6. 【管理员】手动结算佣金

- **接口**：`POST /api/commission/settle`

---

## Feign内部接口

供其他微服务调用的内部接口（路径前缀：`/feign`）

### 社区相关

1. **获取社区信息**
   - `GET /feign/community/{communityId}`

2. **匹配最近的社区**
   - `GET /feign/community/nearest?latitude=39.9042&longitude=116.4074`

3. **验证社区是否存在**
   - `GET /feign/community/exists/{communityId}`

### 团长相关

4. **获取团长信息**
   - `GET /feign/leader/{leaderId}`

5. **查询社区的团长列表**
   - `GET /feign/community/{communityId}/leaders`

6. **验证是否是团长**
   - `GET /feign/leader/check/{userId}`

### 佣金相关

7. **【核心】生成佣金记录**（OrderService调用）
   - `POST /feign/commission/generate`
   - 请求参数：`leaderId`, `orderId`, `orderAmount`

8. **查询待结算佣金**
   - `GET /feign/commission/pending/{leaderId}`

---

## 数据结构

### Community（社区）

```java
{
  "communityId": 1,
  "name": "阳光小区",
  "address": "北京市朝阳区建国路88号",
  "latitude": 39.904200,
  "longitude": 116.407400,
  "serviceRadius": 3000,
  "status": 1,  // 0-待审核 1-正常运营 2-已关闭
  "description": "社区简介",
  "createdAt": "2025-10-30T10:00:00",
  "updatedAt": "2025-10-30T10:00:00"
}
```

### CommunityApplication（社区申请）

```java
{
  "applicationId": 1,
  "applicantId": 1,
  "applicantName": "张三",
  "applicantPhone": "13800138000",
  "communityName": "新社区",
  "address": "...",
  "latitude": 39.9042,
  "longitude": 116.4074,
  "serviceRadius": 3000,
  "applicationReason": "申请理由",
  "status": 0,  // 0-待审核 1-审核通过 2-审核拒绝
  "reviewerId": 1,
  "reviewComment": "审核意见",
  "reviewedAt": "2025-10-30T12:00:00",
  "createdCommunityId": 2,
  "createdAt": "2025-10-30T10:00:00"
}
```

### GroupLeaderStore（团长团点）

```java
{
  "storeId": 1,
  "leaderId": 1,
  "leaderName": "李四",
  "leaderPhone": "13900139000",
  "communityId": 1,
  "communityName": "阳光小区",
  "storeName": "李四团点",
  "address": "社区1号楼",
  "description": "团点简介",
  "commissionRate": 10.00,  // 佣金比例10%
  "totalCommission": 500.00,
  "status": 1,  // 0-待审核 1-正常运营 2-已停用
  "reviewerId": 1,
  "reviewComment": "审核意见",
  "reviewedAt": "2025-10-30T12:00:00",
  "createdAt": "2025-10-30T10:00:00"
}
```

### CommissionRecord（佣金记录）

```java
{
  "recordId": 1,
  "leaderId": 1,
  "leaderName": "李四",
  "orderId": 100,
  "orderAmount": 100.00,
  "commissionRate": 10.00,
  "commissionAmount": 10.00,  // 佣金金额 = 订单金额 * 佣金比例 / 100
  "status": 0,  // 0-待结算 1-已结算 2-结算失败
  "settledAt": "2025-11-01T02:00:00",
  "settlementBatch": "20251101",
  "remark": "订单完成，生成佣金记录",
  "createdAt": "2025-10-30T12:00:00"
}
```

---

## 核心算法

### 1. Haversine距离计算公式

用于计算地球表面两点间的球面距离。

```java
/**
 * Haversine公式计算两点间球面距离
 * 
 * 公式：
 * a = sin²(Δφ/2) + cos(φ1) * cos(φ2) * sin²(Δλ/2)
 * c = 2 * atan2(√a, √(1−a))
 * d = R * c
 * 
 * 其中：
 * - φ 表示纬度（latitude）
 * - λ 表示经度（longitude）
 * - R 表示地球半径（6371000米）
 */
private double calculateHaversineDistance(
        BigDecimal lat1, BigDecimal lon1,
        BigDecimal lat2, BigDecimal lon2
) {
    // 转换为弧度
    double lat1Rad = Math.toRadians(lat1.doubleValue());
    double lon1Rad = Math.toRadians(lon1.doubleValue());
    double lat2Rad = Math.toRadians(lat2.doubleValue());
    double lon2Rad = Math.toRadians(lon2.doubleValue());

    // 计算差值
    double deltaLat = lat2Rad - lat1Rad;
    double deltaLon = lon2Rad - lon1Rad;

    // Haversine公式
    double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
            Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                    Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    // 距离（米）
    return 6371000.0 * c;
}
```

### 2. 佣金计算公式

```java
/**
 * 佣金金额 = 订单金额 * 佣金比例 / 100
 * 
 * 示例：订单100元，佣金比例10%，则佣金 = 100 * 10 / 100 = 10元
 */
private BigDecimal calculateCommissionAmount(BigDecimal orderAmount, BigDecimal commissionRate) {
    return orderAmount
            .multiply(commissionRate)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
}
```

---

## 定时任务

### 佣金结算定时任务

- **执行时间**：每月1号凌晨2点
- **Cron表达式**：`0 0 2 1 * ?`
- **业务流程**：
  1. 查询所有待结算的佣金记录
  2. 按团长分组，计算每个团长的佣金总额
  3. 调用UserService为团长增加余额（TODO：待实现）
  4. 更新佣金记录状态为"已结算"
  5. 更新团长的累计佣金

---

## 响应状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 后续开发计划

1. **UserService适配**：在UserService中添加 `POST /feign/user/{userId}/role` 接口，供LeaderService调用更新用户角色
2. **OrderService集成**：订单完成时调用 `POST /feign/commission/generate` 生成佣金记录
3. **网关路由配置**：在gateway-service中添加LeaderService的路由规则
4. **前端集成**：开发团长端前端页面，调用LeaderService的API接口

---

**Swagger在线文档**：启动服务后访问 http://localhost:8068/swagger-ui.html 查看完整的API文档。

