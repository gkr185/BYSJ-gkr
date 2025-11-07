<template>
  <MainLayout>
    <div class="payment-container">
      <div class="payment-card">
        <!-- 页面标题 -->
        <div class="page-header">
          <h2 class="page-title">
            <el-icon><Wallet /></el-icon>
            订单支付
          </h2>
        </div>

        <!-- 订单信息 -->
        <div v-if="orderInfo" class="order-section">
          <div class="section-title">
            <el-icon><DocumentChecked /></el-icon>
            订单信息
          </div>
          <div class="order-content">
            <div class="order-item">
              <span class="label">订单编号</span>
              <span class="value">{{ orderInfo.orderSn }}</span>
            </div>
            <div v-if="orderType === 'groupbuy'" class="order-item">
              <span class="label">订单类型</span>
              <el-tag type="danger" effect="dark">
                <el-icon><UserFilled /></el-icon>
                拼团订单
              </el-tag>
            </div>
            <div class="order-item">
              <span class="label">商品信息</span>
              <span class="value">{{ orderInfo.productName || '拼团商品' }}</span>
            </div>
            <div class="order-item amount-item">
              <span class="label">应付金额</span>
              <span class="amount">¥{{ payAmount }}</span>
            </div>
          </div>
        </div>

        <!-- 账户余额 -->
        <div v-if="accountInfo" class="account-section">
          <div class="section-title">
            <el-icon><CreditCard /></el-icon>
            账户余额
          </div>
          <div class="account-content">
            <div class="balance-display">
              <div class="balance-label">当前余额</div>
              <div class="balance-amount">¥{{ accountInfo.balance?.toFixed(2) || '0.00' }}</div>
            </div>
            <div v-if="accountInfo.balance < payAmount" class="insufficient-tip">
              <el-icon><WarningFilled /></el-icon>
              余额不足，请先充值
              <el-button type="primary" size="small" text @click="handleGoToRecharge">
                立即充值
              </el-button>
            </div>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="payment-method-section">
          <div class="section-title">
            <el-icon><Money /></el-icon>
            支付方式
          </div>
          <el-radio-group v-model="payType" class="payment-methods">
            <el-radio :label="3" border class="payment-radio">
              <div class="payment-option">
                <el-icon size="24" color="#67c23a"><Wallet /></el-icon>
                <div class="payment-info">
                  <div class="payment-name">余额支付</div>
                  <div class="payment-desc">使用账户余额支付</div>
                </div>
              </div>
            </el-radio>
          </el-radio-group>
          <div class="payment-tip">
            <el-icon><InfoFilled /></el-icon>
            当前仅支持余额支付，微信、支付宝支付功能开发中
          </div>
        </div>

        <!-- 支付按钮 -->
        <div class="payment-actions">
          <el-button size="large" @click="handleCancel">
            取消支付
          </el-button>
          <el-button
            type="danger"
            size="large"
            :loading="paying"
            :disabled="accountInfo && accountInfo.balance < payAmount"
            @click="handlePay"
          >
            <el-icon v-if="!paying"><Wallet /></el-icon>
            {{ paying ? '支付中...' : `立即支付 ¥${payAmount}` }}
          </el-button>
        </div>
      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Wallet, Money, CreditCard, DocumentChecked, UserFilled, InfoFilled, WarningFilled
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { getOrderDetail } from '@/api/order'
import { getAccountInfo } from '@/api/user'
import { createPayment } from '@/api/payment'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据
const orderInfo = ref(null)
const accountInfo = ref(null)
const payType = ref(3) // 3-余额支付
const paying = ref(false)

// 从路由参数获取
const orderId = computed(() => route.query.orderId)
const payAmount = computed(() => parseFloat(route.query.amount || 0).toFixed(2))
const orderType = computed(() => route.query.type || 'normal')

// 加载订单详情
const loadOrderDetail = async () => {
  if (!orderId.value) {
    ElMessage.error('订单ID不能为空')
    router.back()
    return
  }

  try {
    const res = await getOrderDetail(orderId.value)
    if (res.code === 200) {
      orderInfo.value = res.data
    } else {
      ElMessage.error(res.message || '获取订单详情失败')
      router.back()
    }
  } catch (error) {
    console.error('❌ 加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
    router.back()
  }
}

// 加载账户信息
const loadAccountInfo = async () => {
  if (!userStore.userInfo?.userId) {
    return
  }

  try {
    const res = await getAccountInfo(userStore.userInfo.userId)
    if (res.code === 200) {
      accountInfo.value = res.data
    }
  } catch (error) {
    console.error('❌ 加载账户信息失败:', error)
  }
}

// 处理支付
const handlePay = async () => {
  if (!accountInfo.value) {
    ElMessage.warning('无法获取账户信息')
    return
  }

  if (accountInfo.value.balance < payAmount.value) {
    ElMessage.warning('余额不足，请先充值')
    return
  }

  ElMessageBox.confirm(
    `确认支付 ¥${payAmount.value} 吗？`,
    '确认支付',
    {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    paying.value = true
    try {
      const res = await createPayment({
        orderId: parseInt(orderId.value),
        payType: payType.value,
        amount: parseFloat(payAmount.value)
      })

      if (res.code === 200) {
        ElMessage.success('支付成功！')
        
        // 延迟跳转，让用户看到成功提示
        setTimeout(() => {
          if (orderType.value === 'groupbuy') {
            // 拼团订单跳转到我的拼团页面
            router.push('/user/groups')
          } else {
            // 普通订单跳转到我的订单页面
            router.push('/user/orders')
          }
        }, 1500)
      } else {
        ElMessage.error(res.message || '支付失败')
      }
    } catch (error) {
      console.error('❌ 支付失败:', error)
      ElMessage.error(error.message || '支付失败，请稍后重试')
    } finally {
      paying.value = false
    }
  }).catch(() => {
    ElMessage.info('已取消支付')
  })
}

// 取消支付
const handleCancel = () => {
  ElMessageBox.confirm(
    '确定要取消支付吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '继续支付',
      type: 'warning'
    }
  ).then(() => {
    router.back()
  }).catch(() => {
    // 用户选择继续支付
  })
}

// 跳转到充值页面
const handleGoToRecharge = () => {
  router.push('/user/balance')
}

onMounted(() => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  loadOrderDetail()
  loadAccountInfo()
})
</script>

<style scoped>
.payment-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 20px;
}

.payment-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.page-header {
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  color: #333;
  margin: 0;
}

/* 订单信息 */
.order-section,
.account-section,
.payment-method-section {
  margin-bottom: 24px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.order-content {
  background: #f9fafb;
  border-radius: 12px;
  padding: 20px;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px dashed #e5e7eb;
}

.order-item:last-child {
  border-bottom: none;
}

.order-item .label {
  color: #666;
  font-size: 14px;
}

.order-item .value {
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.order-item.amount-item {
  padding-top: 16px;
  margin-top: 8px;
  border-top: 2px solid #f56c6c;
  border-bottom: none;
}

.order-item .amount {
  color: #f56c6c;
  font-size: 28px;
  font-weight: bold;
}

/* 账户余额 */
.account-content {
  background: linear-gradient(135deg, #f0f9ff 0%, #f0fff4 100%);
  border-radius: 12px;
  padding: 20px;
  border: 2px solid #67c23a;
}

.balance-display {
  text-align: center;
  margin-bottom: 12px;
}

.balance-label {
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.balance-amount {
  color: #67c23a;
  font-size: 32px;
  font-weight: bold;
}

.insufficient-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: #fff3e0;
  border-radius: 8px;
  color: #e6a23c;
  font-size: 14px;
  font-weight: 500;
}

/* 支付方式 */
.payment-methods {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.payment-radio {
  width: 100%;
  height: auto;
  padding: 16px;
  margin: 0;
}

.payment-radio :deep(.el-radio__label) {
  width: 100%;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 16px;
}

.payment-info {
  flex: 1;
  text-align: left;
}

.payment-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.payment-desc {
  font-size: 13px;
  color: #999;
}

.payment-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 8px;
  color: #409eff;
  font-size: 13px;
}

/* 支付按钮 */
.payment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 2px solid #f0f0f0;
}

.payment-actions .el-button {
  min-width: 150px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

/* 响应式 */
@media (max-width: 768px) {
  .payment-container {
    margin: 20px auto;
    padding: 12px;
  }

  .payment-card {
    padding: 20px;
  }

  .page-title {
    font-size: 20px;
  }

  .order-item .amount {
    font-size: 24px;
  }

  .balance-amount {
    font-size: 28px;
  }

  .payment-actions {
    flex-direction: column;
  }

  .payment-actions .el-button {
    width: 100%;
  }
}
</style>

