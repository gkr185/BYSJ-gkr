<template>
  <MainLayout>
    <div class="groupbuy-list-page">
      <div class="container">
        <h1 class="page-title">拼团活动</h1>

        <el-card class="placeholder-card">
          <div class="placeholder-content">
            <el-icon :size="80" color="#67C23A"><Grid /></el-icon>
            <h2>拼团活动列表</h2>
            <p>该功能正在开发中，敬请期待！</p>
            
            <div class="feature-preview">
              <h3>功能预览：</h3>
              <div class="preview-grid">
                <div class="preview-card">
                  <div class="preview-icon">🎯</div>
                  <h4>活动浏览</h4>
                  <p>查看团长发起的所有拼团活动</p>
                </div>
                <div class="preview-card">
                  <div class="preview-icon">👥</div>
                  <h4>参与拼团</h4>
                  <p>一键加入感兴趣的拼团</p>
                </div>
                <div class="preview-card">
                  <div class="preview-icon">⏰</div>
                  <h4>实时状态</h4>
                  <p>查看拼团进度和倒计时</p>
                </div>
                <div class="preview-card">
                  <div class="preview-icon">📍</div>
                  <h4>社区定位</h4>
                  <p>推荐附近社区的拼团活动</p>
                </div>
              </div>
            </div>

            <div class="notice-box">
              <h4>温馨提示：</h4>
              <p>🌟 根据v3.0拼团逻辑，只有团长可以发起拼团活动</p>
              <p>🎁 拼团成功后商品以更优惠的价格配送到团长团点</p>
              <p>🔄 拼团失败会自动退款到您的账户余额</p>
            </div>

            <div class="action-buttons">
              <el-button type="primary" @click="router.push('/products')">
                浏览商品
              </el-button>
              <el-button type="success" @click="router.push('/user/groups')">
                我的拼团
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
.groupbuy-list-page {
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
  max-width: 900px;
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

.feature-preview {
  margin-bottom: 40px;
}

.feature-preview h3 {
  font-size: 20px;
  color: #333;
  margin-bottom: 24px;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.preview-card {
  padding: 24px;
  background-color: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.preview-card:hover {
  background-color: #ecf5ff;
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.preview-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.preview-card h4 {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
}

.preview-card p {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.notice-box {
  text-align: left;
  margin-bottom: 40px;
  padding: 24px;
  background: linear-gradient(135deg, #E8F5E9 0%, #C8E6C9 100%);
  border-radius: 8px;
  border-left: 4px solid #4CAF50;
}

.notice-box h4 {
  font-size: 16px;
  color: #333;
  margin-bottom: 12px;
  font-weight: bold;
}

.notice-box p {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  line-height: 1.6;
}

.notice-box p:last-child {
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

/* 响应式 */
@media (max-width: 768px) {
  .preview-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}
</style>

