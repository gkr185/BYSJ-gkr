<template>
  <MainLayout>
    <div class="confirm-order-page">
      <div class="container">
        <!-- 页面标题 -->
        <div class="page-header">
          <div class="page-title">
            <el-icon class="title-icon"><DocumentChecked /></el-icon>
            <h1>确认订单</h1>
          </div>
          <p class="page-subtitle">请仔细核对订单信息，确保收货信息准确无误</p>
        </div>

        <el-row :gutter="24">
          <!-- 左侧：订单信息 -->
          <el-col :xs="24" :lg="16">
            <!-- 商品信息 -->
            <el-card class="order-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <el-icon><ShoppingBag /></el-icon>
                  <span>商品信息</span>
                  <el-tag type="info" size="small">{{ cartItems.length }}件商品</el-tag>
                </div>
              </template>

              <div class="product-list">
                <div
                  v-for="item in cartItems"
                  :key="item.cartId"
                  class="product-item"
                >
                  <div class="product-main">
                    <el-image
                      :src="item.productImg"
                      fit="cover"
                      class="product-image"
                      :lazy="true"
                    >
                      <template #error>
                        <div class="image-error">
                          <el-icon><Picture /></el-icon>
                        </div>
                      </template>
                      <template #loading>
                        <div class="image-loading">
                          <el-icon class="is-loading"><Loading /></el-icon>
                        </div>
                      </template>
                    </el-image>

                    <div class="product-info">
                      <div class="product-name">{{ item.productName }}</div>
                      <div class="product-meta">
                        <span class="product-price">¥{{ formatPrice(item.price) }}</span>
                        <span v-if="item.activityId" class="group-tag">拼团</span>
                      </div>
                    </div>

                    <div class="product-actions">
                      <el-input-number
                        v-model="item.quantity"
                        :min="1"
                        :max="item.inStock ? 99 : item.quantity"
                        size="small"
                        :disabled="!item.inStock"
                        @change="(val) => updateItemQuantity(item, val)"
                        class="quantity-input"
                      />
                      <div class="item-total">
                        <span class="subtotal">¥{{ formatPrice(item.price * item.quantity) }}</span>
                        <el-button
                          type="danger"
                          size="small"
                          text
                          @click="removeItem(item)"
                          class="remove-btn"
                        >
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </div>
                    </div>
                  </div>

                  <div v-if="!item.inStock" class="stock-warning">
                    <el-alert
                      type="warning"
                      :closable="false"
                      show-icon
                      size="small"
                      title="商品库存不足，仅可购买现有数量"
                    />
                  </div>
                </div>
              </div>

              <!-- 商品清单汇总 -->
              <div class="product-summary">
                <div class="summary-item">
                  <span>商品总价</span>
                  <span class="amount">¥{{ formatPrice(goodsTotal) }}</span>
                </div>
              </div>
            </el-card>

            <!-- 收货地址 -->
            <el-card class="order-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <el-icon><Location /></el-icon>
                  <span>收货地址</span>
                  <el-button link type="primary" @click="goToAddressManage">
                    <el-icon><EditPen /></el-icon>
                    管理地址
                  </el-button>
                </div>
              </template>

              <div v-if="addresses.length === 0" class="empty-address">
                <el-empty description="暂无收货地址，请先添加地址">
                  <el-button type="primary" @click="goToAddressManage">
                    <el-icon><Plus /></el-icon>
                    添加地址
                  </el-button>
                </el-empty>
              </div>

              <div v-else class="address-selection">
                <el-radio-group v-model="selectedAddressId" @change="handleAddressChange">
                  <div
                    v-for="address in addresses"
                    :key="address.addressId"
                    class="address-item"
                  >
                    <el-radio :label="address.addressId" class="address-radio">
                      <div class="address-content">
                        <div class="address-header">
                          <span class="receiver">{{ address.receiverName || address.receiver }}</span>
                          <span class="phone">{{ address.receiverPhone || address.phone }}</span>
                          <el-tag v-if="address.isDefault" size="small" type="success" effect="dark">
                            默认地址
                          </el-tag>
                        </div>
                        <div class="address-detail">
                          {{ formatAddress(address) }}
                        </div>
                      </div>
                    </el-radio>
                  </div>
                </el-radio-group>

                <el-button
                  type="primary"
                  plain
                  size="small"
                  @click="goToAddressManage"
                  class="add-address-btn"
                >
                  <el-icon><Plus /></el-icon>
                  新增收货地址
                </el-button>
              </div>
            </el-card>

            <!-- 配送信息 -->
            <el-card class="order-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <el-icon><Van /></el-icon>
                  <span>配送信息</span>
                </div>
              </template>

              <div class="delivery-info">
                <div class="delivery-item">
                  <span class="label">配送方式：</span>
                  <span class="value">{{ selectedDelivery?.name || '标准配送' }}</span>
                </div>
                <div class="delivery-item">
                  <span class="label">配送时间：</span>
                  <span class="value">{{ selectedDelivery?.time || '预计2-3个工作日' }}</span>
                </div>
                <div class="delivery-item">
                  <span class="label">运费：</span>
                  <span class="value freight">{{ selectedDelivery?.fee ? `¥${selectedDelivery.fee}` : '¥0.00' }}</span>
                </div>
              </div>
            </el-card>

            <!-- 订单备注 -->
            <el-card class="order-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <el-icon><Message /></el-icon>
                  <span>订单备注</span>
                </div>
              </template>

              <el-input
                v-model="orderRemark"
                type="textarea"
                :rows="3"
                placeholder="请填写订单备注信息（选填）"
                maxlength="200"
                show-word-limit
                class="remark-input"
              />
            </el-card>
          </el-col>

          <!-- 右侧：订单摘要 -->
          <el-col :xs="24" :lg="8">
            <el-card class="summary-card" shadow="never" v-loading="loading">
              <template #header>
                <div class="card-header">
                  <el-icon><Money /></el-icon>
                  <span>订单摘要</span>
                </div>
              </template>

              <!-- 费用明细 -->
              <div class="fee-breakdown">
                <div class="fee-item">
                  <span>商品总价</span>
                  <span>¥{{ formatPrice(goodsTotal) }}</span>
                </div>
                <div class="fee-item">
                  <span>运费</span>
                  <span>{{ selectedDelivery?.fee ? `¥${selectedDelivery.fee}` : '¥0.00' }}</span>
                </div>
                <div v-if="couponDiscount > 0" class="fee-item discount">
                  <span>优惠券折扣</span>
                  <span>-¥{{ formatPrice(couponDiscount) }}</span>
                </div>
                <el-divider />
                <div class="fee-item total">
                  <span>实付金额</span>
                  <span class="total-amount">¥{{ formatPrice(finalTotal) }}</span>
                </div>
              </div>

              <!-- 优惠券选择 -->
              <div class="coupon-section">
                <div class="section-title">优惠券</div>
                <el-select
                  v-model="selectedCouponId"
                  placeholder="选择优惠券（可选）"
                  clearable
                  @change="handleCouponChange"
                  class="coupon-select"
                >
                  <el-option
                    v-for="coupon in availableCoupons"
                    :key="coupon.id"
                    :label="`${coupon.name} - 减¥${coupon.discount}`"
                    :value="coupon.id"
                    :disabled="!canUseCoupon(coupon)"
                  />
                </el-select>
                <div v-if="selectedCoupon" class="coupon-info">
                  <el-text size="small" type="info">
                    {{ selectedCoupon.description }}
                  </el-text>
                </div>
              </div>

              <!-- 支付方式 -->
              <div class="payment-section">
                <div class="section-title">支付方式</div>
                <el-radio-group v-model="payMethod" class="payment-methods">
                  <el-radio-button
                    v-for="method in paymentMethods"
                    :key="method.value"
                    :label="method.value"
                    :disabled="!method.available"
                    class="payment-method"
                  >
                    <el-icon :class="method.iconClass">
                      <component :is="method.icon" />
                    </el-icon>
                    {{ method.label }}
                  </el-radio-button>
                </el-radio-group>

                <div v-if="payMethod === 'balance'" class="balance-info">
                  <el-text size="small">
                    当前余额：¥{{ formatPrice(userBalance) }}
                    <span :class="{ 'insufficient': userBalance < finalTotal }">
                      {{ userBalance >= finalTotal ? '(余额充足)' : '(余额不足)' }}
                    </span>
                  </el-text>
                </div>
              </div>

              <!-- 提交订单 -->
              <el-button
                type="danger"
                size="large"
                :loading="submitting"
                :disabled="!canSubmit"
                @click="showConfirmDialog"
                class="submit-btn"
                block
              >
                <el-icon><CreditCard /></el-icon>
                提交订单
              </el-button>

              <div class="order-tips">
                <el-alert
                  type="info"
                  :closable="false"
                  show-icon
                  size="small"
                  title="提交订单后将立即扣款，请确认信息无误"
                />
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 确认订单对话框 -->
      <el-dialog
        v-model="confirmDialogVisible"
        title="确认提交订单"
        width="500px"
        center
      >
        <div class="confirm-dialog-content">
          <div class="confirm-item">
            <span class="label">收货地址：</span>
            <span class="value">{{ selectedAddress?.receiverName }} {{ formatAddress(selectedAddress) }}</span>
          </div>
          <div class="confirm-item">
            <span class="label">支付方式：</span>
            <span class="value">{{ getPaymentMethodName(payMethod) }}</span>
          </div>
          <div class="confirm-item">
            <span class="label">应付金额：</span>
            <span class="value total">¥{{ formatPrice(finalTotal) }}</span>
          </div>
          <div v-if="orderRemark" class="confirm-item">
            <span class="label">订单备注：</span>
            <span class="value">{{ orderRemark }}</span>
          </div>
        </div>

        <template #footer>
          <el-button @click="confirmDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitOrder" :loading="submitting">
            确认提交
          </el-button>
        </template>
      </el-dialog>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import MainLayout from '@/components/common/MainLayout.vue'
import { useUserStore } from '@/stores/user'
import { checkoutCart, updateCartQuantity, removeFromCart } from '@/api/cart'
import { getCartList } from '@/api/cart'
import { getUserAddresses } from '@/api/user'
import { getCommunityLeaders } from '@/api/leader'
import { getAccountInfo } from '@/api/user'
import { getProductImageUrl } from '@/utils/image'
import {
  DocumentChecked,
  Picture,
  Loading,
  ShoppingBag,
  Location,
  EditPen,
  Plus,
  Van,
  Message,
  Money,
  CreditCard,
  Delete,
  Wallet
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const confirmDialogVisible = ref(false)
const cartItems = ref([])
const addresses = ref([])
const leaders = ref([])
const userBalance = ref(0)

// 表单数据
const selectedAddressId = ref(null)
const selectedLeaderId = ref(null)
const orderRemark = ref('')
const selectedCouponId = ref(null)
const payMethod = ref('balance')

// 从路由参数获取数据
const cartIds = computed(() => route.query.cartIds?.split(',').map(id => parseInt(id)) || [])
const addressId = computed(() => parseInt(route.query.addressId) || null)
const leaderId = computed(() => parseInt(route.query.leaderId) || null)

// 计算属性
const selectedAddress = computed(() => {
  return addresses.value.find(addr => addr.addressId === selectedAddressId.value)
})

const selectedLeader = computed(() => {
  return leaders.value.find(leader => leader.leaderId === selectedLeaderId.value)
})

const goodsTotal = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0)
})

const couponDiscount = computed(() => {
  return selectedCoupon.value?.discount || 0
})

const deliveryFee = computed(() => {
  return selectedDelivery.value?.fee || 0
})

const finalTotal = computed(() => {
  return Math.max(0, goodsTotal.value + deliveryFee.value - couponDiscount.value)
})

// 配送方式配置
const deliveryMethods = ref([
  { id: 1, name: '标准配送', time: '预计2-3个工作日', fee: 0 },
  { id: 2, name: '快递配送', time: '预计1-2个工作日', fee: 8 },
  { id: 3, name: '当日达', time: '预计当日送达', fee: 15 }
])

const selectedDelivery = computed(() => {
  return deliveryMethods.value[0] // 默认标准配送
})

// 支付方式配置
const paymentMethods = ref([
  {
    value: 'balance',
    label: '余额支付',
    icon: Wallet,
    iconClass: 'wallet-icon',
    available: true
  },
  {
    value: 'wechat',
    label: '微信支付',
    icon: CreditCard,
    iconClass: 'wechat-icon',
    available: false // 暂时不支持
  },
  {
    value: 'alipay',
    label: '支付宝',
    icon: CreditCard,
    iconClass: 'alipay-icon',
    available: false // 暂时不支持
  }
])

// 优惠券数据
const availableCoupons = ref([
  { id: 1, name: '新人优惠券', discount: 10, description: '新用户首次下单优惠10元' },
  { id: 2, name: '满减券', discount: 20, description: '满100减20' }
])

const selectedCoupon = computed(() => {
  return availableCoupons.value.find(coupon => coupon.id === selectedCouponId.value)
})

// 表单验证
const canSubmit = computed(() => {
  const hasItems = cartItems.value.length > 0
  const hasAddress = !!selectedAddressId.value
  const hasLeader = !!selectedLeaderId.value
  const sufficientBalance = payMethod.value === 'balance' ? userBalance.value >= finalTotal.value : true
  const allItemsValid = cartItems.value.every(item => item.quantity > 0 && item.inStock)

  return hasItems && hasAddress && hasLeader && sufficientBalance && allItemsValid
})

// 方法
const formatPrice = (price) => {
  return (Math.round(price * 100) / 100).toFixed(2)
}

const formatAddress = (address) => {
  if (!address) return ''
  return `${address.province} ${address.city} ${address.district} ${address.detailAddress || address.detail}`
}


const getPaymentMethodName = (method) => {
  const methodMap = {
    balance: '余额支付',
    wechat: '微信支付',
    alipay: '支付宝支付'
  }
  return methodMap[method] || method
}

const canUseCoupon = (coupon) => {
  // 简单判断优惠券是否可用
  return goodsTotal.value >= coupon.discount
}

// 加载购物车数据
const loadCartData = async () => {
  if (!cartIds.value.length) {
    ElMessage.error('购物车ID不能为空')
    router.back()
    return
  }

  try {
    loading.value = true
    const response = await getCartList(userStore.userInfo.userId)
    if (response.code === 200) {
      // 过滤出选中的购物车项
      cartItems.value = response.data
        .filter(item => cartIds.value.includes(item.cartId))
        .map(item => ({
          ...item,
          quantity: item.quantity || 1,
          inStock: item.inStock !== false // 默认认为有库存
        }))
    }
  } catch (error) {
    console.error('加载购物车数据失败:', error)
    ElMessage.error('加载购物车数据失败')
  } finally {
    loading.value = false
  }
}

// 加载地址和团长数据
const loadAddressAndLeaderData = async () => {
  try {
    // 加载用户地址
    const addressRes = await getUserAddresses(userStore.userInfo.userId)
    if (addressRes.code === 200) {
      addresses.value = addressRes.data || []
      // 设置默认选中的地址
      if (addressId.value) {
        selectedAddressId.value = addressId.value
      } else if (addresses.value.length > 0) {
        const defaultAddr = addresses.value.find(addr => addr.isDefault)
        selectedAddressId.value = defaultAddr ? defaultAddr.addressId : addresses.value[0].addressId
      }
    }

    // 加载社区团长
    const communityId = userStore.userInfo?.communityId
    if (communityId) {
      const leadersRes = await getCommunityLeaders(communityId)
      if (leadersRes.code === 200) {
        // ⭐⭐⭐ 关键修复：只选择正常运营的团长（status=1）
        const allLeaders = leadersRes.data || []
        leaders.value = allLeaders.filter(leader => leader.status === 1)
        
        console.log('团长列表加载:', {
          全部团长: allLeaders.length,
          可用团长: leaders.value.length,
          团长状态: allLeaders.map(l => ({ id: l.leaderId, name: l.leaderName, status: l.status }))
        })
        
        // 设置默认选中的团长
        if (leaderId.value) {
          // 验证传入的leaderId是否是可用团长
          const validLeader = leaders.value.find(l => l.leaderId === leaderId.value)
          if (validLeader) {
            selectedLeaderId.value = leaderId.value
          } else {
            console.warn('传入的团长不可用，自动选择其他团长')
            if (leaders.value.length > 0) {
              selectedLeaderId.value = leaders.value[0].leaderId
            }
          }
        } else if (leaders.value.length > 0) {
          selectedLeaderId.value = leaders.value[0].leaderId
        }
        
        console.log('✅ 选中团长ID:', selectedLeaderId.value)
      }
    } else {
      console.warn('用户未关联社区，无法加载团长列表')
    }

    // 加载用户余额
    const balanceRes = await getAccountInfo(userStore.userInfo.userId)
    if (balanceRes.code === 200) {
      userBalance.value = balanceRes.data?.balance || 0
    }
  } catch (error) {
    console.error('加载地址和团长数据失败:', error)
  }
}

// 更新商品数量
const updateItemQuantity = async (item, newQuantity) => {
  try {
    const response = await updateCartQuantity(userStore.userInfo.userId, item.cartId, newQuantity)
    if (response.code === 200) {
      item.quantity = newQuantity
      ElMessage.success('数量更新成功')
    } else {
      // 恢复原数量
      item.quantity = item.quantity
      ElMessage.error('数量更新失败')
    }
  } catch (error) {
    console.error('更新数量失败:', error)
    // 恢复原数量
    item.quantity = item.quantity
    ElMessage.error('数量更新失败')
  }
}

// 删除商品
const removeItem = async (item) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await removeFromCart(userStore.userInfo.userId, item.cartId)
    if (response.code === 200) {
      // 从列表中移除
      const index = cartItems.value.findIndex(cartItem => cartItem.cartId === item.cartId)
      if (index > -1) {
        cartItems.value.splice(index, 1)
      }
      ElMessage.success('商品已删除')

      // 如果购物车为空，返回上一页
      if (cartItems.value.length === 0) {
        ElMessage.warning('购物车已清空')
        router.back()
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
      ElMessage.error('删除商品失败')
    }
  }
}

// 处理地址变更
const handleAddressChange = (addressId) => {
  selectedAddressId.value = addressId
}

// 处理优惠券变更
const handleCouponChange = (couponId) => {
  selectedCouponId.value = couponId
}

// 跳转到地址管理
const goToAddressManage = () => {
  router.push('/user/address')
}

// 显示确认对话框
const showConfirmDialog = () => {
  if (!canSubmit.value) {
    ElMessage.warning('请完善订单信息')
    return
  }
  confirmDialogVisible.value = true
}

// 提交订单
const submitOrder = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  if (!selectedLeader.value) {
    ElMessage.warning('请选择团长')
    return
  }

  if (payMethod.value === 'balance' && userBalance.value < finalTotal.value) {
    ElMessage.warning('余额不足，请选择其他支付方式或充值')
    return
  }

  submitting.value = true
  confirmDialogVisible.value = false

  try {
    // 准备结算参数
    const checkoutData = {
      cartIds: cartItems.value.map(item => item.cartId),
      addressId: selectedAddressId.value,
      leaderId: selectedLeaderId.value,
      remark: orderRemark.value,
      couponId: selectedCouponId.value,
      payMethod: payMethod.value
    }

    // 调用购物车结算API
    const response = await checkoutCart(checkoutData)

    const orderIds = response.data || []

    if (orderIds.length === 0) {
      ElMessage.warning('订单创建失败')
      return
    }

    ElMessage.success('订单提交成功，正在跳转支付...')

    // 跳转到支付页面
    if (orderIds.length === 1) {
      router.push({
        path: '/payment',
        query: {
          orderId: orderIds[0],
          amount: finalTotal.value.toFixed(2),
          type: 'normal'
        }
      })
    } else {
      // 多个订单跳转到订单列表
      router.push('/user/orders')
    }

  } catch (error) {
    console.error('提交订单失败:', error)
    ElMessage.error('提交订单失败，请重试')
  } finally {
    submitting.value = false
  }
}

// 生命周期
onMounted(async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  await Promise.all([
    loadCartData(),
    loadAddressAndLeaderData()
  ])
})
</script>

<style scoped>
.confirm-order-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 页面标题 */
.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.title-icon {
  color: #409EFF;
}

.page-subtitle {
  color: #909399;
  font-size: 16px;
}

/* 卡片样式 */
.order-card {
  margin-bottom: 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

/* 商品信息 */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-item {
  padding: 20px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #f0f2f5;
}

.product-main {
  display: flex;
  align-items: center;
  gap: 16px;
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.image-error {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  color: #c0c4cc;
}

.image-loading {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.4;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.product-price {
  color: #f56c6c;
  font-weight: 500;
}

.group-tag {
  background: #e1f3d8;
  color: #67c23a;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.product-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 200px;
}

.quantity-input {
  width: 120px;
}

.item-total {
  min-width: 120px;
  text-align: right;
}

.subtotal {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.remove-btn {
  color: #f56c6c;
}

.stock-warning {
  margin-top: 12px;
}

.product-summary {
  padding: 16px 20px;
  background: #f8f9fa;
  border-radius: 0 0 8px 8px;
  border-top: 1px solid #f0f2f5;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
}

.amount {
  color: #f56c6c;
  font-weight: 600;
}

/* 地址选择 */
.address-selection {
  padding: 16px 0;
}

.address-item {
  margin-bottom: 12px;
}

.address-radio {
  width: 100%;
  margin-right: 0;
}

.address-radio :deep(.el-radio__label) {
  width: 100%;
}

.address-content {
  width: 100%;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.receiver {
  font-weight: 500;
  color: #303133;
}

.phone {
  color: #606266;
}

.address-detail {
  color: #909399;
  line-height: 1.4;
}

.add-address-btn {
  margin-top: 16px;
}

/* 配送信息 */
.delivery-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px 0;
}

.delivery-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.label {
  font-weight: 500;
  color: #303133;
  min-width: 80px;
}

.value {
  color: #606266;
}

.freight {
  color: #f56c6c;
  font-weight: 500;
}

/* 订单备注 */
.remark-input {
  margin-top: 12px;
}

/* 订单摘要 */
.summary-card {
  position: sticky;
  top: 20px;
}

.fee-breakdown {
  margin-bottom: 20px;
}

.fee-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 14px;
  color: #606266;
}

.fee-item.discount {
  color: #67c23a;
}

.total {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  padding: 16px 0;
  border-top: 2px solid #f56c6c;
  margin-top: 8px;
}

.total-amount {
  color: #f56c6c;
  font-size: 20px;
}

/* 优惠券 */
.coupon-section {
  margin-bottom: 20px;
  padding: 16px 0;
  border-top: 1px solid #f0f2f5;
}

.section-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
  font-size: 14px;
}

.coupon-select {
  width: 100%;
}

.coupon-info {
  margin-top: 8px;
}

/* 支付方式 */
.payment-section {
  margin-bottom: 20px;
  padding: 16px 0;
  border-top: 1px solid #f0f2f5;
}

.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 12px;
}

.payment-method {
  padding: 12px 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s;
}

.payment-method:hover {
  border-color: #409eff;
}

.wallet-icon {
  color: #409eff;
}

.wechat-icon {
  color: #07c160;
}

.alipay-icon {
  color: #1677ff;
}

.balance-info {
  font-size: 14px;
  color: #606266;
}

.insufficient {
  color: #f56c6c;
  font-weight: 500;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  margin-top: 16px;
}

/* 提示信息 */
.order-tips {
  margin-top: 16px;
}

/* 确认对话框 */
.confirm-dialog-content {
  padding: 20px 0;
}

.confirm-item {
  display: flex;
  margin-bottom: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f5;
}

.confirm-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.confirm-item .label {
  font-weight: 500;
  color: #303133;
  min-width: 80px;
  flex-shrink: 0;
}

.confirm-item .value {
  color: #606266;
  flex: 1;
  word-break: break-all;
}

.confirm-item .total {
  color: #f56c6c;
  font-weight: 600;
  font-size: 18px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 0 15px;
  }

  .page-title {
    font-size: 24px;
  }

  .product-main {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .product-image {
    width: 80px;
    height: 80px;
  }

  .product-actions {
    width: 100%;
    justify-content: space-between;
  }

  .quantity-input {
    flex: 1;
  }

  .address-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .delivery-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .confirm-item {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
