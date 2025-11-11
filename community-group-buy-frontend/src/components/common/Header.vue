<template>
  <header class="app-header">
    <!-- 顶部栏 -->
    <div class="header-container">
      <!-- Logo -->
      <div class="header-logo" @click="$router.push('/')">
        <el-icon :size="32" color="#409EFF">
          <ShoppingBag />
        </el-icon>
        <span class="logo-text">社区团购</span>
      </div>

      <!-- 搜索框 -->
      <div class="header-search">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>

      <!-- 右侧操作区 -->
      <div class="header-actions">
        <!-- 购物车 -->
        <div class="cart-icon" @click="$router.push('/cart')">
          <el-badge :value="cartStore.totalCount" :hidden="cartStore.totalCount === 0">
            <el-icon :size="24"><ShoppingCart /></el-icon>
          </el-badge>
          <span class="action-text">购物车</span>
        </div>

        <!-- 用户菜单 -->
        <el-dropdown v-if="userStore.isLogin" class="user-menu" @command="handleUserCommand">
          <div class="user-trigger">
            <el-avatar :size="32" :src="userStore.userInfo?.avatar">
              {{ userStore.userInfo?.username?.charAt(0).toUpperCase() }}
            </el-avatar>
            <span class="username">{{ userStore.userInfo?.username }}</span>
            <el-icon><User /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item command="orders">
                <el-icon><Document /></el-icon>
                我的订单
              </el-dropdown-item>
              <el-dropdown-item command="groups">
                <el-icon><Grid /></el-icon>
                我的拼团
              </el-dropdown-item>
              <el-dropdown-item v-if="userStore.isLeader" command="leader" divided>
                <el-icon><Star /></el-icon>
                团长中心
              </el-dropdown-item>
              <el-dropdown-item command="switchUser" divided v-if="isDevelopment">
                <el-icon><Refresh /></el-icon>
                切换测试用户
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 登录/注册按钮 -->
        <div v-else class="login-actions">
          <el-button type="primary" @click="$router.push('/login')">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </div>
      </div>
    </div>

    <!-- 导航菜单 -->
    <div class="header-nav">
      <div class="nav-container">
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :ellipsis="false"
          background-color="#409EFF"
          text-color="#fff"
          active-text-color="#fff"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            首页
          </el-menu-item>
          <el-menu-item index="/products">
            <el-icon><ShoppingBag /></el-icon>
            商品列表
          </el-menu-item>
          <el-menu-item index="/groupbuy">
            <el-icon><Grid /></el-icon>
            拼团活动
          </el-menu-item>
          <el-menu-item v-if="userStore.isLogin" index="/profile">
            <el-icon><User /></el-icon>
            个人中心
          </el-menu-item>
        </el-menu>
      </div>
    </div>

    <!-- 切换测试用户对话框 -->
    <SwitchUserDialog
      v-model="showSwitchUserDialog"
      @switched="handleUserSwitched"
    />
  </header>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  ShoppingCart,
  User,
  Document,
  Grid,
  Star,
  SwitchButton,
  HomeFilled,
  ShoppingBag,
  Refresh
} from '@element-plus/icons-vue'
import SwitchUserDialog from './SwitchUserDialog.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const showSwitchUserDialog = ref(false)

// 是否为开发环境
const isDevelopment = computed(() => {
  return import.meta.env.DEV || import.meta.env.MODE === 'development'
})

// 当前激活的菜单
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/' || path === '/home') return '/'
  if (path.startsWith('/products')) return '/products'
  if (path.startsWith('/groupbuy')) return '/groupbuy'
  if (path.startsWith('/profile') || path.startsWith('/user')) return '/profile'
  return path
})

// 搜索商品
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  router.push({
    path: '/products',
    query: { keyword: searchKeyword.value }
  })
}

// 菜单选择
const handleMenuSelect = (index) => {
  router.push(index)
}

// 用户菜单命令
const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'orders':
      router.push('/user/orders')
      break
    case 'groups':
      router.push('/user/groups')
      break
    case 'leader':
      router.push('/profile')
      break
    case 'switchUser':
      showSwitchUserDialog.value = true
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning'
      }).then(() => {
        userStore.logout()
        cartStore.clearCart()
        ElMessage.success('已退出登录')
        router.push('/')
      }).catch(() => {})
      break
  }
}

// 用户切换成功
const handleUserSwitched = () => {
  // 切换用户的购物车数据
  cartStore.switchUser()
  // 刷新当前页面
  router.go(0)
}
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 40px;
}

/* Logo */
.header-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
  padding: 8px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.header-logo:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.logo-img {
  width: 36px;
  height: 36px;
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff 0%, #f0f0f0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.5px;
}

/* 搜索框 */
.header-search {
  flex: 1;
  max-width: 600px;
}

.header-search :deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 24px 0 0 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  border: 2px solid transparent;
}

.header-search :deep(.el-input__wrapper:hover) {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  border-color: rgba(255, 255, 255, 0.5);
}

.header-search :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  border-color: #fff;
}

.header-search :deep(.el-input-group__append) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  border-radius: 0 24px 24px 0;
  padding: 0 20px;
}

.header-search :deep(.el-input-group__append .el-button) {
  color: #fff;
  font-weight: 600;
  background: transparent;
  border: none;
}

.header-search :deep(.el-input-group__append .el-button:hover) {
  background: rgba(255, 255, 255, 0.1);
}

/* 右侧操作区 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}

/* 购物车图标 */
.cart-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 8px 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
  position: relative;
}

.cart-icon:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.cart-icon :deep(.el-badge__content) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: 2px solid #fff;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.action-text {
  font-size: 12px;
  margin-top: 4px;
  font-weight: 500;
}

/* 用户菜单 */
.user-menu {
  cursor: pointer;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
  border-radius: 24px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
}

.user-trigger:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.user-trigger :deep(.el-avatar) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.username {
  font-size: 14px;
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 登录/注册按钮 */
.login-actions {
  display: flex;
  gap: 12px;
}

.login-actions :deep(.el-button) {
  border-radius: 20px;
  padding: 10px 24px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.login-actions :deep(.el-button--primary) {
  background: rgba(255, 255, 255, 0.95);
  color: #667eea;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.login-actions :deep(.el-button--primary:hover) {
  background: #fff;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

.login-actions :deep(.el-button--default) {
  background: transparent;
  color: #fff;
}

.login-actions :deep(.el-button--default:hover) {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

/* 导航菜单 */
.header-nav {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.95) 0%, rgba(118, 75, 162, 0.95) 100%);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
}

.header-nav :deep(.el-menu) {
  border-bottom: none;
  background: transparent;
}

.header-nav :deep(.el-menu-item) {
  font-size: 15px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 8px;
  margin: 0px 8px;
  position: relative;
}

.header-nav :deep(.el-menu-item::before) {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%) scaleX(0);
  width: 60%;
  height: 3px;
  background: linear-gradient(90deg, #f093fb 0%, #f5576c 100%);
  border-radius: 3px;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.header-nav :deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.header-nav :deep(.el-menu-item:hover::before) {
  transform: translateX(-50%) scaleX(1);
}

.header-nav :deep(.el-menu-item.is-active) {
  background-color: rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.header-nav :deep(.el-menu-item.is-active::before) {
  transform: translateX(-50%) scaleX(1);
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu) {
  border-radius: 12px;
  padding: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(0, 0, 0, 0.05);
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
}

:deep(.el-dropdown-menu__item) {
  border-radius: 8px;
  margin: 4px 0;
  transition: all 0.3s;
  padding: 10px 16px;
}

:deep(.el-dropdown-menu__item:hover) {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  transform: translateX(4px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-container {
    padding: 12px 16px;
    gap: 16px;
  }
  
  .header-search {
    max-width: 100%;
  }
  
  .action-text {
    display: none;
  }
  
  .username {
    display: none;
  }
  
  .logo-text {
    font-size: 18px;
  }
}
</style>

