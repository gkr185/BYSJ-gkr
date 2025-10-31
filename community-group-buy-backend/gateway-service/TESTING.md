# Gateway Service 测试验证指南

## 📋 测试前准备

### 1. 确认服务状态

#### 检查清单
- [ ] Consul已启动（http://localhost:8500）
- [ ] UserService已启动并注册到Consul
- [ ] ProductService已启动并注册到Consul
- [ ] LeaderService已启动并注册到Consul
- [ ] GroupBuyService已启动并注册到Consul ⭐ 新增
- [ ] Gateway已启动并注册到Consul

#### 启动命令

```bash
# 1. 启动Consul（开发模式）
consul agent -dev

# 2. 启动UserService
cd community-group-buy-backend/UserService
mvn spring-boot:run

# 3. 启动ProductService
cd community-group-buy-backend/ProductService
mvn spring-boot:run

# 4. 启动LeaderService
cd community-group-buy-backend/LeaderService
mvn spring-boot:run

# 5. 启动GroupBuyService ⭐ 新增
cd community-group-buy-backend/GroupBuyService
mvn spring-boot:run

# 6. 启动Gateway
cd community-group-buy-backend/gateway-service
mvn spring-boot:run
```

### 2. 验证服务注册

访问Consul控制台：http://localhost:8500/ui/dc1/services

确认看到以下服务：
- ✅ gateway-service
- ✅ UserService
- ✅ ProductService
- ✅ LeaderService
- ✅ GroupBuyService ⭐ 新增

---

## 🧪 API测试（使用curl）

### 测试1：健康检查（无需Token）

```bash
curl http://localhost:9000/actuator/health
```

**预期结果**：
```json
{
  "status": "UP"
}
```

---

### 测试2：用户注册（白名单，无需Token）

```bash
curl -X POST http://localhost:9000/api/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "gateway_test",
    "password": "123456",
    "realName": "网关测试用户",
    "phone": "13900139000",
    "role": 0
  }'
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "gateway_test",
    "realName": "网关测试用户",
    ...
  }
}
```

---

### 测试3：用户登录（白名单，无需Token）

```bash
curl -X POST http://localhost:9000/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "gateway_test",
    "password": "123456"
  }'
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 604800,
    "userInfo": {
      "userId": 1,
      "username": "gateway_test",
      ...
    }
  }
}
```

**⚠️ 重要**：保存返回的Token，后续测试需要！

---

### 测试4：获取用户信息（需要Token）

```bash
# 替换YOUR_TOKEN为实际Token
export TOKEN="YOUR_JWT_TOKEN_HERE"

curl -X GET http://localhost:9000/api/user/info/1 \
  -H "Authorization: Bearer $TOKEN"
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "gateway_test",
    "realName": "网关测试用户",
    ...
  }
}
```

---

### 测试5：无Token访问（应该401）

```bash
curl -X GET http://localhost:9000/api/user/info/1
```

**预期结果**（401错误）：
```json
{
  "code": 401,
  "message": "请先登录",
  "data": null,
  "timestamp": 1698646800000
}
```

---

### 测试6：错误Token（应该401）

```bash
curl -X GET http://localhost:9000/api/user/info/1 \
  -H "Authorization: Bearer INVALID_TOKEN"
```

**预期结果**（401错误）：
```json
{
  "code": 401,
  "message": "Token无效",
  "data": null
}
```

---

### 测试7：更新用户信息

```bash
curl -X PUT http://localhost:9000/api/user/update/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "realName": "网关测试用户(已更新)",
    "phone": "13900139001"
  }'
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "realName": "网关测试用户(已更新)",
    "phone": "13900139001",
    ...
  }
}
```

---

### 测试8：获取商品列表（ProductService，无需Token）

```bash
curl -X GET "http://localhost:9000/api/product/list?page=0&size=10"
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "productId": 1,
        "productName": "新鲜苹果",
        "price": 9.99,
        "stock": 100,
        "status": 1
      }
    ],
    "totalElements": 10,
    "totalPages": 1
  }
}
```

---

### 测试9：获取分类列表（ProductService，无需Token）

```bash
curl -X GET "http://localhost:9000/api/category/list"
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "categoryId": 1,
      "categoryName": "水果",
      "parentId": 0,
      "sort": 1,
      "status": 1
    }
  ]
}
```

---

### 测试10：商品搜索（ProductService，无需Token）

```bash
curl -X GET "http://localhost:9000/api/product/search?keyword=苹果&page=0&size=10"
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "productId": 1,
        "productName": "新鲜苹果",
        "price": 9.99,
        "stock": 100
      }
    ]
  }
}
```

---

### 测试11：创建商品（管理端，需要Token + 管理员权限）

```bash
curl -X POST http://localhost:9000/api/admin/product \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 1,
    "productName": "测试商品",
    "coverImg": "http://example.com/image.jpg",
    "detail": "这是一个测试商品",
    "price": 19.99,
    "groupPrice": 15.99,
    "stock": 100
  }'
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "productId": 2,
    "productName": "测试商品",
    "price": 19.99,
    "status": 1
  }
}
```

---

### 测试12：Feign内部接口 - 扣减库存

```bash
curl -X POST http://localhost:9000/feign/product/1/stock/deduct \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5
  }'
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": true
}
```

---

## 🌐 前端测试

### 用户端测试（端口5173）

#### 步骤1：启动前端
```bash
cd community-group-buy-frontend
npm run dev
```

访问：http://localhost:5173

#### 步骤2：测试登录
1. 点击"登录"或访问个人中心
2. 输入用户名：`gateway_test`
3. 输入密码：`123456`
4. 点击登录

**预期结果**：
- ✅ 登录成功
- ✅ 自动跳转到个人中心
- ✅ localStorage中保存了Token

#### 步骤3：测试个人信息
1. 点击"个人信息"
2. 查看用户信息是否正确显示
3. 修改真实姓名或手机号
4. 点击保存

**预期结果**：
- ✅ 信息显示正确
- ✅ 修改成功并提示

#### 步骤4：测试地址管理
1. 点击"收货地址"
2. 点击"添加地址"
3. 填写地址信息
4. 点击保存

**预期结果**：
- ✅ 地址添加成功
- ✅ 列表中显示新地址

#### 步骤5：测试余额查询
1. 点击"我的余额"
2. 查看余额信息

**预期结果**：
- ✅ 余额正确显示
- ✅ 可以查看余额变动记录

#### 步骤6：测试反馈提交
1. 点击"意见反馈"
2. 选择反馈类型
3. 填写反馈内容
4. 点击提交

**预期结果**：
- ✅ 反馈提交成功
- ✅ 提示消息显示

---

### 管理端测试（端口5174）

#### 步骤1：启动前端
```bash
cd community-group-buy-admin
npm run dev
```

访问：http://localhost:5174

#### 步骤2：测试登录
1. 访问管理端
2. 输入管理员用户名和密码
3. 点击登录

**预期结果**：
- ✅ 登录成功
- ✅ 跳转到用户管理页面

#### 步骤3：测试用户管理
1. 查看用户列表
2. 搜索用户
3. 查看用户详情
4. 编辑用户信息

**预期结果**：
- ✅ 用户列表正确显示
- ✅ 搜索功能正常
- ✅ 编辑功能正常

#### 步骤4：测试反馈管理
1. 点击"反馈管理"
2. 查看反馈列表
3. 回复用户反馈

**预期结果**：
- ✅ 反馈列表正确显示
- ✅ 回复功能正常

#### 步骤5：测试日志管理
1. 点击"日志管理"
2. 查看操作日志
3. 筛选日志（按模块、用户等）
4. 导出Excel

**预期结果**：
- ✅ 日志列表正确显示
- ✅ 筛选功能正常
- ✅ Excel导出成功

---

## 📊 日志验证

### Gateway日志

查看Gateway控制台输出，应该看到：

```
===> Gateway请求开始 | RequestId: xxx | POST /api/user/login | IP: 127.0.0.1
用户认证成功: userId=1, username=gateway_test, role=0, path=/api/user/info/1
<=== Gateway请求结束 | RequestId: xxx | GET /api/user/info/1 | 状态码: 200 | 耗时: 45ms
```

### UserService日志

查看UserService控制台输出，应该看到：

```
网关请求，用户信息: userId=1, username=gateway_test
```

---

## ✅ 测试清单

### 基础功能测试
- [ ] 健康检查接口正常
- [ ] 用户注册功能正常（白名单）
- [ ] 用户登录功能正常（白名单）
- [ ] JWT鉴权正常（有Token可访问）
- [ ] JWT鉴权拦截正常（无Token返回401）
- [ ] 错误Token被拒绝（返回401）

### UserService接口测试（通过网关）
- [ ] 获取用户信息
- [ ] 更新用户信息
- [ ] 删除用户
- [ ] 搜索用户
- [ ] 按角色查询用户
- [ ] 地址管理（CRUD）
- [ ] 账户管理（查询/充值/扣款）
- [ ] 反馈管理（提交/查询/回复）

### 前端集成测试
- [ ] 用户端登录
- [ ] 用户端个人信息
- [ ] 用户端地址管理
- [ ] 用户端余额查询
- [ ] 用户端反馈提交
- [ ] 管理端登录
- [ ] 管理端用户管理
- [ ] 管理端反馈管理
- [ ] 管理端日志管理

### 非功能测试
- [ ] CORS跨域正常
- [ ] 请求日志记录正常
- [ ] 服务注册到Consul
- [ ] 负载均衡正常（如有多实例）
- [ ] 性能测试（响应时间<100ms）

---

## 🐛 常见测试问题

### 问题1：Consul连接失败
**现象**：Gateway启动报错，无法连接Consul  
**检查**：
```bash
# 检查Consul是否启动
curl http://localhost:8500/v1/status/leader
```
**解决**：启动Consul `consul agent -dev`

---

### 问题2：UserService未注册
**现象**：Gateway启动成功，但调用UserService 503错误  
**检查**：访问 http://localhost:8500/ui 查看服务列表  
**解决**：启动UserService

---

### 问题3：401错误（登录后仍然）
**现象**：登录成功，但调用其他接口返回401  
**检查**：
1. Token是否正确保存到localStorage
2. 请求头是否正确添加`Authorization: Bearer xxx`
3. Token是否过期

**调试**：在浏览器控制台查看Network请求头

---

### 问题4：CORS跨域错误
**现象**：浏览器控制台报CORS错误  
**检查**：Gateway的CORS配置是否正确  
**解决**：检查`application.yml`中的`globalcors`配置

---

### 问题5：JWT验证失败
**现象**：Token明明有效，但返回401  
**检查**：
1. Gateway和UserService的JWT密钥是否一致
2. Token格式是否正确（`Bearer xxx`）

**调试**：查看Gateway和UserService日志

---

## 📈 性能测试（可选）

### 使用Apache Bench

```bash
# 测试登录接口性能（100请求，10并发）
ab -n 100 -c 10 -p login.json -T application/json \
  http://localhost:9000/api/user/login

# login.json内容
echo '{"username":"gateway_test","password":"123456"}' > login.json
```

**预期结果**：
- 平均响应时间 < 100ms
- 错误率 = 0%

---

## 🎯 GroupBuyService测试（⭐新增）

### 测试10：获取活动列表（白名单，无需Token）

```bash
curl -X GET http://localhost:9000/api/groupbuy/activities
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "activityId": 1,
      "productId": 1,
      "groupPrice": 19.90,
      "requiredNum": 3,
      "status": 1,
      ...
    }
  ]
}
```

---

### 测试11：团长发起拼团（需要团长Token）

```bash
# 需要团长身份（role=2）
export LEADER_TOKEN="YOUR_LEADER_TOKEN_HERE"

curl -X POST http://localhost:9000/api/groupbuy/team/launch \
  -H "Authorization: Bearer $LEADER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "activityId": 1,
    "joinImmediately": true,
    "addressId": 1,
    "quantity": 1
  }'
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "teamId": 1,
    "teamNo": "T20251031001",
    "communityId": 10,
    "communityName": "幸福小区",
    "requiredNum": 3,
    "currentNum": 1,
    "remainNum": 2,
    "teamStatus": 0,
    "teamStatusDesc": "拼团中",
    ...
  }
}
```

**⚠️ 注意**：
- 仅团长可发起（role=2）
- 自动关联团长的社区（v3.0特性）
- 团号格式：T + yyyyMMdd + 6位随机数

---

### 测试12：用户参与拼团（需要Token）

```bash
export USER_TOKEN="YOUR_USER_TOKEN_HERE"

curl -X POST http://localhost:9000/api/groupbuy/team/join \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "teamId": 1,
    "addressId": 2,
    "quantity": 1
  }'
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "orderId": 8002,
    "teamId": 1,
    "teamNo": "T20251031001",
    "currentNum": 2,
    "requiredNum": 3,
    "remainNum": 1,
    "payAmount": 19.90,
    "expireTime": "2025-11-01 20:00:00"
  }
}
```

**⚠️ 技术亮点**：
- 行锁防并发（SELECT ... FOR UPDATE）
- 防重复参团（唯一索引 uk_team_user）
- Feign调用OrderService创建订单

---

### 测试13：获取团详情（白名单，无需Token）

```bash
curl -X GET http://localhost:9000/api/groupbuy/team/1/detail
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "teamId": 1,
    "teamNo": "T20251031001",
    "communityId": 10,
    "communityName": "幸福小区",
    "requiredNum": 3,
    "currentNum": 2,
    "remainNum": 1,
    "teamStatus": 0,
    "teamStatusDesc": "拼团中",
    "members": [
      {
        "userId": 1,
        "username": "leader123",
        "isLauncher": 1,
        "status": 0,
        "statusDesc": "待支付"
      },
      {
        "userId": 2,
        "username": "user001",
        "isLauncher": 0,
        "status": 0,
        "statusDesc": "待支付"
      }
    ],
    ...
  }
}
```

---

### 测试14：获取活动团列表（社区优先，无需Token）⭐v3.0

```bash
# 传入用户社区ID，本社区的团优先显示
curl -X GET "http://localhost:9000/api/groupbuy/activity/1/teams?communityId=10"
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "teamId": 1,
      "communityId": 10,
      "communityName": "幸福小区",
      "currentNum": 2,
      "requiredNum": 3,
      ...
    },
    {
      "teamId": 3,
      "communityId": 11,
      "communityName": "阳光小区",
      ...
    }
  ]
}
```

**⚠️ v3.0特性**：
- SQL ORDER BY CASE实现社区优先排序
- communityId=10的团排在前面
- 提升用户体验

---

### 测试15：模拟支付回调（内部接口）

```bash
# 模拟PaymentService回调
curl -X POST "http://localhost:9000/api/groupbuy/payment/callback?orderId=8002"
```

**预期结果**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

**⚠️ 核心逻辑**：
- 更新参团状态（UNPAID → PAID）
- 更新团人数（current_num++）
- 检查是否成团（current_num >= required_num）
- 幂等性保证（双重行锁 + 双重状态检查）

---

### 测试16：完整拼团流程（集成测试）

**步骤1：管理员创建活动**
```bash
export ADMIN_TOKEN="YOUR_ADMIN_TOKEN_HERE"

curl -X POST http://localhost:9000/api/groupbuy/activity \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "groupPrice": 19.90,
    "requiredNum": 3,
    "startTime": "2025-10-31T00:00:00",
    "endTime": "2025-12-31T23:59:59"
  }'
```

**步骤2：团长发起拼团**
```bash
export LEADER_TOKEN="YOUR_LEADER_TOKEN_HERE"

curl -X POST http://localhost:9000/api/groupbuy/team/launch \
  -H "Authorization: Bearer $LEADER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "activityId": 1,
    "joinImmediately": true,
    "addressId": 1,
    "quantity": 1
  }'
# 保存返回的teamId=1, orderId=8001
```

**步骤3：用户1参团**
```bash
export USER1_TOKEN="YOUR_USER1_TOKEN_HERE"

curl -X POST http://localhost:9000/api/groupbuy/team/join \
  -H "Authorization: Bearer $USER1_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "teamId": 1,
    "addressId": 2,
    "quantity": 1
  }'
# 保存返回的orderId=8002
```

**步骤4：用户2参团（满3人）**
```bash
export USER2_TOKEN="YOUR_USER2_TOKEN_HERE"

curl -X POST http://localhost:9000/api/groupbuy/team/join \
  -H "Authorization: Bearer $USER2_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "teamId": 1,
    "addressId": 3,
    "quantity": 1
  }'
# 保存返回的orderId=8003
```

**步骤5：模拟支付回调（3次）**
```bash
# 团长支付
curl -X POST "http://localhost:9000/api/groupbuy/payment/callback?orderId=8001"

# 用户1支付
curl -X POST "http://localhost:9000/api/groupbuy/payment/callback?orderId=8002"

# 用户2支付（触发成团）⭐
curl -X POST "http://localhost:9000/api/groupbuy/payment/callback?orderId=8003"
```

**步骤6：验证成团**
```bash
curl -X GET http://localhost:9000/api/groupbuy/team/1/detail
```

**预期结果**：
- teamStatus = 1（已成团）
- 所有成员status = 2（已成团）
- 订单状态 = 1（待发货）

**⚠️ 测试要点**：
- 成团逻辑只触发一次（幂等性）
- 所有成员状态同步更新
- 订单状态批量更新

---

## ✅ 测试完成标准

所有以下项目都通过，则测试完成：

1. ✅ 所有基础功能测试通过
2. ✅ 所有UserService接口测试通过
3. ✅ 所有GroupBuyService接口测试通过 ⭐ 新增
4. ✅ 完整拼团流程测试通过（集成测试）⭐ 新增
5. ✅ 用户端所有功能测试通过
6. ✅ 管理端所有功能测试通过
7. ✅ 日志记录正常
8. ✅ 无CORS错误
9. ✅ 性能测试达标（可选）

---

## 📞 测试支持

如遇到问题，请按以下步骤排查：

1. 检查Consul是否启动
2. 检查所有服务是否启动并注册
3. 查看Gateway日志
4. 查看UserService日志
5. 检查浏览器控制台Network和Console

**开发者**: 耿康瑞  
**测试日期**: 2025-10-30  
**文档版本**: v1.0

---

**状态**: ✅ 测试指南完成

