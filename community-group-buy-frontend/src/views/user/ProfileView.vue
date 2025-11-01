<template>
  <div class="profile-page-wrapper">
    <div class="profile-page">
    <h2>个人中心</h2>
    
    <!-- 用户信息卡片 -->
    <el-card style="margin-bottom: 20px;">
      <div class="user-info">
        <div>
          <div class="info-item">
            <span class="label">用户名：</span>
            <span>{{ userStore.userInfo?.username || '未登录' }}</span>
          </div>
          <div class="info-item">
            <span class="label">真实姓名：</span>
            <span>{{ userStore.userInfo?.realName || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">手机号：</span>
            <span>{{ userStore.userInfo?.phone || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">账户余额：</span>
            <span style="color: #f56c6c; font-weight: bold;">¥{{ accountBalance }}</span>
          </div>
        </div>
        <div v-if="!userStore.isLogin">
          <el-button type="primary" @click="router.push('/login')">登录</el-button>
        </div>
      </div>
    </el-card>

    <!-- 功能菜单 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="menu-card" @click="navigateTo('/user/info')">
          <div class="menu-title">个人信息</div>
          <div class="menu-desc">查看和编辑个人信息</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="menu-card" @click="navigateTo('/user/address')">
          <div class="menu-title">收货地址</div>
          <div class="menu-desc">管理我的收货地址</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="menu-card" @click="navigateTo('/user/balance')">
          <div class="menu-title">我的余额</div>
          <div class="menu-desc">查看账户余额和交易记录</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="menu-card" @click="navigateTo('/user/orders')">
          <div class="menu-title">我的订单</div>
          <div class="menu-desc">查看订单列表和详情</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="menu-card" @click="navigateTo('/user/groups')">
          <div class="menu-title">我的拼团</div>
          <div class="menu-desc">查看参与的拼团活动</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="menu-card" @click="navigateTo('/user/feedback')">
          <div class="menu-title">意见反馈</div>
          <div class="menu-desc">提交问题和建议</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 申请成为团长（普通用户可见）-->
    <el-card v-if="!userStore.isLeader" class="apply-leader-card" @click="navigateTo('/leader/apply')">
      <div class="apply-content">
        <div class="apply-icon">
          <el-icon :size="48" color="#F57C00"><Star /></el-icon>
        </div>
        <div class="apply-text">
          <h3>成为团长，开启收益之旅</h3>
          <p>发起拼团活动，赚取订单佣金，服务社区居民</p>
        </div>
        <el-button type="warning" size="large">
          立即申请
          <el-icon class="el-icon--right"><Right /></el-icon>
        </el-button>
      </div>
    </el-card>

    <!-- 我的申请状态（普通用户可见）-->
    <div v-if="!userStore.isLeader" style="margin-top: 20px;">
      <el-card>
        <template #header>
          <div style="display: flex; align-items: center; gap: 8px;">
            <el-icon><Document /></el-icon>
            <span>我的申请</span>
            <el-button 
              type="text" 
              size="small" 
              @click.stop="fetchApplicationStatus" 
              style="margin-left: auto;"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>

        <!-- 社区申请状态 -->
        <div v-if="communityApplication" style="margin-bottom: 15px;">
          <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 8px;">
            <el-tag type="info" size="small">社区申请</el-tag>
            <el-tag :type="getStatusType(communityApplication.status)" size="small">
              {{ getStatusText(communityApplication.status) }}
            </el-tag>
          </div>
          <div style="color: #606266; font-size: 14px;">
            <p style="margin: 5px 0;">社区名称：{{ communityApplication.communityName }}</p>
            <p style="margin: 5px 0;">申请时间：{{ formatDate(communityApplication.createdAt) }}</p>
            <p v-if="communityApplication.reviewComment" style="margin: 5px 0; color: #909399;">
              审核意见：{{ communityApplication.reviewComment }}
            </p>
          </div>
        </div>

        <!-- 团长申请状态 -->
        <div v-if="leaderApplication">
          <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 8px;">
            <el-tag type="warning" size="small">团长申请</el-tag>
            <el-tag :type="getLeaderStatusType(leaderApplication.status)" size="small">
              {{ getLeaderStatusText(leaderApplication.status) }}
            </el-tag>
          </div>
          <div style="color: #606266; font-size: 14px;">
            <p style="margin: 5px 0;">团点名称：{{ leaderApplication.storeName }}</p>
            <p style="margin: 5px 0;">所属社区：{{ leaderApplication.communityName || '待分配' }}</p>
            <p style="margin: 5px 0;">申请时间：{{ formatDate(leaderApplication.createdAt) }}</p>
            <p v-if="leaderApplication.reviewComment" style="margin: 5px 0; color: #909399;">
              审核意见：{{ leaderApplication.reviewComment }}
            </p>
          </div>
        </div>

        <!-- 无申请记录 -->
        <el-empty 
          v-if="!communityApplication && !leaderApplication && !loadingApplications"
          description="暂无申请记录"
          :image-size="80"
        />

        <!-- 加载中 -->
        <div v-if="loadingApplications" style="text-align: center; padding: 20px;">
          <el-icon class="is-loading" :size="30"><Loading /></el-icon>
          <p style="margin-top: 10px; color: #909399;">加载中...</p>
        </div>
      </el-card>
    </div>

    <!-- 团长专属菜单（v3.0优化版）-->
    <div v-if="userStore.isLeader" class="leader-section">
      <el-divider>
        <el-tag type="warning" size="large" effect="dark">
          <el-icon><Star /></el-icon>
          团长管理中心
        </el-tag>
      </el-divider>

      <!-- 团长工作台入口（大卡片）-->
      <el-card class="dashboard-card" @click="navigateTo('/leader/dashboard')">
        <div class="dashboard-content">
          <div class="dashboard-icon">
            <el-icon :size="56"><DataBoard /></el-icon>
          </div>
          <div class="dashboard-text">
            <h3>团长工作台</h3>
            <p>数据概览、快捷操作、团队管理一站式服务</p>
          </div>
          <el-button type="warning" size="large">
            进入工作台
            <el-icon class="el-icon--right"><Right /></el-icon>
          </el-button>
        </div>
      </el-card>

      <!-- 团长功能菜单 -->
      <el-row :gutter="15" style="margin-top: 20px;">
        <el-col :span="6" :xs="12">
          <el-card class="menu-card leader-menu-card" @click="navigateTo('/leader/launch')">
            <div class="leader-icon">
              <el-icon :size="28" color="#67C23A"><Plus /></el-icon>
            </div>
            <div class="menu-title">发起拼团</div>
            <div class="menu-desc">选择活动并发起</div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="menu-card leader-menu-card" @click="navigateTo('/leader/teams')">
            <div class="leader-icon">
              <el-icon :size="28" color="#409EFF"><User /></el-icon>
            </div>
            <div class="menu-title">我的团队</div>
            <div class="menu-desc">查看拼团管理</div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="menu-card leader-menu-card" @click="navigateTo('/leader/commission')">
            <div class="leader-icon">
              <el-icon :size="28" color="#F56C6C"><Money /></el-icon>
            </div>
            <div class="menu-title">佣金管理</div>
            <div class="menu-desc">查看收益明细</div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="menu-card leader-menu-card" @click="navigateTo('/leader/store')">
            <div class="leader-icon">
              <el-icon :size="28" color="#E6A23C"><Setting /></el-icon>
            </div>
            <div class="menu-title">团点设置</div>
            <div class="menu-desc">管理团点信息</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="15" style="margin-top: 15px;">
        <el-col :span="6" :xs="12">
          <el-card class="menu-card leader-menu-card placeholder-card" @click="navigateTo('/leader/orders')">
            <div class="leader-icon">
              <el-icon :size="28" color="#909399"><Document /></el-icon>
            </div>
            <div class="menu-title">订单管理</div>
            <div class="menu-desc">
              <el-tag type="info" size="small">待开发</el-tag>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="menu-card leader-menu-card placeholder-card" @click="navigateTo('/leader/delivery')">
            <div class="leader-icon">
              <el-icon :size="28" color="#909399"><Van /></el-icon>
            </div>
            <div class="menu-title">配送管理</div>
            <div class="menu-desc">
              <el-tag type="info" size="small">待开发</el-tag>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="menu-card leader-menu-card placeholder-card" @click="navigateTo('/leader/statistics')">
            <div class="leader-icon">
              <el-icon :size="28" color="#909399"><DataAnalysis /></el-icon>
            </div>
            <div class="menu-title">数据统计</div>
            <div class="menu-desc">
              <el-tag type="info" size="small">待开发</el-tag>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="menu-card leader-menu-card" @click="ElMessage.info('更多功能开发中...')">
            <div class="leader-icon">
              <el-icon :size="28" color="#C0C4CC"><MoreFilled /></el-icon>
            </div>
            <div class="menu-title">更多功能</div>
            <div class="menu-desc">敬请期待</div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAccountInfo } from '@/api/user'
import { getMyCommunityApplications, getMyLeaderInfo } from '@/api/leader'
import { ElMessage } from 'element-plus'
import {
  Star,
  Plus,
  Money,
  User,
  Setting,
  Van,
  DataAnalysis,
  DataBoard,
  MoreFilled,
  Right,
  Document,
  Refresh,
  Loading
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const accountBalance = ref('0.00')
const communityApplication = ref(null)
const leaderApplication = ref(null)
const loadingApplications = ref(false)

// 获取账户余额
const fetchBalance = async () => {
  if (!userStore.userInfo?.userId) return
  
  try {
    const data = await getAccountInfo(userStore.userInfo.userId)
    accountBalance.value = data.balance
  } catch (error) {
    console.error('Failed to fetch balance:', error)
  }
}

// 页面跳转
const navigateTo = (path) => {
  if (!userStore.isLogin && path !== '/login') {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push(path)
}

// 获取申请状态
const fetchApplicationStatus = async () => {
  if (!userStore.userInfo?.userId) return
  
  loadingApplications.value = true
  try {
    // 获取社区申请记录
    const communityRes = await getMyCommunityApplications(userStore.userInfo.userId)
    if (communityRes.code === 200 && communityRes.data && communityRes.data.length > 0) {
      // 取最新的申请记录（假设返回的是数组）
      communityApplication.value = communityRes.data[0]
    } else {
      communityApplication.value = null
    }
    
    // 获取团长申请记录
    const leaderRes = await getMyLeaderInfo(userStore.userInfo.userId)
    if (leaderRes.code === 200 && leaderRes.data) {
      leaderApplication.value = leaderRes.data
      
      // 如果团长申请已通过（status=1），刷新用户信息以更新角色
      if (leaderRes.data.status === 1) {
        await userStore.updateUserInfo()
      }
    } else {
      leaderApplication.value = null
    }
  } catch (error) {
    console.error('获取申请状态失败:', error)
    // 发生错误时清空数据，避免显示旧数据
    communityApplication.value = null
    leaderApplication.value = null
  } finally {
    loadingApplications.value = false
  }
}

// 社区申请状态辅助函数
const getStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[status] || '未知'
}

// 团长申请状态辅助函数
const getLeaderStatusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const getLeaderStatusText = (status) => {
  const map = { 0: '待审核', 1: '正常运营', 2: '已拒绝/已停用' }
  return map[status] || '未知'
}

// 日期格式化
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(async () => {
  if (userStore.isLogin) {
    // 刷新用户信息（确保角色是最新的）
    await userStore.updateUserInfo()
    
    // 获取账户余额和申请状态
    fetchBalance()
    fetchApplicationStatus()
  }
})
</script>

<style scoped>
.profile-page-wrapper {
  min-height: 100vh;
  padding-top: 84px; /* 64px导航栏 + 20px间隔 */
  background-color: #f5f5f5;
}

.profile-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 20px 20px;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}

.user-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item {
  margin-bottom: 10px;
  font-size: 14px;
}

.label {
  color: #909399;
  margin-right: 10px;
}

.menu-card {
  cursor: pointer;
  transition: all 0.3s;
}

.menu-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.menu-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 8px;
  color: #333;
}

.menu-desc {
  font-size: 13px;
  color: #909399;
}

/* 申请成为团长卡片 */
.apply-leader-card {
  margin-top: 30px;
  cursor: pointer;
  transition: all 0.3s;
  background: linear-gradient(135deg, #FFF9E6 0%, #FFE0B2 100%);
  border: 2px solid #F57C00;
}

.apply-leader-card:hover {
  box-shadow: 0 8px 20px rgba(245, 124, 0, 0.3);
  transform: translateY(-2px);
}

.apply-content {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 10px;
}

.apply-icon {
  flex-shrink: 0;
}

.apply-text {
  flex: 1;
}

.apply-text h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #333;
}

.apply-text p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

/* 团长专属区块（v3.0优化版）*/
.leader-section {
  margin-top: 40px;
  padding: 30px;
  background: linear-gradient(135deg, #FFF9E6 0%, #FFE0B2 100%);
  border-radius: 12px;
}

.leader-section :deep(.el-divider__text) {
  background: transparent;
}

/* 团长工作台大卡片 */
.dashboard-card {
  cursor: pointer;
  transition: all 0.3s;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  margin-bottom: 20px;
}

.dashboard-card:hover {
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  transform: translateY(-4px);
}

.dashboard-card :deep(.el-card__body) {
  padding: 25px;
}

.dashboard-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.dashboard-icon {
  flex-shrink: 0;
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.dashboard-text {
  flex: 1;
  color: white;
}

.dashboard-text h3 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: white;
}

.dashboard-text p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

/* 团长功能菜单卡片 */
.leader-menu-card {
  background: white;
  border: 2px solid transparent;
  transition: all 0.3s;
  text-align: center;
}

.leader-menu-card:hover {
  border-color: #F57C00;
  box-shadow: 0 6px 16px rgba(245, 124, 0, 0.2);
  transform: translateY(-3px);
}

.leader-menu-card.placeholder-card {
  opacity: 0.7;
}

.leader-menu-card.placeholder-card:hover {
  border-color: #909399;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.leader-icon {
  margin-bottom: 12px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-page-wrapper {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }
}
</style>
