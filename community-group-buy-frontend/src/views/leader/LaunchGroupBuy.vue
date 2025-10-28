<template>
  <div class="launch-groupbuy">
    <!-- 顶部导航 -->
    <TopNav />
    
    <div class="launch-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1>发起拼团</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/leader/dashboard' }">工作台</el-breadcrumb-item>
          <el-breadcrumb-item>发起拼团</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <!-- 步骤条 -->
      <el-steps :active="currentStep" align-center finish-status="success" class="steps-bar">
        <el-step title="选择活动" icon="Goods" />
        <el-step title="设置团信息" icon="Setting" />
        <el-step title="完成发起" icon="SuccessFilled" />
      </el-steps>
      
      <!-- 步骤1：选择活动 -->
      <div v-show="currentStep === 0" class="step-content">
        <el-card>
          <template #header>
            <span class="card-title">
              <el-icon><ShoppingBag /></el-icon>
              选择拼团活动
            </span>
          </template>
          
          <el-row v-if="activities.length" :gutter="20">
            <el-col 
              v-for="activity in activities" 
              :key="activity.activityId" 
              :span="8"
            >
              <el-card 
                shadow="hover" 
                class="activity-card"
                :class="{ selected: selectedActivity?.activityId === activity.activityId }"
                @click="selectActivity(activity)"
              >
                <img 
                  :src="activity.productImg || '/images/placeholder.png'" 
                  :alt="activity.activityName"
                  class="product-img"
                  @error="handleImgError"
                />
                <h3 class="activity-name">{{ activity.activityName }}</h3>
                <div class="price-info">
                  <span class="group-price">¥{{ activity.groupPrice }}</span>
                  <span class="original-price">原价：¥{{ activity.price }}</span>
                </div>
                <div class="activity-info">
                  <el-tag size="small" type="warning">
                    {{ activity.requiredNum }}人成团
                  </el-tag>
                  <el-tag size="small" type="info">
                    {{ activity.timeLimit }}小时内成团
                  </el-tag>
                </div>
                <div class="stock-info">
                  <span>库存：{{ activity.stock }}</span>
                  <span>已售：{{ activity.sales }}</span>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <el-empty v-else description="暂无拼团活动" />
          
          <div class="step-actions">
            <el-button @click="router.back()">返回</el-button>
            <el-button 
              type="primary" 
              :disabled="!selectedActivity"
              @click="nextStep"
            >
              下一步
            </el-button>
          </div>
        </el-card>
      </div>
      
      <!-- 步骤2：设置团信息 -->
      <div v-show="currentStep === 1" class="step-content">
        <el-card>
          <template #header>
            <span class="card-title">
              <el-icon><Setting /></el-icon>
              设置团信息
            </span>
          </template>
          
          <el-form 
            :model="teamForm" 
            :rules="teamRules"
            ref="teamFormRef"
            label-width="120px"
            class="team-form"
          >
            <el-form-item label="活动">
              <div class="activity-preview">
                <img 
                  :src="selectedActivity?.productImg || '/images/placeholder.png'" 
                  @error="handleImgError"
                />
                <div class="preview-info">
                  <h4>{{ selectedActivity?.activityName }}</h4>
                  <p>拼团价：¥{{ selectedActivity?.groupPrice }} | {{ selectedActivity?.requiredNum }}人成团</p>
                </div>
              </div>
            </el-form-item>
            
            <el-form-item label="我的社区">
              <el-text size="large">
                <el-icon><OfficeBuilding /></el-icon>
                {{ leaderStore.leaderInfo?.communityName || userStore.userInfo?.communityName || '未设置' }}
              </el-text>
              <el-text type="info" size="small" style="display: block; margin-top: 8px;">
                自动关联到您的社区，社区内用户将优先看到此团
              </el-text>
            </el-form-item>
            
            <el-form-item label="团长参团">
              <el-switch 
                v-model="teamForm.joinTeam"
                active-text="参加"
                inactive-text="不参加"
              />
              <el-text type="info" size="small" style="display: block; margin-top: 8px;">
                建议团长参团，提升成团成功率
              </el-text>
            </el-form-item>
            
            <el-form-item label="活动说明">
              <el-input 
                v-model="teamForm.description"
                type="textarea"
                :rows="3"
                placeholder="可选填写活动说明，如商品特点、配送时间等"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-form>
          
          <div class="step-actions">
            <el-button @click="prevStep">上一步</el-button>
            <el-button 
              type="primary" 
              :loading="submitting"
              @click="submitLaunch"
            >
              {{ submitting ? '发起中...' : '发起拼团' }}
            </el-button>
          </div>
        </el-card>
      </div>
      
      <!-- 步骤3：完成 -->
      <div v-show="currentStep === 2" class="step-content">
        <el-card>
          <el-result 
            icon="success" 
            title="拼团发起成功"
          >
            <template #sub-title>
              <div class="success-info">
                <p>团号：<strong>{{ launchResult?.teamNo }}</strong></p>
                <p>快速分享链接给好友参团吧</p>
              </div>
            </template>
            <template #extra>
              <div class="share-section">
                <el-input 
                  v-model="shareLink" 
                  readonly 
                  class="share-input"
                >
                  <template #append>
                    <el-button @click="copyLink">
                      <el-icon><DocumentCopy /></el-icon>
                      复制链接
                    </el-button>
                  </template>
                </el-input>
                
                <div class="action-buttons">
                  <el-button @click="viewTeam" type="primary" size="large">
                    <el-icon><View /></el-icon>
                    查看团详情
                  </el-button>
                  <el-button @click="launchAgain" size="large">
                    <el-icon><Plus /></el-icon>
                    继续发起
                  </el-button>
                  <el-button @click="backToDashboard" size="large">
                    <el-icon><Odometer /></el-icon>
                    返回工作台
                  </el-button>
                </div>
              </div>
            </template>
          </el-result>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useLeaderStore } from '@/stores/leader'
import TopNav from '@/components/common/TopNav.vue'
import { ElMessage } from 'element-plus'
import {
  ShoppingBag,
  Setting,
  OfficeBuilding,
  DocumentCopy,
  View,
  Plus,
  Odometer
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const leaderStore = useLeaderStore()

const currentStep = ref(0)
const activities = ref([])
const selectedActivity = ref(null)
const submitting = ref(false)
const launchResult = ref(null)
const shareLink = ref('')

const teamForm = ref({
  joinTeam: true,
  description: ''
})

const teamFormRef = ref(null)

const teamRules = {
  // 表单验证规则（可选）
}

// 加载活动列表
onMounted(async () => {
  try {
    // 加载团长信息
    if (!leaderStore.leaderInfo) {
      await leaderStore.fetchLeaderInfo()
    }
    
    // 加载活动列表
    activities.value = await leaderStore.fetchActivities()
  } catch (error) {
    console.error('加载活动列表失败', error)
  }
})

// 选择活动
const selectActivity = (activity) => {
  selectedActivity.value = activity
}

// 下一步
const nextStep = () => {
  if (!selectedActivity.value) {
    ElMessage.warning('请先选择活动')
    return
  }
  currentStep.value = 1
}

// 上一步
const prevStep = () => {
  currentStep.value = 0
}

// 提交发起
const submitLaunch = async () => {
  try {
    submitting.value = true
    
    const result = await leaderStore.launchTeam({
      activityId: selectedActivity.value.activityId,
      joinTeam: teamForm.value.joinTeam,
      description: teamForm.value.description
    })
    
    launchResult.value = result
    shareLink.value = result.shareLink || `${window.location.origin}/groupbuy/team/${result.teamId}`
    
    currentStep.value = 2
  } catch (error) {
    console.error('发起失败', error)
  } finally {
    submitting.value = false
  }
}

// 复制链接
const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(shareLink.value)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 查看团详情
const viewTeam = () => {
  if (launchResult.value?.teamId) {
    router.push(`/groupbuy/team/${launchResult.value.teamId}`)
  }
}

// 继续发起
const launchAgain = () => {
  // 重置表单
  currentStep.value = 0
  selectedActivity.value = null
  teamForm.value = {
    joinTeam: true,
    description: ''
  }
  launchResult.value = null
  shareLink.value = ''
}

// 返回工作台
const backToDashboard = () => {
  router.push('/leader/dashboard')
}

// 图片加载失败处理
const handleImgError = (e) => {
  e.target.src = '/images/placeholder.png'
}
</script>

<style scoped>
.launch-container {
  max-width: 1200px;
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

/* 步骤条 */
.steps-bar {
  margin-bottom: 40px;
  padding: 20px;
  background: white;
  border-radius: 8px;
}

/* 步骤内容 */
.step-content {
  min-height: 400px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

/* 活动卡片 */
.activity-card {
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.activity-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.activity-card.selected {
  border: 2px solid #409EFF;
}

.product-img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
  margin-bottom: 12px;
}

.activity-name {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-info {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 12px;
}

.group-price {
  font-size: 24px;
  font-weight: 600;
  color: #F56C6C;
}

.original-price {
  font-size: 14px;
  color: #909399;
  text-decoration: line-through;
}

.activity-info {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.stock-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #909399;
}

/* 团信息表单 */
.team-form {
  max-width: 600px;
  margin: 0 auto;
}

.activity-preview {
  display: flex;
  gap: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.activity-preview img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.preview-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.preview-info p {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

/* 步骤按钮 */
.step-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #EBEEF5;
}

/* 成功页面 */
.success-info {
  margin: 20px 0;
}

.success-info p {
  margin: 10px 0;
  font-size: 16px;
  color: #606266;
}

.success-info strong {
  font-size: 20px;
  color: #409EFF;
}

.share-section {
  max-width: 600px;
  margin: 0 auto;
}

.share-input {
  margin-bottom: 30px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

/* 空状态 */
:deep(.el-empty) {
  padding: 60px 0;
}
</style>
