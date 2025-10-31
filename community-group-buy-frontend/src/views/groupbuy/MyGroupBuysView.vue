<template>
  <MainLayout>
    <div class="groupbuys-page">
      <div class="container">
        <h1 class="page-title">我的拼团</h1>

        <el-card class="placeholder-card">
          <div class="placeholder-content">
            <el-icon :size="80" color="#909399"><Grid /></el-icon>
            <h2>拼团功能开发中</h2>
            <p>该功能正在开发中，敬请期待！</p>
            <div class="feature-list">
              <h3>即将支持的功能：</h3>
              <ul>
                <li>🎯 参与拼团活动</li>
                <li>👥 查看拼团成员</li>
                <li>⏰ 拼团倒计时</li>
                <li>✅ 拼团成功通知</li>
                <li>↩️ 拼团失败自动退款</li>
                <li>📊 拼团历史记录</li>
              </ul>
            </div>
            <div class="info-box">
              <h4>拼团优势：</h4>
              <p>✨ 社区团购模式下，只有团长可以发起拼团活动</p>
              <p>💰 拼团价格更优惠，享受集体采购的实惠</p>
              <p>🚚 集中配送到团长团点，更加便捷</p>
            </div>
            <div class="action-buttons">
              <el-button type="primary" @click="router.push('/products')">
                查看商品
              </el-button>
              <el-button @click="router.push('/')">
                返回首页
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
import { Grid } from '@element-plus/icons-vue'

const router = useRouter()
</script>

<style scoped>
.groupbuys-page {
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
  max-width: 700px;
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
  margin-bottom: 32px;
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

.info-box {
  text-align: left;
  margin-bottom: 40px;
  padding: 24px;
  background: linear-gradient(135deg, #FFF9E6 0%, #FFE0B2 100%);
  border-radius: 8px;
  border-left: 4px solid #f57c00;
}

.info-box h4 {
  font-size: 16px;
  color: #333;
  margin-bottom: 12px;
  font-weight: bold;
}

.info-box p {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  line-height: 1.6;
}

.info-box p:last-child {
  margin-bottom: 0;
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

