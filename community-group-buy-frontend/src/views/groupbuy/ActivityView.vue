<template>
  <div class="activity-view">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 活动不存在 -->
    <el-empty v-else-if="!activity" description="活动不存在或已下架" />

    <!-- 活动详情 -->
    <div v-else>
      <!-- 活动信息卡片 -->
      <el-card class="activity-card" shadow="hover">
        <div class="activity-header">
          <div class="product-image">
            <img :src="activity.product_img" :alt="activity.product_name" />
          </div>
          <div class="activity-info">
            <h2 class="product-name">{{ activity.product_name }}</h2>
            <div class="price-section">
              <div class="group-price">
                <span class="label">拼团价</span>
                <span class="price">¥{{ activity.group_price.toFixed(2) }}</span>
              </div>
              <div class="original-price">
                原价：¥{{ activity.original_price.toFixed(2) }}
              </div>
            </div>
            <div class="activity-meta">
              <el-tag :type="getActivityStatusType(activity.status)">
                {{ getActivityStatusText(activity.status) }}
              </el-tag>
              <span class="meta-item">
                <el-icon><UserFilled /></el-icon>
                {{ activity.required_num }}人成团
              </span>
              <span v-if="activity.max_num" class="meta-item">
                <el-icon><StarFilled /></el-icon>
                最多{{ activity.max_num }}人
              </span>
            </div>
            <div class="activity-time">
              <p><el-icon><Clock /></el-icon> 活动时间：{{ formatDateTime(activity.start_time) }} 至 {{ formatDateTime(activity.end_time) }}</p>
            </div>
            <div class="action-buttons">
              <el-button 
                type="primary" 
                size="large" 
                :icon="Plus" 
                @click="handleLaunchTeam"
                :disabled="activity.status !== ACTIVITY_STATUS.ONGOING"
              >
                发起拼团
              </el-button>
              <el-button 
                size="large" 
                :icon="Share" 
                @click="handleShare"
              >
                分享活动
              </el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 团列表 -->
      <el-card class="teams-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="title">
              <el-icon><Grid /></el-icon>
              拼团列表
            </span>
            <el-radio-group v-model="teamFilter" size="small" @change="handleFilterChange">
              <el-radio-button label="all">全部</el-radio-button>
              <el-radio-button label="joining">拼团中</el-radio-button>
              <el-radio-button label="success">已成团</el-radio-button>
            </el-radio-group>
          </div>
        </template>

        <!-- 团列表加载状态 -->
        <div v-if="teamsLoading" class="loading-container">
          <el-icon class="is-loading" :size="30"><Loading /></el-icon>
        </div>

        <!-- 无团数据 -->
        <el-empty v-else-if="teams.length === 0" description="暂无拼团" />

        <!-- 团列表 -->
        <div v-else class="teams-list">
          <div 
            v-for="team in teams" 
            :key="team.team_id" 
            class="team-item"
            @click="goToTeamDetail(team.team_id)"
          >
            <div class="team-header">
              <div class="team-no">
                <el-icon><Document /></el-icon>
                {{ team.team_no }}
              </div>
              <el-tag 
                :type="getTeamStatusType(team.team_status)" 
                size="small"
              >
                {{ getTeamStatusText(team.team_status) }}
              </el-tag>
            </div>

            <div class="team-members">
              <div class="members-avatars">
                <el-avatar 
                  v-for="member in team.members.slice(0, 5)" 
                  :key="member.member_id"
                  :src="member.avatar"
                  :size="40"
                >
                  {{ member.user_name.charAt(0) }}
                </el-avatar>
                <div v-if="team.members.length > 5" class="more-members">
                  +{{ team.members.length - 5 }}
                </div>
              </div>
              <div class="members-info">
                <div class="progress-bar">
                  <el-progress 
                    :percentage="Math.floor((team.current_num / team.required_num) * 100)" 
                    :status="team.team_status === TEAM_STATUS.SUCCESS ? 'success' : ''"
                    :stroke-width="8"
                  />
                </div>
                <div class="progress-text">
                  {{ team.current_num }}/{{ team.required_num }}人
                  <span v-if="team.team_status === TEAM_STATUS.JOINING" class="need-more">
                    还差{{ team.required_num - team.current_num }}人
                  </span>
                </div>
              </div>
            </div>

            <div class="team-footer">
              <div class="team-time">
                <template v-if="team.team_status === TEAM_STATUS.JOINING">
                  <el-icon><Clock /></el-icon>
                  剩余时间: 
                  <span class="countdown">
                    <template v-if="team.remaining_time.expired">已过期</template>
                    <template v-else>
                      {{ team.remaining_time.hours }}小时{{ team.remaining_time.minutes }}分{{ team.remaining_time.seconds }}秒
                    </template>
                  </span>
                </template>
                <template v-else-if="team.team_status === TEAM_STATUS.SUCCESS">
                  <el-icon><CircleCheckFilled /></el-icon>
                  成团时间: {{ formatDateTime(team.success_time) }}
                </template>
                <template v-else>
                  <el-icon><CircleCloseFilled /></el-icon>
                  已失败
                </template>
              </div>
              <el-button 
                v-if="team.team_status === TEAM_STATUS.JOINING"
                type="primary" 
                size="small"
                @click.stop="goToTeamDetail(team.team_id)"
              >
                立即参团
              </el-button>
              <el-button 
                v-else
                size="small"
                @click.stop="goToTeamDetail(team.team_id)"
              >
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Loading, UserFilled, Clock, CircleCheckFilled, CircleCloseFilled,
  Plus, Share, Document, Grid, StarFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import {
  apiGetActivityDetail,
  apiGetActivityTeams,
  apiLaunchTeam,
  ACTIVITY_STATUS,
  TEAM_STATUS,
  getActivityStatusText,
  getTeamStatusText,
  getTeamStatusType,
  formatDateTime
} from '@/api/groupbuy'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 数据
const loading = ref(true)
const teamsLoading = ref(false)
const activity = ref(null)
const teams = ref([])
const teamFilter = ref('all')
let countdownTimer = null

// 获取活动ID
const activityId = ref(route.params.id || route.query.id)

// 获取活动状态类型
const getActivityStatusType = (status) => {
  const typeMap = {
    [ACTIVITY_STATUS.NOT_STARTED]: 'info',
    [ACTIVITY_STATUS.ONGOING]: 'success',
    [ACTIVITY_STATUS.ENDED]: 'info',
    [ACTIVITY_STATUS.ABNORMAL]: 'danger'
  }
  return typeMap[status] || 'info'
}

// 加载活动详情
const fetchActivityDetail = async () => {
  loading.value = true
  try {
    const data = await apiGetActivityDetail(activityId.value)
    if (data) {
      activity.value = data
      await fetchTeams()
    } else {
      ElMessage.warning('活动不存在')
    }
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败')
  } finally {
    loading.value = false
  }
}

// 加载团列表
const fetchTeams = async () => {
  teamsLoading.value = true
  try {
    const data = await apiGetActivityTeams(activityId.value, teamFilter.value)
    teams.value = data
    startCountdown()
  } catch (error) {
    console.error('获取团列表失败:', error)
    ElMessage.error('获取团列表失败')
  } finally {
    teamsLoading.value = false
  }
}

// 筛选条件改变
const handleFilterChange = () => {
  fetchTeams()
}

// 发起拼团
const handleLaunchTeam = async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await ElMessageBox.confirm(
      '发起拼团后，您将成为团长。是否继续？',
      '确认发起',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    // 发起拼团
    const teamData = {
      activity_id: activityId.value,
      user_id: userStore.userInfo.userId || 1,
      leader_id: 1001 // TODO: 应该根据用户地址匹配团长
    }

    const newTeam = await apiLaunchTeam(teamData)
    
    ElMessage.success('发起成功！')
    
    // 跳转到团详情页
    router.push(`/groupbuy/team/${newTeam.team_id}`)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发起拼团失败:', error)
      ElMessage.error(error.message || '发起拼团失败')
    }
  }
}

// 分享活动
const handleShare = () => {
  ElMessage.info('分享功能开发中...')
}

// 跳转到团详情页
const goToTeamDetail = (teamId) => {
  router.push(`/groupbuy/team/${teamId}`)
}

// 启动倒计时
const startCountdown = () => {
  stopCountdown()
  countdownTimer = setInterval(() => {
    teams.value.forEach(team => {
      if (team.team_status === TEAM_STATUS.JOINING && team.remaining_time) {
        const end = new Date(team.expire_time).getTime()
        const now = new Date().getTime()
        const diff = end - now
        
        if (diff <= 0) {
          team.remaining_time = { hours: 0, minutes: 0, seconds: 0, expired: true }
        } else {
          const hours = Math.floor(diff / (1000 * 60 * 60))
          const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
          const seconds = Math.floor((diff % (1000 * 60)) / 1000)
          team.remaining_time = { hours, minutes, seconds, expired: false }
        }
      }
    })
  }, 1000)
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
  fetchActivityDetail()
})

onUnmounted(() => {
  stopCountdown()
})

// 导出给模板使用
defineExpose({
  ACTIVITY_STATUS,
  TEAM_STATUS
})
</script>

<style scoped>
.activity-view {
  min-height: 100vh;
  padding-top: 84px; /* 64px导航栏 + 20px间隔 */
  background-color: #f5f5f5;
}

.activity-container {
  max-width: 1200px;
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

/* 活动信息卡片 */
.activity-card {
  margin-bottom: 20px;
}

.activity-header {
  display: flex;
  gap: 30px;
}

.product-image {
  flex-shrink: 0;
  width: 300px;
  height: 300px;
  border-radius: 8px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-name {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 20px;
}

.group-price {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.group-price .label {
  font-size: 14px;
  color: #F56C6C;
  font-weight: 500;
}

.group-price .price {
  font-size: 36px;
  font-weight: 700;
  color: #F56C6C;
}

.original-price {
  font-size: 14px;
  color: #909399;
  text-decoration: line-through;
}

.activity-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  color: #606266;
}

.activity-time {
  color: #606266;
  font-size: 14px;
}

.activity-time p {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: auto;
}

/* 团列表卡片 */
.teams-card {
  margin-bottom: 20px;
}

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

.teams-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.team-item {
  border: 1px solid #EBEEF5;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
}

.team-item:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.team-no {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.team-members {
  margin-bottom: 15px;
}

.members-avatars {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.more-members {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #F2F6FC;
  color: #909399;
  font-size: 12px;
}

.members-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.progress-bar {
  width: 100%;
}

.progress-text {
  font-size: 13px;
  color: #606266;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.need-more {
  color: #F56C6C;
  font-weight: 500;
}

.team-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #EBEEF5;
}

.team-time {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #909399;
}

.countdown {
  color: #F56C6C;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 768px) {
  .activity-view {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }

  .activity-header {
    flex-direction: column;
  }

  .product-image {
    width: 100%;
    height: auto;
    aspect-ratio: 1;
  }

  .teams-list {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}
</style>

