<template>
  <div class="member-manage">
    <!-- 顶部导航 -->
    <TopNav />
    
    <div class="manage-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1>团员管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/leader/dashboard' }">工作台</el-breadcrumb-item>
          <el-breadcrumb-item>团员管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <!-- 团列表 -->
      <el-card>
        <template #header>
          <div class="card-header">
            <span class="card-title">
              <el-icon><UserFilled /></el-icon>
              我发起的团
            </span>
            <el-radio-group v-model="teamFilter" @change="filterTeams">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="joining">拼团中</el-radio-button>
              <el-radio-button value="success">已成团</el-radio-button>
              <el-radio-button value="failed">已失败</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        
        <el-table 
          v-if="filteredTeams.length" 
          :data="filteredTeams" 
          stripe
          style="width: 100%"
          @row-click="viewMembers"
          class="team-table"
        >
          <el-table-column prop="teamNo" label="团号" width="150" />
          <el-table-column prop="activityName" label="活动名称" min-width="200" />
          <el-table-column label="进度" width="150" align="center">
            <template #default="{ row }">
              <el-progress 
                :percentage="Math.round((row.currentNum / row.requiredNum) * 100)" 
                :status="row.teamStatus === 1 ? 'success' : undefined"
              >
                <span class="progress-text">{{ row.currentNum }}/{{ row.requiredNum }}</span>
              </el-progress>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.teamStatus)" size="large">
                {{ getStatusText(row.teamStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="发起时间" width="180" />
          <el-table-column label="操作" width="150" fixed="right" align="center">
            <template #default="{ row }">
              <el-button 
                size="small" 
                type="primary"
                link
                @click.stop="viewMembers(row)"
              >
                <el-icon><View /></el-icon>
                查看成员
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty 
          v-else 
          description="暂无数据" 
          :image-size="100"
        />
      </el-card>
      
      <!-- 成员列表抽屉 -->
      <el-drawer 
        v-model="memberDrawerVisible" 
        :title="`团员列表 - ${currentTeam?.teamNo}`"
        size="50%"
        direction="rtl"
      >
        <div class="drawer-content">
          <!-- 团信息卡片 -->
          <el-card class="team-info-card" shadow="never">
            <div class="team-summary">
              <div class="summary-item">
                <span class="label">活动名称：</span>
                <span class="value">{{ currentTeam?.activityName }}</span>
              </div>
              <div class="summary-item">
                <span class="label">成团要求：</span>
                <span class="value">{{ currentTeam?.requiredNum }}人</span>
              </div>
              <div class="summary-item">
                <span class="label">当前人数：</span>
                <span class="value">{{ currentTeam?.currentNum }}人</span>
              </div>
              <div class="summary-item">
                <span class="label">团状态：</span>
                <el-tag :type="getStatusType(currentTeam?.teamStatus)">
                  {{ getStatusText(currentTeam?.teamStatus) }}
                </el-tag>
              </div>
            </div>
          </el-card>
          
          <!-- 成员列表 -->
          <el-card class="members-card">
            <template #header>
              <span class="card-title">
                <el-icon><User /></el-icon>
                成员详情（{{ members.length }}人）
              </span>
            </template>
            
            <div v-if="members.length" class="members-list">
              <div 
                v-for="member in members" 
                :key="member.memberId"
                class="member-item"
              >
                <div class="member-avatar">
                  <el-avatar 
                    :size="50" 
                    :src="member.avatar || '/images/avatar-default.png'"
                  >
                    {{ member.username?.charAt(0) }}
                  </el-avatar>
                </div>
                <div class="member-info">
                  <div class="info-row">
                    <span class="member-name">{{ member.username }}</span>
                    <el-tag v-if="member.isLauncher" type="warning" size="small">
                      <el-icon><Star /></el-icon>
                      发起人
                    </el-tag>
                  </div>
                  <div class="info-row">
                    <span class="member-phone">{{ member.phone }}</span>
                  </div>
                  <div class="info-row">
                    <span class="join-time">参团时间：{{ member.joinTime }}</span>
                  </div>
                </div>
                <div class="member-status">
                  <el-tag :type="member.status >= 1 ? 'success' : 'info'" size="large">
                    {{ member.status >= 1 ? '已支付' : '待支付' }}
                  </el-tag>
                  <div class="order-id">订单：{{ member.orderId }}</div>
                </div>
              </div>
            </div>
            
            <el-empty v-else description="暂无成员" :image-size="80" />
          </el-card>
        </div>
      </el-drawer>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLeaderStore } from '@/stores/leader'
import TopNav from '@/components/common/TopNav.vue'
import { ElMessage } from 'element-plus'
import {
  UserFilled,
  View,
  User,
  Star
} from '@element-plus/icons-vue'

const router = useRouter()
const leaderStore = useLeaderStore()

const teams = ref([])
const teamFilter = ref('all')
const memberDrawerVisible = ref(false)
const currentTeam = ref(null)
const members = ref([])

// 过滤后的团列表
const filteredTeams = computed(() => {
  if (teamFilter.value === 'all') return teams.value
  if (teamFilter.value === 'joining') return teams.value.filter(t => t.teamStatus === 0)
  if (teamFilter.value === 'success') return teams.value.filter(t => t.teamStatus === 1)
  if (teamFilter.value === 'failed') return teams.value.filter(t => t.teamStatus === 2)
  return teams.value
})

// 加载团列表
onMounted(async () => {
  try {
    teams.value = await leaderStore.fetchTeams(true)
  } catch (error) {
    console.error('加载团列表失败', error)
  }
})

// 筛选团
const filterTeams = () => {
  // 筛选逻辑已通过computed实现
}

// 查看成员
const viewMembers = async (row) => {
  try {
    currentTeam.value = row
    members.value = await leaderStore.fetchMembers(row.teamId)
    memberDrawerVisible.value = true
  } catch (error) {
    console.error('加载成员失败', error)
    ElMessage.error('加载成员失败')
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',  // 拼团中
    1: 'success',  // 已成团
    2: 'info'      // 已失败
  }
  return typeMap[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '拼团中',
    1: '已成团',
    2: '已失败'
  }
  return textMap[status] || '未知'
}
</script>

<style scoped>
.manage-container {
  max-width: 1400px;
  margin: 80px auto 20px;
  padding: 20px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
}

/* 卡片头部 */
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

/* 表格 */
.team-table :deep(.el-table__row) {
  cursor: pointer;
}

.team-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

.progress-text {
  font-size: 12px;
  color: #606266;
}

/* 抽屉内容 */
.drawer-content {
  padding: 0 20px 20px;
}

.team-info-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
}

.team-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-item .label {
  font-size: 14px;
  color: #606266;
}

.summary-item .value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.members-card {
  margin-top: 20px;
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
  border: 1px solid #EBEEF5;
  border-radius: 8px;
  transition: all 0.3s;
}

.member-item:hover {
  background: #f5f7fa;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.member-avatar {
  flex-shrink: 0;
}

.member-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.member-phone {
  font-size: 14px;
  color: #909399;
}

.join-time {
  font-size: 13px;
  color: #909399;
}

.member-status {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.order-id {
  font-size: 12px;
  color: #909399;
}

/* 空状态 */
:deep(.el-empty) {
  padding: 60px 0;
}
</style>
