<template>
  <div class="payment-view">
    <div class="payment-container">
      <!-- 订单信息 -->
      <el-card class="order-info-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <el-icon><DocumentChecked /></el-icon>
            <span>订单信息</span>
          </div>
        </template>
        
        <div v-if="orderInfo" class="order-info">
          <div class="info-row">
            <span class="label">订单编号：</span>
            <span class="value">{{ orderInfo.order_sn }}</span>
          </div>
          
          <div v-if="orderInfo.team_info" class="info-row">
            <span class="label">拼团团号：</span>
            <span class="value">
              <el-tag type="danger" size="small">{{ orderInfo.team_info.team_no }}</el-tag>
            </span>
          </div>
          
          <div class="info-row">
            <span class="label">商品信息：</span>
            <div class="product-list">
              <div
                v-for="item in orderInfo.items"
                :key="item.item_id"
                class="product-item"
              >
                <el-image
                  :src="item.product_img"
                  fit="cover"
                  class="product-img"
                />
                <div class="product-info">
                  <div class="product-name">{{ item.product_name }}</div>
                  <div class="product-price">
                    ¥{{ item.price }} × {{ item.quantity }}
                  </div>
                </div>
                <div class="product-total">
                  ¥{{ item.total_price }}
                </div>
              </div>
            </div>
          </div>
          
          <el-divider />
          
          <div class="info-row amount-row">
            <span class="label">订单金额：</span>
            <span class="value">¥{{ orderInfo.total_amount }}</span>
          </div>
          
          <div class="info-row amount-row" v-if="orderInfo.discount_amount > 0">
            <span class="label">优惠金额：</span>
            <span class="value discount">-¥{{ orderInfo.discount_amount }}</span>
          </div>
          
          <div class="info-row amount-row total">
            <span class="label">实付金额：</span>
            <span class="value price">¥{{ orderInfo.pay_amount }}</span>
          </div>
        </div>
      </el-card>
      
      <!-- 支付方式选择 -->
      <el-card class="payment-method-card">
        <template #header>
          <div class="card-header">
            <el-icon><CreditCard /></el-icon>
            <span>支付方式</span>
          </div>
        </template>
        
        <el-radio-group v-model="paymentMethod" class="payment-methods">
          <el-radio :label="1" class="payment-option">
            <div class="payment-option-content">
              <el-icon class="payment-icon wechat"><ChatLineSquare /></el-icon>
              <span>微信支付</span>
            </div>
          </el-radio>
          
          <el-radio :label="2" class="payment-option">
            <div class="payment-option-content">
              <el-icon class="payment-icon alipay"><Wallet /></el-icon>
              <span>支付宝支付</span>
            </div>
          </el-radio>
          
          <el-radio :label="3" class="payment-option">
            <div class="payment-option-content">
              <el-icon class="payment-icon balance"><Coin /></el-icon>
              <span>余额支付</span>
              <span class="balance-info">(当前余额: ¥{{ userBalance }})</span>
            </div>
          </el-radio>
        </el-radio-group>
      </el-card>
      
      <!-- 支付按钮 -->
      <div class="payment-actions">
        <el-button size="large" @click="handleCancel">取消支付</el-button>
        <el-button
          type="primary"
          size="large"
          :loading="paying"
          @click="handlePay"
        >
          立即支付 ¥{{ orderInfo?.pay_amount || 0 }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DocumentChecked,
  CreditCard,
  ChatLineSquare,
  Wallet,
  Coin
} from '@element-plus/icons-vue'
import { getOrderById } from '@/mock/orders'
import { apiPayOrder } from '@/api/payment'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 数据
const orderId = ref(route.query.order_id || null)
const orderInfo = ref(null)
const paymentMethod = ref(3) // 默认余额支付
const userBalance = ref(0)
const loading = ref(true)
const paying = ref(false)

// 加载订单信息
const loadOrderInfo = async () => {
  if (!orderId.value) {
    ElMessage.error('订单ID不存在')
    router.push('/user/orders')
    return
  }
  
  try {
    loading.value = true
    // Mock: 从订单列表中查找
    const order = getOrderById(parseInt(orderId.value))
    
    if (!order) {
      ElMessage.error('订单不存在')
      router.push('/user/orders')
      return
    }
    
    // 检查订单状态
    if (order.order_status !== 0) {
      ElMessage.warning('订单已支付或已取消')
      router.push('/user/orders')
      return
    }
    
    orderInfo.value = order
    
    // Mock: 获取用户余额
    userBalance.value = userStore.userInfo?.balance || 158.00
    
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

// 处理支付
const handlePay = async () => {
  if (!orderInfo.value) return
  
  // 余额支付时检查余额是否足够
  if (paymentMethod.value === 3) {
    if (userBalance.value < orderInfo.value.pay_amount) {
      ElMessage.error('余额不足，请选择其他支付方式或充值')
      return
    }
  }
  
  try {
    await ElMessageBox.confirm(
      `确认使用${getPaymentMethodName(paymentMethod.value)}支付 ¥${orderInfo.value.pay_amount} 吗？`,
      '确认支付',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    paying.value = true
    
    // Mock: 调用支付API
    const result = await apiPayOrder({
      order_id: orderId.value,
      pay_type: paymentMethod.value,
      pay_amount: orderInfo.value.pay_amount
    })
    
    ElMessage.success('支付成功！')
    
    // 如果是拼团订单，跳转到团详情页
    if (result.team_id) {
      setTimeout(() => {
        router.push(`/groupbuy/team/${result.team_id}`)
      }, 1000)
    } else {
      // 普通订单跳转到订单列表
      setTimeout(() => {
        router.push('/user/orders')
      }, 1000)
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

// 取消支付
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要取消支付吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '返回',
        type: 'warning'
      }
    )
    
    router.back()
  } catch (error) {
    // 用户取消
  }
}

// 获取支付方式名称
const getPaymentMethodName = (method) => {
  const names = {
    1: '微信支付',
    2: '支付宝支付',
    3: '余额支付'
  }
  return names[method] || '未知'
}

// 页面加载
onMounted(() => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  loadOrderInfo()
})
</script>

<style scoped lang="scss">
.payment-view {
  min-height: 100vh;
  padding-top: 84px; /* 64px导航栏 + 20px间隔 */
  background-color: #f5f5f5;
  padding-bottom: 120px;
}

.payment-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.order-info-card,
.payment-method-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.order-info {
  .info-row {
    display: flex;
    align-items: flex-start;
    margin-bottom: 16px;
    
    .label {
      min-width: 100px;
      color: #909399;
      font-size: 14px;
    }
    
    .value {
      flex: 1;
      color: #303133;
      font-size: 14px;
    }
  }
  
  .product-list {
    flex: 1;
    
    .product-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      background-color: #f9f9f9;
      border-radius: 8px;
      margin-bottom: 8px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .product-img {
        width: 60px;
        height: 60px;
        border-radius: 4px;
        flex-shrink: 0;
      }
      
      .product-info {
        flex: 1;
        
        .product-name {
          font-size: 14px;
          color: #303133;
          margin-bottom: 4px;
        }
        
        .product-price {
          font-size: 12px;
          color: #909399;
        }
      }
      
      .product-total {
        font-size: 16px;
        font-weight: 600;
        color: #f56c6c;
      }
    }
  }
  
  .amount-row {
    font-size: 16px;
    margin-bottom: 12px;
    
    .value {
      font-weight: 600;
      
      &.discount {
        color: #67c23a;
      }
      
      &.price {
        color: #f56c6c;
        font-size: 18px;
      }
    }
  }
  
  .total {
    padding-top: 12px;
    border-top: 2px dashed #e4e7ed;
    
    .label {
      color: #303133;
      font-weight: 600;
    }
  }
}

.payment-methods {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  
  .payment-option {
    width: 100%;
    margin-right: 0;
    padding: 16px;
    border: 2px solid #e4e7ed;
    border-radius: 8px;
    transition: all 0.3s;
    
    &:hover {
      border-color: #409eff;
      background-color: #ecf5ff;
    }
    
    :deep(.el-radio__input.is-checked + .el-radio__label) {
      color: #409eff;
    }
    
    :deep(.el-radio__input.is-checked) {
      ~ .payment-option-content {
        .payment-icon {
          color: #409eff;
        }
      }
    }
    
    .payment-option-content {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .payment-icon {
        font-size: 24px;
        transition: all 0.3s;
        
        &.wechat {
          color: #09bb07;
        }
        
        &.alipay {
          color: #1677ff;
        }
        
        &.balance {
          color: #f56c6c;
        }
      }
      
      .balance-info {
        margin-left: auto;
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

.payment-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  padding: 16px;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: center;
  gap: 16px;
  z-index: 10;
  
  .el-button {
    min-width: 150px;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .payment-view {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }
  
  .payment-container {
    padding: 12px;
  }
  
  .order-info {
    .info-row {
      flex-direction: column;
      gap: 4px;
      
      .label {
        min-width: auto;
      }
    }
  }
  
  .payment-actions {
    padding: 12px;
    
    .el-button {
      min-width: 120px;
      font-size: 14px;
    }
  }
}
</style>

