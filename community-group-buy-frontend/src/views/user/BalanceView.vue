<template>
  <div class="balance-page-wrapper">
    <div class="balance-page">
    <h2>我的余额</h2>
    
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">账户余额</div>
            <div class="stat-value">¥{{ accountInfo.balance || '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">累计充值</div>
            <div class="stat-value">¥{{ accountInfo.totalRecharge || '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">累计消费</div>
            <div class="stat-value">¥{{ accountInfo.totalExpense || '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div class="stat-item">
            <div class="stat-label">累计返佣</div>
            <div class="stat-value">¥{{ accountInfo.totalCommission || '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div style="margin-top: 20px;">
      <el-button type="primary" @click="ElMessage.info('充值功能')">充值</el-button>
      <el-button @click="ElMessage.info('提现功能')">提现</el-button>
    </div>

    <el-card style="margin-top: 20px;">
      <h3>交易记录</h3>
      <el-empty description="暂无交易记录" />
    </el-card>
  </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAccountInfo } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const accountInfo = ref({
  balance: '0.00',
  totalRecharge: '0.00',
  totalExpense: '0.00',
  totalCommission: '0.00'
})

// 获取账户信息
const fetchAccountInfo = async () => {
  if (!userStore.userInfo?.userId) return
  
  try {
    const data = await getAccountInfo(userStore.userInfo.userId)
    accountInfo.value = data
  } catch (error) {
    console.error('Failed to fetch account info:', error)
    ElMessage.error('获取账户信息失败')
  }
}

onMounted(() => {
  fetchAccountInfo()
})
</script>

<style scoped>
.balance-page-wrapper {
  min-height: 100vh;
  padding-top: 84px; /* 64px导航栏 + 20px间隔 */
  background-color: #f5f5f5;
}

.balance-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 20px 20px;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}

h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 16px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .balance-page-wrapper {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }
}
</style>
