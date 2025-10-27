<template>
  <div class="order-confirm-page">
    <div class="order-confirm-container">
      <h2 class="page-title">确认订单</h2>

      <template v-if="loading">
        <el-skeleton :rows="10" animated />
      </template>

      <template v-else-if="cartItems.length === 0">
        <el-empty description="购物车为空">
          <el-button type="primary" @click="router.push('/products')">去购物</el-button>
        </el-empty>
      </template>

      <template v-else>
        <!-- 收货地址 -->
        <el-card class="address-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Location /></el-icon>
                收货地址
              </span>
              <el-button 
                text 
                type="primary" 
                @click="showAddressDialog = true"
              >
                {{ selectedAddress ? '更换地址' : '选择地址' }}
              </el-button>
            </div>
          </template>

          <div v-if="selectedAddress" class="address-info">
            <div class="address-main">
              <span class="receiver">{{ selectedAddress.receiver }}</span>
              <span class="phone">{{ selectedAddress.phone }}</span>
              <el-tag v-if="selectedAddress.is_default === 1" type="success" size="small">
                默认
              </el-tag>
            </div>
            <div class="address-detail">
              {{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }}{{ selectedAddress.detail }}
            </div>
          </div>

          <el-empty v-else description="请选择收货地址" :image-size="80">
            <el-button type="primary" @click="showAddressDialog = true">
              选择地址
            </el-button>
          </el-empty>
        </el-card>

        <!-- 商品清单 -->
        <el-card class="goods-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><ShoppingBag /></el-icon>
                商品清单
              </span>
              <span class="goods-count">共 {{ totalQuantity }} 件商品</span>
            </div>
          </template>

          <el-table :data="cartItems" border>
            <el-table-column label="商品信息" min-width="300">
              <template #default="{ row }">
                <div class="product-info">
                  <el-image 
                    :src="row.cover_img" 
                    style="width: 60px; height: 60px; border-radius: 4px;"
                    fit="cover"
                  />
                  <div class="product-details">
                    <div class="product-name">{{ row.product_name }}</div>
                    <el-tag v-if="row.team_id" type="danger" size="small">
                      拼团商品 · {{ row.team_no }}
                    </el-tag>
                    <el-tag v-else-if="row.activity_id" type="warning" size="small">
                      拼团活动
                    </el-tag>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120" align="center">
              <template #default="{ row }">
                <span class="price">¥{{ row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="100" align="center">
              <template #default="{ row }">
                × {{ row.quantity }}
              </template>
            </el-table-column>
            <el-table-column label="小计" width="120" align="center">
              <template #default="{ row }">
                <span class="total-price">¥{{ (row.price * row.quantity).toFixed(2) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <!-- 配送方式 -->
        <el-card class="delivery-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Van /></el-icon>
                配送方式
              </span>
            </div>
          </template>

          <el-radio-group v-model="deliveryMethod" class="delivery-options">
            <el-radio :label="1">
              <div class="delivery-option">
                <div class="option-name">团长配送</div>
                <div class="option-desc">由团长统一配送到指定地址</div>
              </div>
            </el-radio>
            <el-radio :label="2">
              <div class="delivery-option">
                <div class="option-name">上门自提</div>
                <div class="option-desc">到团长团点自行提货</div>
              </div>
            </el-radio>
          </el-radio-group>
        </el-card>

        <!-- 支付方式 -->
        <el-card class="payment-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Wallet /></el-icon>
                支付方式
              </span>
            </div>
          </template>

          <el-radio-group v-model="paymentMethod" class="payment-options">
            <el-radio :label="1">
              <div class="payment-option">
                <el-icon class="payment-icon" color="#09BB07"><ChatDotSquare /></el-icon>
                <span>微信支付</span>
              </div>
            </el-radio>
            <el-radio :label="2">
              <div class="payment-option">
                <el-icon class="payment-icon" color="#1677FF"><CreditCard /></el-icon>
                <span>支付宝支付</span>
              </div>
            </el-radio>
            <el-radio :label="3">
              <div class="payment-option">
                <el-icon class="payment-icon" color="#F56C6C"><Wallet /></el-icon>
                <span>余额支付</span>
                <span class="balance-amount">（余额：¥{{ accountBalance }}）</span>
              </div>
            </el-radio>
          </el-radio-group>
        </el-card>

        <!-- 订单备注 -->
        <el-card class="remark-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><EditPen /></el-icon>
                订单备注
              </span>
            </div>
          </template>

          <el-input
            v-model="orderRemark"
            type="textarea"
            :rows="3"
            placeholder="选填：对本次订单的补充说明（限200字）"
            maxlength="200"
            show-word-limit
          />
        </el-card>

        <!-- 订单金额 -->
        <el-card class="amount-card" shadow="hover">
          <div class="amount-item">
            <span class="label">商品总额：</span>
            <span class="value">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
          <div class="amount-item">
            <span class="label">优惠金额：</span>
            <span class="value discount">-¥{{ discountAmount.toFixed(2) }}</span>
          </div>
          <el-divider />
          <div class="amount-item total">
            <span class="label">应付金额：</span>
            <span class="value">¥{{ payAmount.toFixed(2) }}</span>
          </div>
        </el-card>

        <!-- 提交订单 -->
        <div class="submit-section">
          <div class="submit-info">
            <span class="total-label">实付金额：</span>
            <span class="total-amount">¥{{ payAmount.toFixed(2) }}</span>
          </div>
          <el-button 
            type="primary" 
            size="large" 
            :loading="submitting"
            :disabled="!canSubmit"
            @click="handleSubmitOrder"
          >
            提交订单
          </el-button>
        </div>
      </template>

      <!-- 地址选择对话框 -->
      <el-dialog
        v-model="showAddressDialog"
        title="选择收货地址"
        width="600px"
      >
        <div class="address-list">
          <div
            v-for="addr in addressList"
            :key="addr.address_id"
            :class="['address-item', { active: selectedAddress?.address_id === addr.address_id }]"
            @click="selectAddress(addr)"
          >
            <div class="address-main">
              <span class="receiver">{{ addr.receiver }}</span>
              <span class="phone">{{ addr.phone }}</span>
              <el-tag v-if="addr.is_default === 1" type="success" size="small">
                默认
              </el-tag>
            </div>
            <div class="address-detail">
              {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detail }}
            </div>
            <el-icon v-if="selectedAddress?.address_id === addr.address_id" class="check-icon" color="#67C23A">
              <CircleCheckFilled />
            </el-icon>
          </div>

          <el-empty v-if="addressList.length === 0" description="暂无收货地址">
            <el-button type="primary" @click="goToAddressManage">
              添加地址
            </el-button>
          </el-empty>
        </div>

        <template #footer>
          <el-button @click="showAddressDialog = false">取消</el-button>
          <el-button type="primary" @click="goToAddressManage">
            管理地址
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Location, ShoppingBag, Van, Wallet, EditPen, 
  ChatDotSquare, CreditCard, CircleCheckFilled
} from '@element-plus/icons-vue'
import { getCart, clearCart } from '@/utils/cart'
import { getUserAddressList } from '@/api/user'
import { mockProducts } from '@/mock/products'
import { createOrder } from '@/mock/orders'
import { apiGetTeamDetail } from '@/api/groupbuy' // ⭐新增：获取团详情

const router = useRouter()
const route = useRoute() // ⭐新增：获取路由参数
const userStore = useUserStore()

const loading = ref(true)
const submitting = ref(false)
const showAddressDialog = ref(false)

// ⭐新增：拼团相关参数（从URL获取）
const teamId = ref(route.query.team_id || null)
const activityId = ref(route.query.activity_id || null)
const productId = ref(route.query.product_id || null)
const teamInfo = ref(null) // 存储团详情

// 地址相关
const addressList = ref([])
const selectedAddress = ref(null)

// 购物车商品
const cartItems = ref([])

// 配送和支付方式
const deliveryMethod = ref(1) // 1:团长配送 2:上门自提
const paymentMethod = ref(1) // 1:微信 2:支付宝 3:余额

// 订单备注
const orderRemark = ref('')

// 账户余额
const accountBalance = ref('0.00')

// 计算商品总数量
const totalQuantity = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

// 计算商品总额 (total_amount)
const totalAmount = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0)
})

// 计算优惠金额 (discount_amount)
const discountAmount = computed(() => {
  // 这里可以添加优惠券、满减等逻辑
  return 0.00
})

// 计算实付金额 (pay_amount = total_amount - discount_amount)
const payAmount = computed(() => {
  return totalAmount.value - discountAmount.value
})

// 是否可以提交订单
const canSubmit = computed(() => {
  return selectedAddress.value && cartItems.value.length > 0 && !submitting.value
})

// 获取收货地址列表
const fetchAddressList = async () => {
  try {
    const data = await getUserAddressList()
    addressList.value = data || []
    
    // 自动选择默认地址
    const defaultAddr = addressList.value.find(addr => addr.is_default === 1)
    if (defaultAddr) {
      selectedAddress.value = defaultAddr
    } else if (addressList.value.length > 0) {
      selectedAddress.value = addressList.value[0]
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
  }
}

// ⭐新增：加载拼团信息
const loadTeamInfo = async () => {
  if (!teamId.value) return
  
  try {
    const data = await apiGetTeamDetail(teamId.value)
    if (data) {
      teamInfo.value = data
      // 使用拼团活动的商品信息
      cartItems.value = [{
        product_id: data.activity.product_id,
        product_name: data.activity.product_name,
        cover_img: data.activity.product_img,
        price: data.activity.group_price,
        quantity: 1,
        activity_id: data.activity_id,
        team_id: data.team_id, // ⭐关键：关联团ID
        team_no: data.team_no
      }]
      loading.value = false
    }
  } catch (error) {
    console.error('获取团详情失败:', error)
    ElMessage.error('获取拼团信息失败')
    loading.value = false
  }
}

// 加载购物车商品
const loadCartItems = () => {
  // ⭐如果是拼团订单，不从购物车加载，而是从团信息加载
  if (teamId.value) {
    loadTeamInfo()
    return
  }
  
  const cart = getCart()
  
  // 如果购物车为空，使用默认测试商品
  if (cart.length === 0) {
    // 默认添加测试商品（草莓和苹果）
    cartItems.value = [
      {
        product_id: 1,
        product_name: '新鲜草莓',
        cover_img: 'https://via.placeholder.com/100/FF6B6B/FFFFFF?text=草莓',
        price: 25.00,
        quantity: 1,
        activity_id: null,
        team_id: null // ⭐新增
      },
      {
        product_id: 2,
        product_name: '苹果',
        cover_img: 'https://via.placeholder.com/100/FFB6C1/FFFFFF?text=苹果',
        price: 19.90,
        quantity: 1,
        activity_id: 1, // 拼团商品
        team_id: null // ⭐新增
      }
    ]
    console.log('使用默认测试商品（购物车为空）')
  } else {
    // 根据product_id从mock数据中获取完整商品信息
    cartItems.value = cart.map(cartItem => {
      const product = mockProducts.find(p => p.product_id === cartItem.productId)
      
      if (!product) return null
      
      return {
        product_id: product.product_id,
        product_name: product.product_name,
        cover_img: product.cover_img,
        price: cartItem.activityId ? product.group_price : product.price, // 如果是拼团商品使用拼团价
        quantity: cartItem.quantity,
        activity_id: cartItem.activityId || null, // 拼团活动ID
        team_id: cartItem.teamId || null // ⭐新增：拼团团ID
      }
    }).filter(item => item !== null) // 过滤掉找不到的商品
  }
  
  loading.value = false
}

// 选择地址
const selectAddress = (addr) => {
  selectedAddress.value = addr
  showAddressDialog.value = false
}

// 跳转到地址管理
const goToAddressManage = () => {
  router.push('/user/address')
}

// 提交订单
const handleSubmitOrder = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  
  // 余额支付需要检查余额是否足够
  if (paymentMethod.value === 3) {
    const balance = parseFloat(accountBalance.value)
    if (balance < payAmount.value) {
      ElMessage.error('账户余额不足')
      return
    }
  }
  
  try {
    submitting.value = true
    
    // 准备订单数据 (严格遵循order_main表字段标准)
    const orderData = {
      user_id: userStore.userInfo?.userId || 1,
      leader_id: 1001, // 默认团长ID（实际应该根据收货地址自动匹配）
      total_amount: totalAmount.value,
      discount_amount: discountAmount.value,
      receive_address_id: selectedAddress.value.address_id,
      address: {
        receiver: selectedAddress.value.receiver,
        phone: selectedAddress.value.phone,
        province: selectedAddress.value.province,
        city: selectedAddress.value.city,
        district: selectedAddress.value.district,
        detail: selectedAddress.value.detail
      },
      // 准备订单项数据 (严格遵循order_item表字段标准)
      items: cartItems.value.map((item, index) => ({
        item_id: index + 1, // Mock用，实际由数据库生成
        product_id: item.product_id,
        activity_id: item.activity_id,
        team_id: item.team_id || null, // ⭐新增：拼团团ID
        team_no: item.team_no || null, // ⭐新增：团号
        product_name: item.product_name, // 快照：商品名称
        product_img: item.cover_img, // 快照：商品图片
        price: item.price, // 购买单价（拼团价或原价）
        quantity: item.quantity,
        total_price: item.price * item.quantity // 小计金额
      }))
    }
    
    // 模拟创建订单
    const order = createOrder(orderData)
    
    // ⭐如果不是拼团订单，才清空购物车
    if (!teamId.value) {
      clearCart()
      // 触发购物车更新事件
      window.dispatchEvent(new Event('cart-updated'))
    }
    
    ElMessage.success('订单提交成功！')
    
    // ⭐拼团订单跳转到团详情页，普通订单跳转到订单列表
    setTimeout(() => {
      if (teamId.value) {
        router.push(`/groupbuy/team/${teamId.value}`)
      } else {
        router.push(`/user/orders`)
      }
    }, 1000)
  } catch (error) {
    console.error('提交订单失败:', error)
    ElMessage.error('订单提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCartItems()
  fetchAddressList()
  
  // 获取账户余额（这里使用模拟数据）
  accountBalance.value = userStore.userInfo?.balance || '0.00'
})
</script>

<style scoped>
.order-confirm-page {
  min-height: 100vh;
  padding-top: 84px; /* 64px导航栏 + 20px间隔 */
  background-color: #f5f5f5;
}

.order-confirm-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
  padding-left: 12px;
  border-left: 4px solid #667eea;
}

/* 卡片通用样式 */
.address-card,
.goods-card,
.delivery-card,
.payment-card,
.remark-card,
.amount-card {
  margin-bottom: 20px;
  animation: fadeInUp 0.4s ease-out;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.goods-count {
  font-size: 14px;
  color: #999;
}

/* 地址信息 */
.address-info {
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border-left: 4px solid #67C23A;
}

.address-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.receiver {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.phone {
  font-size: 14px;
  color: #666;
}

.address-detail {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

/* 商品信息 */
.product-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price {
  font-size: 16px;
  color: #F56C6C;
  font-weight: 500;
}

.total-price {
  font-size: 16px;
  color: #F56C6C;
  font-weight: bold;
}

/* 配送方式 */
.delivery-options,
.payment-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.delivery-option,
.payment-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-name {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.option-desc {
  font-size: 13px;
  color: #999;
}

.payment-icon {
  font-size: 20px;
  margin-right: 4px;
}

.balance-amount {
  margin-left: 8px;
  font-size: 13px;
  color: #999;
}

/* 订单金额 */
.amount-card {
  padding: 24px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 15px;
}

.amount-item .label {
  color: #666;
}

.amount-item .value {
  color: #333;
  font-weight: 500;
}

.amount-item .value.discount {
  color: #F56C6C;
}

.amount-item.total {
  font-size: 18px;
  font-weight: bold;
}

.amount-item.total .value {
  color: #F56C6C;
  font-size: 24px;
}

/* 提交订单 */
.submit-section {
  position: sticky;
  bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 20px;
  padding: 20px 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.submit-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.total-label {
  font-size: 16px;
  color: #666;
}

.total-amount {
  font-size: 28px;
  font-weight: bold;
  color: #F56C6C;
}

.submit-section .el-button {
  min-width: 160px;
  height: 48px;
  font-size: 16px;
  font-weight: bold;
}

/* 地址选择对话框 */
.address-list {
  max-height: 400px;
  overflow-y: auto;
}

.address-item {
  position: relative;
  padding: 16px;
  margin-bottom: 12px;
  background-color: #f9f9f9;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-item:hover {
  background-color: #f0f0f0;
  border-color: #667eea;
}

.address-item.active {
  background-color: #f0f7ff;
  border-color: #67C23A;
}

.check-icon {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 24px;
}

/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-confirm-page {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }

  .page-title {
    font-size: 20px;
  }

  .submit-section {
    flex-direction: column;
    gap: 12px;
  }

  .total-amount {
    font-size: 24px;
  }

  .submit-section .el-button {
    width: 100%;
  }

  .product-info {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
