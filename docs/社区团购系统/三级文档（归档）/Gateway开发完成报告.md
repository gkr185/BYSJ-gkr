# API Gateway 开发完成报告

**服务名称**: gateway-service  
**开发版本**: v1.0.0  
**开发时间**: 2025-10-30  
**开发状态**: ✅ 已完成  
**端口**: 9000

---

## 📊 开发概览

### 核心成果
- ✅ 创建完整的API Gateway服务
- ✅ 实现JWT统一鉴权
- ✅ 配置7个微服务路由
- ✅ UserService成功迁移到网关架构
- ✅ 前端（用户端+管理端）统一调用网关
- ✅ CORS跨域统一处理
- ✅ 全局日志记录

---

## 🏗️ 架构变更

### 变更前（直连架构）
```
前端 (5173/5174)
    ↓ 直接HTTP调用
UserService:8061
ProductService:8062（待开发）
... 其他服务
```

**问题**：
- 前端需要配置多个服务地址
- 每个服务都需要JWT验证和CORS配置
- 服务地址变更需要修改前端

### 变更后（网关架构）⭐
```
前端 (5173/5174)
    ↓ 统一调用
API Gateway:9000 ⭐
    ├─ JWT鉴权
    ├─ CORS处理
    ├─ 路由转发
    └─ 负载均衡
    ↓ 转发
各微服务 (8061/8062/...)
```

**优势**：
- ✅ 前端只需配置一个网关地址
- ✅ JWT鉴权在网关层统一处理
- ✅ CORS在网关层统一配置
- ✅ 后端服务开发更简单（无需JWT过滤器）
- ✅ 支持负载均衡和服务发现

---

## 📦 已完成功能

### 1. 核心组件

#### 1.1 GatewayApplication.java
- Spring Boot启动类
- 启用服务发现（@EnableDiscoveryClient）
- 启动成功提示信息

#### 1.2 application.yml配置
- 服务端口：9000
- Consul服务注册配置
- 7个微服务路由规则
- 全局CORS配置
- JWT配置
- 白名单配置

#### 1.3 JWT工具类（JwtUtil.java）
- 解析JWT Token
- 提取用户ID、用户名、角色
- 支持多种数据类型转换

### 2. 核心过滤器

#### 2.1 JwtAuthenticationFilter（JWT鉴权）
**功能**：
- 白名单路径检查（login/register等）
- OPTIONS请求放行（CORS预检）
- JWT Token验证
- 提取用户信息（userId, username, role）
- 将用户信息通过请求头传递给后端服务

**请求头传递**：
```
X-User-Id: 用户ID
X-Username: 用户名
X-User-Role: 用户角色
X-Gateway-Request: true（标识来自网关）
```

**执行顺序**: Order = -100（高优先级）

#### 2.2 GlobalLogFilter（全局日志）
**功能**：
- 生成请求ID（UUID）
- 记录请求信息（方法、路径、IP、UA）
- 记录响应信息（状态码、耗时）
- 请求ID传递给后端

**执行顺序**: Order = -200（最高优先级）

### 3. 路由配置

已配置7个微服务路由：

| 路由ID | Path | 目标服务 | 状态 |
|-------|------|---------|------|
| user-service | /api/user/** | lb://UserService | ✅ 已测试 |
| product-service | /api/product/** | lb://ProductService | ⏳ 待开发 |
| groupbuy-service | /api/groupbuy/** | lb://GroupBuyService | ⏳ 待开发 |
| order-service | /api/order/** | lb://OrderService | ⏳ 待开发 |
| payment-service | /api/payment/** | lb://PaymentService | ⏳ 待开发 |
| leader-service | /api/leader/** | lb://LeaderService | ⏳ 待开发 |
| delivery-service | /api/delivery/** | lb://DeliveryService | ⏳ 待开发 |

### 4. UserService适配

**修改文件**: `UserService/src/main/java/com/bcu/edu/config/JwtAuthenticationFilter.java`

**修改内容**：
1. 增加网关请求检测逻辑
2. 如果是网关请求（`X-Gateway-Request: true`），直接信任并从请求头获取用户信息
3. 如果非网关请求，仍然验证JWT（支持直接访问和Feign调用）

**关键代码**：
```java
// 检查是否来自网关
String fromGateway = request.getHeader("X-Gateway-Request");
if ("true".equals(fromGateway)) {
    // 直接信任，从请求头获取用户信息
    String userIdStr = request.getHeader("X-User-Id");
    String username = request.getHeader("X-Username");
    String roleStr = request.getHeader("X-User-Role");
    // ... 存储到request attributes
    return;
}
// 否则验证JWT
```

### 5. 前端适配

#### 用户端
**文件**: `community-group-buy-frontend/src/utils/request.js`  
**修改**: baseURL从`http://localhost:8061`改为`http://localhost:9000`

#### 管理端
**文件**: `community-group-buy-admin/src/utils/request.js`  
**修改**: baseURL从`http://localhost:8061`改为`http://localhost:9000`

---

## 🎯 技术亮点

### 1. 双重验证机制 ⭐⭐⭐⭐⭐
- 网关请求：信任网关，从请求头获取用户信息（性能优）
- 直接访问/Feign调用：验证JWT Token（安全性高）

### 2. 用户信息透传 ⭐⭐⭐⭐
- 网关验证JWT后，将用户信息通过请求头传递
- 后端服务无需重复解析JWT
- 提升性能，降低复杂度

### 3. 统一CORS处理 ⭐⭐⭐⭐
- 网关层统一配置CORS
- 后端服务无需配置
- 避免重复配置

### 4. 负载均衡 ⭐⭐⭐⭐
- 通过`lb://ServiceName`从Consul动态获取服务实例
- 自动负载均衡
- 支持服务扩缩容

### 5. 白名单机制 ⭐⭐⭐
- 灵活配置无需鉴权的接口
- 支持AntPathMatcher模式匹配
- 易于扩展

---

## 📈 代码量统计

| 类型 | 文件数 | 代码行数 |
|------|--------|---------|
| Java代码 | 5个 | ~600行 |
| 配置文件 | 2个 | ~150行 |
| 文档 | 1个 | ~300行 |
| **总计** | **8个** | **~1,050行** |

### 文件清单
```
gateway-service/
├── pom.xml                                    # Maven配置
├── src/main/java/com/bcu/edu/gateway/
│   ├── GatewayApplication.java                # 启动类
│   ├── config/
│   │   ├── JwtUtil.java                       # JWT工具类
│   │   └── SwaggerResourceConfig.java         # Swagger配置
│   └── filter/
│       ├── JwtAuthenticationFilter.java       # JWT鉴权过滤器
│       └── GlobalLogFilter.java               # 全局日志过滤器
└── src/main/resources/
    └── application.yml                         # 应用配置
```

---

## 🧪 测试验证

### 测试场景

#### 1. 白名单接口（无需Token）✅
- [x] 用户注册 `POST /api/user/register`
- [x] 用户登录 `POST /api/user/login`
- [x] 健康检查 `GET /actuator/health`

#### 2. 需要鉴权的接口 ✅
- [x] 获取用户信息 `GET /api/user/info/{userId}`
- [x] 更新用户信息 `PUT /api/user/update/{userId}`
- [x] 地址管理接口
- [x] 账户管理接口
- [x] 反馈管理接口

#### 3. 前端集成测试 ✅
- [x] 用户端登录功能
- [x] 用户端个人信息管理
- [x] 用户端地址管理
- [x] 用户端余额查询
- [x] 用户端反馈提交
- [x] 管理端登录功能
- [x] 管理端用户管理
- [x] 管理端反馈管理
- [x] 管理端日志管理

### 测试结果
✅ **所有测试通过**，网关运行正常！

---

## 📚 相关文档

- [Gateway启动说明](../../community-group-buy-backend/gateway-service/README.md)
- [UserService API文档](../二级文档（参考）/API_UserService.md)
- [项目总览](../一级文档（持续更新）/01_项目总览.md)

---

## 🚀 后续计划

### 短期（本周）
- [ ] 开发LeaderService并接入网关
- [ ] 开发ProductService并接入网关

### 中期（2周内）
- [ ] 完善Swagger文档聚合
- [ ] 集成Resilience4j限流熔断

### 长期
- [ ] 性能优化和压力测试
- [ ] 添加链路追踪（Sleuth + Zipkin）
- [ ] 请求日志写入数据库

---

## 💡 开发经验总结

### 成功经验
1. **双重验证机制设计合理**：既支持网关转发，又支持直接访问
2. **用户信息透传高效**：避免后端服务重复解析JWT
3. **配置灵活**：白名单、路由规则易于扩展
4. **测试充分**：前后端集成测试确保功能正常

### 遇到的问题
1. **问题**: JWT密钥不一致导致验证失败  
   **解决**: 确保Gateway和UserService的JWT密钥配置一致

2. **问题**: CORS配置冲突  
   **解决**: 移除后端服务的CORS配置，统一在网关处理

3. **问题**: 白名单配置格式错误  
   **解决**: 使用AntPathMatcher支持通配符匹配

---

## 🎓 技术收获

1. **Spring Cloud Gateway深入理解**：
   - GlobalFilter vs GatewayFilter
   - 过滤器执行顺序（Order）
   - 路由谓词（Predicates）和过滤器（Filters）

2. **微服务架构实践**：
   - 服务注册与发现（Consul）
   - 负载均衡（LoadBalancer）
   - 跨服务通信

3. **安全性设计**：
   - JWT统一鉴权
   - 白名单机制
   - 用户信息安全传递

---

## 📊 整体进度更新

### 项目完成度：70% → **75%**

| 模块 | 计划 | 已完成 | 待开发 | 完成率 |
|------|------|--------|--------|--------|
| 后端服务 | 8个 | 3个⭐ | 5个 | 38% |
| 前端应用 | 3个 | 2个 | 1个 | 67% |
| 基础设施 | 2个 | 2个⭐ | 0个 | 100% |

**新增完成**：
- ✅ Common模块
- ✅ UserService
- ⭐ **Gateway服务**（新增）

**基础设施完成**：
- ✅ Consul服务注册中心
- ✅ API Gateway统一入口

---

## 👨‍💻 开发信息

**开发者**: 耿康瑞  
**学号**: 20221204229  
**开发时间**: 2025-10-30  
**开发周期**: 1天  
**代码量**: ~1,050行  

---

**创建日期**: 2025-10-30  
**文档版本**: v1.0  
**状态**: ✅ Gateway开发完成并成功集成UserService

---

## 🎉 总结

API Gateway的成功开发和部署，标志着项目架构从单体应用向微服务架构的重要转变。统一网关入口为后续微服务开发提供了坚实基础，显著降低了服务间通信的复杂度，提升了系统的可维护性和可扩展性。

**下一步**: 开始开发LeaderService（社区和团长管理服务）！🚀

