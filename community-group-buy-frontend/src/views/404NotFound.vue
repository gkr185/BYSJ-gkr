<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <!-- 404图标 -->
      <div class="error-icon">
        <el-icon :size="120" color="#409EFF">
          <WarningFilled />
        </el-icon>
      </div>

      <!-- 错误信息 -->
      <h1 class="error-code">404</h1>
      <h2 class="error-title">页面未找到</h2>
      <p class="error-desc">抱歉，您访问的页面不存在或已被删除</p>

      <!-- 操作按钮 -->
      <div class="error-actions">
        <el-button type="primary" size="large" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button size="large" @click="goBack">
          <el-icon><Back /></el-icon>
          返回上一页
        </el-button>
      </div>

      <!-- 友好提示 -->
      <div class="helpful-links">
        <p>您可能想要：</p>
        <el-space :size="16" wrap>
          <el-link type="primary" @click="$router.push('/products')">浏览商品</el-link>
          <el-link type="primary" @click="$router.push('/groupbuy')">查看拼团</el-link>
          <el-link type="primary" @click="$router.push('/profile')" v-if="userStore.isLogin">个人中心</el-link>
          <el-link type="primary" @click="$router.push('/login')" v-else>登录账号</el-link>
        </el-space>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { WarningFilled, HomeFilled, Back } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  padding: 20px;
}

.not-found-content {
  text-align: center;
  max-width: 600px;
}

.error-icon {
  margin-bottom: 24px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

.error-code {
  font-size: 100px;
  font-weight: bold;
  color: #409EFF;
  margin: 0 0 16px 0;
  letter-spacing: 4px;
}

.error-title {
  font-size: 32px;
  color: #333;
  margin: 0 0 12px 0;
}

.error-desc {
  font-size: 16px;
  color: #666;
  margin: 0 0 40px 0;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 48px;
  flex-wrap: wrap;
}

.helpful-links {
  padding: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.helpful-links p {
  font-size: 14px;
  color: #666;
  margin: 0 0 16px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .error-code {
    font-size: 72px;
  }
  
  .error-title {
    font-size: 24px;
  }
  
  .error-desc {
    font-size: 14px;
  }
}
</style>

