# ProductService API文档

**版本**: v1.0.0  
**基础路径**: `http://localhost:8062`  
**通过网关访问**: `http://localhost:9000/product-service`

---

## 📋 接口概览

| 模块 | 接口数 | 路径前缀 |
|------|--------|---------|
| 商品分类（C端） | 4个 | `/api/product/category` |
| 商品分类管理（管理端） | 4个 | `/api/product/admin/category` |
| 商品（C端） | 7个 | `/api/product` |
| 商品管理（管理端） | 8个 | `/api/product/admin` |
| Feign内部接口 | 6个 | `/feign/product` |
| **总计** | **29个** | - |

---

## 1. 商品分类（C端）

### 1.1 获取分类列表
```http
GET /api/product/category/list
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "categoryId": 1,
      "parentId": 0,
      "categoryName": "新鲜水果",
      "sort": 1,
      "status": 1
    },
    {
      "categoryId": 2,
      "parentId": 1,
      "categoryName": "苹果",
      "sort": 1,
      "status": 1
    }
  ]
}
```

---

### 1.2 获取分类树
```http
GET /api/product/category/tree
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "categoryId": 1,
      "parentId": 0,
      "categoryName": "新鲜水果",
      "sort": 1,
      "status": 1,
      "children": [
        {
          "categoryId": 2,
          "parentId": 1,
          "categoryName": "苹果",
          "sort": 1,
          "status": 1,
          "children": []
        },
        {
          "categoryId": 3,
          "parentId": 1,
          "categoryName": "香蕉",
          "sort": 2,
          "status": 1,
          "children": []
        }
      ]
    }
  ]
}
```

---

### 1.3 获取分类详情
```http
GET /api/product/category/{id}
```

**路径参数**:
- `id`: 分类ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "categoryId": 1,
    "parentId": 0,
    "categoryName": "新鲜水果",
    "sort": 1,
    "status": 1
  }
}
```

---

### 1.4 获取分类下的商品
```http
GET /api/product/category/{id}/products?page=0&size=10
```

**路径参数**:
- `id`: 分类ID

**Query参数**:
- `page`: 页码（从0开始，默认0）
- `size`: 每页数量（默认10）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "productId": 1,
        "categoryId": 1,
        "productName": "红富士苹果",
        "coverImg": "http://localhost:8062/uploads/product/20251031120000_123456.jpg",
        "detail": "新鲜红富士苹果，甜脆可口",
        "price": 12.99,
        "groupPrice": 9.99,
        "stock": 500,
        "status": 1,
        "createTime": "2025-10-31T12:00:00",
        "updateTime": "2025-10-31T12:00:00"
      }
    ],
    "totalElements": 15,
    "totalPages": 2,
    "number": 0,
    "size": 10
  }
}
```

---

## 2. 商品分类管理（管理端）

### 2.1 创建分类
```http
POST /api/product/admin/category
Content-Type: application/json
```

**请求体**:
```json
{
  "parentId": 0,
  "categoryName": "新鲜水果",
  "sort": 1,
  "status": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "categoryId": 1,
    "parentId": 0,
    "categoryName": "新鲜水果",
    "sort": 1,
    "status": 1
  }
}
```

---

### 2.2 更新分类
```http
PUT /api/product/admin/category/{id}
Content-Type: application/json
```

**路径参数**:
- `id`: 分类ID

**请求体**:
```json
{
  "categoryName": "新鲜水果（更新）",
  "sort": 2,
  "status": 1
}
```

**响应示例**: 同创建分类

---

### 2.3 删除分类
```http
DELETE /api/product/admin/category/{id}
```

**路径参数**:
- `id`: 分类ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**业务规则**:
- 如果分类下有子分类，不允许删除
- 如果分类下有商品，不允许删除

---

### 2.4 调整分类排序
```http
PUT /api/product/admin/category/{id}/sort?sort=5
```

**路径参数**:
- `id`: 分类ID

**Query参数**:
- `sort`: 新的排序值

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "categoryId": 1,
    "parentId": 0,
    "categoryName": "新鲜水果",
    "sort": 5,
    "status": 1
  }
}
```

---

## 3. 商品（C端）

### 3.1 获取商品列表
```http
GET /api/product/list?page=0&size=10&sort=create_time&keyword=&categoryId=
```

**Query参数**:
- `page`: 页码（从0开始，默认0）
- `size`: 每页数量（默认10）
- `sort`: 排序方式（`create_time`、`price_asc`、`price_desc`，默认`create_time`）
- `keyword`: 搜索关键词（可选）
- `categoryId`: 分类ID（可选）

**响应示例**: 同"获取分类下的商品"

---

### 3.2 获取商品详情
```http
GET /api/product/{id}
```

**路径参数**:
- `id`: 商品ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "productId": 1,
    "categoryId": 1,
    "productName": "红富士苹果",
    "coverImg": "http://localhost:8062/uploads/product/20251031120000_123456.jpg",
    "detail": "新鲜红富士苹果，甜脆可口",
    "price": 12.99,
    "groupPrice": 9.99,
    "stock": 500,
    "status": 1,
    "createTime": "2025-10-31T12:00:00",
    "updateTime": "2025-10-31T12:00:00"
  }
}
```

---

### 3.3 商品搜索
```http
GET /api/product/search?keyword=苹果&categoryId=1&page=0&size=10
```

**Query参数**:
- `keyword`: 搜索关键词（可选）
- `categoryId`: 分类ID（可选）
- `page`: 页码（默认0）
- `size`: 每页数量（默认10）

**响应示例**: 分页结果同"获取商品列表"

---

### 3.4 热门商品推荐
```http
GET /api/product/hot?limit=10
```

**Query参数**:
- `limit`: 返回数量（默认10）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "productId": 1,
      "categoryId": 1,
      "productName": "红富士苹果",
      "coverImg": "http://localhost:8062/uploads/product/20251031120000_123456.jpg",
      "price": 12.99,
      "groupPrice": 9.99,
      "stock": 500,
      "status": 1
    }
  ]
}
```

---

### 3.5 推荐商品
```http
GET /api/product/recommend?categoryId=1&limit=10
```

**Query参数**:
- `categoryId`: 分类ID（可选，不传则返回热门商品）
- `limit`: 返回数量（默认10）

**响应示例**: 同"热门商品推荐"

---

### 3.6 查询商品库存
```http
GET /api/product/{id}/stock
```

**路径参数**:
- `id`: 商品ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 500
}
```

---

### 3.7 按分类查询商品
```http
GET /api/product/category/{categoryId}/list?page=0&size=10
```

**路径参数**:
- `categoryId`: 分类ID

**Query参数**:
- `page`: 页码（默认0）
- `size`: 每页数量（默认10）

**响应示例**: 分页结果同"获取商品列表"

---

## 4. 商品管理（管理端）

### 4.1 创建商品
```http
POST /api/product/admin/product
Content-Type: application/json
```

**请求体**:
```json
{
  "categoryId": 1,
  "productName": "红富士苹果",
  "coverImg": "http://localhost:8062/uploads/product/20251031120000_123456.jpg",
  "detail": "新鲜红富士苹果，甜脆可口",
  "price": 12.99,
  "groupPrice": 9.99,
  "stock": 500
}
```

**响应示例**: 同"获取商品详情"

---

### 4.2 更新商品
```http
PUT /api/product/admin/product/{id}
Content-Type: application/json
```

**路径参数**:
- `id`: 商品ID

**请求体**: 同"创建商品"

**响应示例**: 同"获取商品详情"

---

### 4.3 删除商品
```http
DELETE /api/product/admin/product/{id}
```

**路径参数**:
- `id`: 商品ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 4.4 更新商品状态（上下架）
```http
PUT /api/product/admin/product/{id}/status?status=1
```

**路径参数**:
- `id`: 商品ID

**Query参数**:
- `status`: 状态（0-下架；1-上架）

**响应示例**: 同"获取商品详情"

---

### 4.5 调整商品库存
```http
PUT /api/product/admin/product/{id}/stock?quantity=100
```

**路径参数**:
- `id`: 商品ID

**Query参数**:
- `quantity`: 调整数量（正数增加，负数减少）

**响应示例**: 同"获取商品详情"

**示例**:
- 增加100件: `?quantity=100`
- 减少50件: `?quantity=-50`

---

### 4.6 上传商品图片
```http
POST /api/product/admin/upload
Content-Type: multipart/form-data
```

**表单参数**:
- `file`: 图片文件（jpg、jpeg、png、gif，最大5MB）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "http://localhost:8062/uploads/product/20251031120000_123456.jpg"
}
```

**文件命名规则**: `yyyyMMddHHmmss_随机6位数字.扩展名`

---

### 4.7 获取商品列表（管理端）
```http
GET /api/product/admin/product/list?page=0&size=10
```

**Query参数**:
- `page`: 页码（默认0）
- `size`: 每页数量（默认10）

**响应示例**: 分页结果同"获取商品列表"

**说明**: 管理端可查看所有商品，包括下架商品

---

### 4.8 获取商品统计数据
```http
GET /api/product/admin/statistics
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalProducts": 120,
    "onSaleProducts": 100,
    "offSaleProducts": 20,
    "lowStockProducts": 8,
    "todayNewProducts": 3
  }
}
```

---

## 5. Feign内部接口

### 5.1 扣减库存 ⭐
```http
POST /feign/product/{productId}/stock/deduct
Content-Type: application/json
```

**路径参数**:
- `productId`: 商品ID

**请求体**:
```json
{
  "quantity": 2
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**核心逻辑**: 数据库乐观锁
```sql
UPDATE product SET stock = stock - :quantity 
WHERE product_id = :productId 
  AND stock >= :quantity 
  AND status = 1
```

**异常情况**:
- 库存不足: `{"code": 400, "message": "库存不足或商品已下架"}`
- 商品不存在: `{"code": 404, "message": "商品不存在"}`

---

### 5.2 恢复库存
```http
POST /feign/product/{productId}/stock/restore
Content-Type: application/json
```

**路径参数**:
- `productId`: 商品ID

**请求体**:
```json
{
  "quantity": 2
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**使用场景**: 订单取消、退款

---

### 5.3 检查商品是否可售
```http
GET /feign/product/{productId}/check
```

**路径参数**:
- `productId`: 商品ID

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

**检查规则**:
- `status = 1`（已上架）
- `stock > 0`（有库存）

---

### 5.4 批量检查商品状态
```http
POST /feign/product/batch-check
Content-Type: application/json
```

**请求体**:
```json
[1, 2, 3, 4, 5]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "1": true,
    "2": true,
    "3": false,
    "4": true,
    "5": false
  }
}
```

---

### 5.5 获取商品信息（快照用）
```http
GET /feign/product/{productId}/info
```

**路径参数**:
- `productId`: 商品ID

**响应示例**: 同"获取商品详情"

**使用场景**: OrderService创建订单时保存商品快照

---

### 5.6 批量获取商品信息
```http
POST /feign/product/batch-info
Content-Type: application/json
```

**请求体**:
```json
[1, 2, 3, 4, 5]
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "productId": 1,
      "categoryId": 1,
      "productName": "红富士苹果",
      "coverImg": "http://localhost:8062/uploads/product/20251031120000_123456.jpg",
      "price": 12.99,
      "groupPrice": 9.99,
      "stock": 500,
      "status": 1
    }
  ]
}
```

---

## 📊 接口统计

### 按模块统计
| 模块 | 接口数 | 说明 |
|------|--------|------|
| 分类管理（C端） | 4个 | 查询类接口 |
| 分类管理（管理端） | 4个 | 增删改查 |
| 商品管理（C端） | 7个 | 查询、搜索、推荐 |
| 商品管理（管理端） | 8个 | 增删改查、上传、统计 |
| Feign内部接口 | 6个 | 库存扣减、商品信息 |
| **总计** | **29个** | - |

### 按HTTP方法统计
| 方法 | 接口数 |
|------|--------|
| GET | 15个 |
| POST | 9个 |
| PUT | 4个 |
| DELETE | 1个 |
| **总计** | **29个** |

---

## 🔐 权限说明

### 需要管理员权限的接口
以下接口需要通过Gateway的JWT验证（`role = ADMIN`）：
- 所有 `/api/product/admin/**` 接口（12个）

### 需要登录的接口
以下接口需要JWT Token（`role = USER` 或 `ADMIN`）：
- 目前C端接口暂不要求登录（未来可根据需求调整）

### 内部接口
以下接口仅供内部微服务调用：
- 所有 `/feign/product/**` 接口（6个）

---

## 🚀 测试指南

### Swagger测试
访问: http://localhost:8062/swagger-ui.html

### Postman测试
1. 导入Collection（见附件）
2. 设置环境变量:
   - `base_url`: `http://localhost:8062`
   - `gateway_url`: `http://localhost:9000/product-service`

### 库存并发测试
使用JMeter测试脚本（见附件）：
- 模拟100个用户同时扣减库存
- 验证无超卖现象

---

## 📞 技术支持

**开发者**: 耿康瑞  
**项目**: 社区团购系统毕业设计  
**文档版本**: v1.0  
**更新日期**: 2025-10-31

