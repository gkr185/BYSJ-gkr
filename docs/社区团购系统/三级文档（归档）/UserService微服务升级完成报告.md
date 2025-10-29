# UserService 微服务升级完成报告

**服务名称**: UserService（用户服务）  
**升级版本**: v3.0 → v4.0（微服务拆分版）  
**升级时间**: 2025-10-29  
**数据库**: user_service_db  
**端口**: 8061  
**升级状态**: ✅ 已完成

---

## 📊 升级概览

### 核心变更
1. ✅ **数据库连接**：`community_group_buy` → `user_service_db`（独立数据库）
2. ✅ **社区机制**：支持v3.0社区关联功能（`community_id`字段）
3. ✅ **Feign客户端**：提供内部API供其他微服务调用
4. ✅ **分布式事务**：支持Saga事务（带`sagaId`参数）
5. ✅ **跨库关联**：通过应用层验证替代物理外键

---

## 🔧 详细修改内容

### 1. 数据库配置修改

**文件**: `UserService/src/main/resources/application.yml`

**修改前**:
```yaml
datasource:
  url: jdbc:mysql://localhost:3306/community_group_buy?...
```

**修改后**:
```yaml
datasource:
  url: jdbc:mysql://localhost:3306/user_service_db?...
```

**影响**: 服务独立数据库，数据隔离

---

### 2. 实体类升级

#### 2.1 SysUser（用户表）

**文件**: `UserService/src/main/java/com/bcu/edu/entity/SysUser.java`

**新增字段**:
```java
@Column(name = "community_id")
@Comment("归属社区ID（v3.0新增，跨库关联）")
private Long communityId;
```

**新增索引**:
```java
@Index(name = "idx_community_id", columnList = "community_id")
```

**业务意义**: 用户可关联社区，实现社区优先推荐机制

---

### 3. DTO类升级

#### 3.1 UserInfoResponse

**文件**: `UserService/src/main/java/com/bcu/edu/dto/response/UserInfoResponse.java`

**新增字段**:
```java
private Long communityId;       // v3.0新增
private String communityName;   // v3.0新增（跨库查询）
```

**转换方法更新**:
```java
public static UserInfoResponse fromEntity(SysUser user) {
    return UserInfoResponse.builder()
        // ... 原有字段 ...
        .communityId(user.getCommunityId())
        // communityName需要跨服务调用LeaderService获取，暂时为null
        .build();
}
```

---

### 4. Repository层升级

#### 4.1 SysUserRepository

**文件**: `UserService/src/main/java/com/bcu/edu/repository/SysUserRepository.java`

**新增方法**:
```java
// 根据社区ID查询用户列表
List<SysUser> findByCommunityId(Long communityId);

// 查询指定社区内的团长
List<SysUser> findByCommunityIdAndRole(Long communityId, Integer role);

// 统计社区内的用户数量
long countByCommunityId(Long communityId);
```

**用途**: 支持社区维度的用户查询和统计

---

### 5. Service层升级

#### 5.1 UserService（新增社区关联功能）

**文件**: `UserService/src/main/java/com/bcu/edu/service/UserService.java`

**新增方法**:

| 方法名 | 参数 | 返回值 | 说明 |
|--------|------|--------|------|
| `associateCommunity` | userId, communityId | UserInfoResponse | 关联用户到社区 |
| `getUsersByCommunity` | communityId | List<UserInfoResponse> | 查询社区内的所有用户 |
| `getLeadersByCommunity` | communityId | List<UserInfoResponse> | 查询社区内的团长 |
| `countUsersByCommunity` | communityId | long | 统计社区内的用户数量 |

**代码示例**:
```java
@Transactional
public UserInfoResponse associateCommunity(Long userId, Long communityId) {
    SysUser user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
    
    // TODO: 调用LeaderService验证社区是否存在
    // if (!leaderServiceClient.existsCommunity(communityId)) {
    //     throw new BusinessException("社区不存在");
    // }
    
    user.setCommunityId(communityId);
    user = userRepository.save(user);
    
    log.info("用户已关联社区: userId={}, communityId={}", userId, communityId);
    
    return UserInfoResponse.fromEntity(user);
}
```

---

#### 5.2 AccountService（新增Feign调用接口）

**文件**: `UserService/src/main/java/com/bcu/edu/service/AccountService.java`

**新增方法**（支持分布式事务）:

| 方法名 | 参数 | 说明 | 事务支持 |
|--------|------|------|---------|
| `deductBalanceForFeign` | userId, amount, sagaId | 扣减余额（供其他服务调用） | ✅ Saga事务 |
| `refundBalanceForFeign` | userId, amount, sagaId | 返还余额（退款/补偿） | ✅ Saga事务 |
| `checkBalance` | userId, amount | 验证余额是否充足 | 只读 |

**代码示例**:
```java
@Transactional
public void deductBalanceForFeign(Long userId, BigDecimal amount, String sagaId) {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new BusinessException(ResultCode.VALIDATE_FAILED.getCode(), "扣款金额必须大于0");
    }
    
    UserAccount account = accountRepository.findByUserId(userId)
            .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "账户不存在"));
    
    boolean success = account.deductBalance(amount);
    if (!success) {
        throw new BusinessException(ResultCode.INSUFFICIENT_BALANCE);
    }
    
    account = accountRepository.save(account);
    
    log.info("[Saga-{}] 余额扣减成功: userId={}, amount={}, newBalance={}", 
        sagaId, userId, amount, account.getBalance());
}
```

---

### 6. Controller层新增

#### 6.1 FeignController（新增）⭐

**文件**: `UserService/src/main/java/com/bcu/edu/controller/FeignController.java`（新建）

**用途**: 专门提供给其他微服务调用的内部接口

**接口清单**:

##### 用户信息查询接口
| 路径 | 方法 | 说明 | 供谁调用 |
|------|------|------|---------|
| `/feign/user/{userId}` | GET | 获取用户信息 | OrderService、GroupBuyService |
| `/feign/user/batch` | POST | 批量获取用户信息 | 所有服务 |
| `/feign/user/exists/{userId}` | GET | 验证用户是否存在 | 所有服务 |

##### 社区相关接口
| 路径 | 方法 | 说明 | 供谁调用 |
|------|------|------|---------|
| `/feign/community/{communityId}/users` | GET | 查询社区内的用户 | LeaderService |
| `/feign/community/{communityId}/leaders` | GET | 查询社区内的团长 | LeaderService |
| `/feign/community/{communityId}/count` | GET | 统计社区内的用户数量 | LeaderService |

##### 账户余额接口（分布式事务）
| 路径 | 方法 | 说明 | 供谁调用 |
|------|------|------|---------|
| `/feign/account/deduct` | POST | 扣减余额 | OrderService、PaymentService |
| `/feign/account/refund` | POST | 返还余额 | OrderService、PaymentService |
| `/feign/account/check` | GET | 验证余额是否充足 | OrderService |

**代码示例**:
```java
@PostMapping("/account/deduct")
@Operation(summary = "扣减余额", description = "供其他服务调用，支持Saga事务")
public Result<Void> deductBalance(
        @RequestParam Long userId,
        @RequestParam BigDecimal amount,
        @RequestParam String sagaId) {
    accountService.deductBalanceForFeign(userId, amount, sagaId);
    return Result.success();
}
```

---

### 7. 依赖升级

#### 7.1 pom.xml

**文件**: `UserService/pom.xml`

**新增依赖**:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**用途**: 支持Feign客户端，调用其他微服务（如LeaderService验证社区）

---

### 8. 启动类升级

#### 8.1 UserServiceApplication

**文件**: `UserService/src/main/java/com/bcu/edu/UserServiceApplication.java`

**新增注解**:
```java
@EnableFeignClients  // 启用Feign客户端
```

**完整代码**:
```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients  // ⭐ 新增
@EnableJpaRepositories(basePackages = {"com.bcu.edu.repository", "com.bcu.edu.common.repository"})
@EntityScan(basePackages = {"com.bcu.edu.entity", "com.bcu.edu.common.entity"})
@ComponentScan(basePackages = {"com.bcu.edu", "com.bcu.edu.common"})
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

---

## 📋 文件变更清单

### 修改的文件（7个）
1. ✅ `application.yml` - 数据库连接修改
2. ✅ `SysUser.java` - 添加`community_id`字段
3. ✅ `UserInfoResponse.java` - 添加社区字段
4. ✅ `SysUserRepository.java` - 添加社区查询方法
5. ✅ `UserService.java` - 添加社区关联功能
6. ✅ `AccountService.java` - 添加Feign调用接口
7. ✅ `pom.xml` - 添加OpenFeign依赖
8. ✅ `UserServiceApplication.java` - 启用Feign客户端

### 新增的文件（1个）
1. ✅ `FeignController.java` - Feign调用接口控制器

---

## 🎯 微服务架构适配

### 数据库隔离
```
原架构（单体数据库）：
  community_group_buy（所有表）
  
新架构（微服务数据库）：
  user_service_db（用户相关表）
  ├─ sys_user
  ├─ user_address
  ├─ user_account
  ├─ user_feedback
  └─ sys_operation_log
```

### 跨库关联处理

#### 场景1：用户关联社区
**原设计**: `sys_user.community_id` → 物理外键 → `community.community_id`  
**新设计**: `sys_user.community_id` → 应用层验证（调用LeaderService）  

**验证逻辑**（待实现）:
```java
// TODO: 后续创建LeaderServiceClient后实现
@Autowired
private LeaderServiceClient leaderServiceClient;

public UserInfoResponse associateCommunity(Long userId, Long communityId) {
    // 跨服务调用验证社区是否存在
    if (!leaderServiceClient.existsCommunity(communityId)) {
        throw new BusinessException("社区不存在");
    }
    // ... 保存用户
}
```

#### 场景2：订单扣减余额
**原设计**: 直接调用本地`AccountService.deduct()`  
**新设计**: OrderService通过Feign调用 `UserService/feign/account/deduct`  

**调用示例**（OrderService端）:
```java
@Autowired
private UserServiceClient userServiceClient;

public void createOrder(Long userId, BigDecimal amount) {
    String sagaId = UUID.randomUUID().toString();
    
    // 跨服务调用扣减余额
    userServiceClient.deductBalance(userId, amount, sagaId);
    
    // 创建订单...
}
```

---

## 🧪 测试验证

### 1. 数据库连接测试
```bash
# 启动UserService
mvn clean package -DskipTests
java -jar UserService/target/UserService.jar

# 预期输出
✅ Started UserServiceApplication in 8.xxx seconds
✅ Consul Discovery Client started with service-id: UserService-8061
✅ Connected to user_service_db successfully
```

### 2. Consul注册测试
```bash
# 访问Consul UI
http://localhost:8500/ui/dc1/services/UserService

# 预期结果
✅ UserService-8061 (Passing)
```

### 3. API接口测试

#### 3.1 用户信息查询（Feign接口）
```bash
curl http://localhost:8061/feign/user/1
```

**预期响应**:
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "userId": 1,
    "username": "admin",
    "role": 3,
    "roleName": "管理员",
    "communityId": null,
    "communityName": null,
    "status": 1,
    "statusName": "正常"
  }
}
```

#### 3.2 社区用户查询
```bash
curl http://localhost:8061/feign/community/1/users
```

#### 3.3 余额扣减（Saga事务）
```bash
curl -X POST "http://localhost:8061/feign/account/deduct?userId=1&amount=100.00&sagaId=test-saga-123"
```

**预期日志**:
```
[Saga-test-saga-123] 余额扣减成功: userId=1, amount=100.00, newBalance=900.00
```

### 4. Swagger文档测试
```bash
# 访问Swagger UI
http://localhost:8061/swagger-ui.html

# 检查接口
✅ Feign调用接口（10个）
✅ 用户管理接口（原有接口）
✅ 地址管理接口（原有接口）
✅ 账户管理接口（原有接口）
✅ 反馈管理接口（原有接口）
```

---

## 📊 数据库表结构

### user_service_db数据库（5张表）

#### 1. sys_user（用户表）
```sql
CREATE TABLE sys_user (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户唯一ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
  password VARCHAR(100) NOT NULL COMMENT '加密密码',
  role TINYINT NOT NULL COMMENT '角色（1-普通用户；2-团长；3-管理员）',
  real_name VARCHAR(50) COMMENT '真实姓名',
  phone VARCHAR(128) UNIQUE COMMENT '手机号（AES加密）',
  wx_openid VARCHAR(100) UNIQUE COMMENT '微信OpenID',
  avatar VARCHAR(255) COMMENT '头像URL',
  community_id BIGINT COMMENT '归属社区ID（v3.0新增）', -- ⭐ 新增字段
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-禁用；1-正常）',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT NULL COMMENT '更新时间',
  INDEX idx_community_id (community_id) -- ⭐ 新增索引
) COMMENT = '用户基础信息表';
```

#### 2. user_address（用户地址表）
```sql
CREATE TABLE user_address (
  address_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  receiver VARCHAR(50) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  province VARCHAR(20) NOT NULL,
  city VARCHAR(20) NOT NULL,
  district VARCHAR(20) NOT NULL,
  detail VARCHAR(255) NOT NULL,
  longitude DECIMAL(10,6) NOT NULL,
  latitude DECIMAL(10,6) NOT NULL,
  is_default TINYINT NOT NULL DEFAULT 0,
  FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE
) COMMENT = '用户收货地址表';
```

#### 3. user_account（用户账户表）
```sql
CREATE TABLE user_account (
  account_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  freeze_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE
) COMMENT = '用户账户表';
```

#### 4. user_feedback（用户反馈表）
```sql
CREATE TABLE user_feedback (
  feedback_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  type TINYINT NOT NULL,
  content TEXT NOT NULL,
  images TEXT,
  status TINYINT NOT NULL DEFAULT 0,
  reply TEXT,
  reply_time DATETIME,
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME,
  FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE CASCADE
) COMMENT = '用户反馈表';
```

#### 5. sys_operation_log（系统操作日志表）
```sql
CREATE TABLE sys_operation_log (
  log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  username VARCHAR(50),
  operation VARCHAR(255) NOT NULL,
  module VARCHAR(50) NOT NULL,
  method VARCHAR(500),
  params TEXT,
  result VARCHAR(20),
  error_msg TEXT,
  duration INT,
  ip VARCHAR(50),
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(user_id) ON DELETE SET NULL
) COMMENT = '系统操作日志表';
```

---

## 🔗 服务依赖关系

### UserService调用其他服务
```
UserService（用户服务）
  └─ 调用 → LeaderService（团长服务）
      └─ existsCommunity(communityId) - 验证社区是否存在
      └─ getCommunity(communityId) - 获取社区信息（用于填充communityName）
```

### 其他服务调用UserService
```
OrderService（订单服务）
  └─ 调用 → UserService
      └─ getUserById(userId) - 验证用户存在
      └─ deductBalance(userId, amount, sagaId) - 扣减余额
      └─ refundBalance(userId, amount, sagaId) - 返还余额

PaymentService（支付服务）
  └─ 调用 → UserService
      └─ deductBalance(userId, amount, sagaId) - 扣减余额
      └─ refundBalance(userId, amount, sagaId) - 退款

GroupBuyService（拼团服务）
  └─ 调用 → UserService
      └─ getUserById(userId) - 获取用户信息

LeaderService（团长服务）
  └─ 调用 → UserService
      └─ getUsersByCommunity(communityId) - 查询社区用户
      └─ getLeadersByCommunity(communityId) - 查询社区团长
```

---

## ⚠️ 注意事项

### 1. 跨服务调用延迟
- Feign调用会引入网络延迟（约10-50ms）
- 建议使用缓存优化高频查询（如`getUserById`）

### 2. 分布式事务
- `deductBalance`和`refundBalance`需要在事务上下文中调用
- 建议使用Saga模式处理跨服务事务
- `sagaId`用于追踪和补偿

### 3. 社区验证待实现
- `associateCommunity`方法中的社区验证逻辑已预留
- 需要等待LeaderService开发完成后实现

### 4. 数据一致性
- `communityName`字段需要跨服务查询
- 建议使用事件驱动或缓存同步

---

## 📝 后续工作

### 立即执行（高优先级）
- [ ] 重新编译并启动UserService
- [ ] 验证Consul服务注册
- [ ] 测试Feign接口可用性
- [ ] 检查数据库连接

### 待LeaderService完成后
- [ ] 创建LeaderServiceClient
- [ ] 实现社区验证逻辑
- [ ] 实现communityName跨服务查询
- [ ] 端到端测试社区关联功能

### 性能优化
- [ ] 添加Redis缓存（用户信息、社区信息）
- [ ] 配置Feign超时时间
- [ ] 实现降级策略（Hystrix/Sentinel）

---

## ✅ 升级检查清单

- [x] 数据库连接改为`user_service_db`
- [x] SysUser实体添加`community_id`字段
- [x] UserInfoResponse添加社区字段
- [x] SysUserRepository添加社区查询方法
- [x] UserService添加社区关联功能
- [x] AccountService添加Feign调用接口
- [x] 创建FeignController提供内部API
- [x] 添加OpenFeign依赖
- [x] 启动类启用Feign客户端
- [ ] 重新编译并启动（待执行）
- [ ] 测试验证（待执行）

---

## 📞 技术支持

### 相关文档
- `docs/社区团购系统/数据库设计说明文档.md` - 数据库设计详情
- `docs/社区团购系统/后端微服务拆分修改方案.md` - 完整修改方案
- `community-group-buy-backend/sql/01_user_service_db.sql` - 数据库创建脚本

### 配置文件
- `UserService/src/main/resources/application.yml` - 服务配置
- `UserService/pom.xml` - Maven依赖

### 启动命令
```bash
# 编译
cd E:\E\BYSJ\community-group-buy-backend
mvn clean package -pl UserService -DskipTests

# 启动
java -jar UserService/target/UserService.jar
```

---

**报告完成时间**: 2025-10-29  
**报告版本**: v1.0  
**UserService版本**: v4.0（微服务拆分版）  
**状态**: ✅ 升级完成，待测试验证

