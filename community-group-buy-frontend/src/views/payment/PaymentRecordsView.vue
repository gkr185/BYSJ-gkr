<template>
  <div class="payment-records-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>支付记录</h2>
      <p class="subtitle">查看所有支付和充值记录</p>
    </div>

    <!-- 记录类型切换 -->
    <el-card class="filter-card" shadow="hover">
      <el-radio-group v-model="recordType" @change="handleTypeChange">
        <el-radio-button value="all">全部记录</el-radio-button>
        <el-radio-button value="recharge">充值记录</el-radio-button>
      </el-radio-group>
    </el-card>

    <!-- 记录列表 -->
    <el-card class="records-card" shadow="hover">
      <el-skeleton v-if="loading" :rows="5" animated />
      
      <el-empty v-else-if="!records.length" description="暂无记录" />

      <div v-else class="records-list">
        <div 
          v-for="record in records" 
          :key="record.payId" 
          class="record-item"
          :class="{ 'refund': record.amount < 0 }"
        >
          <div class="record-icon">
            <el-icon :size="32" :color="getRecordIcon(record).color">
              <component :is="getRecordIcon(record).icon" />
            </el-icon>
          </div>

          <div class="record-info">
            <div class="record-title">
              {{ getRecordTitle(record) }}
            </div>
            <div class="record-desc">
              <span>{{ getPayTypeText(record.payType) }}</span>
              <span class="separator">|</span>
              <span>{{ formatTime(record.createTime) }}</span>
            </div>
            <div v-if="record.orderId" class="record-order">
              订单号: {{ record.orderId }}
            </div>
          </div>

          <div class="record-amount" :class="{ 'negative': record.amount < 0 }">
            <div class="amount">
              {{ record.amount >= 0 ? '+' : '' }}¥{{ Math.abs(record.amount).toFixed(2) }}
            </div>
            <el-tag :type="record.payStatus === 1 ? 'success' : 'danger'" size="small">
              {{ getPayStatusText(record.payStatus) }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 统计信息 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="8" :xs="24">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
            <el-icon :size="28"><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ totalRecords }}</div>
            <div class="stat-label">总记录数</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8" :xs="24">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <el-icon :size="28"><CreditCard /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ totalRecharge }}</div>
            <div class="stat-label">累计充值</div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8" :xs="24">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
            <el-icon :size="28"><ShoppingBag /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ totalPayment }}</div>
            <div class="stat-label">累计支付</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  TrendCharts, 
  CreditCard, 
  ShoppingBag,
  Wallet,
  Money,
  RefreshLeft
} from '@element-plus/icons-vue'
import { getPaymentRecords, getRechargeRecords, getPayTypeText, getPayStatusText } from '@/api/payment'
import { formatTime } from '@/utils/formatter'

// 记录类型
const recordType = ref('all')

// 记录列表
const records = ref([])
const loading = ref(false)

// 统计数据
const totalRecords = computed(() => records.value.length)

const totalRecharge = computed(() => {
  return records.value
    .filter(r => r.orderId === null && r.amount > 0)
    .reduce((sum, r) => sum + r.amount, 0)
    .toFixed(2)
})

const totalPayment = computed(() => {
  return records.value
    .filter(r => r.orderId !== null && r.amount > 0)
    .reduce((sum, r) => sum + r.amount, 0)
    .toFixed(2)
})

// 获取记录图标
const getRecordIcon = (record) => {
  if (record.orderId === null) {
    // 充值
    return { icon: CreditCard, color: '#409EFF' }
  } else if (record.amount < 0) {
    // 退款
    return { icon: RefreshLeft, color: '#F56C6C' }
  } else {
    // 支付
    return { icon: Wallet, color: '#67C23A' }
  }
}

// 获取记录标题
const getRecordTitle = (record) => {
  if (record.orderId === null) {
    return '余额充值'
  } else if (record.amount < 0) {
    return '订单退款'
  } else {
    return '订单支付'
  }
}

// 获取所有记录
const fetchAllRecords = async () => {
  loading.value = true
  try {
    const res = await getPaymentRecords()
    if (res.code === 200) {
      records.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取记录失败')
    }
  } catch (error) {
    console.error('获取记录失败:', error)
    ElMessage.error('获取记录失败')
  } finally {
    loading.value = false
  }
}

// 获取充值记录
const fetchRechargeRecords = async () => {
  loading.value = true
  try {
    const res = await getRechargeRecords()
    if (res.code === 200) {
      records.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取充值记录失败')
    }
  } catch (error) {
    console.error('获取充值记录失败:', error)
    ElMessage.error('获取充值记录失败')
  } finally {
    loading.value = false
  }
}

// 处理类型切换
const handleTypeChange = () => {
  if (recordType.value === 'all') {
    fetchAllRecords()
  } else {
    fetchRechargeRecords()
  }
}

// 页面初始化
onMounted(() => {
  fetchAllRecords()
})
</script>

<style scoped>
.payment-records-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

/* 过滤卡片 */
.filter-card {
  margin-bottom: 20px;
  text-align: center;
}

/* 记录列表 */
.records-card {
  margin-bottom: 20px;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.record-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.record-item:hover {
  background: #ebeef5;
  transform: translateX(5px);
}

.record-item.refund {
  background: #fef0f0;
}

.record-item.refund:hover {
  background: #fde2e2;
}

.record-icon {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 50%;
  margin-right: 20px;
}

.record-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.record-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.record-desc {
  font-size: 14px;
  color: #909399;
}

.separator {
  margin: 0 8px;
}

.record-order {
  font-size: 12px;
  color: #C0C4CC;
}

.record-amount {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5px;
}

.amount {
  font-size: 20px;
  font-weight: 600;
  color: #67C23A;
}

.record-amount.negative .amount {
  color: #F56C6C;
}

/* 统计卡片 */
.stats-cards {
  margin-top: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

:deep(.stat-card .el-card__body) {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 20px;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

/* 响应式 */
@media (max-width: 768px) {
  .record-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .record-icon {
    margin-right: 0;
  }

  .record-amount {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }
}
</style>

