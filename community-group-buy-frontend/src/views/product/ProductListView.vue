<template>
  <MainLayout>
    <div class="product-list-page">
      <div class="container">
        <div class="page-layout">
          <!-- 左侧分类筛选 -->
          <aside class="sidebar">
            <el-card class="filter-card">
              <template #header>
                <div class="card-header">
                  <span>商品分类</span>
                </div>
              </template>
              <div class="category-tree">
                <div
                  v-for="category in categories"
                  :key="category.categoryId"
                  class="category-item"
                  :class="{ active: selectedCategoryId === category.categoryId }"
                  @click="selectCategory(category.categoryId)"
                >
                  <el-icon><FolderOpened /></el-icon>
                  <span>{{ category.categoryName }}</span>
                </div>
                <div
                  class="category-item"
                  :class="{ active: selectedCategoryId === null }"
                  @click="selectCategory(null)"
                >
                  <el-icon><Grid /></el-icon>
                  <span>全部商品</span>
                </div>
              </div>
            </el-card>
          </aside>

          <!-- 右侧商品列表 -->
          <main class="main-content">
            <!-- 筛选工具栏 -->
            <el-card class="toolbar-card">
              <div class="toolbar">
                <div class="search-box">
                  <el-input
                    v-model="searchKeyword"
                    placeholder="搜索商品"
                    @keyup.enter="handleSearch"
                    clearable
                  >
                    <template #append>
                      <el-button :icon="Search" @click="handleSearch" />
                    </template>
                  </el-input>
                </div>
                <div class="sort-options">
                  <el-radio-group v-model="sortType" @change="handleSort">
                    <el-radio-button label="create_time">最新</el-radio-button>
                    <el-radio-button label="price_asc">价格升序</el-radio-button>
                    <el-radio-button label="price_desc">价格降序</el-radio-button>
                  </el-radio-group>
                </div>
              </div>
            </el-card>

            <!-- 商品列表 -->
            <div class="products-section" v-loading="loading">
              <div v-if="products.length > 0" class="products-grid">
                <ProductCard
                  v-for="product in products"
                  :key="product.productId"
                  :product="product"
                />
              </div>
              <el-empty v-else description="暂无商品" />
            </div>

            <!-- 分页 -->
            <div class="pagination-wrapper">
              <el-pagination
                :current-page="currentPage"
                :page-size="pageSize"
                :total="total"
                :page-sizes="[12, 24, 48]"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="handlePageChange"
                @size-change="handleSizeChange"
              />
            </div>
          </main>
        </div>
      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MainLayout from '@/components/common/MainLayout.vue'
import ProductCard from '@/components/common/ProductCard.vue'
import { Search, FolderOpened, Grid } from '@element-plus/icons-vue'
import { getCategoryList, getProductList } from '@/api/product'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

// 分类列表
const categories = ref([])
const selectedCategoryId = ref(null)

// 搜索和排序
const searchKeyword = ref('')
const sortType = ref('create_time')

// 商品列表
const products = ref([])
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 获取分类列表
const fetchCategories = async () => {
  try {
    const data = await getCategoryList()
    categories.value = data.filter(cat => cat.status === 1)
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  }
}

// 获取商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sort: sortType.value
    }

    if (selectedCategoryId.value) {
      params.categoryId = selectedCategoryId.value
    }

    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    const data = await getProductList(params)
    products.value = data.content || []
    total.value = data.totalElements || 0
  } catch (error) {
    console.error('Failed to fetch products:', error)
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// 选择分类
const selectCategory = (categoryId) => {
  selectedCategoryId.value = categoryId
  currentPage.value = 1
  router.push({
    path: '/products',
    query: {
      ...route.query,
      categoryId: categoryId || undefined,
      page: 1
    }
  })
  fetchProducts()
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  router.push({
    path: '/products',
    query: {
      ...route.query,
      keyword: searchKeyword.value || undefined,
      page: 1
    }
  })
  fetchProducts()
}

// 排序
const handleSort = () => {
  currentPage.value = 1
  router.push({
    path: '/products',
    query: {
      ...route.query,
      sort: sortType.value,
      page: 1
    }
  })
  fetchProducts()
}

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  router.push({
    path: '/products',
    query: {
      ...route.query,
      page
    }
  })
  fetchProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 每页数量变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchProducts()
}

// 监听路由变化
watch(() => route.query, (newQuery) => {
  if (route.path === '/products') {
    selectedCategoryId.value = newQuery.categoryId ? Number(newQuery.categoryId) : null
    searchKeyword.value = newQuery.keyword || ''
    sortType.value = newQuery.sort || 'create_time'
    currentPage.value = newQuery.page ? Number(newQuery.page) : 1
  }
}, { immediate: true })

onMounted(() => {
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped>
.product-list-page {
  min-height: 100vh;
  padding: 20px 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-layout {
  display: flex;
  gap: 20px;
}

/* 左侧边栏 */
.sidebar {
  width: 240px;
  flex-shrink: 0;
}

.filter-card {
  position: sticky;
  top: 140px;
}

.card-header {
  font-size: 16px;
  font-weight: bold;
}

.category-tree {
  max-height: 600px;
  overflow-y: auto;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.3s;
}

.category-item:hover {
  background-color: #f5f7fa;
}

.category-item.active {
  background-color: #ecf5ff;
  color: #409EFF;
  font-weight: 500;
}

/* 主内容区 */
.main-content {
  flex: 1;
  min-width: 0;
}

.toolbar-card {
  margin-bottom: 20px;
}

.toolbar {
  display: flex;
  gap: 20px;
  align-items: center;
}

.search-box {
  flex: 1;
  max-width: 400px;
}

.sort-options {
  flex-shrink: 0;
}

/* 商品区域 */
.products-section {
  min-height: 400px;
  margin-bottom: 20px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 响应式 */
@media (max-width: 1024px) {
  .page-layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .filter-card {
    position: static;
  }

  .category-tree {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    max-height: none;
  }

  .category-item {
    flex: 0 0 auto;
  }
}

@media (max-width: 768px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: 100%;
  }

  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  }
}
</style>

