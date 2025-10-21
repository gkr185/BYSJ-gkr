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
  </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAccountInfo } from '@/api/user'
import { ElMessage } from 'element-plus'

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

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-page-wrapper {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }
}
</style>
