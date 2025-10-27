<template>
  <div class="team-view">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 团不存在 -->
    <el-empty v-else-if="!team" description="拼团不存在" />

    <!-- 团详情 -->
    <div v-else class="team-container">
      <!-- 商品信息卡片 -->
      <el-card class="product-card" shadow="hover">
        <div class="product-info">
          <div class="product-image">
            <img :src="team.activity.product_img" :alt="team.activity.product_name" />
          </div>
          <div class="product-detail">
            <h2>{{ team.activity.product_name }}</h2>
            <div class="price">
              <span class="current-price">¥{{ team.activity.group_price.toFixed(2) }}</span>
              <span class="original-price">原价：¥{{ team.activity.original_price.toFixed(2) }}</span>
            </div>
            <div class="team-meta">
              <span>{{ team.activity.required_num }}人成团</span>
              <span v-if="team.activity.max_num">· 最多{{ team.activity.max_num }}人</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 团状态卡片 -->
      <el-card class="status-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="title">
              <el-icon><Trophy /></el-icon>
              拼团进度
            </span>
            <el-tag :type="getTeamStatusType(team.team_status)" size="large">
              {{ getTeamStatusText(team.team_status) }}
            </el-tag>
          </div>
        </template>

        <div class="status-content">
          <!-- 团号 -->
          <div class="team-no-section">
            <el-icon><Document /></el-icon>
            <span>团号：{{ team.team_no }}</span>
          </div>

          <!-- 进度条 -->
          <div class="progress-section">
            <el-progress 
              :percentage="Math.floor((team.current_num / team.required_num) * 100)" 
              :status="team.team_status === TEAM_STATUS.SUCCESS ? 'success' : ''"
              :stroke-width="12"
            >
              <template #default="{ percentage }">
                <span class="progress-text">{{ percentage }}%</span>
              </template>
            </el-progress>
            <div class="progress-info">
              <span class="current">已有{{ team.current_num }}人参团</span>
              <span v-if="team.team_status === TEAM_STATUS.JOINING" class="need">
                还差{{ team.required_num - team.current_num }}人成团
              </span>
            </div>
          </div>

          <!-- 倒计时 -->
          <div v-if="team.team_status === TEAM_STATUS.JOINING" class="countdown-section">
            <el-icon><Clock /></el-icon>
            <div class="countdown-content">
              <div class="countdown-label">剩余时间</div>
              <div class="countdown-time" v-if="!team.remaining_time.expired">
                <span class="time-unit">
                  <span class="value">{{ String(team.remaining_time.hours).padStart(2, '0') }}</span>
                  <span class="label">时</span>
                </span>
                <span class="separator">:</span>
                <span class="time-unit">
                  <span class="value">{{ String(team.remaining_time.minutes).padStart(2, '0') }}</span>
                  <span class="label">分</span>
                </span>
                <span class="separator">:</span>
                <span class="time-unit">
                  <span class="value">{{ String(team.remaining_time.seconds).padStart(2, '0') }}</span>
                  <span class="label">秒</span>
                </span>
              </div>
              <div v-else class="countdown-expired">已过期</div>
            </div>
          </div>

          <!-- 成团时间 -->
          <div v-else-if="team.team_status === TEAM_STATUS.SUCCESS" class="success-section">
            <el-icon><CircleCheckFilled /></el-icon>
            <div>
              <div class="success-label">成团时间</div>
              <div class="success-time">{{ formatDateTime(team.success_time) }}</div>
            </div>
          </div>

          <!-- 失败提示 -->
          <div v-else-if="team.team_status === TEAM_STATUS.FAILED" class="failed-section">
            <el-icon><CircleCloseFilled /></el-icon>
            <div>
              <div class="failed-text">拼团已失败</div>
              <div class="failed-tip">款项将原路退回</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 成员列表卡片 -->
      <el-card class="members-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="title">
              <el-icon><UserFilled /></el-icon>
              参团成员（{{ team.members.length }}）
            </span>
          </div>
        </template>

        <div class="members-list">
          <div 
            v-for="member in team.members" 
            :key="member.member_id"
            class="member-item"
          >
            <el-avatar :src="member.avatar" :size="50">
              {{ member.user_name.charAt(0) }}
            </el-avatar>
            <div class="member-info">
              <div class="member-name">
                {{ member.user_name }}
                <el-tag v-if="member.is_launcher" type="danger" size="small">团长</el-tag>
              </div>
              <div class="member-time">{{ formatDateTime(member.join_time) }}</div>
            </div>
            <div class="member-status">
              <el-tag 
                :type="getMemberStatusType(member.status)" 
                size="small"
              >
                {{ getMemberStatusText(member.status) }}
              </el-tag>
            </div>
          </div>

          <!-- 空位提示 -->
          <div 
            v-for="i in (team.required_num - team.current_num)" 
            :key="'empty-' + i"
            class="member-item empty"
          >
            <el-avatar :size="50">
              <el-icon><Plus /></el-icon>
            </el-avatar>
            <div class="member-info">
              <div class="member-name">等待参团...</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-section">
        <!-- 拼团中 - 可以参团 -->
        <template v-if="team.team_status === TEAM_STATUS.JOINING">
          <el-button 
            v-if="!isUserInTeam"
            type="primary" 
            size="large" 
            :icon="ShoppingCart"
            @click="handleJoinTeam"
            :disabled="team.remaining_time.expired"
          >
            立即参团
          </el-button>
          <el-button 
            v-else
            type="danger" 
            size="large" 
            plain
            @click="handleQuitTeam"
          >
            退出拼团
          </el-button>
        </template>

        <!-- 已成团 - 查看订单 -->
        <template v-else-if="team.team_status === TEAM_STATUS.SUCCESS && isUserInTeam">
          <el-button 
            type="success" 
            size="large" 
            :icon="Document"
            @click="goToOrder"
          >
            查看订单
          </el-button>
        </template>

        <!-- 分享按钮 -->
        <el-button 
          v-if="team.team_status === TEAM_STATUS.JOINING"
          size="large" 
          :icon="Share"
          @click="handleShare"
        >
          邀请好友
        </el-button>

        <!-- 返回活动页 -->
        <el-button 
          size="large"
          @click="goBackToActivity"
        >
          返回活动
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Loading, UserFilled, Clock, CircleCheckFilled, CircleCloseFilled,
  Plus, Share, Document, Trophy, ShoppingCart
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  apiGetTeamDetail,
  apiJoinTeam,
  apiQuitTeam,
  TEAM_STATUS,
  MEMBER_STATUS,
  getTeamStatusText,
  getTeamStatusType,
  formatDateTime
} from '@/api/groupbuy'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 数据
const loading = ref(true)
const team = ref(null)
let countdownTimer = null

// 获取团ID
const teamId = ref(route.params.id || route.query.id)

// 当前用户是否在团中
const isUserInTeam = computed(() => {
  if (!team.value || !userStore.isLogin) return false
  const userId = userStore.userInfo.userId || 1
  return team.value.members.some(m => m.user_id === userId)
})

// 当前用户的参团记录
const currentUserMember = computed(() => {
  if (!team.value || !userStore.isLogin) return null
  const userId = userStore.userInfo.userId || 1
  return team.value.members.find(m => m.user_id === userId)
})

// 获取成员状态文本
const getMemberStatusText = (status) => {
  const statusMap = {
    [MEMBER_STATUS.UNPAID]: '待支付',
    [MEMBER_STATUS.PAID]: '已支付',
    [MEMBER_STATUS.SUCCESS]: '已成团',
    [MEMBER_STATUS.CANCELLED]: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取成员状态类型
const getMemberStatusType = (status) => {
  const typeMap = {
    [MEMBER_STATUS.UNPAID]: 'warning',
    [MEMBER_STATUS.PAID]: 'success',
    [MEMBER_STATUS.SUCCESS]: 'success',
    [MEMBER_STATUS.CANCELLED]: 'info'
  }
  return typeMap[status] || 'info'
}

// 加载团详情
const fetchTeamDetail = async () => {
  loading.value = true
  try {
    const data = await apiGetTeamDetail(teamId.value)
    if (data) {
      team.value = data
      startCountdown()
    } else {
      ElMessage.warning('拼团不存在')
    }
  } catch (error) {
    console.error('获取团详情失败:', error)
    ElMessage.error('获取团详情失败')
  } finally {
    loading.value = false
  }
}

// 参团
const handleJoinTeam = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (isUserInTeam.value) {
    ElMessage.warning('您已参加此团')
    return
  }

  try {
    // 跳转到订单确认页，携带团信息
    router.push({
      path: '/order/confirm',
      query: {
        team_id: teamId.value,
        activity_id: team.value.activity_id,
        product_id: team.value.activity.product_id
      }
    })
  } catch (error) {
    console.error('参团失败:', error)
    ElMessage.error(error.message || '参团失败')
  }
}

// 退出拼团
const handleQuitTeam = async () => {
  try {
    await ElMessageBox.confirm(
      '退出后将取消订单并退款，是否确认？',
      '确认退出',
      {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const userId = userStore.userInfo.userId || 1
    await apiQuitTeam(teamId.value, userId)
    
    ElMessage.success('已退出拼团')
    
    // 刷新团详情
    await fetchTeamDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出拼团失败:', error)
      ElMessage.error(error.message || '退出拼团失败')
    }
  }
}

// 分享
const handleShare = () => {
  ElMessage.info('分享功能开发中...')
}

// 查看订单
const goToOrder = () => {
  if (currentUserMember.value && currentUserMember.value.order_id) {
    router.push(`/order/list`)
  } else {
    ElMessage.warning('未找到订单信息')
  }
}

// 返回活动页
const goBackToActivity = () => {
  if (team.value) {
    router.push(`/groupbuy/activity/${team.value.activity_id}`)
  } else {
    router.back()
  }
}

// 启动倒计时
const startCountdown = () => {
  stopCountdown()
  if (team.value && team.value.team_status === TEAM_STATUS.JOINING) {
    countdownTimer = setInterval(() => {
      const end = new Date(team.value.expire_time).getTime()
      const now = new Date().getTime()
      const diff = end - now
      
      if (diff <= 0) {
        team.value.remaining_time = { hours: 0, minutes: 0, seconds: 0, expired: true }
        stopCountdown()
      } else {
        const hours = Math.floor(diff / (1000 * 60 * 60))
        const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
        const seconds = Math.floor((diff % (1000 * 60)) / 1000)
        team.value.remaining_time = { hours, minutes, seconds, expired: false }
      }
    }, 1000)
  }
}

// 停止倒计时
const stopCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

// 生命周期
onMounted(() => {
  fetchTeamDetail()
})

onUnmounted(() => {
  stopCountdown()
})

// 导出给模板使用
defineExpose({
  TEAM_STATUS,
  MEMBER_STATUS
})
</script>

<style scoped>
.team-view {
  min-height: 100vh;
  padding-top: 84px; /* 64px导航栏 + 20px间隔 */
  background-color: #f5f5f5;
}

.team-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px 20px 20px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.loading-container p {
  margin-top: 15px;
  font-size: 14px;
}

.team-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 商品信息卡片 */
.product-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.product-info {
  display: flex;
  gap: 20px;
}

.product-image {
  flex-shrink: 0;
  width: 150px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-detail h2 {
  margin: 0 0 15px 0;
  font-size: 24px;
  font-weight: 600;
}

.price {
  margin-bottom: 10px;
}

.current-price {
  font-size: 32px;
  font-weight: 700;
  margin-right: 15px;
}

.original-price {
  font-size: 14px;
  opacity: 0.8;
  text-decoration: line-through;
}

.team-meta {
  font-size: 14px;
  opacity: 0.9;
}

/* 团状态卡片 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.status-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.team-no-section {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
  padding: 10px;
  background: #F5F7FA;
  border-radius: 4px;
}

.progress-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.progress-info .current {
  color: #606266;
}

.progress-info .need {
  color: #F56C6C;
  font-weight: 500;
}

.countdown-section,
.success-section,
.failed-section {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: #F5F7FA;
  border-radius: 8px;
}

.countdown-section {
  background: linear-gradient(135deg, #FFF8E7 0%, #FFE5D9 100%);
}

.countdown-section .el-icon {
  font-size: 32px;
  color: #E6A23C;
}

.countdown-content {
  flex: 1;
}

.countdown-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.countdown-time {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 28px;
  font-weight: 700;
  color: #E6A23C;
}

.time-unit {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.time-unit .value {
  font-size: 32px;
}

.time-unit .label {
  font-size: 14px;
  font-weight: 400;
}

.separator {
  color: #E6A23C;
  font-weight: 400;
}

.countdown-expired {
  font-size: 18px;
  color: #F56C6C;
  font-weight: 600;
}

.success-section {
  background: linear-gradient(135deg, #E7F9F0 0%, #D4F4DD 100%);
}

.success-section .el-icon {
  font-size: 32px;
  color: #67C23A;
}

.success-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 5px;
}

.success-time {
  font-size: 16px;
  font-weight: 600;
  color: #67C23A;
}

.failed-section {
  background: linear-gradient(135deg, #F5F5F5 0%, #E8E8E8 100%);
}

.failed-section .el-icon {
  font-size: 32px;
  color: #909399;
}

.failed-text {
  font-size: 16px;
  font-weight: 600;
  color: #909399;
  margin-bottom: 5px;
}

.failed-tip {
  font-size: 13px;
  color: #C0C4CC;
}

/* 成员列表 */
.members-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border: 1px solid #EBEEF5;
  border-radius: 8px;
  transition: all 0.3s;
}

.member-item:hover {
  background: #F5F7FA;
  border-color: #DCDFE6;
}

.member-item.empty {
  opacity: 0.5;
  border-style: dashed;
}

.member-item.empty:hover {
  background: transparent;
}

.member-info {
  flex: 1;
}

.member-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-time {
  font-size: 13px;
  color: #909399;
}

/* 操作按钮 */
.action-section {
  display: flex;
  gap: 15px;
  padding: 20px;
  background: #F5F7FA;
  border-radius: 8px;
}

.action-section .el-button {
  flex: 1;
}

/* 响应式 */
@media (max-width: 768px) {
  .team-view {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }

  .product-info {
    flex-direction: column;
  }

  .product-image {
    width: 100%;
    height: auto;
    aspect-ratio: 1;
  }

  .countdown-time {
    font-size: 24px;
  }

  .time-unit .value {
    font-size: 28px;
  }

  .action-section {
    flex-direction: column;
  }
}
</style>

