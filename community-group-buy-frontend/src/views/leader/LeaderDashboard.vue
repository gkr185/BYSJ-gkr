<template>
  <div class="leader-dashboard">
    <!-- 顶部导航 -->
    <TopNav />
    
    <div class="dashboard-container">
      <!-- 欢迎卡片 -->
      <el-card class="welcome-card" shadow="never">
        <div class="welcome-content">
          <div class="welcome-text">
            <h2>欢迎，{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</h2>
            <p class="subtitle">
              <el-icon><OfficeBuilding /></el-icon>
              {{ leaderStore.leaderInfo?.storeName || '加载中...' }} · 
              {{ leaderStore.leaderInfo?.communityName || '加载中...' }}
            </p>
          </div>
          <div class="welcome-badge">
            <el-tag type="warning" size="large" effect="dark">
              <el-icon><Star /></el-icon>
              团长身份
            </el-tag>
          </div>
        </div>
      </el-card>
      
      <!-- 数据统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card stat-card-primary">
            <el-statistic 
              title="新订单" 
              :value="dashboard?.todayOrders?.newOrders || 0" 
            >
              <template #prefix>
                <el-icon :size="20" color="#409EFF"><ShoppingCart /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card stat-card-warning">
            <el-statistic 
              title="待发货" 
              :value="dashboard?.todayOrders?.toDeliver || 0"
            >
              <template #prefix>
                <el-icon :size="20" color="#E6A23C"><Box /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card stat-card-success">
            <el-statistic 
              title="配送中" 
              :value="dashboard?.todayOrders?.delivering || 0"
            >
              <template #prefix>
                <el-icon :size="20" color="#67C23A"><Van /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card stat-card-danger">
            <el-statistic 
              title="今日佣金" 
              :value="dashboard?.todayOrders?.todayCommission || 0"
              :precision="2"
              prefix="¥"
            >
              <template #prefix>
                <el-icon :size="20" color="#F56C6C"><Money /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 活跃拼团 -->
      <el-card class="section-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">
              <el-icon><UserFilled /></el-icon>
              活跃拼团
            </span>
            <el-button type="primary" size="small" @click="goToLaunch">
              <el-icon><Plus /></el-icon>
              发起新团
            </el-button>
          </div>
        </template>
        
        <el-table 
          v-if="dashboard?.activeTeams?.length" 
          :data="dashboard.activeTeams" 
          stripe 
          style="width: 100%"
        >
          <el-table-column prop="activityName" label="活动名称" min-width="200" />
          <el-table-column prop="teamNo" label="团号" width="150" />
          <el-table-column label="进度" width="120" align="center">
            <template #default="{ row }">
              <el-tag type="info" size="large">
                {{ row.currentNum }}/{{ row.requiredNum }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getTeamStatusType(row.teamStatus)">
                {{ getTeamStatusText(row.teamStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remainingTime" label="剩余时间" width="150" />
          <el-table-column prop="createTime" label="发起时间" width="180" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button 
                size="small" 
                type="primary"
                link
                @click="viewTeam(row.teamId)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty 
          v-else 
          description="暂无活跃拼团" 
          :image-size="100"
        >
          <el-button type="primary" @click="goToLaunch">
            发起第一个团
          </el-button>
        </el-empty>
      </el-card>
      
      <!-- 待处理事项 -->
      <el-card class="section-card">
        <template #header>
          <span class="card-title">
            <el-icon><Bell /></el-icon>
            待处理事项
          </span>
        </template>
        
        <el-timeline v-if="dashboard?.pendingTasks?.length">
          <el-timeline-item 
            v-for="(task, index) in dashboard.pendingTasks" 
            :key="index"
            type="primary"
            hollow
          >
            <div class="task-item">
              <el-icon><Warning /></el-icon>
              <span>{{ task }}</span>
            </div>
          </el-timeline-item>
        </el-timeline>
        
        <el-empty 
          v-else 
          description="暂无待处理事项" 
          :image-size="80"
        />
      </el-card>
      
      <!-- 快捷操作 -->
      <el-card class="section-card">
        <template #header>
          <span class="card-title">
            <el-icon><Grid /></el-icon>
            快捷操作
          </span>
        </template>
        
        <el-row :gutter="20" class="quick-actions">
          <el-col :span="4">
            <div class="action-item" @click="router.push('/leader/launch')">
              <el-icon :size="32" color="#409EFF"><Plus /></el-icon>
              <span>发起拼团</span>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="action-item" @click="router.push('/leader/members')">
              <el-icon :size="32" color="#67C23A"><User /></el-icon>
              <span>团员管理</span>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="action-item" @click="router.push('/leader/delivery')">
              <el-icon :size="32" color="#E6A23C"><Van /></el-icon>
              <span>配送管理</span>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="action-item" @click="router.push('/leader/commission')">
              <el-icon :size="32" color="#F56C6C"><Money /></el-icon>
              <span>我的佣金</span>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="action-item" @click="router.push('/leader/community/apply')">
              <el-icon :size="32" color="#909399"><OfficeBuilding /></el-icon>
              <span>申请社区</span>
            </div>
          </el-col>
          <el-col :span="4">
            <div class="action-item" @click="router.push('/user/orders')">
              <el-icon :size="32" color="#606266"><Document /></el-icon>
              <span>订单管理</span>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useLeaderStore } from '@/stores/leader'
import TopNav from '@/components/common/TopNav.vue'
import { 
  ShoppingCart, 
  Box, 
  Van, 
  Money,
  UserFilled,
  Plus,
  Bell,
  Warning,
  Grid,
  User,
  Document,
  OfficeBuilding,
  Star
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const leaderStore = useLeaderStore()

const dashboard = ref(null)

// 加载数据
onMounted(async () => {
  try {
    // 加载团长信息
    if (!leaderStore.leaderInfo) {
      await leaderStore.fetchLeaderInfo()
    }
    
    // 加载工作台数据
    dashboard.value = await leaderStore.fetchDashboard()
  } catch (error) {
    console.error('加载工作台数据失败', error)
  }
})

// 去发起拼团
const goToLaunch = () => {
  router.push('/leader/launch')
}

// 查看团详情
const viewTeam = (teamId) => {
  router.push(`/groupbuy/team/${teamId}`)
}

// 获取团状态类型
const getTeamStatusType = (status) => {
  const typeMap = {
    0: 'warning',  // 拼团中
    1: 'success',  // 已成团
    2: 'info'      // 已失败
  }
  return typeMap[status] || ''
}

// 获取团状态文本
const getTeamStatusText = (status) => {
  const textMap = {
    0: '拼团中',
    1: '已成团',
    2: '已失败'
  }
  return textMap[status] || '未知'
}
</script>

<style scoped>
.dashboard-container {
  max-width: 1400px;
  margin: 80px auto 20px;
  padding: 20px;
}

/* 欢迎卡片 */
.welcome-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.welcome-card :deep(.el-card__body) {
  padding: 30px;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  opacity: 0.9;
}

.welcome-badge {
  font-size: 16px;
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-card :deep(.el-statistic__head) {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-card :deep(.el-statistic__content) {
  font-size: 28px;
  font-weight: 600;
}

/* 区块卡片 */
.section-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

/* 任务列表 */
.task-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

/* 快捷操作 */
.quick-actions {
  padding: 10px 0;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 30px 20px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
  background: #f5f7fa;
}

.action-item:hover {
  background: #ecf5ff;
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.action-item span {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* 空状态 */
:deep(.el-empty__description) {
  margin-top: 12px;
}

/* 表格优化 */
:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background: #f5f7fa;
  font-weight: 600;
}
</style>
