<template>
  <div class="home-page">
    <div class="home-container">
      <!-- 轮播图 -->
      <el-carousel height="400px" class="banner-carousel" :interval="4000">
      <el-carousel-item v-for="banner in banners" :key="banner.id">
        <div class="banner-item" @click="router.push(banner.link_url)">
          <img :src="banner.image_url" alt="轮播图" class="banner-image" />
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 搜索栏 -->
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索商品名称"
        size="large"
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button :icon="Search" @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 分类导航 -->
    <div class="category-section">
      <div
        v-for="category in categories"
        :key="category.category_id"
        class="category-item"
        @click="goToCategory(category.category_id)"
      >
        <el-icon :size="40" class="category-icon">
          <component :is="getIconComponent(category.icon)" />
        </el-icon>
        <div class="category-name">{{ category.category_name }}</div>
      </div>
    </div>

    <!-- 热门推荐 -->
    <div class="product-section">
      <div class="section-header">
        <h2 class="section-title">
          <el-icon><Star /></el-icon>
          热门推荐
        </h2>
        <el-link type="primary" @click="router.push('/products')">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </el-link>
      </div>
      <div class="product-grid">
        <ProductCard
          v-for="product in recommendProducts"
          :key="product.product_id"
          :product="product"
        />
      </div>
    </div>

    <!-- 拼团专区 -->
    <div class="product-section">
      <div class="section-header">
        <h2 class="section-title">
          <el-icon><PriceTag /></el-icon>
          拼团专区
        </h2>
        <el-link type="primary" @click="router.push('/products?has_group=1')">
          更多拼团 <el-icon><ArrowRight /></el-icon>
        </el-link>
      </div>
      <div class="product-grid">
        <ProductCard
          v-for="product in groupBuyProducts"
          :key="product.product_id"
          :product="product"
        />
      </div>
    </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, Star, ArrowRight, PriceTag,
  Apple, Orange, CoffeeCup, Chicken, Food, Goods
} from '@element-plus/icons-vue'
import ProductCard from '@/components/common/ProductCard.vue'
import { mockProducts, mockCategories, mockBanners } from '@/mock/products'

const router = useRouter()

// 数据
const banners = ref(mockBanners)
const categories = ref(mockCategories)
const searchKeyword = ref('')

// 推荐商品（按销量排序，取前8个，只显示上架商品）
const recommendProducts = computed(() => {
  return mockProducts
    .filter(p => p.status === 1)
    .sort((a, b) => (b.sales || 0) - (a.sales || 0))
    .slice(0, 8)
})

// 拼团商品（有拼团价的商品，取前8个）
const groupBuyProducts = computed(() => {
  return mockProducts
    .filter(p => p.status === 1 && p.group_price !== null)
    .slice(0, 8)
})

// 图标映射
const iconMap = {
  'Apple': Apple,
  'Orange': Orange,
  'CoffeeCup': CoffeeCup,
  'Chicken': Chicken,
  'Food': Food,
  'Goods': Goods
}

const getIconComponent = (iconName) => {
  return iconMap[iconName] || Apple
}

// 方法
const goToCategory = (categoryId) => {
  router.push(`/products?category=${categoryId}`)
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push(`/products?keyword=${searchKeyword.value}`)
  }
}
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-top: 64px;
}

.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

/* 轮播图样式 */
.banner-carousel {
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.banner-item {
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 搜索栏样式 */
.search-section {
  margin-bottom: 20px;
  padding: 0 10px;
}

.search-input {
  width: 100%;
}

/* 分类导航样式 */
.category-section {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 30px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 8px;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
}

.category-item:hover {
  background-color: #f0f9ff;
  transform: translateY(-2px);
}

.category-icon {
  color: #409EFF;
  margin-bottom: 8px;
}

.category-name {
  font-size: 14px;
  color: #333;
  text-align: center;
}

/* 商品区域样式 */
.product-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-page {
    padding-top: 56px;
  }
  
  .category-section {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
