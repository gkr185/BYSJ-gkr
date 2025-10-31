<template>
  <MainLayout>
    <div class="my-groupbuys-page">
      <div class="container">
        <h1 class="page-title">我的拼团</h1>

        <!-- 加载中 -->
        <div v-if="loading" class="loading-wrapper">
          <el-icon class="is-loading" :size="60"><Loading /></el-icon>
          <p>加载中...</p>
        </div>

        <!-- 拼团记录列表 -->
        <template v-else-if="teams.length > 0">
          <el-row :gutter="20">
            <el-col
              v-for="team in teams"
              :key="team.teamId"
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
            >
              <el-card class="team-card" @click="goToTeamDetail(team.teamId)">
                <!-- 状态标签 -->
                <div class="status-badge" :class="getStatusClass(team.teamStatus)">
                  {{ team.teamStatusDesc }}
                </div>

                <!-- 团信息 -->
                <div class="team-info">
                  <h3 class="team-title">{{ team.activityName }}</h3>
                  <div class="team-meta">
                    <el-icon><User /></el-icon>
                    <span>{{ team.leaderName || '团长' }}</span>
                  </div>
                  <div class="team-meta">
                    <el-icon><Location /></el-icon>
                    <span>{{ team.communityName || '社区' }}</span>
                  </div>
                </div>

                <!-- 进度信息 -->
                <div class="team-progress">
                  <div class="progress-bar">
                    <div
                      class="progress-fill"
                      :style="{ width: getProgressPercent(team) + '%' }"
                    ></div>
                  </div>
                  <div class="progress-text">
                    {{ team.currentNum }}/{{ team.requiredNum }}人
                  </div>
                </div>

                <!-- 价格和时间 -->
                <div class="team-footer">
                  <div class="price">
                    <span class="label">拼团价</span>
                    <span class="value">¥{{ team.groupPrice }}</span>
                  </div>
                  <div class="time" v-if="team.teamStatus === 0">
                    <el-icon><Clock /></el-icon>
                    <span>{{ formatExpireTime(team.expireTime) }}</span>
                  </div>
                  <div class="time success" v-else-if="team.teamStatus === 1">
                    <el-icon><SuccessFilled /></el-icon>
                    <span>{{ formatSuccessTime(team.successTime) }}</span>
                  </div>
                </div>

                <!-- 我的参团状态 -->
                <div class="my-status" v-if="team.members && team.members.length > 0">
                  <template v-for="member in team.members">
                    <el-tag
                      v-if="member.userId === userStore.userInfo.userId"
                      :key="member.memberId"
                      :type="getMemberStatusType(member.status)"
                      size="small"
                    >
                      {{ member.isLauncher ? '发起人' : '参与者' }} · {{ member.statusDesc }}
                    </el-tag>
                  </template>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </template>

        <!-- 空状态 -->
        <el-empty
          v-else
          description="您还没有参与过拼团"
          :image-size="200"
        >
          <el-button type="primary" @click="router.push('/groupbuys')">
            去拼团
          </el-button>
        </el-empty>
      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  Loading,
  User,
  Location,
  Clock,
  SuccessFilled
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { getMyTeams } from '@/api/groupbuy'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const teams = ref([])

// 获取我的拼团记录
const fetchMyTeams = async () => {
  loading.value = true
  try {
    const data = await getMyTeams()
    teams.value = data || []
    console.log('我的拼团记录:', teams.value)
  } catch (error) {
    console.error('获取拼团记录失败:', error)
    ElMessage.error(error.message || '获取拼团记录失败')
  } finally {
    loading.value = false
  }
}

// 跳转到团详情
const goToTeamDetail = (teamId) => {
  router.push(`/team/${teamId}`)
}

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 0:
      return 'status-joining'
    case 1:
      return 'status-success'
    case 2:
      return 'status-failed'
    default:
      return ''
  }
}

// 获取进度百分比
const getProgressPercent = (team) => {
  if (!team.requiredNum) return 0
  return Math.min((team.currentNum / team.requiredNum) * 100, 100)
}

// 格式化过期时间
const formatExpireTime = (expireTime) => {
  if (!expireTime) return ''
  const now = new Date()
  const expire = new Date(expireTime)
  const diff = expire - now
  
  if (diff <= 0) return '已过期'
  
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟后结束`
  } else {
    return `${minutes}分钟后结束`
  }
}

// 格式化成团时间
const formatSuccessTime = (successTime) => {
  if (!successTime) return ''
  const date = new Date(successTime)
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  return `${month}月${day}日 ${hour}:${minute.toString().padStart(2, '0')} 成团`
}

// 获取成员状态标签类型
const getMemberStatusType = (status) => {
  switch (status) {
    case 0:
      return 'warning' // 待支付
    case 1:
      return 'info' // 已支付
    case 2:
      return 'success' // 已成团
    case 3:
      return 'danger' // 已退款
    default:
      return 'info'
  }
}

// 页面加载时获取拼团记录
onMounted(async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  await fetchMyTeams()
})
</script>

<style scoped>
.my-groupbuys-page {
  min-height: 100vh;
  padding: 20px 0;
  background-color: #f5f5f5;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 30px;
  color: #333;
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.loading-wrapper p {
  margin-top: 20px;
  font-size: 16px;
  color: #909399;
}

.team-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.team-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.status-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  color: white;
}

.status-joining {
  background-color: #f57c00;
}

.status-success {
  background-color: #67c23a;
}

.status-failed {
  background-color: #f56c6c;
}

.team-info {
  margin-bottom: 16px;
}

.team-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
  padding-right: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.team-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}

.team-meta .el-icon {
  color: #909399;
}

.team-progress {
  margin-bottom: 16px;
}

.progress-bar {
  height: 8px;
  background-color: #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #f57c00 0%, #ff9800 100%);
  border-radius: 4px;
  transition: width 0.3s;
}

.progress-text {
  font-size: 13px;
  color: #666;
  text-align: right;
}

.team-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
  margin-bottom: 12px;
}

.price .label {
  font-size: 12px;
  color: #909399;
  margin-right: 6px;
}

.price .value {
  font-size: 20px;
  font-weight: bold;
  color: #f57c00;
}

.time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #f57c00;
}

.time.success {
  color: #67c23a;
}

.time .el-icon {
  font-size: 14px;
}

.my-status {
  margin-top: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-title {
    font-size: 24px;
    margin-bottom: 20px;
  }

  .team-title {
    font-size: 15px;
  }

  .price .value {
    font-size: 18px;
  }
}
</style>
