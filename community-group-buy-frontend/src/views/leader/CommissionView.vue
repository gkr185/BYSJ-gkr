<template>
  <MainLayout>
    <div class="commission-page">
      <div class="container">
        <h1 class="page-title">佣金管理</h1>

        <el-card class="placeholder-card">
          <div class="placeholder-content">
            <el-icon :size="80" color="#67C23A"><Money /></el-icon>
            <h2>佣金管理功能</h2>
            <p>该功能正在开发中，敬请期待！</p>
            
            <div class="feature-preview">
              <h3>功能预览：</h3>
              <div class="preview-grid">
                <div class="preview-card">
                  <div class="preview-icon">💰</div>
                  <h4>佣金总览</h4>
                  <p>查看总收益和余额</p>
                </div>
                <div class="preview-card">
                  <div class="preview-icon">📝</div>
                  <h4>佣金明细</h4>
                  <p>查看每笔佣金记录</p>
                </div>
                <div class="preview-card">
                  <div class="preview-icon">⏳</div>
                  <h4>待结算</h4>
                  <p>查看待结算佣金</p>
                </div>
                <div class="preview-card">
                  <div class="preview-icon">✅</div>
                  <h4>已结算</h4>
                  <p>查看历史结算记录</p>
                </div>
              </div>
            </div>

            <div class="notice-box">
              <h4>佣金说明：</h4>
              <p>💵 成团后自动计算佣金，默认佣金比例为5%</p>
              <p>📅 每月统一结算一次佣金</p>
              <p>💳 提现请联系平台客服，线下处理</p>
            </div>

            <div class="action-buttons">
              <el-button type="primary" @click="router.push('/leader/teams')">
                查看我的团队
              </el-button>
              <el-button @click="router.push('/groupbuy')">
                去发起拼团
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
import { Money } from '@element-plus/icons-vue'

const router = useRouter()
</script>

<style scoped>
.commission-page {
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
  background-color: #f0f9ff;
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.2);
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
  background: linear-gradient(135deg, #FFF9E6 0%, #FFE0B2 100%);
  border-radius: 8px;
  border-left: 4px solid #FF9800;
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

