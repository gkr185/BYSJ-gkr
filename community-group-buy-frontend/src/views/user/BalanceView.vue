<template>
  <div class="balance-page-wrapper">
    <div class="balance-page">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>我的余额</h2>
        <p class="subtitle">查看账户余额和交易记录</p>
      </div>
    
      <!-- 余额概览卡片 -->
      <el-row :gutter="20" class="balance-cards">
        <el-col :span="6" :xs="12">
          <el-card class="balance-card" shadow="hover">
            <div class="card-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon :size="32"><Wallet /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">
                <el-skeleton v-if="loading" :rows="1" animated />
                <span v-else>¥{{ accountInfo.balance || '0.00' }}</span>
              </div>
              <div class="card-label">账户余额</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="balance-card" shadow="hover">
            <div class="card-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
              <el-icon :size="32"><CreditCard /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">
                <el-skeleton v-if="loading" :rows="1" animated />
                <span v-else>¥{{ accountInfo.totalRecharge || '0.00' }}</span>
              </div>
              <div class="card-label">累计充值</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="balance-card" shadow="hover">
            <div class="card-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
              <el-icon :size="32"><ShoppingBag /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">
                <el-skeleton v-if="loading" :rows="1" animated />
                <span v-else>¥{{ accountInfo.totalExpense || '0.00' }}</span>
              </div>
              <div class="card-label">累计消费</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="balance-card" shadow="hover">
            <div class="card-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
              <el-icon :size="32"><Money /></el-icon>
            </div>
            <div class="card-content">
              <div class="card-value">
                <el-skeleton v-if="loading" :rows="1" animated />
                <span v-else>¥{{ accountInfo.totalCommission || '0.00' }}</span>
              </div>
              <div class="card-label">累计返佣</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" size="large" :icon="Plus" @click="handleRecharge">
          充值
        </el-button>
        <el-button size="large" :icon="Download" @click="handleWithdraw">
          提现
        </el-button>
      </div>

      <!-- 充值/提现说明 -->
      <el-alert type="info" :closable="false" style="margin-top: 20px;">
        <template #title>
          💡 充值/提现说明
        </template>
        <ul style="margin: 10px 0 0 0; padding-left: 20px;">
          <li>充值功能暂未开通，敬请期待</li>
          <li>提现需联系客服处理，微信号：<strong>wxid_community_groupbuy</strong></li>
          <li>余额可用于支付订单，满100元可提现</li>
        </ul>
      </el-alert>

      <!-- 交易记录 -->
      <el-card style="margin-top: 20px;" shadow="hover">
        <template #header>
          <div class="card-header">
            <span><el-icon><List /></el-icon> 交易记录</span>
            <el-button type="text" size="small" @click="fetchAccountInfo">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>

        <!-- 筛选器 -->
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="交易类型">
            <el-select v-model="filterForm.type" placeholder="全部类型">
              <el-option label="全部" :value="null" />
              <el-option label="充值" value="recharge" />
              <el-option label="消费" value="expense" />
              <el-option label="返佣" value="commission" />
              <el-option label="提现" value="withdraw" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" disabled>查询</el-button>
          </el-form-item>
        </el-form>

        <!-- 交易记录表格 -->
        <el-table :data="testTransactions" border stripe v-if="testTransactions.length > 0">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="type" label="交易类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getTypeTagType(row.type)" size="small">
                {{ getTypeText(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">
              <span :class="row.type === 'recharge' || row.type === 'commission' ? 'amount-plus' : 'amount-minus'">
                {{ row.type === 'recharge' || row.type === 'commission' ? '+' : '-' }}¥{{ row.amount }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="balance" label="余额" width="120">
            <template #default="{ row }">
              ¥{{ row.balance }}
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createTime" label="交易时间" width="160" />
        </el-table>

        <!-- 无数据 -->
        <el-empty v-else description="暂无交易记录" />

        <!-- 分页 -->
        <el-pagination
          v-if="testTransactions.length > 0"
          :current-page="filterForm.page"
          :page-size="filterForm.limit"
          :total="testTransactions.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          style="margin-top: 20px; justify-content: center;"
          disabled
        />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAccountInfo } from '@/api/user'
import { ElMessage } from 'element-plus'
import {
  Wallet,
  CreditCard,
  ShoppingBag,
  Money,
  Plus,
  Download,
  List,
  Refresh,
  Search
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)

const accountInfo = ref({
  balance: '0.00',
  totalRecharge: '0.00',
  totalExpense: '0.00',
  totalCommission: '0.00'
})

// 筛选表单
const filterForm = reactive({
  type: null,
  page: 1,
  limit: 10
})

// 测试交易记录
const testTransactions = ref([
  {
    type: 'recharge',
    amount: '100.00',
    balance: '100.00',
    remark: '测试充值 - 支付宝充值',
    createTime: '2025-11-01 10:00:00'
  },
  {
    type: 'expense',
    amount: '29.90',
    balance: '70.10',
    remark: '测试消费 - 订单支付（订单号：TEST001）',
    createTime: '2025-11-01 11:00:00'
  },
  {
    type: 'commission',
    amount: '5.00',
    balance: '75.10',
    remark: '测试返佣 - 团长佣金（订单号：TEST002）',
    createTime: '2025-11-01 12:00:00'
  }
])

// 获取账户信息
const fetchAccountInfo = async () => {
  if (!userStore.userInfo?.userId) return
  
  loading.value = true
  try {
    const data = await getAccountInfo(userStore.userInfo.userId)
    accountInfo.value = data
  } catch (error) {
    console.error('Failed to fetch account info:', error)
    ElMessage.error('获取账户信息失败')
  } finally {
    loading.value = false
  }
}

// 充值
const handleRecharge = () => {
  ElMessage.info('充值功能开发中，敬请期待')
}

// 提现
const handleWithdraw = () => {
  ElMessage.info('提现请联系客服微信：wxid_community_groupbuy')
}

// 获取交易类型标签
const getTypeTagType = (type) => {
  const typeMap = {
    'recharge': 'success',
    'expense': 'danger',
    'commission': 'warning',
    'withdraw': 'info'
  }
  return typeMap[type] || 'info'
}

// 获取交易类型文本
const getTypeText = (type) => {
  const textMap = {
    'recharge': '充值',
    'expense': '消费',
    'commission': '返佣',
    'withdraw': '提现'
  }
  return textMap[type] || '未知'
}

onMounted(() => {
  fetchAccountInfo()
})
</script>

<style scoped>
.balance-page-wrapper {
  min-height: 100vh;
  padding-top: 84px;
  background-color: #f5f5f5;
}

.balance-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px 20px 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 余额卡片 */
.balance-cards {
  margin-bottom: 20px;
}

.balance-card {
  cursor: default;
  transition: all 0.3s;
}

.balance-card:hover {
  transform: translateY(-4px);
}

.balance-card :deep(.el-card__body) {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.card-label {
  font-size: 14px;
  color: #909399;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.action-buttons .el-button {
  flex: 1;
  max-width: 200px;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.card-header > span {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 筛选表单 */
.filter-form {
  margin-bottom: 20px;
}

/* 金额显示 */
.amount-plus {
  color: #67c23a;
  font-weight: bold;
}

.amount-minus {
  color: #f56c6c;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .balance-page-wrapper {
    padding-top: 76px;
  }

  .page-header h2 {
    font-size: 24px;
  }

  .card-value {
    font-size: 20px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .action-buttons .el-button {
    max-width: 100%;
  }
}
</style>
