<template>
  <MainLayout>
    <div class="product-detail-container">
      <el-skeleton v-if="loading" :rows="10" animated />
      
      <template v-else-if="product">
        <!-- 商品主要信息区 -->
        <div class="product-main">
          <el-row :gutter="32">
            <!-- 左侧：商品图片 -->
            <el-col :xs="24" :sm="24" :md="12" :lg="10">
              <div class="product-gallery">
                <el-image
                  :src="productImageUrl"
                  :preview-src-list="[productImageUrl]"
                  fit="cover"
                  class="main-image"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon :size="80"><Picture /></el-icon>
                      <div>暂无图片</div>
                    </div>
                  </template>
                </el-image>
                
                <!-- 标签 -->
                <div class="product-tags">
                  <el-tag v-if="product.stock > 0" type="success" effect="dark">
                    <el-icon><Check /></el-icon> 有货
                  </el-tag>
                  <el-tag v-else type="info" effect="dark">
                    <el-icon><Close /></el-icon> 已售罄
                  </el-tag>
                </div>
              </div>
            </el-col>

            <!-- 右侧：商品信息和购买选项 -->
            <el-col :xs="24" :sm="24" :md="12" :lg="14">
              <div class="product-info">
                <!-- 商品名称 -->
                <h1 class="product-name">{{ product.productName }}</h1>

                <!-- 价格区 -->
                <div class="price-section">
                  <!-- 拼团价 -->
                  <div v-if="product.groupPrice" class="group-price-box">
                    <div class="price-label">拼团价</div>
                    <div class="price-value">
                      <span class="currency">¥</span>
                      <span class="amount">{{ product.groupPrice }}</span>
                      <el-tag type="danger" size="small" class="save-tag">
                        省 ¥{{ (product.price - product.groupPrice).toFixed(2) }}
                      </el-tag>
                    </div>
                  </div>

                  <!-- 原价 -->
                  <div class="original-price-box">
                    <div class="price-label">单买价</div>
                    <div class="price-value">
                      <span class="currency">¥</span>
                      <span class="amount">{{ product.price }}</span>
                    </div>
                  </div>
                </div>

                <!-- 拼团快览区（简洁版）-->
                <div v-if="hasGroupBuy" class="groupbuy-preview">
                  <div class="preview-header">
                    <div class="header-left">
                      <el-icon color="#f56c6c"><UserFilled /></el-icon>
                      <span class="preview-title">拼团进行中</span>
                      <el-tag type="danger" size="small">{{ totalTeamsCount }}个团</el-tag>
                    </div>
                    <el-button
                      type="danger"
                      size="small"
                      @click="goToGroupBuyDetail"
                    >
                      查看全部 <el-icon><ArrowRight /></el-icon>
                    </el-button>
                  </div>

                  <!-- 滚动的团列表 -->
                  <div class="teams-carousel">
                    <el-carousel
                      height="80px"
                      direction="vertical"
                      :autoplay="true"
                      :interval="3000"
                      indicator-position="none"
                      arrow="never"
                    >
                      <el-carousel-item
                        v-for="team in previewTeams"
                        :key="team.teamId"
                      >
                        <div class="team-preview-item">
                          <div class="team-avatar">
                            <el-icon :size="24"><User /></el-icon>
                          </div>
                          <div class="team-info">
                            <div class="team-main">
                              <span class="leader-name">{{ team.leaderName }}</span>
                              <span class="team-text">的团</span>
                              <el-tag v-if="isUserCommunity(team.communityId)" type="success" size="small">
                                本社区
                              </el-tag>
                            </div>
                            <div class="team-progress-mini">
                              <span class="progress-text">还差{{ team.requiredNum - team.currentNum }}人成团</span>
                              <el-progress
                                :percentage="getTeamProgress(team)"
                                :show-text="false"
                                :stroke-width="4"
                                :color="getProgressColor(team)"
                              />
                            </div>
                          </div>
                          <el-button
                            type="danger"
                            size="small"
                            class="join-quick-btn"
                            @click="handleQuickJoin(team)"
                          >
                            去拼团
                          </el-button>
                        </div>
                      </el-carousel-item>
                    </el-carousel>
                  </div>
                </div>

                <!-- 暂无拼团区域 -->
                <div v-else-if="canGroupBuy && !hasGroupBuy" class="no-groupbuy-box">
                  <div class="no-groupbuy-content">
                    <el-icon :size="48" color="#909399"><Warning /></el-icon>
                    <div class="no-groupbuy-text">
                      <div class="no-groupbuy-title">暂无进行中的拼团</div>
                      <div class="no-groupbuy-desc">该商品支持拼团购买，成为团长可发起拼团活动</div>
                    </div>
                  </div>
                  <el-button
                    type="danger"
                    size="large"
                    class="start-group-btn"
                    :icon="Plus"
                    @click="handleStartGroupBuy"
                  >
                    我要发起团购
                  </el-button>
                </div>

                <!-- 商品属性 -->
                <div class="product-attrs">
                  <div class="attr-item">
                    <span class="attr-label">库存</span>
                    <span class="attr-value">{{ product.stock }} 件</span>
                  </div>
                  <div class="attr-item">
                    <span class="attr-label">分类</span>
                    <span class="attr-value">{{ categoryName }}</span>
                  </div>
                </div>

                <!-- 数量选择 -->
                <div class="quantity-section">
                  <span class="quantity-label">购买数量</span>
                  <el-input-number
                    v-model="quantity"
                    :min="1"
                    :max="product.stock"
                    :disabled="product.stock === 0"
                  />
                </div>

                <!-- 操作按钮 -->
                <div class="action-buttons">
                  <!-- 加入购物车 -->
                  <el-button
                    size="large"
                    class="cart-btn"
                    :icon="ShoppingCart"
                    @click="handleAddToCart"
                    :disabled="product.stock === 0"
                  >
                    加入购物车
                  </el-button>

                  <!-- 立即购买 -->
                  <el-button
                    type="warning"
                    size="large"
                    class="buy-btn"
                    :icon="ShoppingBag"
                    @click="handleDirectBuy"
                    :disabled="product.stock === 0"
                  >
                    立即购买
                  </el-button>

                  <!-- 马上拼团（大按钮）-->
                  <el-button
                    v-if="hasGroupBuy"
                    type="danger"
                    size="large"
                    class="groupbuy-main-btn"
                    :icon="UserFilled"
                    @click="goToGroupBuyDetail"
                  >
                    <span class="btn-text">马上拼团</span>
                    <span class="btn-price">¥{{ product.groupPrice }}</span>
                  </el-button>
                </div>

                <!-- 温馨提示 -->
                <div class="tips-box">
                  <el-alert
                    v-if="hasGroupBuy"
                    title="拼团更优惠"
                    type="success"
                    :closable="false"
                  >
                    参与拼团可享优惠价，成团即发货，支持本社区优先配送
                  </el-alert>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 商品详情 -->
        <div class="product-detail-section">
          <div class="section-header">
            <h2 class="section-title">
              <el-icon><Document /></el-icon>
              商品详情
            </h2>
          </div>
          <div class="detail-content" v-html="product.detail || '暂无详情'"></div>
        </div>

        <!-- 推荐商品 -->
        <div class="recommend-section">
          <div class="section-header">
            <h2 class="section-title">
              <el-icon><Star /></el-icon>
              推荐商品
            </h2>
          </div>
          <div v-loading="recommendLoading" class="products-grid">
            <ProductCard
              v-for="item in recommendProducts"
              :key="item.productId"
              :product="item"
            />
          </div>
        </div>
      </template>

      <!-- 商品不存在 -->
      <el-empty v-else description="商品不存在或已下架" :image-size="200" />
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Picture, Check, Close, User, ShoppingCart, ShoppingBag, UserFilled,
  ArrowRight, Document, Star, Warning, Plus
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import ProductCard from '@/components/common/ProductCard.vue'
import { getProductDetail, getRecommendProducts, getCategoryDetail } from '@/api/product'
import { getProductGroupBuyActivities } from '@/api/groupbuy'
import { getProductImageUrl } from '@/utils/image'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 数据
const loading = ref(false)
const product = ref(null)
const quantity = ref(1)
const categoryName = ref('未分类')

// 拼团相关
const groupBuyLoading = ref(false)
const groupBuyActivities = ref([])

// 推荐商品
const recommendLoading = ref(false)
const recommendProducts = ref([])

// 计算属性
const productImageUrl = computed(() => {
  return getProductImageUrl(product.value)
})

const hasGroupBuy = computed(() => {
  return product.value?.groupPrice && groupBuyActivities.value.length > 0
})

// 是否可以拼团（商品设置了拼团价）
const canGroupBuy = computed(() => {
  return product.value?.groupPrice && product.value.groupPrice > 0
})

// 计算总团数
const totalTeamsCount = computed(() => {
  return groupBuyActivities.value.reduce((total, activity) => {
    return total + (activity.teams?.length || 0)
  }, 0)
})

// 预览团列表（最多显示5个，本社区优先）
const previewTeams = computed(() => {
  const allTeams = []
  groupBuyActivities.value.forEach(activity => {
    if (activity.teams && activity.teams.length > 0) {
      activity.teams.forEach(team => {
        allTeams.push({ ...team, activityId: activity.activityId })
      })
    }
  })
  
  // 本社区优先排序
  allTeams.sort((a, b) => {
    const aIsMyCommunity = isUserCommunity(a.communityId)
    const bIsMyCommunity = isUserCommunity(b.communityId)
    if (aIsMyCommunity && !bIsMyCommunity) return -1
    if (!aIsMyCommunity && bIsMyCommunity) return 1
    return 0
  })
  
  return allTeams.slice(0, 5)
})

// 获取商品详情
const fetchProductDetail = async () => {
  loading.value = true
  try {
    const productId = route.params.id
    const res = await getProductDetail(productId)
    
    if (res.code === 200) {
      product.value = res.data
      console.log('✅ 商品详情加载成功:', product.value)
      
      // 获取分类名称
      if (product.value.categoryId) {
        fetchCategoryName(product.value.categoryId)
      }
      
      // 获取拼团活动
      if (product.value.groupPrice) {
        fetchGroupBuyActivities(productId)
      }
      
      // 获取推荐商品
      fetchRecommendProducts(product.value.categoryId)
    } else {
      ElMessage.error(res.message || '获取商品详情失败')
    }
  } catch (error) {
    console.error('❌ 获取商品详情失败:', error)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

// 获取分类名称
const fetchCategoryName = async (categoryId) => {
  try {
    const res = await getCategoryDetail(categoryId)
    if (res.code === 200) {
      categoryName.value = res.data.categoryName
    }
  } catch (error) {
    console.error('❌ 获取分类名称失败:', error)
  }
}

// 获取拼团活动
const fetchGroupBuyActivities = async (productId) => {
  groupBuyLoading.value = true
  try {
    const params = {
      communityId: userStore.userInfo?.communityId
    }
    const res = await getProductGroupBuyActivities(productId, params)
    
    if (res.code === 200) {
      groupBuyActivities.value = res.data || []
      console.log('✅ 拼团活动加载成功:', groupBuyActivities.value.length, '个活动')
    }
  } catch (error) {
    console.error('❌ 获取拼团活动失败:', error)
  } finally {
    groupBuyLoading.value = false
  }
}

// 获取推荐商品
const fetchRecommendProducts = async (categoryId) => {
  recommendLoading.value = true
  try {
    const res = await getRecommendProducts({ categoryId, limit: 8 })
    if (res.code === 200) {
      recommendProducts.value = res.data || []
    }
  } catch (error) {
    console.error('❌ 获取推荐商品失败:', error)
  } finally {
    recommendLoading.value = false
  }
}

// 加入购物车
const handleAddToCart = () => {
  if (!product.value) return
  
  cartStore.addItem(product.value, quantity.value)
  ElMessage.success(`已添加 ${quantity.value} 件商品到购物车`)
}

// 立即购买
const handleDirectBuy = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // TODO: 跳转到订单确认页
  ElMessage.info('立即购买功能开发中')
}

// 跳转到拼团详情页
const goToGroupBuyDetail = () => {
  const productId = route.params.id
  router.push(`/groupbuy/product/${productId}`)
}

// 快速参团
const handleQuickJoin = (team) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 跳转到拼团详情页并定位到该团
  router.push({
    path: `/groupbuy/product/${route.params.id}`,
    query: { teamId: team.teamId }
  })
}

// 判断是否为用户社区
const isUserCommunity = (communityId) => {
  return userStore.userInfo?.communityId === communityId
}

// 计算团进度百分比
const getTeamProgress = (team) => {
  return Math.round((team.currentNum / team.requiredNum) * 100)
}

// 获取进度条颜色
const getProgressColor = (team) => {
  const progress = team.currentNum / team.requiredNum
  if (progress >= 0.8) return '#f56c6c'
  if (progress >= 0.5) return '#e6a23c'
  return '#909399'
}

// 处理发起团购
const handleStartGroupBuy = () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  // 判断用户是否是团长
  if (userStore.isLeader) {
    // 是团长，跳转到团长工作台
    ElMessage.success('即将跳转到团长工作台，您可以在那里发起拼团活动')
    router.push('/leader/dashboard')
  } else {
    // 不是团长，跳转到申请成为团长页面
    ElMessageBox.confirm(
      '发起拼团活动需要成为团长，是否前往申请成为团长？',
      '提示',
      {
        confirmButtonText: '去申请',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
      .then(() => {
        router.push('/leader/apply')
      })
      .catch(() => {
        ElMessage.info('您可以随时申请成为团长')
      })
  }
}

onMounted(() => {
  fetchProductDetail()
})
</script>

<style scoped>
.product-detail-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

/* 商品主要信息区 */
.product-main {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

/* 商品图片区 */
.product-gallery {
  position: relative;
}

.main-image {
  width: 100%;
  height: 500px;
  border-radius: 12px;
  overflow: hidden;
}

.main-image :deep(.el-image__inner) {
  object-fit: contain;
  background: #f5f5f5;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 500px;
  background: #f5f5f5;
  color: #ccc;
  font-size: 16px;
}

.product-tags {
  position: absolute;
  top: 16px;
  left: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 商品信息区 */
.product-info {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: 28px;
  color: #333;
  margin: 0 0 24px 0;
  line-height: 1.4;
  font-weight: 600;
}

/* 价格区 */
.price-section {
  background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
  border: 2px solid #f56c6c;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
}

.group-price-box {
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #e0e0e0;
}

.price-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.price-value {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.price-value .currency {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.group-price-box .price-value .amount {
  font-size: 42px;
  color: #f56c6c;
  font-weight: bold;
  line-height: 1;
}

.original-price-box .price-value .amount {
  font-size: 28px;
  color: #666;
  font-weight: 600;
}

.save-tag {
  margin-left: 8px;
}

/* 拼团快览区（新增简洁版）*/
.groupbuy-preview {
  background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
  border: 1px solid #ffccc7;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.teams-carousel {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.team-preview-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  height: 80px;
}

.team-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8787 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.team-info {
  flex: 1;
  min-width: 0;
}

.team-main {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.leader-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.team-text {
  font-size: 14px;
  color: #666;
}

.team-progress-mini {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.progress-text {
  font-size: 12px;
  color: #999;
}

.join-quick-btn {
  flex-shrink: 0;
  height: 36px;
  padding: 0 20px;
  font-weight: 600;
}

/* 暂无拼团区域 */
.no-groupbuy-box {
  background: linear-gradient(135deg, #f5f7fa 0%, #fff 100%);
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
  text-align: center;
}

.no-groupbuy-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.no-groupbuy-text {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.no-groupbuy-title {
  font-size: 18px;
  font-weight: 600;
  color: #606266;
}

.no-groupbuy-desc {
  font-size: 14px;
  color: #909399;
}

.start-group-btn {
  min-width: 200px;
  border-radius: 12px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
  transition: all 0.3s;
}

.start-group-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 108, 108, 0.4);
}

/* 商品属性 */
.product-attrs {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
}

.attr-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.attr-label {
  width: 80px;
  color: #666;
  font-size: 14px;
}

.attr-value {
  color: #333;
  font-size: 15px;
  font-weight: 500;
}

/* 数量选择 */
.quantity-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.quantity-label {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.cart-btn,
.buy-btn {
  flex: 1;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
}

.cart-btn {
  border-color: #409EFF;
  color: #409EFF;
}

.cart-btn:hover {
  background: #409EFF;
  color: #fff;
}

/* 马上拼团大按钮 */
.groupbuy-main-btn {
  flex: 2;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.groupbuy-main-btn .btn-text {
  font-size: 18px;
}

.groupbuy-main-btn .btn-price {
  font-size: 24px;
  font-weight: bold;
}

/* 温馨提示 */
.tips-box {
  margin-top: auto;
}

/* 商品详情 */
.product-detail-section {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.section-header {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  color: #333;
  margin: 0;
  font-weight: 600;
}

.detail-content {
  color: #666;
  line-height: 1.8;
  font-size: 15px;
}

/* 推荐商品 */
.recommend-section {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

/* 响应式 */
@media (max-width: 768px) {
  .product-detail-container {
    padding: 12px;
  }

  .product-main {
    padding: 16px;
  }

  .main-image {
    height: 300px;
  }

  .product-name {
    font-size: 20px;
  }

  .group-price-box .price-value .amount {
    font-size: 32px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .cart-btn,
  .buy-btn,
  .groupbuy-main-btn {
    flex: none;
    width: 100%;
  }

  .product-detail-section,
  .recommend-section {
    padding: 16px;
  }

  .products-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>
