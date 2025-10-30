# API Gateway Service

## 📋 服务概述

**服务名称**: gateway-service  
**端口**: 9000  
**功能**: API网关统一入口，提供路由转发、JWT鉴权、CORS处理、负载均衡等功能  
**版本**: v1.0.0  
**开发日期**: 2025-10-30

---

## 🏗️ 架构说明

### 功能特性

- ✅ **统一路由**: 所有前端请求通过网关统一入口（9000端口）
- ✅ **JWT鉴权**: 网关层统一验证JWT Token，后端服务无需重复验证
- ✅ **CORS处理**: 网关层统一配置跨域，后端服务无需配置
- ✅ **负载均衡**: 从Consul动态获取服务实例，自动负载均衡
- ✅ **服务发现**: 集成Consul服务注册与发现
- ✅ **请求日志**: 全局日志过滤器记录所有请求
- ✅ **用户透传**: 将用户信息（userId, username, role）通过请求头传递给后端服务
- ✅ **Swagger聚合**: 聚合各微服务API文档（待实现）

### 路由规则

| 前端请求 | 网关路由 | 后端服务 | 端口 |
|---------|---------|---------|------|
| `/api/user/**` | → | UserService | 8061 |
| `/api/product/**` | → | ProductService | 8062 |
| `/api/groupbuy/**` | → | GroupBuyService | 8063 |
| `/api/order/**` | → | OrderService | 8065 |
| `/api/payment/**` | → | PaymentService | 8066 |
| `/api/leader/**` | → | LeaderService | 8068 |
| `/api/delivery/**` | → | DeliveryService | 8067 |

---

## 🚀 启动步骤

### 前置条件

1. **启动Consul服务注册中心**
```bash
# 下载并启动Consul（开发模式）
consul agent -dev
# 访问 http://localhost:8500 确认启动成功
```

2. **启动UserService**
```bash
cd UserService
mvn spring-boot:run
# 等待服务注册到Consul
```

### 启动Gateway

#### 方式1：Maven启动
```bash
cd gateway-service
mvn clean spring-boot:run
```

#### 方式2：IDEA启动
```
1. 打开 GatewayApplication.java
2. 右键 Run 'GatewayApplication'
```

### 验证启动

访问以下地址确认服务正常：

- **健康检查**: http://localhost:9000/actuator/health
- **Consul控制台**: http://localhost:8500/ui （查看gateway-service是否已注册）

---

## 🧪 测试验证

### 1. 测试白名单接口（无需Token）

```bash
# 用户注册
curl -X POST http://localhost:9000/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "realName": "测试用户",
    "phone": "13800138000",
    "role": 0
  }'

# 用户登录
curl -X POST http://localhost:9000/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

### 2. 测试需要鉴权的接口

```bash
# 获取用户信息（需要Token）
curl -X GET http://localhost:9000/api/user/info/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. 前端测试

#### 用户端（端口5173）
```bash
cd community-group-buy-frontend
npm run dev
# 访问 http://localhost:5173
# 测试登录、用户信息、地址管理等功能
```

#### 管理端（端口5174）
```bash
cd community-group-buy-admin
npm run dev
# 访问 http://localhost:5174
# 测试用户管理、反馈管理、日志管理等功能
```

---

## 📝 配置说明

### JWT配置

```yaml
jwt:
  secret: bcu-community-group-buy-system-jwt-secret-key-2025-must-be-at-least-256-bits-long-for-hs256-algorithm
  expiration: 604800000  # 7天
```

**注意**: JWT密钥必须与UserService保持一致！

### 白名单配置

无需JWT鉴权的接口：

```yaml
gateway:
  whitelist:
    - /api/user/register      # 用户注册
    - /api/user/login         # 用户登录
    - /api-docs               # API文档
    - /swagger-ui             # Swagger UI
    - /actuator/health        # 健康检查
```

---

## 🔧 与后端服务的集成

### UserService适配

UserService的JWT过滤器已修改为：

1. **来自网关的请求**：检查`X-Gateway-Request: true`头，直接信任并从以下头获取用户信息：
   - `X-User-Id`: 用户ID
   - `X-Username`: 用户名
   - `X-User-Role`: 用户角色

2. **非网关请求**：仍然验证JWT Token（支持直接访问和Feign调用）

### 后续服务开发规范

后续开发的微服务（ProductService、OrderService等）**无需**再实现JWT鉴权过滤器，只需：

1. 从请求属性中获取用户信息：
```java
Long userId = (Long) request.getAttribute("userId");
String username = (String) request.getAttribute("username");
Integer role = (Integer) request.getAttribute("role");
```

2. 或从请求头获取：
```java
String userId = request.getHeader("X-User-Id");
```

---

## 🌐 前端配置

### 用户端
`community-group-buy-frontend/src/utils/request.js`:
```javascript
const request = axios.create({
  baseURL: 'http://localhost:9000', // ⭐ 网关地址
  timeout: 10000
})
```

### 管理端
`community-group-buy-admin/src/utils/request.js`:
```javascript
const service = axios.create({
  baseURL: 'http://localhost:9000', // ⭐ 网关地址
  timeout: 10000
})
```

---

## 📊 请求流程

```
前端请求
http://localhost:9000/api/user/info/1
    ↓
【1. GlobalLogFilter】记录请求日志，生成RequestId
    ↓
【2. JwtAuthenticationFilter】
    ├─ 白名单检查
    ├─ JWT Token验证
    └─ 提取用户信息（userId, username, role）
    ↓
【3. Gateway路由匹配】
    /api/user/** → UserService
    ↓
【4. 负载均衡】
    从Consul获取UserService实例
    ↓
【5. 转发请求】
    http://localhost:8061/api/user/info/1
    Headers:
    - X-Gateway-Request: true
    - X-User-Id: 1
    - X-Username: admin
    - X-User-Role: 2
    ↓
UserService处理
    ↓
响应返回前端
```

---

## 🐛 常见问题

### 问题1: 启动失败 - Consul连接失败
**原因**: Consul未启动  
**解决**: 先启动Consul `consul agent -dev`

### 问题2: 前端401错误
**原因**: JWT Token过期或无效  
**解决**: 重新登录获取新Token

### 问题3: 路由404错误
**原因**: 后端服务未启动或未注册到Consul  
**解决**: 检查后端服务是否启动，Consul中是否能看到服务

### 问题4: CORS跨域错误
**原因**: 前端地址不在允许列表  
**解决**: 检查`application.yml`中的CORS配置

---

## 📈 后续优化

- [ ] 集成Resilience4j限流熔断
- [ ] 完善Swagger文档聚合
- [ ] 添加Redis缓存
- [ ] 请求日志写入数据库
- [ ] 集成链路追踪（Sleuth + Zipkin）
- [ ] 性能优化和压力测试

---

## 👨‍💻 开发者

**姓名**: 耿康瑞  
**学号**: 20221204229  
**日期**: 2025-10-30

---

**状态**: ✅ Gateway开发完成，已集成UserService

