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

        <!-- 拼团专区 -->
        <div v-if="product && groupBuyActivities.length > 0" class="groupbuy-section">
          <div class="section-header">
            <h2 class="section-title">
              <el-icon><Grid /></el-icon>
              拼团专区
            </h2>
            <p class="section-desc">团购更优惠，支持本社区优先配送</p>
          </div>

          <el-skeleton v-if="groupbuyLoading" :rows="4" animated />

          <div v-else class="activities-list">
            <el-card 
              v-for="activity in groupBuyActivities" 
              :key="activity.activityId"
              class="activity-card"
              shadow="hover"
            >
              <!-- 活动信息 -->
              <div class="activity-header">
                <div class="activity-info">
                  <el-tag type="danger" size="large" effect="dark">拼团活动</el-tag>
                  <div class="price-info">
                    <span class="group-price">¥{{ activity.groupPrice }}</span>
                    <span class="price-label">拼团价</span>
                  </div>
                  <div class="people-info">
                    <el-icon><User /></el-icon>
                    <span>{{ activity.requiredNum }}人成团</span>
                  </div>
                </div>
                <div class="activity-time">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatActivityTime(activity.startTime, activity.endTime) }}</span>
                </div>
              </div>

              <!-- 团列表 -->
              <div v-if="activity.teams && activity.teams.length > 0" class="teams-section">
                <div class="teams-header">
                  <span class="teams-count">进行中的团（{{ activity.teams.length }}）</span>
                  <el-button 
                    v-if="activity.teams.length > 3"
                    link 
                    type="primary"
                    @click="viewAllTeams(activity)"
                  >
                    查看全部
                  </el-button>
                </div>

                <div class="teams-list">
                  <div 
                    v-for="team in activity.teams.slice(0, 3)" 
                    :key="team.teamId"
                    class="team-item"
                  >
                    <!-- 团信息 -->
                    <div class="team-header">
                      <div class="team-info">
                        <span class="team-no">{{ team.teamNo }}</span>
                        <el-tag 
                          v-if="team.communityId === userStore.userInfo?.communityId"
                          type="success" 
                          size="small"
                          effect="dark"
                        >
                          本社区
                        </el-tag>
                        <el-tag 
                          v-else-if="team.communityName"
                          size="small"
                        >
                          {{ team.communityName }}
                        </el-tag>
                      </div>
                      <span class="leader-name">{{ team.leaderName }}团长</span>
                    </div>

                    <!-- 进度条 -->
                    <div class="team-progress">
                      <div class="progress-info">
                        <span class="progress-text">{{ team.currentNum }}/{{ team.requiredNum }}人</span>
                        <span class="expire-time">{{ getExpireTime(team.expireTime) }}</span>
                      </div>
                      <el-progress 
                        :percentage="(team.currentNum / team.requiredNum) * 100"
                        :stroke-width="10"
                        :color="getProgressColor(team.currentNum, team.requiredNum)"
                      />
                    </div>

                    <!-- 参团按钮 -->
                    <el-button 
                      type="primary"
                      size="large"
                      class="join-button"
                      @click="handleJoinTeam(team, activity)"
                    >
                      <el-icon><UserFilled /></el-icon>
                      立即参团
                    </el-button>
                  </div>
                </div>
              </div>

              <!-- 无团提示 -->
              <div v-else class="no-teams">
                <el-icon :size="40" color="#909399"><InfoFilled /></el-icon>
                <p>该活动暂无进行中的团</p>
                <p class="hint">可以等待团长发起新的团哦~</p>
              </div>
            </el-card>
          </div>
        </div>

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
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MainLayout from '@/components/common/MainLayout.vue'
import ProductCard from '@/components/common/ProductCard.vue'
import { ShoppingCart, Grid, User, Clock, UserFilled, InfoFilled } from '@element-plus/icons-vue'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { getProductDetail, getCategoryList, getRecommendProducts } from '@/api/product'
import { getProductGroupBuyActivities, joinTeam } from '@/api/groupbuy'
import { ElMessage, ElMessageBox } from 'element-plus'

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

// 拼团活动
const groupBuyActivities = ref([])
const groupbuyLoading = ref(false)

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
    // 获取拼团活动
    fetchGroupBuyActivities(productId)
  } catch (error) {
    console.error('Failed to fetch product detail:', error)
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

// 获取拼团活动
const fetchGroupBuyActivities = async (productId) => {
  groupbuyLoading.value = true
  try {
    const params = userStore.userInfo?.communityId 
      ? { communityId: userStore.userInfo.communityId } 
      : {}
    const data = await getProductGroupBuyActivities(productId, params)
    groupBuyActivities.value = data || []
  } catch (error) {
    console.error('Failed to fetch group buy activities:', error)
    // 静默失败，不影响用户体验
    groupBuyActivities.value = []
  } finally {
    groupbuyLoading.value = false
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

// 参团
const handleJoinTeam = async (team, activity) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  // 简化版：直接跳转到团详情页
  router.push(`/groupbuy/team/${team.teamId}`)
}

// 查看全部团
const viewAllTeams = (activity) => {
  router.push(`/groupbuy/activity/${activity.activityId}`)
}

// 格式化活动时间
const formatActivityTime = (start, end) => {
  const startDate = new Date(start).toLocaleDateString('zh-CN')
  const endDate = new Date(end).toLocaleDateString('zh-CN')
  return `${startDate} ~ ${endDate}`
}

// 获取过期时间
const getExpireTime = (expireTime) => {
  const now = new Date()
  const expire = new Date(expireTime)
  const diff = expire - now
  
  if (diff <= 0) return '已过期'
  
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  
  if (hours > 24) {
    const days = Math.floor(hours / 24)
    return `剩余${days}天`
  } else if (hours > 0) {
    return `剩余${hours}小时`
  } else {
    return `剩余${minutes}分钟`
  }
}

// 进度条颜色
const getProgressColor = (current, required) => {
  const percentage = (current / required) * 100
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 50) return '#e6a23c'
  return '#409eff'
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

/* 拼团专区 */
.groupbuy-section {
  margin: 40px 0;
  padding: 30px;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe6e6 100%);
  border-radius: 12px;
  border: 2px dashed #f56c6c;
}

.groupbuy-section .section-header {
  margin-bottom: 24px;
  text-align: center;
}

.groupbuy-section .section-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
  margin: 0 0 8px 0;
}

.groupbuy-section .section-desc {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.activities-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.activity-card {
  border-radius: 12px;
  transition: all 0.3s;
}

.activity-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(245, 108, 108, 0.2);
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f5f7fa;
}

.activity-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.price-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.group-price {
  font-size: 28px;
  font-weight: bold;
  color: #f56c6c;
  line-height: 1;
}

.price-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.people-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #606266;
  padding: 8px 16px;
  background: #f5f7fa;
  border-radius: 20px;
}

.activity-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.teams-section {
  margin-top: 16px;
}

.teams-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.teams-count {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.teams-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.team-item {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.team-item:hover {
  background: #ecf5ff;
  transform: translateX(4px);
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.team-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.team-no {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.leader-name {
  font-size: 13px;
  color: #606266;
}

.team-progress {
  margin-bottom: 12px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.progress-text {
  color: #303133;
  font-weight: 500;
}

.expire-time {
  color: #f56c6c;
}

.join-button {
  width: 100%;
}

.no-teams {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.no-teams p {
  margin: 8px 0;
  font-size: 14px;
}

.no-teams .hint {
  font-size: 12px;
  color: #c0c4cc;
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

