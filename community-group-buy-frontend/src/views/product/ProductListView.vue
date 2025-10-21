<template>
  <div class="product-list-page">
    <div class="product-list-container">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>商品列表</el-breadcrumb-item>
      <el-breadcrumb-item v-if="selectedCategoryName">{{ selectedCategoryName }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="content-wrapper">
      <!-- 侧边栏筛选 -->
      <aside class="sidebar">
        <el-card class="filter-card">
          <template #header>
            <div class="card-header">
              <span>商品分类</span>
              <el-button 
                v-if="filters.category_id" 
                link 
                type="primary" 
                size="small"
                @click="clearCategory"
              >
                清除
              </el-button>
            </div>
          </template>
          <div class="category-list">
            <div
              v-for="category in categories"
              :key="category.category_id"
              :class="['category-option', { active: filters.category_id === category.category_id }]"
              @click="selectCategory(category.category_id)"
            >
              {{ category.category_name }}
            </div>
          </div>
        </el-card>

        <el-card class="filter-card">
          <template #header>
            <div class="card-header">
              <span>价格区间</span>
              <el-button 
                v-if="filters.minPrice !== undefined || filters.maxPrice !== undefined" 
                link 
                type="primary" 
                size="small"
                @click="clearPrice"
              >
                清除
              </el-button>
            </div>
          </template>
          <div class="price-list">
            <div
              v-for="range in priceRanges"
              :key="range.label"
              :class="['price-option', { active: isPriceRangeActive(range) }]"
              @click="selectPriceRange(range)"
            >
              {{ range.label }}
            </div>
          </div>
        </el-card>
      </aside>

      <!-- 主内容区 -->
      <main class="main-content">
        <el-card class="toolbar-card">
          <div class="toolbar">
            <div class="result-info">
              <span>共 <strong>{{ totalCount }}</strong> 件商品</span>
              <span v-if="filters.keyword" class="keyword-tag">
                搜索: "{{ filters.keyword }}"
                <el-icon class="close-icon" @click="clearKeyword"><Close /></el-icon>
              </span>
            </div>
            <div class="sort-options">
              <el-radio-group v-model="filters.sortBy" @change="handleSortChange">
                <el-radio-button value="default">默认</el-radio-button>
                <el-radio-button value="sales">销量</el-radio-button>
                <el-radio-button value="priceAsc">价格↑</el-radio-button>
                <el-radio-button value="priceDesc">价格↓</el-radio-button>
              </el-radio-group>
            </div>
          </div>
        </el-card>

        <!-- 商品列表 -->
        <div v-if="paginatedResult.items.length > 0" class="product-grid">
          <ProductCard
            v-for="product in paginatedResult.items"
            :key="product.product_id"
            :product="product"
          />
        </div>

        <!-- 空状态 -->
        <el-empty 
          v-else 
          description="暂无商品" 
          :image-size="200"
        >
          <el-button type="primary" @click="router.push('/home')">返回首页</el-button>
        </el-empty>

        <!-- 分页 -->
        <div v-if="paginatedResult.totalPages > 1" class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[12, 24, 36, 48]"
            :total="totalCount"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </main>
    </div>
  </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Close } from '@element-plus/icons-vue'
import ProductCard from '@/components/common/ProductCard.vue'
import { mockProducts, mockCategories } from '@/mock/products'
import { filterProducts, paginate } from '@/utils/productFilter'

const router = useRouter()
const route = useRoute()

// 数据
const categories = ref(mockCategories)
const currentPage = ref(1)
const pageSize = ref(12)

// 筛选条件
const filters = ref({
  category_id: null,
  keyword: '',
  minPrice: undefined,
  maxPrice: undefined,
  sortBy: 'default'
})

// 价格区间选项
const priceRanges = [
  { label: '0-10元', min: 0, max: 10 },
  { label: '10-20元', min: 10, max: 20 },
  { label: '20-50元', min: 20, max: 50 },
  { label: '50元以上', min: 50, max: undefined }
]

// 筛选后的商品
const filteredProducts = computed(() => {
  return filterProducts(mockProducts, filters.value)
})

// 分页后的结果
const paginatedResult = computed(() => {
  return paginate(filteredProducts.value, currentPage.value, pageSize.value)
})

// 总数量
const totalCount = computed(() => filteredProducts.value.length)

// 当前选中的分类名称
const selectedCategoryName = computed(() => {
  if (!filters.value.category_id) return ''
  const category = categories.value.find(c => c.category_id === filters.value.category_id)
  return category ? category.category_name : ''
})

// 判断价格区间是否激活
const isPriceRangeActive = (range) => {
  return filters.value.minPrice === range.min && 
         filters.value.maxPrice === range.max
}

// 从URL参数初始化筛选条件
const initFiltersFromQuery = () => {
  const { category, keyword } = route.query
  
  if (category) {
    filters.value.category_id = parseInt(category)
  }
  
  if (keyword) {
    filters.value.keyword = keyword
  }
}

onMounted(() => {
  initFiltersFromQuery()
})

// 监听路由变化
watch(() => route.query, () => {
  initFiltersFromQuery()
}, { deep: true })

// 方法
const selectCategory = (categoryId) => {
  filters.value.category_id = categoryId
  currentPage.value = 1
  updateURL()
}

const clearCategory = () => {
  filters.value.category_id = null
  currentPage.value = 1
  updateURL()
}

const selectPriceRange = (range) => {
  filters.value.minPrice = range.min
  filters.value.maxPrice = range.max
  currentPage.value = 1
}

const clearPrice = () => {
  filters.value.minPrice = undefined
  filters.value.maxPrice = undefined
  currentPage.value = 1
}

const clearKeyword = () => {
  filters.value.keyword = ''
  currentPage.value = 1
  updateURL()
}

const handleSortChange = () => {
  currentPage.value = 1
}

const handlePageChange = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleSizeChange = () => {
  currentPage.value = 1
}

const updateURL = () => {
  const query = {}
  if (filters.value.category_id) query.category = filters.value.category_id
  if (filters.value.keyword) query.keyword = filters.value.keyword
  router.replace({ query })
}
</script>

<style scoped>
.product-list-page {
  min-height: 100vh;
  padding-top: 84px; /* 64px导航栏 + 20px间隔 */
  background-color: #f5f5f5;
}

.product-list-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 20px 20px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.content-wrapper {
  display: flex;
  gap: 20px;
}

/* 侧边栏样式 */
.sidebar {
  width: 220px;
  flex-shrink: 0;
}

.filter-card {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.category-list,
.price-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-option,
.price-option {
  padding: 10px 12px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
  color: #666;
}

.category-option:hover,
.price-option:hover {
  background-color: #f5f7fa;
  color: #409EFF;
}

.category-option.active,
.price-option.active {
  background-color: #ecf5ff;
  color: #409EFF;
  font-weight: bold;
}

/* 主内容区样式 */
.main-content {
  flex: 1;
  min-width: 0;
}

.toolbar-card {
  margin-bottom: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-info {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #666;
}

.keyword-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background-color: #ecf5ff;
  color: #409EFF;
  border-radius: 4px;
  font-size: 14px;
}

.close-icon {
  cursor: pointer;
}

.sort-options {
  flex-shrink: 0;
}

/* 商品网格样式 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

/* 分页样式 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-list-page {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }
  
  .content-wrapper {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
  }
  
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>

