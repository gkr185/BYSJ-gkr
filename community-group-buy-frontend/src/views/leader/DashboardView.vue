<template>
  <div class="leader-dashboard-wrapper">
    <div class="leader-dashboard-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>团长工作台</h2>
        <p class="subtitle">欢迎回来，{{ userStore.userInfo?.realName || '团长' }}</p>
      </div>

      <!-- 数据卡片区域 -->
      <el-row :gutter="20" class="stats-row">
        <!-- 本月佣金 -->
        <el-col :span="6" :xs="12">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon :size="32"><Coin /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                <el-skeleton v-if="loadingCommission" :rows="1" animated />
                <span v-else>¥{{ commissionSummary?.monthly || 0 }}</span>
              </div>
              <div class="stat-label">本月佣金</div>
            </div>
          </el-card>
        </el-col>

        <!-- 活跃拼团 -->
        <el-col :span="6" :xs="12">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <el-icon :size="32"><UserFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                <el-skeleton v-if="loadingTeams" :rows="1" animated />
                <span v-else>{{ activeTeamsCount }}</span>
              </div>
              <div class="stat-label">活跃拼团</div>
            </div>
          </el-card>
        </el-col>

        <!-- 今日订单 -->
        <el-col :span="6" :xs="12">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <el-icon :size="32"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                <el-tag type="info" size="small">待开发</el-tag>
              </div>
              <div class="stat-label">今日订单</div>
              <div class="stat-tip">需要OrderService</div>
            </div>
          </el-card>
        </el-col>

        <!-- 待处理事项 -->
        <el-col :span="6" :xs="12">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
              <el-icon :size="32"><Bell /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                <el-tag type="info" size="small">待开发</el-tag>
              </div>
              <div class="stat-label">待处理事项</div>
              <div class="stat-tip">需要后端支持</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 快捷入口 -->
      <el-card class="quick-actions-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Operation /></el-icon>
            <span>快捷操作</span>
          </div>
        </template>
        <el-row :gutter="15">
          <el-col :span="4" :xs="8" v-for="action in quickActions" :key="action.path">
            <div class="quick-action-item" @click="handleQuickAction(action.path)">
              <div class="action-icon" :style="{ background: action.color }">
                <el-icon :size="28"><component :is="action.icon" /></el-icon>
              </div>
              <div class="action-label">{{ action.label }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 活跃拼团列表 -->
      <el-card class="active-teams-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <div>
              <el-icon><TrendCharts /></el-icon>
              <span>进行中的拼团</span>
            </div>
            <el-button text type="primary" @click="router.push('/leader/teams')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
        </template>

        <!-- 加载状态 -->
        <el-skeleton v-if="loadingTeams" :rows="3" animated />

        <!-- 有数据 -->
        <div v-else-if="activeTeams.length > 0" class="teams-list">
          <div class="team-item" v-for="team in activeTeams" :key="team.teamId">
            <div class="team-info">
              <div class="team-header">
                <span class="team-name">{{ team.activityName || '拼团活动' }}</span>
                <el-tag :type="team.teamStatus === 0 ? 'warning' : 'success'" size="small">
                  {{ getTeamStatusText(team.teamStatus) }}
                </el-tag>
              </div>
              <div class="team-meta">
                <span class="team-no">团号：{{ team.teamNo }}</span>
                <span class="team-progress">
                  {{ team.currentNum }}/{{ team.requiredNum }}人
                </span>
              </div>
            </div>
            <div class="team-progress-bar">
              <el-progress 
                :percentage="Math.floor((team.currentNum / team.requiredNum) * 100)" 
                :color="team.currentNum >= team.requiredNum ? '#67c23a' : '#409eff'"
              />
            </div>
            <div class="team-actions">
              <el-button size="small" @click="viewTeamDetail(team.teamId)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>

        <!-- 无数据 -->
        <el-empty v-else description="暂无进行中的拼团" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  Coin,
  UserFilled,
  Document,
  Bell,
  Operation,
  TrendCharts,
  ArrowRight,
  ShoppingCart,
  User,
  Setting,
  DataAnalysis,
  Box
} from '@element-plus/icons-vue'
import { getMyCommissionSummary } from '@/api/leader'
import { getLeaderTeams } from '@/api/groupbuy'

const router = useRouter()
const userStore = useUserStore()

// 数据状态
const loadingCommission = ref(false)
const loadingTeams = ref(false)
const commissionSummary = ref(null)
const activeTeams = ref([])

// 计算活跃拼团数
const activeTeamsCount = computed(() => activeTeams.value.length)

// 快捷操作配置
const quickActions = ref([
  {
    label: '活动管理',
    icon: 'Box',
    path: '/leader/activities',
    color: 'linear-gradient(135deg, #f5af19 0%, #f12711 100%)'
  },
  {
    label: '发起拼团',
    icon: 'ShoppingCart',
    path: '/leader/launch',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    label: '我的团队',
    icon: 'User',
    path: '/leader/teams',
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    label: '佣金管理',
    icon: 'Coin',
    path: '/leader/commission',
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    label: '团点设置',
    icon: 'Setting',
    path: '/leader/store',
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
  },
  {
    label: '订单管理',
    icon: 'Document',
    path: '/leader/orders',
    color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  },
  {
    label: '数据统计',
    icon: 'DataAnalysis',
    path: '/leader/statistics',
    color: 'linear-gradient(135deg, #30cfd0 0%, #330867 100%)'
  }
])

// 获取佣金概览
const fetchCommissionSummary = async () => {
  if (!userStore.userInfo?.userId) return
  
  loadingCommission.value = true
  try {
    const data = await getMyCommissionSummary(userStore.userInfo.userId)
    commissionSummary.value = data
  } catch (error) {
    console.error('获取佣金数据失败:', error)
    commissionSummary.value = null
  } finally {
    loadingCommission.value = false
  }
}

// 获取活跃拼团
const fetchActiveTeams = async () => {
  if (!userStore.userInfo?.userId) return
  
  loadingTeams.value = true
  try {
    const data = await getLeaderTeams({
      leaderId: userStore.userInfo.userId,
      status: 0, // 拼团中
      page: 1,
      limit: 5
    })
    activeTeams.value = data?.list || []
  } catch (error) {
    console.error('获取拼团数据失败:', error)
    activeTeams.value = []
  } finally {
    loadingTeams.value = false
  }
}

// 快捷操作处理
const handleQuickAction = (path) => {
  router.push(path)
}

// 查看团详情
const viewTeamDetail = (teamId) => {
  router.push(`/groupbuy/team/${teamId}`)
}

// 获取团状态文本
const getTeamStatusText = (status) => {
  const statusMap = {
    0: '拼团中',
    1: '已成团',
    2: '已失败'
  }
  return statusMap[status] || '未知'
}

// 页面加载
onMounted(() => {
  if (!userStore.isLeader) {
    ElMessage.warning('请先申请成为团长')
    router.push('/leader/apply')
    return
  }
  
  fetchCommissionSummary()
  fetchActiveTeams()
})
</script>

<style scoped>
.leader-dashboard-wrapper {
  min-height: 100vh;
  padding-top: 84px;
  background-color: #f5f5f5;
}

.leader-dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #909399;
}

/* 数据卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-tip {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}

/* 快捷操作 */
.quick-actions-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
}

.quick-action-item {
  text-align: center;
  padding: 15px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
}

.quick-action-item:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  color: white;
}

.action-label {
  font-size: 14px;
  color: #606266;
}

/* 活跃拼团 */
.active-teams-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.active-teams-card .card-header > div {
  display: flex;
  align-items: center;
  gap: 8px;
}

.teams-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.team-item {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.team-item:hover {
  background-color: #e8ebf0;
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.team-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.team-meta {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
}

.team-progress-bar {
  margin-bottom: 12px;
}

.team-actions {
  display: flex;
  justify-content: flex-end;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .leader-dashboard-wrapper {
    padding-top: 76px;
  }

  .leader-dashboard-container {
    padding: 10px;
  }

  .page-header h2 {
    font-size: 24px;
  }

  .stat-value {
    font-size: 20px;
  }

  .action-icon {
    width: 48px;
    height: 48px;
  }
}
</style>

