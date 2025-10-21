<template>
  <div class="product-detail-page">
    <div class="product-detail-container">
    <div v-if="product" class="detail-content">
      <!-- 面包屑导航 -->
      <el-breadcrumb separator="/" class="breadcrumb">
        <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: '/products' }">商品列表</el-breadcrumb-item>
        <el-breadcrumb-item>{{ product.product_name }}</el-breadcrumb-item>
      </el-breadcrumb>

      <div class="product-main">
        <!-- 左侧：商品图片 -->
        <div class="product-gallery">
          <div class="main-image">
            <img :src="currentImage" :alt="product.product_name" />
          </div>
          <div class="thumbnail-list">
            <div
              v-for="(image, index) in product.images"
              :key="index"
              :class="['thumbnail-item', { active: currentImageIndex === index }]"
              @click="currentImageIndex = index"
            >
              <img :src="image" :alt="`图片${index + 1}`" />
            </div>
          </div>
        </div>

        <!-- 右侧：商品信息 -->
        <div class="product-info">
          <h1 class="product-title">{{ product.product_name }}</h1>
          
          <div class="product-meta">
            <el-tag size="small" type="info">分类: {{ getCategoryName(product.category_id) }}</el-tag>
            <span class="sales-info">已售 {{ product.sales || 0 }} 件</span>
          </div>

          <div class="price-section">
            <div v-if="product.group_price" class="price-row">
              <span class="price-label">拼团价:</span>
              <span class="group-price">¥{{ product.group_price }}</span>
              <el-tag type="danger" size="small">立省¥{{ (product.price - product.group_price).toFixed(2) }}</el-tag>
            </div>
            <div class="price-row">
              <span class="price-label">{{ product.group_price ? '单买价:' : '价格:' }}</span>
              <span :class="['original-price', { 'has-group': product.group_price }]">
                ¥{{ product.price }}
              </span>
            </div>
          </div>

          <div class="stock-info">
            <span>库存: </span>
            <span :class="['stock-number', { 'low-stock': product.stock < 10 }]">
              {{ product.stock }} 件
            </span>
          </div>

          <div class="quantity-section">
            <span class="section-label">数量:</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="product.stock"
              :disabled="product.stock === 0"
            />
          </div>

          <div class="action-buttons">
            <el-button
              v-if="product.group_price"
              type="danger"
              size="large"
              :icon="UserFilled"
              :disabled="product.stock === 0"
              @click="handleGroupBuy"
            >
              发起拼团
            </el-button>
            <el-button
              type="primary"
              size="large"
              :icon="ShoppingCart"
              :disabled="product.stock === 0"
              @click="handleAddToCart"
            >
              {{ product.stock === 0 ? '已售罄' : '加入购物车' }}
            </el-button>
          </div>
        </div>
      </div>

      <!-- 商品详情和评价 -->
      <el-card class="detail-tabs-card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div class="detail-content-text">
              <pre>{{ product.description || product.detail }}</pre>
            </div>
          </el-tab-pane>
          <el-tab-pane label="用户评价" name="reviews">
            <div v-if="product.reviews && product.reviews.length > 0" class="reviews-list">
              <div v-for="(review, index) in product.reviews" :key="index" class="review-item">
                <div class="review-header">
                  <span class="review-user">{{ review.user_name }}</span>
                  <el-rate v-model="review.rating" disabled />
                  <span class="review-time">{{ review.create_time }}</span>
                </div>
                <div class="review-comment">{{ review.comment }}</div>
              </div>
            </div>
            <el-empty v-else description="暂无评价" :image-size="150" />
          </el-tab-pane>
        </el-tabs>
      </el-card>
    </div>

    <!-- 加载状态 -->
    <div v-else-if="loading" class="loading-wrapper">
      <el-skeleton :rows="10" animated />
    </div>

    <!-- 未找到商品 -->
    <el-empty v-else description="商品不存在" :image-size="200">
      <el-button type="primary" @click="router.push('/products')">返回商品列表</el-button>
    </el-empty>
  </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { UserFilled, ShoppingCart } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getProductDetail } from '@/mock/products'
import { mockCategories } from '@/mock/products'
import { addToCart } from '@/utils/cart'

const router = useRouter()
const route = useRoute()

// 数据
const product = ref(null)
const loading = ref(true)
const currentImageIndex = ref(0)
const quantity = ref(1)
const activeTab = ref('detail')

// 当前显示的图片
const currentImage = computed(() => {
  if (!product.value || !product.value.images) return ''
  return product.value.images[currentImageIndex.value]
})

// 获取分类名称
const getCategoryName = (categoryId) => {
  const category = mockCategories.find(c => c.category_id === categoryId)
  return category ? category.category_name : '未知分类'
}

// 加载商品详情
const loadProductDetail = () => {
  loading.value = true
  const productId = route.params.id
  
  // 模拟异步加载
  setTimeout(() => {
    const productData = getProductDetail(productId)
    product.value = productData
    loading.value = false
    
    if (!productData) {
      ElMessage.error('商品不存在')
    }
  }, 300)
}

onMounted(() => {
  loadProductDetail()
})

// 方法
const handleAddToCart = () => {
  if (product.value.stock === 0) {
    ElMessage.warning('商品已售罄')
    return
  }
  
  if (quantity.value > product.value.stock) {
    ElMessage.warning('库存不足')
    return
  }
  
  addToCart(product.value, quantity.value)
  ElMessage.success(`已添加 ${quantity.value} 件到购物车`)
}

const handleGroupBuy = () => {
  if (product.value.stock === 0) {
    ElMessage.warning('商品已售罄')
    return
  }
  
  // 跳转到拼团页面
  router.push(`/group-buy?product_id=${product.value.product_id}`)
}
</script>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  padding-top: 84px; /* 64px导航栏 + 20px间隔 */
  background-color: #f5f5f5;
}

.product-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 20px 20px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.product-main {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
}

/* 商品图片样式 */
.product-gallery {
  flex: 1;
  max-width: 500px;
}

.main-image {
  width: 100%;
  height: 500px;
  background-color: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 12px;
}

.main-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.thumbnail-item {
  width: 80px;
  height: 80px;
  border: 2px solid transparent;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.thumbnail-item:hover {
  border-color: #409EFF;
}

.thumbnail-item.active {
  border-color: #409EFF;
}

.thumbnail-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 商品信息样式 */
.product-info {
  flex: 1;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.product-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 16px 0;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e5e5e5;
}

.sales-info {
  font-size: 14px;
  color: #999;
}

.price-section {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.price-row:last-child {
  margin-bottom: 0;
}

.price-label {
  font-size: 14px;
  color: #666;
  min-width: 70px;
}

.group-price {
  font-size: 32px;
  color: #F56C6C;
  font-weight: bold;
}

.original-price {
  font-size: 24px;
  color: #333;
  font-weight: bold;
}

.original-price.has-group {
  font-size: 18px;
  color: #999;
  text-decoration: line-through;
  font-weight: normal;
}

.stock-info {
  margin-bottom: 20px;
  font-size: 14px;
  color: #666;
}

.stock-number {
  font-weight: bold;
  color: #67C23A;
}

.stock-number.low-stock {
  color: #F56C6C;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.section-label {
  font-size: 14px;
  color: #666;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-buttons .el-button {
  flex: 1;
}

/* 详情标签页样式 */
.detail-tabs-card {
  margin-top: 30px;
}

.detail-content-text {
  padding: 20px;
  line-height: 1.8;
  color: #666;
}

.detail-content-text pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: inherit;
  margin: 0;
}

.reviews-list {
  padding: 20px;
}

.review-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.review-user {
  font-weight: bold;
  color: #333;
}

.review-time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.review-comment {
  color: #666;
  line-height: 1.6;
}

.loading-wrapper {
  padding: 40px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-detail-page {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }
  
  .product-main {
    flex-direction: column;
  }
  
  .product-gallery {
    max-width: 100%;
  }
  
  .action-buttons {
    flex-direction: column;
  }
}
</style>

