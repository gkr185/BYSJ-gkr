<template>
  <MainLayout>
    <div class="my-teams-page">
      <div class="container">
        <h1 class="page-title">我的团队</h1>

        <el-card class="placeholder-card">
          <div class="placeholder-content">
            <el-icon :size="80" color="#409EFF"><Grid /></el-icon>
            <h2>团队管理功能</h2>
            <p>该功能正在开发中，敬请期待！</p>
            
            <div class="feature-preview">
              <h3>功能预览：</h3>
              <div class="preview-grid">
                <div class="preview-card">
                  <div class="preview-icon">📋</div>
                  <h4>我发起的团</h4>
                  <p>查看所有发起的拼团</p>
                </div>
                <div class="preview-card">
                  <div class="preview-icon">👥</div>
                  <h4>团员管理</h4>
                  <p>管理团的参与成员</p>
                </div>
                <div class="preview-card">
                  <div class="preview-icon">📊</div>
                  <h4>团统计</h4>
                  <p>查看拼团成功率等数据</p>
                </div>
                <div class="preview-card">
                  <div class="preview-icon">✅</div>
                  <h4>成团记录</h4>
                  <p>查看已成功的团</p>
                </div>
              </div>
            </div>

            <div class="notice-box">
              <h4>团长专属功能：</h4>
              <p>🌟 管理您发起的所有拼团活动</p>
              <p>📈 查看拼团成功率和团员参与情况</p>
              <p>💰 成团后自动计算佣金收益</p>
            </div>

            <div class="action-buttons">
              <el-button type="primary" @click="router.push('/groupbuy')">
                去发起拼团
              </el-button>
              <el-button @click="router.push('/leader/commission')">
                查看佣金
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
.my-teams-page {
  min-height: 100vh;
  padding: 20px 0;
  background: #f5f7fa;
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
  color: #303133;
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
  color: #303133;
  margin: 24px 0 16px;
}

.placeholder-content > p {
  font-size: 16px;
  color: #909399;
  margin-bottom: 40px;
}

.feature-preview {
  margin-bottom: 40px;
}

.feature-preview h3 {
  font-size: 20px;
  color: #303133;
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
  color: #303133;
  margin-bottom: 8px;
}

.preview-card p {
  font-size: 14px;
  color: #909399;
  line-height: 1.5;
}

.notice-box {
  text-align: left;
  margin-bottom: 40px;
  padding: 24px;
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 100%);
  border-radius: 8px;
  border-left: 4px solid #2196F3;
}

.notice-box h4 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 12px;
  font-weight: bold;
}

.notice-box p {
  font-size: 14px;
  color: #606266;
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

