# 微服务命名统一性修复文档

**日期**: 2025-11-01  
**修复原因**: 服务名称不一致导致 Feign 调用失败（503 Service Unavailable）

## 🔧 修复内容

### 统一命名规范

**规范**: 所有微服务统一使用 **小写-连字符** 命名风格（kebab-case）

### 修改的服务

| 服务 | 修改前 | 修改后 | 状态 |
|------|-------|--------|------|
| **ProductService** | `ProductService` | `product-service` | ✅ 已修改 |
| **LeaderService** | `LeaderService` | `leader-service` | ✅ 已修改 |
| **UserService** | `UserService` | `user-service` | ✅ 已修改 |
| **GroupBuyService** | `groupbuy-service` | `groupbuy-service` | ✅ 无需修改 |
| **Gateway** | `gateway-service` | `gateway-service` | ✅ 无需修改 |

## 📁 修改的文件

### 1. 服务配置文件（application.yml）

#### ProductService
```yaml
# 文件: community-group-buy-backend/ProductService/src/main/resources/application.yml
spring:
  application:
    name: product-service  # ✅ 修改为小写-连字符
```

#### LeaderService
```yaml
# 文件: community-group-buy-backend/LeaderService/src/main/resources/application.yml
spring:
  application:
    name: leader-service  # ✅ 修改为小写-连字符
```

#### UserService
```yaml
# 文件: community-group-buy-backend/UserService/src/main/resources/application.yml
spring:
  application:
    name: user-service  # ✅ 修改为小写-连字符
```

### 2. Gateway 路由配置

```yaml
# 文件: community-group-buy-backend/gateway-service/src/main/resources/application.yml

# UserService 路由
- id: user-service
  uri: lb://user-service  # ✅ 修改为小写-连字符

# ProductService 路由
- id: product-service
  uri: lb://product-service  # ✅ 修改为小写-连字符

# LeaderService 路由
- id: leader-service
  uri: lb://leader-service  # ✅ 修改为小写-连字符

# GroupBuyService 路由
- id: groupbuy-service
  uri: lb://groupbuy-service  # ✅ 原本就正确
```

### 3. Feign 客户端配置

#### GroupBuyService 的 Feign 客户端（已正确）

```java
// ProductServiceClient.java
@FeignClient(name = "product-service", fallback = ...)  // ✅ 已修改

// LeaderServiceClient.java
@FeignClient(name = "leader-service", fallback = ...)  // ✅ 正确

// UserServiceClient.java
@FeignClient(name = "user-service", fallback = ...)  // ✅ 正确
```

## 🚀 重启服务

修改配置后，需要按顺序重启以下服务：

### 重启顺序（重要！）

1. **Gateway Service** (端口 9000)
   - 修改了路由配置，需要重启

2. **ProductService** (端口 8062)
   - 修改了服务名，需要重启

3. **LeaderService** (端口 8068)
   - 修改了服务名，需要重启

4. **UserService** (端口 8061)
   - 修改了服务名，需要重启

5. **GroupBuyService** (端口 8063)
   - 虽然没有修改，但建议重启以刷新服务发现缓存

### 重启方法

**方法1: 在 IDE 中**
1. 停止所有服务
2. 按上述顺序逐个启动

**方法2: 命令行**
```bash
# 停止所有服务（Ctrl+C）

# 按顺序启动
cd gateway-service && mvn spring-boot:run &
cd ProductService && mvn spring-boot:run &
cd LeaderService && mvn spring-boot:run &
cd UserService && mvn spring-boot:run &
cd GroupBuyService && mvn spring-boot:run &
```

## ✅ 验证步骤

### 1. 检查 Consul 服务注册

访问：http://localhost:8500/ui/dc1/services

应该看到：
- ✅ `product-service` (绿色健康)
- ✅ `leader-service` (绿色健康)
- ✅ `user-service` (绿色健康)
- ✅ `groupbuy-service` (绿色健康)
- ✅ `gateway-service` (绿色健康)

**不应该再看到**：
- ❌ `ProductService`
- ❌ `LeaderService`
- ❌ `UserService`

### 2. 测试 API 访问

#### 通过 Gateway 测试

```bash
# ProductService
curl http://localhost:9000/api/product/list?page=0&size=10

# LeaderService
curl http://localhost:9000/api/community/list

# UserService
curl http://localhost:9000/api/user/info
```

#### 直接访问测试

```bash
# ProductService (8062)
curl http://localhost:8062/api/product/list?page=0&size=10

# LeaderService (8068)
curl http://localhost:8068/api/community/list

# UserService (8061)
curl http://localhost:8061/api/user/info
```

### 3. 测试 Feign 调用

在前端创建拼团活动，确保：
- ✅ 能获取商品列表（ProductService）
- ✅ 能创建活动（GroupBuyService → ProductService Feign 调用）
- ✅ 能获取用户信息（UserService）

## 📊 服务命名规范总结

### ✅ 正确的命名风格

```yaml
spring:
  application:
    name: user-service        # ✓ 小写-连字符
    name: product-service     # ✓ 小写-连字符
    name: leader-service      # ✓ 小写-连字符
    name: groupbuy-service    # ✓ 小写-连字符
```

### ❌ 错误的命名风格

```yaml
spring:
  application:
    name: UserService         # ✗ 驼峰命名
    name: ProductService      # ✗ 驼峰命名
    name: LeaderService       # ✗ 驼峰命名
    name: user_service        # ✗ 下划线命名
```

## 🔍 为什么需要统一命名？

### 问题原因

1. **Feign 客户端配置**：
   ```java
   @FeignClient(name = "product-service")  // 期望服务名
   ```

2. **实际注册到 Consul**：
   ```yaml
   name: ProductService  # 实际注册名（不匹配！）
   ```

3. **结果**：
   - Feign 查找 `product-service` 服务
   - Consul 中只有 `ProductService`
   - 找不到服务 → 503 Service Unavailable

### 修复后

1. **Feign 客户端配置**：
   ```java
   @FeignClient(name = "product-service")  // 期望服务名
   ```

2. **实际注册到 Consul**：
   ```yaml
   name: product-service  # 实际注册名（匹配！✅）
   ```

3. **结果**：
   - Feign 查找 `product-service` 服务
   - Consul 中有 `product-service` ✅
   - 成功调用！

## 🎯 最佳实践

### 1. 服务命名规范

- **使用**: 小写字母 + 连字符（kebab-case）
- **示例**: `user-service`, `product-service`, `order-service`
- **原因**: 与 HTTP URL 规范一致，易于阅读

### 2. 保持一致性

- ✅ `application.yml` 中的 `spring.application.name`
- ✅ `@FeignClient(name = "...")` 中的服务名
- ✅ Gateway 路由配置中的 `uri: lb://服务名`
- ✅ Consul 中注册的服务名

### 3. 避免混用

❌ 不要在同一项目中混用不同的命名风格：
- 部分用驼峰：`UserService`
- 部分用连字符：`product-service`

## 📝 备注

- **修改时间**: 2025-11-01
- **修改人员**: AI Assistant
- **影响范围**: 所有微服务的服务发现和调用
- **向后兼容**: 需要同时重启所有服务

---

**重要提醒**: 修改服务名后，务必按照"重启顺序"重启所有服务，并验证 Consul 中的服务注册状态！

