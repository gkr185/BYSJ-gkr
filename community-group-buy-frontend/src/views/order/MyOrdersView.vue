<template>
  <MainLayout>
    <div class="orders-page">
      <div class="container">
        <h1 class="page-title">我的订单</h1>

        <el-card class="placeholder-card">
          <div class="placeholder-content">
            <el-icon :size="80" color="#909399"><Document /></el-icon>
            <h2>订单功能开发中</h2>
            <p>该功能正在开发中，敬请期待！</p>
            <div class="feature-list">
              <h3>即将支持的功能：</h3>
              <ul>
                <li>📋 查看订单列表</li>
                <li>🔍 订单搜索和筛选</li>
                <li>📦 订单详情查看</li>
                <li>💰 订单支付</li>
                <li>🚚 物流跟踪</li>
                <li>↩️ 退款申请</li>
              </ul>
            </div>
            <div class="action-buttons">
              <el-button type="primary" @click="router.push('/products')">
                继续购物
              </el-button>
              <el-button @click="router.push('/cart')">
                查看购物车
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import { useRouter } from 'vue-router'
import MainLayout from '@/components/common/MainLayout.vue'
import { Document } from '@element-plus/icons-vue'

const router = useRouter()
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  padding: 20px 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.placeholder-card {
  min-height: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-content {
  text-align: center;
  max-width: 600px;
  padding: 40px;
}

.placeholder-content h2 {
  font-size: 28px;
  color: #333;
  margin: 24px 0 16px;
}

.placeholder-content > p {
  font-size: 16px;
  color: #666;
  margin-bottom: 40px;
}

.feature-list {
  text-align: left;
  margin-bottom: 40px;
  padding: 24px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.feature-list h3 {
  font-size: 18px;
  color: #333;
  margin-bottom: 16px;
}

.feature-list ul {
  list-style: none;
  padding: 0;
}

.feature-list li {
  font-size: 15px;
  color: #666;
  padding: 8px 0;
  border-bottom: 1px solid #e4e7ed;
}

.feature-list li:last-child {
  border-bottom: none;
}

.action-buttons {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.action-buttons .el-button {
  min-width: 140px;
}
</style>

