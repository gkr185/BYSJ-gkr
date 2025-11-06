# 商品详情页开发文档

## 功能概述

商品详情页是用户了解商品信息和进行购买决策的核心页面，支持**直接购买**和**拼团购买**两种模式。

## 页面结构

### 1. 商品主要信息区
- **商品图片展示**
  - 大图预览（支持点击放大）
  - 标签显示（有货/缺货、拼团中）
  - 图片支持懒加载

- **商品信息**
  - 商品名称
  - 拼团价格（如有）
  - 单买价格
  - 库存数量
  - 商品分类

- **购买操作**
  - 数量选择器
  - 加入购物车按钮
  - 立即购买按钮（黄色）
  - 拼团购买按钮（红色，仅拼团商品显示）

### 2. 拼团专区
显示该商品的所有进行中拼团活动，每个活动包含：

- **活动信息**
  - 拼团价
  - 成团人数
  - 活动时间范围

- **团列表**（默认显示3个）
  - 团号
  - 社区标签（本社区优先显示）
  - 团长信息
  - 拼团进度（进度条 + 百分比）
  - 参与拼团按钮

- **查看全部**按钮（超过3个团时显示）

### 3. 商品详情区
- 富文本内容展示
- 支持HTML格式

### 4. 推荐商品区
- 同分类商品推荐
- 商品卡片展示
- 响应式网格布局

## API集成

### 1. 商品详情接口
```javascript
GET /api/product/{id}

// 响应数据
{
  productId: 1,
  categoryId: 1,
  productName: "红富士苹果",
  coverImg: "/uploads/product/apple.jpg",
  detail: "商品详情HTML",
  price: 12.99,
  groupPrice: 9.99,  // 有此字段表示支持拼团
  stock: 500,
  status: 1
}
```

### 2. 拼团活动接口
```javascript
GET /api/groupbuy/product/{productId}/activities?communityId=xxx

// 响应数据
[
  {
    activityId: 1,
    productId: 101,
    groupPrice: 29.9,
    requiredNum: 3,
    startTime: "2025-11-01T00:00:00",
    endTime: "2025-11-10T23:59:59",
    teams: [
      {
        teamId: 5001,
        teamNo: "T20251101001",
        leaderId: 2001,
        leaderName: "张团长",
        communityId: 3001,
        communityName: "阳光小区",
        currentNum: 2,
        requiredNum: 3,
        teamStatus: 0,
        expireTime: "2025-11-02T12:00:00"
      }
    ]
  }
]
```

### 3. 推荐商品接口
```javascript
GET /api/product/recommend?categoryId=1&limit=8
```

## 核心功能

### 1. 加入购物车
- 将商品添加到购物车（使用Pinia Store）
- 提示成功消息
- 支持数量选择

### 2. 立即购买
- 需要登录验证
- 跳转到订单确认页面
- 传递商品信息和数量

### 3. 拼团购买
- 点击"拼团购买"按钮滚动到拼团区域
- 选择要参与的团
- 确认参团（显示拼团价）
- 需要登录验证
- 创建拼团订单

### 4. 社区优先显示
- 本社区的团排在前面
- 显示"本社区"绿色标签
- 其他社区显示社区名称灰色标签

### 5. 拼团进度可视化
- 进度条显示当前人数/需要人数
- 百分比显示
- 进度条颜色：
  - 80%以上：红色 (#f56c6c)
  - 50-80%：橙色 (#e6a23c)
  - 50%以下：灰色 (#909399)

## 设计特色

### 1. 视觉层次分明
- 拼团价使用红色高亮（¥29.90）
- 单买价使用普通灰色
- 节省金额使用红色标签显示

### 2. 卡片式布局
- 所有模块使用白色卡片
- 圆角12px
- 轻微阴影效果
- 模块间距24px

### 3. 响应式设计
- 桌面端：左右双栏布局
- 平板端：上下布局
- 移动端：单栏布局，按钮全宽

### 4. 交互反馈
- 按钮悬停效果
- 团项悬停变色（边框变红）
- 加载骨架屏
- 操作成功提示

### 5. 颜色系统
- 主色：#409EFF（蓝色）- 加入购物车
- 警告色：#E6A23C（橙色）- 立即购买
- 危险色：#F56C6C（红色）- 拼团相关
- 成功色：#67C23A（绿色）- 库存标签

## 状态管理

### 使用的Store
1. **useCartStore** - 购物车管理
   - `addItem(product, quantity)` - 添加商品到购物车

2. **useUserStore** - 用户信息
   - `isLoggedIn` - 登录状态
   - `userInfo.communityId` - 用户社区ID
   - `userInfo.role` - 用户角色（2=团长）

## 路由配置

```javascript
{
  path: '/products/:id',
  name: 'productDetail',
  component: () => import('../views/ProductDetailView.vue'),
  meta: { title: '商品详情' }
}
```

## 使用示例

### 从商品列表跳转
```javascript
router.push(`/products/${productId}`)
```

### 从首页推荐跳转
```javascript
<ProductCard :product="product" />
// ProductCard组件内部会跳转到详情页
```

## 待实现功能（TODO）

1. **立即购买逻辑**
   - 创建普通订单
   - 跳转到订单确认页

2. **参团逻辑完善**
   - 显示选择地址对话框
   - 调用参团API
   - 创建拼团订单
   - 跳转到支付页面

3. **发起新团**
   - 团长权限验证
   - 跳转到发起拼团页面

4. **图片轮播**
   - 支持多图展示
   - 缩略图切换

5. **收藏功能**
   - 添加收藏按钮
   - 收藏状态同步

6. **商品评价**
   - 评价列表展示
   - 评分统计

## 注意事项

### 1. 图片URL处理
使用 `getProductImageUrl` 工具函数处理图片路径：
```javascript
import { getProductImageUrl } from '@/utils/image'

const productImageUrl = computed(() => {
  return getProductImageUrl(product.value)
})
```

### 2. 权限判断
```javascript
// 检查是否登录
if (!userStore.isLoggedIn) {
  router.push('/login')
  return
}

// 检查是否团长
if (userStore.userInfo?.role !== 2) {
  ElMessage.warning('只有团长才能发起拼团')
  return
}
```

### 3. 社区匹配
```javascript
const isUserCommunity = (communityId) => {
  return userStore.userInfo?.communityId === communityId
}
```

### 4. 日期格式化
```javascript
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}
```

## 性能优化

1. **图片懒加载**
   - 使用Element Plus的`el-image`组件
   - 自动支持懒加载

2. **骨架屏**
   - 数据加载时显示骨架屏
   - 提升用户体验

3. **按需加载**
   - 推荐商品异步加载
   - 拼团数据独立加载

4. **平滑滚动**
   - 点击"拼团购买"按钮平滑滚动到拼团区
   - `scrollIntoView({ behavior: 'smooth' })`

## 测试要点

### 1. 功能测试
- [ ] 商品详情正确显示
- [ ] 加入购物车成功
- [ ] 拼团区域数据加载
- [ ] 社区优先排序正确
- [ ] 进度条显示准确

### 2. 边界测试
- [ ] 商品不存在时显示空状态
- [ ] 无拼团活动时不显示拼团区
- [ ] 库存为0时按钮禁用
- [ ] 未登录时提示登录

### 3. 响应式测试
- [ ] 桌面端布局正常
- [ ] 平板端布局正常
- [ ] 移动端布局正常
- [ ] 图片自适应

---

**创建日期**: 2025-11-06  
**版本**: v1.0.0  
**作者**: AI助手  
**相关文件**:
- `src/views/ProductDetailView.vue` - 商品详情页主组件
- `src/api/product.js` - 商品API
- `src/api/groupbuy.js` - 拼团API
- `src/utils/image.js` - 图片工具函数

