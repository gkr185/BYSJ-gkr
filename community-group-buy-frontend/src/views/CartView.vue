<template>
  <MainLayout>
    <div class="cart-page">
      <div class="container">
        <h1 class="page-title">购物车</h1>

        <div v-if="cartStore.items.length > 0" class="cart-content">
          <!-- 购物车列表 -->
          <el-card class="cart-card">
            <!-- 表头 -->
            <div class="cart-header">
              <el-checkbox
                :model-value="cartStore.isAllSelected"
                @change="cartStore.toggleAllCheck"
              >
                全选
              </el-checkbox>
              <span class="header-item product-col">商品信息</span>
              <span class="header-item price-col">单价</span>
              <span class="header-item quantity-col">数量</span>
              <span class="header-item total-col">小计</span>
              <span class="header-item action-col">操作</span>
            </div>

            <!-- 商品列表 -->
            <div class="cart-items">
              <div
                v-for="item in cartStore.items"
                :key="item.productId"
                class="cart-item"
              >
                <el-checkbox
                  :model-value="item.checked"
                  @change="cartStore.toggleItemCheck(item.productId)"
                />
                
                <!-- 商品信息 -->
                <div class="product-info">
                  <img :src="item.coverImg" :alt="item.productName" class="product-img" />
                  <div class="product-detail">
                    <div class="product-name" @click="goToProduct(item.productId)">
                      {{ item.productName }}
                    </div>
                    <el-tag v-if="item.groupPrice" type="danger" size="small">拼团</el-tag>
                  </div>
                </div>

                <!-- 单价 -->
                <div class="item-price">
                  <div v-if="item.groupPrice" class="group-price">¥{{ item.groupPrice }}</div>
                  <div :class="['original-price', item.groupPrice ? 'has-group' : '']">
                    ¥{{ item.price }}
                  </div>
                </div>

                <!-- 数量 -->
                <div class="item-quantity">
                  <el-input-number
                    :model-value="item.quantity"
                    :min="1"
                    :max="item.stock"
                    @change="(value) => cartStore.updateQuantity(item.productId, value)"
                  />
                  <div class="stock-hint">库存：{{ item.stock }}</div>
                </div>

                <!-- 小计 -->
                <div class="item-total">
                  ¥{{ calculateItemTotal(item).toFixed(2) }}
                </div>

                <!-- 操作 -->
                <div class="item-action">
                  <el-button
                    text
                    type="danger"
                    @click="handleRemove(item.productId)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 结算栏 -->
          <el-card class="settlement-card">
            <div class="settlement-content">
              <div class="settlement-info">
                <div class="info-item">
                  <span>已选商品：</span>
                  <span class="value">{{ cartStore.selectedCount }} 件</span>
                </div>
                <div class="info-item">
                  <span>合计：</span>
                  <span class="total-price">¥{{ cartStore.selectedTotalPrice.toFixed(2) }}</span>
                </div>
              </div>
              <div class="settlement-actions">
                <el-button @click="handleClearSelected">清空选中</el-button>
                <el-button
                  type="primary"
                  size="large"
                  @click="handleCheckout"
                  :disabled="cartStore.selectedCount === 0"
                >
                  去结算（{{ cartStore.selectedCount }}）
                </el-button>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 空购物车 -->
        <el-card v-else class="empty-cart">
          <el-empty description="购物车是空的">
            <el-button type="primary" @click="router.push('/products')">
              去逛逛
            </el-button>
          </el-empty>
        </el-card>
      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import { useRouter } from 'vue-router'
import MainLayout from '@/components/common/MainLayout.vue'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 计算单个商品小计
const calculateItemTotal = (item) => {
  const price = item.groupPrice || item.price
  return price * item.quantity
}

// 跳转到商品详情
const goToProduct = (productId) => {
  router.push(`/products/${productId}`)
}

// 删除商品
const handleRemove = (productId) => {
  ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
    type: 'warning'
  }).then(() => {
    cartStore.removeItem(productId)
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 清空选中
const handleClearSelected = () => {
  if (cartStore.selectedCount === 0) {
    ElMessage.warning('请先选择商品')
    return
  }

  ElMessageBox.confirm(`确定要清空选中的 ${cartStore.selectedCount} 件商品吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    cartStore.removeSelectedItems()
    ElMessage.success('清空成功')
  }).catch(() => {})
}

// 去结算
const handleCheckout = () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (cartStore.selectedCount === 0) {
    ElMessage.warning('请先选择商品')
    return
  }

  // TODO: 跳转到订单确认页面
  ElMessage.info('订单功能正在开发中，敬请期待！')
}
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  padding: 20px 0;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.cart-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 购物车列表卡片 */
.cart-card {
  overflow: hidden;
}

.cart-header {
  display: grid;
  grid-template-columns: 50px 1fr 150px 180px 150px 100px;
  align-items: center;
  padding: 16px;
  background-color: #f5f7fa;
  font-weight: 500;
  color: #606266;
}

.header-item {
  text-align: center;
}

.product-col {
  text-align: left;
  padding-left: 20px;
}

/* 商品列表 */
.cart-items {
  display: flex;
  flex-direction: column;
}

.cart-item {
  display: grid;
  grid-template-columns: 50px 1fr 150px 180px 150px 100px;
  align-items: center;
  padding: 20px 16px;
  border-bottom: 1px solid #ebeef5;
}

.cart-item:last-child {
  border-bottom: none;
}

/* 商品信息 */
.product-info {
  display: flex;
  gap: 16px;
  align-items: center;
}

.product-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  background-color: #f5f5f5;
  flex-shrink: 0;
}

.product-detail {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-name {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: color 0.3s;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-name:hover {
  color: #409EFF;
}

/* 价格 */
.item-price {
  text-align: center;
}

.group-price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.original-price {
  font-size: 16px;
  color: #333;
}

.original-price.has-group {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}

/* 数量 */
.item-quantity {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stock-hint {
  font-size: 12px;
  color: #999;
}

/* 小计 */
.item-total {
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

/* 操作 */
.item-action {
  text-align: center;
}

/* 结算栏 */
.settlement-card {
  position: sticky;
  bottom: 0;
  background-color: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

.settlement-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}

.settlement-info {
  display: flex;
  gap: 40px;
  align-items: center;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.value {
  font-weight: bold;
  color: #409EFF;
}

.total-price {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.settlement-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

/* 空购物车 */
.empty-cart {
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 1024px) {
  .cart-header,
  .cart-item {
    grid-template-columns: 40px 1fr 120px 150px 120px 80px;
    font-size: 14px;
  }

  .product-img {
    width: 60px;
    height: 60px;
  }

  .settlement-content {
    flex-direction: column;
    gap: 20px;
  }

  .settlement-actions {
    width: 100%;
  }

  .settlement-actions .el-button {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .cart-header {
    display: none;
  }

  .cart-item {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 16px;
  }

  .item-price,
  .item-quantity,
  .item-total,
  .item-action {
    text-align: left;
  }

  .settlement-info {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
    width: 100%;
  }
}
</style>

