# T字开发 - 模块化拆解计划

**创建时间**: 2025-10-21  
**文档版本**: v1.0  
**拆解原则**: 按功能模块（页面+数据）逐一拆解  
**执行方式**: 逐模块开发，每完成一个模块即可验证

---

## 📋 拆解说明

### 拆解原则
- **一个模块 = 一个页面 + 完整的Mock数据**
- **模块独立**: 每个模块可独立开发和测试
- **数据先行**: 先准备Mock数据，再开发页面
- **验收明确**: 每个模块有清晰的验收标准

### 开发流程
```
1. 准备Mock数据（JSON文件）
   ↓
2. 创建页面组件（.vue文件）
   ↓
3. 配置路由（router/index.js）
   ↓
4. 功能测试（页面展示、交互）
   ↓
5. 记录完成（截图、文档）
```

---

## 🎯 模块总览

| 序号 | 模块名称 | 所属端 | 优先级 | 预计时间 | 依赖 |
|------|---------|--------|--------|---------|------|
| M01 | 商品展示模块 | 用户端 | 🔴 高 | 0.5天 | 无 |
| M02 | 商品分类模块 | 用户端 | 🔴 高 | 0.5天 | M01 |
| M03 | 商品详情模块 | 用户端 | 🔴 高 | 0.5天 | M01 |
| M04 | 拼团活动模块 | 用户端 | 🔴 高 | 1天 | M03 |
| M05 | 购物车模块 | 用户端 | 🟡 中 | 0.5天 | M03 |
| M06 | 订单确认模块 | 用户端 | 🔴 高 | 0.5天 | M05 |
| M07 | 订单列表模块 | 用户端 | 🟡 中 | 0.5天 | M06 |
| M08 | 个人中心模块 | 用户端 | 🟡 中 | 0.5天 | UserService API |
| M09 | 团长工作台模块 | 团长端 | 🟡 中 | 0.5天 | 无 |
| M10 | 团员管理模块 | 团长端 | 🟡 中 | 0.5天 | M09 |
| M11 | 订单管理模块 | 团长端 | 🔴 高 | 1天 | M07 |
| M12 | 配送管理模块 | 团长端 | 🟢 低 | 0.5天 | M11 |
| M13 | 佣金查看模块 | 团长端 | 🟡 中 | 0.5天 | M11 |
| M14 | 商品管理模块 | 管理端 | 🔴 高 | 1天 | M01 |
| M15 | 拼团管理模块 | 管理端 | 🟡 中 | 0.5天 | M04 |
| M16 | 团长审核模块 | 管理端 | 🟡 中 | 0.5天 | M09 |
| M17 | 数据统计模块 | 管理端 | 🟢 低 | 0.5天 | 无 |

**总计**: 17个模块，预计9-10天

---

## 📦 模块详细拆解

---

## M01: 商品展示模块（首页）

### 基本信息
- **所属端**: 用户端
- **优先级**: 🔴 高
- **预计时间**: 0.5天
- **依赖**: 无

### 功能描述
展示首页商品推荐、分类导航、搜索入口

### 数据准备

**文件**: `src/mock/products.js`

```javascript
// Mock商品数据（至少准备20个商品）
export const mockProducts = [
  {
    id: 1,
    name: '新鲜红富士苹果',
    categoryId: 1,
    categoryName: '水果',
    price: 12.80,
    groupPrice: 9.90,
    stock: 100,
    sales: 256,
    cover: 'https://via.placeholder.com/300x300/FF6B6B/FFFFFF?text=Apple',
    images: [
      'https://via.placeholder.com/600x600/FF6B6B/FFFFFF?text=Apple1',
      'https://via.placeholder.com/600x600/FF6B6B/FFFFFF?text=Apple2',
      'https://via.placeholder.com/600x600/FF6B6B/FFFFFF?text=Apple3'
    ],
    description: '新鲜红富士苹果，香甜可口，产地直供',
    unit: '斤',
    status: 1, // 1-上架 0-下架
    createTime: '2025-10-15 10:00:00'
  },
  {
    id: 2,
    name: '新鲜香蕉',
    categoryId: 1,
    categoryName: '水果',
    price: 8.50,
    groupPrice: 6.80,
    stock: 150,
    sales: 189,
    cover: 'https://via.placeholder.com/300x300/FFD93D/FFFFFF?text=Banana',
    images: ['https://via.placeholder.com/600x600/FFD93D/FFFFFF?text=Banana1'],
    description: '进口香蕉，软糯香甜',
    unit: '斤',
    status: 1,
    createTime: '2025-10-15 11:00:00'
  },
  {
    id: 3,
    name: '有机西红柿',
    categoryId: 2,
    categoryName: '蔬菜',
    price: 9.80,
    groupPrice: 7.50,
    stock: 80,
    sales: 145,
    cover: 'https://via.placeholder.com/300x300/E74C3C/FFFFFF?text=Tomato',
    images: ['https://via.placeholder.com/600x600/E74C3C/FFFFFF?text=Tomato1'],
    description: '有机种植西红柿，口感沙甜',
    unit: '斤',
    status: 1,
    createTime: '2025-10-15 12:00:00'
  }
  // ... 继续添加至少20个商品数据
]

// 商品分类数据
export const mockCategories = [
  { id: 1, name: '水果', icon: '🍎', sort: 1 },
  { id: 2, name: '蔬菜', icon: '🥬', sort: 2 },
  { id: 3, name: '肉禽蛋', icon: '🥩', sort: 3 },
  { id: 4, name: '粮油调味', icon: '🌾', sort: 4 },
  { id: 5, name: '日用百货', icon: '🧴', sort: 5 },
  { id: 6, name: '零食饮料', icon: '🍪', sort: 6 }
]

// 首页轮播数据
export const mockBanners = [
  {
    id: 1,
    image: 'https://via.placeholder.com/1200x400/4ECDC4/FFFFFF?text=Banner1',
    link: '/products?category=1',
    title: '新鲜水果特惠'
  },
  {
    id: 2,
    image: 'https://via.placeholder.com/1200x400/FF6B6B/FFFFFF?text=Banner2',
    link: '/products?category=2',
    title: '有机蔬菜上新'
  }
]
```

### 页面开发

**文件**: `src/views/user/HomeView.vue`

```vue
<template>
  <div class="home-container">
    <!-- 轮播图 -->
    <el-carousel height="400px" class="banner-carousel">
      <el-carousel-item v-for="banner in banners" :key="banner.id">
        <img :src="banner.image" :alt="banner.title" class="banner-image" />
      </el-carousel-item>
    </el-carousel>

    <!-- 分类导航 -->
    <div class="category-nav">
      <div
        v-for="category in categories"
        :key="category.id"
        class="category-item"
        @click="goToCategory(category.id)"
      >
        <div class="category-icon">{{ category.icon }}</div>
        <div class="category-name">{{ category.name }}</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品"
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <!-- 商品推荐 -->
    <div class="product-section">
      <h2 class="section-title">热门推荐</h2>
      <div class="product-grid">
        <ProductCard
          v-for="product in recommendProducts"
          :key="product.id"
          :product="product"
          @click="goToDetail(product.id)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import ProductCard from '@/components/common/ProductCard.vue'
import { mockProducts, mockCategories, mockBanners } from '@/mock/products'

const router = useRouter()

// 数据
const banners = ref(mockBanners)
const categories = ref(mockCategories)
const searchKeyword = ref('')

// 推荐商品（销量前8）
const recommendProducts = computed(() => {
  return mockProducts
    .sort((a, b) => b.sales - a.sales)
    .slice(0, 8)
})

// 方法
const goToCategory = (categoryId) => {
  router.push(`/products?category=${categoryId}`)
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push(`/products?keyword=${searchKeyword.value}`)
  }
}

const goToDetail = (productId) => {
  router.push(`/products/${productId}`)
}
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.banner-carousel {
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 30px;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.category-nav {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 30px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.category-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.category-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.category-name {
  font-size: 14px;
  color: #606266;
}

.search-bar {
  margin-bottom: 30px;
}

.search-input {
  max-width: 600px;
  margin: 0 auto;
}

.product-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

@media (max-width: 768px) {
  .category-nav {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
```

### 路由配置

**文件**: `src/router/index.js`（添加）

```javascript
{
  path: '/',
  name: 'home',
  component: () => import('@/views/user/HomeView.vue'),
  meta: { title: '首页' }
}
```

### 验收标准

- [ ] 轮播图正常展示和切换
- [ ] 分类导航显示6个分类
- [ ] 点击分类跳转到商品列表（带categoryId参数）
- [ ] 搜索框输入并回车可跳转
- [ ] 显示8个热门商品（按销量排序）
- [ ] 点击商品卡片跳转到详情页
- [ ] 响应式布局适配移动端

---

## M02: 商品分类模块（商品列表）

### 基本信息
- **所属端**: 用户端
- **优先级**: 🔴 高
- **预计时间**: 0.5天
- **依赖**: M01（商品数据）

### 功能描述
按分类展示商品列表，支持筛选、排序、搜索

### 数据准备

**复用**: `src/mock/products.js`（已在M01准备）

**新增筛选逻辑**:

```javascript
// src/utils/productFilter.js
export const filterProducts = (products, filters) => {
  let result = [...products]
  
  // 按分类筛选
  if (filters.categoryId) {
    result = result.filter(p => p.categoryId === filters.categoryId)
  }
  
  // 按关键词搜索
  if (filters.keyword) {
    const keyword = filters.keyword.toLowerCase()
    result = result.filter(p => 
      p.name.toLowerCase().includes(keyword) ||
      p.description.toLowerCase().includes(keyword)
    )
  }
  
  // 按价格区间筛选
  if (filters.priceMin !== undefined) {
    result = result.filter(p => p.groupPrice >= filters.priceMin)
  }
  if (filters.priceMax !== undefined) {
    result = result.filter(p => p.groupPrice <= filters.priceMax)
  }
  
  // 排序
  if (filters.sortBy === 'price-asc') {
    result.sort((a, b) => a.groupPrice - b.groupPrice)
  } else if (filters.sortBy === 'price-desc') {
    result.sort((a, b) => b.groupPrice - a.groupPrice)
  } else if (filters.sortBy === 'sales') {
    result.sort((a, b) => b.sales - a.sales)
  }
  
  return result
}
```

### 页面开发

**文件**: `src/views/user/ProductListView.vue`

```vue
<template>
  <div class="product-list-container">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>{{ currentCategoryName }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="content-wrapper">
      <!-- 侧边栏筛选 -->
      <div class="filter-sidebar">
        <div class="filter-section">
          <h3 class="filter-title">商品分类</h3>
          <el-radio-group v-model="filters.categoryId" @change="handleFilterChange">
            <el-radio :label="null">全部</el-radio>
            <el-radio
              v-for="category in categories"
              :key="category.id"
              :label="category.id"
            >
              {{ category.name }}
            </el-radio>
          </el-radio-group>
        </div>

        <div class="filter-section">
          <h3 class="filter-title">价格区间</h3>
          <el-radio-group v-model="priceRange" @change="handlePriceChange">
            <el-radio label="all">全部</el-radio>
            <el-radio label="0-10">0-10元</el-radio>
            <el-radio label="10-20">10-20元</el-radio>
            <el-radio label="20-50">20-50元</el-radio>
            <el-radio label="50+">50元以上</el-radio>
          </el-radio-group>
        </div>
      </div>

      <!-- 商品列表区域 -->
      <div class="product-area">
        <!-- 工具栏 -->
        <div class="toolbar">
          <div class="result-info">
            找到 <span class="highlight">{{ filteredProducts.length }}</span> 件商品
          </div>
          <div class="sort-options">
            <el-radio-group v-model="filters.sortBy" @change="handleFilterChange">
              <el-radio-button label="default">默认</el-radio-button>
              <el-radio-button label="sales">销量</el-radio-button>
              <el-radio-button label="price-asc">价格升序</el-radio-button>
              <el-radio-button label="price-desc">价格降序</el-radio-button>
            </el-radio-group>
          </div>
        </div>

        <!-- 商品网格 -->
        <div v-if="filteredProducts.length > 0" class="product-grid">
          <ProductCard
            v-for="product in paginatedProducts"
            :key="product.id"
            :product="product"
            @click="goToDetail(product.id)"
          />
        </div>

        <!-- 空状态 -->
        <el-empty v-else description="暂无商品" />

        <!-- 分页 -->
        <el-pagination
          v-if="filteredProducts.length > 0"
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="filteredProducts.length"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          class="pagination"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import ProductCard from '@/components/common/ProductCard.vue'
import { mockProducts, mockCategories } from '@/mock/products'
import { filterProducts } from '@/utils/productFilter'

const router = useRouter()
const route = useRoute()

// 数据
const categories = ref(mockCategories)
const filters = ref({
  categoryId: null,
  keyword: '',
  priceMin: undefined,
  priceMax: undefined,
  sortBy: 'default'
})
const priceRange = ref('all')
const pagination = ref({
  page: 1,
  pageSize: 12
})

// 计算属性
const currentCategoryName = computed(() => {
  if (!filters.value.categoryId) return '全部商品'
  const category = categories.value.find(c => c.id === filters.value.categoryId)
  return category ? category.name : '全部商品'
})

const filteredProducts = computed(() => {
  return filterProducts(mockProducts, filters.value)
})

const paginatedProducts = computed(() => {
  const start = (pagination.value.page - 1) * pagination.value.pageSize
  const end = start + pagination.value.pageSize
  return filteredProducts.value.slice(start, end)
})

// 方法
const handleFilterChange = () => {
  pagination.value.page = 1 // 重置页码
}

const handlePriceChange = (value) => {
  if (value === 'all') {
    filters.value.priceMin = undefined
    filters.value.priceMax = undefined
  } else if (value === '0-10') {
    filters.value.priceMin = 0
    filters.value.priceMax = 10
  } else if (value === '10-20') {
    filters.value.priceMin = 10
    filters.value.priceMax = 20
  } else if (value === '20-50') {
    filters.value.priceMin = 20
    filters.value.priceMax = 50
  } else if (value === '50+') {
    filters.value.priceMin = 50
    filters.value.priceMax = undefined
  }
  handleFilterChange()
}

const goToDetail = (productId) => {
  router.push(`/products/${productId}`)
}

// 监听路由参数
watch(() => route.query, (query) => {
  if (query.category) {
    filters.value.categoryId = parseInt(query.category)
  }
  if (query.keyword) {
    filters.value.keyword = query.keyword
  }
}, { immediate: true })

onMounted(() => {
  // 初始化筛选条件
  if (route.query.category) {
    filters.value.categoryId = parseInt(route.query.category)
  }
  if (route.query.keyword) {
    filters.value.keyword = route.query.keyword
  }
})
</script>

<style scoped>
.product-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.el-breadcrumb {
  margin-bottom: 20px;
}

.content-wrapper {
  display: flex;
  gap: 20px;
}

.filter-sidebar {
  width: 200px;
  flex-shrink: 0;
}

.filter-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.filter-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
}

.el-radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-area {
  flex: 1;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
}

.result-info {
  font-size: 14px;
  color: #606266;
}

.highlight {
  color: #409EFF;
  font-weight: bold;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

@media (max-width: 768px) {
  .content-wrapper {
    flex-direction: column;
  }
  
  .filter-sidebar {
    width: 100%;
  }
  
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
```

### 路由配置

```javascript
{
  path: '/products',
  name: 'products',
  component: () => import('@/views/user/ProductListView.vue'),
  meta: { title: '商品列表' }
}
```

### 验收标准

- [ ] 面包屑导航显示正确
- [ ] 侧边栏分类筛选正常工作
- [ ] 价格区间筛选正常工作
- [ ] 排序功能（默认/销量/价格）正常
- [ ] 显示筛选结果数量
- [ ] 分页功能正常（每页12/24/36/48可选）
- [ ] 从首页带参数跳转能正确筛选
- [ ] 点击商品跳转到详情页
- [ ] 空状态显示正确

---

## M03: 商品详情模块

### 基本信息
- **所属端**: 用户端
- **优先级**: 🔴 高
- **预计时间**: 0.5天
- **依赖**: M01（商品数据）

### 功能描述
展示商品完整信息、图片轮播、拼团/加购物车按钮

### 数据准备

**复用**: `src/mock/products.js`（商品数据）

**新增**: 商品详情辅助数据

```javascript
// src/mock/products.js（追加）

// 商品规格数据（可选，简化可不用）
export const mockProductSpecs = {
  1: { // productId
    specs: [
      { name: '重量', value: '5斤装' },
      { name: '产地', value: '山东烟台' },
      { name: '保质期', value: '常温7天' }
    ]
  }
}

// 商品评价数据（简化Mock）
export const mockProductReviews = {
  1: [
    {
      id: 1,
      userId: 101,
      userName: '张三',
      avatar: 'https://via.placeholder.com/60x60',
      rating: 5,
      content: '很新鲜，很甜，下次还会买！',
      images: [],
      createTime: '2025-10-20 14:30:00'
    },
    {
      id: 2,
      userId: 102,
      userName: '李四',
      avatar: 'https://via.placeholder.com/60x60',
      rating: 4,
      content: '不错，就是价格有点贵',
      images: [],
      createTime: '2025-10-19 10:15:00'
    }
  ]
}

// 获取商品详情（包含完整信息）
export const getProductDetail = (productId) => {
  const product = mockProducts.find(p => p.id === productId)
  if (!product) return null
  
  return {
    ...product,
    specs: mockProductSpecs[productId]?.specs || [],
    reviews: mockProductReviews[productId] || [],
    reviewCount: mockProductReviews[productId]?.length || 0,
    avgRating: 4.8
  }
}
```

### 页面开发

**文件**: `src/views/user/ProductDetailView.vue`

```vue
<template>
  <div class="product-detail-container">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/products' }">商品列表</el-breadcrumb-item>
      <el-breadcrumb-item>{{ product?.name }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-if="product" class="product-main">
      <!-- 左侧：图片展示 -->
      <div class="product-gallery">
        <div class="main-image">
          <img :src="currentImage" alt="商品图片" />
        </div>
        <div class="thumbnail-list">
          <div
            v-for="(img, index) in product.images"
            :key="index"
            class="thumbnail-item"
            :class="{ active: currentImageIndex === index }"
            @click="currentImageIndex = index"
          >
            <img :src="img" :alt="`商品图${index + 1}`" />
          </div>
        </div>
      </div>

      <!-- 右侧：商品信息 -->
      <div class="product-info">
        <h1 class="product-name">{{ product.name }}</h1>
        <div class="product-meta">
          <span class="category">{{ product.categoryName }}</span>
          <span class="sales">已售{{ product.sales }}件</span>
        </div>

        <div class="price-section">
          <div class="group-price-row">
            <span class="label">拼团价</span>
            <span class="price">¥{{ product.groupPrice }}</span>
            <span class="unit">/ {{ product.unit }}</span>
          </div>
          <div class="original-price-row">
            <span class="label">单买价</span>
            <span class="price">¥{{ product.price }}</span>
          </div>
        </div>

        <div class="stock-info">
          <span class="label">库存：</span>
          <span class="value">{{ product.stock }}{{ product.unit }}</span>
        </div>

        <div class="quantity-selector">
          <span class="label">数量：</span>
          <el-input-number
            v-model="quantity"
            :min="1"
            :max="product.stock"
            size="large"
          />
        </div>

        <div class="action-buttons">
          <el-button
            type="danger"
            size="large"
            class="action-btn"
            @click="handleGroupBuy"
          >
            <el-icon><UserFilled /></el-icon>
            发起拼团
          </el-button>
          <el-button
            type="primary"
            size="large"
            class="action-btn"
            @click="handleAddCart"
          >
            <el-icon><ShoppingCart /></el-icon>
            加入购物车
          </el-button>
        </div>
      </div>
    </div>

    <!-- 商品详情Tabs -->
    <div v-if="product" class="product-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="商品详情" name="detail">
          <div class="detail-content">
            <p>{{ product.description }}</p>
            <div v-if="product.specs.length > 0" class="specs-table">
              <h3>商品规格</h3>
              <table>
                <tr v-for="spec in product.specs" :key="spec.name">
                  <td class="spec-name">{{ spec.name }}</td>
                  <td class="spec-value">{{ spec.value }}</td>
                </tr>
              </table>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane :label="`用户评价(${product.reviewCount})`" name="reviews">
          <div class="reviews-section">
            <div v-if="product.reviews.length > 0">
              <div
                v-for="review in product.reviews"
                :key="review.id"
                class="review-item"
              >
                <div class="review-header">
                  <img :src="review.avatar" class="user-avatar" />
                  <div class="user-info">
                    <div class="user-name">{{ review.userName }}</div>
                    <el-rate v-model="review.rating" disabled />
                  </div>
                  <div class="review-time">{{ review.createTime }}</div>
                </div>
                <div class="review-content">{{ review.content }}</div>
              </div>
            </div>
            <el-empty v-else description="暂无评价" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-empty v-else description="商品不存在" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, ShoppingCart } from '@element-plus/icons-vue'
import { getProductDetail } from '@/mock/products'

const router = useRouter()
const route = useRoute()

// 数据
const product = ref(null)
const currentImageIndex = ref(0)
const quantity = ref(1)
const activeTab = ref('detail')

// 计算属性
const currentImage = computed(() => {
  return product.value?.images[currentImageIndex.value] || product.value?.cover
})

// 方法
const handleGroupBuy = () => {
  // TODO: 跳转到拼团页面（M04模块）
  ElMessage.warning('拼团功能将在M04模块实现')
  // router.push(`/group-buy/create?productId=${product.value.id}&quantity=${quantity.value}`)
}

const handleAddCart = () => {
  // 添加到购物车（LocalStorage）
  const cart = JSON.parse(localStorage.getItem('cart') || '[]')
  const existingItem = cart.find(item => item.productId === product.value.id)
  
  if (existingItem) {
    existingItem.quantity += quantity.value
  } else {
    cart.push({
      productId: product.value.id,
      productName: product.value.name,
      price: product.value.groupPrice,
      quantity: quantity.value,
      cover: product.value.cover
    })
  }
  
  localStorage.setItem('cart', JSON.stringify(cart))
  ElMessage.success('已添加到购物车')
}

// 生命周期
onMounted(() => {
  const productId = parseInt(route.params.id)
  product.value = getProductDetail(productId)
  
  if (!product.value) {
    ElMessage.error('商品不存在')
  }
})
</script>

<style scoped>
.product-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.el-breadcrumb {
  margin-bottom: 20px;
}

.product-main {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
  background: #fff;
  padding: 30px;
  border-radius: 8px;
}

.product-gallery {
  width: 500px;
}

.main-image {
  width: 100%;
  height: 500px;
  border: 1px solid #DCDFE6;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-list {
  display: flex;
  gap: 12px;
}

.thumbnail-item {
  width: 80px;
  height: 80px;
  border: 2px solid #DCDFE6;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.thumbnail-item.active {
  border-color: #409EFF;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 12px;
}

.product-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #909399;
}

.category {
  background: #F5F7FA;
  padding: 4px 12px;
  border-radius: 4px;
}

.price-section {
  background: #FFF5F5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.group-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.group-price-row .label {
  font-size: 14px;
  color: #F56C6C;
}

.group-price-row .price {
  font-size: 32px;
  font-weight: bold;
  color: #F56C6C;
}

.group-price-row .unit {
  font-size: 14px;
  color: #909399;
}

.original-price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.original-price-row .label {
  font-size: 14px;
  color: #909399;
}

.original-price-row .price {
  font-size: 16px;
  color: #909399;
  text-decoration: line-through;
}

.stock-info,
.quantity-selector {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.stock-info .label,
.quantity-selector .label {
  font-size: 14px;
  color: #606266;
  margin-right: 12px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 30px;
}

.action-btn {
  flex: 1;
  height: 50px;
  font-size: 16px;
}

.product-tabs {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
}

.detail-content {
  padding: 20px 0;
}

.specs-table {
  margin-top: 30px;
}

.specs-table h3 {
  font-size: 18px;
  margin-bottom: 16px;
}

.specs-table table {
  width: 100%;
  border-collapse: collapse;
}

.specs-table td {
  padding: 12px;
  border: 1px solid #DCDFE6;
}

.spec-name {
  background: #F5F7FA;
  width: 200px;
  font-weight: bold;
}

.reviews-section {
  padding: 20px 0;
}

.review-item {
  padding: 20px;
  border-bottom: 1px solid #DCDFE6;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 14px;
  font-weight: bold;
  margin-bottom: 4px;
}

.review-time {
  font-size: 12px;
  color: #909399;
}

.review-content {
  padding-left: 52px;
  line-height: 1.6;
  color: #606266;
}
</style>
```

### 路由配置

```javascript
{
  path: '/products/:id',
  name: 'product-detail',
  component: () => import('@/views/user/ProductDetailView.vue'),
  meta: { title: '商品详情' }
}
```

### 验收标准

- [ ] 面包屑导航显示正确
- [ ] 商品图片轮播正常
- [ ] 缩略图切换主图正常
- [ ] 价格信息显示正确（拼团价、单买价）
- [ ] 库存信息显示正确
- [ ] 数量选择器正常工作（不超过库存）
- [ ] "加入购物车"功能正常（LocalStorage）
- [ ] "发起拼团"按钮显示（跳转留到M04）
- [ ] 商品详情Tab切换正常
- [ ] 用户评价显示正常（有数据/空状态）
- [ ] 商品不存在时显示空状态

---

## 📝 说明与后续

### 已完成3个模块拆解
1. ✅ **M01: 商品展示模块**（首页）
2. ✅ **M02: 商品分类模块**（商品列表）
3. ✅ **M03: 商品详情模块**

### 剩余14个模块
- M04-M08: 用户端剩余模块（拼团、购物车、订单）
- M09-M13: 团长端5个模块
- M14-M17: 管理端4个模块

### 文档结构
本文档将按照以下结构继续：
```
M01-M08: 用户端模块（8个）
M09-M13: 团长端模块（5个）
M14-M17: 管理端模块（4个）
```

每个模块包含：
- 基本信息
- 功能描述
- 数据准备（Mock数据 + 代码）
- 页面开发（完整Vue组件代码）
- 路由配置
- 验收标准

---

**继续阅读下一个模块...**

（由于文档长度限制，完整的17个模块将分为多个部分）

**下一部分将包含**:
- M04: 拼团活动模块
- M05: 购物车模块
- M06: 订单确认模块
- M07: 订单列表模块
- M08: 个人中心模块

