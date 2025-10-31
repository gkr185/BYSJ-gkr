<template>
  <header class="app-header">
    <div class="header-container">
      <!-- Logo -->
      <div class="header-logo" @click="router.push('/')">
        <img src="@/assets/logo.svg" alt="社区团购" class="logo-img" />
        <span class="logo-text">社区团购</span>
      </div>

      <!-- 搜索框 -->
      <div class="header-search">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品"
          @keyup.enter="handleSearch"
          clearable
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>

      <!-- 右侧操作区 -->
      <div class="header-actions">
        <!-- 购物车 -->
        <div class="cart-icon" @click="router.push('/cart')">
          <el-badge :value="cartStore.totalCount" :max="99" :hidden="cartStore.totalCount === 0">
            <el-icon :size="24"><ShoppingCart /></el-icon>
          </el-badge>
          <span class="action-text">购物车</span>
        </div>

        <!-- 用户信息 -->
        <div v-if="userStore.isLogin" class="user-menu">
          <el-dropdown @command="handleUserCommand">
            <div class="user-trigger">
              <el-avatar :size="32">{{ userStore.userInfo?.username?.[0] || 'U' }}</el-avatar>
              <span class="username">{{ userStore.userInfo?.username }}</span>
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
                  团长工作台
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 未登录状态 -->
        <div v-else class="login-btn">
          <el-button type="primary" @click="router.push('/login')">登录</el-button>
        </div>
      </div>
    </div>

    <!-- 导航菜单 -->
    <nav class="header-nav">
      <div class="nav-container">
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          :ellipsis="false"
          background-color="#409EFF"
          text-color="#fff"
          active-text-color="#FFD700"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/products">
            <el-icon><ShoppingBag /></el-icon>
            <span>商品分类</span>
          </el-menu-item>
          <el-menu-item index="/groupbuy">
            <el-icon><Grid /></el-icon>
            <span>拼团活动</span>
          </el-menu-item>
          <el-menu-item v-if="userStore.isLogin" index="/profile">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
        </el-menu>
      </div>
    </nav>
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
  ShoppingBag
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')

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
</script>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 40px;
}

/* Logo */
.header-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
}

.logo-img {
  width: 36px;
  height: 36px;
}

.logo-text {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

/* 搜索框 */
.header-search {
  flex: 1;
  max-width: 600px;
}

.header-search :deep(.el-input-group__append) {
  background-color: #409EFF;
  border-color: #409EFF;
}

.header-search :deep(.el-input-group__append .el-button) {
  color: #fff;
}

/* 右侧操作区 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-shrink: 0;
}

/* 购物车图标 */
.cart-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  padding: 4px 8px;
}

.cart-icon:hover {
  color: #409EFF;
  transform: translateY(-2px);
}

.action-text {
  font-size: 12px;
  margin-top: 4px;
}

/* 用户菜单 */
.user-menu {
  cursor: pointer;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 20px;
  transition: all 0.3s;
}

.user-trigger:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #333;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 登录按钮 */
.login-btn {
  flex-shrink: 0;
}

/* 导航菜单 */
.header-nav {
  background-color: #409EFF;
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
}

.header-nav :deep(.el-menu) {
  border-bottom: none;
}

.header-nav :deep(.el-menu-item) {
  font-size: 15px;
  font-weight: 500;
}

.header-nav :deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.1);
}

.header-nav :deep(.el-menu-item.is-active) {
  background-color: rgba(255, 255, 255, 0.15);
}
</style>

