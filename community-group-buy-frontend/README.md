# 社区团购系统 - 用户端前端（PC网页版）

## 项目简介

社区团购系统的用户端前端，采用**PC网页版**设计，使用 Vue 3 + Element Plus 构建。

## 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **Element Plus** - Vue 3 UI组件库
- **Pinia** - 状态管理
- **Axios** - HTTP客户端
- **Vite** - 构建工具

## 快速开始

### 安装依赖
```bash
npm install
```

### 启动开发服务器
```bash
npm run dev
```

访问：http://localhost:5174

### 构建生产版本
```bash
npm run build
```

## 测试账号

- 用户名：1
- 密码：123123

## 主要功能

### ✅ 已完成
- 用户登录/登出
- 个人中心主页
- 个人信息管理
- 收货地址管理
- 我的余额查看
- 意见反馈提交

### 🚧 功能占位
- 首页
- 我的订单
- 我的拼团
- 关于我们

## 项目结构

```
src/
├── api/          # API接口
├── router/       # 路由配置
├── stores/       # 状态管理
├── utils/        # 工具函数
└── views/        # 页面组件
    ├── user/     # 用户相关页面
    ├── HomeView.vue
    └── LoginView.vue
```

## 开发说明

- 后端服务：http://localhost:8061
- 所有页面使用简洁布局，不添加复杂样式
- 使用Element Plus组件，保持界面统一

## 相关文档

- 详细使用说明：USAGE.md
- 后端API文档：docs/社区团购系统/API_UserService.md

