# 社区团购前端（用户端）使用说明

**版本**: v1.0.0  
**更新日期**: 2025-10-12  
**端口**: 5174  
**技术栈**: Vue 3 + Vant 4 + Pinia + Axios

---

## 📱 项目简介

这是社区团购系统的**用户端（C端）前端**，面向普通用户、团长使用。采用移动端UI设计，适配手机浏览器和微信小程序。

---

## 🚀 快速开始

### 1. 安装依赖

```bash
cd community-group-buy-frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问：`http://localhost:5174`

### 3. 构建生产版本

```bash
npm run build
```

---

## 📋 功能清单

### ✅ 已完成功能

#### 1. 用户认证
- [x] 用户登录
- [x] 用户注册（页面占位）
- [x] 退出登录
- [x] 登录状态持久化

#### 2. 个人中心主页
- [x] 用户信息展示（头像、姓名、手机号）
- [x] 账户余额显示
- [x] 功能菜单导航
- [x] 未登录状态处理

#### 3. 个人信息管理
- [x] 查看个人信息
- [x] 编辑真实姓名
- [x] 修改手机号
- [x] 查看角色和状态

#### 4. 收货地址管理
- [x] 地址列表展示
- [x] 新增收货地址
- [x] 编辑收货地址
- [x] 删除收货地址
- [x] 设置默认地址

#### 5. 我的余额
- [x] 账户余额展示
- [x] 累计充值/消费/返佣统计
- [x] 充值按钮（功能占位）
- [x] 提现按钮（功能占位）
- [x] 交易记录（功能占位）

#### 6. 意见反馈
- [x] 提交反馈（功能问题、商品问题等）
- [x] 我的反馈列表
- [x] 反馈状态查看
- [x] 管理员回复查看

### 🚧 功能占位页面

以下功能显示占位页面，提示"功能开发中"：

- [ ] 首页（商品展示、拼团活动）
- [ ] 我的订单
- [ ] 我的拼团
- [ ] 关于我们
- [ ] 充值功能
- [ ] 提现功能
- [ ] 交易记录

---

## 🎯 页面路由

| 路由路径 | 页面名称 | 是否需要登录 | 是否显示底部导航 |
|---------|---------|-------------|----------------|
| `/home` | 首页 | ❌ | ✅ |
| `/profile` | 个人中心 | ❌ | ✅ |
| `/login` | 登录 | ❌ | ❌ |
| `/user/info` | 个人信息 | ✅ | ❌ |
| `/user/address` | 收货地址 | ✅ | ❌ |
| `/user/balance` | 我的余额 | ✅ | ❌ |
| `/user/feedback` | 意见反馈 | ✅ | ❌ |
| `/user/orders` | 我的订单 | ✅ | ❌ |
| `/user/groups` | 我的拼团 | ✅ | ❌ |
| `/user/about` | 关于我们 | ❌ | ❌ |

---

## 🧪 测试账号

### 测试账号1（管理员）
```
用户名：1
密码：123123
角色：管理员
```

### 测试账号2（普通用户）
可以通过管理后台创建新用户进行测试。

---

## 📦 项目结构

```
community-group-buy-frontend/
├── public/                    # 静态资源
├── src/
│   ├── api/                   # API接口封装
│   │   └── user.js            # 用户相关API
│   ├── assets/                # 资源文件
│   ├── router/                # 路由配置
│   │   └── index.js
│   ├── stores/                # 状态管理
│   │   └── user.js            # 用户状态
│   ├── utils/                 # 工具函数
│   │   └── request.js         # Axios配置
│   ├── views/                 # 页面组件
│   │   ├── user/              # 用户相关页面
│   │   │   ├── ProfileView.vue      # 个人中心主页
│   │   │   ├── UserInfoView.vue     # 个人信息
│   │   │   ├── AddressView.vue      # 收货地址
│   │   │   ├── BalanceView.vue      # 我的余额
│   │   │   ├── FeedbackView.vue     # 意见反馈
│   │   │   └── PlaceholderView.vue  # 占位页面
│   │   ├── HomeView.vue       # 首页
│   │   └── LoginView.vue      # 登录页
│   ├── App.vue                # 根组件
│   └── main.js                # 入口文件
├── vite.config.js             # Vite配置
├── package.json               # 项目配置
└── USAGE.md                   # 使用说明
```

---

## 🔌 API接口

### 后端服务

- **UserService**: `http://localhost:8061`
- **代理配置**: `/api` → `http://localhost:8061`

### 已集成接口

| 功能 | 接口路径 | 方法 | 状态 |
|------|---------|------|------|
| 用户登录 | `/api/user/login` | POST | ✅ |
| 用户注册 | `/api/user/register` | POST | ✅ |
| 获取用户信息 | `/api/user/info/:userId` | GET | ✅ |
| 更新用户信息 | `/api/user/update/:userId` | PUT | ✅ |
| 获取地址列表 | `/api/user/address/:userId` | GET | ✅ |
| 新增地址 | `/api/user/address` | POST | ✅ |
| 更新地址 | `/api/user/address/:addressId` | PUT | ✅ |
| 删除地址 | `/api/user/address/:addressId` | DELETE | ✅ |
| 设置默认地址 | `/api/user/address/:userId/default/:addressId` | PUT | ✅ |
| 获取账户信息 | `/api/user/account/:userId` | GET | ✅ |
| 提交反馈 | `/api/user/feedback` | POST | ✅ |
| 获取我的反馈 | `/api/user/feedback/:userId` | GET | ✅ |

---

## 💡 核心功能说明

### 1. 用户状态管理（Pinia）

```javascript
// stores/user.js
const userStore = useUserStore()

// 登录
await userStore.login({ username, password })

// 登出
userStore.logout()

// 更新用户信息
await userStore.updateUserInfo()

// 获取登录状态
userStore.isLogin

// 获取用户信息
userStore.userInfo
```

---

### 2. API请求封装

```javascript
// api/user.js
import request from '@/utils/request'

// 示例：获取用户信息
export const getUserInfo = (userId) => {
  return request({
    url: `/api/user/info/${userId}`,
    method: 'GET'
  })
}
```

---

### 3. 路由守卫

- **登录验证**: 需要登录的页面会自动跳转到登录页
- **页面标题**: 根据路由meta自动设置页面标题
- **底部导航**: 首页和个人中心显示底部导航栏

```javascript
// router/index.js
{
  path: '/user/info',
  meta: { 
    title: '个人信息',
    requireAuth: true  // 需要登录
  }
}
```

---

### 4. Axios拦截器

**请求拦截器**：
- 自动添加JWT Token到请求头

**响应拦截器**：
- 统一处理后端返回格式
- 401错误自动跳转登录
- 显示错误提示

---

## 🎨 UI组件库（Vant 4）

项目使用**Vant 4**移动端组件库，主要组件：

### 常用组件
- `van-nav-bar` - 导航栏
- `van-tabbar` - 底部导航
- `van-cell` / `van-cell-group` - 单元格
- `van-form` / `van-field` - 表单
- `van-button` - 按钮
- `van-popup` - 弹出层
- `van-empty` - 空状态
- `van-list` - 列表
- `van-image` - 图片
- `van-tag` - 标签

### 反馈组件
- `van-toast` - 轻提示
- `van-dialog` - 对话框
- `van-notify` - 通知栏
- `van-loading` - 加载

---

## 🔐 登录流程

```
1. 用户输入用户名密码
   ↓
2. 调用 userStore.login(loginForm)
   ↓
3. 后端返回 { token, userInfo }
   ↓
4. 保存到 localStorage 和 Pinia store
   ↓
5. 跳转到个人中心
```

---

## 🐛 常见问题

### 1. 登录后刷新页面丢失状态

**原因**: 状态没有持久化  
**解决**: 已使用localStorage保存token和用户信息

### 2. 跨域问题

**原因**: 前端端口5174，后端端口8061  
**解决**: 已配置Vite代理 `/api` → `http://localhost:8061`

### 3. 地址列表为空

**原因**: 用户没有添加地址  
**解决**: 点击"新增地址"按钮添加

### 4. 余额显示0.00

**原因**: 新用户默认余额为0  
**解决**: 正常现象，充值功能待开发

---

## 📸 功能截图说明

### 个人中心主页
- 用户信息卡片（头像、姓名、手机号）
- 服务菜单（个人信息、地址、余额）
- 订单菜单（我的订单、拼团）
- 其他菜单（反馈、关于）
- 退出登录按钮

### 个人信息
- 头像展示
- 用户名（不可编辑）
- 真实姓名（可编辑）
- 手机号（可编辑）
- 角色（只读）
- 状态（只读）

### 收货地址
- 地址列表
- 默认地址标签
- 新增地址按钮
- 编辑/删除操作
- 设置默认地址

### 我的余额
- 余额卡片（渐变背景）
- 累计统计（充值、消费、返佣）
- 充值/提现按钮
- 交易记录（占位）

### 意见反馈
- 提交反馈（类型选择、内容输入）
- 我的反馈列表
- 反馈状态标签
- 管理员回复展示

---

## 🚀 开发建议

### 1. 添加新页面

```javascript
// 1. 创建组件
// src/views/NewPage.vue

// 2. 添加路由
// src/router/index.js
{
  path: '/new-page',
  name: 'newPage',
  component: () => import('../views/NewPage.vue'),
  meta: { title: '新页面', requireAuth: true }
}

// 3. 添加导航
// 在ProfileView.vue或其他页面添加跳转按钮
```

---

### 2. 添加新API

```javascript
// src/api/user.js
export const newApi = (data) => {
  return request({
    url: '/api/new-endpoint',
    method: 'POST',
    data
  })
}
```

---

### 3. 使用Vant组件

```vue
<template>
  <van-button type="primary" @click="handleClick">
    点击按钮
  </van-button>
</template>

<script setup>
import { showToast } from 'vant'

const handleClick = () => {
  showToast('点击成功')
}
</script>
```

---

## 📝 后续开发计划

### 高优先级
- [ ] 首页商品展示
- [ ] 商品详情页
- [ ] 购物车功能
- [ ] 下单流程
- [ ] 订单列表和详情

### 中优先级
- [ ] 拼团功能
- [ ] 支付功能
- [ ] 充值提现
- [ ] 交易记录

### 低优先级
- [ ] 消息通知
- [ ] 优惠券
- [ ] 积分系统
- [ ] 分享功能

---

## 🔗 相关文档

- **后端API文档**: `docs/社区团购系统/API_UserService.md`
- **管理后台**: `community-group-buy-admin/`
- **后端服务**: `community-group-buy-backend/UserService/`

---

## 📞 技术支持

如有问题，请提交Issue或联系开发团队。

---

**文档版本**: v1.0.0  
**更新时间**: 2025-10-12  
**作者**: AI Assistant  
**状态**: ✅ 个人中心功能已完成

