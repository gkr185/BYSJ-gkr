<template>
  <header class="top-nav" v-if="show">
    <div class="nav-content">
      <div class="nav-left">
        <div class="logo-section" @click="router.push('/home')">
          <el-icon :size="28" class="logo-icon">
            <ShoppingBag />
          </el-icon>
          <span class="site-name">社区团购</span>
        </div>
        <nav class="nav-menu">
          <div 
            :class="['menu-item', { active: isActive('home') }]"
            @click="navigateTo('/home')"
          >
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </div>
          <div 
            :class="['menu-item', { active: isActive('products') }]"
            @click="navigateTo('/products')"
          >
            <el-icon><Goods /></el-icon>
            <span>商品</span>
          </div>
          <div 
            :class="['menu-item', { active: isActive('orders') }]"
            @click="navigateTo('/user/orders')"
          >
            <el-icon><Document /></el-icon>
            <span>订单</span>
          </div>
        </nav>
      </div>
      <div class="nav-right">
        <el-badge :value="cartCount" :hidden="cartCount === 0" :max="99">
          <el-button :icon="ShoppingCart" @click="router.push('/cart')">
            购物车
          </el-button>
        </el-badge>
        
        <!-- 用户菜单 -->
        <el-dropdown v-if="userStore.isLogin" trigger="click" @command="handleUserCommand">
          <el-button :icon="User">
            {{ userStore.userInfo?.username || '用户' }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
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
              <el-dropdown-item command="groupbuy">
                <el-icon><UserFilled /></el-icon>
                我的拼团
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 未登录显示登录按钮 -->
        <el-button v-else :icon="User" @click="router.push('/login')">
          登录
        </el-button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ShoppingBag, HomeFilled, Goods, Document,
  ShoppingCart, User, ArrowDown, SwitchButton, UserFilled
} from '@element-plus/icons-vue'
import { getCartCount } from '@/utils/cart'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const props = defineProps({
  show: {
    type: Boolean,
    default: true
  }
})

const router = useRouter()
const route = useRoute()

const cartCount = ref(0)

// 判断菜单项是否激活
const isActive = (routeName) => {
  const currentPath = route.path
  
  if (routeName === 'home') {
    return currentPath === '/home' || currentPath === '/'
  } else if (routeName === 'products') {
    // ⭐更新：包含新的拼团路由
    return currentPath.startsWith('/products') || currentPath.startsWith('/groupbuy')
  } else if (routeName === 'orders') {
    return currentPath.startsWith('/user/orders') || currentPath === '/order/confirm'
  }
  
  return false
}

// 更新购物车数量
const updateCartCount = () => {
  cartCount.value = getCartCount()
}

// 监听购物车更新事件
const handleCartUpdate = () => {
  updateCartCount()
}

onMounted(() => {
  updateCartCount()
  window.addEventListener('cart-updated', handleCartUpdate)
})

onUnmounted(() => {
  window.removeEventListener('cart-updated', handleCartUpdate)
})

// 导航方法
const navigateTo = (path) => {
  router.push(path)
}

// 处理用户菜单命令
const handleUserCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'orders') {
    router.push('/user/orders')
  } else if (command === 'groupbuy') {
    router.push('/groupbuy/my')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm(
        '确定要退出登录吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      // 执行退出登录
      userStore.logout()
      ElMessage.success('已退出登录')
      
      // 跳转到首页
      router.push('/home')
    } catch (error) {
      // 用户取消
    }
  }
}
</script>

<style scoped>
/* 顶部导航栏样式 */
.top-nav {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #f4eaff 100%);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  animation: slideDown 0.4s ease-out;
}

.nav-content {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: opacity 0.3s;
}

.logo-section:hover {
  opacity: 0.9;
}

.logo-icon {
  filter: brightness(0) invert(1);
}

.site-name {
  font-size: 20px;
  font-weight: bold;
  color: #3a3a3a;
  letter-spacing: 1px;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  color: rgba(46, 46, 46, 0.85);
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.3s;
  font-size: 14px;
  position: relative;
}

.menu-item:hover {
  background-color: rgba(255, 255, 255, 0.15);
  color: #292929;
}

.menu-item.active {
  background-color: rgba(255, 255, 255, 0.25);
  color: #3a3a3a;
  font-weight: 500;
}

.menu-item.active::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: #303030;
  border-radius: 3px 3px 0 0;
}

.menu-item .el-icon {
  font-size: 16px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-right .el-button {
  background-color: rgba(255, 255, 255, 0.2);
  border: none;
  color: #3f3f3f;
  transition: all 0.3s;
  font-weight: 500;
}

.nav-right .el-button:hover {
  background-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.nav-right :deep(.el-badge__content) {
  border: 2px solid #667eea;
}

/* 用户下拉菜单样式 */
.nav-right .el-dropdown {
  cursor: pointer;
}

.nav-right :deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
}

.nav-right :deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .nav-left {
    gap: 20px;
  }
  
  .nav-menu {
    gap: 4px;
  }
  
  .menu-item {
    padding: 6px 12px;
    font-size: 13px;
  }
  
  .nav-right .el-button span {
    display: none;
  }
}

@media (max-width: 768px) {
  .top-nav {
    height: 56px;
  }
  
  .site-name {
    font-size: 16px;
  }
  
  .nav-content {
    padding: 0 12px;
  }
  
  .nav-left {
    gap: 16px;
  }
  
  .nav-menu {
    display: none;
  }
  
  .nav-right {
    gap: 8px;
  }
  
  .nav-right .el-button {
    padding: 8px 12px;
  }
}

/* 动画效果 */
@keyframes slideDown {
  from {
    transform: translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>

