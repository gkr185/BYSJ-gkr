# ALIGNMENT - 团长端界面开发

**任务名称**: 团长端界面原型开发（合并到用户端）  
**创建日期**: 2025-10-28  
**文档版本**: v1.0  
**项目**: 社区团购系统 - 用户端前端扩展

---

## 1. 原始需求

用户要求：
> 横向开发PC团长端界面原型，页面设计符合拼团逻辑v3.0，模拟数据符合数据库设计文档v3.0

**核心诉求**：
- 开发团长专属功能模块
- 符合拼团v3.0逻辑（仅团长可发起拼团）
- 符合数据库v3.0设计（社区机制）
- 使用Mock数据进行原型演示

---

## 2. 项目上下文分析

### 2.1 现有项目结构

**项目名称**: `community-group-buy-frontend`  
**技术栈**:
- Vue 3.5.22 (Composition API)
- Vite 6.0.7
- Element Plus (UI组件库)
- Pinia 3.0.3 (状态管理)
- Vue Router 4.5.1
- Axios (HTTP请求)

**已完成功能**:
- ✅ 用户端15个页面（100%完成）
- ✅ 完整的用户认证系统（JWT）
- ✅ 拼团v2.0逻辑（三层架构）
- ✅ Mock数据支持（商品、拼团、订单）
- ✅ 路由守卫和权限控制

**代码规范**:
- Composition API (`<script setup>`)
- TypeScript风格的JSDoc注释
- Element Plus组件库
- 统一的TopNav导航栏
- Mock数据存放在 `src/mock/` 目录
- API封装在 `src/api/` 目录

### 2.2 现有目录结构

```
community-group-buy-frontend/
├── src/
│   ├── views/
│   │   ├── HomeView.vue           # 首页
│   │   ├── LoginView.vue          # 登录
│   │   ├── product/               # 商品模块
│   │   │   ├── ProductListView.vue
│   │   │   └── ProductDetailView.vue
│   │   ├── groupbuy/              # 拼团模块（v2.0三层架构）
│   │   │   ├── ActivityView.vue   # 活动详情
│   │   │   ├── TeamView.vue       # 团详情
│   │   │   └── MyGroupBuyView.vue # 我的拼团
│   │   ├── order/                 # 订单模块
│   │   │   ├── CartView.vue
│   │   │   ├── OrderConfirmView.vue
│   │   │   └── OrderListView.vue
│   │   ├── payment/
│   │   │   └── PaymentView.vue    # 支付页面
│   │   └── user/                  # 用户模块
│   │       ├── ProfileView.vue    # 个人中心
│   │       ├── UserInfoView.vue   # 个人信息
│   │       ├── AddressView.vue    # 收货地址
│   │       ├── BalanceView.vue    # 我的余额
│   │       └── FeedbackView.vue   # 意见反馈
│   ├── components/
│   │   └── common/
│   │       ├── TopNav.vue         # 统一导航栏
│   │       └── ProductCard.vue    # 商品卡片
│   ├── stores/
│   │   └── user.js                # 用户状态管理
│   ├── api/
│   │   ├── user.js                # 用户API
│   │   ├── groupbuy.js            # 拼团API
│   │   └── payment.js             # 支付API
│   ├── mock/
│   │   ├── products.js            # 商品Mock数据
│   │   ├── groupbuy.js            # 拼团Mock数据
│   │   ├── orders.js              # 订单Mock数据
│   │   └── payment.js             # 支付Mock数据
│   └── router/
│       └── index.js               # 路由配置
```

### 2.3 拼团v3.0核心逻辑

根据《拼团逻辑优化方案.md v3.0》：

**核心变更**：
1. ⭐ **仅团长可发起拼团** (`role=2`)
   - `launcher_id = leader_id`（发起人即团长）
   - 普通用户只能参团

2. ⭐ **引入社区机制**
   - 团长归属社区 (`community_id`)
   - 团自动关联团长的社区
   - 优先推荐本社区的团（非强制绑定）

3. ⭐ **三层架构**
   ```
   活动 (group_buy) 
     ↓ 1:N
   团实例 (group_buy_team)
     ↓ 1:N
   参团记录 (group_buy_member)
     ↓ 1:1
   订单 (order_main)
   ```

4. ❌ **废弃固定绑定**
   - 废弃 `group_member` 表（固定团员绑定）
   - 改用动态参团记录 (`group_buy_member`)

### 2.4 数据库v3.0设计

**新增表**（v3.0）：
- `community` - 社区表
- `community_application` - 社区申请表

**关键字段**：
- `sys_user.role`: 1-普通用户，2-团长，3-管理员
- `sys_user.community_id`: 归属社区（v3.0新增）
- `group_leader_store.community_id`: 团点归属社区（v3.0新增）
- `group_buy_team.launcher_id`: 发起人（v3.0仅团长可发起）
- `group_buy_team.leader_id`: 归属团长（v3.0中=launcher_id）
- `group_buy_team.community_id`: 团归属社区（v3.0新增）

---

## 3. 任务边界确认

### 3.1 任务范围

**✅ 包含**：
1. 在现有用户端基础上扩展团长专属功能
2. 创建 `src/views/leader/` 目录
3. 开发5个团长专属页面
4. 配置团长路由守卫（`role === 2`）
5. 准备团长相关Mock数据
6. 个人中心增加团长管理入口

**❌ 不包含**：
1. 修改现有用户端页面逻辑
2. 后端API实现（仅Mock数据）
3. 独立开发团长端应用（采用合并方案）
4. 真实数据库对接（原型阶段）
5. Dijkstra路径规划算法实现（仅展示参考路径）

### 3.2 技术约束

**必须遵循**：
- Vue 3 Composition API (`<script setup>`)
- Element Plus组件库
- Pinia状态管理
- 现有路由结构
- 现有Mock数据格式
- TopNav导航栏组件复用

**禁止使用**：
- Options API
- 其他UI框架
- 直接操作DOM

---

## 4. 需求理解

### 4.1 业务逻辑理解

根据《团长端业务逻辑文档.md v1.1》：

**团长角色定位**：
- 社区服务点负责人
- `role=2`
- 必须绑定社区（`community_id`）

**核心职责**：
1. **发起拼团** - 仅团长可发起（v3.0核心）
2. **团员管理** - 管理团的参与成员（非固定绑定）
3. **订单管理** - 查看归属自己的所有订单
4. **配送管理** - 查看配送路径参考
5. **佣金查询** - 查看佣金明细和余额

**与普通用户的关系**：
- 团长也是用户，可以浏览商品、参团、下单
- 个人中心根据 `role=2` 显示团长专属菜单

### 4.2 功能模块理解

**团长专属页面**（5个）：

1. **团长工作台** (`LeaderDashboard.vue`)
   - 数据概览（订单数、拼团数、佣金）
   - 快捷操作入口
   - 待处理事项

2. **发起拼团** (`LaunchGroupBuy.vue`)
   - 选择拼团活动
   - 发起团实例
   - 可选择是否参团
   - 生成分享链接

3. **团员管理** (`MemberManage.vue`)
   - 查看团长发起的所有团
   - 查看每个团的成员列表
   - 团状态监控

4. **配送管理** (`DeliveryManage.vue`)
   - 查看待配送订单
   - 展示配送路径参考（贪心算法结果）
   - 标记配送状态

5. **佣金中心** (`CommissionView.vue`)
   - 佣金明细
   - 佣金余额
   - 提现说明（线下提现）

### 4.3 数据流理解

**用户登录流程**：
```
用户登录 → 获取userInfo（含role） → Pinia存储
  ↓
role=1 → 仅显示用户功能
role=2 → 显示用户功能 + 团长功能
role=3 → 跳转管理后台（不在本任务范围）
```

**团长发起拼团流程**（v3.0）：
```
1. 团长点击"发起拼团"
2. 选择活动（activity_id）
3. 验证权限（role=2）
4. 创建团实例（team_id）
   - launcher_id = leader_id（团长）
   - community_id = 团长的社区ID
5. 可选：团长参团
6. 生成分享链接（team_no）
```

**团员管理数据流**：
```
查询团长发起的团 → group_buy_team (WHERE launcher_id = ?)
  ↓
查询每个团的成员 → group_buy_member (WHERE team_id = ?)
  ↓
关联用户信息 → sys_user
```

---

## 5. 疑问澄清

### 5.1 已确认事项（基于文档和项目分析）

**Q1: 团长端是否独立开发？**
✅ **已确认**：合并到用户端，团长是"高级用户"

**Q2: Mock数据格式？**
✅ **已确认**：
- 参考现有Mock数据格式
- 符合数据库v3.0设计
- 存放在 `src/mock/leader.js`

**Q3: 路由守卫如何实现？**
✅ **已确认**：
```javascript
router.beforeEach((to, from, next) => {
  if (to.meta.requiresLeader && userStore.role !== 2) {
    ElMessage.warning('仅团长可访问')
    next('/profile')
  }
})
```

**Q4: 拼团v3.0逻辑如何体现？**
✅ **已确认**：
- 发起拼团时检查 `role=2`
- 团自动关联 `community_id`
- 显示社区信息

### 5.2 需要用户确认的问题

**Q1: 配送管理的路径规划算法**
- ❓ **问题**：是否需要实现Dijkstra算法？
- 💡 **建议**：原型阶段仅展示Mock的路径参考，不实现复杂算法
- 🎯 **决策**：等待用户确认

**Q2: 佣金提现功能**
- ❓ **问题**：是否需要实现线上提现流程？
- 💡 **建议**：仅显示余额和明细，提示"联系客服线下提现"
- 🎯 **决策**：等待用户确认

**Q3: 社区申请流程**
- ❓ **问题**：团长端是否包含"申请新社区"功能？
- 💡 **建议**：暂不包含，由管理端负责社区审核
- 🎯 **决策**：等待用户确认

**Q4: 订单管理范围**
- ❓ **问题**：团长查看订单是"所有归属订单"还是"仅拼团订单"？
- 💡 **建议**：查看所有归属订单（`order_main.leader_id = ?`）
- 🎯 **决策**：等待用户确认

---

## 6. 技术实现方案（初步）

### 6.1 目录结构扩展

```
src/
├── views/
│   └── leader/                    # 团长专属模块（新增）
│       ├── LeaderDashboard.vue    # 团长工作台
│       ├── LaunchGroupBuy.vue     # 发起拼团
│       ├── MemberManage.vue       # 团员管理
│       ├── DeliveryManage.vue     # 配送管理
│       └── CommissionView.vue     # 佣金中心
├── api/
│   └── leader.js                  # 团长API（新增）
├── mock/
│   └── leader.js                  # 团长Mock数据（新增）
└── router/
    └── index.js                   # 扩展路由配置
```

### 6.2 路由配置（扩展）

```javascript
// 团长专属路由（新增）
{
  path: '/leader',
  meta: { requiresLeader: true },
  children: [
    { path: 'dashboard', component: LeaderDashboard },
    { path: 'launch', component: LaunchGroupBuy },
    { path: 'members', component: MemberManage },
    { path: 'delivery', component: DeliveryManage },
    { path: 'commission', component: CommissionView }
  ]
}
```

### 6.3 用户状态扩展

```javascript
// stores/user.js（扩展）
const userInfo = ref({
  userId: 1001,
  username: 'leaderUser',
  role: 2,                    // 团长角色
  communityId: 1,             // 归属社区（v3.0新增）
  communityName: '幸福社区',   // 社区名称（v3.0新增）
  // ... 其他字段
})

// 计算属性
const isLeader = computed(() => userInfo.value?.role === 2)
```

### 6.4 Mock数据结构（示例）

```javascript
// mock/leader.js
export const mockLeaderDashboard = {
  todayOrders: {
    newOrders: 15,
    toDeliver: 23,
    delivering: 8,
    todayCommission: 125.50
  },
  activeTeams: [
    {
      teamId: 5001,
      teamNo: 'T20251027001',
      activityName: '苹果3人团',
      currentNum: 2,
      requiredNum: 3,
      remainingTime: '23小时'
    }
  ],
  pendingTasks: [
    '5个订单待发货',
    '2个拼团即将过期'
  ]
}
```

---

## 7. 验收标准

### 7.1 功能验收

- [ ] 5个团长专属页面创建完成
- [ ] 路由守卫正确拦截非团长用户
- [ ] 个人中心显示团长管理入口（`role=2`时）
- [ ] Mock数据符合数据库v3.0设计
- [ ] 发起拼团流程符合v3.0逻辑
- [ ] 团员管理显示动态参团记录

### 7.2 代码质量

- [ ] 遵循Composition API规范
- [ ] 使用Element Plus组件
- [ ] 代码注释完整
- [ ] 无ESLint错误
- [ ] TopNav导航栏复用

### 7.3 文档质量

- [ ] Mock数据文档完整
- [ ] 路由配置文档清晰
- [ ] API接口文档（Mock）

---

## 8. 风险评估

### 8.1 技术风险

| 风险 | 概率 | 影响 | 应对策略 |
|------|------|------|---------|
| Mock数据结构与后端不一致 | 中 | 中 | 严格参考数据库v3.0设计 |
| 路由守卫逻辑冲突 | 低 | 中 | 测试所有路由场景 |
| Element Plus组件兼容性 | 低 | 低 | 使用稳定版本 |

### 8.2 进度风险

| 风险 | 概率 | 影响 | 应对策略 |
|------|------|------|---------|
| 需求理解偏差 | 中 | 高 | 及时与用户确认 |
| Mock数据准备时间长 | 低 | 中 | 复用现有数据格式 |

---

## 9. 项目特性规范对齐

### 9.1 与现有项目的一致性

✅ **技术栈一致**：Vue 3 + Vite + Element Plus + Pinia  
✅ **代码风格一致**：Composition API + `<script setup>`  
✅ **组件复用**：TopNav、ProductCard  
✅ **Mock数据格式一致**：参考现有格式  
✅ **路由守卫模式一致**：扩展现有守卫逻辑

### 9.2 与业务逻辑的一致性

✅ **符合拼团v3.0逻辑**：仅团长可发起拼团  
✅ **符合数据库v3.0设计**：社区机制、三层架构  
✅ **符合团长端业务逻辑文档**：5个核心功能模块  
✅ **符合角色定位**：团长是"高级用户"

---

## 10. 下一步行动

### 10.1 等待用户确认

请用户确认以下疑问（第5.2节）：
1. ❓ 配送管理是否实现Dijkstra算法？
2. ❓ 佣金提现是否需要线上流程？
3. ❓ 是否包含社区申请功能？
4. ❓ 订单管理范围确认

### 10.2 确认后执行

1. 创建 `CONSENSUS_团长端界面开发.md`
2. 创建 `DESIGN_团长端界面开发.md`
3. 创建 `TASK_团长端界面开发.md`
4. 开始开发

---

**文档状态**: ✅ 对齐完成，等待用户确认  
**创建日期**: 2025-10-28  
**更新日期**: 2025-10-28  
**版本**: v1.0

