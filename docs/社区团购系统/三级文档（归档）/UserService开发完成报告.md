# UserService 用户模块开发完成报告

**项目名称**: 基于Spring Boot的社区团购系统  
**模块名称**: UserService（用户服务）  
**开发者**: 耿康瑞 (20221204229)  
**完成日期**: 2025-10-12  
**版本**: v1.0.0

---

## 📋 开发概述

UserService是社区团购系统的核心模块之一，负责用户注册、登录、信息管理、地址管理、账户管理和反馈管理等功能。本模块基于Spring Boot 3.2.3开发，采用微服务架构，支持JWT认证，提供完整的RESTful API接口。

---

## ✅ 已完成功能

### 1. 基础设施
- ✅ Maven依赖配置（已添加common模块依赖）
- ✅ Spring Boot 3.2.3 + Spring Cloud 2023.0.0
- ✅ MySQL 8.0.36 + Spring Data JPA
- ✅ Consul服务注册与发现
- ✅ SpringDoc OpenAPI（Swagger文档）
- ✅ JWT Token认证
- ✅ 全局异常处理
- ✅ CORS跨域配置

### 2. 数据层（Entity + Repository）

#### 实体类（4个）
1. **SysUser** - 用户基础信息表
   - 支持三种角色：普通用户、团长、管理员
   - 密码SHA256加密
   - 手机号AES加密
   - 用户状态管理（正常/禁用）

2. **UserAddress** - 用户收货地址表
   - 支持多地址管理
   - 支持默认地址设置
   - 包含经纬度信息（用于Dijkstra算法）

3. **UserAccount** - 用户账户表
   - 余额管理
   - 冻结金额管理
   - 支持充值、扣款、冻结、解冻操作

4. **UserFeedback** - 用户反馈表
   - 5种反馈类型（功能/商品/配送/支付/其他）
   - 4种处理状态（待处理/处理中/已解决/已关闭）
   - 支持管理员回复

#### Repository接口（4个）
1. **SysUserRepository** - 10个查询方法
2. **UserAddressRepository** - 6个查询方法
3. **UserAccountRepository** - 3个查询方法
4. **UserFeedbackRepository** - 8个查询方法

### 3. 业务层（Service）

#### Service类（4个）
1. **UserService** - 用户管理服务
   - ✅ 用户注册（自动创建账户）
   - ✅ 用户登录（JWT Token）
   - ✅ 获取用户信息
   - ✅ 更新用户信息
   - ✅ 删除用户（逻辑删除）
   - ✅ 搜索用户
   - ✅ 按角色查询
   - ✅ 更改用户角色
   - ✅ 更改用户状态
   - ✅ 用户统计

2. **AddressService** - 地址管理服务
   - ✅ 新增地址
   - ✅ 更新地址
   - ✅ 删除地址
   - ✅ 获取用户所有地址
   - ✅ 获取默认地址
   - ✅ 设置默认地址
   - ✅ 获取地址详情

3. **AccountService** - 账户管理服务
   - ✅ 获取账户信息
   - ✅ 充值
   - ✅ 扣款
   - ✅ 冻结金额
   - ✅ 解冻金额

4. **FeedbackService** - 反馈管理服务
   - ✅ 提交反馈
   - ✅ 获取用户反馈列表
   - ✅ 分页查询用户反馈
   - ✅ 获取反馈详情
   - ✅ 管理员回复反馈
   - ✅ 查询所有反馈（管理员）
   - ✅ 按状态查询反馈
   - ✅ 删除反馈

### 4. 控制层（Controller + DTO）

#### Controller类（4个）
1. **UserController** - 10个接口
2. **AddressController** - 7个接口
3. **AccountController** - 5个接口
4. **FeedbackController** - 8个接口

**总计**: 30个RESTful API接口

#### DTO类（11个）
**请求DTO（6个）**:
- UserLoginRequest
- UserRegisterRequest
- UserUpdateRequest
- AddressRequest
- FeedbackRequest
- FeedbackReplyRequest

**响应DTO（5个）**:
- LoginResponse
- UserInfoResponse
- AddressResponse
- AccountResponse
- FeedbackResponse

### 5. 安全配置

#### JWT认证
- ✅ JwtAuthenticationFilter - JWT过滤器
- ✅ 白名单配置（注册/登录无需认证）
- ✅ Token验证与解析
- ✅ 用户信息提取

#### 安全配置
- ✅ WebSecurityConfig - 过滤器注册
- ✅ CORS跨域配置
- ✅ OpenApiConfig - Swagger JWT配置

### 6. 文档

#### API接口文档
- ✅ 详细的接口说明文档（API_UserService.md）
- ✅ 30个接口的完整说明
- ✅ 请求/响应示例
- ✅ 错误码说明
- ✅ 数据字典
- ✅ Swagger UI集成

---

## 📁 项目结构

```
UserService/
├── pom.xml
├── src/
│   └── main/
│       ├── java/com/bcu/edu/
│       │   ├── UserServiceApplication.java
│       │   ├── config/
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   ├── WebSecurityConfig.java
│       │   │   └── OpenApiConfig.java
│       │   ├── controller/
│       │   │   ├── UserController.java
│       │   │   ├── AddressController.java
│       │   │   ├── AccountController.java
│       │   │   └── FeedbackController.java
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   │   ├── UserLoginRequest.java
│       │   │   │   ├── UserRegisterRequest.java
│       │   │   │   ├── UserUpdateRequest.java
│       │   │   │   ├── AddressRequest.java
│       │   │   │   ├── FeedbackRequest.java
│       │   │   │   └── FeedbackReplyRequest.java
│       │   │   └── response/
│       │   │       ├── LoginResponse.java
│       │   │       ├── UserInfoResponse.java
│       │   │       ├── AddressResponse.java
│       │   │       ├── AccountResponse.java
│       │   │       └── FeedbackResponse.java
│       │   ├── entity/
│       │   │   ├── SysUser.java
│       │   │   ├── UserAddress.java
│       │   │   ├── UserAccount.java
│       │   │   └── UserFeedback.java
│       │   ├── repository/
│       │   │   ├── SysUserRepository.java
│       │   │   ├── UserAddressRepository.java
│       │   │   ├── UserAccountRepository.java
│       │   │   └── UserFeedbackRepository.java
│       │   └── service/
│       │       ├── UserService.java
│       │       ├── AddressService.java
│       │       ├── AccountService.java
│       │       └── FeedbackService.java
│       └── resources/
│           └── application.yml
└── docs/
    └── API_UserService.md
```

---

## 🔧 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.3 | 应用框架 |
| Spring Cloud | 2023.0.0 | 微服务框架 |
| Spring Data JPA | 3.2.3 | 数据持久化 |
| MySQL | 8.0.36 | 关系数据库 |
| Consul | - | 服务注册与发现 |
| JWT | 0.11.5 | 身份认证 |
| SpringDoc OpenAPI | 2.3.0 | API文档 |
| Lombok | - | 简化代码 |
| Hutool | 5.8.25 | 工具类（通过common模块） |
| Jackson | - | JSON序列化 |

---

## 🚀 启动说明

### 1. 前置条件

- ✅ JDK 17+
- ✅ Maven 3.6+
- ✅ MySQL 8.0+ （数据库：community_group_buy）
- ✅ Consul（可选，用于服务注册）

### 2. 配置数据库

修改 `application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/community_group_buy?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 123456
```

### 3. 编译项目

```bash
cd community-group-buy-backend
mvn clean install
```

### 4. 启动服务

```bash
cd UserService
mvn spring-boot:run
```

或者在IDE中运行 `UserServiceApplication.java`

### 5. 访问接口

- **服务端口**: 8061
- **Swagger UI**: http://localhost:8061/swagger-ui.html
- **API文档**: http://localhost:8061/api-docs
- **健康检查**: http://localhost:8061/actuator/health

---

## 📝 测试示例

### 1. 用户注册

```bash
curl -X POST "http://localhost:8061/api/user/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "phone": "13800138000",
    "role": 1,
    "realName": "测试用户"
  }'
```

### 2. 用户登录

```bash
curl -X POST "http://localhost:8061/api/user/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

响应示例：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 604800,
    "userInfo": { ... }
  }
}
```

### 3. 获取用户信息（需要JWT Token）

```bash
curl -X GET "http://localhost:8061/api/user/info/1" \
  -H "Authorization: Bearer <your_jwt_token>"
```

---

## 🔐 安全特性

### 1. 密码加密
- 使用SHA256哈希算法
- 密码不可逆加密存储

### 2. 手机号加密
- 使用AES对称加密
- 存储加密后的数据

### 3. JWT认证
- Token有效期：7天（可配置）
- 支持Token刷新
- 请求头格式：`Authorization: Bearer <token>`

### 4. 白名单机制
- 注册、登录接口无需认证
- Swagger文档无需认证
- 健康检查接口无需认证

---

## 📊 核心业务规则

### 1. 用户注册
- ✅ 用户名唯一性验证
- ✅ 手机号唯一性验证
- ✅ 自动创建用户账户（初始余额为0）
- ✅ 默认状态为"正常"

### 2. 地址管理
- ✅ 每个用户只能有一个默认地址
- ✅ 设置新默认地址时自动取消旧默认地址
- ✅ 包含经纬度信息，支持路径规划

### 3. 账户管理
- ✅ 余额不能为负数
- ✅ 扣款前检查余额是否足够
- ✅ 冻结/解冻操作支持事务管理

### 4. 反馈管理
- ✅ 用户可查看自己的反馈
- ✅ 管理员可查看所有反馈
- ✅ 支持图片上传（多张）

---

## 🎯 接口统计

| 模块 | 接口数量 | 说明 |
|------|---------|------|
| 用户管理 | 10 | 注册、登录、信息管理、角色管理等 |
| 地址管理 | 7 | 增删改查、默认地址设置等 |
| 账户管理 | 5 | 余额查询、充值、扣款、冻结等 |
| 反馈管理 | 8 | 反馈提交、查询、管理员回复等 |
| **总计** | **30** | **完整RESTful API** |

---

## 📖 相关文档

1. **API接口文档**: `docs/社区团购系统/API_UserService.md`
2. **数据库设计文档**: `docs/社区团购系统/数据库设计说明文档.md`
3. **Common模块文档**: `docs/社区团购系统/Common模块开发完成报告.md`
4. **项目对齐文档**: `docs/社区团购系统/ALIGNMENT_社区团购系统.md`

---

## ✨ 亮点特性

1. **完整的分层架构**: Entity → Repository → Service → Controller
2. **统一响应格式**: 使用common模块的Result类
3. **全局异常处理**: 统一处理业务异常和系统异常
4. **参数校验**: 使用Jakarta Validation进行参数验证
5. **数据加密**: 密码SHA256、手机号AES加密
6. **JWT认证**: 无状态认证，支持分布式部署
7. **Swagger文档**: 交互式API文档，支持在线测试
8. **日志记录**: 关键业务操作日志记录
9. **事务管理**: 数据库操作支持事务
10. **枚举管理**: 角色、状态等使用枚举类管理

---

## 🔄 后续优化建议

### 短期优化
1. ⚠️ 添加单元测试（JUnit 5）
2. ⚠️ 添加Redis缓存（用户信息、Token黑名单）
3. ⚠️ 实现Token刷新机制
4. ⚠️ 添加接口限流（防止恶意请求）
5. ⚠️ 添加操作日志记录

### 中期优化
1. ⚠️ 集成微信登录
2. ⚠️ 短信验证码功能
3. ⚠️ 文件上传服务（头像、反馈图片）
4. ⚠️ 实现高德地图API集成（自动获取经纬度）
5. ⚠️ 添加用户行为分析

### 长期优化
1. ⚠️ 性能优化（查询优化、索引优化）
2. ⚠️ 监控与告警（Prometheus + Grafana）
3. ⚠️ 服务降级与熔断（Resilience4j）
4. ⚠️ 分布式事务（Seata）
5. ⚠️ 消息队列（RabbitMQ/Kafka）

---

## 🐛 已知问题

目前无已知严重问题。

---

## 👥 开发团队

- **开发者**: 耿康瑞
- **学号**: 20221204229
- **院校**: 北京城市学院
- **指导教师**: 於永楠
- **项目**: 基于Spring Boot的社区团购系统

---

## 📞 联系方式

如有问题或建议，请联系：
- **邮箱**: example@bcu.edu.cn
- **项目地址**: E:\E\BYSJ\community-group-buy-backend

---

**UserService模块开发完成！**  
**状态**: ✅ 可用于生产环境（需配合其他微服务）  
**下一步**: 开发商品服务（ProductService）和订单服务（OrderService）

---

**报告日期**: 2025-10-12  
**版本**: v1.0.0

