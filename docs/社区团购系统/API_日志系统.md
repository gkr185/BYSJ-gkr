# 社区团购系统 - 日志系统API文档

**版本**: v1.0  
**创建时间**: 2025-10-14  
**基础路径**: `/api/admin/logs`  
**认证方式**: JWT Bearer Token

---

## 📋 API概览

| 接口名称 | 请求方法 | 路径 | 说明 |
|---------|---------|------|------|
| 分页查询操作日志 | GET | `/operations` | 支持多条件筛选的分页查询 |
| 导出操作日志 | GET | `/export` | 导出Excel文件 |
| 获取操作模块列表 | GET | `/modules` | 获取所有模块（用于筛选） |

---

## 1. 分页查询操作日志

### 基本信息
- **接口路径**: `GET /api/admin/logs/operations`
- **功能描述**: 分页查询系统操作日志，支持多条件筛选
- **权限要求**: 管理员（role=3）

### 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| page | Integer | 否 | 1 | 页码（从1开始） |
| size | Integer | 否 | 10 | 每页大小 |
| userId | Long | 否 | - | 操作人ID |
| username | String | 否 | - | 操作人用户名（模糊查询） |
| module | String | 否 | - | 操作模块 |
| startDate | DateTime | 否 | - | 开始时间（ISO 8601格式） |
| endDate | DateTime | 否 | - | 结束时间（ISO 8601格式） |
| keyword | String | 否 | - | 关键词（操作内容模糊查询） |

### 请求示例

```http
GET /api/admin/logs/operations?module=用户管理&startDate=2025-10-01T00:00:00&endDate=2025-10-14T23:59:59&page=1&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码（200成功） |
| message | String | 响应消息 |
| data | Object | 分页数据对象 |
| data.total | Long | 总记录数 |
| data.pageNum | Integer | 当前页码 |
| data.pageSize | Integer | 每页大小 |
| data.pages | Integer | 总页数 |
| data.list | Array | 日志列表 |

#### 日志对象结构

| 字段 | 类型 | 说明 |
|------|------|------|
| logId | Long | 日志ID |
| userId | Long | 操作人ID |
| username | String | 操作人用户名 |
| operation | String | 操作内容 |
| module | String | 操作模块 |
| method | String | 方法名 |
| params | String | 请求参数（JSON格式） |
| result | String | 操作结果（SUCCESS/FAIL） |
| errorMsg | String | 错误信息 |
| duration | Integer | 执行时长（毫秒） |
| ip | String | 操作IP地址 |
| createTime | DateTime | 操作时间 |

### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 10,
    "list": [
      {
        "logId": 123,
        "userId": 1,
        "username": "admin",
        "operation": "创建用户",
        "module": "用户管理",
        "method": "com.bcu.edu.controller.UserController.adminCreateUser",
        "params": "[{\"username\":\"test\",\"password\":\"***\",\"role\":1}]",
        "result": "SUCCESS",
        "errorMsg": null,
        "duration": 120,
        "ip": "192.168.1.100",
        "createTime": "2025-10-14T10:30:00"
      },
      {
        "logId": 122,
        "userId": 1,
        "username": "admin",
        "operation": "用户登录",
        "module": "认证管理",
        "method": "com.bcu.edu.controller.UserController.login",
        "params": null,
        "result": "SUCCESS",
        "errorMsg": null,
        "duration": 85,
        "ip": "192.168.1.100",
        "createTime": "2025-10-14T10:25:00"
      }
    ]
  }
}
```

### 错误响应

```json
{
  "code": 403,
  "message": "无权限访问",
  "data": null
}
```

---

## 2. 导出操作日志

### 基本信息
- **接口路径**: `GET /api/admin/logs/export`
- **功能描述**: 根据查询条件导出操作日志为Excel文件
- **权限要求**: 管理员（role=3）
- **限制**: 最多导出10000条记录

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 否 | 操作人ID |
| module | String | 否 | 操作模块 |
| startDate | DateTime | 否 | 开始时间（ISO 8601格式） |
| endDate | DateTime | 否 | 结束时间（ISO 8601格式） |

### 请求示例

```http
GET /api/admin/logs/export?module=用户管理&startDate=2025-10-01T00:00:00&endDate=2025-10-14T23:59:59
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 响应

- **Content-Type**: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- **Content-Disposition**: `attachment;filename=operation_logs_yyyyMMdd_HHmmss.xlsx`

返回Excel文件流（二进制数据）

### Excel文件格式

| 列名 | 说明 |
|------|------|
| 操作时间 | yyyy-MM-dd HH:mm:ss |
| 操作人 | 用户名 |
| 操作内容 | 操作描述 |
| 操作模块 | 所属模块 |
| 操作结果 | 成功/失败 |
| IP地址 | 客户端IP |
| 执行时长(ms) | 执行时长 |
| 错误信息 | 失败时的错误信息 |

### 错误响应

```json
{
  "code": 400,
  "message": "导出数据量过大，请缩小查询范围（最大10000条）",
  "data": null
}
```

---

## 3. 获取操作模块列表

### 基本信息
- **接口路径**: `GET /api/admin/logs/modules`
- **功能描述**: 获取所有已记录的操作模块列表（用于筛选下拉框）
- **权限要求**: 管理员（role=3）

### 请求参数

无

### 请求示例

```http
GET /api/admin/logs/modules
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| code | Integer | 状态码（200成功） |
| message | String | 响应消息 |
| data | Array<String> | 模块列表 |

### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    "用户管理",
    "认证管理",
    "反馈管理",
    "订单管理",
    "商品管理"
  ]
}
```

---

## 📝 操作模块说明

当前系统已记录的操作模块：

| 模块名 | 说明 | 主要操作 |
|--------|------|---------|
| 认证管理 | 用户认证相关 | 用户登录、用户注册 |
| 用户管理 | 用户信息管理 | 创建用户、编辑用户、删除用户、修改角色、修改状态 |
| 反馈管理 | 用户反馈处理 | 回复用户反馈、删除用户反馈 |
| 订单管理 | 订单处理（待实现） | 创建订单、取消订单、确认订单、退款 |
| 商品管理 | 商品维护（待实现） | 创建商品、编辑商品、删除商品、上下架商品 |

---

## 🔐 认证说明

所有接口都需要JWT Token认证：

### 请求头

```http
Authorization: Bearer <JWT_TOKEN>
```

### Token获取

通过用户登录接口获取Token：

```http
POST /api/user/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

响应：

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "admin",
    "role": 3
  }
}
```

---

## ⚠️ 错误码说明

| 错误码 | 说明 | 处理建议 |
|--------|------|---------|
| 200 | 成功 | - |
| 400 | 参数错误 | 检查请求参数 |
| 401 | 未认证 | 检查Token是否有效 |
| 403 | 无权限 | 确认用户是否为管理员 |
| 404 | 资源不存在 | 检查请求路径 |
| 500 | 服务器错误 | 联系技术支持 |

---

## 📊 使用示例

### 前端调用示例（Vue3 + Axios）

```javascript
import request from '@/utils/request'

// 1. 查询操作日志
export function getOperationLogs(params) {
  return request({
    url: '/api/admin/logs/operations',
    method: 'get',
    params
  })
}

// 2. 导出操作日志
export function exportOperationLogs(params) {
  return request({
    url: '/api/admin/logs/export',
    method: 'get',
    params,
    responseType: 'blob' // 重要：接收二进制数据
  })
}

// 3. 获取模块列表
export function getLogModules() {
  return request({
    url: '/api/admin/logs/modules',
    method: 'get'
  })
}

// 使用示例
const loadLogs = async () => {
  try {
    const res = await getOperationLogs({
      page: 1,
      size: 10,
      module: '用户管理',
      startDate: '2025-10-01T00:00:00',
      endDate: '2025-10-14T23:59:59'
    })
    console.log('日志列表:', res.data.list)
  } catch (error) {
    console.error('查询失败:', error)
  }
}

// Excel导出示例
const handleExport = async () => {
  try {
    const res = await exportOperationLogs({
      module: '用户管理'
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
  } catch (error) {
    console.error('导出失败:', error)
  }
}
```

---

## 🔍 Swagger文档

系统已集成Swagger UI，可访问在线API文档：

**访问地址**: http://localhost:8061/swagger-ui.html

---

## 📅 更新日志

### v1.0 (2025-10-14)
- ✅ 初始版本
- ✅ 实现分页查询接口
- ✅ 实现Excel导出接口
- ✅ 实现模块列表接口
- ✅ 支持多条件筛选
- ✅ 支持参数脱敏

---

**文档状态**: ✅ 完成  
**维护人**: 耿康瑞  
**最后更新**: 2025-10-14

