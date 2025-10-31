# Common 公共模块

## 模块说明

Common模块是社区团购系统的公共基础模块，为所有微服务提供统一的工具类、异常处理、返回结果封装等功能。

## 模块结构

```
common/
├── src/main/java/com/bcu/edu/common/
│   ├── annotation/             # 自定义注解
│   │   └── OperationLog.java   # 操作日志注解
│   ├── aspect/                 # AOP切面
│   │   └── OperationLogAspect.java  # 操作日志切面（Feign调用）⭐改造 v3.0
│   ├── config/                 # 配置类
│   │   ├── WebConfig.java      # Web配置（CORS等）
│   │   └── AsyncConfig.java    # 异步任务配置
│   ├── constant/               # 常量类
│   │   └── Constants.java      # 系统常量定义
│   ├── dto/                    # 数据传输对象
│   │   ├── OperationLogDTO.java  # 操作日志DTO（跨服务传输）⭐新增 v3.0
│   │   └── OperationLogQuery.java # 日志查询条件
│   ├── entity/                 # JPA实体类
│   │   └── SysOperationLog.java  # 操作日志实体（UserService专用）
│   ├── enums/                  # 枚举类
│   │   └── ResultCode.java     # 返回结果码枚举
│   ├── exception/              # 异常类
│   │   ├── BaseException.java      # 基础异常
│   │   └── BusinessException.java  # 业务异常
│   ├── feign/                  # Feign客户端 ⭐新增 v3.0
│   │   ├── LogFeignClient.java     # 日志服务Feign客户端
│   │   └── LogFeignClientFallback.java  # Feign降级处理
│   ├── handler/                # 处理器
│   │   └── GlobalExceptionHandler.java  # 全局异常处理器
│   ├── repository/             # JPA数据访问层
│   │   └── SysOperationLogRepository.java  # 日志Repository（UserService专用）
│   ├── result/                 # 返回结果封装
│   │   ├── Result.java         # 统一返回结果
│   │   └── PageResult.java     # 分页结果
│   └── utils/                  # 工具类
│       ├── SecurityUtil.java   # 安全工具（加密、解密、脱敏）
│       ├── JwtUtil.java        # JWT工具
│       ├── DateUtil.java       # 日期工具
│       ├── IpUtil.java         # IP地址工具
│       └── ExcelUtil.java      # Excel导出工具
├── src/main/resources/
│   └── logback-spring.xml      # Logback配置
└── pom.xml

注意：
- LogController 和 LogService 已移至 UserService（日志服务提供方）
- common 模块只保留 Feign 客户端接口和 DTO
```

## 依赖说明

### 核心依赖

- **Spring Boot Web**: 提供Web相关功能
- **Spring Boot Validation**: 参数校验
- **Spring Boot AOP**: AOP切面编程
- **Spring Data JPA**: ORM数据持久化
- **Spring Cloud OpenFeign**: 微服务间调用 ⭐新增（v3.0）
- **Lombok**: 简化代码
- **Hutool**: 工具类库（加密、日期等）
- **JWT**: JSON Web Token认证
- **Jackson**: JSON序列化
- **Apache POI**: Excel导出

### 版本信息

- Spring Boot: 3.2.3
- Hutool: 5.8.25
- JWT: 0.11.5
- Apache POI: 5.2.5 ⭐新增

## 使用指南

### 1. 在其他模块中引入Common模块

在其他微服务的`pom.xml`中添加依赖：

```xml
<dependency>
    <groupId>bcu.edu</groupId>
    <artifactId>common</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 统一返回结果使用

#### Controller示例

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    // 成功返回（无数据）
    @GetMapping("/test")
    public Result<Void> test() {
        return Result.success();
    }
    
    // 成功返回（带数据）
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }
    
    // 成功返回（自定义消息）
    @PostMapping
    public Result<Void> createUser(@RequestBody User user) {
        userService.save(user);
        return Result.success("用户创建成功");
    }
    
    // 失败返回（使用ResultCode）
    @GetMapping("/check/{id}")
    public Result<Void> checkUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        return Result.success();
    }
    
    // 分页查询
    @GetMapping("/page")
    public Result<PageResult<User>> getUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<User> pageResult = userService.getPage(pageNum, pageSize);
        return Result.success(pageResult);
    }
}
```

### 3. 业务异常使用

#### Service层抛出异常

```java
@Service
public class UserService {
    
    public User getById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            // 使用ResultCode抛出异常
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            // 自定义消息
            throw new BusinessException(ResultCode.USER_DISABLED, "该用户已被禁用，请联系管理员");
        }
        return user;
    }
    
    public void register(String username, String password) {
        // 检查用户名是否存在
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ResultCode.USERNAME_ALREADY_EXISTS);
        }
        // 保存用户...
    }
}
```

全局异常处理器会自动捕获并返回统一格式：

```json
{
  "code": 1010,
  "message": "用户不存在",
  "timestamp": "2025-10-12T10:30:00"
}
```

### 4. 加密工具使用

#### 密码加密

```java
// 用户注册时加密密码
String rawPassword = "123456";
String encryptedPassword = SecurityUtil.encryptPassword(rawPassword);
user.setPassword(encryptedPassword);

// 用户登录时验证密码
if (!SecurityUtil.verifyPassword(rawPassword, user.getPassword())) {
    throw new BusinessException(ResultCode.PASSWORD_ERROR);
}
```

#### 敏感数据加密（手机号、支付信息）

```java
// 加密手机号
String phone = "13812345678";
String encryptedPhone = SecurityUtil.aesEncrypt(phone);
user.setPhone(encryptedPhone);

// 解密手机号
String decryptedPhone = SecurityUtil.aesDecrypt(user.getPhone());

// 手机号脱敏显示
String maskedPhone = SecurityUtil.maskPhone("13812345678");
// 输出: 138****5678
```

#### 数据签名（支付场景）

```java
// 生成签名
String data = "order_id=123&amount=100&user_id=1";
String key = "your_secret_key";
String signature = SecurityUtil.sign(data, key);

// 验证签名
boolean valid = SecurityUtil.verifySign(data, key, signature);
```

### 5. JWT Token使用

#### 生成Token

```java
@Service
public class AuthService {
    
    public String login(String username, String password) {
        // 验证用户名密码...
        User user = userRepository.findByUsername(username);
        
        // 生成JWT Token
        String token = JwtUtil.generateToken(
            user.getUserId(),
            user.getUsername(),
            user.getRole()
        );
        
        return token;
    }
}
```

#### 解析Token

```java
@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) {
        // 获取Token
        String token = request.getHeader(JwtUtil.HEADER_NAME);
        if (token == null) {
            throw new BusinessException(ResultCode.TOKEN_MISSING);
        }
        
        // 验证Token
        if (!JwtUtil.validateToken(token)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }
        
        // 解析Token获取用户信息
        Long userId = JwtUtil.getUserId(token);
        String username = JwtUtil.getUsername(token);
        Integer role = JwtUtil.getRole(token);
        
        // 将用户信息存入ThreadLocal或Request Attribute
        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("role", role);
        
        return true;
    }
}
```

#### 刷新Token

```java
@PostMapping("/refresh-token")
public Result<String> refreshToken(@RequestHeader(JwtUtil.HEADER_NAME) String token) {
    // 验证Token
    if (!JwtUtil.validateToken(token)) {
        return Result.error(ResultCode.TOKEN_INVALID);
    }
    
    // 刷新Token
    String newToken = JwtUtil.refreshToken(token);
    return Result.success(newToken);
}
```

### 6. 日期工具使用

```java
// 格式化日期时间
String now = DateUtil.now();  // 2025-10-12 10:30:00
String today = DateUtil.today();  // 2025-10-12

// 紧凑格式（用于文件名）
String compact = DateUtil.nowCompact();  // 20251012103000

// 解析字符串
LocalDateTime dateTime = DateUtil.parseDateTime("2025-10-12 10:30:00");
LocalDate date = DateUtil.parseDate("2025-10-12");

// 日期计算
LocalDateTime tomorrow = DateUtil.plusDays(LocalDateTime.now(), 1);
LocalDateTime oneHourLater = DateUtil.plusHours(LocalDateTime.now(), 1);

// 计算日期差
long days = DateUtil.daysBetween(startDate, endDate);
long hours = DateUtil.hoursBetween(startTime, endTime);

// 判断是否过期
boolean expired = DateUtil.isExpired(expirationTime);

// 判断是否在范围内
boolean inRange = DateUtil.isBetween(dateTime, startTime, endTime);
```

### 7. 常量使用

```java
// 用户角色判断
if (user.getRole().equals(Constants.UserRole.ADMIN)) {
    // 管理员权限...
}

// 订单状态判断
if (order.getOrderStatus().equals(Constants.OrderStatus.PENDING_PAYMENT)) {
    // 待支付订单...
}

// 支付类型
if (payType.equals(Constants.PayType.WECHAT)) {
    // 微信支付...
}

// 分页参数
Integer pageNum = request.getPageNum() != null ? request.getPageNum() : Constants.Page.DEFAULT_PAGE;
Integer pageSize = request.getPageSize() != null ? request.getPageSize() : Constants.Page.DEFAULT_SIZE;

// 订单超时时间
LocalDateTime timeout = LocalDateTime.now().plus(Constants.Time.ORDER_TIMEOUT, ChronoUnit.MILLIS);
```

### 8. 参数校验

结合Spring Validation使用：

```java
@Data
public class UserRegisterDTO {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名长度为4-20位")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = Constants.Regex.PASSWORD, message = "密码格式不正确（8-20位，包含字母和数字）")
    private String password;
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = Constants.Regex.PHONE, message = "手机号格式不正确")
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    private String email;
}

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody UserRegisterDTO dto) {
        // 参数校验失败会自动被GlobalExceptionHandler捕获并返回错误信息
        userService.register(dto);
        return Result.success("注册成功");
    }
}
```

### 9. 操作日志系统（微服务架构）⭐改造 v3.0

系统实现了基于微服务的日志架构：
1. **Logback技术日志** - 文件日志，用于开发调试和错误追踪
2. **AOP操作日志** - 通过Feign调用UserService保存到数据库，用于业务审计和操作追溯

**架构说明**：
- 日志统一存储在 `user_service_db.sys_operation_log` 表中
- 各微服务通过 Feign 调用 UserService 的日志接口保存日志
- Fallback 降级处理，确保日志失败不影响业务
- 异步保存，响应时间增加 < 10ms

#### 9.1 使用@OperationLog注解记录操作

只需在Controller方法上添加注解，即可自动记录操作日志：

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    // 基础使用
    @OperationLog(value = "创建用户", module = "用户管理")
    @PostMapping
    public Result<User> createUser(@RequestBody UserCreateRequest request) {
        return Result.success(userService.createUser(request));
    }
    
    // 不记录参数（用于登录、注册等敏感操作）
    @OperationLog(
        value = "用户登录", 
        module = "认证管理", 
        recordParams = false  // 不记录请求参数
    )
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        return Result.success(userService.login(request));
    }
    
    // 自定义敏感字段脱敏
    @OperationLog(
        value = "修改密码", 
        module = "用户管理",
        sensitiveFields = {"password", "newPassword", "oldPassword"}
    )
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody PasswordChangeRequest request) {
        userService.changePassword(request);
        return Result.success();
    }
}
```

**注解参数说明**：
- `value`: 操作内容描述（必填）
- `module`: 操作模块（可选，默认空字符串）
- `recordParams`: 是否记录请求参数（可选，默认true）
- `recordResult`: 是否记录返回结果（可选，默认false）
- `sensitiveFields`: 敏感参数字段，会被替换为`***`（可选，默认包含password、token、secret）

**自动记录的信息**：
- 操作人ID和用户名（从JWT Token提取）
- 操作内容和所属模块
- 方法全路径
- 请求参数（JSON格式，支持脱敏）
- 操作结果（SUCCESS/FAIL）
- 错误信息（失败时记录）
- 执行时长（毫秒）
- 客户端IP地址
- 操作时间

#### 9.2 日志流转流程

```
ProductService/LeaderService (业务服务)
         ↓
   @OperationLog 注解
         ↓
   OperationLogAspect (切面)
         ↓
   LogFeignClient (Feign客户端)
         ↓ Feign调用
   UserService LogController
         ↓
   LogService (日志服务)
         ↓
   SysOperationLogRepository
         ↓
   user_service_db.sys_operation_log (数据库)
```

**降级处理**：
- 如果 UserService 不可用，触发 `LogFeignClientFallback`
- 本地记录警告日志，业务继续执行
- 日志保存失败不影响业务

#### 9.3 查询操作日志

使用 UserService 的 LogController 提供的API查询日志：

```java
// 前端示例：分页查询操作日志
import { getOperationLogs } from '@/api/log'

const loadLogs = async () => {
  const res = await getOperationLogs({
    page: 1,
    size: 10,
    username: 'admin',      // 可选：操作人用户名
    module: '用户管理',      // 可选：操作模块
    startDate: '2025-10-01T00:00:00',  // 可选：开始时间
    endDate: '2025-10-14T23:59:59',    // 可选：结束时间
    keyword: '创建'           // 可选：关键词
  })
  console.log(res.data.list)  // 日志列表
  console.log(res.data.total) // 总记录数
}
```

#### 9.4 导出操作日志为Excel

```java
// 前端示例：导出Excel
import { exportOperationLogs } from '@/api/log'

const handleExport = async () => {
  const res = await exportOperationLogs({
    module: '用户管理',
    startDate: '2025-10-01T00:00:00',
    endDate: '2025-10-14T23:59:59'
  })
  
  // 创建下载链接
  const blob = new Blob([res], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'operation_logs.xlsx'
  link.click()
  window.URL.revokeObjectURL(url)
}
```

#### 9.5 Logback配置

`logback-spring.xml` 已预配置：
- 控制台彩色输出
- 文件持久化（按日期+大小滚动）
- INFO/ERROR分级输出
- 异步写入提升性能
- 保留30天历史日志

日志文件位置：`./logs/${spring.application.name}/`

#### 9.6 微服务日志功能接入

**步骤1：启用 Feign 客户端扫描**

在微服务启动类添加 Feign 扫描配置（ProductService、LeaderService 等）：

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.bcu.edu.common.feign", "com.bcu.edu"})
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
```

**步骤2：在 Controller 中使用 @OperationLog 注解**

```java
@RestController
@RequestMapping("/api/product/admin/category")
public class AdminCategoryController {
    
    @OperationLog(value = "创建分类", module = "商品管理")
    @PostMapping
    public Result<Category> createCategory(@RequestBody CategoryRequest request) {
        // 业务逻辑...
    }
}
```

**步骤3：验证日志保存**

```sql
-- 查询 UserService 数据库中的日志
SELECT * FROM user_service_db.sys_operation_log 
WHERE module = '商品管理' 
ORDER BY create_time DESC;
```

**特殊说明（UserService）**：
- UserService 自身不需要配置 Feign 扫描（因为它是日志服务提供方）
- UserService 的日志可以选择直接使用本地 Repository 保存
- `OperationLogAspect` 中已处理循环依赖问题（`@Autowired(required = false)`）

## 注意事项

1. **密钥安全**：生产环境中，加密密钥、JWT密钥、数据库密码等敏感信息应从配置文件或环境变量读取，不要硬编码在代码中。

2. **异常处理**：业务逻辑中应使用`BusinessException`抛出业务异常，全局异常处理器会自动捕获并返回统一格式。

3. **Token刷新**：JWT Token有效期为7天，前端应在Token即将过期时调用刷新接口。

4. **跨域配置**：生产环境中应将`allowedOriginPatterns("*")`改为具体的前端域名。

5. **日志记录**：所有工具类已集成Slf4j日志，异常会自动记录到日志文件。

6. **日志系统依赖**：操作日志通过 Feign 调用 UserService 保存，需确保：
   - UserService 服务正常运行
   - Consul 服务发现可用
   - 如果 UserService 不可用，会触发 Fallback 降级，业务不受影响
   
7. **编译顺序**：修改 common 模块后，需重新编译：
   ```bash
   mvn clean install -pl common -am
   ```

## 版本历史

- **v3.0** (2025-10-31): 日志系统微服务改造 ⭐重大更新
  - 日志系统改造为 Feign 调用方式
  - 新增 LogFeignClient 和 LogFeignClientFallback
  - 新增 OperationLogDTO 跨服务传输对象
  - 日志集中存储在 UserService 数据库
  - Fallback 降级处理，确保高可用
  - 符合微服务架构最佳实践
  - 详细文档：`docs/社区团购系统/三级文档（归档）/日志系统改造完成报告.md`

- **v2.0** (2025-10-14): 日志系统版本
  - 双轨制日志架构（Logback + AOP）
  - @OperationLog注解自动记录操作日志
  - 异步日志保存提升性能
  - 敏感参数自动脱敏
  - 日志查询与Excel导出API
  - IP地址获取工具
  - Excel导出工具

- **v1.0** (2025-10-12): 初始版本
  - 统一返回结果封装
  - 全局异常处理
  - 安全工具类（加密、JWT）
  - 日期工具类
  - 常量定义

## 作者

耿康瑞 - 北京城市学院软件工程专业

## 许可证

本项目仅用于毕业设计，未经允许不得用于商业用途。

