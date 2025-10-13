# Common 公共模块

## 模块说明

Common模块是社区团购系统的公共基础模块，为所有微服务提供统一的工具类、异常处理、返回结果封装等功能。

## 模块结构

```
common/
├── src/main/java/com/bcu/edu/common/
│   ├── config/                 # 配置类
│   │   └── WebConfig.java      # Web配置（CORS等）
│   ├── constant/               # 常量类
│   │   └── Constants.java      # 系统常量定义
│   ├── enums/                  # 枚举类
│   │   └── ResultCode.java     # 返回结果码枚举
│   ├── exception/              # 异常类
│   │   ├── BaseException.java      # 基础异常
│   │   └── BusinessException.java  # 业务异常
│   ├── handler/                # 处理器
│   │   └── GlobalExceptionHandler.java  # 全局异常处理器
│   ├── result/                 # 返回结果封装
│   │   ├── Result.java         # 统一返回结果
│   │   └── PageResult.java     # 分页结果
│   └── utils/                  # 工具类
│       ├── SecurityUtil.java   # 安全工具（加密、解密、脱敏）
│       ├── JwtUtil.java        # JWT工具
│       └── DateUtil.java       # 日期工具
└── pom.xml
```

## 依赖说明

### 核心依赖

- **Spring Boot Web**: 提供Web相关功能
- **Spring Boot Validation**: 参数校验
- **Lombok**: 简化代码
- **Hutool**: 工具类库（加密、日期等）
- **JWT**: JSON Web Token认证
- **Jackson**: JSON序列化

### 版本信息

- Spring Boot: 3.2.3
- Hutool: 5.8.25
- JWT: 0.11.5

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

## 注意事项

1. **密钥安全**：生产环境中，加密密钥、JWT密钥、数据库密码等敏感信息应从配置文件或环境变量读取，不要硬编码在代码中。

2. **异常处理**：业务逻辑中应使用`BusinessException`抛出业务异常，全局异常处理器会自动捕获并返回统一格式。

3. **Token刷新**：JWT Token有效期为7天，前端应在Token即将过期时调用刷新接口。

4. **跨域配置**：生产环境中应将`allowedOriginPatterns("*")`改为具体的前端域名。

5. **日志记录**：所有工具类已集成Slf4j日志，异常会自动记录到日志文件。

## 版本历史

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

