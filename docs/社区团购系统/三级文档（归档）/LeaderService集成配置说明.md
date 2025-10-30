# LeaderService 集成配置说明

**版本**：v1.0.0  
**作者**：耿康瑞  
**完成日期**：2025-10-30  

---

## 📋 配置概述

为了让LeaderService与现有系统集成，已完成以下配置修改：

1. ✅ Gateway网关路由配置
2. ✅ UserService添加Feign接口
3. ✅ Common模块检查（无需修改）

---

## 1️⃣ Gateway网关路由配置

### 修改文件
`community-group-buy-backend/gateway-service/src/main/resources/application.yml`

### 修改内容

**原配置**：
```yaml
# ==================== LeaderService 路由（待开发）====================
- id: leader-service
  uri: lb://LeaderService
  predicates:
    - Path=/api/leader/**,/api/community/**
```

**新配置**：
```yaml
# ==================== LeaderService 路由 ====================
- id: leader-service
  uri: lb://LeaderService
  predicates:
    - Path=/api/leader/**,/api/community/**,/api/community-application/**,/api/commission/**,/api/admin/community/**
```

### 路由说明

| 路径 | 说明 |
|------|------|
| `/api/leader/**` | 团长管理接口（9个API） |
| `/api/community/**` | 社区查询接口（3个C端API） |
| `/api/community-application/**` | 社区申请审核接口（6个API） |
| `/api/commission/**` | 佣金管理接口（5个API） |
| `/api/admin/community/**` | 管理员社区管理接口（5个API） |

### 验证方式

启动Gateway和LeaderService后，通过网关访问：

```bash
# 1. 查询所有社区
curl http://localhost:9000/api/community/list

# 2. 匹配最近社区
curl http://localhost:9000/api/community/nearest?latitude=39.9042&longitude=116.4074

# 3. 查询团长列表
curl http://localhost:9000/api/leader/list

# 4. 查询佣金统计
curl http://localhost:9000/api/commission/my/summary?leaderId=1
```

---

## 2️⃣ UserService Feign接口扩展

### 修改文件

1. `community-group-buy-backend/UserService/src/main/java/com/bcu/edu/controller/FeignController.java`
2. `community-group-buy-backend/UserService/src/main/java/com/bcu/edu/service/UserService.java`

### 新增接口

#### FeignController.java

```java
/**
 * 更新用户角色（供LeaderService调用）
 * 
 * 角色说明：
 * 0 - 普通用户
 * 1 - 管理员
 * 2 - 团长
 */
@PostMapping("/user/{userId}/role")
@Operation(summary = "更新用户角色", description = "供LeaderService调用，团长审核通过后更新用户角色")
public Result<Void> updateUserRole(
        @PathVariable Long userId,
        @RequestParam Integer role) {
    log.info("[Feign] 更新用户角色：userId={}, role={}", userId, role);
    
    try {
        userService.updateUserRole(userId, role);
        return Result.success("用户角色更新成功");
    } catch (Exception e) {
        log.error("[Feign] 更新用户角色失败：{}", e.getMessage(), e);
        return Result.error("用户角色更新失败：" + e.getMessage());
    }
}
```

#### UserService.java

```java
/**
 * 更新用户角色（供LeaderService调用）
 * @param userId 用户ID
 * @param role 角色（0-普通用户 1-管理员 2-团长）
 */
@Transactional
public void updateUserRole(Long userId, Integer role) {
    SysUser user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

    user.setRole(role);
    userRepository.save(user);

    log.info("[Feign] 用户角色已更新: userId={}, role={}", userId, role);
}
```

### 接口说明

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 更新用户角色 | POST | `/feign/user/{userId}/role` | LeaderService调用，团长审核通过后更新用户角色为2 |

### 调用示例

LeaderService中的调用代码：

```java
// 团长审核通过后，调用UserService更新用户角色
Result<Void> result = userServiceClient.updateUserRole(store.getLeaderId(), 2);
if (!result.isSuccess()) {
    log.error("更新用户角色失败：{}", result.getMessage());
    throw new IllegalStateException("更新用户角色失败：" + result.getMessage());
}
```

---

## 3️⃣ Common模块检查

### 检查结果

✅ **Common模块无需修改**

Common模块已提供以下完善的功能，LeaderService可直接使用：

| 功能模块 | 说明 |
|----------|------|
| `Result.java` | 统一返回结果封装 |
| `GlobalExceptionHandler.java` | 全局异常处理 |
| `JwtUtil.java` | JWT工具类 |
| `SecurityUtil.java` | 安全工具（SHA256、AES加密） |
| `@OperationLog` | 操作日志注解 |
| `PageResult.java` | 分页结果封装 |

### 使用示例

LeaderService已正确引入Common模块：

```xml
<!-- LeaderService/pom.xml -->
<dependency>
    <groupId>com.bcu.edu</groupId>
    <artifactId>common</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

---

## 4️⃣ 数据库初始化

### 创建数据库

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS leader_service_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE leader_service_db;

-- 查看自动创建的表（启动服务后JPA会自动建表）
SHOW TABLES;
```

### 表清单（JPA自动创建）

| 表名 | 说明 |
|------|------|
| `community` | 社区表（10个字段） |
| `community_application` | 社区申请表（17个字段） |
| `group_leader_store` | 团长团点表（17个字段） |
| `commission_record` | 佣金记录表（13个字段） |

---

## 5️⃣ 启动顺序

### 推荐启动顺序

1. **启动Consul**
   ```bash
   consul agent -dev
   ```

2. **创建数据库**
   ```sql
   CREATE DATABASE leader_service_db;
   ```

3. **启动UserService**（端口8061）
   ```bash
   cd community-group-buy-backend/UserService
   mvn spring-boot:run
   ```

4. **启动LeaderService**（端口8068）
   ```bash
   cd community-group-buy-backend/LeaderService
   mvn spring-boot:run
   ```

5. **启动Gateway**（端口9000）
   ```bash
   cd community-group-buy-backend/gateway-service
   mvn spring-boot:run
   ```

### 验证启动

```bash
# 1. 检查Consul服务注册
curl http://localhost:8500/v1/catalog/services

# 2. 检查LeaderService健康状态
curl http://localhost:8068/actuator/health

# 3. 访问Swagger文档
# 直接访问：http://localhost:8068/swagger-ui.html
# 通过网关访问：http://localhost:9000/leader/api-docs
```

---

## 6️⃣ 集成测试

### 测试场景1：社区管理

```bash
# 1. 创建社区（管理员接口）
curl -X POST http://localhost:9000/api/admin/community \
  -H "Content-Type: application/json" \
  -d '{
    "name": "测试社区",
    "address": "北京市朝阳区建国路88号",
    "latitude": 39.9042,
    "longitude": 116.4074,
    "serviceRadius": 3000
  }'

# 2. 查询所有社区
curl http://localhost:9000/api/community/list

# 3. 匹配最近社区
curl "http://localhost:9000/api/community/nearest?latitude=39.9042&longitude=116.4074"
```

### 测试场景2：团长申请与审核

```bash
# 1. 提交团长申请
curl -X POST http://localhost:9000/api/leader/apply \
  -H "Content-Type: application/json" \
  -d '{
    "leaderId": 1,
    "leaderName": "张三",
    "leaderPhone": "13800138000",
    "communityId": 1,
    "storeName": "张三团点",
    "address": "社区1号楼"
  }'

# 2. 管理员审核（通过）
curl -X POST "http://localhost:9000/api/leader/1/review?reviewerId=1&approved=true&reviewComment=审核通过"

# 3. 验证用户角色已更新（调用UserService）
curl http://localhost:9000/api/user/1
# 返回的role应该为2（团长）
```

### 测试场景3：佣金生成与结算

```bash
# 1. 【OrderService调用】生成佣金记录
curl -X POST "http://localhost:8068/feign/commission/generate?leaderId=1&orderId=100&orderAmount=100.00"

# 2. 查询团长佣金统计
curl "http://localhost:9000/api/commission/my/summary?leaderId=1"

# 3. 管理员手动结算佣金
curl -X POST http://localhost:9000/api/commission/settle
```

---

## 7️⃣ 注意事项

### ⚠️ 重要配置

1. **JWT密钥配置**
   - Gateway和所有微服务的JWT密钥必须一致
   - 配置位置：`application.yml` 中的 `jwt.secret`

2. **Consul地址配置**
   - 所有微服务的Consul地址必须一致
   - 默认：`localhost:8500`

3. **数据库配置**
   - LeaderService数据库：`leader_service_db`
   - UserService数据库：`user_service_db`
   - 确保两个数据库都已创建

### 🔧 常见问题

#### Q1: LeaderService调用UserService失败

**原因**：UserService未启动或未注册到Consul

**解决**：
1. 检查UserService是否启动
2. 访问 http://localhost:8500 查看Consul服务列表
3. 确认UserService已注册

#### Q2: 团长审核通过后用户角色未更新

**原因**：UserService的`updateUserRole`接口未实现

**解决**：
1. 确认UserService已更新到最新代码
2. 检查UserService的日志，查看是否有报错
3. 直接调用Feign接口测试：
   ```bash
   curl -X POST "http://localhost:8061/feign/user/1/role?role=2"
   ```

#### Q3: 通过网关访问LeaderService 404

**原因**：Gateway路由配置不正确

**解决**：
1. 检查Gateway的`application.yml`路由配置
2. 重启Gateway服务
3. 查看Gateway日志确认路由是否生效

---

## 8️⃣ 后续集成

### OrderService集成

OrderService需要调用LeaderService生成佣金记录：

```java
// OrderService中创建LeaderServiceClient
@FeignClient(name = "LeaderService", path = "/feign")
public interface LeaderServiceClient {
    
    @PostMapping("/commission/generate")
    Result<CommissionRecord> generateCommission(
        @RequestParam Long leaderId,
        @RequestParam Long orderId,
        @RequestParam BigDecimal orderAmount
    );
}

// 订单完成时调用
orderService.completeOrder(orderId);
leaderServiceClient.generateCommission(leaderId, orderId, orderAmount);
```

### 前端集成

前端调用LeaderService需要通过网关：

```javascript
// 查询所有社区
axios.get('http://localhost:9000/api/community/list')

// 匹配最近社区
axios.get('http://localhost:9000/api/community/nearest', {
  params: { latitude: 39.9042, longitude: 116.4074 }
})

// 提交团长申请
axios.post('http://localhost:9000/api/leader/apply', {
  leaderId: 1,
  communityId: 1,
  storeName: '我的团点'
})
```

---

## 总结

✅ **已完成配置**：
- ✅ Gateway网关路由（包含5个LeaderService路径）
- ✅ UserService Feign接口（updateUserRole）
- ✅ Common模块检查（无需修改）

✅ **可直接使用**：
- ✅ LeaderService所有37个API接口
- ✅ Haversine距离计算算法
- ✅ 跨服务调用（团长审核更新用户角色）
- ✅ 佣金结算定时任务

🎯 **下一步**：
1. 启动服务进行集成测试
2. OrderService集成（生成佣金记录）
3. 前端开发（团长端页面）

---

**配置完成时间**：2025-10-30  
**负责人**：耿康瑞

