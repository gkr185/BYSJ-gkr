<template>
  <MainLayout>
    <div class="profile-container">
      <!-- 用户信息卡片 -->
      <div class="user-card">
        <div class="user-card-bg"></div>
        <div class="user-info-section">
          <div class="avatar-wrapper">
            <el-avatar :size="100" :src="userStore.userInfo?.avatar" class="user-avatar">
              {{ userStore.userInfo?.username?.charAt(0).toUpperCase() }}
            </el-avatar>
            <div class="avatar-badge" v-if="userStore.isLeader">
              <el-icon><Star /></el-icon>
              <span>团长</span>
            </div>
          </div>
          
          <div class="user-details">
            <h2 class="username">{{ userStore.userInfo?.username }}</h2>
            <p class="user-id">ID: {{ userStore.userInfo?.userId }}</p>
            <div class="user-meta">
              <el-tag v-if="userStore.userInfo?.role === 3" type="danger" effect="dark">管理员</el-tag>
              <el-tag v-else-if="userStore.isLeader" type="warning" effect="dark">团长</el-tag>
              <el-tag v-else type="primary" effect="plain">普通用户</el-tag>
              <el-tag v-if="userStore.userInfo?.status === 1" type="success" effect="plain">正常</el-tag>
              <el-tag v-else type="info" effect="plain">已禁用</el-tag>
            </div>
          </div>
          
          <div class="user-actions">
            <el-button type="primary" :icon="Edit" @click="$router.push('/user/info')">
              编辑资料
            </el-button>
          </div>
        </div>
      </div>

      <!-- 统计数据 -->
      <div class="stats-grid">
        <div class="stat-card" @click="$router.push('/user/orders')">
          <div class="stat-icon orders">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.orderCount }}</div>
            <div class="stat-label">我的订单</div>
          </div>
        </div>

        <div class="stat-card" @click="$router.push('/user/groups')">
          <div class="stat-icon groups">
            <el-icon :size="32"><Grid /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.groupCount }}</div>
            <div class="stat-label">我的拼团</div>
          </div>
        </div>

        <div class="stat-card" @click="$router.push('/user/balance')">
          <div class="stat-icon balance">
            <el-icon :size="32"><Wallet /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ accountInfo.balance?.toFixed(2) || '0.00' }}</div>
            <div class="stat-label">账户余额</div>
          </div>
        </div>

        <div class="stat-card" @click="$router.push('/user/address')">
          <div class="stat-icon address">
            <el-icon :size="32"><Location /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.addressCount }}</div>
            <div class="stat-label">收货地址</div>
          </div>
        </div>
      </div>

      <!-- 功能菜单 -->
      <div class="menu-grid">
        <div class="menu-section">
          <h3 class="section-title">
            <el-icon><User /></el-icon>
            账户管理
          </h3>
          <div class="menu-list">
            <div class="menu-item" @click="$router.push('/user/info')">
              <div class="menu-item-icon">
                <el-icon><Edit /></el-icon>
              </div>
              <div class="menu-item-content">
                <div class="menu-item-title">个人资料</div>
                <div class="menu-item-desc">修改个人信息</div>
              </div>
              <el-icon class="menu-item-arrow"><ArrowRight /></el-icon>
            </div>

            <div class="menu-item" @click="$router.push('/user/address')">
              <div class="menu-item-icon">
                <el-icon><Location /></el-icon>
              </div>
              <div class="menu-item-content">
                <div class="menu-item-title">收货地址</div>
                <div class="menu-item-desc">管理收货地址</div>
              </div>
              <el-icon class="menu-item-arrow"><ArrowRight /></el-icon>
            </div>

            <div class="menu-item" @click="$router.push('/user/balance')">
              <div class="menu-item-icon">
                <el-icon><Wallet /></el-icon>
              </div>
              <div class="menu-item-content">
                <div class="menu-item-title">账户余额</div>
                <div class="menu-item-desc">充值、查看余额</div>
              </div>
              <el-icon class="menu-item-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>

        <div class="menu-section">
          <h3 class="section-title">
            <el-icon><ShoppingBag /></el-icon>
            订单服务
          </h3>
          <div class="menu-list">
            <div class="menu-item" @click="$router.push('/user/orders')">
              <div class="menu-item-icon">
                <el-icon><Document /></el-icon>
              </div>
              <div class="menu-item-content">
                <div class="menu-item-title">我的订单</div>
                <div class="menu-item-desc">查看所有订单</div>
              </div>
              <el-icon class="menu-item-arrow"><ArrowRight /></el-icon>
            </div>

            <div class="menu-item" @click="$router.push('/user/groups')">
              <div class="menu-item-icon">
                <el-icon><Grid /></el-icon>
              </div>
              <div class="menu-item-content">
                <div class="menu-item-title">我的拼团</div>
                <div class="menu-item-desc">查看拼团状态</div>
              </div>
              <el-icon class="menu-item-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>

        <div class="menu-section">
          <h3 class="section-title">
            <el-icon><Setting /></el-icon>
            其他服务
          </h3>
          <div class="menu-list">
            <!-- 团长工作台 -->
            <div 
              v-if="userStore.isLeader" 
              class="menu-item leader-dashboard" 
              @click="$router.push('/leader/dashboard')"
            >
              <div class="menu-item-icon dashboard">
                <el-icon><DataBoard /></el-icon>
              </div>
              <div class="menu-item-content">
                <div class="menu-item-title">团长工作台</div>
                <div class="menu-item-desc">管理团点和佣金</div>
              </div>
              <el-icon class="menu-item-arrow"><ArrowRight /></el-icon>
              <el-tag type="success" size="small" effect="dark" class="vip-tag">VIP</el-tag>
            </div>

            <!-- 申请成为团长 -->
            <div 
              v-if="!userStore.isLeader && userStore.userInfo?.role !== 3" 
              class="menu-item leader-apply" 
              @click="$router.push('/leader/apply')"
            >
              <div class="menu-item-icon leader">
                <el-icon><Star /></el-icon>
              </div>
              <div class="menu-item-content">
                <div class="menu-item-title">申请成为团长</div>
                <div class="menu-item-desc">成为团长，赚取佣金</div>
              </div>
              <el-icon class="menu-item-arrow"><ArrowRight /></el-icon>
              <el-tag type="danger" size="small" effect="dark" class="hot-tag">HOT</el-tag>
            </div>

            <div class="menu-item" @click="$router.push('/user/feedback')">
              <div class="menu-item-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="menu-item-content">
                <div class="menu-item-title">意见反馈</div>
                <div class="menu-item-desc">提交问题和建议</div>
              </div>
              <el-icon class="menu-item-arrow"><ArrowRight /></el-icon>
            </div>

            <div class="menu-item" @click="handleLogout">
              <div class="menu-item-icon logout">
                <el-icon><SwitchButton /></el-icon>
              </div>
              <div class="menu-item-content">
                <div class="menu-item-title">退出登录</div>
                <div class="menu-item-desc">退出当前账号</div>
              </div>
              <el-icon class="menu-item-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Edit,
  User,
  Document,
  Grid,
  Wallet,
  Location,
  ArrowRight,
  Star,
  ShoppingBag,
  Setting,
  ChatDotRound,
  SwitchButton,
  DataBoard
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { getAccountInfo, getAddressList } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

// 统计数据
const stats = ref({
  orderCount: 0,
  groupCount: 0,
  addressCount: 0
})

// 账户信息
const accountInfo = ref({
  balance: 0,
  frozenBalance: 0
})

// 加载账户信息
const loadAccountInfo = async () => {
  // 检查用户是否登录
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    return
  }

  try {
    const res = await getAccountInfo(userStore.userInfo.userId)
    if (res.code === 200) {
      accountInfo.value = res.data
    }
  } catch (error) {
    console.error('加载账户信息失败:', error)
  }
}

// 加载地址数量
const loadAddressCount = async () => {
  // 检查用户是否登录
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    return
  }

  try {
    const res = await getAddressList(userStore.userInfo.userId)
    if (res.code === 200) {
      stats.value.addressCount = res.data?.length || 0
    }
  } catch (error) {
    console.error('加载地址数量失败:', error)
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    userStore.logout()
    cartStore.clearCart()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

onMounted(() => {
  // 检查用户是否登录
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  loadAccountInfo()
  loadAddressCount()
  // TODO: 加载订单和拼团统计数据
  stats.value.orderCount = 0
  stats.value.groupCount = 0
})
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* 用户信息卡片 */
.user-card {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.user-card-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 180px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.user-info-section {
  position: relative;
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  margin-top: 140px;
  border-radius: 20px;
}

.avatar-wrapper {
  position: relative;
  flex-shrink: 0;
}

.user-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  font-size: 36px;
  font-weight: 700;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  border: 4px solid #fff;
}

.avatar-badge {
  position: absolute;
  bottom: -5px;
  right: -5px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  background: linear-gradient(135deg, #ffd89b 0%, #19547b 100%);
  color: #fff;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.user-details {
  flex: 1;
}

.username {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.user-id {
  font-size: 14px;
  color: #999;
  margin: 0 0 16px 0;
}

.user-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.user-actions {
  flex-shrink: 0;
}

.user-actions :deep(.el-button) {
  border-radius: 12px;
  padding: 12px 28px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: all 0.3s;
}

.user-actions :deep(.el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

/* 统计数据网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  color: #fff;
  flex-shrink: 0;
}

.stat-icon.orders {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.groups {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.balance {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.address {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

/* 功能菜单网格 */
.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
  gap: 24px;
}

.menu-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
}

.menu-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  transition: all 0.3s;
  cursor: pointer;
}

.menu-item:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  transform: translateX(4px);
}

.menu-item-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
  font-size: 24px;
  flex-shrink: 0;
}

.menu-item-icon.logout {
  background: linear-gradient(135deg, rgba(245, 87, 108, 0.1) 0%, rgba(240, 147, 251, 0.1) 100%);
  color: #f5576c;
}

.menu-item-icon.leader {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.2) 0%, rgba(255, 165, 0, 0.2) 100%);
  color: #ffa500;
}

.menu-item.leader-apply {
  position: relative;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.05) 0%, rgba(255, 165, 0, 0.05) 100%);
  border: 1px dashed #ffd700;
}

.menu-item.leader-apply:hover {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.1) 0%, rgba(255, 165, 0, 0.1) 100%);
  border: 1px dashed #ffa500;
  transform: translateX(8px);
}

.hot-tag,
.vip-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  font-size: 11px;
  padding: 2px 8px;
  font-weight: 700;
}

.menu-item-icon.dashboard {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.2) 0%, rgba(52, 211, 153, 0.2) 100%);
  color: #67c23a;
}

.menu-item.leader-dashboard {
  position: relative;
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.05) 0%, rgba(52, 211, 153, 0.05) 100%);
  border: 1px solid rgba(103, 194, 58, 0.3);
}

.menu-item.leader-dashboard:hover {
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.1) 0%, rgba(52, 211, 153, 0.1) 100%);
  border: 1px solid rgba(103, 194, 58, 0.5);
  transform: translateX(8px);
}

.menu-item-content {
  flex: 1;
}

.menu-item-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.menu-item-desc {
  font-size: 13px;
  color: #999;
}

.menu-item-arrow {
  color: #ccc;
  font-size: 18px;
  transition: all 0.3s;
}

.menu-item:hover .menu-item-arrow {
  color: #667eea;
  transform: translateX(4px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-info-section {
    flex-direction: column;
    text-align: center;
    padding: 24px;
  }

  .user-actions {
    width: 100%;
  }

  .user-actions :deep(.el-button) {
    width: 100%;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .menu-grid {
    grid-template-columns: 1fr;
  }
}
</style>

