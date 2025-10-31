# 社区团购用户端 - PC端开发说明

## 📋 项目概述

本项目为社区团购系统的用户端前端应用，采用 **PC端桌面版** 设计，提供完整的购物体验。

### 技术栈

- **框架**: Vue 3 (Composition API)
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP请求**: Axios
- **构建工具**: Vite

---

## 🎯 已完成功能

### 1. 统一顶部导航栏（Header.vue）

位置：`src/components/common/Header.vue`

**功能特性：**
- ✅ Logo和品牌名称
- ✅ 搜索框（支持关键词搜索）
- ✅ 购物车图标（显示商品数量徽章）
- ✅ 用户登录/未登录状态切换
- ✅ 用户下拉菜单（个人中心、订单、拼团、退出登录）
- ✅ 水平导航菜单（首页、商品分类、拼团活动、个人中心）
- ✅ 固定定位（fixed），确保不遮挡内容

**特点：**
- 高度：120px（包含顶部栏60px + 导航菜单60px）
- 响应式设计
- 统一样式风格

---

### 2. 主布局组件（MainLayout.vue）

位置：`src/components/common/MainLayout.vue`

**功能特性：**
- ✅ 整合Header组件
- ✅ 主内容区（自动添加padding-top避免被Header遮挡）
- ✅ 统一底部（Footer）
- ✅ 确保结构清晰，不相互遮挡

**使用方法：**
```vue
<template>
  <MainLayout>
    <!-- 页面内容 -->
  </MainLayout>
</template>

<script setup>
import MainLayout from '@/components/common/MainLayout.vue'
</script>
```

---

### 3. 购物车管理（Cart Store）

位置：`src/stores/cart.js`

**功能特性：**
- ✅ 添加商品到购物车
- ✅ 更新商品数量
- ✅ 删除商品
- ✅ 商品选中状态管理
- ✅ 全选/取消全选
- ✅ 计算总价格和数量
- ✅ 本地存储（localStorage持久化）
- ✅ 清空购物车
- ✅ 获取选中商品

**数据一致性保证：**
- 所有购物车操作自动同步到localStorage
- 页面刷新后自动恢复购物车状态
- 实时计算总价和数量

---

### 4. 商品卡片组件（ProductCard.vue）

位置：`src/components/common/ProductCard.vue`

**功能特性：**
- ✅ 商品图片展示
- ✅ 商品名称、价格
- ✅ 拼团价格显示（如果有）
- ✅ 库存状态
- ✅ 添加到购物车按钮
- ✅ 点击跳转到商品详情

---

### 5. 页面列表

#### 5.1 首页（HomeView.vue）

路径：`/` 或 `/home`

**功能特性：**
- ✅ Banner轮播图
- ✅ 商品分类展示（点击跳转到分类筛选）
- ✅ 热门商品推荐
- ✅ 精选商品推荐
- ✅ API集成：
  - `GET /api/product/category/list` - 获取分类
  - `GET /api/product/hot` - 获取热门商品
  - `GET /api/product/recommend` - 获取推荐商品

#### 5.2 商品列表页（ProductListView.vue）

路径：`/products`

**功能特性：**
- ✅ 左侧分类筛选侧边栏（固定定位）
- ✅ 搜索框（支持关键词搜索）
- ✅ 排序选项（最新、价格升序、价格降序）
- ✅ 商品网格展示
- ✅ 分页功能
- ✅ URL参数同步（支持直接分享链接）
- ✅ API集成：
  - `GET /api/product/category/list` - 获取分类
  - `GET /api/product/list` - 获取商品列表

**URL参数：**
- `?categoryId=1` - 分类筛选
- `?keyword=苹果` - 关键词搜索
- `?sort=price_asc` - 排序方式
- `?page=2` - 页码

#### 5.3 商品详情页（ProductDetailView.vue）

路径：`/products/:id`

**功能特性：**
- ✅ 商品大图展示
- ✅ 商品基本信息（名称、价格、库存）
- ✅ 拼团价/原价对比
- ✅ 商品描述
- ✅ 数量选择器
- ✅ 加入购物车按钮
- ✅ 立即购买按钮
- ✅ 商品详细信息表格
- ✅ 同类推荐商品
- ✅ API集成：
  - `GET /api/product/{id}` - 获取商品详情
  - `GET /api/product/recommend` - 获取推荐商品

#### 5.4 购物车页（CartView.vue）

路径：`/cart`

**功能特性：**
- ✅ 购物车商品列表
- ✅ 商品选中/取消选中
- ✅ 全选/取消全选
- ✅ 数量修改
- ✅ 删除商品
- ✅ 实时计算小计和总价
- ✅ 结算栏（固定底部）
- ✅ 清空选中商品
- ✅ 去结算按钮（TODO：跳转到订单确认页）
- ✅ 空购物车状态展示

**结构特点：**
- 表格式布局（商品信息、单价、数量、小计、操作）
- 底部结算栏固定定位
- 响应式设计

#### 5.5 我的订单（MyOrdersView.vue）【占位页面】

路径：`/user/orders`

**状态：** 占位页面，功能开发中

**计划功能：**
- 订单列表
- 订单搜索和筛选
- 订单详情
- 订单支付
- 物流跟踪
- 退款申请

#### 5.6 拼团活动列表（GroupBuyListView.vue）【占位页面】

路径：`/groupbuy`

**状态：** 占位页面，功能开发中

**计划功能：**
- 查看所有拼团活动
- 参与拼团
- 拼团进度和倒计时
- 社区定位推荐

**业务规则（v3.0）：**
- 只有团长可以发起拼团活动
- 拼团成功后以拼团价配送
- 拼团失败自动退款

#### 5.7 我的拼团（MyGroupBuysView.vue）【占位页面】

路径：`/user/groups`

**状态：** 占位页面，功能开发中

**计划功能：**
- 查看参与的拼团活动
- 拼团成员列表
- 拼团状态跟踪
- 拼团历史记录

---

## 🗂️ 目录结构

```
src/
├── components/
│   └── common/
│       ├── Header.vue          # 统一顶部导航栏
│       ├── MainLayout.vue      # 主布局组件
│       └── ProductCard.vue     # 商品卡片组件
├── stores/
│   ├── user.js                 # 用户状态管理
│   └── cart.js                 # 购物车状态管理
├── views/
│   ├── HomeView.vue            # 首页
│   ├── CartView.vue            # 购物车
│   ├── LoginView.vue           # 登录
│   ├── product/
│   │   ├── ProductListView.vue      # 商品列表
│   │   └── ProductDetailView.vue    # 商品详情
│   ├── groupbuy/
│   │   ├── GroupBuyListView.vue     # 拼团活动列表（占位）
│   │   └── MyGroupBuysView.vue      # 我的拼团（占位）
│   ├── order/
│   │   └── MyOrdersView.vue         # 我的订单（占位）
│   └── user/
│       ├── ProfileView.vue          # 个人中心
│       ├── UserInfoView.vue         # 个人信息
│       ├── AddressView.vue          # 收货地址
│       ├── BalanceView.vue          # 我的余额
│       └── FeedbackView.vue         # 意见反馈
├── router/
│   └── index.js                # 路由配置
├── utils/
│   ├── request.js              # Axios封装
│   └── formatter.js            # 格式化工具
└── api/
    └── user.js                 # 用户API
```

---

## 🔌 API集成状态

### ✅ 已集成的API

1. **商品分类 API**
   - `GET /api/product/category/list` - 获取分类列表
   - `GET /api/product/category/tree` - 获取分类树

2. **商品 API**
   - `GET /api/product/list` - 获取商品列表（支持分页、搜索、排序）
   - `GET /api/product/{id}` - 获取商品详情
   - `GET /api/product/hot` - 获取热门商品
   - `GET /api/product/recommend` - 获取推荐商品

3. **用户 API**
   - `POST /api/user/login` - 用户登录
   - `GET /api/user/info/{userId}` - 获取用户信息
   - `GET /api/account/{userId}` - 获取账户信息

### 🚧 待集成的API

1. **订单 API**（OrderService开发中）
2. **拼团 API**（GroupBuyService开发中）
3. **支付 API**（PaymentService开发中）

---

## 📐 设计原则

### 1. 数据一致性

- **购物车状态**：使用Pinia Store + localStorage双重保障
- **用户状态**：登录信息持久化到localStorage
- **字段命名**：严格按照API文档使用驼峰命名（productId, productName, coverImg等）

### 2. 页面架构

- **固定Header**：高度120px，z-index: 1000
- **主内容区**：padding-top: 120px，避免被Header遮挡
- **固定Footer**：底部信息栏
- **侧边栏**：使用sticky定位，跟随滚动
- **结算栏**：使用sticky定位，固定在底部

### 3. 响应式设计

虽然主要针对PC端，但仍保留基本的响应式支持：
- 最大宽度：1400px
- 最小宽度：1024px（推荐）
- 网格布局：使用CSS Grid自动适应

### 4. 用户体验

- **加载状态**：所有API请求都有loading提示
- **错误提示**：使用ElMessage统一提示
- **空状态**：使用ElEmpty组件
- **确认操作**：删除等重要操作使用ElMessageBox确认
- **路由守卫**：需要登录的页面自动跳转到登录页

---

## 🚀 使用指南

### 1. 启动项目

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

### 2. 配置API地址

编辑 `src/utils/request.js`：

```javascript
const request = axios.create({
  baseURL: 'http://localhost:9000', // API网关地址
  timeout: 10000
})
```

### 3. 测试账号

（根据实际情况填写测试账号）

---

## 📝 开发规范

### 1. 组件命名

- **页面组件**：使用 `XxxView.vue` 格式
- **公共组件**：使用 `XxxComponent.vue` 或简短的名称
- **布局组件**：使用 `XxxLayout.vue` 格式

### 2. API字段命名

严格按照API文档使用字段名称：

| 前端字段 | API字段 | 说明 |
|---------|---------|------|
| productId | productId | 商品ID |
| productName | productName | 商品名称 |
| coverImg | coverImg | 封面图片 |
| groupPrice | groupPrice | 拼团价格 |

### 3. 路由命名

- **路径**：使用小写字母 + 中划线（kebab-case）
- **名称**：使用驼峰命名（camelCase）

### 4. Store使用

```javascript
// 导入Store
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'

// 在setup中使用
const cartStore = useCartStore()
const userStore = useUserStore()

// 访问状态
console.log(cartStore.totalCount)
console.log(userStore.isLogin)

// 调用方法
cartStore.addItem(product, 1)
userStore.logout()
```

---

## 🔄 后续开发计划

### 短期（1-2周）

1. **订单功能**
   - 订单确认页面
   - 订单支付页面
   - 订单列表页面
   - 订单详情页面

2. **拼团功能**
   - 拼团活动列表
   - 拼团详情页面
   - 参与拼团流程

### 中期（2-4周）

1. **支付功能**
   - 支付方式选择
   - 支付结果页面
   - 支付记录查询

2. **配送功能**
   - 配送状态跟踪
   - 配送路线查看

### 长期（1-2个月）

1. **团长功能**
   - 团长工作台
   - 发起拼团
   - 团员管理
   - 配送管理
   - 佣金管理

2. **社区功能**
   - 社区申请
   - 社区列表
   - 附近社区推荐

---

## 🐛 已知问题

1. **购物车同步**：多标签页打开时，购物车状态可能不同步
   - **解决方案**：使用 `storage` 事件监听localStorage变化

2. **图片加载**：部分商品图片可能加载失败
   - **解决方案**：添加图片加载失败的默认占位图

---

## 📞 技术支持

**开发者**: 耿康瑞  
**项目**: 社区团购系统毕业设计  
**文档版本**: v1.0  
**更新日期**: 2025-10-31

---

## 📄 相关文档

- [API_ProductService.md](../../docs/社区团购系统/二级文档（参考）/API_ProductService.md)
- [API_UserService.md](../../docs/社区团购系统/二级文档（参考）/API_UserService.md)
- [LeaderService_API文档.md](../../docs/社区团购系统/二级文档（参考）/LeaderService_API文档.md)
- [拼团逻辑优化方案.md](../../docs/社区团购系统/二级文档（参考）/拼团逻辑优化方案.md)

---

**祝开发顺利！🎉**

