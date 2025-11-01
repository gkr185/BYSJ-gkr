# 社区团购系统 - 拼团服务API接口文档

**服务名称**: GroupBuyService  
**服务端口**: 8063  
**Base URL**: `http://localhost:8063`  
**通过网关访问**: `http://localhost:9000`  
**版本**: v1.0.1  
**文档日期**: 2025-10-31  
**最后更新**: 2025-11-01 18:30

---

## 📋 目录

1. [接口概述](#1-接口概述)
2. [认证方式](#2-认证方式)
3. [通用响应格式](#3-通用响应格式)
4. [错误码说明](#4-错误码说明)
5. [拼团活动管理接口](#5-拼团活动管理接口)
6. [团管理接口](#6-团管理接口)
7. [查询接口](#7-查询接口)
8. [定时任务](#8-定时任务)
9. [Swagger文档](#9-swagger文档)

---

## 1. 接口概述

### 1.1 服务功能

拼团服务（GroupBuyService）是社区团购系统的核心服务之一，负责：

- ✅ 拼团活动管理（创建、查询、更新、删除）
- ✅ 团长发起拼团（v3.0：验证团长身份、自动关联社区）
- ✅ 用户参与拼团（行锁防并发、防重复参团）
- ✅ 支付回调处理（成团检查、幂等性保证）
- ✅ 查询功能（团详情、活动团列表、我的拼团）
- ✅ 退出拼团（成团前可退、自动退款）
- ✅ 定时任务（过期团自动退款）

### 1.2 技术栈

- **框架**: Spring Boot 3.2.3
- **微服务**: Spring Cloud 2023.0.0
- **数据库**: MySQL 8.0.36
- **ORM**: Spring Data JPA
- **服务注册**: Consul
- **服务调用**: OpenFeign
- **API文档**: SpringDoc OpenAPI 2.3.0
- **认证**: JWT Token（Gateway统一鉴权）

### 1.3 技术亮点

- ⭐ **无Redis分布式锁**：使用数据库行锁（SELECT ... FOR UPDATE）
- ⭐ **三层幂等性设计**：支付回调、成团逻辑、定时任务
- ⭐ **Saga模式**：跨服务调用 + 补偿机制
- ⭐ **社区优先推荐**：SQL ORDER BY CASE实现（v3.0）
- ⭐ **异常隔离**：定时任务单个团失败不影响其他团

### 1.4 接口统计

| 模块 | 接口数 | 路径前缀 |
|------|--------|---------|
| 拼团活动管理（管理端） | 6个 | `/api/groupbuy/activity`, `/api/groupbuy/product` |
| 团管理（核心⭐） | 6个 | `/api/groupbuy/team` |
| 支付回调（内部） | 1个 | `/api/groupbuy/payment` |
| **总计** | **13个** | - |

---

## 2. 认证方式

### 2.1 JWT Token认证

所有需要鉴权的API请求都需要在请求头中携带JWT Token：

```http
Authorization: Bearer <your_jwt_token>
```

**说明**: JWT鉴权在Gateway层统一处理，GroupBuyService从请求头获取用户信息：
- `X-User-Id`: 用户ID
- `X-Username`: 用户名
- `X-User-Role`: 用户角色（1-普通用户/2-团长/3-管理员）

### 2.2 白名单接口（无需认证）

以下接口无需JWT Token：

- `GET /api/groupbuy/team/{teamId}/detail` - 团详情
- `GET /api/groupbuy/activity/{activityId}/teams` - 活动团列表
- `GET /api/groupbuy/product/{productId}/activities` - 商品的拼团活动（含团列表）⭐新增
- `GET /api/groupbuy/activities` - 活动列表
- `GET /api/groupbuy/activities/ongoing` - 进行中的活动
- `GET /api/groupbuy/activity/{id}` - 活动详情
- `GET /actuator/health` - 健康检查
- `/swagger-ui/**` - Swagger UI
- `/v3/api-docs/**` - API文档

### 2.3 获取Token

通过UserService的登录接口获取Token：

```bash
POST http://localhost:9000/api/user/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}
```

---

## 3. 通用响应格式

### 3.1 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": "2025-10-31T20:00:00"
}
```

### 3.2 失败响应

```json
{
  "code": 400,
  "message": "错误信息",
  "data": null,
  "timestamp": "2025-10-31T20:00:00"
}
```

### 3.3 分页响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [ ... ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10
  },
  "timestamp": "2025-10-31T20:00:00"
}
```

---

## 4. 错误码说明

| 错误码 | 说明 | 常见场景 |
|-------|------|---------|
| 200 | 成功 | - |
| 400 | 参数错误 | 必填参数缺失、参数格式错误 |
| 401 | 未授权 | Token缺失或过期 |
| 403 | 无权限 | 非团长用户发起拼团 |
| 404 | 资源不存在 | 团ID不存在、活动ID不存在 |
| 409 | 业务冲突 | 重复参团、团已满员、拼团已结束 |
| 500 | 服务器错误 | 数据库异常、服务调用失败 |
| 503 | 服务不可用 | 依赖服务（OrderService/UserService）不可用 |

### 4.1 业务错误码

| 错误信息 | 说明 | 解决方案 |
|---------|------|---------|
| "拼团活动不存在" | 活动ID无效 | 检查activityId |
| "拼团活动未开始或已结束" | 活动不在时间范围内 | 检查活动状态 |
| "仅团长可发起拼团" | 用户role!=2 | 确认用户为团长 |
| "团不存在" | 团ID无效 | 检查teamId |
| "拼团已结束" | 团状态!=0 | 查询其他团 |
| "团已满员" | current_num >= required_num | 查询其他团 |
| "拼团已过期" | expire_time < now | 查询其他团 |
| "您已参加此团" | 重复参团 | 无需重复操作 |
| "拼团已成功，无法退出" | 成团后不可退 | 联系客服 |
| "订单服务暂时不可用" | OrderService不可用 | 稍后重试 |

---

## 5. 拼团活动管理接口

### 5.1 创建拼团活动

**接口**: `POST /api/groupbuy/activity`  
**权限**: 管理员  
**鉴权**: 需要Token

**请求示例**:
```json
{
  "productId": 1,
  "groupPrice": 19.90,
  "requiredNum": 3,
  "maxNum": 100,
  "startTime": "2025-10-31T00:00:00",
  "endTime": "2025-12-31T23:59:59"
}
```

**请求参数说明**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| productId | Long | ✅ | 商品ID（跨库关联ProductService） |
| groupPrice | BigDecimal | ✅ | 拼团价（必须小于商品原价） |
| requiredNum | Integer | ✅ | 成团人数（2-10人） |
| maxNum | Integer | ❌ | 最大人数限制（可为null，表示无限制） |
| startTime | String | ✅ | 活动开始时间（ISO 8601格式） |
| endTime | String | ✅ | 活动结束时间（ISO 8601格式） |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "activityId": 1,
    "productId": 1,
    "groupPrice": 19.90,
    "requiredNum": 3,
    "maxNum": 100,
    "startTime": "2025-10-31T00:00:00",
    "endTime": "2025-12-31T23:59:59",
    "status": 1,
    "qrcodeUrl": null,
    "link": null,
    "createTime": "2025-10-31T20:00:00"
  },
  "timestamp": "2025-10-31T20:00:00"
}
```

**业务规则**:
1. Feign验证商品存在（调用ProductService）
2. 拼团价必须小于商品原价
3. startTime不能晚于endTime
4. 活动创建后自动设置status=1（进行中）

---

### 5.2 更新拼团活动

**接口**: `PUT /api/groupbuy/activity/{id}`  
**权限**: 管理员  
**鉴权**: 需要Token

**请求示例**:
```json
{
  "groupPrice": 18.90,
  "requiredNum": 2,
  "status": 1
}
```

**响应**: 同创建活动响应

---

### 5.3 删除拼团活动

**接口**: `DELETE /api/groupbuy/activity/{id}`  
**权限**: 管理员  
**鉴权**: 需要Token

**响应示例**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": "2025-10-31T20:00:00"
}
```

---

### 5.4 获取活动列表

**接口**: `GET /api/groupbuy/activities`  
**权限**: 无  
**鉴权**: 不需要Token

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "activityId": 1,
      "productId": 1,
      "groupPrice": 19.90,
      "requiredNum": 3,
      "maxNum": 100,
      "startTime": "2025-10-31T00:00:00",
      "endTime": "2025-12-31T23:59:59",
      "status": 1,
      "createTime": "2025-10-31T20:00:00"
    }
  ],
  "timestamp": "2025-10-31T20:00:00"
}
```

---

### 5.5 获取进行中的活动

**接口**: `GET /api/groupbuy/activities/ongoing`  
**权限**: 无  
**鉴权**: 不需要Token

**说明**: 返回status=1且在活动时间范围内的活动

**响应**: 同活动列表

---

### 5.6 获取活动详情

**接口**: `GET /api/groupbuy/activity/{id}`  
**权限**: 无  
**鉴权**: 不需要Token

**响应**: 单个活动信息

---

## 6. 团管理接口

### 6.1 团长发起拼团（⭐v3.0核心接口）

**接口**: `POST /api/groupbuy/team/launch`  
**权限**: 团长（role=2）  
**鉴权**: 需要Token

**特性**:
- ✅ 仅团长可发起（Feign调用UserService验证role=2）
- ✅ 自动关联团长的社区（v3.0特性）
- ✅ 团长可选择是否立即参与
- ✅ 生成唯一团号（格式：T20251031001）

**请求示例**:
```json
{
  "activityId": 1,
  "joinImmediately": true,
  "addressId": 1,
  "quantity": 1
}
```

**请求参数说明**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| activityId | Long | ✅ | 活动ID |
| joinImmediately | Boolean | ❌ | 是否立即参与（默认false） |
| addressId | Long | ⚠️ | 收货地址ID（joinImmediately=true时必填） |
| quantity | Integer | ❌ | 购买数量（默认1） |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "teamId": 1,
    "teamNo": "T20251031001",
    "activityId": 1,
    "activityName": "拼团活动-1",
    "groupPrice": 19.90,
    "leaderId": 1,
    "leaderName": "张团长",
    "communityId": 10,
    "communityName": "幸福小区",
    "requiredNum": 3,
    "currentNum": 1,
    "remainNum": 2,
    "teamStatus": 0,
    "teamStatusDesc": "拼团中",
    "successTime": null,
    "expireTime": "2025-11-01T20:00:00",
    "createTime": "2025-10-31T20:00:00",
    "members": [
      {
        "memberId": 1,
        "userId": 1,
        "username": "leader123",
        "realName": "张团长",
        "avatar": null,
        "isLauncher": 1,
        "payAmount": 19.90,
        "joinTime": "2025-10-31T20:00:00",
        "status": 0,
        "statusDesc": "待支付"
      }
    ],
    "shareLink": "http://localhost:5173/team/1"
  },
  "timestamp": "2025-10-31T20:00:00"
}
```

**业务流程**:
1. Feign调用UserService验证团长身份（role=2）
2. 获取团长的社区ID（communityId）
3. 创建团实例（launcher_id = leader_id = userId）
4. 自动关联社区（community_id = 团长社区）⭐v3.0
5. 生成团号（T + yyyyMMdd + 6位随机数）
6. 如果joinImmediately=true：
   - Feign调用OrderService创建订单
   - 记录参团（is_launcher=1, status=0待支付）

**错误响应**:
```json
{
  "code": 403,
  "message": "仅团长可发起拼团，当前角色：1",
  "data": null,
  "timestamp": "2025-10-31T20:00:00"
}
```

---

### 6.2 用户参与拼团（⭐核心接口）

**接口**: `POST /api/groupbuy/team/join`  
**权限**: 已登录用户  
**鉴权**: 需要Token

**特性**:
- ✅ 行锁检查团状态（SELECT ... FOR UPDATE）
- ✅ 防重复参团（唯一索引 uk_team_user）
- ✅ Feign调用OrderService创建订单
- ✅ 记录参团（status=0待支付）

**请求示例**:
```json
{
  "teamId": 1,
  "addressId": 2,
  "quantity": 1
}
```

**请求参数说明**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| teamId | Long | ✅ | 团ID |
| addressId | Long | ✅ | 收货地址ID |
| quantity | Integer | ✅ | 购买数量（最小为1） |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "orderId": 8002,
    "teamId": 1,
    "teamNo": "T20251031001",
    "currentNum": 2,
    "requiredNum": 3,
    "remainNum": 1,
    "payAmount": 19.90,
    "expireTime": "2025-11-01 20:00:00"
  },
  "timestamp": "2025-10-31T20:05:00"
}
```

**业务流程**:
1. 查询团（加行锁）⭐ `SELECT ... FOR UPDATE`
2. 状态校验：
   - 团状态必须为"拼团中"（team_status=0）
   - 当前人数 < 成团人数
   - 未过期（expire_time > now）
3. 防重复参团检查（existsByTeamIdAndUserId）
4. Feign调用OrderService创建订单
5. 记录参团（order_id关联，status=0待支付）
6. 返回订单ID（前端跳转支付页面）

**错误响应**:

```json
// 重复参团
{
  "code": 409,
  "message": "您已参加此团",
  "data": null,
  "timestamp": "2025-10-31T20:05:00"
}

// 团已满
{
  "code": 409,
  "message": "团已满员",
  "data": null,
  "timestamp": "2025-10-31T20:05:00"
}

// 团已结束
{
  "code": 409,
  "message": "拼团已结束，当前状态：已成团",
  "data": null,
  "timestamp": "2025-10-31T20:05:00"
}
```

---

### 6.3 支付回调（⭐核心接口，内部调用）

**接口**: `POST /api/groupbuy/payment/callback`  
**权限**: 内部接口  
**鉴权**: 不需要Token

**说明**: 由PaymentService支付成功后回调，触发成团检查

**请求示例**:
```bash
POST /api/groupbuy/payment/callback?orderId=8002
```

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| orderId | Long | ✅ | 订单ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": "2025-10-31T20:10:00"
}
```

**业务流程**（⭐核心逻辑）:
1. 查询参团记录（加行锁）⭐ `findByOrderIdForUpdate`
2. 幂等性检查：`if (status != UNPAID) return;` ⭐
3. 更新参团状态（UNPAID → PAID）
4. 查询团（加行锁）⭐ `findByIdForUpdate`
5. 更新团人数（current_num++）
6. 检查是否成团（current_num >= required_num）
7. 如果成团，调用`teamSuccess(teamId)`

**成团逻辑**（幂等性保证）:
```java
@Transactional
public void teamSuccess(Long teamId) {
    // 1. 查询团（加行锁）⭐
    GroupBuyTeam team = teamRepository.findByIdForUpdate(teamId);
    
    // 2. 幂等性检查 ⭐
    if (team.getTeamStatus() != JOINING) {
        return;  // 已处理过，直接返回
    }
    
    // 3. 更新团状态（JOINING → SUCCESS）
    // 4. 批量更新成员状态（PAID → SUCCESS）
    // 5. Feign批量更新订单状态（TO_DELIVER）
}
```

**技术亮点**:
- ✅ 双重行锁（参团记录锁 + 团锁）
- ✅ 双重幂等检查（参团状态 + 团状态）
- ✅ 并发支付回调安全
- ✅ 成团逻辑只触发一次

---

### 6.4 退出拼团

**接口**: `POST /api/groupbuy/team/quit`  
**权限**: 已登录用户  
**鉴权**: 需要Token

**说明**: 成团前可退出，自动退款

**请求示例**:
```bash
POST /api/groupbuy/team/quit?teamId=1
```

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| teamId | Long | ✅ | 团ID |

**响应示例**:
```json
{
  "code": 200,
  "message": "退出成功，已退款",
  "data": null,
  "timestamp": "2025-10-31T20:15:00"
}
```

**业务流程**:
1. 查询参团记录
2. 查询团（加行锁）
3. 检查团状态（已成团不可退）
4. 删除参团记录
5. 更新团人数（current_num--）
6. Feign退款到用户余额
7. Feign更新订单状态为"已退款"

**错误响应**:
```json
{
  "code": 409,
  "message": "拼团已成功，无法退出",
  "data": null,
  "timestamp": "2025-10-31T20:15:00"
}
```

---

## 7. 查询接口

### 7.1 获取团详情

**接口**: `GET /api/groupbuy/team/{teamId}/detail`  
**权限**: 无  
**鉴权**: 不需要Token

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "teamId": 1,
    "teamNo": "T20251031001",
    "activityId": 1,
    "activityName": "拼团活动-1",
    "groupPrice": 19.90,
    "leaderId": 1,
    "leaderName": "张团长",
    "communityId": 10,
    "communityName": "幸福小区",
    "requiredNum": 3,
    "currentNum": 3,
    "remainNum": 0,
    "teamStatus": 1,
    "teamStatusDesc": "已成团",
    "successTime": "2025-10-31T20:30:00",
    "expireTime": "2025-11-01T20:00:00",
    "createTime": "2025-10-31T20:00:00",
    "members": [
      {
        "memberId": 1,
        "userId": 1,
        "username": "leader123",
        "realName": "张团长",
        "avatar": null,
        "isLauncher": 1,
        "payAmount": 19.90,
        "joinTime": "2025-10-31T20:00:00",
        "status": 2,
        "statusDesc": "已成团"
      },
      {
        "memberId": 2,
        "userId": 2,
        "username": "user001",
        "realName": "李四",
        "avatar": null,
        "isLauncher": 0,
        "payAmount": 19.90,
        "joinTime": "2025-10-31T20:05:00",
        "status": 2,
        "statusDesc": "已成团"
      },
      {
        "memberId": 3,
        "userId": 3,
        "username": "user002",
        "realName": "王五",
        "avatar": null,
        "isLauncher": 0,
        "payAmount": 19.90,
        "joinTime": "2025-10-31T20:10:00",
        "status": 2,
        "statusDesc": "已成团"
      }
    ],
    "shareLink": "http://localhost:5173/team/1"
  },
  "timestamp": "2025-10-31T20:30:00"
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|-----|------|------|
| teamStatus | Integer | 0-拼团中/1-已成团/2-已失败 |
| remainNum | Integer | 还差几人（计算字段：required_num - current_num） |
| members | Array | 成员列表（按参团时间升序） |
| isLauncher | Integer | 是否发起人（0-否/1-是，每个团只有1个发起人） |

---

### 7.2 获取活动的团列表（⭐社区优先排序）

**接口**: `GET /api/groupbuy/activity/{activityId}/teams`  
**权限**: 无  
**鉴权**: 不需要Token

**特性**:
- ✅ v3.0社区优先推荐
- ✅ 优先显示本社区的团
- ✅ SQL ORDER BY CASE实现
- ✅ 只返回拼团中的团（team_status=0）
- ✅ 只返回未过期的团（expire_time > now）

**请求示例**:
```bash
GET /api/groupbuy/activity/1/teams?communityId=10
```

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| communityId | Long | ❌ | 用户的社区ID（传入后优先显示本社区团） |

**SQL实现**（⭐技术亮点）:
```sql
SELECT * FROM group_buy_team t
WHERE t.activity_id = :activityId
  AND t.team_status = 0
  AND t.expire_time > NOW()
ORDER BY 
  CASE WHEN t.community_id = :communityId THEN 0 ELSE 1 END ASC,
  t.create_time DESC
LIMIT 20
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "teamId": 1,
      "teamNo": "T20251031001",
      "communityId": 10,
      "communityName": "幸福小区",
      "currentNum": 2,
      "requiredNum": 3,
      "remainNum": 1,
      "teamStatus": 0,
      "teamStatusDesc": "拼团中",
      "expireTime": "2025-11-01T20:00:00",
      "createTime": "2025-10-31T20:00:00"
    },
    {
      "teamId": 2,
      "teamNo": "T20251031002",
      "communityId": 11,
      "communityName": "阳光小区",
      "currentNum": 1,
      "requiredNum": 3,
      "remainNum": 2,
      "teamStatus": 0,
      "teamStatusDesc": "拼团中",
      "expireTime": "2025-11-01T20:30:00",
      "createTime": "2025-10-31T20:30:00"
    }
  ],
  "timestamp": "2025-10-31T21:00:00"
}
```

**排序规则**（v3.0）:
1. **优先级1**: communityId匹配的团排在前面（CASE WHEN = 0）
2. **优先级2**: 其他社区的团排在后面（CASE WHEN = 1）
3. **优先级3**: 同一优先级内按创建时间倒序

**示例说明**:
- 用户传入`communityId=10`
- 团1（communityId=10）排在前面⭐
- 团2（communityId=11）排在后面

---

### 7.3 根据商品ID获取拼团活动（⭐商品详情页专用）

**接口**: `GET /api/groupbuy/product/{productId}/activities`  
**权限**: 无  
**鉴权**: 不需要Token  
**版本**: v1.0.1 新增

**特性**:
- ✅ 返回该商品的所有进行中拼团活动
- ✅ 每个活动包含进行中的团列表（最多10个）
- ✅ 支持社区优先排序
- ✅ 无需登录即可访问
- ✅ 用于商品详情页展示拼团信息

**请求示例**:
```bash
GET /api/groupbuy/product/1/activities?communityId=1
```

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|-----|------|------|------|
| productId | Long | ✅ | 商品ID（路径参数） |
| communityId | Long | ❌ | 用户社区ID（可选，用于社区优先排序） |

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "activityId": 1,
      "productId": 1,
      "groupPrice": 19.90,
      "requiredNum": 3,
      "maxNum": 100,
      "startTime": "2025-10-31T00:00:00",
      "endTime": "2025-12-31T23:59:59",
      "status": 1,
      "teams": [
        {
          "teamId": 5001,
          "teamNo": "T20251101001",
          "leaderId": 2001,
          "leaderName": "张团长",
          "communityId": 1,
          "communityName": "阳光小区",
          "requiredNum": 3,
          "currentNum": 2,
          "teamStatus": 0,
          "expireTime": "2025-11-02T12:00:00",
          "createTime": "2025-11-01T12:00:00"
        },
        {
          "teamId": 5002,
          "teamNo": "T20251101002",
          "leaderId": 2002,
          "leaderName": "李团长",
          "communityId": 2,
          "communityName": "幸福小区",
          "requiredNum": 3,
          "currentNum": 1,
          "teamStatus": 0,
          "expireTime": "2025-11-02T14:00:00",
          "createTime": "2025-11-01T14:00:00"
        }
      ]
    },
    {
      "activityId": 2,
      "productId": 1,
      "groupPrice": 18.90,
      "requiredNum": 2,
      "maxNum": 50,
      "startTime": "2025-11-01T00:00:00",
      "endTime": "2025-11-30T23:59:59",
      "status": 1,
      "teams": []
    }
  ],
  "timestamp": "2025-11-01T18:30:00"
}
```

**字段说明**:

| 字段 | 类型 | 说明 |
|-----|------|------|
| activityId | Long | 活动ID |
| productId | Long | 商品ID |
| groupPrice | BigDecimal | 拼团价 |
| requiredNum | Integer | 成团人数 |
| maxNum | Integer | 最大人数限制 |
| startTime | String | 活动开始时间 |
| endTime | String | 活动结束时间 |
| status | Integer | 活动状态（1-进行中） |
| teams | Array | 该活动的团列表（进行中的团，最多10个） |

**团信息字段**:

| 字段 | 类型 | 说明 |
|-----|------|------|
| teamId | Long | 团ID |
| teamNo | String | 团号 |
| leaderId | Long | 团长ID |
| leaderName | String | 团长姓名 |
| communityId | Long | 社区ID |
| communityName | String | 社区名称 |
| requiredNum | Integer | 成团人数 |
| currentNum | Integer | 当前人数 |
| teamStatus | Integer | 团状态（0-拼团中/1-已成团/2-已失败） |
| expireTime | String | 过期时间 |
| createTime | String | 创建时间 |

**业务规则**:
1. 只返回进行中的活动（status=1且在时间范围内）
2. 每个活动最多返回10个进行中的团
3. 团列表支持社区优先排序（传入communityId时优先显示本社区的团）
4. 团列表只包含拼团中的团（team_status=0且未过期）
5. 如果活动无进行中的团，teams字段为空数组

**使用场景**:
- 商品详情页展示拼团信息
- 用户浏览商品时查看可参与的拼团活动和团队
- 支持未登录用户查看（无需Token）

**团列表排序规则**（传入communityId时）:
1. **优先级1**: 本社区的团排在前面⭐
2. **优先级2**: 其他社区的团排在后面
3. **优先级3**: 同一优先级内按创建时间倒序

---

## 8. 定时任务

### 8.1 过期团检查任务（⭐核心任务）

**执行时间**: 每小时执行一次  
**Cron表达式**: `0 0 * * * ?`  
**任务类**: `TeamExpireTask.java`

**功能**:
- ✅ 查询过期的团（team_status=0 且 expire_time < now）
- ✅ 遍历退款（幂等性保证）
- ✅ 异常隔离（单个团失败不影响其他团）

**执行流程**:
```
1. 查询过期团ID列表
   ↓
2. 遍历每个团（try-catch捕获异常）
   ↓
3. 查询团（加行锁）⭐
   ↓
4. 幂等性检查（team_status != JOINING 则跳过）⭐
   ↓
5. 标记团失败（JOINING → FAILED）
   ↓
6. 查询已支付的成员（status=1）
   ↓
7. 遍历退款：
   - Feign退款到用户余额
   - Feign更新订单状态为"已退款"
   - 更新参团状态为"已取消"
   ↓
8. 发送拼团失败通知（TODO）
```

**日志示例**:
```
====================================
开始检查过期团，当前时间：2025-11-01T20:00:00
====================================
发现2个过期团需要处理：[1, 2]
✅ 团1退款成功
✅ 团2退款成功
====================================
过期团处理完成
总计：2个，成功：2个，失败：0个
成功率：100%
====================================
```

**技术亮点**:
- ✅ 幂等性设计：行锁 + 状态检查
- ✅ 异常隔离：try-catch捕获单个团异常
- ✅ 事务独立：每个团单独事务处理
- ✅ 高可用：失败不影响其他团

---

## 9. Swagger文档

### 9.1 访问地址

**直连访问**:
```
http://localhost:8063/swagger-ui.html
```

**通过网关访问**:
```
http://localhost:9000/swagger-ui.html
```

### 9.2 API Docs

**JSON格式**:
```
http://localhost:8063/v3/api-docs
```

### 9.3 接口分组

Swagger UI将接口分为以下分组：

- **拼团管理** (`TeamController`)
  - 团长发起拼团
  - 用户参与拼团
  - 支付回调
  - 获取团详情
  - 获取活动团列表
  - 退出拼团

- **拼团活动管理** (`ActivityController`)
  - 创建活动
  - 更新活动
  - 删除活动
  - 获取活动列表
  - 获取活动详情

---

## 10. 使用示例

### 10.1 完整拼团流程

#### 步骤1: 管理员创建活动

```bash
POST http://localhost:9000/api/groupbuy/activity
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "productId": 1,
  "groupPrice": 19.90,
  "requiredNum": 3,
  "startTime": "2025-10-31T00:00:00",
  "endTime": "2025-12-31T23:59:59"
}
```

#### 步骤2: 团长发起拼团

```bash
POST http://localhost:9000/api/groupbuy/team/launch
Authorization: Bearer {leader_token}
Content-Type: application/json

{
  "activityId": 1,
  "joinImmediately": true,
  "addressId": 1,
  "quantity": 1
}

# 响应：teamId=1, orderId=8001
```

#### 步骤3: 用户1参团

```bash
POST http://localhost:9000/api/groupbuy/team/join
Authorization: Bearer {user1_token}
Content-Type: application/json

{
  "teamId": 1,
  "addressId": 2,
  "quantity": 1
}

# 响应：orderId=8002, currentNum=2, remainNum=1
```

#### 步骤4: 用户2参团（满3人）

```bash
POST http://localhost:9000/api/groupbuy/team/join
Authorization: Bearer {user2_token}
Content-Type: application/json

{
  "teamId": 1,
  "addressId": 3,
  "quantity": 1
}

# 响应：orderId=8003, currentNum=3, remainNum=0
```

#### 步骤5: 模拟支付回调（3次）

```bash
# 团长支付
POST http://localhost:9000/api/groupbuy/payment/callback?orderId=8001

# 用户1支付
POST http://localhost:9000/api/groupbuy/payment/callback?orderId=8002

# 用户2支付（触发成团）⭐
POST http://localhost:9000/api/groupbuy/payment/callback?orderId=8003
```

**成团后自动执行**:
1. 更新团状态为"已成团"（team_status=1）
2. 更新所有成员状态为"已成团"（status=2）
3. Feign批量更新订单状态为"待发货"（order_status=1）

#### 步骤6: 查询团详情（验证成团）

```bash
GET http://localhost:9000/api/groupbuy/team/1/detail

# 响应：teamStatus=1, teamStatusDesc="已成团"
```

---

### 10.2 社区优先推荐示例

```bash
# 用户A（communityId=10）查询活动1的团列表
GET http://localhost:9000/api/groupbuy/activity/1/teams?communityId=10

# 预期结果：
# 1. communityId=10的团排在前面（本社区优先）⭐
# 2. 其他社区的团排在后面
# 3. 同一优先级内按创建时间倒序
```

---

### 10.3 退出拼团示例

```bash
# 用户在成团前退出
POST http://localhost:9000/api/groupbuy/team/quit?teamId=1
Authorization: Bearer {user_token}

# 响应：
# {
#   "code": 200,
#   "message": "退出成功，已退款"
# }

# 系统自动执行：
# 1. 删除参团记录
# 2. 更新团人数（current_num--）
# 3. 退款到用户余额
# 4. 更新订单状态为"已退款"
```

---

## 11. 常见问题

### 11.1 如何验证团长身份？

**答**: GroupBuyService通过Feign调用UserService的`getUserInfo`接口获取用户信息，检查`role`字段是否为2。

```java
Result<UserInfoDTO> userResult = userServiceClient.getUserInfo(userId);
UserInfoDTO user = userResult.getData();
if (user.getRole() != 2) {
    throw new BusinessException("仅团长可发起拼团");
}
```

### 11.2 如何保证并发安全？

**答**: 使用数据库行锁（SELECT ... FOR UPDATE）代替Redis分布式锁。

```java
// 查询团并加行锁
@Query("SELECT t FROM GroupBuyTeam t WHERE t.teamId = :teamId")
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<GroupBuyTeam> findByIdForUpdate(@Param("teamId") Long teamId);

// 应用场景：
// 1. 用户参团时锁定团记录
// 2. 支付回调时锁定参团记录和团记录
// 3. 定时任务处理过期团时锁定团记录
```

### 11.3 如何保证幂等性？

**答**: 三层幂等性设计（状态检查 + 行锁）。

```java
// 1. 支付回调幂等性
if (member.getStatus() != MemberStatus.UNPAID.getCode()) {
    return;  // 已处理过，直接返回
}

// 2. 成团逻辑幂等性
if (team.getTeamStatus() != TeamStatus.JOINING.getCode()) {
    return;  // 已处理过，直接返回
}

// 3. 定时任务幂等性
if (team.getTeamStatus() != TeamStatus.JOINING.getCode()) {
    return;  // 已处理过，直接返回
}
```

### 11.4 社区优先排序如何实现？

**答**: 使用SQL `ORDER BY CASE` 实现。

```sql
ORDER BY 
  CASE WHEN t.community_id = :communityId THEN 0 ELSE 1 END ASC,
  t.create_time DESC
```

### 11.5 定时任务如何保证异常隔离？

**答**: try-catch捕获单个团的异常，不抛出，继续处理其他团。

```java
for (Long teamId : expiredTeamIds) {
    try {
        refundService.refundExpiredTeam(teamId);
        successCount++;
    } catch (Exception e) {
        log.error("团{}退款失败", teamId, e);
        failCount++;
        // 不抛异常，继续处理其他团 ⭐
    }
}
```

---

## 12. 依赖服务接口

### 12.1 UserService依赖

**必需接口**:
```java
GET /api/user/feign/info/{userId} - 获取用户信息（验证团长身份）
POST /api/account/feign/refund - 退款到用户余额
```

### 12.2 OrderService依赖

**必需接口**（⭐待开发）:
```java
POST /api/order/feign/create - 创建订单
POST /api/order/feign/batchUpdateStatus - 批量更新订单状态
POST /api/order/feign/updateStatus - 更新订单状态
POST /api/order/feign/cancel/{orderId} - 取消订单
```

### 12.3 ProductService依赖

**可选接口**:
```java
GET /api/product/feign/{productId} - 获取商品信息
```

### 12.4 LeaderService依赖

**可选接口**:
```java
GET /api/community/feign/{communityId} - 获取社区信息
```

---

## 13. 版本历史

### v1.0.1 (2025-11-01)

**新功能**:
- ✅ 新增商品详情页拼团接口（`GET /api/groupbuy/product/{productId}/activities`）
- ✅ 支持根据商品ID查询所有拼团活动及团列表
- ✅ 每个活动返回最多10个进行中的团（支持社区优先）
- ✅ 优化商品详情页拼团展示逻辑

**优化改进**:
- 🔧 商品详情页可直接查看并参与拼团
- 🔧 用户无需登录即可浏览拼团信息
- 🔧 支持社区优先排序，提升本社区拼团体验

### v1.0.0 (2025-10-31)

**新功能**:
- ✅ 拼团活动管理（CRUD）
- ✅ 团长发起拼团（v3.0：验证团长身份、自动关联社区）
- ✅ 用户参与拼团（行锁防并发、防重复参团）
- ✅ 支付回调处理（成团检查、幂等性保证）
- ✅ 查询功能（团详情、活动团列表、社区优先排序）
- ✅ 退出拼团（成团前可退、自动退款）
- ✅ 定时任务（过期团自动退款、异常隔离）

**技术亮点**:
- ⭐ 无Redis分布式锁方案（数据库行锁）
- ⭐ 三层幂等性设计（支付回调、成团逻辑、定时任务）
- ⭐ Saga模式跨服务调用（补偿机制）
- ⭐ 社区优先推荐（SQL ORDER BY CASE）
- ⭐ 定时任务异常隔离（单个团失败不影响其他团）

---

## 14. 联系方式

**开发者**: 耿康瑞  
**学号**: 20221204229  
**学校**: 北京城市学院  
**专业**: 软件工程  
**邮箱**: gkr@bcu.edu.cn

**项目地址**: E:\E\BYSJ\community-group-buy-backend\GroupBuyService

**相关文档**:
- [README](../../community-group-buy-backend/GroupBuyService/README.md)
- [系统架构设计](../三级文档（归档）/DESIGN_GroupBuyService.md)
- [开发完成报告](../三级文档（归档）/FINAL_GroupBuyService.md)

---

**文档状态**: ✅ 完成  
**最后更新**: 2025-11-01 18:30  
**版本**: v1.0.1

