# ALIGNMENT - 团长工作台前端开发

**任务名称**: 团长工作台前端开发（佣金模块 + 团长订单）  
**创建日期**: 2025-11-13  
**工作流阶段**: Align（对齐阶段）  
**文档版本**: v1.0

---

## 📋 目录

1. [原始需求](#1-原始需求)
2. [项目上下文分析](#2-项目上下文分析)
3. [需求理解](#3-需求理解)
4. [疑问澄清](#4-疑问澄清)
5. [边界确认](#5-边界确认)
6. [技术实现方案](#6-技术实现方案)
7. [验收标准](#7-验收标准)

---

## 1. 原始需求

根据API文档完成前端团长工作台中的：
1. **佣金模块功能** - 团长查看佣金记录和统计
2. **团长订单相关页面功能** - 团长查看和管理订单

注意API一致性

---

## 2. 项目上下文分析

### 2.1 现有项目结构

```
community-group-buy-frontend/
├── src/
│   ├── api/                  # API接口层
│   │   ├── leader.js         # ✅ 已有佣金API
│   │   ├── order.js          # ❌ 缺少团长订单API
│   │   └── ...
│   ├── views/
│   │   ├── leader/           # 团长页面
│   │   │   ├── LeaderDashboardView.vue  # ✅ 已有工作台（显示佣金统计）
│   │   │   ├── LeaderGroupBuyManageView.vue
│   │   │   ├── LeaderApplyView.vue
│   │   │   ├── LeaderActivityCreateView.vue
│   │   │   └── TeamDetailView.vue
│   │   └── ...
│   ├── router/
│   │   └── index.js          # 路由配置
│   └── stores/
│       └── user.js           # 用户状态管理
└── ...
```

### 2.2 技术栈

**前端框架**:
- Vue 3.5.22 (Composition API)
- Vite 5.2.10
- Element Plus (UI组件库)
- Pinia (状态管理)
- Vue Router (路由管理)
- Axios (HTTP客户端)

**代码规范**:
- 使用 `<script setup>` 语法
- 使用 Composition API
- 组件命名采用 PascalCase
- 文件名采用 PascalCase (如 `LeaderCommissionView.vue`)

### 2.3 现有API分析

#### ✅ 已实现的佣金API (`leader.js`)

```javascript
// 查询佣金记录
export const getMyCommissionRecords = (params) => {
  return request({
    url: '/api/commission/my',
    method: 'GET',
    params  // { leaderId, status, page, limit }
  })
}

// 查询佣金统计
export const getMyCommissionSummary = (leaderId) => {
  return request({
    url: '/api/commission/my/summary',
    method: 'GET',
    params: { leaderId }
  })
}
```

#### ❌ 缺少的团长订单API (`order.js`)

需要添加：
```javascript
// GET /api/order/leader/my - 查询团长订单列表
// GET /api/order/leader/summary - 查询团长订单统计
```

### 2.4 现有页面分析

#### ✅ `LeaderDashboardView.vue` (已有)

**功能**:
- 显示佣金统计（待结算/已结算/累计）
- 显示团点信息
- 快捷入口

**数据来源**:
- 已调用 `getMyCommissionSummary()`
- 数据结构：
  ```javascript
  commissionSummary: {
    pendingCommission: 0,    // 待结算佣金
    settledCommission: 0,    // 已结算佣金
    totalCommission: 0       // 累计佣金
  }
  ```

**结论**: 工作台首页已完成，无需修改

---

## 3. 需求理解

### 3.1 佣金模块需求

#### 页面：`LeaderCommissionView.vue`（新建）

**功能点**:
1. **佣金统计卡片**
   - 待结算佣金
   - 已结算佣金
   - 累计佣金

2. **佣金记录列表**
   - 订单编号
   - 订单金额
   - 佣金比例
   - 佣金金额
   - 结算状态（待结算/已结算）
   - 结算时间
   - 结算批次

3. **筛选功能**
   - 按状态筛选（全部/待结算/已结算）

4. **分页功能**
   - 支持分页查询

**API调用**:
```javascript
// 统计数据
GET /api/commission/my/summary?leaderId=xxx

// 佣金记录列表
GET /api/commission/my?leaderId=xxx&status=0&page=0&limit=10
```

---

### 3.2 团长订单模块需求

#### 页面：`LeaderOrdersView.vue`（新建）

**功能点**:
1. **订单统计卡片**
   - 今日订单数
   - 待发货订单数
   - 配送中订单数
   - 订单总数

2. **订单列表**
   - 订单编号
   - 用户信息
   - 商品信息
   - 订单金额
   - 订单状态
   - 下单时间
   - 操作按钮（查看详情）

3. **状态筛选**
   - 全部
   - 待发货
   - 配送中
   - 已送达

4. **分页功能**
   - 支持分页查询

**API调用**:
```javascript
// 订单统计
GET /api/order/leader/summary?leaderId=xxx

// 订单列表
GET /api/order/leader/my?leaderId=xxx&orderStatus=1&page=0&size=10
```

---

## 4. 疑问澄清

### 4.1 已确认的问题

#### Q1: 团长ID从哪里获取？
**A**: 从 Pinia `userStore` 获取
```javascript
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
const leaderId = userStore.userInfo?.userId  // 团长的userId
```

#### Q2: API Base URL是什么？
**A**: 根据现有项目配置：
- 开发环境：`http://localhost:9000` (API Gateway)
- 已在 `utils/request.js` 中配置

#### Q3: 订单状态枚举如何处理？
**A**: 复用现有 `order.js` 中的枚举：
```javascript
export const ORDER_STATUS = {
  PENDING_PAYMENT: 0,    // 待支付
  PENDING_DELIVERY: 1,   // 待发货
  IN_DELIVERY: 2,        // 配送中
  DELIVERED: 3,          // 已送达
  CANCELLED: 4,          // 已取消
  REFUNDING: 5,          // 退款中
  REFUNDED: 6            // 已退款
}
```

#### Q4: 佣金状态如何定义？
**A**: 根据API文档：
```javascript
export const COMMISSION_STATUS = {
  PENDING: 0,   // 待结算
  SETTLED: 1,   // 已结算
  FAILED: 2     // 结算失败
}
```

#### Q5: 路由路径如何命名？
**A**: 遵循现有规范：
```javascript
/leader/commission  // 佣金管理
/leader/orders      // 团长订单
```

---

## 5. 边界确认

### 5.1 本次开发范围

✅ **包含**:
1. 新建 `LeaderCommissionView.vue` - 佣金管理页面
2. 新建 `LeaderOrdersView.vue` - 团长订单页面
3. 在 `order.js` 中添加团长订单API
4. 在 `router/index.js` 中添加路由配置
5. 在 `LeaderDashboardView.vue` 中添加快捷入口按钮

❌ **不包含**:
1. ❌ 修改后端API（后端已完成）
2. ❌ 订单状态修改功能（待后续开发）
3. ❌ 佣金提现功能（待后续开发）
4. ❌ 订单导出功能（待后续开发）
5. ❌ 图表可视化功能（待后续开发）

### 5.2 技术约束

1. **必须使用 Vue 3 Composition API** (`<script setup>`)
2. **必须使用 Element Plus 组件库**
3. **必须遵循现有代码风格和命名规范**
4. **必须复用现有的 `MainLayout` 组件**
5. **必须使用现有的 `request` 工具函数**
6. **必须添加路由权限守卫** (`requireAuth: true, requiresLeader: true`)

### 5.3 数据依赖

1. **用户登录状态** - 从 `userStore` 获取
2. **团长身份** - `userStore.isLeader === true`
3. **团长ID** - `userStore.userInfo.userId`

---

## 6. 技术实现方案

### 6.1 文件清单

需要新建/修改的文件：

```
1. src/api/order.js                       # 修改 - 添加团长订单API
2. src/views/leader/LeaderCommissionView.vue  # 新建 - 佣金管理页面
3. src/views/leader/LeaderOrdersView.vue      # 新建 - 团长订单页面
4. src/router/index.js                     # 修改 - 添加路由
5. src/views/leader/LeaderDashboardView.vue   # 修改 - 添加入口按钮
```

### 6.2 组件结构设计

#### 6.2.1 `LeaderCommissionView.vue`

```vue
<template>
  <MainLayout>
    <!-- 页面头部 -->
    <PageHeader title="佣金管理" />
    
    <!-- 统计卡片 -->
    <StatisticsCards :summary="commissionSummary" />
    
    <!-- 筛选器 -->
    <FilterBar @filter="handleFilter" />
    
    <!-- 佣金记录表格 -->
    <CommissionTable :data="commissionList" :loading="loading" />
    
    <!-- 分页器 -->
    <Pagination @page-change="handlePageChange" />
  </MainLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyCommissionSummary, getMyCommissionRecords } from '@/api/leader'

// 数据加载
const userStore = useUserStore()
const leaderId = userStore.userInfo?.userId

// 加载佣金数据
const loadCommissionData = async () => {
  // ...
}

onMounted(() => {
  loadCommissionData()
})
</script>
```

#### 6.2.2 `LeaderOrdersView.vue`

```vue
<template>
  <MainLayout>
    <!-- 页面头部 -->
    <PageHeader title="我的订单" />
    
    <!-- 统计卡片 -->
    <OrderStatisticsCards :summary="orderSummary" />
    
    <!-- 状态筛选标签 -->
    <StatusTabs @change="handleStatusChange" />
    
    <!-- 订单列表 -->
    <OrderList :data="orderList" :loading="loading" />
    
    <!-- 分页器 -->
    <Pagination @page-change="handlePageChange" />
  </MainLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getLeaderOrders, getLeaderOrdersSummary } from '@/api/order'

// ...
</script>
```

### 6.3 API接口设计

#### `order.js` 新增方法

```javascript
/**
 * 查询团长订单列表
 * @param {Object} params - { leaderId, page, size, orderStatus }
 */
export const getLeaderOrders = (params) => {
  return request({
    url: '/api/order/leader/my',
    method: 'GET',
    params
  })
}

/**
 * 查询团长订单统计
 * @param {Number} leaderId - 团长ID
 */
export const getLeaderOrdersSummary = (leaderId) => {
  return request({
    url: '/api/order/leader/summary',
    method: 'GET',
    params: { leaderId }
  })
}
```

### 6.4 路由配置

```javascript
// router/index.js
{
  path: '/leader/commission',
  name: 'leaderCommission',
  component: () => import('../views/leader/LeaderCommissionView.vue'),
  meta: { 
    title: '佣金管理', 
    requireAuth: true, 
    requiresLeader: true 
  }
},
{
  path: '/leader/orders',
  name: 'leaderOrders',
  component: () => import('../views/leader/LeaderOrdersView.vue'),
  meta: { 
    title: '我的订单', 
    requireAuth: true, 
    requiresLeader: true 
  }
}
```

---

## 7. 验收标准

### 7.1 功能验收

#### 佣金管理页面
- [ ] ✅ 能够正确显示佣金统计（待结算/已结算/累计）
- [ ] ✅ 能够正确显示佣金记录列表
- [ ] ✅ 支持按状态筛选（全部/待结算/已结算）
- [ ] ✅ 支持分页查询
- [ ] ✅ 数据加载状态显示（Loading动画）
- [ ] ✅ 空数据状态显示（Empty组件）
- [ ] ✅ 错误处理（网络错误提示）

#### 团长订单页面
- [ ] ✅ 能够正确显示订单统计（今日/待发货/配送中/总数）
- [ ] ✅ 能够正确显示订单列表
- [ ] ✅ 支持按状态筛选（全部/待发货/配送中/已送达）
- [ ] ✅ 支持分页查询
- [ ] ✅ 订单状态正确显示（使用Tag组件）
- [ ] ✅ 数据加载状态显示
- [ ] ✅ 空数据状态显示
- [ ] ✅ 错误处理

#### 路由和导航
- [ ] ✅ 路由配置正确
- [ ] ✅ 权限守卫生效（需要登录+团长身份）
- [ ] ✅ 面包屑导航正确
- [ ] ✅ 工作台首页有入口按钮

### 7.2 代码质量

- [ ] ✅ 代码符合Vue 3 Composition API规范
- [ ] ✅ 使用 `<script setup>` 语法
- [ ] ✅ 组件命名符合规范
- [ ] ✅ API调用统一使用 `async/await`
- [ ] ✅ 错误处理完善（try-catch）
- [ ] ✅ 代码注释清晰
- [ ] ✅ 无console.log残留
- [ ] ✅ 代码格式统一

### 7.3 UI/UX

- [ ] ✅ 页面布局美观
- [ ] ✅ 响应式设计（支持移动端）
- [ ] ✅ 加载动画流畅
- [ ] ✅ 空数据提示友好
- [ ] ✅ 错误提示明确
- [ ] ✅ 交互反馈及时

### 7.4 性能

- [ ] ✅ 首屏加载时间 < 2秒
- [ ] ✅ 页面切换流畅无卡顿
- [ ] ✅ 数据请求合并优化
- [ ] ✅ 组件懒加载

---

## 8. 风险识别

### 8.1 技术风险

| 风险项 | 等级 | 缓解措施 |
|--------|------|---------|
| API接口数据格式不匹配 | 🟡 中 | 严格按照API文档开发，先测试接口 |
| 团长ID获取失败 | 🟡 中 | 添加错误处理，跳转登录页 |
| 分页参数不一致 | 🟢 低 | 使用现有分页组件 |

### 8.2 时间风险

| 任务 | 预估时间 | 实际时间 | 风险 |
|------|---------|---------|------|
| API层开发 | 30分钟 | - | 🟢 低 |
| 佣金管理页面 | 2小时 | - | 🟢 低 |
| 团长订单页面 | 2小时 | - | 🟢 低 |
| 路由配置 | 20分钟 | - | 🟢 低 |
| 测试验证 | 1小时 | - | 🟡 中 |
| **总计** | **约6小时** | - | 🟢 低 |

---

## 9. 下一步行动

### 9.1 等待确认的问题

**需要用户确认**:
1. ✅ 是否需要在工作台首页添加快捷入口？
2. ✅ 是否需要订单详情页面？（还是复用现有的订单详情）
3. ✅ 佣金记录是否需要导出功能？
4. ✅ 是否需要添加订单搜索功能？

**默认方案**（如果不回复，将按此执行）:
1. ✅ 在工作台首页添加快捷入口按钮
2. ✅ 复用现有的订单详情页面
3. ❌ 暂不开发导出功能
4. ❌ 暂不开发搜索功能

### 9.2 进入下一阶段

✅ **ALIGNMENT完成**，等待用户确认后进入 **ARCHITECT（架构阶段）**

---

**文档状态**: ✅ 需求对齐完成  
**等待**: 用户确认问题和边界  
**下一步**: Architect（架构设计）
