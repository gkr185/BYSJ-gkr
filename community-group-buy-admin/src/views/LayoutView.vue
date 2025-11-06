<template>
  <el-container class="layout-container">
    <!-- 顶部导航 -->
    <el-header>
      <div class="header-content">
        <h2>社区团购管理系统</h2>
        <div class="header-right">
          <span class="username">{{ userStore.username }}</span>
          <el-button type="danger" @click="handleLogout">退出登录</el-button>
        </div>
      </div>
    </el-header>
    
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="200px">
        <el-menu
          :default-active="currentRoute"
          router
          class="el-menu-vertical"
        >
          <el-menu-item index="/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/community">
            <el-icon><OfficeBuilding /></el-icon>
            <span>社区管理</span>
          </el-menu-item>
          <el-menu-item index="/community-application">
            <el-icon><Stamp /></el-icon>
            <span>社区申请审核</span>
          </el-menu-item>
          <el-menu-item index="/leader">
            <el-icon><Avatar /></el-icon>
            <span>团长管理</span>
          </el-menu-item>
          <el-menu-item index="/commission">
            <el-icon><Coin /></el-icon>
            <span>佣金管理</span>
          </el-menu-item>
          <el-menu-item index="/products">
            <el-icon><ShoppingBag /></el-icon>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/categories">
            <el-icon><List /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/groupbuy">
            <el-icon><TrophyBase /></el-icon>
            <span>拼团活动管理</span>
          </el-menu-item>
          <el-menu-item index="/orders">
            <el-icon><ShoppingCart /></el-icon>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/payments">
            <el-icon><CreditCard /></el-icon>
            <span>支付管理</span>
          </el-menu-item>
          <el-menu-item index="/feedback">
            <el-icon><ChatDotRound /></el-icon>
            <span>反馈管理</span>
          </el-menu-item>
          <el-menu-item index="/logs">
            <el-icon><Document /></el-icon>
            <span>系统日志</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <!-- 主内容区 -->
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox } from 'element-plus'
import { User, ChatDotRound, Document, OfficeBuilding, Stamp, Avatar, Coin, ShoppingBag, List, TrophyBase, ShoppingCart, CreditCard } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentRoute = computed(() => route.path)

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    userStore.logout()
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-header {
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  font-size: 14px;
}

.el-aside {
  background-color: #f5f5f5;
  height: calc(100vh - 60px);
}

.el-menu-vertical {
  border-right: none;
  height: 100%;
}

.el-main {
  background-color: #f0f2f5;
  padding: 0;
}
</style>

