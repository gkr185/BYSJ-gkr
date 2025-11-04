<template>
  <MainLayout>
    <div class="balance-container">
      <div class="page-header">
        <el-button :icon="ArrowLeft" @click="$router.back()">返回</el-button>
        <h2 class="page-title">
          <el-icon><Wallet /></el-icon>
          账户余额
        </h2>
      </div>

      <!-- 余额卡片 -->
      <div class="balance-card">
        <div class="balance-bg"></div>
        <div class="balance-content">
          <div class="balance-info">
            <div class="balance-label">可用余额</div>
            <div class="balance-value">¥{{ accountInfo.balance?.toFixed(2) || '0.00' }}</div>
            <div class="frozen-balance">
              冻结金额：¥{{ accountInfo.frozenBalance?.toFixed(2) || '0.00' }}
            </div>
          </div>
          
          <div class="balance-actions">
            <el-button type="primary" :icon="CreditCard" size="large" @click="showRechargeDialog = true">
              充值
            </el-button>
          </div>
        </div>
      </div>

      <!-- 交易记录 -->
      <div class="transaction-section">
        <div class="section-header">
          <h3 class="section-title">
            <el-icon><Document /></el-icon>
            交易记录
          </h3>
          <el-radio-group v-model="filterType" size="small" @change="handleFilterChange">
            <el-radio-button label="">全部</el-radio-button>
            <el-radio-button label="recharge">充值</el-radio-button>
            <el-radio-button label="consume">消费</el-radio-button>
            <el-radio-button label="refund">退款</el-radio-button>
          </el-radio-group>
        </div>

        <div v-loading="loading" class="transaction-list">
          <el-empty v-if="!loading && transactionList.length === 0" description="暂无交易记录" />

          <div
            v-for="transaction in transactionList"
            :key="transaction.id"
            class="transaction-item"
          >
            <div class="transaction-icon" :class="getTransactionTypeClass(transaction.type)">
              <el-icon v-if="transaction.type === 'recharge'"><CreditCard /></el-icon>
              <el-icon v-else-if="transaction.type === 'consume'"><ShoppingCart /></el-icon>
              <el-icon v-else-if="transaction.type === 'refund'"><RefreshLeft /></el-icon>
              <el-icon v-else><Money /></el-icon>
            </div>

            <div class="transaction-content">
              <div class="transaction-title">{{ getTransactionTitle(transaction.type) }}</div>
              <div class="transaction-time">{{ formatTime(transaction.createTime) }}</div>
              <div v-if="transaction.remark" class="transaction-remark">{{ transaction.remark }}</div>
            </div>

            <div class="transaction-amount" :class="getAmountClass(transaction.type)">
              {{ getAmountText(transaction.type, transaction.amount) }}
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination">
          <el-pagination
            :current-page="page"
            :page-size="size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <!-- 充值对话框 -->
      <el-dialog
        v-model="showRechargeDialog"
        title="账户充值"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form
          ref="rechargeFormRef"
          :model="rechargeForm"
          :rules="rechargeRules"
          label-width="100px"
        >
          <el-form-item label="充值金额" prop="amount">
            <el-input
              v-model.number="rechargeForm.amount"
              type="number"
              placeholder="请输入充值金额"
              :prefix-icon="Money"
              clearable
            >
              <template #append>元</template>
            </el-input>
          </el-form-item>

          <el-form-item label="快捷金额">
            <div class="quick-amount">
              <el-button
                v-for="amount in quickAmounts"
                :key="amount"
                @click="rechargeForm.amount = amount"
              >
                {{ amount }}元
              </el-button>
            </div>
          </el-form-item>

          <el-alert
            title="温馨提示"
            type="info"
            :closable="false"
            show-icon
          >
            <p>1. 充值金额将立即到账</p>
            <p>2. 充值后可用于购买商品和参与拼团</p>
            <p>3. 余额不支持提现，请根据实际需求充值</p>
          </el-alert>
        </el-form>

        <template #footer>
          <el-button @click="showRechargeDialog = false">取消</el-button>
          <el-button type="primary" :loading="rechargeLoading" @click="handleRecharge">
            确认充值
          </el-button>
        </template>
      </el-dialog>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Wallet,
  CreditCard,
  Document,
  ShoppingCart,
  RefreshLeft,
  Money
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { getAccountInfo, recharge } from '@/api/user'
import { getPaymentRecords } from '@/api/payment'

const router = useRouter()
const userStore = useUserStore()
const rechargeFormRef = ref()
const loading = ref(false)
const rechargeLoading = ref(false)
const showRechargeDialog = ref(false)
const filterType = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)

// 账户信息
const accountInfo = ref({
  balance: 0,
  frozenBalance: 0
})

// 交易记录
const transactionList = ref([])

// 快捷充值金额
const quickAmounts = [10, 50, 100, 200, 500, 1000]

// 充值表单
const rechargeForm = reactive({
  amount: null
})

// 充值验证规则（简化）
const rechargeRules = {
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' }
  ]
}

// 加载账户信息
const loadAccountInfo = async () => {
  // 检查用户是否登录
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    return
  }

  try {
    const res = await getAccountInfo(userStore.userInfo.userId)
    if (res.code === 200) {
      accountInfo.value = res.data
    }
  } catch (error) {
    console.error('加载账户信息失败:', error)
  }
}

// 加载交易记录
const loadTransactions = async () => {
  // 检查用户是否登录
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    return
  }

  loading.value = true
  try {
    const res = await getPaymentRecords()
    console.log('💰 获取交易记录响应:', res)
    
    if (res.code === 200) {
      // 转换后端数据格式为前端格式
      const records = (res.data || []).map(record => {
        // 判断交易类型
        let type = 'other'
        if (record.amount < 0) {
          type = 'refund' // 退款
        } else if (record.orderId === null) {
          type = 'recharge' // 充值
        } else {
          type = 'consume' // 消费
        }

        return {
          id: record.payId,
          type: type,
          amount: Math.abs(record.amount), // 使用绝对值
          createTime: record.createTime,
          orderId: record.orderId,
          transactionId: record.transactionId,
          remark: record.orderId ? `订单号: ${record.orderId}` : '账户充值'
        }
      })

      // 按类型筛选
      let filteredRecords = records
      if (filterType.value) {
        filteredRecords = records.filter(r => r.type === filterType.value)
      }

      // 手动分页（后端没有分页）
      const start = (page.value - 1) * size.value
      const end = start + size.value
      transactionList.value = filteredRecords.slice(start, end)
      total.value = filteredRecords.length

      console.log('✅ 交易记录加载成功:', {
        total: total.value,
        currentPage: transactionList.value.length
      })
    } else {
      ElMessage.error(res.message || '加载交易记录失败')
      transactionList.value = []
      total.value = 0
    }
  } catch (error) {
    // 如果API未实现或报错，使用空数据
    console.error('❌ 加载交易记录失败:', error)
    transactionList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取交易类型样式
const getTransactionTypeClass = (type) => {
  const classMap = {
    'recharge': 'type-recharge',
    'consume': 'type-consume',
    'refund': 'type-refund'
  }
  return classMap[type] || 'type-other'
}

// 获取交易标题
const getTransactionTitle = (type) => {
  const titleMap = {
    'recharge': '账户充值',
    'consume': '消费支付',
    'refund': '退款到账'
  }
  return titleMap[type] || '其他交易'
}

// 获取金额样式
const getAmountClass = (type) => {
  return type === 'consume' ? 'amount-decrease' : 'amount-increase'
}

// 获取金额文本
const getAmountText = (type, amount) => {
  const prefix = type === 'consume' ? '-' : '+'
  return `${prefix}¥${amount.toFixed(2)}`
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 筛选变化
const handleFilterChange = () => {
  page.value = 1
  loadTransactions()
}

// 分页处理
const handleSizeChange = (newSize) => {
  size.value = newSize
  loadTransactions()
}

const handlePageChange = (newPage) => {
  page.value = newPage
  loadTransactions()
}

// 充值
const handleRecharge = async () => {
  // 检查用户是否登录
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    ElMessage.warning('请先登录')
    showRechargeDialog.value = false
    router.push('/login')
    return
  }

  await rechargeFormRef.value.validate(async (valid) => {
    if (!valid) return

    rechargeLoading.value = true
    try {
      const res = await recharge(userStore.userInfo.userId, rechargeForm.amount)
      
      if (res.code === 200) {
        ElMessage.success('充值成功')
        showRechargeDialog.value = false
        rechargeForm.amount = null
        await loadAccountInfo()
        await loadTransactions()
      } else {
        ElMessage.error(res.message || '充值失败')
      }
    } catch (error) {
      ElMessage.error('充值失败，请稍后重试')
      console.error('充值失败:', error)
    } finally {
      rechargeLoading.value = false
    }
  })
}

onMounted(() => {
  // 检查用户是否登录
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loadAccountInfo()
  loadTransactions()
})
</script>

<style scoped>
.balance-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 余额卡片 */
.balance-card {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  margin-bottom: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.balance-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.balance-content {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40px;
  color: #fff;
}

.balance-info {
  flex: 1;
}

.balance-label {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 12px;
}

.balance-value {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 8px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.frozen-balance {
  font-size: 14px;
  opacity: 0.8;
}

.balance-actions :deep(.el-button) {
  background: rgba(255, 255, 255, 0.95);
  color: #4facfe;
  border: none;
  border-radius: 12px;
  padding: 14px 32px;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.3s;
}

.balance-actions :deep(.el-button:hover) {
  background: #fff;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

/* 交易记录部分 */
.transaction-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f0f0f0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.section-header :deep(.el-radio-button__inner) {
  border-radius: 8px;
  border: none;
}

.section-header :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 交易列表 */
.transaction-list {
  min-height: 300px;
}

.transaction-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  transition: all 0.3s;
  margin-bottom: 12px;
  background: #f8f9fa;
}

.transaction-item:hover {
  background: #f0f0f0;
  transform: translateX(4px);
}

.transaction-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: #fff;
  font-size: 24px;
  flex-shrink: 0;
}

.transaction-icon.type-recharge {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.transaction-icon.type-consume {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.transaction-icon.type-refund {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.transaction-icon.type-other {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.transaction-content {
  flex: 1;
}

.transaction-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.transaction-time {
  font-size: 13px;
  color: #999;
  margin-bottom: 4px;
}

.transaction-remark {
  font-size: 13px;
  color: #666;
}

.transaction-amount {
  font-size: 20px;
  font-weight: 700;
  flex-shrink: 0;
}

.transaction-amount.amount-increase {
  color: #43e97b;
}

.transaction-amount.amount-decrease {
  color: #f5576c;
}

/* 分页 */
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

:deep(.el-pagination) {
  gap: 8px;
}

:deep(.el-pagination button),
:deep(.el-pager li) {
  border-radius: 8px;
}

:deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 16px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

/* 快捷金额 */
.quick-amount {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.quick-amount :deep(.el-button) {
  border-radius: 8px;
  font-weight: 600;
}

/* 表单样式 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

:deep(.el-alert) {
  border-radius: 10px;
  margin-top: 16px;
}

:deep(.el-alert__content p) {
  margin: 4px 0;
  font-size: 13px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .balance-container {
    padding: 16px;
  }

  .balance-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 24px;
    padding: 24px;
  }

  .balance-value {
    font-size: 36px;
  }

  .balance-actions {
    width: 100%;
  }

  .balance-actions :deep(.el-button) {
    width: 100%;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .transaction-item {
    flex-wrap: wrap;
  }

  .transaction-amount {
    width: 100%;
    text-align: right;
  }
}
</style>

