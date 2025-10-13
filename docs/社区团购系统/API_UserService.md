# 社区团购系统 - 用户服务API接口文档

**服务名称**: UserService  
**服务端口**: 8061  
**Base URL**: `http://localhost:8061`  
**版本**: v1.1.0  
**文档日期**: 2025-10-12  
**最后更新**: 2025-10-12 19:30

---

## 目录

1. [接口概述](#1-接口概述)
2. [认证方式](#2-认证方式)
3. [通用响应格式](#3-通用响应格式)
4. [错误码说明](#4-错误码说明)
5. [用户管理接口](#5-用户管理接口)
6. [地址管理接口](#6-地址管理接口)
7. [账户管理接口](#7-账户管理接口)
8. [反馈管理接口](#8-反馈管理接口)
9. [Swagger文档](#9-swagger文档)

---

## 1. 接口概述

### 1.1 服务功能

用户服务（UserService）是社区团购系统的核心服务之一，负责：

- ✅ 用户注册与登录
- ✅ 用户信息管理（CRUD）
- ✅ 用户角色管理（普通用户、团长、管理员）
- ✅ 收货地址管理
- ✅ 用户账户余额管理
- ✅ 用户反馈管理

### 1.2 技术栈

- **框架**: Spring Boot 3.2.3
- **数据库**: MySQL 8.0.36
- **ORM**: Spring Data JPA
- **API文档**: SpringDoc OpenAPI 2.3.0
- **认证**: JWT Token
- **服务注册**: Consul

---

## 2. 认证方式

### 2.1 JWT Token认证

除白名单接口外，所有API请求都需要在请求头中携带JWT Token：

```http
Authorization: Bearer <your_jwt_token>
```

### 2.2 白名单接口（无需认证）

以下接口无需JWT Token：

- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /actuator/health` - 健康检查
- `/swagger-ui/**` - Swagger UI
- `/api-docs/**` - API文档

### 2.3 获取Token

通过登录接口获取Token：

```bash
POST /api/user/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}
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
  },
  "timestamp": "2025-10-12T10:30:00"
}
```

---

## 3. 通用响应格式

### 3.1 成功响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": "2025-10-12T10:30:00"
}
```

### 3.2 失败响应

```json
{
  "code": 400,
  "message": "参数验证失败",
  "data": null,
  "timestamp": "2025-10-12T10:30:00"
}
```

### 3.3 分页响应

**说明**: 后端使用 MyBatis PageHelper 进行分页，返回格式如下：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "size": 10,
    "startRow": 1,
    "endRow": 10,
    "total": 100,
    "pages": 10,
    "list": [ ... ],
    "prePage": 0,
    "nextPage": 2,
    "isFirstPage": true,
    "isLastPage": false,
    "hasPreviousPage": false,
    "hasNextPage": true,
    "navigatePages": 8,
    "navigatepageNums": [1, 2, 3, 4, 5, 6, 7, 8],
    "navigateFirstPage": 1,
    "navigateLastPage": 8
  },
  "timestamp": "2025-10-12T10:30:00"
}
```

**重要字段说明**:
- `list`: 当前页的数据数组（**注意不是 `data` 字段！**）
- `total`: 总记录数
- `pages`: 总页数
- `pageNum`: 当前页码
- `pageSize`: 每页大小

---

## 4. 错误码说明

| 错误码 | 说明 | 处理建议 |
|--------|------|----------|
| 200 | 操作成功 | - |
| 400 | 参数验证失败 | 检查请求参数格式 |
| 401 | 未授权 | 请先登录或检查Token |
| 403 | 无权限 | 当前用户无权访问 |
| 404 | 资源不存在 | 检查请求路径或资源ID |
| 1001 | 用户不存在 | 用户ID错误 |
| 1002 | 用户名已存在 | 更换用户名 |
| 1003 | 手机号已存在 | 更换手机号 |
| 1004 | 账户已禁用 | 联系管理员 |
| 1005 | 余额不足 | 充值后再试 |

---

## 5. 用户管理接口

### 5.1 用户注册

**接口**: `POST /api/user/register`  
**权限**: 无需认证  
**描述**: 新用户注册，支持普通用户和团长注册

#### 请求参数

```json
{
  "username": "testuser",        // 必填，3-50字符，字母数字下划线
  "password": "123456",          // 必填，6-20字符
  "phone": "13800138000",        // 必填，手机号格式
  "role": 1,                     // 必填，1-普通用户；2-团长；3-管理员
  "realName": "张三",            // 可选，真实姓名
  "wxOpenid": "oXXXXXXXXXXXX"   // 可选，微信OpenID
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1001,
    "username": "testuser",
    "role": 1,
    "roleName": "普通用户",
    "realName": "张三",
    "phone": "13800138000",
    "status": 1,
    "statusName": "正常",
    "createTime": "2025-10-12T10:30:00"
  }
}
```

---

### 5.2 用户登录

**接口**: `POST /api/user/login`  
**权限**: 无需认证  
**描述**: 用户名密码登录，返回JWT Token

#### 请求参数

```json
{
  "username": "testuser",
  "password": "123456"
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 604800,
    "userInfo": {
      "userId": 1001,
      "username": "testuser",
      "role": 1,
      "roleName": "普通用户",
      "status": 1
    }
  }
}
```

---

### 5.3 获取用户信息

**接口**: `GET /api/user/info/{userId}`  
**权限**: 需要认证  
**描述**: 根据用户ID获取用户详细信息

#### 路径参数

- `userId` (Long): 用户ID

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1001,
    "username": "testuser",
    "role": 1,
    "roleName": "普通用户",
    "realName": "张三",
    "phone": "13800138000",
    "avatar": "https://example.com/avatar.jpg",
    "status": 1,
    "statusName": "正常",
    "createTime": "2025-10-12T10:30:00",
    "updateTime": "2025-10-12T11:00:00"
  }
}
```

---

### 5.4 更新用户信息

**接口**: `PUT /api/user/update/{userId}`  
**权限**: 需要认证  
**描述**: 更新用户基本信息（密码、手机号、头像等）

#### 路径参数

- `userId` (Long): 用户ID

#### 请求参数

```json
{
  "password": "newPassword123",         // 可选，6-20字符
  "realName": "张三丰",                 // 可选
  "phone": "13900139000",               // 可选，手机号格式
  "avatar": "https://example.com/new.jpg", // 可选
  "wxOpenid": "oYYYYYYYYYYY"           // 可选
}
```

#### 响应示例

同 `5.3 获取用户信息`

---

### 5.5 删除用户（禁用）

**接口**: `DELETE /api/user/delete/{userId}`  
**权限**: 管理员  
**描述**: 逻辑删除用户，将用户状态设为禁用

#### 路径参数

- `userId` (Long): 用户ID

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

### 5.6 搜索用户

**接口**: `GET /api/user/search?keyword={keyword}`  
**权限**: 管理员  
**描述**: 根据关键词搜索用户（用户名或真实姓名）

#### 查询参数

- `keyword` (String): 搜索关键词

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "userId": 1001,
      "username": "testuser",
      "realName": "张三",
      "role": 1,
      "roleName": "普通用户"
    },
    ...
  ]
}
```

---

### 5.7 按角色查询用户

**接口**: `GET /api/user/role/{role}`  
**权限**: 管理员  
**描述**: 查询指定角色的所有用户

#### 路径参数

- `role` (Integer): 角色（1-普通用户；2-团长；3-管理员）

#### 响应示例

同 `5.6 搜索用户`

---

### 5.8 更改用户角色

**接口**: `PUT /api/user/role/{userId}?role={role}`  
**权限**: 管理员  
**描述**: 管理员功能：更改用户角色

#### 路径参数

- `userId` (Long): 用户ID

#### 查询参数

- `role` (Integer): 新角色（1-普通用户；2-团长；3-管理员）

#### 响应示例

同 `5.3 获取用户信息`

---

### 5.9 更改用户状态

**接口**: `PUT /api/user/status/{userId}?status={status}`  
**权限**: 管理员  
**描述**: 管理员功能：启用或禁用用户

#### 路径参数

- `userId` (Long): 用户ID

#### 查询参数

- `status` (Integer): 状态（0-禁用；1-正常）

#### 响应示例

同 `5.3 获取用户信息`

---

### 5.10 用户统计

**接口**: `GET /api/user/statistics`  
**权限**: 管理员  
**描述**: 获取用户统计信息（总数、各角色数量等）

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalUsers": 1523,
    "normalUsers": 1450,
    "leaders": 68,
    "admins": 5,
    "activeUsers": 1500,
    "disabledUsers": 23
  }
}
```

---

### 5.11 管理员创建用户

**接口**: `POST /api/user/admin/create`  
**权限**: 管理员  
**描述**: 管理员功能：手动创建新用户账号

#### 请求参数

```json
{
  "username": "newuser",           // 必填，用户名（3-50字符）
  "password": "123456",            // 必填，密码（6-20字符）
  "phone": "13800138000",          // 必填，手机号（11位）
  "role": 1,                       // 必填，角色（1-普通用户；2-团长；3-管理员）
  "realName": "新用户",            // 可选，真实姓名
  "wxOpenid": "oXXXXXXXXXXXX"     // 可选，微信OpenID
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1524,
    "username": "newuser",
    "role": 1,
    "roleName": "普通用户",
    "realName": "新用户",
    "phone": "13800138000",
    "status": 1,
    "statusName": "正常",
    "createTime": "2025-10-12T19:30:00"
  }
}
```

#### 错误示例

```json
{
  "code": 1002,
  "message": "用户名已存在",
  "data": null
}
```

---

## 6. 地址管理接口

### 6.1 新增收货地址

**接口**: `POST /api/address/add/{userId}`  
**权限**: 需要认证  
**描述**: 为当前用户新增一个收货地址

#### 路径参数

- `userId` (Long): 用户ID

#### 请求参数

```json
{
  "receiver": "张三",              // 必填，收件人
  "phone": "13800138000",          // 必填，收件人电话
  "province": "北京市",            // 必填，省份
  "city": "北京市",                // 必填，城市
  "district": "海淀区",            // 必填，区/县
  "detail": "中关村大街1号",       // 必填，详细地址
  "longitude": 116.308074,         // 必填，经度
  "latitude": 39.974701,           // 必填，纬度
  "isDefault": 0                   // 可选，是否默认（0-否；1-是）
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "addressId": 1,
    "userId": 1001,
    "receiver": "张三",
    "phone": "13800138000",
    "province": "北京市",
    "city": "北京市",
    "district": "海淀区",
    "detail": "中关村大街1号",
    "fullAddress": "北京市北京市海淀区中关村大街1号",
    "longitude": 116.308074,
    "latitude": 39.974701,
    "isDefault": 0
  }
}
```

---

### 6.2 更新收货地址

**接口**: `PUT /api/address/update/{userId}/{addressId}`  
**权限**: 需要认证  
**描述**: 更新指定的收货地址信息

#### 路径参数

- `userId` (Long): 用户ID
- `addressId` (Long): 地址ID

#### 请求参数

同 `6.1 新增收货地址`

#### 响应示例

同 `6.1 新增收货地址`

---

### 6.3 删除收货地址

**接口**: `DELETE /api/address/delete/{userId}/{addressId}`  
**权限**: 需要认证  
**描述**: 删除指定的收货地址

#### 路径参数

- `userId` (Long): 用户ID
- `addressId` (Long): 地址ID

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

### 6.4 获取用户所有地址

**接口**: `GET /api/address/list/{userId}`  
**权限**: 需要认证  
**描述**: 查询当前用户的所有收货地址

#### 路径参数

- `userId` (Long): 用户ID

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "addressId": 1,
      "userId": 1001,
      "receiver": "张三",
      "fullAddress": "北京市北京市海淀区中关村大街1号",
      "isDefault": 1
    },
    ...
  ]
}
```

---

### 6.5 获取默认地址

**接口**: `GET /api/address/default/{userId}`  
**权限**: 需要认证  
**描述**: 获取用户的默认收货地址

#### 路径参数

- `userId` (Long): 用户ID

#### 响应示例

同 `6.1 新增收货地址`

---

### 6.6 设置默认地址

**接口**: `PUT /api/address/default/{userId}/{addressId}`  
**权限**: 需要认证  
**描述**: 将指定地址设置为默认收货地址

#### 路径参数

- `userId` (Long): 用户ID
- `addressId` (Long): 地址ID

#### 响应示例

同 `6.1 新增收货地址`

---

### 6.7 获取地址详情

**接口**: `GET /api/address/detail/{userId}/{addressId}`  
**权限**: 需要认证  
**描述**: 查询指定地址的详细信息

#### 路径参数

- `userId` (Long): 用户ID
- `addressId` (Long): 地址ID

#### 响应示例

同 `6.1 新增收货地址`

---

## 7. 账户管理接口

### 7.1 获取账户信息

**接口**: `GET /api/account/{userId}`  
**权限**: 需要认证  
**描述**: 查询用户账户余额、冻结金额等信息

#### 路径参数

- `userId` (Long): 用户ID

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "accountId": 1,
    "userId": 1001,
    "balance": 1500.50,
    "freezeAmount": 200.00,
    "totalAmount": 1700.50,
    "updateTime": "2025-10-12T11:00:00"
  }
}
```

---

### 7.2 账户充值

**接口**: `POST /api/account/recharge/{userId}?amount={amount}`  
**权限**: 需要认证  
**描述**: 用户充值，增加可用余额

#### 路径参数

- `userId` (Long): 用户ID

#### 查询参数

- `amount` (BigDecimal): 充值金额

#### 响应示例

同 `7.1 获取账户信息`

---

### 7.3 账户扣款

**接口**: `POST /api/account/deduct/{userId}?amount={amount}`  
**权限**: 需要认证  
**描述**: 扣除用户可用余额（如支付订单）

#### 路径参数

- `userId` (Long): 用户ID

#### 查询参数

- `amount` (BigDecimal): 扣款金额

#### 响应示例

同 `7.1 获取账户信息`

---

### 7.4 冻结金额

**接口**: `POST /api/account/freeze/{userId}?amount={amount}`  
**权限**: 需要认证  
**描述**: 冻结用户部分余额（如待结算佣金）

#### 路径参数

- `userId` (Long): 用户ID

#### 查询参数

- `amount` (BigDecimal): 冻结金额

#### 响应示例

同 `7.1 获取账户信息`

---

### 7.5 解冻金额

**接口**: `POST /api/account/unfreeze/{userId}?amount={amount}`  
**权限**: 需要认证  
**描述**: 解冻用户冻结金额，恢复到可用余额

#### 路径参数

- `userId` (Long): 用户ID

#### 查询参数

- `amount` (BigDecimal): 解冻金额

#### 响应示例

同 `7.1 获取账户信息`

---

## 8. 反馈管理接口

### 8.1 提交反馈

**接口**: `POST /api/feedback/submit/{userId}`  
**权限**: 需要认证  
**描述**: 用户提交问题反馈

#### 路径参数

- `userId` (Long): 用户ID

#### 请求参数

```json
{
  "type": 1,                                    // 必填，反馈类型（1-功能问题；2-商品问题；3-配送问题；4-支付问题；5-其他）
  "content": "无法正常下单，系统一直提示错误", // 必填，反馈内容
  "images": "https://example.com/img1.jpg,https://example.com/img2.jpg" // 可选，图片URL（多张用逗号分隔）
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "feedbackId": 1,
    "userId": 1001,
    "type": 1,
    "typeName": "功能问题",
    "content": "无法正常下单，系统一直提示错误",
    "images": "https://example.com/img1.jpg,https://example.com/img2.jpg",
    "status": 0,
    "statusName": "待处理",
    "reply": null,
    "replyTime": null,
    "createTime": "2025-10-12T10:30:00"
  }
}
```

---

### 8.2 获取用户反馈列表

**接口**: `GET /api/feedback/user/{userId}`  
**权限**: 需要认证  
**描述**: 查询指定用户的所有反馈记录

#### 路径参数

- `userId` (Long): 用户ID

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "feedbackId": 1,
      "userId": 1001,
      "type": 1,
      "typeName": "功能问题",
      "status": 0,
      "statusName": "待处理",
      "createTime": "2025-10-12T10:30:00"
    },
    ...
  ]
}
```

---

### 8.3 分页查询用户反馈

**接口**: `GET /api/feedback/user/{userId}/page?page={page}&size={size}`  
**权限**: 需要认证  
**描述**: 分页查询指定用户的反馈记录

#### 路径参数

- `userId` (Long): 用户ID

#### 查询参数

- `page` (int): 页码，默认1
- `size` (int): 每页数量，默认10

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "pageNum": 1,
    "pageSize": 10,
    "total": 25,
    "pages": 3,
    "list": [
      {
        "feedbackId": 1,
        "userId": 1001,
        "type": 1,
        "typeName": "功能问题",
        "content": "无法正常下单，系统一直提示错误",
        "status": 0,
        "statusName": "待处理",
        "createTime": "2025-10-12T10:30:00"
      }
    ]
  }
}
```

---

### 8.4 获取反馈详情

**接口**: `GET /api/feedback/{feedbackId}`  
**权限**: 需要认证  
**描述**: 查询指定反馈的详细信息

#### 路径参数

- `feedbackId` (Long): 反馈ID

#### 响应示例

同 `8.1 提交反馈`

---

### 8.5 管理员回复反馈

**接口**: `POST /api/feedback/reply`  
**权限**: 管理员  
**描述**: 管理员处理并回复用户反馈

#### 请求参数

```json
{
  "feedbackId": 1,                // 必填，反馈ID
  "reply": "问题已修复，请更新到最新版本", // 必填，回复内容
  "status": 2                     // 必填，处理状态（0-待处理；1-处理中；2-已解决；3-已关闭）
}
```

#### 响应示例

同 `8.1 提交反馈`

---

### 8.6 查询所有反馈（管理员）

**接口**: `GET /api/feedback/all?page={page}&size={size}`  
**权限**: 管理员  
**描述**: 管理员分页查询所有用户反馈

#### 查询参数

- `page` (int): 页码，默认1
- `size` (int): 每页数量，默认10

#### 响应示例

同 `8.3 分页查询用户反馈`

---

### 8.7 按状态查询反馈（管理员）

**接口**: `GET /api/feedback/status/{status}?page={page}&size={size}`  
**权限**: 管理员  
**描述**: 管理员按处理状态分页查询反馈

#### 路径参数

- `status` (Integer): 处理状态（0-待处理；1-处理中；2-已解决；3-已关闭）

#### 查询参数

- `page` (int): 页码，默认1
- `size` (int): 每页数量，默认10

#### 响应示例

同 `8.3 分页查询用户反馈`

---

### 8.8 删除反馈

**接口**: `DELETE /api/feedback/delete/{feedbackId}`  
**权限**: 管理员  
**描述**: 删除指定的反馈记录

#### 路径参数

- `feedbackId` (Long): 反馈ID

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 9. Swagger文档

### 9.1 访问Swagger UI

启动UserService后，访问以下地址查看交互式API文档：

```
http://localhost:8061/swagger-ui.html
```

### 9.2 OpenAPI JSON

```
http://localhost:8061/api-docs
```

### 9.3 在Swagger中使用JWT

1. 点击右上角 `Authorize` 按钮
2. 在弹出框中输入JWT Token（不需要Bearer前缀）
3. 点击 `Authorize`
4. 现在可以直接调用需要认证的接口

---

## 附录：数据字典

### 用户角色枚举

| 值 | 说明 |
|----|------|
| 1 | 普通用户（社区居民） |
| 2 | 团长（社区服务点负责人） |
| 3 | 管理员（系统管理员） |

### 用户状态枚举

| 值 | 说明 |
|----|------|
| 0 | 禁用 |
| 1 | 正常 |

### 反馈类型枚举

| 值 | 说明 |
|----|------|
| 1 | 功能问题 |
| 2 | 商品问题 |
| 3 | 配送问题 |
| 4 | 支付问题 |
| 5 | 其他 |

### 反馈状态枚举

| 值 | 说明 |
|----|------|
| 0 | 待处理 |
| 1 | 处理中 |
| 2 | 已解决 |
| 3 | 已关闭 |

---

## 联系方式

**开发者**: 耿康瑞  
**学号**: 20221204229  
**邮箱**: example@bcu.edu.cn  
**项目**: 基于Spring Boot的社区团购系统

---

**文档结束**

