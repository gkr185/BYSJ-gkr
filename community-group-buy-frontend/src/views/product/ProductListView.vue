<template>
  <div class="product-list-page">
    <!-- 顶部导航栏 -->
    <Header />

    <!-- 主体内容 -->
    <div class="page-container">
      <el-container class="main-container">
        <!-- 左侧分类栏 -->
        <el-aside width="240px" class="category-aside">
          <el-card class="category-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Grid /></el-icon>
                <span>商品分类</span>
              </div>
            </template>
            
            <!-- 全部分类 -->
            <div
              class="category-item all-category"
              :class="{ active: !selectedCategoryId }"
              @click="handleCategorySelect(null)"
            >
              <el-icon><List /></el-icon>
              <span>全部商品</span>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>

            <!-- 分类树 -->
            <el-skeleton v-if="categoryLoading" :rows="8" animated />
            <el-tree
              v-else
              :data="categoryTree"
              :props="treeProps"
              node-key="categoryId"
              :current-node-key="selectedCategoryId"
              :highlight-current="true"
              @node-click="handleCategoryNodeClick"
              class="category-tree"
            >
              <template #default="{ node, data }">
                <div class="tree-node">
                  <el-icon v-if="!data.children || data.children.length === 0">
                    <Document />
                  </el-icon>
                  <el-icon v-else>
                    <Folder />
                  </el-icon>
                  <span class="node-label">{{ node.label }}</span>
                </div>
              </template>
            </el-tree>
          </el-card>

          <!-- 筛选条件卡片 -->
          <el-card class="filter-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><Filter /></el-icon>
                <span>筛选条件</span>
              </div>
            </template>

            <div class="filter-section">
              <div class="filter-label">价格区间</div>
              <el-radio-group v-model="priceRange" @change="handleFilterChange">
                <el-radio label="">不限</el-radio>
                <el-radio label="0-10">0-10元</el-radio>
                <el-radio label="10-20">10-20元</el-radio>
                <el-radio label="20-50">20-50元</el-radio>
                <el-radio label="50+">50元以上</el-radio>
              </el-radio-group>
            </div>

            <el-divider />

            <div class="filter-section">
              <div class="filter-label">库存状态</div>
              <el-radio-group v-model="stockStatus" @change="handleFilterChange">
                <el-radio label="">全部</el-radio>
                <el-radio label="inStock">有货</el-radio>
                <el-radio label="lowStock">库存紧张</el-radio>
              </el-radio-group>
            </div>

            <el-button
              type="primary"
              size="small"
              style="width: 100%; margin-top: 16px"
              @click="resetFilters"
            >
              <el-icon><RefreshLeft /></el-icon>
              重置筛选
            </el-button>
          </el-card>
        </el-aside>

        <!-- 右侧商品列表 -->
        <el-main class="product-main">
          <!-- 面包屑导航 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>商品列表</el-breadcrumb-item>
            <el-breadcrumb-item v-if="selectedCategoryName">
              {{ selectedCategoryName }}
            </el-breadcrumb-item>
          </el-breadcrumb>

          <!-- 工具栏 -->
          <div class="toolbar">
            <div class="toolbar-left">
              <div class="result-info">
                共找到 <span class="count">{{ totalElements }}</span> 件商品
              </div>
              
              <!-- 搜索框 -->
              <el-input
                v-model="searchKeyword"
                placeholder="在当前分类中搜索..."
                style="width: 300px"
                clearable
                @clear="handleSearch"
                @keyup.enter="handleSearch"
              >
                <template #append>
                  <el-button :icon="Search" @click="handleSearch" />
                </template>
              </el-input>
            </div>

            <div class="toolbar-right">
              <!-- 排序 -->
              <el-select v-model="sortType" placeholder="排序方式" @change="handleSortChange">
                <el-option label="最新上架" value="create_time" />
                <el-option label="价格从低到高" value="price_asc" />
                <el-option label="价格从高到低" value="price_desc" />
              </el-select>

              <!-- 视图切换 -->
              <el-radio-group v-model="viewMode" size="small">
                <el-radio-button label="grid">
                  <el-icon><Grid /></el-icon>
                </el-radio-button>
                <el-radio-button label="list">
                  <el-icon><List /></el-icon>
                </el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <!-- 商品网格/列表 -->
          <el-skeleton v-if="loading" :rows="6" animated />

          <!-- 空状态 -->
          <el-empty
            v-else-if="!productList || productList.length === 0"
            description="暂无商品"
            :image-size="200"
          >
            <el-button type="primary" @click="resetFilters">查看全部商品</el-button>
          </el-empty>

          <!-- 网格视图 -->
          <div v-else-if="viewMode === 'grid'" class="product-grid">
            <el-card
              v-for="product in productList"
              :key="product.productId"
              class="product-card"
              shadow="hover"
              @click="goToProductDetail(product.productId)"
            >
              <div class="product-image-wrapper">
                <el-image
                  :src="getProductImageUrl(product.coverImg)"
                  fit="cover"
                  class="product-image"
                  lazy
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon :size="60"><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>

                <!-- 标签 -->
                <div class="product-tags">
                  <el-tag v-if="product.stock === 0" type="info" size="small">已售罄</el-tag>
                  <el-tag v-else-if="product.stock < 50" type="warning" size="small">
                    库存紧张
                  </el-tag>
                  <el-tag v-if="product.groupPrice" type="danger" size="small">拼团</el-tag>
                </div>
              </div>

              <div class="product-info">
                <h3 class="product-name" :title="product.productName">
                  {{ product.productName }}
                </h3>

                <div class="product-detail">
                  {{ product.detail || '暂无描述' }}
                </div>

                <div class="product-price">
                  <div v-if="product.groupPrice" class="group-price-section">
                    <div class="group-price">
                      <span class="label">拼团价</span>
                      <span class="price">¥{{ product.groupPrice }}</span>
                    </div>
                    <div class="original-price">¥{{ product.price }}</div>
                  </div>
                  <div v-else class="single-price">
                    <span class="price">¥{{ product.price }}</span>
                  </div>
                </div>

                <div class="product-footer">
                  <div class="stock-info">
                    <el-icon><Box /></el-icon>
                    库存 {{ product.stock }}
                  </div>
                  <el-button
                    type="primary"
                    size="small"
                    :disabled="product.stock === 0"
                    @click.stop="handleAddToCart(product)"
                  >
                    <el-icon><ShoppingCart /></el-icon>
                    加入购物车
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>

          <!-- 列表视图 -->
          <div v-else class="product-list">
            <el-card
              v-for="product in productList"
              :key="product.productId"
              class="product-list-item"
              shadow="hover"
              @click="goToProductDetail(product.productId)"
            >
              <el-row :gutter="20">
                <el-col :span="6">
                  <div class="list-image-wrapper">
                    <el-image
                      :src="getProductImageUrl(product.coverImg)"
                      fit="cover"
                      class="list-image"
                    >
                      <template #error>
                        <div class="image-error">
                          <el-icon :size="40"><Picture /></el-icon>
                        </div>
                      </template>
                    </el-image>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="list-info">
                    <h3 class="list-product-name">{{ product.productName }}</h3>
                    <div class="list-product-detail">{{ product.detail || '暂无描述' }}</div>
                    <div class="list-tags">
                      <el-tag v-if="product.stock === 0" type="info" size="small">已售罄</el-tag>
                      <el-tag v-else-if="product.stock < 50" type="warning" size="small">
                        库存紧张
                      </el-tag>
                      <el-tag v-if="product.groupPrice" type="danger" size="small">支持拼团</el-tag>
                      <span class="stock-text">库存: {{ product.stock }}</span>
                    </div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="list-action">
                    <div v-if="product.groupPrice" class="list-price-group">
                      <div class="list-group-price">
                        <span class="label">拼团价</span>
                        <span class="price">¥{{ product.groupPrice }}</span>
                      </div>
                      <div class="list-original-price">单买 ¥{{ product.price }}</div>
                    </div>
                    <div v-else class="list-price-single">
                      <span class="price">¥{{ product.price }}</span>
                    </div>
                    <el-button
                      type="primary"
                      size="default"
                      :disabled="product.stock === 0"
                      @click.stop="handleAddToCart(product)"
                    >
                      <el-icon><ShoppingCart /></el-icon>
                      加入购物车
                    </el-button>
                  </div>
                </el-col>
              </el-row>
            </el-card>
          </div>

          <!-- 分页 -->
          <div class="pagination-wrapper" v-if="totalPages > 0">
            <el-pagination
              :current-page="currentPage"
              :page-size="pageSize"
              :page-sizes="[12, 24, 36, 48]"
              :total="totalElements"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Grid,
  List,
  Document,
  Folder,
  ArrowRight,
  Search,
  Filter,
  RefreshLeft,
  Picture,
  Box,
  ShoppingCart
} from '@element-plus/icons-vue'
import Header from '@/components/common/Header.vue'
import { getCategoryTree, getProductList, searchProducts } from '@/api/product'
import { useCartStore } from '@/stores/cart'

// ==================== 数据定义 ====================
const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

// 分类相关
const categoryTree = ref([])
const categoryLoading = ref(false)
const selectedCategoryId = ref(null)
const selectedCategoryName = ref('')
const treeProps = {
  children: 'children',
  label: 'categoryName'
}

// 商品列表
const productList = ref([])
const loading = ref(false)

// 分页
const currentPage = ref(1)
const pageSize = ref(12)
const totalElements = ref(0)
const totalPages = ref(0)

// 搜索和筛选
const searchKeyword = ref('')
const sortType = ref('create_time')
const priceRange = ref('')
const stockStatus = ref('')

// 视图模式
const viewMode = ref('grid')

// ==================== 生命周期 ====================
onMounted(() => {
  // 从URL获取初始参数
  if (route.query.categoryId) {
    selectedCategoryId.value = parseInt(route.query.categoryId)
  }
  if (route.query.keyword) {
    searchKeyword.value = route.query.keyword
  }

  loadCategoryTree()
  loadProductList()
})

// 监听路由变化
watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.categoryId) {
      selectedCategoryId.value = parseInt(newQuery.categoryId)
    } else {
      selectedCategoryId.value = null
    }
    if (newQuery.keyword) {
      searchKeyword.value = newQuery.keyword
    }
    loadProductList()
  }
)

// ==================== 分类加载 ====================
const loadCategoryTree = async () => {
  categoryLoading.value = true
  try {
    const res = await getCategoryTree()
    if (res.code === 200) {
      categoryTree.value = res.data || []
      // 设置选中分类的名称
      if (selectedCategoryId.value) {
        updateSelectedCategoryName()
      }
    }
  } catch (error) {
    console.error('加载分类树失败:', error)
    ElMessage.error('加载分类失败')
  } finally {
    categoryLoading.value = false
  }
}

// 更新选中分类名称
const updateSelectedCategoryName = () => {
  const findCategory = (tree, targetId) => {
    for (const node of tree) {
      if (node.categoryId === targetId) {
        return node.categoryName
      }
      if (node.children && node.children.length > 0) {
        const found = findCategory(node.children, targetId)
        if (found) return found
      }
    }
    return null
  }
  selectedCategoryName.value = findCategory(categoryTree.value, selectedCategoryId.value) || ''
}

// ==================== 商品列表加载 ====================
const loadProductList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1, // 后端从0开始
      size: pageSize.value,
      sort: sortType.value
    }

    // 添加搜索关键词
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }

    // 添加分类筛选
    if (selectedCategoryId.value) {
      params.categoryId = selectedCategoryId.value
    }

    const res = await getProductList(params)
    if (res.code === 200 && res.data) {
      productList.value = filterProducts(res.data.content || [])
      totalElements.value = res.data.totalElements || 0
      totalPages.value = res.data.totalPages || 0
    } else {
      productList.value = []
      totalElements.value = 0
      totalPages.value = 0
    }
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
    productList.value = []
  } finally {
    loading.value = false
  }
}

// 前端筛选（价格区间、库存状态）
const filterProducts = (products) => {
  let filtered = [...products]

  // 价格区间筛选
  if (priceRange.value) {
    if (priceRange.value === '0-10') {
      filtered = filtered.filter(p => p.price >= 0 && p.price <= 10)
    } else if (priceRange.value === '10-20') {
      filtered = filtered.filter(p => p.price > 10 && p.price <= 20)
    } else if (priceRange.value === '20-50') {
      filtered = filtered.filter(p => p.price > 20 && p.price <= 50)
    } else if (priceRange.value === '50+') {
      filtered = filtered.filter(p => p.price > 50)
    }
  }

  // 库存状态筛选
  if (stockStatus.value === 'inStock') {
    filtered = filtered.filter(p => p.stock > 50)
  } else if (stockStatus.value === 'lowStock') {
    filtered = filtered.filter(p => p.stock > 0 && p.stock <= 50)
  }

  return filtered
}

// ==================== 事件处理 ====================

// 分类选择（全部分类）
const handleCategorySelect = (categoryId) => {
  selectedCategoryId.value = categoryId
  selectedCategoryName.value = ''
  currentPage.value = 1
  
  // 更新URL
  router.push({
    path: '/products',
    query: categoryId ? { categoryId } : {}
  })
}

// 分类树节点点击
const handleCategoryNodeClick = (data) => {
  selectedCategoryId.value = data.categoryId
  selectedCategoryName.value = data.categoryName
  currentPage.value = 1
  
  // 更新URL
  router.push({
    path: '/products',
    query: { categoryId: data.categoryId }
  })
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  loadProductList()
}

// 排序变化
const handleSortChange = () => {
  currentPage.value = 1
  loadProductList()
}

// 筛选条件变化
const handleFilterChange = () => {
  currentPage.value = 1
  loadProductList()
}

// 重置筛选
const resetFilters = () => {
  selectedCategoryId.value = null
  selectedCategoryName.value = ''
  searchKeyword.value = ''
  priceRange.value = ''
  stockStatus.value = ''
  sortType.value = 'create_time'
  currentPage.value = 1
  
  router.push({ path: '/products' })
  loadProductList()
}

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  loadProductList()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadProductList()
}

// 加入购物车
const handleAddToCart = (product) => {
  if (product.stock === 0) {
    ElMessage.warning('商品已售罄')
    return
  }
  
  cartStore.addToCart({
    productId: product.productId,
    productName: product.productName,
    price: product.price,
    groupPrice: product.groupPrice,
    coverImg: product.coverImg,
    quantity: 1,
    stock: product.stock
  })
  
  ElMessage.success('已加入购物车')
}

// 跳转到商品详情
const goToProductDetail = (productId) => {
  router.push(`/products/${productId}`)
}

// 获取商品图片URL
const getProductImageUrl = (coverImg) => {
  if (!coverImg) {
    return '/placeholder-product.png'
  }
  // 如果是完整URL，直接返回
  if (coverImg.startsWith('http://') || coverImg.startsWith('https://')) {
    return coverImg
  }
  // 否则拼接基础URL
  return `http://localhost:8062${coverImg}`
}
</script>

<style scoped lang="scss">
.product-list-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-top: 80px; // 避免被固定导航栏遮挡
}

.page-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.main-container {
  background: transparent;
  gap: 20px;
}

// ==================== 左侧分类栏 ====================
.category-aside {
  .category-card,
  .filter-card {
    margin-bottom: 20px;
  }

  .card-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
    color: #303133;
  }

  .all-category {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px;
    margin-bottom: 12px;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s;
    color: #606266;
    background: #f5f7fa;

    &:hover {
      background: #e6e8eb;
      color: #409eff;
    }

    &.active {
      background: #409eff;
      color: #fff;

      .arrow-icon {
        transform: rotate(180deg);
      }
    }

    .arrow-icon {
      margin-left: auto;
      transition: transform 0.3s;
    }
  }

  .category-tree {
    :deep(.el-tree-node__content) {
      height: 40px;
      padding: 0 12px;
      border-radius: 6px;
      
      &:hover {
        background: #f5f7fa;
      }
    }

    :deep(.el-tree-node.is-current > .el-tree-node__content) {
      background: #ecf5ff;
      color: #409eff;
      font-weight: 600;
    }

    .tree-node {
      display: flex;
      align-items: center;
      gap: 8px;
      flex: 1;
    }
  }

  .filter-section {
    margin-bottom: 16px;

    .filter-label {
      font-weight: 600;
      margin-bottom: 12px;
      color: #303133;
    }

    :deep(.el-radio-group) {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .el-radio {
        margin-right: 0;
      }
    }
  }
}

// ==================== 右侧商品列表 ====================
.product-main {
  background: transparent;
  padding: 0;

  :deep(.el-breadcrumb) {
    margin-bottom: 20px;
    padding: 16px;
    background: #fff;
    border-radius: 8px;
  }

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px;
    background: #fff;
    border-radius: 8px;

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 16px;

      .result-info {
        color: #606266;
        font-size: 14px;

        .count {
          color: #409eff;
          font-weight: 600;
          font-size: 16px;
        }
      }
    }

    .toolbar-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }

  // ==================== 网格视图 ====================
  .product-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;

    .product-card {
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      :deep(.el-card__body) {
        padding: 0;
      }

      .product-image-wrapper {
        position: relative;
        width: 100%;
        height: 240px;
        overflow: hidden;
        border-radius: 8px 8px 0 0;

        .product-image {
          width: 100%;
          height: 100%;

          :deep(img) {
            transition: transform 0.3s;
          }
        }

        &:hover :deep(img) {
          transform: scale(1.05);
        }

        .image-error {
          display: flex;
          align-items: center;
          justify-content: center;
          height: 100%;
          background: #f5f7fa;
          color: #c0c4cc;
        }

        .product-tags {
          position: absolute;
          top: 12px;
          right: 12px;
          display: flex;
          gap: 8px;
        }
      }

      .product-info {
        padding: 16px;

        .product-name {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 8px 0;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
          min-height: 48px;
        }

        .product-detail {
          font-size: 13px;
          color: #909399;
          margin-bottom: 12px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .product-price {
          margin-bottom: 12px;

          .group-price-section {
            display: flex;
            align-items: center;
            gap: 12px;

            .group-price {
              .label {
                font-size: 12px;
                color: #f56c6c;
                margin-right: 4px;
              }

              .price {
                font-size: 24px;
                font-weight: 700;
                color: #f56c6c;
              }
            }

            .original-price {
              font-size: 14px;
              color: #909399;
              text-decoration: line-through;
            }
          }

          .single-price {
            .price {
              font-size: 24px;
              font-weight: 700;
              color: #409eff;
            }
          }
        }

        .product-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding-top: 12px;
          border-top: 1px solid #ebeef5;

          .stock-info {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 13px;
            color: #909399;
          }
        }
      }
    }
  }

  // ==================== 列表视图 ====================
  .product-list {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .product-list-item {
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      }

      .list-image-wrapper {
        width: 100%;
        height: 160px;
        border-radius: 8px;
        overflow: hidden;

        .list-image {
          width: 100%;
          height: 100%;
        }

        .image-error {
          display: flex;
          align-items: center;
          justify-content: center;
          height: 100%;
          background: #f5f7fa;
          color: #c0c4cc;
        }
      }

      .list-info {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        height: 100%;

        .list-product-name {
          font-size: 18px;
          font-weight: 600;
          color: #303133;
          margin: 0 0 12px 0;
        }

        .list-product-detail {
          font-size: 14px;
          color: #606266;
          margin-bottom: 16px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        .list-tags {
          display: flex;
          align-items: center;
          gap: 8px;

          .stock-text {
            font-size: 13px;
            color: #909399;
          }
        }
      }

      .list-action {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        justify-content: space-between;
        height: 100%;
        text-align: right;

        .list-price-group {
          .list-group-price {
            margin-bottom: 8px;

            .label {
              font-size: 12px;
              color: #f56c6c;
              display: block;
              margin-bottom: 4px;
            }

            .price {
              font-size: 28px;
              font-weight: 700;
              color: #f56c6c;
            }
          }

          .list-original-price {
            font-size: 14px;
            color: #909399;
            text-decoration: line-through;
          }
        }

        .list-price-single {
          .price {
            font-size: 28px;
            font-weight: 700;
            color: #409eff;
          }
        }
      }
    }
  }

  // ==================== 分页 ====================
  .pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 32px;
    padding: 20px;
    background: #fff;
    border-radius: 8px;
  }
}

// ==================== 响应式设计 ====================
@media (max-width: 768px) {
  .page-container {
    padding: 12px;
  }

  .main-container {
    flex-direction: column;
  }

  .category-aside {
    width: 100% !important;
  }

  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 12px;
  }

  .toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch !important;

    .toolbar-left,
    .toolbar-right {
      flex-direction: column;
      width: 100%;
    }
  }
}
</style>

