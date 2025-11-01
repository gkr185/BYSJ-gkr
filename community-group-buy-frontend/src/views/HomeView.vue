<template>
  <MainLayout>
    <div class="home-page">
      <!-- Banner轮播图 -->
      <section class="banner-section">
        <el-carousel height="400px" indicator-position="outside">
          <el-carousel-item v-for="item in banners" :key="item.id">
            <div class="banner-item" :style="{ backgroundColor: item.bgColor }">
              <div class="banner-content">
                <h2>{{ item.title }}</h2>
                <p>{{ item.subtitle }}</p>
                <el-button type="primary" size="large" @click="router.push(item.link)">
                  {{ item.buttonText }}
                </el-button>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </section>

      <!-- 商品分类 -->
      <section class="category-section">
        <div class="container">
          <h2 class="section-title">商品分类</h2>
          <div class="category-grid" v-loading="categoriesLoading">
            <div
              v-for="category in categories"
              :key="category.categoryId"
              class="category-card"
              @click="goToCategory(category.categoryId)"
            >
              <div class="category-icon">
                <el-icon :size="32"><Grid /></el-icon>
              </div>
              <div class="category-name">{{ category.categoryName }}</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 热门商品 -->
      <section class="hot-products-section">
        <div class="container">
          <div class="section-header">
            <h2 class="section-title">热门商品</h2>
            <el-button text type="primary" @click="router.push('/products')">
              查看更多 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="products-grid" v-loading="hotProductsLoading">
            <ProductCard
              v-for="product in hotProducts"
              :key="product.productId"
              :product="product"
            />
          </div>
        </div>
      </section>

      <!-- 推荐商品 -->
      <section class="recommend-products-section">
        <div class="container">
          <div class="section-header">
            <h2 class="section-title">精选推荐</h2>
            <el-button text type="primary" @click="router.push('/products')">
              查看更多 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="products-grid" v-loading="recommendProductsLoading">
            <ProductCard
              v-for="product in recommendProducts"
              :key="product.productId"
              :product="product"
            />
          </div>
        </div>
      </section>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MainLayout from '@/components/common/MainLayout.vue'
import ProductCard from '@/components/common/ProductCard.vue'
import { Grid, ArrowRight } from '@element-plus/icons-vue'
import { getCategoryList, getHotProducts, getRecommendProducts } from '@/api/product'
import { ElMessage } from 'element-plus'

const router = useRouter()

// 轮播图数据
const banners = ref([
  {
    id: 1,
    title: '新鲜水果 限时特惠',
    subtitle: '全场8折起，品质保证',
    buttonText: '立即抢购',
    link: '/products?categoryId=1',
    bgColor: '#FFE5B4'
  },
  {
    id: 2,
    title: '社区团购 更实惠',
    subtitle: '邻里一起拼，优惠更多',
    buttonText: '参与拼团',
    link: '/groupbuy',
    bgColor: '#E0F7FA'
  },
  {
    id: 3,
    title: '每日新鲜 当日配送',
    subtitle: '新鲜直达您家门口',
    buttonText: '查看商品',
    link: '/products',
    bgColor: '#F3E5F5'
  }
])

// 分类数据
const categories = ref([])
const categoriesLoading = ref(false)

// 热门商品
const hotProducts = ref([])
const hotProductsLoading = ref(false)

// 推荐商品
const recommendProducts = ref([])
const recommendProductsLoading = ref(false)

// 获取商品分类
const fetchCategories = async () => {
  categoriesLoading.value = true
  try {
    const data = await getCategoryList()
    // 只取一级分类
    categories.value = (data || []).filter(cat => cat.parentId === 0).slice(0, 8)
  } catch (error) {
    console.error('Failed to fetch categories:', error)
    // ElMessage 已在 request.js 中处理，此处无需重复提示
  } finally {
    categoriesLoading.value = false
  }
}

// 获取热门商品
const fetchHotProducts = async () => {
  hotProductsLoading.value = true
  try {
    const data = await getHotProducts(8)
    hotProducts.value = data || []
  } catch (error) {
    console.error('Failed to fetch hot products:', error)
    // ElMessage 已在 request.js 中处理，此处无需重复提示
  } finally {
    hotProductsLoading.value = false
  }
}

// 获取推荐商品
const fetchRecommendProducts = async () => {
  recommendProductsLoading.value = true
  try {
    const data = await getRecommendProducts({ limit: 8 })
    recommendProducts.value = data || []
  } catch (error) {
    console.error('Failed to fetch recommend products:', error)
    // ElMessage 已在 request.js 中处理，此处无需重复提示
  } finally {
    recommendProductsLoading.value = false
  }
}

// 跳转到分类页面
const goToCategory = (categoryId) => {
  router.push({
    path: '/products',
    query: { categoryId }
  })
}

onMounted(() => {
  fetchCategories()
  fetchHotProducts()
  fetchRecommendProducts()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* Banner */
.banner-section {
  margin-bottom: 40px;
}

.banner-item {
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-content {
  text-align: center;
  color: #333;
}

.banner-content h2 {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 16px;
}

.banner-content p {
  font-size: 24px;
  margin-bottom: 32px;
  color: #666;
}

/* 分类区域 */
.category-section {
  background-color: #fff;
  padding: 40px 0;
  margin-bottom: 40px;
}

.section-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 24px;
  color: #333;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 20px;
}

.category-card {
  background-color: #f8f9fa;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.category-card:hover {
  background-color: #409EFF;
  color: #fff;
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.category-icon {
  margin-bottom: 12px;
}

.category-card:hover .category-icon {
  color: #fff;
}

.category-name {
  font-size: 16px;
  font-weight: 500;
}

/* 商品区域 */
.hot-products-section,
.recommend-products-section {
  background-color: #fff;
  padding: 40px 0;
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

/* 响应式 */
@media (max-width: 768px) {
  .banner-content h2 {
    font-size: 32px;
  }

  .banner-content p {
    font-size: 18px;
  }

  .category-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
}
</style>

