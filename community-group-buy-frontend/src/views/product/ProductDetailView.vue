<template>
  <MainLayout>
    <div class="product-detail-page">
      <div class="container">
        <el-card v-loading="loading">
          <div v-if="product" class="product-detail">
            <!-- 商品图片和基本信息 -->
            <div class="product-main">
              <!-- 商品图片 -->
              <div class="product-image">
                <img :src="product.coverImg" :alt="product.productName" />
              </div>

              <!-- 商品信息 -->
              <div class="product-info">
                <h1 class="product-title">{{ product.productName }}</h1>

                <!-- 价格 -->
                <div class="price-section">
                  <div v-if="product.groupPrice" class="group-price-box">
                    <div class="price-label">拼团价</div>
                    <div class="price-value">¥{{ product.groupPrice }}</div>
                  </div>
                  <div class="original-price-box">
                    <div class="price-label">{{ product.groupPrice ? '原价' : '单买价' }}</div>
                    <div class="price-value" :class="{ 'has-group': product.groupPrice }">
                      ¥{{ product.price }}
                    </div>
                  </div>
                </div>

                <!-- 库存状态 -->
                <div class="stock-section">
                  <el-tag v-if="product.stock > 0" type="success">有货</el-tag>
                  <el-tag v-else type="danger">已售罄</el-tag>
                  <span class="stock-text">库存：{{ product.stock }} 件</span>
                </div>

                <!-- 商品描述 -->
                <div class="product-desc">
                  <p>{{ product.detail }}</p>
                </div>

                <!-- 数量选择 -->
                <div class="quantity-section">
                  <span class="label">数量：</span>
                  <el-input-number
                    v-model="quantity"
                    :min="1"
                    :max="product.stock"
                    :disabled="product.stock === 0"
                  />
                </div>

                <!-- 操作按钮 -->
                <div class="action-buttons">
                  <el-button
                    type="primary"
                    size="large"
                    :icon="ShoppingCart"
                    @click="handleAddToCart"
                    :disabled="product.stock === 0"
                  >
                    加入购物车
                  </el-button>
                  <el-button
                    type="success"
                    size="large"
                    @click="handleBuyNow"
                    :disabled="product.stock === 0"
                  >
                    立即购买
                  </el-button>
                </div>

                <!-- 商品信息表 -->
                <div class="product-meta">
                  <el-descriptions :column="1" border>
                    <el-descriptions-item label="商品ID">
                      {{ product.productId }}
                    </el-descriptions-item>
                    <el-descriptions-item label="分类">
                      {{ getCategoryName(product.categoryId) }}
                    </el-descriptions-item>
                    <el-descriptions-item label="状态">
                      <el-tag v-if="product.status === 1" type="success">已上架</el-tag>
                      <el-tag v-else type="info">已下架</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="创建时间">
                      {{ formatDate(product.createTime) }}
                    </el-descriptions-item>
                    <el-descriptions-item label="更新时间">
                      {{ formatDate(product.updateTime) }}
                    </el-descriptions-item>
                  </el-descriptions>
                </div>
              </div>
            </div>
          </div>

          <el-empty v-else-if="!loading" description="商品不存在" />
        </el-card>

        <!-- 推荐商品 -->
        <div class="recommend-section">
          <h2 class="section-title">推荐商品</h2>
          <div class="products-grid" v-loading="recommendLoading">
            <ProductCard
              v-for="item in recommendProducts"
              :key="item.productId"
              :product="item"
            />
          </div>
        </div>
      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MainLayout from '@/components/common/MainLayout.vue'
import ProductCard from '@/components/common/ProductCard.vue'
import { ShoppingCart } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getProductDetail, getCategoryList, getRecommendProducts } from '@/api/product'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 商品详情
const product = ref(null)
const loading = ref(false)

// 数量
const quantity = ref(1)

// 分类列表
const categories = ref([])

// 推荐商品
const recommendProducts = ref([])
const recommendLoading = ref(false)

// 获取商品详情
const fetchProductDetail = async () => {
  const productId = route.params.id
  if (!productId) return

  loading.value = true
  try {
    const data = await getProductDetail(productId)
    product.value = data
    // 获取同类推荐
    fetchRecommendProducts(data.categoryId)
  } catch (error) {
    console.error('Failed to fetch product detail:', error)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const data = await getCategoryList()
    categories.value = data
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  }
}

// 获取推荐商品
const fetchRecommendProducts = async (categoryId) => {
  recommendLoading.value = true
  try {
    const data = await getRecommendProducts({ categoryId, limit: 8 })
    // 过滤掉当前商品
    recommendProducts.value = data.filter(
      item => item.productId !== product.value?.productId
    )
  } catch (error) {
    console.error('Failed to fetch recommend products:', error)
  } finally {
    recommendLoading.value = false
  }
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  const category = categories.value.find(cat => cat.categoryId === categoryId)
  return category ? category.categoryName : '未知分类'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 加入购物车
const handleAddToCart = () => {
  if (!product.value) return

  cartStore.addItem(product.value, quantity.value)
  ElMessage.success(`已添加 ${quantity.value} 件商品到购物车`)
}

// 立即购买
const handleBuyNow = () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (!product.value) return

  // 添加到购物车
  cartStore.addItem(product.value, quantity.value)
  // 跳转到购物车
  router.push('/cart')
}

onMounted(() => {
  fetchCategories()
  fetchProductDetail()
})
</script>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  padding: 20px 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 商品主体 */
.product-main {
  display: grid;
  grid-template-columns: 500px 1fr;
  gap: 40px;
  margin-bottom: 40px;
}

/* 商品图片 */
.product-image {
  width: 100%;
  height: 500px;
  background-color: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 商品信息 */
.product-info {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.product-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  line-height: 1.4;
}

/* 价格 */
.price-section {
  display: flex;
  gap: 32px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.price-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 8px;
}

.group-price-box .price-value {
  font-size: 36px;
  font-weight: bold;
  color: #f56c6c;
}

.original-price-box .price-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.original-price-box .price-value.has-group {
  font-size: 20px;
  color: #999;
  text-decoration: line-through;
  font-weight: normal;
}

/* 库存 */
.stock-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stock-text {
  color: #666;
  font-size: 14px;
}

/* 商品描述 */
.product-desc {
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
  color: #666;
  line-height: 1.6;
}

/* 数量选择 */
.quantity-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.label {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 16px;
}

.action-buttons .el-button {
  flex: 1;
}

/* 商品信息表 */
.product-meta {
  margin-top: auto;
}

/* 推荐商品 */
.recommend-section {
  margin-top: 40px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
}

.section-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .product-main {
    grid-template-columns: 1fr;
  }

  .product-image {
    height: 400px;
  }
}

@media (max-width: 768px) {
  .price-section {
    flex-direction: column;
    gap: 16px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  }
}
</style>

