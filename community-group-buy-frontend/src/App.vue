<template>
  <div id="app">
    <!-- 顶部导航 -->
    <div class="header" v-if="!hideHeader">
      <div class="header-content">
        <div class="logo">社区团购系统</div>
        <div class="nav">
          <el-button text @click="router.push('/home')">首页</el-button>
          <el-button text @click="router.push('/profile')">个人中心</el-button>
          <el-button text v-if="!userStore.isLogin" @click="router.push('/login')">登录</el-button>
          <el-button text v-else @click="handleLogout">退出</el-button>
        </div>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content" :class="{ 'no-header': hideHeader }">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const hideHeader = computed(() => route.path === '/login')

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  }).catch(() => {})
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
  background-color: #f5f5f5;
}

#app {
  min-height: 100vh;
}

.header {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  padding: 0 20px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.logo {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
}

.nav {
  display: flex;
  gap: 10px;
}

.main-content {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}

.main-content.no-header {
  margin: 0;
  padding: 0;
  max-width: none;
}
</style>
