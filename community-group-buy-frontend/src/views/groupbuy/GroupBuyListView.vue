<template>
  <MainLayout>
    <div class="groupbuy-list-page">
      <div class="container">
        <h1 class="page-title">拼团活动</h1>

        <!-- 加载状态 -->
        <el-skeleton v-if="loading" :rows="8" animated />

        <!-- 活动列表 -->
        <div v-else-if="activities.length > 0">
          <div class="activities-grid">
            <el-card 
              v-for="activity in activities" 
              :key="activity.activityId"
              class="activity-card"
              shadow="hover"
              @click="viewActivity(activity.activityId)"
            >
              <div class="activity-content">
                <div class="activity-header">
                  <h3 class="activity-name">活动{{ activity.activityId }}</h3>
                  <el-tag :type="getStatusType(activity.status)" size="large">
                    {{ getStatusText(activity.status) }}
                  </el-tag>
                </div>
                
                <div class="activity-info">
                  <div class="info-item">
                    <span class="label">拼团价：</span>
                    <span class="price">¥{{ activity.groupPrice }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">成团人数：</span>
                    <span class="value">{{ activity.requiredNum }}人</span>
                  </div>
                  <div class="info-item">
                    <span class="label">活动时间：</span>
                    <span class="time">{{ formatDate(activity.startTime) }} 至 {{ formatDate(activity.endTime) }}</span>
                  </div>
                </div>

                <!-- 团列表预览 -->
                <div v-if="activityTeams[activity.activityId]" class="teams-preview">
                  <div class="teams-header">
                    <span>进行中的团（{{ activityTeams[activity.activityId].length }}）</span>
                    <el-button 
                      link 
                      type="primary"
                      @click.stop="showActivityTeams(activity.activityId)"
                    >
                      查看全部
                    </el-button>
                  </div>
                  <div class="teams-list">
                    <div 
                      v-for="team in activityTeams[activity.activityId].slice(0, 3)" 
                      :key="team.teamId"
                      class="team-item"
                      @click.stop="viewTeam(team.teamId)"
                    >
                      <div class="team-info">
                        <span class="team-no">团号：{{ team.teamNo }}</span>
                        <el-tag 
                          v-if="team.communityId && team.communityId === userCommunityId"
                          type="success" 
                          size="small"
                        >
                          本社区
                        </el-tag>
                      </div>
                      <div class="team-progress">
                        <span class="progress-text">{{ team.currentNum }}/{{ team.requiredNum }}人</span>
                        <el-progress 
                          :percentage="(team.currentNum / team.requiredNum) * 100" 
                          :show-text="false"
                          :stroke-width="6"
                        />
                      </div>
                    </div>
                  </div>
                </div>

                <div class="activity-actions">
                  <el-button 
                    type="primary" 
                    @click.stop="viewActivity(activity.activityId)"
                  >
                    查看活动
                  </el-button>
                  <el-button 
                    v-if="userStore.isLeader"
                    type="success"
                    @click.stop="launchNewTeam(activity.activityId)"
                  >
                    发起拼团
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 空状态 -->
        <el-card v-else class="placeholder-card">
          <div class="placeholder-content">
            <el-icon :size="80" color="#67C23A"><Grid /></el-icon>
            <h2>暂无拼团活动</h2>
            <p>当前没有进行中的拼团活动，敬请期待！</p>
            
            <div class="feature-preview">
              <h3>功能预览：</h3>
              <div class="preview-grid">
                <div class="preview-card">
                  <div class="preview-icon">🎯</div>
                  <h4>活动浏览</h4>
                  <p>查看团长发起的所有拼团活动</p>
                </div>
                <div class="preview-card">
                  <div class="preview-icon">👥</div>
                  <h4>参与拼团</h4>
                  <p>一键加入感兴趣的拼团</p>
                </div>
                <div class="preview-card">
                  <div class="preview-icon">⏰</div>
                  <h4>实时状态</h4>
                  <p>查看拼团进度和倒计时</p>
                </div>
                <div class="preview-card">
                  <div class="preview-icon">📍</div>
                  <h4>社区定位</h4>
                  <p>推荐附近社区的拼团活动</p>
                </div>
              </div>
            </div>

            <div class="notice-box">
              <h4>温馨提示：</h4>
              <p>🌟 根据v3.0拼团逻辑，只有团长可以发起拼团活动</p>
              <p>🎁 拼团成功后商品以更优惠的价格配送到团长团点</p>
              <p>🔄 拼团失败会自动退款到您的账户余额</p>
            </div>

            <div class="action-buttons">
              <el-button type="primary" @click="router.push('/products')">
                浏览商品
              </el-button>
              <el-button type="success" @click="router.push('/user/groups')">
                我的拼团
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 团列表对话框 -->
      <el-dialog
        v-model="teamsDialogVisible"
        title="团列表"
        width="800px"
        top="5vh"
      >
        <div v-if="selectedActivity" class="dialog-content">
          <div class="dialog-header">
            <h3>{{ selectedActivity.activityName }}</h3>
            <p>拼团价：<span class="price">¥{{ selectedActivity.groupPrice }}</span></p>
          </div>
          
          <div v-if="dialogTeamsList.length > 0" class="dialog-teams-list">
            <div 
              v-for="team in dialogTeamsList" 
              :key="team.teamId"
              class="dialog-team-item"
              @click="viewTeam(team.teamId)"
            >
              <div class="team-main">
                <div class="team-left">
                  <div class="team-no">{{ team.teamNo }}</div>
                  <el-tag 
                    v-if="team.communityId === userCommunityId"
                    type="success" 
                    size="small"
                  >
                    本社区
                  </el-tag>
                  <el-tag 
                    v-if="team.communityName"
                    size="small"
                  >
                    {{ team.communityName }}
                  </el-tag>
                </div>
                <div class="team-right">
                  <el-button type="primary" size="small">
                    查看详情
                  </el-button>
                </div>
              </div>
              <div class="team-progress-detail">
                <span class="progress-label">进度：{{ team.currentNum }}/{{ team.requiredNum }}人</span>
                <el-progress 
                  :percentage="(team.currentNum / team.requiredNum) * 100"
                  :stroke-width="8"
                />
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无进行中的团" />
        </div>
      </el-dialog>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Grid } from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { getOngoingActivities, getActivityTeams } from '@/api/groupbuy'
import { useUserStore } from '@/stores/user'
import { formatDate as formatDateUtil } from '@/utils/formatter'

const router = useRouter()
const userStore = useUserStore()

// 数据
const loading = ref(true)
const activities = ref([])
const activityTeams = ref({}) // 存储每个活动的团列表
const teamsDialogVisible = ref(false)
const selectedActivity = ref(null)
const dialogTeamsList = ref([])

// 用户社区ID（用于社区优先显示）
const userCommunityId = computed(() => {
  return userStore.userInfo?.communityId || null
})

// 方法
const fetchActivities = async () => {
  loading.value = true
  try {
    const data = await getOngoingActivities()
    activities.value = data || []
    
    // 为每个活动获取团列表（只获取前几个作为预览）
    for (const activity of activities.value) {
      await fetchActivityTeamsPreview(activity.activityId)
    }
  } catch (error) {
    console.error('获取活动列表失败:', error)
    ElMessage.error('获取活动列表失败')
  } finally {
    loading.value = false
  }
}

const fetchActivityTeamsPreview = async (activityId) => {
  try {
    const params = userCommunityId.value ? { communityId: userCommunityId.value } : {}
    const data = await getActivityTeams(activityId, params)
    activityTeams.value[activityId] = data || []
  } catch (error) {
    console.error(`获取活动${activityId}的团列表失败:`, error)
  }
}

const showActivityTeams = async (activityId) => {
  try {
    selectedActivity.value = activities.value.find(a => a.activityId === activityId)
    const params = userCommunityId.value ? { communityId: userCommunityId.value } : {}
    dialogTeamsList.value = await getActivityTeams(activityId, params)
    teamsDialogVisible.value = true
  } catch (error) {
    console.error('获取团列表失败:', error)
    ElMessage.error('获取团列表失败')
  }
}

const viewActivity = (activityId) => {
  // 跳转到活动详情（目前跳转到团列表对话框）
  showActivityTeams(activityId)
}

const viewTeam = (teamId) => {
  router.push(`/groupbuy/team/${teamId}`)
}

const launchNewTeam = (activityId) => {
  if (!userStore.isLeader) {
    ElMessage.warning('仅团长可发起拼团')
    return
  }
  router.push(`/leader/launch/${activityId}`)
}

const getStatusType = (status) => {
  const types = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    0: '未开始',
    1: '进行中',
    2: '已结束',
    3: '异常'
  }
  return texts[status] || '未知'
}

const formatDate = (dateStr) => {
  return formatDateUtil(dateStr)
}

// 生命周期
onMounted(() => {
  fetchActivities()
})
</script>

<style scoped>
.groupbuy-list-page {
  min-height: 100vh;
  padding: 20px 0;
  background: #f5f7fa;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #303133;
}

/* 活动网格 */
.activities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.activity-card {
  cursor: pointer;
  transition: all 0.3s;
}

.activity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.activity-content {
  padding: 8px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #EBEEF5;
}

.activity-name {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.info-item .label {
  color: #909399;
  margin-right: 8px;
}

.info-item .price {
  font-size: 24px;
  font-weight: bold;
  color: #F56C6C;
}

.info-item .value {
  font-weight: 500;
  color: #409EFF;
}

.info-item .time {
  color: #606266;
  font-size: 13px;
}

/* 团列表预览 */
.teams-preview {
  margin: 16px 0;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.teams-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.teams-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.team-item {
  padding: 12px;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.team-item:hover {
  background: #ecf5ff;
  transform: translateX(4px);
}

.team-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.team-no {
  font-size: 13px;
  color: #606266;
}

.team-progress {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-text {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

/* 活动操作按钮 */
.activity-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #EBEEF5;
}

.activity-actions .el-button {
  flex: 1;
}

/* 占位状态 */
.placeholder-card {
  min-height: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-content {
  text-align: center;
  max-width: 900px;
  padding: 40px;
}

.placeholder-content h2 {
  font-size: 28px;
  color: #303133;
  margin: 24px 0 16px;
}

.placeholder-content > p {
  font-size: 16px;
  color: #909399;
  margin-bottom: 40px;
}

.feature-preview {
  margin-bottom: 40px;
}

.feature-preview h3 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 24px;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.preview-card {
  padding: 24px;
  background-color: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.preview-card:hover {
  background-color: #ecf5ff;
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.preview-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.preview-card h4 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
}

.preview-card p {
  font-size: 14px;
  color: #909399;
  line-height: 1.5;
}

.notice-box {
  text-align: left;
  margin-bottom: 40px;
  padding: 24px;
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  border-radius: 8px;
  border-left: 4px solid #4CAF50;
}

.notice-box h4 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
  font-weight: bold;
}

.notice-box p {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  line-height: 1.6;
}

.notice-box p:last-child {
  margin-bottom: 0;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.action-buttons .el-button {
  min-width: 140px;
}

/* 对话框样式 */
.dialog-content {
  max-height: 60vh;
  overflow-y: auto;
}

.dialog-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #EBEEF5;
}

.dialog-header h3 {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 8px 0;
}

.dialog-header p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.dialog-header .price {
  font-size: 20px;
  font-weight: bold;
  color: #F56C6C;
}

.dialog-teams-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dialog-team-item {
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.dialog-team-item:hover {
  background: #ecf5ff;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.team-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.team-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.team-no {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.team-progress-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-label {
  font-size: 13px;
  color: #909399;
}

/* 响应式 */
@media (max-width: 1024px) {
  .activities-grid {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  }
}

@media (max-width: 768px) {
  .activities-grid {
    grid-template-columns: 1fr;
  }

  .preview-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }

  .activity-actions {
    flex-direction: column;
  }

  .team-main {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>

