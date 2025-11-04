<template>
  <MainLayout>
    <div class="home-container">
      <!-- 轮播图 -->
      <div class="banner-section">
        <el-carousel height="400px" :interval="4000" indicator-position="inside">
          <el-carousel-item v-for="(banner, index) in banners" :key="index">
            <div class="banner-item" :style="{ backgroundImage: `url(${banner.image})` }">
              <div class="banner-content">
                <h1>{{ banner.title }}</h1>
                <p>{{ banner.subtitle }}</p>
                <el-button type="primary" size="large" @click="$router.push(banner.link)">
                  {{ banner.buttonText }}
                </el-button>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <div class="main-content">
        <!-- 分类导航 -->
        <div class="category-section">
          <h2 class="section-title">
            <el-icon><Grid /></el-icon>
            商品分类
          </h2>
          
          <div v-loading="categoriesLoading" class="category-grid">
            <div
              v-for="category in categories"
              :key="category.categoryId"
              class="category-item"
              @click="goToCategory(category.categoryId)"
            >
              <el-icon :size="40">
                <Goods />
              </el-icon>
              <span>{{ category.categoryName }}</span>
            </div>
          </div>
        </div>

        <!-- 热门商品 -->
        <div class="products-section">
          <div class="section-header">
            <h2 class="section-title">
              <el-icon><Star /></el-icon>
              热门商品
            </h2>
            <el-link type="primary" @click="$router.push('/products')">
              查看更多
              <el-icon><ArrowRight /></el-icon>
            </el-link>
          </div>
          
          <div v-loading="hotProductsLoading" class="products-grid">
            <ProductCard
              v-for="product in hotProducts"
              :key="product.productId"
              :product="product"
            />
          </div>
          
          <el-empty v-if="!hotProductsLoading && hotProducts.length === 0" description="暂无商品" />
        </div>

        <!-- 推荐商品 -->
        <div class="products-section">
          <div class="section-header">
            <h2 class="section-title">
              <el-icon><Present /></el-icon>
              推荐商品
            </h2>
            <el-link type="primary" @click="$router.push('/products')">
              查看更多
              <el-icon><ArrowRight /></el-icon>
            </el-link>
          </div>
          
          <div v-loading="recommendProductsLoading" class="products-grid">
            <ProductCard
              v-for="product in recommendProducts"
              :key="product.productId"
              :product="product"
            />
          </div>
          
          <el-empty v-if="!recommendProductsLoading && recommendProducts.length === 0" description="暂无商品" />
        </div>
      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MainLayout from '@/components/common/MainLayout.vue'
import ProductCard from '@/components/common/ProductCard.vue'
import { Grid, ArrowRight, Star, Present, Goods } from '@element-plus/icons-vue'
import { getCategoryList, getHotProducts, getRecommendProducts } from '@/api/product'

const router = useRouter()

// 轮播图数据
const banners = ref([
  {
    title: '社区团购 邻里互助',
    subtitle: '新鲜优质商品，团购更优惠',
    buttonText: '立即参团',
    link: '/groupbuy',
    image: 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=1200&h=400&fit=crop'
  },
  {
    title: '优质商品 源头直采',
    subtitle: '品质保证，价格实惠',
    buttonText: '浏览商品',
    link: '/products',
    image: 'https://images.unsplash.com/photo-1534723452862-4c874018d66d?w=1200&h=400&fit=crop'
  },
  {
    title: '便捷配送 快速到家',
    subtitle: '社区自提，方便快捷',
    buttonText: '了解更多',
    link: '/products',
    image: 'https://images.unsplash.com/photo-1578916171728-46686eac8d58?w=1200&h=400&fit=crop'
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
    const res = await getCategoryList()
    console.log('📦 获取分类响应:', res)
    if (res.code === 200) {
      // 只取一级分类
      categories.value = (res.data || []).filter(cat => cat.parentId === 0).slice(0, 8)
      console.log('✅ 分类加载成功:', categories.value.length, '个')
    } else {
      console.warn('⚠️ 分类加载失败:', res.message)
    }
  } catch (error) {
    console.error('❌ Failed to fetch categories:', error)
    categories.value = []
  } finally {
    categoriesLoading.value = false
  }
}

// 获取热门商品
const fetchHotProducts = async () => {
  hotProductsLoading.value = true
  try {
    const res = await getHotProducts(8)
    console.log('🔥 获取热门商品响应:', res)
    if (res.code === 200) {
      hotProducts.value = res.data || []
      console.log('✅ 热门商品加载成功:', hotProducts.value.length, '个')
    } else {
      console.warn('⚠️ 热门商品加载失败:', res.message)
    }
  } catch (error) {
    console.error('❌ Failed to fetch hot products:', error)
    hotProducts.value = []
  } finally {
    hotProductsLoading.value = false
  }
}

// 获取推荐商品
const fetchRecommendProducts = async () => {
  recommendProductsLoading.value = true
  try {
    const res = await getRecommendProducts({ limit: 8 })
    console.log('⭐ 获取推荐商品响应:', res)
    if (res.code === 200) {
      recommendProducts.value = res.data || []
      console.log('✅ 推荐商品加载成功:', recommendProducts.value.length, '个')
    } else {
      console.warn('⚠️ 推荐商品加载失败:', res.message)
    }
  } catch (error) {
    console.error('❌ Failed to fetch recommend products:', error)
    recommendProducts.value = []
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
.home-container {
  min-height: calc(100vh - 120px);
}

/* 轮播图 */
.banner-section {
  margin-bottom: 32px;
}

.banner-item {
  width: 100%;
  height: 400px;
  background-size: cover;
  background-position: center;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
}

.banner-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: #fff;
}

.banner-content h1 {
  font-size: 48px;
  margin-bottom: 16px;
  font-weight: bold;
}

.banner-content p {
  font-size: 20px;
  margin-bottom: 32px;
  opacity: 0.9;
}

/* 主内容区 */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px 40px;
}

/* 分类区域 */
.category-section {
  margin-bottom: 48px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  color: #333;
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.category-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  color: #409EFF;
}

.category-item span {
  margin-top: 12px;
  font-size: 15px;
  font-weight: 500;
}

/* 商品区域 */
.products-section {
  margin-bottom: 48px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

/* 响应式 */
@media (max-width: 768px) {
  .banner-item {
    height: 300px;
  }
  
  .banner-content h1 {
    font-size: 32px;
  }
  
  .banner-content p {
    font-size: 16px;
  }
  
  .category-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }
  
  .category-item {
    padding: 16px;
  }
  
  .products-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>

