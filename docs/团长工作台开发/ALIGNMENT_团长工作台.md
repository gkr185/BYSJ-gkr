# ALIGNMENT - 团长工作台（佣金结算 + 团长订单）

**任务名称**: 团长工作台 - 佣金结算与团长订单功能  
**创建日期**: 2025-11-13  
**项目**: 社区团购系统  
**6A工作流阶段**: Align（对齐阶段）  
**文档版本**: v1.0

---

## 📋 目录

1. [原始需求](#1-原始需求)
2. [项目上下文分析](#2-项目上下文分析)
3. [后端完成情况分析](#3-后端完成情况分析)
4. [功能需求细化](#4-功能需求细化)
5. [技术实现方案](#5-技术实现方案)
6. [待办事项清单](#6-待办事项清单)
7. [验收标准](#7-验收标准)

---

## 1. 原始需求

### 1.1 需求描述

开发团长工作台的两个核心功能模块：

1. **佣金结算模块**
   - 团长查看自己的佣金记录
   - 团长查看佣金统计（待结算/已结算/累计佣金）
   - 管理员查看所有待结算佣金
   - 管理员手动触发佣金结算

2. **团长订单模块**
   - 团长查看自己负责的所有订单
   - 按订单状态筛选（待发货/配送中/已送达等）
   - 订单详情查看
   - 订单统计（今日订单/订单总数）

### 1.2 用户角色

- **团长（role=2）**: 查看自己的佣金和订单
- **管理员（role=3）**: 查看所有佣金和订单

### 1.3 业务场景

**场景1：团长查看佣金**
```
团长登录 → 进入个人中心 → 点击"佣金管理" → 查看佣金记录和统计
```

**场景2：团长查看订单**
```
团长登录 → 进入个人中心 → 点击"我的订单" → 查看所有由自己负责的订单
```

**场景3：管理员结算佣金**
```
管理员登录 → 进入管理后台 → 点击"佣金管理" → 查看待结算佣金 → 点击"手动结算"
```

---

## 2. 项目上下文分析

### 2.1 技术栈

**后端**:
- Spring Boot 3.2.3
- Spring Data JPA
- MySQL 8.0.36
- OpenFeign（微服务调用）

**前端**:
- Vue 3.5.22
- Element Plus
- Pinia（状态管理）
- Axios（HTTP请求）

### 2.2 微服务架构

```
┌─────────────────────────────────────────────────────────┐
│                    用户端 (5173)                         │
│                    管理端 (5174)                         │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP/REST
                     ▼
         ┌─────────────────────────────────────┐
         │        API Gateway (9000)            │
         └────────────────┬────────────────────┘
                          │
    ┌─────────────────────┼─────────────────────┐
    │                     │                     │
    ▼                     ▼                     ▼
┌─────────┐          ┌─────────┐          ┌─────────┐
│ Leader  │          │  Order  │          │  User   │
│ Service │◄─────────┤ Service │          │ Service │
│  8068   │  Feign   │  8065   │          │  8061   │
└─────────┘          └─────────┘          └─────────┘
     │
     │ 定时任务：每月1号凌晨2点
     │ 调用UserService增加余额
     ▼
```

### 2.3 数据库设计

#### leader_service_db（团长服务数据库）

**commission_record（佣金记录表）**:
```sql
CREATE TABLE commission_record (
  record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  leader_id BIGINT NOT NULL COMMENT '团长ID',
  leader_name VARCHAR(50) COMMENT '团长姓名',
  order_id BIGINT NOT NULL COMMENT '订单ID',
  order_amount DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  commission_rate DECIMAL(5,2) NOT NULL COMMENT '佣金比例(%)',
  commission_amount DECIMAL(10,2) NOT NULL COMMENT '佣金金额',
  status INT DEFAULT 0 COMMENT '状态(0-待结算 1-已结算 2-结算失败)',
  settled_at DATETIME COMMENT '结算时间',
  settlement_batch VARCHAR(50) COMMENT '结算批次号',
  remark TEXT COMMENT '备注',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_leader_id (leader_id),
  INDEX idx_order_id (order_id),
  INDEX idx_status (status),
  INDEX idx_settlement_batch (settlement_batch)
);
```

#### order_service_db（订单服务数据库）

**order_main（订单主表）**:
```sql
CREATE TABLE order_main (
  order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_sn VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  leader_id BIGINT NOT NULL COMMENT '团长ID', -- ⭐关键字段
  address_id BIGINT NOT NULL COMMENT '收货地址ID',
  total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  order_status INT DEFAULT 0 COMMENT '订单状态',
  pay_status INT DEFAULT 0 COMMENT '支付状态',
  activity_id BIGINT COMMENT '拼团活动ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_leader_id (leader_id), -- ⭐团长订单查询索引
  INDEX idx_order_status (order_status),
  INDEX idx_create_time (create_time)
);
```

---

## 3. 后端完成情况分析

### 3.1 ✅ 佣金管理功能（LeaderService）- 100%完成

#### 已实现的API接口

| 接口路径 | 方法 | 功能 | 状态 |
|---------|------|------|------|
| `/api/commission/my` | GET | 查询团长佣金记录 | ✅ 完成 |
| `/api/commission/my/summary` | GET | 查询团长佣金统计 | ✅ 完成 |
| `/api/commission/pending` | GET | 查询待结算佣金（管理员） | ✅ 完成 |
| `/api/commission/batch/{batch}` | GET | 查询结算批次（管理员） | ✅ 完成 |
| `/api/commission/settle` | POST | 手动结算佣金（管理员） | ✅ 完成 |
| `/feign/commission/generate` | POST | 生成佣金记录（Feign接口） | ✅ 完成 |

#### 核心业务逻辑

**文件**: `LeaderService/src/main/java/com/bcu/edu/service/CommissionService.java`

**已实现的核心方法**:
1. ✅ `generateCommission()` - 生成佣金记录（订单完成时调用）
2. ✅ `calculateCommissionAmount()` - 计算佣金（订单金额 × 佣金比例 / 100）
3. ✅ `settleCommissions()` - 批量结算佣金
4. ✅ `getCommissionsByLeader()` - 查询团长佣金记录
5. ✅ `getPendingCommissionByLeader()` - 查询待结算佣金总额
6. ✅ `getSettledCommissionByLeader()` - 查询已结算佣金总额

**定时任务**:
```java
// CommissionSettlementTask.java
@Scheduled(cron = "0 0 2 1 * ?") // 每月1号凌晨2点执行
public void settleMonthlyCommissions() {
    String settlementBatch = generateSettlementBatch();
    commissionService.settleCommissions(settlementBatch);
}
```

#### ⚠️ 待完善项

1. **UserService集成** - TODO（第137行）
   ```java
   // 需要调用UserService为团长增加余额
   // Result<Void> result = userServiceClient.addBalance(leaderId, totalCommission, settlementBatch);
   ```
   - **状态**: 代码已写好，但被注释掉
   - **原因**: 等待UserService提供增加余额接口
   - **影响**: 不影响佣金记录查询，但无法实际结算到余额

---

### 3.2 ⚠️ 团长订单功能（OrderService）- 60%完成

#### 已实现的Repository方法

**文件**: `OrderService/src/main/java/com/bcu/edu/repository/OrderMainRepository.java`

```java
// ✅ 已有Repository方法
Page<OrderMain> findByLeaderIdOrderByCreateTimeDesc(Long leaderId, Pageable pageable);
long countByLeaderId(Long leaderId);
```

#### ❌ 缺少的Controller接口

**文件**: `OrderService/src/main/java/com/bcu/edu/controller/OrderController.java`

**现有接口**（仅支持用户查询自己的订单）:
- ✅ `/api/order/my` - 查询我的订单（userId）
- ✅ `/api/order/{orderId}` - 查询订单详情
- ✅ `/api/order/cancel/{orderId}` - 取消订单
- ✅ `/api/order/confirm/{orderId}` - 确认收货

**❌ 缺少的接口**（团长查询自己负责的订单）:
- ❌ `/api/order/leader/my` - 查询团长订单列表
- ❌ `/api/order/leader/summary` - 查询团长订单统计

#### Service层方法状态

**文件**: `OrderService/src/main/java/com/bcu/edu/service/OrderService.java`

- ✅ `getMyOrders(userId, page, size)` - 查询用户订单
- ❌ `getLeaderOrders(leaderId, page, size)` - **缺少**团长订单查询
- ❌ `getLeaderOrdersSummary(leaderId)` - **缺少**团长订单统计

---

## 4. 功能需求细化

### 4.1 佣金结算模块（前端开发）

#### 4.1.1 团长端 - 佣金查询页面

**页面路径**: `/leader/commission`  
**组件名称**: `LeaderCommissionView.vue`

**页面结构**:
```
┌─────────────────────────────────────────┐
│          我的佣金                        │
├─────────────────────────────────────────┤
│  📊 统计卡片                             │
│  ┌─────────┬─────────┬─────────┐       │
│  │待结算   │已结算   │累计佣金 │       │
│  │¥ 100.00│¥ 500.00│¥ 600.00│       │
│  └─────────┴─────────┴─────────┘       │
├─────────────────────────────────────────┤
│  📋 佣金记录列表                         │
│  ┌───────────────────────────────────┐ │
│  │ 订单编号  │ 订单金额 │ 佣金金额 │...│ │
│  │ 20251101001│¥100.00 │¥10.00  │...│ │
│  └───────────────────────────────────┘ │
│  [上一页] [1] [2] [3] [下一页]          │
└─────────────────────────────────────────┘
```

**功能点**:
1. ✅ 调用 `GET /api/commission/my/summary?leaderId=xxx` 获取统计数据
2. ✅ 调用 `GET /api/commission/my?leaderId=xxx` 获取佣金记录
3. ✅ 支持分页显示
4. ✅ 按状态筛选（待结算/已结算/全部）
5. ✅ 显示佣金计算公式（订单金额 × 佣金比例）
6. ✅ 显示结算批次号（已结算记录）

**API调用**:
```javascript
// 获取佣金统计
export function getCommissionSummary(leaderId) {
  return request({
    url: `/api/commission/my/summary`,
    method: 'get',
    params: { leaderId }
  })
}

// 获取佣金记录
export function getCommissionList(leaderId) {
  return request({
    url: `/api/commission/my`,
    method: 'get',
    params: { leaderId }
  })
}
```

#### 4.1.2 管理端 - 佣金管理页面

**页面路径**: `/admin/commission`  
**组件名称**: `CommissionManageView.vue`

**页面结构**:
```
┌─────────────────────────────────────────┐
│          佣金管理                        │
├─────────────────────────────────────────┤
│  [待结算佣金] [结算历史]                 │
├─────────────────────────────────────────┤
│  📋 待结算佣金列表                       │
│  ┌───────────────────────────────────┐ │
│  │团长 │订单 │订单金额│佣金金额│...│   │
│  │张三 │001 │¥100   │¥10    │...│   │
│  └───────────────────────────────────┘ │
│  [手动结算]                              │
└─────────────────────────────────────────┘
```

**功能点**:
1. ✅ 调用 `GET /api/commission/pending` 获取待结算佣金
2. ✅ 调用 `POST /api/commission/settle` 手动触发结算
3. ✅ 调用 `GET /api/commission/batch/{batch}` 查询结算历史
4. ✅ 显示结算结果（成功/失败条数）
5. ✅ 确认对话框（手动结算前确认）

---

### 4.2 团长订单模块（前后端开发）

#### 4.2.1 后端开发 - OrderService补充

**需要添加的Controller接口**:

**文件**: `OrderService/src/main/java/com/bcu/edu/controller/OrderController.java`

```java
/**
 * 【团长】查询我的订单列表
 * GET /api/order/leader/my?leaderId=xxx&page=0&size=10
 */
@GetMapping("/leader/my")
@Operation(summary = "查询团长订单", description = "团长查询自己负责的所有订单")
public Result<PageResult<OrderVO>> getLeaderOrders(
    @RequestParam Long leaderId,
    @RequestParam(defaultValue = "0") Integer page,
    @RequestParam(defaultValue = "10") Integer size,
    @RequestParam(required = false) Integer orderStatus
) {
    PageResult<OrderVO> result = orderService.getLeaderOrders(leaderId, page, size, orderStatus);
    return Result.success(result);
}

/**
 * 【团长】查询订单统计
 * GET /api/order/leader/summary?leaderId=xxx
 */
@GetMapping("/leader/summary")
@Operation(summary = "查询团长订单统计", description = "统计团长订单数量和金额")
public Result<Map<String, Object>> getLeaderOrdersSummary(
    @RequestParam Long leaderId
) {
    Map<String, Object> summary = orderService.getLeaderOrdersSummary(leaderId);
    return Result.success(summary);
}
```

**需要添加的Service方法**:

**文件**: `OrderService/src/main/java/com/bcu/edu/service/OrderService.java`

```java
/**
 * 查询团长订单列表（分页）
 */
public PageResult<OrderVO> getLeaderOrders(Long leaderId, Integer page, Integer size, Integer orderStatus) {
    Pageable pageable = PageRequest.of(page, size);
    Page<OrderMain> orderPage;
    
    if (orderStatus != null) {
        // 按状态筛选
        orderPage = orderMainRepository.findByLeaderIdAndOrderStatusOrderByCreateTimeDesc(
            leaderId, orderStatus, pageable);
    } else {
        // 查询全部
        orderPage = orderMainRepository.findByLeaderIdOrderByCreateTimeDesc(leaderId, pageable);
    }
    
    List<OrderVO> orderVOs = orderPage.getContent().stream()
        .map(this::convertToOrderVO)
        .collect(Collectors.toList());
    
    return PageResult.of(orderVOs, orderPage.getTotalElements());
}

/**
 * 查询团长订单统计
 */
public Map<String, Object> getLeaderOrdersSummary(Long leaderId) {
    long totalCount = orderMainRepository.countByLeaderId(leaderId);
    
    // 统计今日订单数量
    LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
    long todayCount = orderMainRepository.countByLeaderIdAndCreateTimeGreaterThanEqual(
        leaderId, todayStart);
    
    // 统计待发货订单数
    long pendingCount = orderMainRepository.countByLeaderIdAndOrderStatus(leaderId, 1);
    
    // 统计配送中订单数
    long deliveringCount = orderMainRepository.countByLeaderIdAndOrderStatus(leaderId, 2);
    
    Map<String, Object> summary = new HashMap<>();
    summary.put("totalCount", totalCount);
    summary.put("todayCount", todayCount);
    summary.put("pendingCount", pendingCount);
    summary.put("deliveringCount", deliveringCount);
    
    return summary;
}
```

**需要添加的Repository方法**:

**文件**: `OrderService/src/main/java/com/bcu/edu/repository/OrderMainRepository.java`

```java
/**
 * 按状态查询团长订单
 */
Page<OrderMain> findByLeaderIdAndOrderStatusOrderByCreateTimeDesc(
    Long leaderId, Integer orderStatus, Pageable pageable);

/**
 * 统计团长指定状态的订单数量
 */
long countByLeaderIdAndOrderStatus(Long leaderId, Integer orderStatus);

/**
 * 统计团长今日订单数量
 */
@Query("SELECT COUNT(o) FROM OrderMain o WHERE o.leaderId = :leaderId " +
       "AND o.createTime >= :startTime")
long countByLeaderIdAndCreateTimeGreaterThanEqual(
    @Param("leaderId") Long leaderId, 
    @Param("startTime") LocalDateTime startTime);
```

#### 4.2.2 前端开发 - 团长订单页面

**页面路径**: `/leader/orders`  
**组件名称**: `LeaderOrdersView.vue`

**页面结构**:
```
┌─────────────────────────────────────────┐
│          我的订单                        │
├─────────────────────────────────────────┤
│  📊 统计卡片                             │
│  ┌─────────┬─────────┬─────────┐       │
│  │今日订单 │待发货   │配送中   │       │
│  │  10单  │  5单   │  3单    │       │
│  └─────────┴─────────┴─────────┘       │
├─────────────────────────────────────────┤
│  [全部] [待发货] [配送中] [已送达]      │
├─────────────────────────────────────────┤
│  📋 订单列表                             │
│  ┌───────────────────────────────────┐ │
│  │订单编号│用户│金额│状态│操作│       │
│  │20251101│张三│¥100│待发货│[详情]│   │
│  └───────────────────────────────────┘ │
│  [上一页] [1] [2] [3] [下一页]          │
└─────────────────────────────────────────┘
```

**功能点**:
1. ✅ 调用 `GET /api/order/leader/summary?leaderId=xxx` 获取统计数据
2. ✅ 调用 `GET /api/order/leader/my?leaderId=xxx&page=0&size=10` 获取订单列表
3. ✅ 支持按状态筛选（全部/待发货/配送中/已送达）
4. ✅ 支持分页显示
5. ✅ 点击"详情"查看订单详情
6. ✅ 显示用户信息（姓名、手机号）
7. ✅ 显示配送地址

**API调用**:
```javascript
// 获取团长订单统计
export function getLeaderOrdersSummary(leaderId) {
  return request({
    url: `/api/order/leader/summary`,
    method: 'get',
    params: { leaderId }
  })
}

// 获取团长订单列表
export function getLeaderOrders(leaderId, page, size, orderStatus) {
  return request({
    url: `/api/order/leader/my`,
    method: 'get',
    params: { leaderId, page, size, orderStatus }
  })
}
```

---

## 5. 技术实现方案

### 5.1 佣金管理前端实现

#### 5.1.1 API层

**文件**: `community-group-buy-frontend/src/api/commission.js`

```javascript
import request from '@/utils/request'

// 获取团长佣金统计
export function getCommissionSummary(leaderId) {
  return request({
    url: `/api/commission/my/summary`,
    method: 'get',
    params: { leaderId }
  })
}

// 获取团长佣金记录
export function getCommissionList(leaderId) {
  return request({
    url: `/api/commission/my`,
    method: 'get',
    params: { leaderId }
  })
}

// 【管理员】获取待结算佣金
export function getPendingCommissions() {
  return request({
    url: `/api/commission/pending`,
    method: 'get'
  })
}

// 【管理员】手动结算佣金
export function settleCommissions() {
  return request({
    url: `/api/commission/settle`,
    method: 'post'
  })
}

// 【管理员】查询结算批次
export function getCommissionsByBatch(settlementBatch) {
  return request({
    url: `/api/commission/batch/${settlementBatch}`,
    method: 'get'
  })
}
```

#### 5.1.2 页面组件

**文件**: `community-group-buy-frontend/src/views/leader/CommissionView.vue`

核心功能：
1. 使用 `el-statistic` 展示统计数据
2. 使用 `el-table` 展示佣金记录
3. 使用 `el-tag` 显示状态（待结算/已结算）
4. 使用 `el-pagination` 实现分页

---

### 5.2 团长订单前后端实现

#### 5.2.1 后端实现步骤

1. **Repository层** - 添加查询方法
2. **Service层** - 实现业务逻辑
3. **Controller层** - 暴露API接口
4. **测试** - Swagger测试接口

#### 5.2.2 前端实现步骤

1. **API层** - 创建order.js的团长订单接口
2. **组件层** - 创建LeaderOrdersView.vue
3. **路由层** - 添加路由配置
4. **导航层** - 在个人中心添加入口

---

## 6. 待办事项清单

### 6.1 🔴 高优先级（必须完成）

#### 后端开发

- [ ] **OrderService补充团长订单接口**
  - [ ] 添加Repository方法（2个）
  - [ ] 添加Service方法（2个）
  - [ ] 添加Controller接口（2个）
  - [ ] Swagger测试

- [ ] **UserService补充余额增加接口**（佣金结算需要）
  - [ ] 添加 `POST /feign/user/{userId}/addBalance` 接口
  - [ ] LeaderService解除TODO注释

#### 前端开发

- [ ] **佣金管理页面（团长端）**
  - [ ] 创建 `src/api/commission.js`
  - [ ] 创建 `src/views/leader/CommissionView.vue`
  - [ ] 添加路由配置
  - [ ] 在个人中心添加入口

- [ ] **团长订单页面（团长端）**
  - [ ] 创建 `src/api/leader.js`（团长订单接口）
  - [ ] 创建 `src/views/leader/OrdersView.vue`
  - [ ] 添加路由配置
  - [ ] 在个人中心添加入口

- [ ] **佣金管理页面（管理端）**
  - [ ] 创建 `community-group-buy-admin/src/views/CommissionManageView.vue`
  - [ ] 添加路由配置
  - [ ] 在侧边栏添加菜单

### 6.2 🟡 中优先级（推荐完成）

- [ ] 佣金记录导出Excel功能
- [ ] 团长订单高级筛选（按日期、金额范围）
- [ ] 订单状态流转图展示

### 6.3 🟢 低优先级（可选）

- [ ] 佣金走势图表（Echarts）
- [ ] 订单数据可视化
- [ ] 实时订单提醒（WebSocket）

---

## 7. 验收标准

### 7.1 佣金管理功能

#### 团长端

- [ ] ✅ 能够查看佣金统计（待结算/已结算/累计）
- [ ] ✅ 能够查看佣金记录列表
- [ ] ✅ 佣金记录显示完整信息（订单编号、金额、佣金、状态等）
- [ ] ✅ 支持按状态筛选（待结算/已结算）
- [ ] ✅ 页面响应迅速（加载时间<2秒）

#### 管理端

- [ ] ✅ 能够查看所有待结算佣金
- [ ] ✅ 能够手动触发佣金结算
- [ ] ✅ 结算成功后显示结算结果
- [ ] ✅ 能够查询历史结算批次
- [ ] ✅ 结算前有确认对话框

---

### 7.2 团长订单功能

#### 后端接口

- [ ] ✅ `GET /api/order/leader/my` 接口正常工作
- [ ] ✅ `GET /api/order/leader/summary` 接口正常工作
- [ ] ✅ Swagger文档正确生成
- [ ] ✅ 接口响应时间<500ms
- [ ] ✅ 分页功能正常

#### 前端页面

- [ ] ✅ 能够查看订单统计（今日/待发货/配送中）
- [ ] ✅ 能够查看订单列表
- [ ] ✅ 支持按状态筛选
- [ ] ✅ 支持分页显示
- [ ] ✅ 点击详情能够查看订单详情
- [ ] ✅ 页面响应迅速（加载时间<2秒）

---

### 7.3 集成测试

- [ ] ✅ 佣金生成流程完整（订单完成→生成佣金记录）
- [ ] ✅ 佣金结算流程完整（手动结算→更新状态→增加余额）
- [ ] ✅ 团长订单查询准确（只显示自己负责的订单）
- [ ] ✅ 权限控制正确（团长只能查看自己的数据）
- [ ] ✅ 跨浏览器兼容（Chrome, Firefox, Edge）

---

## 📝 附录

### A. 订单状态枚举

| 状态码 | 状态名称 | 说明 |
|--------|---------|------|
| 0 | 待支付 | 订单创建，等待支付 |
| 1 | 待发货 | 已支付，等待团长发货 |
| 2 | 配送中 | 团长已发货，正在配送 |
| 3 | 已送达 | 用户已确认收货 |
| 4 | 已取消 | 用户或系统取消订单 |
| 5 | 退款中 | 正在处理退款 |
| 6 | 已退款 | 退款完成 |
| 7 | 已过期 | 订单超时未支付 |

### B. 佣金状态枚举

| 状态码 | 状态名称 | 说明 |
|--------|---------|------|
| 0 | 待结算 | 佣金记录已生成，等待结算 |
| 1 | 已结算 | 佣金已结算到团长余额 |
| 2 | 结算失败 | 结算过程中发生错误 |

### C. 佣金计算公式

```
佣金金额 = 订单金额 × 佣金比例 / 100

示例：
订单金额 = 100.00元
佣金比例 = 10%
佣金金额 = 100.00 × 10 / 100 = 10.00元
```

---

**文档状态**: ✅ 需求对齐完成  
**下一步**: 进入Architect（架构阶段）- 生成DESIGN文档  
**预计开发时间**: 后端1天 + 前端2天 = 3天  
**风险评估**: 🟢 低风险（功能需求明确，技术栈成熟）
