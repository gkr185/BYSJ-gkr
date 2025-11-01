<template>
  <div class="payment-page-wrapper">
    <div class="payment-page">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>订单支付</h2>
        <p class="subtitle">选择支付方式完成订单</p>
      </div>

      <!-- 订单信息 -->
      <el-card class="order-info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>订单信息</span>
            <el-tag type="info" size="small">订单号: {{ order.orderId }}</el-tag>
          </div>
        </template>
        
        <el-skeleton v-if="loadingOrder" :rows="3" animated />
        <div v-else class="order-details">
          <div class="detail-row">
            <span class="label">商品名称</span>
            <span class="value">{{ order.productName }}</span>
          </div>
          <div class="detail-row">
            <span class="label">购买数量</span>
            <span class="value">x{{ order.quantity }}</span>
          </div>
          <div class="detail-row">
            <span class="label">商品单价</span>
            <span class="value">¥{{ order.price }}</span>
          </div>
          <div class="detail-row total">
            <span class="label">订单总额</span>
            <span class="value">¥{{ order.totalAmount }}</span>
          </div>
        </div>
      </el-card>

      <!-- 支付方式选择 -->
      <el-card class="payment-method-card" shadow="hover">
        <template #header>
          <span>选择支付方式</span>
        </template>
        
        <el-radio-group v-model="selectedPayType" class="payment-methods">
          <!-- 余额支付 -->
          <el-radio :value="3" :disabled="balanceInsufficient" border size="large">
            <div class="payment-option">
              <div class="option-left">
                <el-icon :size="32" color="#409EFF"><Wallet /></el-icon>
                <div class="option-info">
                  <span class="option-name">余额支付</span>
                  <span class="option-desc">
                    当前余额: ¥{{ accountBalance }}
                    <span v-if="balanceInsufficient" class="insufficient-tip">（余额不足）</span>
                  </span>
                </div>
              </div>
              <el-icon v-if="selectedPayType === 3" class="check-icon" :size="24" color="#67C23A">
                <CircleCheck />
              </el-icon>
            </div>
          </el-radio>

          <!-- 微信支付 -->
          <el-radio :value="1" disabled border size="large">
            <div class="payment-option">
              <div class="option-left">
                <el-icon :size="32" color="#07C160"><Comment /></el-icon>
                <div class="option-info">
                  <span class="option-name">微信支付</span>
                  <span class="option-desc">暂未开放</span>
                </div>
              </div>
              <el-icon v-if="selectedPayType === 1" class="check-icon" :size="24" color="#67C23A">
                <CircleCheck />
              </el-icon>
            </div>
          </el-radio>

          <!-- 支付宝支付 -->
          <el-radio :value="2" disabled border size="large">
            <div class="payment-option">
              <div class="option-left">
                <el-icon :size="32" color="#1677FF"><CreditCard /></el-icon>
                <div class="option-info">
                  <span class="option-name">支付宝支付</span>
                  <span class="option-desc">暂未开放</span>
                </div>
              </div>
              <el-icon v-if="selectedPayType === 2" class="check-icon" :size="24" color="#67C23A">
                <CircleCheck />
              </el-icon>
            </div>
          </el-radio>
        </el-radio-group>

        <!-- 余额不足提示 -->
        <el-alert
          v-if="balanceInsufficient"
          title="余额不足"
          type="warning"
          :closable="false"
          show-icon
          class="balance-alert"
        >
          <template #default>
            <div class="alert-content">
              <span>您的余额不足，请先充值。</span>
              <el-button type="primary" size="small" @click="handleRecharge">
                立即充值
              </el-button>
            </div>
          </template>
        </el-alert>
      </el-card>

      <!-- 支付确认 -->
      <el-card class="payment-summary-card" shadow="hover">
        <div class="payment-summary">
          <div class="summary-row">
            <span class="label">订单总额</span>
            <span class="value">¥{{ order.totalAmount }}</span>
          </div>
          <div class="summary-row">
            <span class="label">优惠减免</span>
            <span class="value">-¥0.00</span>
          </div>
          <div class="summary-row total">
            <span class="label">实付金额</span>
            <span class="value">¥{{ order.totalAmount }}</span>
          </div>
        </div>
      </el-card>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button size="large" @click="handleCancel">
          取消
        </el-button>
        <el-button 
          type="primary" 
          size="large" 
          :loading="paying" 
          :disabled="!selectedPayType || balanceInsufficient"
          @click="handlePay"
        >
          {{ paying ? '支付中...' : '确认支付' }}
        </el-button>
      </div>
    </div>

    <!-- 充值对话框 -->
    <el-dialog
      v-model="rechargeDialogVisible"
      title="余额充值"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="rechargeForm" :rules="rechargeRules" ref="rechargeFormRef" label-width="100px">
        <el-form-item label="充值金额" prop="amount">
          <el-input
            v-model.number="rechargeForm.amount"
            placeholder="请输入充值金额"
            type="number"
            :min="0.01"
            :step="0.01"
          >
            <template #prefix>¥</template>
          </el-input>
        </el-form-item>
        
        <!-- 快捷金额 -->
        <el-form-item label="快捷金额">
          <el-button-group>
            <el-button @click="rechargeForm.amount = 50">¥50</el-button>
            <el-button @click="rechargeForm.amount = 100">¥100</el-button>
            <el-button @click="rechargeForm.amount = 200">¥200</el-button>
            <el-button @click="rechargeForm.amount = 500">¥500</el-button>
          </el-button-group>
        </el-form-item>

        <el-alert 
          title="温馨提示"
          type="info"
          :closable="false"
          show-icon
        >
          <div>当前为简化版本，充值后余额立即到账。</div>
        </el-alert>
      </el-form>

      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="recharging" @click="handleConfirmRecharge">
          确认充值
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Wallet, Comment, CreditCard, CircleCheck } from '@element-plus/icons-vue'
import { getOrderDetail } from '@/api/order'
import { createPayment, recharge } from '@/api/payment'
import { getAccount } from '@/api/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 订单信息
const order = ref({
  orderId: null,
  productName: '',
  quantity: 0,
  price: 0,
  totalAmount: 0
})
const loadingOrder = ref(false)

// 账户余额
const accountBalance = ref(0)
const loadingBalance = ref(false)

// 选中的支付方式
const selectedPayType = ref(3) // 默认余额支付

// 余额是否不足
const balanceInsufficient = computed(() => {
  return accountBalance.value < order.value.totalAmount
})

// 支付状态
const paying = ref(false)

// 充值对话框
const rechargeDialogVisible = ref(false)
const recharging = ref(false)
const rechargeForm = ref({
  amount: 100
})
const rechargeFormRef = ref(null)
const rechargeRules = {
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '充值金额必须大于0', trigger: 'blur' }
  ]
}

// 获取订单详情
const fetchOrderDetail = async () => {
  const orderId = route.query.orderId
  if (!orderId) {
    ElMessage.error('缺少订单ID')
    router.back()
    return
  }

  loadingOrder.value = true
  try {
    const res = await getOrderDetail(orderId)
    if (res.code === 200) {
      order.value = {
        orderId: res.data.orderId,
        productName: res.data.productName,
        quantity: res.data.quantity,
        price: res.data.price,
        totalAmount: res.data.totalAmount
      }
    } else {
      ElMessage.error(res.message || '获取订单详情失败')
      router.back()
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
    router.back()
  } finally {
    loadingOrder.value = false
  }
}

// 获取账户余额
const fetchAccountBalance = async () => {
  loadingBalance.value = true
  try {
    const res = await getAccount(userStore.userId)
    if (res.code === 200) {
      accountBalance.value = res.data.balance || 0
    }
  } catch (error) {
    console.error('获取账户余额失败:', error)
  } finally {
    loadingBalance.value = false
  }
}

// 处理支付
const handlePay = async () => {
  if (!selectedPayType.value) {
    ElMessage.warning('请选择支付方式')
    return
  }

  if (balanceInsufficient.value) {
    ElMessage.warning('余额不足，请先充值')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认支付 ¥${order.value.totalAmount} 吗？`,
      '确认支付',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    paying.value = true
    const res = await createPayment({
      orderId: order.value.orderId,
      payType: selectedPayType.value,
      amount: order.value.totalAmount
    })

    if (res.code === 200 && res.data.payStatus === 1) {
      ElMessage.success('支付成功！')
      
      // 跳转到订单详情页面
      setTimeout(() => {
        router.push({
          name: 'orderDetail',
          params: { id: order.value.orderId }
        })
      }, 1500)
    } else {
      ElMessage.error(res.message || res.data?.errorMessage || '支付失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
      ElMessage.error('支付失败，请重试')
    }
  } finally {
    paying.value = false
  }
}

// 处理充值
const handleRecharge = () => {
  rechargeDialogVisible.value = true
}

// 确认充值
const handleConfirmRecharge = async () => {
  if (!rechargeFormRef.value) return
  
  await rechargeFormRef.value.validate(async (valid) => {
    if (!valid) return

    recharging.value = true
    try {
      const res = await recharge({
        amount: rechargeForm.value.amount,
        payType: 3 // 简化版本
      })

      if (res.code === 200) {
        ElMessage.success('充值成功！')
        rechargeDialogVisible.value = false
        
        // 刷新余额
        await fetchAccountBalance()
      } else {
        ElMessage.error(res.message || '充值失败')
      }
    } catch (error) {
      console.error('充值失败:', error)
      ElMessage.error('充值失败，请重试')
    } finally {
      recharging.value = false
    }
  })
}

// 取消支付
const handleCancel = () => {
  router.back()
}

// 页面初始化
onMounted(() => {
  fetchOrderDetail()
  fetchAccountBalance()
})
</script>

<style scoped>
.payment-page-wrapper {
  min-height: calc(100vh - 120px);
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.payment-page {
  max-width: 800px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 40px;
  color: #fff;
}

.page-header h2 {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

/* 卡片样式 */
.order-info-card,
.payment-method-card,
.payment-summary-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 订单详情 */
.order-details {
  padding: 10px 0;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row.total {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
  margin-top: 10px;
  padding-top: 15px;
  border-top: 2px solid #f0f0f0;
}

.label {
  color: #909399;
}

.value {
  color: #303133;
  font-weight: 500;
}

.detail-row.total .value {
  font-size: 24px;
  color: #f56c6c;
}

/* 支付方式 */
.payment-methods {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

:deep(.el-radio) {
  width: 100%;
  margin-right: 0;
  height: auto;
  padding: 20px;
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.el-radio:hover) {
  border-color: #409EFF;
  transform: translateY(-2px);
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

:deep(.el-radio.is-checked) {
  border-color: #409EFF;
  background-color: #f0f9ff;
}

:deep(.el-radio.is-disabled) {
  opacity: 0.6;
}

.payment-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.option-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.option-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.option-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.option-desc {
  font-size: 14px;
  color: #909399;
}

.insufficient-tip {
  color: #f56c6c;
  font-weight: 500;
}

.check-icon {
  flex-shrink: 0;
}

/* 余额不足提示 */
.balance-alert {
  margin-top: 20px;
}

.alert-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* 支付汇总 */
.payment-summary {
  padding: 10px 0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.summary-row.total {
  font-size: 20px;
  font-weight: 600;
  color: #f56c6c;
  margin-top: 10px;
  padding-top: 15px;
  border-top: 2px solid #f0f0f0;
}

.summary-row.total .value {
  font-size: 28px;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

.action-buttons .el-button {
  width: 200px;
  height: 50px;
  font-size: 16px;
}

/* 响应式 */
@media (max-width: 768px) {
  .payment-page {
    padding: 0 10px;
  }

  .page-header h2 {
    font-size: 24px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 10px;
  }

  .action-buttons .el-button {
    width: 100%;
  }
}
</style>

