<template>
  <div class="my-teams-wrapper">
    <div class="my-teams-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>我的团队</h2>
        <p class="subtitle">管理您发起的所有拼团</p>
      </div>

      <!-- 筛选器 -->
      <el-card class="filter-card" shadow="never">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="团状态">
            <el-select v-model="filterForm.status" placeholder="全部状态" @change="handleFilter">
              <el-option label="全部" :value="null" />
              <el-option label="拼团中" :value="0" />
              <el-option label="已成团" :value="1" />
              <el-option label="已失败" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleFilter">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 团列表 -->
      <div class="teams-content">
        <!-- 加载状态 -->
        <el-skeleton v-if="loading" :rows="5" animated />

        <!-- 有数据 -->
        <div v-else-if="teams.length > 0" class="teams-list">
          <el-card 
            v-for="team in teams" 
            :key="team.teamId"
            class="team-card"
            shadow="hover"
          >
            <div class="team-header">
              <div class="team-title">
                <h3>{{ team.activityName || '拼团活动' }}</h3>
                <el-tag :type="getStatusTagType(team.teamStatus)" size="small">
                  {{ getStatusText(team.teamStatus) }}
                </el-tag>
              </div>
              <div class="team-no">团号：{{ team.teamNo }}</div>
            </div>

            <el-divider />

            <div class="team-body">
              <div class="team-stats">
                <div class="stat-item">
                  <span class="label">成团人数：</span>
                  <span class="value">{{ team.requiredNum }}人</span>
                </div>
                <div class="stat-item">
                  <span class="label">当前人数：</span>
                  <span class="value highlight">{{ team.currentNum }}人</span>
                </div>
                <div class="stat-item">
                  <span class="label">创建时间：</span>
                  <span class="value">{{ formatDateTime(team.createTime) }}</span>
                </div>
                <div class="stat-item" v-if="team.teamStatus === 0">
                  <span class="label">过期时间：</span>
                  <span class="value warning">{{ formatDateTime(team.expireTime) }}</span>
                </div>
                <div class="stat-item" v-if="team.teamStatus === 1">
                  <span class="label">成团时间：</span>
                  <span class="value success">{{ formatDateTime(team.successTime) }}</span>
                </div>
              </div>

              <!-- 团进度 -->
              <div class="team-progress" v-if="team.teamStatus === 0">
                <el-progress 
                  :percentage="Math.floor((team.currentNum / team.requiredNum) * 100)" 
                  :color="team.currentNum >= team.requiredNum ? '#67c23a' : '#409eff'"
                  :status="team.currentNum >= team.requiredNum ? 'success' : null"
                >
                  <template #default="{ percentage }">
                    <span class="progress-text">{{ percentage }}%</span>
                  </template>
                </el-progress>
                <div class="progress-tip">
                  还差 <span class="highlight">{{ team.requiredNum - team.currentNum }}</span> 人成团
                </div>
              </div>

              <!-- 成员列表预览 -->
              <div class="team-members" v-if="team.members && team.members.length > 0">
                <div class="members-title">参团成员 ({{ team.members.length }})</div>
                <div class="members-list">
                  <div 
                    v-for="member in team.members.slice(0, 5)" 
                    :key="member.userId"
                    class="member-item"
                  >
                    <el-avatar :size="32">{{ member.userName?.charAt(0) || 'U' }}</el-avatar>
                    <span class="member-name">{{ member.userName }}</span>
                    <el-tag v-if="member.isLauncher === 1" type="danger" size="small">发起人</el-tag>
                    <el-tag :type="getMemberStatusTagType(member.status)" size="small">
                      {{ getMemberStatusText(member.status) }}
                    </el-tag>
                  </div>
                  <div v-if="team.members.length > 5" class="more-members">
                    还有 {{ team.members.length - 5 }} 人...
                  </div>
                </div>
              </div>
            </div>

            <el-divider />

            <div class="team-actions">
              <el-button size="small" @click="viewTeamDetail(team.teamId)">
                查看详情
              </el-button>
              <el-button 
                size="small" 
                type="primary" 
                v-if="team.teamStatus === 0"
                @click="copyShareLink(team.teamId)"
              >
                复制分享链接
              </el-button>
            </div>
          </el-card>
        </div>

        <!-- 无数据 -->
        <el-empty v-else description="暂无团队数据">
          <el-button type="primary" @click="router.push('/leader/launch')">
            去发起拼团
          </el-button>
        </el-empty>

        <!-- 分页 -->
        <el-pagination
          v-if="total > 0"
          :current-page="filterForm.page"
          :page-size="filterForm.limit"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="val => { filterForm.limit = val; handleFilter() }"
          @current-change="val => { filterForm.page = val; handleFilter() }"
          style="margin-top: 20px; justify-content: center;"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getLeaderTeams } from '@/api/groupbuy'

const router = useRouter()
const userStore = useUserStore()

// 数据状态
const loading = ref(false)
const teams = ref([])
const total = ref(0)

// 筛选表单
const filterForm = ref({
  status: null,
  page: 1,
  limit: 10
})

// 获取团列表
const fetchTeams = async () => {
  if (!userStore.userInfo?.userId) return
  
  loading.value = true
  try {
    const params = {
      leaderId: userStore.userInfo.userId,
      status: filterForm.value.status,
      page: filterForm.value.page,
      limit: filterForm.value.limit
    }
    
    const data = await getLeaderTeams(params)
    teams.value = data?.list || []
    total.value = data?.total || 0
  } catch (error) {
    console.error('获取团列表失败:', error)
    ElMessage.error('获取团列表失败')
    teams.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 筛选处理
const handleFilter = () => {
  filterForm.value.page = 1
  fetchTeams()
}

// 重置筛选
const handleReset = () => {
  filterForm.value = {
    status: null,
    page: 1,
    limit: 10
  }
  fetchTeams()
}

// 查看详情
const viewTeamDetail = (teamId) => {
  router.push(`/groupbuy/team/${teamId}`)
}

// 复制分享链接
const copyShareLink = (teamId) => {
  const link = `${window.location.origin}/groupbuy/team/${teamId}`
  
  if (navigator.clipboard) {
    navigator.clipboard.writeText(link).then(() => {
      ElMessage.success('分享链接已复制')
    }).catch(() => {
      fallbackCopy(link)
    })
  } else {
    fallbackCopy(link)
  }
}

// 降级复制方法
const fallbackCopy = (text) => {
  const textarea = document.createElement('textarea')
  textarea.value = text
  document.body.appendChild(textarea)
  textarea.select()
  try {
    document.execCommand('copy')
    ElMessage.success('分享链接已复制')
  } catch (err) {
    ElMessage.error('复制失败，请手动复制')
  }
  document.body.removeChild(textarea)
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || 'info'
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

// 获取成员状态标签类型
const getMemberStatusTagType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取成员状态文本
const getMemberStatusText = (status) => {
  const textMap = {
    0: '待支付',
    1: '已支付',
    2: '已成团',
    3: '已取消'
  }
  return textMap[status] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 页面加载
onMounted(() => {
  if (!userStore.isLeader) {
    ElMessage.warning('仅团长可访问')
    router.push('/leader/apply')
    return
  }
  
  fetchTeams()
})
</script>

<style scoped>
.my-teams-wrapper {
  min-height: 100vh;
  padding-top: 84px;
  background-color: #f5f5f5;
}

.my-teams-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
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

.filter-card {
  margin-bottom: 20px;
}

.teams-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.team-card {
  transition: all 0.3s;
}

.team-card:hover {
  transform: translateY(-2px);
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.team-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.team-title h3 {
  font-size: 18px;
  color: #333;
  margin: 0;
}

.team-no {
  font-size: 14px;
  color: #909399;
}

.team-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.stat-item {
  font-size: 14px;
}

.stat-item .label {
  color: #909399;
}

.stat-item .value {
  color: #333;
  font-weight: 500;
}

.stat-item .value.highlight {
  color: #409eff;
  font-weight: bold;
}

.stat-item .value.warning {
  color: #e6a23c;
}

.stat-item .value.success {
  color: #67c23a;
}

.team-progress {
  margin-bottom: 20px;
}

.progress-text {
  font-size: 12px;
}

.progress-tip {
  text-align: center;
  font-size: 13px;
  color: #606266;
  margin-top: 8px;
}

.progress-tip .highlight {
  color: #f56c6c;
  font-weight: bold;
  font-size: 16px;
}

.team-members {
  margin-top: 15px;
}

.members-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  font-weight: 500;
}

.members-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.member-name {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.more-members {
  text-align: center;
  font-size: 13px;
  color: #909399;
  padding: 8px;
}

.team-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-teams-wrapper {
    padding-top: 76px;
  }

  .my-teams-container {
    padding: 10px;
  }

  .team-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .team-stats {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .team-actions {
    flex-direction: column;
  }

  .team-actions .el-button {
    width: 100%;
  }
}
</style>

