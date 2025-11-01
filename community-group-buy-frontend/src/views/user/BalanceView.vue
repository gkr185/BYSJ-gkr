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
        <el-button size="large" :icon="Document" @click="goToPaymentRecords">
          支付记录
        </el-button>
      </div>

      <!-- 充值/提现说明 -->
      <el-alert type="info" :closable="false" style="margin-top: 20px;">
        <template #title>
          💡 充值/提现说明
        </template>
        <ul style="margin: 10px 0 0 0; padding-left: 20px;">
          <li>点击"充值"按钮可以进行余额充值（简化版本）</li>
          <li>提现需联系客服处理，微信号：<strong>wxid_community_groupbuy</strong></li>
          <li>余额可用于支付订单，满100元可提现</li>
        </ul>
      </el-alert>

      <!-- 交易记录 -->
      <el-card style="margin-top: 20px;" shadow="hover">
        <template #header>
          <div class="card-header">
            <span><el-icon><List /></el-icon> 最近交易记录</span>
            <el-button type="text" size="small" @click="fetchPaymentRecords">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>

        <el-skeleton v-if="loadingRecords" :rows="5" animated />

        <div v-else>
          <!-- 交易记录表格 -->
          <el-table :data="displayRecords" border stripe v-if="displayRecords.length > 0">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column label="交易类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getRecordTagType(row)" size="small">
                  {{ getRecordTypeText(row) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="金额" width="120">
              <template #default="{ row }">
                <span :class="row.amount >= 0 ? 'amount-plus' : 'amount-minus'">
                  {{ row.amount >= 0 ? '+' : '' }}¥{{ Math.abs(row.amount).toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="支付方式" width="120">
              <template #default="{ row }">
                {{ getPayTypeTextLocal(row.payType) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.payStatus === 1 ? 'success' : 'danger'" size="small">
                  {{ row.payStatus === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="备注" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.orderId">
                  订单支付（订单号：{{ row.orderId }}）
                </span>
                <span v-else-if="row.amount > 0">
                  余额充值
                </span>
                <span v-else>
                  订单退款（订单号：{{ row.orderId }}）
                </span>
              </template>
            </el-table-column>
            <el-table-column label="交易时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>

          <!-- 无数据 -->
          <el-empty v-else description="暂无交易记录" />

          <!-- 查看全部按钮 -->
          <div v-if="displayRecords.length > 0" style="text-align: center; margin-top: 20px;">
            <el-button type="primary" @click="goToPaymentRecords">
              查看全部记录
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getAccountInfo } from '@/api/user'
import { getPaymentRecords, recharge } from '@/api/payment'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Wallet,
  CreditCard,
  ShoppingBag,
  Money,
  Plus,
  Download,
  Document,
  List,
  Refresh,
  Search
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const loadingRecords = ref(false)
const recharging = ref(false)

const accountInfo = ref({
  balance: '0.00',
  totalRecharge: '0.00',
  totalExpense: '0.00',
  totalCommission: '0.00'
})

// 支付记录
const paymentRecords = ref([])

// 显示最近5条记录
const displayRecords = computed(() => {
  return paymentRecords.value.slice(0, 5)
})

// 充值表单
const rechargeAmount = ref(100)

// 获取账户信息
const fetchAccountInfo = async () => {
  if (!userStore.userInfo?.userId) return
  
  loading.value = true
  try {
    const res = await getAccountInfo(userStore.userInfo.userId)
    if (res.code === 200) {
      accountInfo.value = res.data
    } else {
      ElMessage.error(res.message || '获取账户信息失败')
    }
  } catch (error) {
    console.error('Failed to fetch account info:', error)
    ElMessage.error('获取账户信息失败')
  } finally {
    loading.value = false
  }
}

// 获取支付记录
const fetchPaymentRecords = async () => {
  loadingRecords.value = true
  try {
    const res = await getPaymentRecords()
    if (res.code === 200) {
      paymentRecords.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取支付记录失败')
    }
  } catch (error) {
    console.error('Failed to fetch payment records:', error)
    ElMessage.error('获取支付记录失败')
  } finally {
    loadingRecords.value = false
  }
}

// 充值
const handleRecharge = async () => {
  try {
    const { value: amount } = await ElMessageBox.prompt('请输入充值金额', '余额充值', {
      confirmButtonText: '确认充值',
      cancelButtonText: '取消',
      inputPattern: /^[0-9]+(\.[0-9]{1,2})?$/,
      inputErrorMessage: '请输入有效的金额',
      inputValue: '100'
    })

    if (!amount || parseFloat(amount) <= 0) {
      ElMessage.warning('充值金额必须大于0')
      return
    }

    recharging.value = true
    const res = await recharge({
      amount: parseFloat(amount),
      payType: 3 // 简化版本
    })

    if (res.code === 200) {
      ElMessage.success('充值成功！')
      
      // 刷新数据
      await fetchAccountInfo()
      await fetchPaymentRecords()
    } else {
      ElMessage.error(res.message || '充值失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('充值失败:', error)
      ElMessage.error('充值失败，请重试')
    }
  } finally {
    recharging.value = false
  }
}

// 提现
const handleWithdraw = () => {
  ElMessage.info('提现请联系客服微信：wxid_community_groupbuy')
}

// 跳转到支付记录
const goToPaymentRecords = () => {
  router.push({ name: 'paymentRecords' })
}

// 获取记录类型标签
const getRecordTagType = (record) => {
  if (record.orderId === null) {
    return 'success' // 充值
  } else if (record.amount < 0) {
    return 'info' // 退款
  } else {
    return 'danger' // 支付
  }
}

// 获取记录类型文本
const getRecordTypeText = (record) => {
  if (record.orderId === null) {
    return '充值'
  } else if (record.amount < 0) {
    return '退款'
  } else {
    return '支付'
  }
}

// 获取支付方式文本
const getPayTypeTextLocal = (payType) => {
  const map = {
    1: '微信支付',
    2: '支付宝',
    3: '余额支付'
  }
  return map[payType] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

onMounted(() => {
  fetchAccountInfo()
  fetchPaymentRecords()
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
