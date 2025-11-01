<template>
  <MainLayout>
    <div class="team-detail-page">
      <div class="container">
        <!-- 加载状态 -->
        <el-skeleton v-if="loading" :rows="10" animated />

        <!-- 团详情 -->
        <div v-else-if="teamDetail" class="detail-content">
          <!-- 顶部信息卡片 -->
          <el-card class="header-card" shadow="hover">
            <div class="team-header">
              <div class="header-left">
                <el-tag :type="getStatusType(teamDetail.teamStatus)" size="large">
                  {{ teamDetail.teamStatusDesc }}
                </el-tag>
                <h1 class="team-title">{{ teamDetail.activityName }}</h1>
                <div class="team-info">
                  <span class="team-no">团号：{{ teamDetail.teamNo }}</span>
                  <span class="divider">|</span>
                  <span class="community">{{ teamDetail.communityName || '全平台' }}</span>
                </div>
              </div>
              <div class="header-right">
                <div class="price-box">
                  <span class="label">拼团价</span>
                  <span class="price">¥{{ teamDetail.groupPrice }}</span>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 商品信息 -->
          <el-card v-if="teamDetail.productName" class="product-card" shadow="hover">
            <div class="product-info-section">
              <el-image 
                v-if="teamDetail.productCoverImg"
                :src="teamDetail.productCoverImg" 
                fit="cover"
                class="product-image"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="product-details">
                <h2 class="product-name">{{ teamDetail.productName }}</h2>
                <div class="product-price-info">
                  <div class="price-item">
                    <span class="label">拼团价</span>
                    <span class="price group-price">¥{{ teamDetail.groupPrice }}</span>
                  </div>
                  <div v-if="teamDetail.productPrice" class="price-item">
                    <span class="label">原价</span>
                    <span class="price original-price">¥{{ teamDetail.productPrice }}</span>
                  </div>
                  <div v-if="teamDetail.productPrice && teamDetail.groupPrice" class="price-item">
                    <el-tag type="danger" size="large">
                      立省 ¥{{ (teamDetail.productPrice - teamDetail.groupPrice).toFixed(2) }}
                    </el-tag>
                  </div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 拼团进度 -->
          <el-card class="progress-card" shadow="hover">
            <div class="progress-header">
              <h2>拼团进度</h2>
              <div v-if="teamDetail.teamStatus === 0" class="countdown">
                <el-icon><Timer /></el-icon>
                <span>剩余：{{ remainingTime }}</span>
              </div>
            </div>
            
            <div class="progress-content">
              <div class="progress-bar-wrapper">
                <el-progress 
                  :percentage="progress" 
                  :status="progressStatus"
                  :stroke-width="20"
                />
              </div>
              
              <div class="progress-stats">
                <div class="stat-item">
                  <span class="stat-label">当前人数</span>
                  <span class="stat-value success">{{ teamDetail.currentNum }}</span>
                </div>
                <div class="stat-divider">/</div>
                <div class="stat-item">
                  <span class="stat-label">成团人数</span>
                  <span class="stat-value">{{ teamDetail.requiredNum }}</span>
                </div>
                <div class="stat-divider">|</div>
                <div class="stat-item">
                  <span class="stat-label">还差</span>
                  <span class="stat-value warning">{{ teamDetail.remainNum }}</span>
                  <span class="stat-unit">人</span>
                </div>
              </div>
            </div>

            <!-- 参团按钮 -->
            <div v-if="teamDetail.teamStatus === 0 && teamDetail.remainNum > 0" class="join-actions">
              <el-button 
                type="primary" 
                size="large" 
                :loading="joining"
                @click="handleJoinTeam"
              >
                <el-icon><User /></el-icon>
                立即参团
              </el-button>
              <el-button 
                size="large" 
                @click="handleShareTeam"
              >
                <el-icon><Share /></el-icon>
                分享邀请
              </el-button>
            </div>

            <!-- 成团/失败提示 -->
            <el-alert
              v-if="teamDetail.teamStatus === 1"
              title="拼团成功！"
              type="success"
              :description="`成团时间：${formatTime(teamDetail.successTime)}`"
              show-icon
              :closable="false"
            />
            <el-alert
              v-if="teamDetail.teamStatus === 2"
              title="拼团失败"
              type="error"
              description="拼团已过期，已自动退款到您的余额"
              show-icon
              :closable="false"
            />
          </el-card>

          <!-- 团长信息 -->
          <el-card class="leader-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><User /></el-icon>
                <span>团长信息</span>
              </div>
            </template>
            <div class="leader-info">
              <el-avatar :size="60" :src="teamDetail.leaderAvatar || undefined">
                {{ teamDetail.leaderName?.[0] || '团' }}
              </el-avatar>
              <div class="leader-details">
                <h3>{{ teamDetail.leaderName }}</h3>
                <p class="community-tag">
                  <el-icon><LocationInformation /></el-icon>
                  {{ teamDetail.communityName }}
                </p>
              </div>
            </div>
          </el-card>

          <!-- 成员列表 -->
          <el-card class="members-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <el-icon><UserFilled /></el-icon>
                <span>参团成员（{{ teamDetail.members?.length || 0 }}人）</span>
              </div>
            </template>
            <div class="members-list">
              <div 
                v-for="member in teamDetail.members" 
                :key="member.memberId"
                class="member-item"
              >
                <el-avatar :size="50" :src="member.avatar || undefined">
                  {{ member.realName?.[0] || member.username?.[0] || '用' }}
                </el-avatar>
                <div class="member-info">
                  <div class="member-name">
                    {{ member.realName || member.username }}
                    <el-tag v-if="member.isLauncher === 1" type="warning" size="small">
                      发起人
                    </el-tag>
                  </div>
                  <div class="member-meta">
                    <span>{{ formatTime(member.joinTime) }}</span>
                    <span class="divider">|</span>
                    <el-tag :type="getMemberStatusType(member.status)" size="small">
                      {{ member.statusDesc }}
                    </el-tag>
                  </div>
                </div>
                <div class="member-amount">
                  ¥{{ member.payAmount }}
                </div>
              </div>
            </div>
          </el-card>

          <!-- 分享链接 -->
          <el-card v-if="teamDetail.shareLink" class="share-card">
            <div class="share-content">
              <p class="share-title">邀请好友一起拼团</p>
              <div class="share-link">
                <el-input 
                  :model-value="teamDetail.shareLink" 
                  readonly
                />
                <el-button 
                  type="primary" 
                  @click="handleCopyLink"
                >
                  复制链接
                </el-button>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 错误状态 -->
        <el-empty v-else description="团不存在或已被删除" />
      </div>

      <!-- 参团对话框 -->
      <el-dialog
        v-model="joinDialogVisible"
        title="参与拼团"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form :model="joinForm" :rules="joinRules" ref="joinFormRef" label-width="100px">
          <el-form-item label="收货地址" prop="addressId">
            <el-select 
              v-model="joinForm.addressId" 
              placeholder="请选择收货地址"
              style="width: 100%"
            >
              <el-option
                v-for="addr in addressList"
                :key="addr.addressId"
                :label="`${addr.receiver} ${addr.phone} ${addr.province}${addr.city}${addr.district}${addr.detail}`"
                :value="addr.addressId"
              />
            </el-select>
            <el-button 
              link 
              type="primary" 
              @click="router.push('/user/address')"
              style="margin-top: 8px"
            >
              <el-icon><Plus /></el-icon>
              添加新地址
            </el-button>
          </el-form-item>
          <el-form-item label="购买数量" prop="quantity">
            <el-input-number 
              v-model="joinForm.quantity" 
              :min="1" 
              :max="10"
            />
          </el-form-item>
          <el-form-item label="支付金额">
            <span class="pay-amount">¥{{ totalAmount }}</span>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="joinDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            @click="submitJoinTeam" 
            :loading="joining"
          >
            确认参团
          </el-button>
        </template>
      </el-dialog>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Timer, User, Share, UserFilled, LocationInformation, Plus
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { getTeamDetail, joinTeam } from '@/api/groupbuy'
import { getAddressList } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@/utils/formatter'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据
const loading = ref(true)
const teamDetail = ref(null)
const addressList = ref([])
const remainingTime = ref('')
const timerInterval = ref(null)

// 参团相关
const joining = ref(false)
const joinDialogVisible = ref(false)
const joinFormRef = ref(null)
const joinForm = ref({
  addressId: null,
  quantity: 1
})

const joinRules = {
  addressId: [
    { required: true, message: '请选择收货地址', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入购买数量', trigger: 'blur' }
  ]
}

// 计算属性
const progress = computed(() => {
  if (!teamDetail.value) return 0
  const percent = (teamDetail.value.currentNum / teamDetail.value.requiredNum) * 100
  return Math.min(percent, 100)
})

const progressStatus = computed(() => {
  if (!teamDetail.value) return undefined
  if (teamDetail.value.teamStatus === 1) return 'success'
  if (teamDetail.value.teamStatus === 2) return 'exception'
  return undefined
})

const totalAmount = computed(() => {
  if (!teamDetail.value) return '0.00'
  return (teamDetail.value.groupPrice * joinForm.value.quantity).toFixed(2)
})

// 方法
const fetchTeamDetail = async () => {
  const teamId = route.params.id
  if (!teamId) {
    router.push('/groupbuy')
    return
  }

  loading.value = true
  try {
    const data = await getTeamDetail(teamId)
    teamDetail.value = data
    
    // 启动倒计时
    if (data.teamStatus === 0) {
      startCountdown()
    }
  } catch (error) {
    console.error('获取团详情失败:', error)
    ElMessage.error('获取团详情失败')
  } finally {
    loading.value = false
  }
}

const fetchAddressList = async () => {
  if (!userStore.isLogin) return
  
  try {
    const data = await getAddressList(userStore.userInfo.userId)
    addressList.value = data || []
    
    // 自动选择默认地址
    const defaultAddr = addressList.value.find(addr => addr.isDefault === 1)
    if (defaultAddr) {
      joinForm.value.addressId = defaultAddr.addressId
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
  }
}

const startCountdown = () => {
  if (timerInterval.value) {
    clearInterval(timerInterval.value)
  }
  
  updateRemainingTime()
  timerInterval.value = setInterval(updateRemainingTime, 1000)
}

const updateRemainingTime = () => {
  if (!teamDetail.value?.expireTime) return
  
  const now = new Date().getTime()
  const expireTime = new Date(teamDetail.value.expireTime).getTime()
  const diff = expireTime - now
  
  if (diff <= 0) {
    remainingTime.value = '已过期'
    if (timerInterval.value) {
      clearInterval(timerInterval.value)
    }
    return
  }
  
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)
  
  remainingTime.value = `${hours}小时${minutes}分${seconds}秒`
}

const handleJoinTeam = () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  fetchAddressList()
  joinDialogVisible.value = true
}

const submitJoinTeam = async () => {
  if (!joinFormRef.value) return
  
  await joinFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    joining.value = true
    try {
      const result = await joinTeam({
        teamId: teamDetail.value.teamId,
        addressId: joinForm.value.addressId,
        quantity: joinForm.value.quantity
      })
      
      ElMessage.success('参团成功！请前往支付')
      joinDialogVisible.value = false
      
      // TODO: 跳转到支付页面
      // router.push(`/payment?orderId=${result.orderId}`)
      
      // 刷新团详情
      await fetchTeamDetail()
    } catch (error) {
      console.error('参团失败:', error)
      ElMessage.error(error.message || '参团失败，请重试')
    } finally {
      joining.value = false
    }
  })
}

const handleShareTeam = () => {
  if (teamDetail.value?.shareLink) {
    handleCopyLink()
  } else {
    ElMessage.warning('分享链接不可用')
  }
}

const handleCopyLink = async () => {
  try {
    await navigator.clipboard.writeText(teamDetail.value.shareLink)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制')
  }
}

const getStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return types[status] || 'info'
}

const getMemberStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'info'
  }
  return types[status] || 'info'
}

const formatTime = (time) => {
  return formatDateTime(time)
}

// 生命周期
onMounted(() => {
  fetchTeamDetail()
})

onUnmounted(() => {
  if (timerInterval.value) {
    clearInterval(timerInterval.value)
  }
})
</script>

<style scoped>
.team-detail-page {
  min-height: 100vh;
  padding: 20px 0;
  background: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 顶部信息卡片 */
.header-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  flex: 1;
}

.team-title {
  font-size: 28px;
  font-weight: bold;
  margin: 12px 0;
  color: white;
}

.team-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  opacity: 0.9;
}

.divider {
  opacity: 0.5;
}

.header-right {
  text-align: right;
}

.price-box {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.price-box .label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 4px;
}

.price-box .price {
  font-size: 36px;
  font-weight: bold;
  color: #FFD700;
}

/* 商品信息卡片 */
.product-card {
  background: white;
}

.product-info-section {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.product-image {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  flex-shrink: 0;
  overflow: hidden;
}

.product-image .image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  font-size: 48px;
  color: #c0c4cc;
}

.product-details {
  flex: 1;
}

.product-name {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 16px 0;
}

.product-price-info {
  display: flex;
  gap: 32px;
  align-items: center;
}

.product-price-info .price-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-price-info .label {
  font-size: 14px;
  color: #909399;
}

.product-price-info .price {
  font-size: 28px;
  font-weight: bold;
}

.product-price-info .group-price {
  color: #F56C6C;
}

.product-price-info .original-price {
  color: #909399;
  text-decoration: line-through;
  font-size: 18px;
}

/* 拼团进度卡片 */
.progress-card {
  background: white;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.progress-header h2 {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  color: #F56C6C;
  font-weight: 500;
}

.progress-bar-wrapper {
  margin-bottom: 24px;
}

.progress-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.stat-value.success {
  color: #67C23A;
}

.stat-value.warning {
  color: #E6A23C;
}

.stat-unit {
  font-size: 16px;
  margin-left: 4px;
  color: #909399;
}

.stat-divider {
  font-size: 24px;
  color: #DCDFE6;
}

.join-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
}

/* 团长信息卡片 */
.leader-card {
  background: white;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
}

.leader-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.leader-details h3 {
  font-size: 18px;
  margin: 0 0 8px 0;
  color: #303133;
}

.community-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 14px;
  margin: 0;
}

/* 成员列表 */
.members-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.member-item:hover {
  background: #ecf5ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.1);
}

.member-info {
  flex: 1;
}

.member-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-meta {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-amount {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

/* 分享卡片 */
.share-card {
  background: linear-gradient(135deg, #FFF9E6 0%, #FFE0B2 100%);
}

.share-content {
  text-align: center;
}

.share-title {
  font-size: 16px;
  color: #606266;
  margin-bottom: 16px;
}

.share-link {
  display: flex;
  gap: 12px;
}

/* 支付金额 */
.pay-amount {
  font-size: 24px;
  font-weight: bold;
  color: #F56C6C;
}

/* 响应式 */
@media (max-width: 768px) {
  .team-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-right {
    margin-top: 16px;
    text-align: left;
  }

  .progress-stats {
    flex-wrap: wrap;
  }

  .member-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .member-amount {
    align-self: flex-end;
  }

  .share-link {
    flex-direction: column;
  }
}
</style>

