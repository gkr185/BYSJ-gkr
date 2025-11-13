<template>
  <MainLayout>
    <div class="cart-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">
          <el-icon><ShoppingCart /></el-icon>
          购物车
        </h1>
        <div class="page-subtitle">
          <span>共 {{ totalCount }} 件商品</span>
        </div>
      </div>

      <!-- 购物车为空 -->
      <div v-if="cartItems.length === 0" class="empty-cart">
        <div class="empty-content">
          <el-empty
            description="购物车空空如也"
            :image="emptyImage"
          >
            <template #image>
              <el-icon size="80" class="empty-icon">
                <ShoppingCart />
              </el-icon>
            </template>
            <el-button type="primary" @click="$router.push('/products')">
              去逛逛
            </el-button>
          </el-empty>
        </div>
      </div>

      <!-- 购物车内容 -->
      <div v-else class="cart-content">
        <!-- 购物车商品列表 -->
        <div class="cart-main">
          <!-- 操作栏 -->
          <div class="cart-toolbar">
            <div class="select-all">
              <el-checkbox
                v-model="selectAllChecked"
                @change="handleToggleAll"
                :indeterminate="isIndeterminate"
              >
                全选
              </el-checkbox>
            </div>
            <div class="toolbar-actions">
              <el-button
                type="danger"
                plain
                size="small"
                :disabled="selectedCount === 0"
                @click="handleBatchRemove"
                :loading="loading"
              >
                批量删除
              </el-button>
              <el-button
                type="warning"
                plain
                size="small"
                @click="handleClearCart"
                :loading="loading"
              >
                清空购物车
              </el-button>
            </div>
          </div>

          <!-- 商品列表 -->
          <div class="cart-items">
            <div
              v-for="item in cartItems"
              :key="item.cartId"
              class="cart-item"
            >
              <div class="item-check">
                <el-checkbox
                  v-model="item.checked"
                  @change="handleToggleItem(item.cartId)"
                />
              </div>

              <div class="item-image">
                <el-image
                  :src="item.productImg"
                  :alt="item.productName"
                  fit="cover"
                  class="product-image"
                  @click="$router.push(`/products/${item.productId}`)"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
              </div>

              <div class="item-info">
                <div class="product-name">
                  <span @click="$router.push(`/products/${item.productId}`)">
                    {{ item.productName }}
                  </span>
                  <el-tag
                    v-if="item.activityId"
                    type="danger"
                    size="small"
                    class="group-tag"
                  >
                    拼团
                  </el-tag>
                </div>
                <div class="product-meta">
                  <span class="stock-info">
                    库存: {{ item.stock }}
                    <el-tag
                      :type="item.stock > 0 ? 'success' : 'danger'"
                      size="small"
                    >
                      {{ item.stock > 0 ? '有货' : '缺货' }}
                    </el-tag>
                  </span>
                </div>
              </div>

              <div class="item-price">
                <div class="price-info">
                  <div v-if="item.activityId" class="group-price">
                    <span class="original-price">¥{{ item.price }}</span>
                    <span class="current-price">¥{{ item.groupPrice }}</span>
                  </div>
                  <div v-else class="normal-price">
                    ¥{{ item.price }}
                  </div>
                </div>
              </div>

              <div class="item-quantity">
                <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="item.stock"
                  size="small"
                  @change="handleQuantityChange(item.cartId, item.quantity)"
                  :disabled="item.stock <= 0"
                  :loading="item.updating"
                />
              </div>

              <div class="item-subtotal">
                ¥{{ calculateItemSubtotal(item) }}
              </div>

              <div class="item-actions">
                <el-button
                  type="danger"
                  size="small"
                  text
                  @click="handleRemoveItem(item.cartId)"
                  :loading="item.deleting"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 购物车结算栏 -->
        <div class="cart-sidebar">
          <div class="settlement-card">
            <h3 class="settlement-title">结算信息</h3>

            <div class="settlement-content">
              <div class="settlement-row">
                <span>已选择 {{ selectedCount }} 件商品</span>
              </div>

              <div class="settlement-row total-row">
                <span>商品总价：</span>
                <span class="total-price">¥{{ formatPrice(selectedTotalPrice) }}</span>
              </div>

              <div class="settlement-actions">
                <!-- 直接结算按钮 -->
                <el-button
                  type="primary"
                  size="large"
                  :disabled="selectedCount === 0"
                  @click="handleDirectCheckout"
                  :loading="directCheckoutLoading"
                  class="direct-checkout-btn"
                >
                  <el-icon><ShoppingCart /></el-icon>
                  直接结算 ({{ selectedCount }})
                </el-button>

                <!-- 拼团结算按钮（醒目提示） -->
                <el-button
                  type="danger"
                  size="large"
                  :disabled="selectedCount === 0"
                  @click="handleGroupBuyCheckout"
                  :loading="groupBuyCheckoutLoading"
                  class="groupbuy-checkout-btn"
                >
                  <el-icon><UserFilled /></el-icon>
                  <span class="groupbuy-text">拼团结算</span>
                  <span class="groupbuy-badge">省钱</span>
                </el-button>

                <el-button
                  size="large"
                  @click="$router.push('/products')"
                  class="continue-shopping-btn"
                >
                  继续购物
                </el-button>
              </div>
            </div>
          </div>

          <!-- 结算提示 -->
          <div class="settlement-tips">
            <el-alert
              title="温馨提示"
              type="info"
              :closable="false"
              show-icon
            >
              <template #description>
                <ul>
                  <li>商品价格以结算时为准</li>
                  <li>部分商品可能存在库存限制</li>
                  <li>结算后将跳转至订单确认页面</li>
                </ul>
              </template>
            </el-alert>
          </div>
        </div>
      </div>

    </div>
  </MainLayout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import MainLayout from '@/components/common/MainLayout.vue'
import {
  getCartList,
  updateCartQuantity,
  removeFromCart,
  batchRemoveCart,
  checkoutCart
} from '@/api/cart'
import { getProductGroupBuyActivities, joinTeam } from '@/api/groupbuy'
import { getUserAddresses } from '@/api/user'
import { getCommunityLeaders } from '@/api/leader'
import {
  ShoppingCart,
  Picture,
  Delete,
  Plus,
  Minus,
  UserFilled
} from '@element-plus/icons-vue'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const directCheckoutLoading = ref(false)
const groupBuyCheckoutLoading = ref(false)
const userAddresses = ref([])
const nearbyLeaders = ref([])

// 全选状态控制
const selectAllChecked = ref(false)

// 购物车数据（从API获取）
const cartItems = ref([])
const selectedCartIds = ref([])

// 结算表单
const checkoutForm = ref({
  addressId: null,
  leaderId: null
})

// 选中的购物车项（用于结算）
const selectedCartItems = ref([])

// 计算属性
const selectedCount = computed(() => selectedCartIds.value.length)
const totalCount = computed(() => cartItems.value.length)
const isAllSelected = computed(() => {
  return totalCount.value > 0 && selectedCount.value === totalCount.value
})
const isIndeterminate = computed(() => {
  return selectedCount.value > 0 && selectedCount.value < totalCount.value
})
const selectedTotalPrice = computed(() => {
  return cartItems.value
    .filter(item => selectedCartIds.value.includes(item.cartId))
    .reduce((total, item) => total + item.subtotal, 0)
})


// 空状态图片
const emptyImage = ref('')

// 方法
const formatPrice = (price) => {
  return (Math.round(price * 100) / 100).toFixed(2)
}

const calculateItemSubtotal = (item) => {
  return formatPrice(item.subtotal)
}

// 加载购物车数据
const loadCartData = async () => {
  // 检查用户登录状态
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  loading.value = true
  try {
    const userId = userStore.userInfo.userId
    const response = await getCartList(userId)
    cartItems.value = response.data || []
    // 初始化选中状态
    selectedCartIds.value = []
    // 为每个商品添加loading状态
    cartItems.value.forEach(item => {
      item.checked = false
      item.updating = false
      item.deleting = false
    })
  } catch (error) {
    console.error('加载购物车失败:', error)
    ElMessage.error('加载购物车失败')
    cartItems.value = []
  } finally {
    loading.value = false
  }
}

// 更新商品数量
const handleQuantityChange = async (cartId, quantity) => {
  const item = cartItems.value.find(item => item.cartId === cartId)
  if (!item) return

  const oldQuantity = item.quantity
  item.updating = true

  try {
    const userId = userStore.userInfo.userId
    await updateCartQuantity(userId, cartId, quantity)
    item.quantity = quantity
    item.subtotal = item.price * quantity // 重新计算小计
    ElMessage.success('数量更新成功')
  } catch (error) {
    console.error('更新数量失败:', error)
    item.quantity = oldQuantity // 恢复原数量
    ElMessage.error('更新数量失败')
  } finally {
    item.updating = false
  }
}

// 删除单个商品
const handleRemoveItem = async (cartId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个商品吗？', '提示', {
      type: 'warning'
    })

    const item = cartItems.value.find(item => item.cartId === cartId)
    if (!item) return

    item.deleting = true

    const userId = userStore.userInfo.userId
    await removeFromCart(userId, cartId)
    // 从列表中移除
    const index = cartItems.value.findIndex(item => item.cartId === cartId)
    if (index > -1) {
      cartItems.value.splice(index, 1)
    }
    // 从选中列表中移除
    const selectedIndex = selectedCartIds.value.indexOf(cartId)
    if (selectedIndex > -1) {
      selectedCartIds.value.splice(selectedIndex, 1)
    }
    
    // 同步更新 cartStore 状态，确保导航栏角标更新
    cartStore.loadCart()

    ElMessage.success('商品已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
      ElMessage.error('删除商品失败')
    }
  } finally {
    const item = cartItems.value.find(item => item.cartId === cartId)
    if (item) {
      item.deleting = false
    }
  }
}

// 全选/取消全选
const handleToggleAll = () => {
  if (selectAllChecked.value) {
    // 全选
    selectedCartIds.value = cartItems.value.map(item => item.cartId)
    cartItems.value.forEach(item => {
      item.checked = true
    })
  } else {
    // 取消全选
    selectedCartIds.value = []
    cartItems.value.forEach(item => {
      item.checked = false
    })
  }
}

// 切换单个商品选中状态
const handleToggleItem = (cartId) => {
  const index = selectedCartIds.value.indexOf(cartId)
  const item = cartItems.value.find(item => item.cartId === cartId)

  if (index > -1) {
    // 取消选中
    selectedCartIds.value.splice(index, 1)
    item.checked = false
  } else {
    // 选中
    selectedCartIds.value.push(cartId)
    item.checked = true
  }
}

// 批量删除
const handleBatchRemove = async () => {
  if (selectedCartIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的商品')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedCartIds.value.length} 个商品吗？`,
      '批量删除',
      { type: 'warning' }
    )

    loading.value = true
    const userId = userStore.userInfo.userId
    await batchRemoveCart(userId, selectedCartIds.value)

    // 从列表中移除选中的商品
    cartItems.value = cartItems.value.filter(item =>
      !selectedCartIds.value.includes(item.cartId)
    )
    selectedCartIds.value = []
    
    // 同步更新 cartStore 状态，确保导航栏角标更新
    cartStore.loadCart()

    ElMessage.success('批量删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  } finally {
    loading.value = false
  }
}

// 清空购物车
const handleClearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '清空购物车', {
      type: 'warning'
    })

    loading.value = true

    // 批量删除所有商品
    const userId = userStore.userInfo.userId
    const allCartIds = cartItems.value.map(item => item.cartId)
    await batchRemoveCart(userId, allCartIds)

    cartItems.value = []
    selectedCartIds.value = []
    
    // 同步更新 cartStore 状态，确保导航栏角标归零
    cartStore.clearCart()

    ElMessage.success('购物车已清空')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空购物车失败:', error)
      ElMessage.error('清空购物车失败')
    }
  } finally {
    loading.value = false
  }
}

// 直接结算 - 跳转到直接结算确认订单页面
const handleDirectCheckout = async () => {
  if (selectedCartIds.value.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }

  // 检查是否有缺货商品
  const selectedItems = cartItems.value.filter(item =>
    selectedCartIds.value.includes(item.cartId)
  )
  const outOfStockItems = selectedItems.filter(item => item.stock <= 0)
  if (outOfStockItems.length > 0) {
    ElMessage.warning('选中商品中包含缺货商品，请先移除')
    return
  }

  directCheckoutLoading.value = true
  try {
    // 设置选中的购物车项
    selectedCartItems.value = selectedItems

    // 加载结算数据（地址和团长信息）
    await loadCheckoutData()

    // 跳转到直接结算确认订单页面
    router.push({
      path: '/order/confirm/direct',
      query: {
        cartIds: selectedCartIds.value.join(','),
        addressId: checkoutForm.value.addressId,
        leaderId: checkoutForm.value.leaderId
      }
    })
  } catch (error) {
    console.error('跳转直接结算确认页面失败:', error)
    ElMessage.error('跳转结算页面失败')
  } finally {
    directCheckoutLoading.value = false
  }
}

// 拼团结算 - 跳转到团购确认订单页面
const handleGroupBuyCheckout = async () => {
  if (selectedCartIds.value.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }

  // 检查是否有缺货商品
  const selectedItems = cartItems.value.filter(item =>
    selectedCartIds.value.includes(item.cartId)
  )
  const outOfStockItems = selectedItems.filter(item => item.stock <= 0)
  if (outOfStockItems.length > 0) {
    ElMessage.warning('选中商品中包含缺货商品，请先移除')
    return
  }

  groupBuyCheckoutLoading.value = true
  try {
    // 设置选中的购物车项
    selectedCartItems.value = selectedItems

    // 加载结算数据（地址和团长信息）
    await loadCheckoutData()

    // 跳转到团购确认订单页面（复用现有的拼团确认页面）
    router.push({
      path: '/order/confirm/groupbuy',
      query: {
        cartIds: selectedCartIds.value.join(','),
        addressId: checkoutForm.value.addressId,
        leaderId: checkoutForm.value.leaderId
      }
    })
  } catch (error) {
    console.error('跳转拼团结算确认页面失败:', error)
    ElMessage.error('跳转结算页面失败')
  } finally {
    groupBuyCheckoutLoading.value = false
  }
}

// 加载结算数据
const loadCheckoutData = async () => {
  try {
    // 加载用户地址
    const addressRes = await getUserAddresses(userStore.userInfo.userId)
    if (addressRes.code === 200) {
      userAddresses.value = addressRes.data || []
      // 默认选中第一个地址
      if (userAddresses.value.length > 0) {
        checkoutForm.value.addressId = userAddresses.value[0].addressId
      }
    }
  } catch (error) {
    console.error('加载地址失败:', error)
    // 使用模拟数据作为fallback
    userAddresses.value = [
      {
        addressId: 1,
        receiverName: '张三',
        receiverPhone: '13800138001',
        province: '北京市',
        city: '朝阳区',
        district: '某某街道',
        detailAddress: '某某小区1号楼101室'
      },
      {
        addressId: 2,
        receiverName: '李四',
        receiverPhone: '13800138002',
        province: '北京市',
        city: '朝阳区',
        district: '某某街道',
        detailAddress: '某某小区2号楼202室'
      }
    ]
  }

  // 加载社区团长列表
  try {
    const communityId = userStore.userInfo?.communityId
    if (communityId) {
      const leadersRes = await getCommunityLeaders(communityId)
      if (leadersRes.code === 200) {
        nearbyLeaders.value = leadersRes.data || []
        console.log('✅ 加载团长列表成功:', nearbyLeaders.value.length, '个团长')

        // 默认选中第一个团长
        if (nearbyLeaders.value.length > 0) {
          checkoutForm.value.leaderId = nearbyLeaders.value[0].leaderId
        }
      }
    } else {
      console.warn('用户未关联社区，无法加载团长列表')
    }
  } catch (error) {
    console.error('加载团长列表失败:', error)
    // 使用模拟数据作为fallback（仅用于测试）
    nearbyLeaders.value = [
      {
        storeId: 1,
        storeName: '张团长的小店',
        distance: 0.5
      },
      {
        storeId: 2,
        storeName: '李团长的便利店',
        distance: 1.2
      }
    ]
    console.log('⚠️ 使用模拟团长数据作为fallback')
  }
}

const handleAddressChange = (addressId) => {
  // 根据地址重新计算附近团长（实际应该调用API）
  console.log('地址改变:', addressId)
}

const handleLeaderChange = (leaderId) => {
  console.log('团长改变:', leaderId)
  checkoutForm.value.leaderId = leaderId
}


// 监听选择状态变化，同步全选checkbox状态
watch([selectedCartIds, cartItems], () => {
  if (totalCount.value === 0) {
    selectAllChecked.value = false
  } else if (selectedCount.value === totalCount.value) {
    selectAllChecked.value = true
  } else {
    selectAllChecked.value = false
  }
}, { immediate: true })

// 生命周期
onMounted(() => {
  loadCartData()
})
</script>

<style scoped>
.cart-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  min-height: calc(100vh - 200px);
}

/* 页面标题 */
.page-header {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-title .el-icon {
  color: #409EFF;
}

.page-subtitle {
  color: #909399;
  font-size: 14px;
}

/* 空购物车 */
.empty-cart {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.empty-content {
  text-align: center;
}

.empty-icon {
  color: #c0c4cc;
  margin-bottom: 20px;
}

/* 购物车内容 */
.cart-content {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.cart-main {
  flex: 1;
}

/* 工具栏 */
.cart-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #e9ecef;
}

.select-all {
  font-size: 16px;
  font-weight: 500;
}

.toolbar-actions {
  display: flex;
  gap: 12px;
}

/* 商品列表 */
.cart-items {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  overflow: hidden;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s;
}

.cart-item:hover {
  background-color: #fafafa;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-check {
  margin-right: 20px;
}

.item-image {
  margin-right: 16px;
  cursor: pointer;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  transition: transform 0.3s;
}

.product-image:hover {
  transform: scale(1.05);
}

.image-error {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.image-error .el-icon {
  color: #c0c4cc;
}

.item-info {
  flex: 1;
  margin-right: 20px;
}

.product-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  cursor: pointer;
  transition: color 0.3s;
}

.product-name:hover {
  color: #409EFF;
}

.group-tag {
  font-size: 12px;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: #909399;
}

.stock-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.item-price {
  width: 120px;
  text-align: center;
  margin-right: 20px;
}

.price-info {
  font-size: 16px;
  font-weight: 500;
}

.group-price .original-price {
  text-decoration: line-through;
  color: #909399;
  font-size: 14px;
  margin-right: 8px;
}

.group-price .current-price {
  color: #f56c6c;
}

.normal-price {
  color: #303133;
}

.item-quantity {
  width: 120px;
  text-align: center;
  margin-right: 20px;
}

.item-subtotal {
  width: 100px;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-right: 20px;
}

.item-actions {
  width: 80px;
  text-align: center;
}

/* 侧边栏 */
.cart-sidebar {
  width: 350px;
  flex-shrink: 0;
}

.settlement-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  padding: 24px;
  margin-bottom: 20px;
}

.settlement-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
  padding-bottom: 16px;
  border-bottom: 1px solid #e9ecef;
}

.settlement-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.settlement-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.total-row {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  padding-top: 12px;
  border-top: 1px solid #e9ecef;
}

.total-price {
  font-size: 20px;
  font-weight: 600;
  color: #f56c6c;
}

.settlement-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.checkout-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  transition: all 0.3s;
}

.checkout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);
}

.checkout-btn:disabled {
  background: #c0c4cc;
  transform: none;
}

.direct-checkout-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  transition: all 0.3s;
  margin-right: 12px;
  margin-left: 12px;
}

.direct-checkout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(102, 126, 234, 0.3);
}

.direct-checkout-btn:disabled {
  background: #c0c4cc;
  transform: none;
}

.groupbuy-checkout-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
  border: none;
  border-radius: 8px;
  transition: all 0.3s;
  margin-right: 12px;
  margin-left: 12px;
  position: relative;
  overflow: hidden;
}

.groupbuy-checkout-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(255, 107, 107, 0.3);
}

.groupbuy-checkout-btn:disabled {
  background: #c0c4cc;
  transform: none;
}

.groupbuy-text {
  margin-right: 8px;
}

.groupbuy-badge {
  background: #fff;
  color: #ff6b6b;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: bold;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.continue-shopping-btn {
  height: 44px;
  font-size: 14px;
  border: 2px solid #409EFF;
  color: #409EFF;
  background: transparent;
  border-radius: 8px;
  transition: all 0.3s;
  margin-right: 12px;
}

.continue-shopping-btn:hover {
  background: #409EFF;
  color: #fff;
  transform: translateY(-1px);
}

/* 结算提示 */
.settlement-tips {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  overflow: hidden;
}

.settlement-tips :deep(.el-alert) {
  border-radius: 8px;
  margin-bottom: 0;
}

.settlement-tips ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
}

.settlement-tips li {
  margin-bottom: 4px;
  color: #606266;
  font-size: 14px;
}

/* 结算对话框 */
.checkout-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.checkout-summary {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-top: 10px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.summary-item.total {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 0;
  padding-top: 12px;
  border-top: 1px solid #e9ecef;
}

.total-amount {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

.savings-amount {
  font-size: 16px;
  font-weight: 600;
  color: #67c23a;
}

/* 结算方式选择 */
.settlement-type-section {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.type-options {
  display: flex;
  gap: 20px;
}

.type-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.type-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
}

.type-desc {
  font-size: 13px;
  color: #909399;
}

/* 拼团结算选择 */
.groupbuy-selection {
  border: 1px solid #ffccc7;
  border-radius: 8px;
  padding: 16px;
  background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
}

.groupbuy-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.groupbuy-item {
  background: #fff;
  border: 1px solid #ffccc7;
  border-radius: 8px;
  padding: 16px;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.item-thumb {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  flex-shrink: 0;
}

.item-text {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 14px;
}

.group-price {
  color: #f56c6c;
  font-weight: 600;
}

.original-price {
  color: #909399;
  text-decoration: line-through;
}

.team-selection {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.selection-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.groupbuy-warning {
  margin-top: 16px;
}

/* 收货信息 */
.delivery-section {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cart-container {
    padding: 10px;
  }

  .cart-content {
    flex-direction: column;
    gap: 20px;
  }

  .cart-sidebar {
    width: 100%;
  }

  .cart-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 16px;
  }

  .item-check,
  .item-image,
  .item-price,
  .item-quantity,
  .item-subtotal,
  .item-actions {
    margin-right: 0;
  }

  .cart-toolbar {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .toolbar-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .checkout-form {
    gap: 16px;
  }
}

@media (max-width: 480px) {
  .page-title {
    font-size: 24px;
  }

  .cart-item {
    padding: 12px;
  }

  .product-name {
    font-size: 14px;
  }

  .settlement-card {
    padding: 16px;
  }

  .checkout-btn,
  .continue-shopping-btn {
    height: 44px;
  }
}
</style>
