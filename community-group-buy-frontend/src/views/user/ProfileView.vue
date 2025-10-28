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

    <!-- 团长专属菜单（v3.0新增）-->
    <div v-if="userStore.isLeader" class="leader-section">
      <el-divider>
        <el-tag type="warning" size="large" effect="dark">
          <el-icon><Star /></el-icon>
          团长管理
        </el-tag>
      </el-divider>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-card class="menu-card leader-menu-card" @click="navigateTo('/leader/dashboard')">
            <div class="leader-icon">
              <el-icon :size="32" color="#409EFF"><DataAnalysis /></el-icon>
            </div>
            <div class="menu-title">团长工作台</div>
            <div class="menu-desc">数据概览和快捷操作</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="menu-card leader-menu-card" @click="navigateTo('/leader/launch')">
            <div class="leader-icon">
              <el-icon :size="32" color="#67C23A"><Plus /></el-icon>
            </div>
            <div class="menu-title">发起拼团</div>
            <div class="menu-desc">选择活动并发起拼团</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="menu-card leader-menu-card" @click="navigateTo('/leader/members')">
            <div class="leader-icon">
              <el-icon :size="32" color="#E6A23C"><UserFilled /></el-icon>
            </div>
            <div class="menu-title">团员管理</div>
            <div class="menu-desc">查看和管理团成员</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="8">
          <el-card class="menu-card leader-menu-card" @click="navigateTo('/leader/delivery')">
            <div class="leader-icon">
              <el-icon :size="32" color="#F56C6C"><Van /></el-icon>
            </div>
            <div class="menu-title">配送管理</div>
            <div class="menu-desc">查看配送订单和路线</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="menu-card leader-menu-card" @click="navigateTo('/leader/commission')">
            <div class="leader-icon">
              <el-icon :size="32" color="#F56C6C"><Money /></el-icon>
            </div>
            <div class="menu-title">我的佣金</div>
            <div class="menu-desc">查看佣金余额和明细</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card class="menu-card leader-menu-card" @click="navigateTo('/leader/community/apply')">
            <div class="leader-icon">
              <el-icon :size="32" color="#909399"><OfficeBuilding /></el-icon>
            </div>
            <div class="menu-title">申请社区</div>
            <div class="menu-desc">申请新的团购社区</div>
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
import { ElMessage } from 'element-plus'
import {
  Star,
  DataAnalysis,
  Plus,
  UserFilled,
  Van,
  Money,
  OfficeBuilding
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const accountBalance = ref('0.00')

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

onMounted(() => {
  if (userStore.isLogin) {
    fetchBalance()
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

/* 团长专属区块（v3.0新增）*/
.leader-section {
  margin-top: 40px;
  padding: 30px;
  background: linear-gradient(135deg, #FFF9E6 0%, #FFE0B2 100%);
  border-radius: 12px;
}

.leader-section :deep(.el-divider__text) {
  background: transparent;
}

.leader-menu-card {
  background: white;
  border: 2px solid transparent;
  transition: all 0.3s;
}

.leader-menu-card:hover {
  border-color: #F57C00;
  box-shadow: 0 6px 16px rgba(245, 124, 0, 0.2);
  transform: translateY(-3px);
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
