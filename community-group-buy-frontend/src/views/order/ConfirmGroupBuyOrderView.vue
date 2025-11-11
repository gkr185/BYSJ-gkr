<template>
  <MainLayout>
    <div class="confirm-order-page">
      <div class="container">
        <!-- 页面标题 -->
        <div class="page-header">
          <div class="page-title">
            <el-icon class="title-icon"><ShoppingCart /></el-icon>
            <h1>{{ isCartGroupBuy ? '确认拼团结算' : '确认拼团订单' }}</h1>
          </div>
          <p class="page-subtitle">
            {{ isCartGroupBuy ? '为购物车商品选择拼团，享优惠价格' : '请确认拼团信息和收货地址' }}
          </p>
        </div>

        <el-row :gutter="24">
          <!-- 左侧：订单信息 -->
          <el-col :xs="24" :lg="16">
            <!-- 商品信息 -->
            <el-card class="order-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <el-icon><ShoppingBag /></el-icon>
                  <span>拼团商品</span>
                  <el-tag v-if="isCartGroupBuy" type="info" size="small">{{ cartItems.length }}件商品</el-tag>
                </div>
              </template>

              <!-- 单个商品拼团 -->
              <div v-if="!isCartGroupBuy" class="single-product">
                <div class="product-main">
                  <el-image
                    :src="productImageUrl"
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
                    <div class="product-name">{{ product.productName }}</div>
                    <div class="product-meta">
                      <span class="product-price">¥{{ selectedActivity?.groupPrice }}</span>
                      <span class="group-tag">拼团价</span>
                    </div>
                    <div class="product-detail">
                      <span class="detail-item">商品ID: {{ product.productId }}</span>
                      <span class="detail-item">库存: {{ product?.stock || '-' }} 件</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 购物车拼团结算 -->
              <div v-else class="cart-products">
                <div
                  v-for="item in cartItems"
                  :key="item.cartId"
                  class="cart-item"
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
                    </el-image>

                    <div class="product-info">
                      <div class="product-name">{{ item.productName }}</div>
                      <div class="product-meta">
                        <span class="original-price">¥{{ item.price }}</span>
                        <span v-if="cartItemDetails[item.cartId]?.groupPrice" class="group-price">¥{{ cartItemDetails[item.cartId].groupPrice }}</span>
                        <span class="group-tag">拼团</span>
                      </div>
                      <div class="product-detail">
                        <span class="detail-item">数量: {{ item.quantity }}</span>
                        <span class="detail-item">小计: ¥{{ formatPrice((cartItemDetails[item.cartId]?.groupPrice || item.price) * item.quantity) }}</span>
                      </div>
                    </div>

                    <div class="team-selection">
                      <div class="selection-header">
                        <span class="selection-label">选择拼团队伍</span>
                        <el-tag
                          :type="item.selectedTeamId ? 'success' : 'warning'"
                          size="small"
                        >
                          {{ item.selectedTeamId ? '已选择' : '未选择' }}
                        </el-tag>
                      </div>
                      <el-select
                        v-model="item.selectedTeamId"
                        placeholder="请选择进行中的团"
                        style="width: 100%"
                        :loading="item.loadingTeams"
                        @change="(teamId) => handleTeamSelection(item, teamId)"
                        filterable
                      >
                        <el-option
                          v-for="team in cartItemTeams[item.cartId]"
                          :key="team.teamId"
                          :label="`${team.teamNo} - ${team.leaderName}的团 (${team.currentNum}/${team.requiredNum})`"
                          :value="team.teamId"
                          :disabled="team.currentNum >= team.requiredNum"
                        >
                          <div class="team-option">
                            <div class="team-info">
                              <span class="team-no">{{ team.teamNo }}</span>
                              <span class="team-leader">{{ team.leaderName }}的团</span>
                            </div>
                            <div class="team-progress">
                              <span class="progress-text">{{ team.currentNum }}/{{ team.requiredNum }}人</span>
                              <el-progress
                                :percentage="(team.currentNum / team.requiredNum) * 100"
                                :show-text="false"
                                :stroke-width="4"
                                :color="team.currentNum >= team.requiredNum ? '#f56c6c' : '#67c23a'"
                              />
                            </div>
                          </div>
                        </el-option>
                      </el-select>

                      <div v-if="item.selectedTeamId" class="selected-team-info">
                        <el-text size="small" type="info">
                          已选择: {{ getSelectedTeamInfo(item) }}
                        </el-text>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 商品清单汇总 -->
              <div class="product-summary">
                <div class="summary-item">
                  <span>商品总价</span>
                  <span class="amount">¥{{ formatPrice(goodsTotal) }}</span>
                </div>
                <div class="summary-item">
                  <span>拼团优惠</span>
                  <span class="discount-amount">-¥{{ formatPrice(totalDiscount) }}</span>
                </div>
              </div>
            </el-card>

            <!-- 收货地址 -->
            <el-card class="order-card" shadow="never">
              <template #header>
                <div class="card-header">
                  <el-icon><Location /></el-icon>
                  <span>收货地址</span>
                  <el-button link type="primary" @click="handleGoToAddressManage">
                    <el-icon><EditPen /></el-icon>
                    管理地址
                  </el-button>
                </div>
              </template>

              <div v-if="addressList.length === 0" class="empty-address">
                <el-empty description="暂无收货地址，请先添加地址">
                  <el-button type="primary" @click="handleGoToAddressManage">
                    <el-icon><Plus /></el-icon>
                    添加地址
                  </el-button>
                </el-empty>
              </div>

              <div v-else class="address-selection">
                <el-radio-group v-model="selectedAddressId" @change="handleAddressChange">
                  <div
                    v-for="address in addressList"
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
                  @click="handleGoToAddressManage"
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
                  <span>拼团摘要</span>
                </div>
              </template>

              <!-- 拼团信息 -->
              <div v-if="selectedActivity && !isCartGroupBuy" class="group-buy-info">
                <div class="info-section">
                  <h4 class="section-title">拼团详情</h4>
                  <div class="info-item">
                    <span class="label">拼团价：</span>
                    <span class="value price">¥{{ selectedActivity.groupPrice }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">购买数量：</span>
                    <el-input-number
                      v-model="quantity"
                      :min="1"
                      :max="(product?.stock || 1)"
                      :step="1"
                      controls-position="right"
                      size="small"
                      style="width: 100px"
                    />
                  </div>
                  <div class="info-item">
                    <span class="label">成团人数：</span>
                    <span class="value">{{ selectedActivity.requiredNum }}人</span>
                  </div>
                  <div class="info-item">
                    <span class="label">当前人数：</span>
                    <span class="value">{{ selectedTeam?.currentNum || 0 }}人</span>
                  </div>
                  <div class="info-item">
                    <span class="label">活动时间：</span>
                    <span class="value">{{ formatDateRange(selectedActivity) }}</span>
                  </div>
                </div>

                <div class="progress-section">
                  <div class="progress-header">
                    <span>拼团进度</span>
                    <span class="progress-text">{{ selectedTeam?.currentNum || 0 }}/{{ selectedActivity.requiredNum }}</span>
                  </div>
                  <el-progress
                    :percentage="((selectedTeam?.currentNum || 0) / selectedActivity.requiredNum) * 100"
                    :stroke-width="8"
                    :color="selectedTeam?.currentNum >= selectedActivity.requiredNum ? '#f56c6c' : '#67c23a'"
                    :show-text="false"
                  />
                </div>
              </div>

              <!-- 购物车拼团结算统计 -->
              <div v-else-if="isCartGroupBuy" class="cart-summary">
                <div class="summary-stats">
                  <div class="stat-item">
                    <span class="stat-label">已选择拼团</span>
                    <span class="stat-value">{{ selectedTeamsCount }}/{{ cartItems.length }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">预计节省</span>
                    <span class="stat-value discount">¥{{ formatPrice(totalDiscount) }}</span>
                  </div>
                </div>
              </div>

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
                <div v-if="totalDiscount > 0" class="fee-item discount">
                  <span>拼团优惠</span>
                  <span>-¥{{ formatPrice(totalDiscount) }}</span>
                </div>
                <el-divider />
                <div class="fee-item total">
                  <span>实付金额</span>
                  <span class="total-amount">¥{{ formatPrice(finalTotal) }}</span>
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
                确认并支付
              </el-button>

              <div class="order-tips">
                <el-alert
                  type="info"
                  :closable="false"
                  show-icon
                  size="small"
                  title="拼团商品支付后将锁定名额，请确认信息无误"
                />
                <div class="group-buy-tips">
                  <el-text size="small" type="warning">
                    💡 拼团成功后不可退款，请谨慎选择
                  </el-text>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 确认订单对话框 -->
      <el-dialog
        v-model="confirmDialogVisible"
        title="确认提交拼团订单"
        width="500px"
        center
      >
        <div class="confirm-dialog-content">
          <div v-if="!isCartGroupBuy" class="confirm-item">
            <span class="label">拼团信息：</span>
            <span class="value">{{ selectedTeam?.teamNo }} - {{ selectedActivity?.groupPrice }}元/件 × {{ quantity }}</span>
          </div>
          <div v-else class="confirm-item">
            <span class="label">拼团结算：</span>
            <span class="value">{{ selectedTeamsCount }}件商品已选择拼团，预计节省 ¥{{ formatPrice(totalDiscount) }}</span>
          </div>
          <div class="confirm-item">
            <span class="label">收货地址：</span>
            <span class="value">{{ selectedAddress?.receiverName }} {{ formatAddress(selectedAddress) }}</span>
          </div>
          <div class="confirm-item">
            <span class="label">支付方式：</span>
            <span class="value">{{ getPaymentMethodName(payMethod) }}</span>
          </div>
          <div class="confirm-item">
            <span class="label">实付金额：</span>
            <span class="value total">¥{{ formatPrice(finalTotal) }}</span>
          </div>
          <div v-if="orderRemark" class="confirm-item">
            <span class="label">订单备注：</span>
            <span class="value">{{ orderRemark }}</span>
          </div>
        </div>

        <template #footer>
          <el-button @click="confirmDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确认支付
          </el-button>
        </template>
      </el-dialog>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ShoppingCart, Location, EditPen, Plus, CreditCard, Picture, Loading,
  Van, Message, Money, Wallet, Delete
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { useUserStore } from '@/stores/user'
import { getUserAddresses, getAccountInfo } from '@/api/user'
import { getProductDetail } from '@/api/product'
import { getProductGroupBuyActivities, joinTeam } from '@/api/groupbuy'
import { getCartList } from '@/api/cart'
import { getProductImageUrl } from '@/utils/image'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const submitting = ref(false)
const confirmDialogVisible = ref(false)

// 路由参数
const productId = computed(() => route.query.productId || route.params.productId)
const teamId = computed(() => route.query.teamId ? parseInt(route.query.teamId) : null)
const cartIds = computed(() => route.query.cartIds?.split(',').map(id => parseInt(id)) || [])
const addressId = computed(() => parseInt(route.query.addressId))
const leaderId = computed(() => parseInt(route.query.leaderId))

// 判断是否为购物车拼团结算
const isCartGroupBuy = computed(() => cartIds.value.length > 0)

// 数据
const product = ref(null)
const selectedActivity = ref(null)
const selectedTeam = ref(null)
const userBalance = ref(0)

// 购物车拼团结算数据
const cartItems = ref([])
const cartItemTeams = ref({}) // 存储每个购物车项的可用团队
const cartItemDetails = ref({}) // 存储每个购物车项的拼团详情

// 表单数据
const addressList = ref([])
const selectedAddressId = ref(null)
const orderRemark = ref('')
const payMethod = ref('balance')

// 数量
const quantity = ref(1)

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

// 计算属性
const productImageUrl = computed(() => getProductImageUrl(product.value))

const goodsTotal = computed(() => {
  if (isCartGroupBuy.value) {
    // 商品总价应该是原价的总和，不考虑拼团优惠
    return cartItems.value.reduce((sum, item) => {
      return sum + (item.price * item.quantity)
    }, 0)
  } else {
    // 单个商品拼团时，商品总价也是原价
    return (product.value?.price || 0) * quantity.value
  }
})

const totalDiscount = computed(() => {
  if (isCartGroupBuy.value) {
    return cartItems.value.reduce((sum, item) => {
      const detail = cartItemDetails.value[item.cartId]
      if (detail && detail.groupPrice) {
        const discount = (detail.originalPrice - detail.groupPrice) * item.quantity
        return sum + Math.max(0, discount)
      }
      return sum
    }, 0)
  } else {
    const originalPrice = product.value?.price || 0
    const groupPrice = selectedActivity.value?.groupPrice || 0
    return Math.max(0, (originalPrice - groupPrice) * quantity.value)
  }
})

const deliveryFee = computed(() => {
  return selectedDelivery.value?.fee || 0
})

const finalTotal = computed(() => {
  return Math.max(0, goodsTotal.value + deliveryFee.value - totalDiscount.value)
})

const selectedTeamsCount = computed(() => {
  return cartItems.value.filter(item => item.selectedTeamId).length
})

const canSubmit = computed(() => {
  const hasAddress = !!selectedAddressId.value
  const sufficientBalance = payMethod.value === 'balance' ? userBalance.value >= finalTotal.value : true

  if (isCartGroupBuy.value) {
    // 购物车拼团结算：所有商品都要选择拼团
    const allSelected = cartItems.value.every(item => item.selectedTeamId)
    return hasAddress && allSelected && sufficientBalance && cartItems.value.length > 0
  } else {
    // 单个商品拼团
    const inStock = (product.value?.stock || 0) > 0
    const validQty = quantity.value >= 1 && quantity.value <= (product.value?.stock || 1)
    return hasAddress && !!selectedTeam.value && !!selectedActivity.value && !!product.value && inStock && validQty && sufficientBalance
  }
})

// 方法
const formatPrice = (price) => {
  return (Math.round(price * 100) / 100).toFixed(2)
}

const formatAddress = (address) => {
  if (!address) return ''
  return `${address.province} ${address.city} ${address.district} ${address.detailAddress || address.detail}`
}

const formatDateRange = (activity) => {
  if (!activity) return '-'
  const start = formatDate(activity.startTime)
  const end = formatDate(activity.endTime)
  return `${start} ~ ${end}`
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${mm}-${dd} ${hh}:${mi}`
}

const getPaymentMethodName = (method) => {
  const methodMap = {
    balance: '余额支付',
    wechat: '微信支付',
    alipay: '支付宝支付'
  }
  return methodMap[method] || method
}

const getSelectedTeamInfo = (item) => {
  const team = cartItemTeams.value[item.cartId]?.find(t => t.teamId === item.selectedTeamId)
  return team ? `${team.teamNo} - ${team.leaderName}的团` : ''
}

const loadUserAddresses = async () => {
  try {
    const res = await getUserAddresses(userStore.userInfo.userId)
    if (res.code === 200) {
      addressList.value = res.data || []
      // 设置默认选中的地址
      if (addressId.value) {
        selectedAddressId.value = addressId.value
      } else if (addressList.value.length > 0) {
        const defaultAddr = addressList.value.find(a => a.isDefault)
        selectedAddressId.value = defaultAddr ? defaultAddr.addressId : addressList.value[0].addressId
      }
    }

    // 加载用户余额
    const balanceRes = await getAccountInfo(userStore.userInfo.userId)
    if (balanceRes.code === 200) {
      userBalance.value = balanceRes.data?.balance || 0
    }
  } catch (error) {
    console.error('加载地址和余额失败:', error)
  }
}

// 加载购物车商品和拼团信息（购物车拼团结算）
const loadCartItemsAndTeams = async () => {
  if (!cartIds.value.length) return
  loading.value = true
  try {
    // 加载购物车数据
    const cartRes = await getCartList(userStore.userInfo.userId)
    if (cartRes.code === 200) {
      // 过滤出选中的购物车项，并添加拼团选择字段
      cartItems.value = cartRes.data.filter(item =>
        cartIds.value.includes(item.cartId)
      ).map(item => ({
        ...item,
        selectedTeamId: null,
        loadingTeams: false
      }))

      // 为每个购物车项加载拼团活动
      for (const item of cartItems.value) {
        try {
          item.loadingTeams = true
          const actRes = await getProductGroupBuyActivities(item.productId, {
            communityId: userStore.userInfo?.communityId
          })
          if (actRes.code === 200) {
            const activities = actRes.data || []
            // 收集所有可用的团队
            const allTeams = []
            activities.forEach(activity => {
              if (activity.teams && activity.teams.length > 0) {
                activity.teams.forEach(team => {
                  allTeams.push({
                    ...team,
                    activityId: activity.activityId,
                    groupPrice: activity.groupPrice
                  })
                })
              }
            })
            cartItemTeams.value[item.cartId] = allTeams
          }
        } catch (error) {
          console.error(`加载商品${item.productId}的团队失败:`, error)
          cartItemTeams.value[item.cartId] = []
        } finally {
          item.loadingTeams = false
        }
      }
    }
  } catch (error) {
    console.error('加载购物车数据失败:', error)
    ElMessage.error('加载购物车数据失败')
  } finally {
    loading.value = false
  }
}

const loadProductAndTeam = async () => {
  if (!productId.value) return
  loading.value = true
  try {
    const prodRes = await getProductDetail(productId.value)
    if (prodRes.code === 200) {
      product.value = prodRes.data
    }
    const actRes = await getProductGroupBuyActivities(productId.value, {
      communityId: userStore.userInfo?.communityId
    })
    if (actRes.code === 200) {
      const activities = actRes.data || []
      // 定位team和其活动
      outer:
      for (const activity of activities) {
        for (const t of (activity.teams || [])) {
          if (t.teamId === teamId.value) {
            selectedTeam.value = t
            selectedActivity.value = activity
            break outer
          }
        }
      }
      if (!selectedTeam.value) {
        ElMessage.warning('团队已失效或不存在')
        router.replace({ path: `/groupbuy/product/${productId.value}` })
        return
      }
    }
  } catch (error) {
    console.error('加载商品和团队信息失败:', error)
    ElMessage.error('加载商品信息失败')
  } finally {
    loading.value = false
  }
}

// 处理团队选择（购物车拼团结算）
const handleTeamSelection = async (cartItem, teamId) => {
  cartItem.selectedTeamId = teamId

  if (teamId) {
    try {
      // 查找选中的团队详情
      const team = cartItemTeams.value[cartItem.cartId]?.find(t => t.teamId === teamId)
      if (team) {
        // 获取拼团活动的详细信息
        const actRes = await getProductGroupBuyActivities(cartItem.productId, {
          communityId: userStore.userInfo?.communityId
        })
        if (actRes.code === 200) {
          const activity = actRes.data.find(act => act.activityId === team.activityId)
          if (activity) {
            // 保存拼团详情到cartItemDetails
            cartItemDetails.value[cartItem.cartId] = {
              team,
              activity,
              groupPrice: activity.groupPrice,
              originalPrice: cartItem.price
            }
          }
        }
      }
    } catch (error) {
      console.error(`获取购物车项${cartItem.cartId}的拼团详情失败:`, error)
    }
  } else {
    // 取消选择时清除详情
    delete cartItemDetails.value[cartItem.cartId]
  }

  console.log(`购物车项${cartItem.cartId}选择了团队${teamId}`)
}

// 处理地址变更
const handleAddressChange = (addressId) => {
  selectedAddressId.value = addressId
}

// 跳转到地址管理
const handleGoToAddressManage = () => {
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

const handleSubmit = async () => {
  if (isCartGroupBuy.value) {
    // 购物车拼团结算
    await handleCartGroupBuySubmit()
  } else {
    // 单个商品拼团
    await handleSingleGroupBuySubmit()
  }
}

// 单个商品拼团提交
const handleSingleGroupBuySubmit = async () => {
  if (!canSubmit.value) {
    ElMessage.warning('请完善拼团信息')
    return
  }

  if (payMethod.value === 'balance' && userBalance.value < finalTotal.value) {
    ElMessage.warning('余额不足，请选择其他支付方式或充值')
    return
  }

  submitting.value = true
  confirmDialogVisible.value = false

  try {
    const res = await joinTeam({
      teamId: selectedTeam.value.teamId,
      addressId: selectedAddressId.value,
      quantity: quantity.value,
      remark: orderRemark.value
    })

    if (res.code === 200) {
      const { orderId, payAmount } = res.data
      ElMessage.success('拼团成功，正在跳转支付...')

      router.replace({
        path: '/payment',
        query: {
          orderId,
          amount: payAmount,
          type: 'groupbuy'
        }
      })
    } else {
      ElMessage.error(res.message || '参团失败，请稍后重试')
    }
  } catch (error) {
    console.error('参团失败:', error)
    ElMessage.error(error.message || '参团失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 购物车拼团结算提交
const handleCartGroupBuySubmit = async () => {
  // 检查是否选择了地址
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  // 检查是否所有商品都选择了拼团
  const unselectedItems = cartItems.value.filter(item => !item.selectedTeamId)
  if (unselectedItems.length > 0) {
    ElMessage.warning(`还有${unselectedItems.length}个商品未选择拼团`)
    return
  }

  if (payMethod.value === 'balance' && userBalance.value < finalTotal.value) {
    ElMessage.warning('余额不足，请选择其他支付方式或充值')
    return
  }

  submitting.value = true
  confirmDialogVisible.value = false

  try {
    // 逐个参与拼团
    const orderIds = []
    for (const cartItem of cartItems.value) {
      const res = await joinTeam({
        teamId: cartItem.selectedTeamId,
        addressId: selectedAddressId.value,
        quantity: cartItem.quantity,
        remark: orderRemark.value
      })

      if (res.code === 200) {
        const { orderId } = res.data
        orderIds.push(orderId)
      } else {
        throw new Error(res.message || `商品"${cartItem.productName}"参团失败`)
      }
    }

    ElMessage.success('拼团结算成功，正在跳转支付...')

    // 跳转到支付页面
    if (orderIds.length === 1) {
      // 单个订单直接跳转支付
      router.push({
        path: '/payment',
        query: {
          orderId: orderIds[0],
          type: 'groupbuy'
        }
      })
    } else {
      // 多个订单跳转到订单列表
      router.push('/user/orders')
    }

  } catch (error) {
    console.error('拼团结算失败:', error)
    ElMessage.error(error.message || '拼团结算失败，请重试')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.replace('/login')
    return
  }

  if (isCartGroupBuy.value) {
    // 购物车拼团结算
    await Promise.all([loadUserAddresses(), loadCartItemsAndTeams()])
  } else {
    // 单个商品拼团
    await Promise.all([loadUserAddresses(), loadProductAndTeam()])
  }
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
.single-product {
  padding: 20px;
}

.single-product .product-main {
  display: flex;
  align-items: center;
  gap: 20px;
}

.single-product .product-image {
  width: 120px;
  height: 120px;
  border-radius: 12px;
  object-fit: cover;
  flex-shrink: 0;
}

.single-product .product-info {
  flex: 1;
}

.single-product .product-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  line-height: 1.4;
}

.single-product .product-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

.single-product .product-price {
  font-size: 20px;
  color: #f56c6c;
  font-weight: 600;
}

.single-product .group-tag {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
}

.single-product .product-detail {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #606266;
}

.single-product .detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 购物车商品列表 */
.cart-products {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cart-item {
  padding: 20px;
  background: #fafbfc;
  border-radius: 12px;
  border: 1px solid #f0f2f5;
}

.cart-item .product-main {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.cart-item .product-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.cart-item .product-info {
  flex: 1;
  min-width: 0;
}

.cart-item .product-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.4;
}

.cart-item .product-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.cart-item .original-price {
  color: #909399;
  text-decoration: line-through;
  font-size: 14px;
}

.cart-item .group-price {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

.cart-item .group-tag {
  background: #e1f3d8;
  color: #67c23a;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.cart-item .product-detail {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #606266;
}

.cart-item .detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.cart-item .team-selection {
  width: 280px;
  margin-left: 20px;
  flex-shrink: 0;
}

.cart-item .selection-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.cart-item .selection-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.cart-item .selected-team-info {
  margin-top: 8px;
  font-size: 12px;
}

.cart-item .team-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.cart-item .team-info {
  flex: 1;
}

.cart-item .team-no {
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}

.cart-item .team-leader {
  font-size: 12px;
  color: #909399;
}

.cart-item .team-progress {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.cart-item .progress-text {
  font-size: 12px;
  color: #606266;
}

/* 商品清单汇总 */
.product-summary {
  padding: 16px 20px;
  background: #f8f9fa;
  border-radius: 0 0 12px 12px;
  border-top: 1px solid #f0f2f5;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.discount-amount {
  color: #67c23a;
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

.group-buy-info {
  margin-bottom: 20px;
}

.info-section {
  margin-bottom: 16px;
}

.section-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  font-size: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-item .label {
  font-weight: 500;
  color: #303133;
  min-width: 80px;
  flex-shrink: 0;
}

.info-item .value {
  color: #606266;
  flex: 1;
}

.info-item .price {
  color: #f56c6c;
  font-weight: 600;
  font-size: 16px;
}

.progress-section {
  margin-top: 16px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.cart-summary {
  margin-bottom: 20px;
}

.summary-stats {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.stat-value.discount {
  color: #67c23a;
}

/* 费用明细 */
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

.group-buy-tips {
  margin-top: 8px;
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

  .single-product .product-main,
  .cart-item .product-main {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .single-product .product-image,
  .cart-item .product-image {
    width: 80px;
    height: 80px;
  }

  .cart-item .team-selection {
    width: 100%;
    margin-left: 0;
    margin-top: 16px;
  }

  .summary-stats {
    flex-direction: column;
    gap: 12px;
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



