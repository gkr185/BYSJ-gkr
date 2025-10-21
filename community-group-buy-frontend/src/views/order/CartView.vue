<template>
  <div class="cart-page">
    <div class="cart-container">
    <h1 class="page-title">购物车</h1>

    <div v-if="cartItems.length > 0" class="cart-content">
      <el-card class="cart-table-card">
        <el-table :data="cartItems" style="width: 100%">
          <el-table-column width="60" align="center">
            <template #default="{ row }">
              <el-checkbox v-model="row.selected" @change="updateTotal" />
            </template>
          </el-table-column>

          <el-table-column label="商品" min-width="300">
            <template #default="{ row }">
              <div class="product-cell">
                <img :src="row.cover_img" :alt="row.product_name" class="product-image" />
                <div class="product-info">
                  <div class="product-name">{{ row.product_name }}</div>
                  <el-tag v-if="row.group_price" type="danger" size="small">拼团价</el-tag>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="单价" width="120" align="center">
            <template #default="{ row }">
              <span class="price">¥{{ row.group_price || row.price }}</span>
            </template>
          </el-table-column>

          <el-table-column label="数量" width="180" align="center">
            <template #default="{ row }">
              <el-input-number
                v-model="row.quantity"
                :min="1"
                :max="row.stock"
                size="small"
                @change="handleQuantityChange(row)"
              />
            </template>
          </el-table-column>

          <el-table-column label="小计" width="120" align="center">
            <template #default="{ row }">
              <span class="subtotal">¥{{ calculateSubtotal(row) }}</span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="100" align="center">
            <template #default="{ row }">
              <el-button
                type="danger"
                text
                :icon="Delete"
                @click="handleRemove(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card class="cart-summary-card">
        <div class="cart-actions">
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <el-button text type="danger" @click="handleClearCart">清空购物车</el-button>
        </div>
        
        <div class="cart-total">
          <div class="total-info">
            <span class="total-label">已选商品:</span>
            <span class="total-count">{{ selectedCount }} 件</span>
          </div>
          <div class="total-info">
            <span class="total-label">合计:</span>
            <span class="total-amount">¥{{ totalAmount }}</span>
          </div>
          <el-button
            type="danger"
            size="large"
            :disabled="selectedCount === 0"
            @click="handleCheckout"
          >
            去结算 ({{ selectedCount }})
          </el-button>
        </div>
      </el-card>
    </div>

    <el-empty v-else description="购物车是空的" :image-size="200">
      <el-button type="primary" @click="router.push('/products')">去逛逛</el-button>
    </el-empty>
  </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCart,
  updateCartItemQuantity,
  removeCartItem,
  clearCart,
  calculateTotal
} from '@/utils/cart'

const router = useRouter()

// 数据
const cartItems = ref([])
const selectAll = ref(false)

// 加载购物车数据
const loadCart = () => {
  const cart = getCart()
  // 给每个商品添加selected属性
  cartItems.value = cart.map(item => ({
    ...item,
    selected: false
  }))
}

// 已选商品数量
const selectedCount = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((count, item) => count + item.quantity, 0)
})

// 总金额
const totalAmount = computed(() => {
  const selectedItems = cartItems.value.filter(item => item.selected)
  return calculateTotal(selectedItems).toFixed(2)
})

// 计算小计
const calculateSubtotal = (item) => {
  const price = item.group_price || item.price
  return (price * item.quantity).toFixed(2)
}

// 更新总价
const updateTotal = () => {
  // 检查是否全选
  selectAll.value = cartItems.value.every(item => item.selected)
}

// 全选/取消全选
const handleSelectAll = () => {
  cartItems.value.forEach(item => {
    item.selected = selectAll.value
  })
}

// 数量变化
const handleQuantityChange = (item) => {
  updateCartItemQuantity(item.product_id, item.quantity)
  ElMessage.success('已更新数量')
}

// 删除商品
const handleRemove = (item) => {
  ElMessageBox.confirm(
    `确定要删除 "${item.product_name}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    removeCartItem(item.product_id)
    loadCart()
    ElMessage.success('已删除')
  }).catch(() => {
    // 取消删除
  })
}

// 清空购物车
const handleClearCart = () => {
  if (cartItems.value.length === 0) {
    ElMessage.warning('购物车已经是空的')
    return
  }
  
  ElMessageBox.confirm(
    '确定要清空购物车吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    clearCart()
    loadCart()
    ElMessage.success('购物车已清空')
  }).catch(() => {
    // 取消清空
  })
}

// 去结算
const handleCheckout = () => {
  if (selectedCount.value === 0) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }
  
  // 跳转到订单确认页面
  router.push('/order/confirm')
}

// 监听购物车更新事件
const handleCartUpdate = () => {
  loadCart()
}

onMounted(() => {
  loadCart()
  window.addEventListener('cart-updated', handleCartUpdate)
})

onUnmounted(() => {
  window.removeEventListener('cart-updated', handleCartUpdate)
})
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  padding-top: 84px; /* 64px导航栏 + 20px间隔 */
  background-color: #f5f5f5;
}

.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 20px 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

.cart-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cart-table-card {
  margin-bottom: 0;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  background-color: #f5f5f5;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  line-height: 1.4;
}

.price {
  font-size: 16px;
  color: #F56C6C;
  font-weight: bold;
}

.subtotal {
  font-size: 16px;
  color: #F56C6C;
  font-weight: bold;
}

.cart-summary-card {
  position: sticky;
  bottom: 20px;
}

.cart-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e5e5;
  margin-bottom: 16px;
}

.cart-total {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 30px;
}

.total-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.total-label {
  font-size: 14px;
  color: #666;
}

.total-count {
  font-size: 16px;
  color: #333;
  font-weight: bold;
}

.total-amount {
  font-size: 24px;
  color: #F56C6C;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .cart-page {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }
  
  .cart-total {
    flex-direction: column;
    align-items: flex-end;
    gap: 12px;
  }
}
</style>

