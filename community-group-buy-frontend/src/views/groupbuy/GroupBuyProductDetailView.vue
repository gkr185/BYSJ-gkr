<template>
  <MainLayout>
    <div class="groupbuy-detail-container">
      <!-- 商品信息卡片 -->
      <div v-if="product" class="product-summary-card">
        <div class="product-brief">
          <el-image
            :src="productImageUrl"
            fit="cover"
            class="product-thumb"
          >
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <div class="product-text">
            <h2 class="product-title">{{ product.productName }}</h2>
            <div class="product-prices">
              <div class="group-price">
                <span class="label">拼团价</span>
                <span class="price">¥{{ product.groupPrice }}</span>
              </div>
              <div class="original-price">
                单买价 ¥{{ product.price }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 拼团活动列表 -->
      <div class="activities-container">
        <el-skeleton v-if="loading" :rows="8" animated />

        <template v-else-if="groupBuyActivities.length > 0">
          <div
            v-for="activity in groupBuyActivities"
            :key="activity.activityId"
            class="activity-section"
          >
            <!-- 活动标题 -->
            <div class="activity-header">
              <div class="activity-info">
                <el-tag type="danger" size="large" class="activity-tag">
                  {{ activity.requiredNum }}人团
                </el-tag>
                <span class="activity-price">¥{{ activity.groupPrice }}</span>
                <span class="activity-desc">成团即发货</span>
              </div>
              <div class="activity-time">
                <el-icon><Clock /></el-icon>
                {{ formatDate(activity.startTime) }} - {{ formatDate(activity.endTime) }}
              </div>
            </div>

            <!-- 团列表 -->
            <div v-if="activity.teams && activity.teams.length > 0" class="teams-section">
              <div class="section-header">
                <h3 class="section-title">
                  <el-icon><UserFilled /></el-icon>
                  进行中的团（{{ activity.teams.length }}个）
                </h3>
              </div>

              <div class="teams-grid">
                <div
                  v-for="team in activity.teams"
                  :key="team.teamId"
                  class="team-card"
                  :class="{ 'my-community': isUserCommunity(team.communityId), 'highlight': highlightTeamId === team.teamId }"
                >
                  <!-- 团头部 -->
                  <div class="team-header">
                    <div class="team-leader">
                      <div class="leader-avatar">
                        <el-icon :size="24"><User /></el-icon>
                      </div>
                      <div class="leader-info">
                        <div class="leader-name">{{ team.leaderName }}</div>
                        <div class="leader-role">团长</div>
                      </div>
                    </div>
                    <el-tag
                      v-if="isUserCommunity(team.communityId)"
                      type="success"
                      effect="dark"
                      class="community-tag"
                    >
                      <el-icon><LocationFilled /></el-icon>
                      本社区
                    </el-tag>
                    <el-tag v-else type="info" size="small" class="community-tag">
                      {{ team.communityName }}
                    </el-tag>
                  </div>

                  <!-- 团信息 -->
                  <div class="team-body">
                    <div class="team-meta">
                      <div class="meta-item">
                        <span class="meta-label">团号</span>
                        <span class="meta-value">{{ team.teamNo }}</span>
                      </div>
                      <div class="meta-item">
                        <span class="meta-label">成团人数</span>
                        <span class="meta-value">{{ team.requiredNum }}人</span>
                      </div>
                    </div>

                    <!-- 进度条 -->
                    <div class="team-progress">
                      <div class="progress-header">
                        <span class="progress-label">拼团进度</span>
                        <span class="progress-count">{{ team.currentNum }}/{{ team.requiredNum }}人</span>
                      </div>
                      <el-progress
                        :percentage="getTeamProgress(team)"
                        :color="getProgressColor(team)"
                        :stroke-width="12"
                      >
                        <template #default="{ percentage }">
                          <span class="progress-text">{{ percentage }}%</span>
                        </template>
                      </el-progress>
                      <div class="progress-tip">
                        还差 <span class="highlight-num">{{ team.requiredNum - team.currentNum }}</span> 人成团
                      </div>
                    </div>

                    <!-- 倒计时 -->
                    <div v-if="team.expireTime" class="team-countdown">
                      <el-icon><Timer /></el-icon>
                      剩余时间：{{ formatCountdown(team.expireTime) }}
                    </div>
                  </div>

                  <!-- 操作按钮 -->
                  <div class="team-footer">
                    <el-button
                      type="danger"
                      size="large"
                      class="join-btn"
                      @click="handleJoinTeam(team, activity)"
                    >
                      <el-icon><UserFilled /></el-icon>
                      参与拼团
                    </el-button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 无团提示 -->
            <el-empty v-else description="暂无进行中的团" :image-size="120">
              <el-button
                v-if="userStore.userInfo?.role === 2"
                type="danger"
                @click="handleLaunchTeam(activity)"
              >
                <el-icon><Plus /></el-icon>
                我是团长，发起新团
              </el-button>
              <div v-else class="empty-tip">
                等待团长发起拼团...
              </div>
            </el-empty>
          </div>
        </template>

        <!-- 无活动 -->
        <el-empty v-else description="该商品暂无拼团活动" :image-size="200" />
      </div>

      <!-- 地址选择对话框 -->
      <el-dialog
        v-model="addressDialogVisible"
        title="选择收货地址"
        width="600px"
        :close-on-click-modal="false"
      >
        <div class="address-selection">
          <el-empty v-if="addressList.length === 0" description="暂无收货地址">
            <el-button type="primary" @click="handleGoToAddressManage">
              <el-icon><Location /></el-icon>
              添加收货地址
            </el-button>
          </el-empty>

          <el-radio-group v-else v-model="selectedAddress" class="address-radio-group">
            <el-radio
              v-for="address in addressList"
              :key="address.addressId"
              :label="address.addressId"
              class="address-radio-item"
            >
              <div class="address-card-mini">
                <div class="address-header-mini">
                  <span class="receiver-name">{{ address.receiverName || address.receiver }}</span>
                  <span class="receiver-phone">{{ address.receiverPhone || address.phone }}</span>
                  <el-tag v-if="address.isDefault" type="danger" size="small" effect="dark">
                    默认
                  </el-tag>
                </div>
                <div class="address-detail-mini">
                  <el-icon><Location /></el-icon>
                  <span>{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress || address.detail }}</span>
                </div>
              </div>
            </el-radio>
          </el-radio-group>

          <div v-if="addressList.length > 0" class="address-actions-row">
            <el-button text type="primary" @click="handleGoToAddressManage">
              <el-icon><Plus /></el-icon>
              添加新地址
            </el-button>
          </div>

          <!-- 拼团信息摘要 -->
          <div v-if="selectedActivity" class="join-summary">
            <div class="summary-title">
              <el-icon><ShoppingCart /></el-icon>
              拼团信息
            </div>
            <div class="summary-content">
              <div class="summary-item">
                <span class="label">拼团价格</span>
                <span class="value price">¥{{ selectedActivity.groupPrice }}</span>
              </div>
              <div class="summary-item">
                <span class="label">成团人数</span>
                <span class="value">{{ selectedActivity.requiredNum }}人</span>
              </div>
              <div class="summary-item">
                <span class="label">当前进度</span>
                <span class="value">{{ selectedTeam?.currentNum || 0 }}/{{ selectedActivity.requiredNum }}人</span>
              </div>
            </div>
          </div>
        </div>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="addressDialogVisible = false">取消</el-button>
            <el-button
              type="danger"
              :loading="joiningTeam"
              :disabled="!selectedAddress"
              @click="handleConfirmJoin"
            >
              <el-icon v-if="!joiningTeam"><UserFilled /></el-icon>
              {{ joiningTeam ? '参团中...' : '确认参团' }}
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Picture, User, UserFilled, Clock, Timer, LocationFilled, Plus, Location, PhoneFilled, ShoppingCart
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { getProductDetail } from '@/api/product'
import { getProductGroupBuyActivities, joinTeam } from '@/api/groupbuy'
import { getUserAddresses } from '@/api/user'
import { getProductImageUrl } from '@/utils/image'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据
const loading = ref(false)
const product = ref(null)
const groupBuyActivities = ref([])
const highlightTeamId = ref(null)

// 计算属性
const productImageUrl = computed(() => {
  return getProductImageUrl(product.value)
})

// 获取商品详情
const fetchProductDetail = async (productId) => {
  try {
    const res = await getProductDetail(productId)
    if (res.code === 200) {
      product.value = res.data
    }
  } catch (error) {
    console.error('❌ 获取商品详情失败:', error)
  }
}

// 获取拼团活动
const fetchGroupBuyActivities = async (productId) => {
  loading.value = true
  try {
    const params = {
      communityId: userStore.userInfo?.communityId
    }
    const res = await getProductGroupBuyActivities(productId, params)
    
    if (res.code === 200) {
      groupBuyActivities.value = res.data || []
      console.log('✅ 拼团活动加载成功:', groupBuyActivities.value.length, '个')
    }
  } catch (error) {
    console.error('❌ 获取拼团活动失败:', error)
    ElMessage.error('获取拼团活动失败')
  } finally {
    loading.value = false
  }
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

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

// 格式化倒计时
const formatCountdown = (expireTime) => {
  if (!expireTime) return '计算中...'
  
  const now = new Date().getTime()
  const expire = new Date(expireTime).getTime()
  const diff = expire - now
  
  if (diff <= 0) return '已过期'
  
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  
  return `${hours}小时${minutes}分钟`
}

// 地址选择对话框
const addressDialogVisible = ref(false)
const addressList = ref([])
const selectedAddress = ref(null)
const selectedTeam = ref(null)
const selectedActivity = ref(null)
const joiningTeam = ref(false)

// 参与拼团
const handleJoinTeam = async (team, activity) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 保存选中的团和活动信息
  selectedTeam.value = team
  selectedActivity.value = activity
  
  // 加载用户地址列表
  await loadUserAddresses()
  
  // 显示地址选择对话框
  addressDialogVisible.value = true
}

// 加载用户地址列表
const loadUserAddresses = async () => {
  try {
    const res = await getUserAddresses(userStore.userInfo.userId)
    if (res.code === 200) {
      addressList.value = res.data || []
      
      // 默认选中默认地址
      const defaultAddr = addressList.value.find(addr => addr.isDefault)
      if (defaultAddr) {
        selectedAddress.value = defaultAddr.addressId
      } else if (addressList.value.length > 0) {
        selectedAddress.value = addressList.value[0].addressId
      }
    }
  } catch (error) {
    console.error('❌ 加载地址列表失败:', error)
  }
}

// 确认参团
const handleConfirmJoin = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  joiningTeam.value = true
  try {
    const res = await joinTeam({
      teamId: selectedTeam.value.teamId,
      addressId: selectedAddress.value,
      quantity: 1
    })
    
    if (res.code === 200) {
      const { orderId, payAmount, teamNo } = res.data
      
      ElMessage.success(`参团成功！团号：${teamNo}`)
      
      // 关闭对话框
      addressDialogVisible.value = false
      
      // 跳转到支付页面
      router.push({
        path: '/payment',
        query: {
          orderId: orderId,
          amount: payAmount,
          type: 'groupbuy'
        }
      })
    } else {
      ElMessage.error(res.message || '参团失败')
    }
  } catch (error) {
    console.error('❌ 参团失败:', error)
    ElMessage.error(error.message || '参团失败，请稍后重试')
  } finally {
    joiningTeam.value = false
  }
}

// 跳转到地址管理页面
const handleGoToAddressManage = () => {
  router.push('/user/address')
}

// 发起新团
const handleLaunchTeam = (activity) => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  if (userStore.userInfo?.role !== 2) {
    ElMessage.warning('只有团长才能发起拼团')
    return
  }
  
  router.push(`/leader/launch/${activity.activityId}`)
}

// 初始化
onMounted(() => {
  const productId = route.params.productId
  if (productId) {
    fetchProductDetail(productId)
    fetchGroupBuyActivities(productId)
    
    // 如果有teamId参数，高亮显示
    if (route.query.teamId) {
      highlightTeamId.value = parseInt(route.query.teamId)
      // 3秒后取消高亮
      setTimeout(() => {
        highlightTeamId.value = null
      }, 3000)
    }
  }
})

// 监听路由变化
watch(() => route.params.productId, (newId) => {
  if (newId) {
    fetchProductDetail(newId)
    fetchGroupBuyActivities(newId)
  }
})
</script>

<style scoped>
.groupbuy-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* 商品信息卡片 */
.product-summary-card {
  background: linear-gradient(135deg, #fff 0%, #fffaf5 100%);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.1);
  border: 2px solid #ffccc7;
}

.product-brief {
  display: flex;
  align-items: center;
  gap: 24px;
}

.product-thumb {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #ccc;
  font-size: 40px;
}

.product-text {
  flex: 1;
}

.product-title {
  font-size: 24px;
  color: #333;
  margin: 0 0 16px 0;
  font-weight: 600;
}

.product-prices {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.group-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.group-price .label {
  font-size: 14px;
  color: #666;
}

.group-price .price {
  font-size: 32px;
  color: #f56c6c;
  font-weight: bold;
}

.original-price {
  font-size: 16px;
  color: #999;
  text-decoration: line-through;
}

/* 活动列表 */
.activities-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.activity-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 20px;
  margin-bottom: 24px;
  border-bottom: 2px dashed #f0f0f0;
}

.activity-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.activity-tag {
  font-size: 16px;
  font-weight: bold;
  padding: 8px 16px;
}

.activity-price {
  font-size: 28px;
  color: #f56c6c;
  font-weight: bold;
}

.activity-desc {
  font-size: 14px;
  color: #666;
}

.activity-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #999;
  font-size: 14px;
}

/* 团列表 */
.teams-section {
  margin-top: 24px;
}

.section-header {
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  color: #333;
  font-weight: 600;
  margin: 0;
}

.teams-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

.team-card {
  background: linear-gradient(135deg, #fff 0%, #fffbf5 100%);
  border: 2px solid #f0f0f0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s;
}

.team-card:hover {
  border-color: #f56c6c;
  box-shadow: 0 8px 24px rgba(245, 108, 108, 0.2);
  transform: translateY(-4px);
}

.team-card.my-community {
  border-color: #67c23a;
  background: linear-gradient(135deg, #f0f9ff 0%, #f0fff4 100%);
}

.team-card.highlight {
  animation: highlight-pulse 2s ease-in-out 3;
}

@keyframes highlight-pulse {
  0%, 100% {
    box-shadow: 0 8px 24px rgba(245, 108, 108, 0.2);
  }
  50% {
    box-shadow: 0 8px 32px rgba(245, 108, 108, 0.4);
  }
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.team-leader {
  display: flex;
  align-items: center;
  gap: 12px;
}

.leader-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8787 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.leader-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.leader-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.leader-role {
  font-size: 12px;
  color: #999;
}

.community-tag {
  display: flex;
  align-items: center;
  gap: 4px;
}

.team-body {
  margin-bottom: 16px;
}

.team-meta {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
  padding: 12px;
  background: rgba(245, 108, 108, 0.05);
  border-radius: 8px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-size: 12px;
  color: #999;
}

.meta-value {
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

.team-progress {
  margin-bottom: 12px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.progress-label {
  font-size: 14px;
  color: #666;
}

.progress-count {
  font-size: 14px;
  color: #f56c6c;
  font-weight: bold;
}

.progress-text {
  font-size: 12px;
  font-weight: bold;
}

.progress-tip {
  margin-top: 8px;
  font-size: 13px;
  color: #666;
  text-align: center;
}

.highlight-num {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.team-countdown {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #fff3e0;
  border-radius: 6px;
  color: #e6a23c;
  font-size: 13px;
  font-weight: 600;
}

.team-footer {
  margin-top: 16px;
}

.join-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.empty-tip {
  margin-top: 12px;
  font-size: 14px;
  color: #999;
}

/* 地址选择对话框 */
.address-selection {
  max-height: 500px;
  overflow-y: auto;
}

.address-radio-group {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-radio-item {
  width: 100%;
  margin: 0;
  padding: 0;
}

.address-radio-item :deep(.el-radio__label) {
  width: 100%;
  padding-left: 12px;
}

.address-card-mini {
  width: 100%;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #f9fafb;
  transition: all 0.3s;
}

.address-card-mini:hover {
  border-color: #f56c6c;
  background: #fff;
}

.address-radio-item :deep(.el-radio__input.is-checked) + .el-radio__label .address-card-mini {
  border-color: #f56c6c;
  background: #fff5f5;
}

.address-header-mini {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.receiver-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.receiver-phone {
  font-size: 14px;
  color: #666;
}

.address-detail-mini {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.address-detail-mini .el-icon {
  margin-top: 3px;
  color: #999;
  flex-shrink: 0;
}

.address-actions-row {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e5e7eb;
  text-align: center;
}

.join-summary {
  margin-top: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #fff5f5 0%, #fffbf5 100%);
  border-radius: 8px;
  border: 2px solid #ffccc7;
}

.summary-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.summary-item .label {
  color: #666;
}

.summary-item .value {
  color: #333;
  font-weight: 600;
}

.summary-item .value.price {
  color: #f56c6c;
  font-size: 18px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式 */
@media (max-width: 768px) {
  .groupbuy-detail-container {
    padding: 12px;
  }

  .product-brief {
    flex-direction: column;
    text-align: center;
  }

  .product-thumb {
    width: 100px;
    height: 100px;
  }

  .product-title {
    font-size: 20px;
  }

  .activity-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .teams-grid {
    grid-template-columns: 1fr;
  }
}
</style>

