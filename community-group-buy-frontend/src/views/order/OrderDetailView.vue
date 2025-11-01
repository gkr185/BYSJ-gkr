<template>
  <MainLayout>
    <div class="order-detail-page">
      <div class="container">
        <!-- 返回按钮 -->
        <div class="back-button">
          <el-button @click="router.back()" icon="ArrowLeft">
            返回订单列表
          </el-button>
        </div>

        <!-- 加载中 -->
        <el-card v-loading="loading">
          <!-- 订单状态卡片 -->
          <div class="status-card">
            <el-result
              :icon="getStatusIcon(orderDetail.orderStatus)"
              :title="orderDetail.orderStatusText"
            >
              <template #sub-title>
                <div class="status-subtitle">
                  <p v-if="orderDetail.orderStatus === 0">
                    请在{{ formatExpireTime(orderDetail.createTime) }}前完成支付
                  </p>
                  <p v-else-if="orderDetail.orderStatus === 1">
                    商家正在备货，请耐心等待
                  </p>
                  <p v-else-if="orderDetail.orderStatus === 2">
                    商品正在配送中，预计今天送达
                  </p>
                  <p v-else-if="orderDetail.orderStatus === 3">
                    交易已完成，感谢您的支持
                  </p>
                </div>
              </template>
              <template #extra>
                <div class="status-actions">
                  <el-button 
                    v-if="orderDetail.orderStatus === 0"
                    type="primary" 
                    size="large"
                    @click="handlePay"
                  >
                    立即支付
                  </el-button>
                  <el-button 
                    v-if="orderDetail.orderStatus === 0 || orderDetail.orderStatus === 1"
                    size="large"
                    @click="handleCancel"
                  >
                    取消订单
                  </el-button>
                  <el-button 
                    v-if="orderDetail.orderStatus === 2"
                    type="primary" 
                    size="large"
                    @click="handleConfirmReceipt"
                  >
                    确认收货
                  </el-button>
                </div>
              </template>
            </el-result>
          </div>

          <!-- 订单信息 -->
          <div class="section">
            <h3 class="section-title">订单信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="订单编号">
                {{ orderDetail.orderSn }}
              </el-descriptions-item>
              <el-descriptions-item label="订单状态">
                <el-tag :type="getStatusTagType(orderDetail.orderStatus)">
                  {{ orderDetail.orderStatusText }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="支付状态">
                <el-tag :type="orderDetail.payStatus === 1 ? 'success' : 'warning'">
                  {{ orderDetail.payStatus === 1 ? '已支付' : '未支付' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDateTime(orderDetail.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="支付时间">
                {{ orderDetail.payTime ? formatDateTime(orderDetail.payTime) : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="更新时间">
                {{ orderDetail.updateTime ? formatDateTime(orderDetail.updateTime) : '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 商品信息 -->
          <div class="section">
            <h3 class="section-title">商品信息</h3>
            <el-table :data="orderDetail.items" border>
              <el-table-column label="商品" min-width="300">
                <template #default="{ row }">
                  <div class="product-cell">
                    <el-image
                      :src="row.productImg"
                      fit="cover"
                      class="product-image"
                    >
                      <template #error>
                        <div class="image-placeholder">
                          <el-icon><Picture /></el-icon>
                        </div>
                      </template>
                    </el-image>
                    <div class="product-info">
                      <div class="product-name">{{ row.productName }}</div>
                      <div v-if="row.activityId" class="product-tag">
                        <el-tag size="small" type="danger">拼团商品</el-tag>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="单价" align="center" width="120">
                <template #default="{ row }">
                  <span class="price">¥{{ row.price }}</span>
                </template>
              </el-table-column>
              <el-table-column label="数量" prop="quantity" align="center" width="100" />
              <el-table-column label="小计" align="center" width="120">
                <template #default="{ row }">
                  <span class="price">¥{{ row.totalPrice }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 金额信息 -->
          <div class="section">
            <h3 class="section-title">金额信息</h3>
            <div class="amount-info">
              <div class="amount-row">
                <span class="label">商品总额：</span>
                <span class="value">¥{{ orderDetail.totalAmount }}</span>
              </div>
              <div class="amount-row" v-if="orderDetail.discountAmount > 0">
                <span class="label">优惠金额：</span>
                <span class="value discount">-¥{{ orderDetail.discountAmount }}</span>
              </div>
              <el-divider />
              <div class="amount-row total">
                <span class="label">实付金额：</span>
                <span class="value">¥{{ orderDetail.payAmount }}</span>
              </div>
            </div>
          </div>

          <!-- 收货信息 -->
          <div class="section">
            <h3 class="section-title">收货信息</h3>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="团长信息">
                {{ orderDetail.leaderName || '加载中...' }}
              </el-descriptions-item>
              <el-descriptions-item label="收货地址">
                {{ orderDetail.receiveAddress || '加载中...' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Picture } from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { 
  getOrderDetail, 
  cancelOrder, 
  confirmReceipt,
  ORDER_STATUS_TAG_TYPE
} from '@/api/order'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const orderDetail = ref({
  orderId: null,
  orderSn: '',
  totalAmount: 0,
  discountAmount: 0,
  payAmount: 0,
  orderStatus: 0,
  orderStatusText: '',
  payStatus: 0,
  leaderId: null,
  leaderName: null,
  receiveAddressId: null,
  receiveAddress: null,
  items: [],
  createTime: null,
  payTime: null,
  updateTime: null
})

// 获取订单详情
const fetchOrderDetail = async () => {
  try {
    loading.value = true
    const orderId = route.params.id
    const data = await getOrderDetail(orderId)
    orderDetail.value = data
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

// 立即支付
const handlePay = () => {
  router.push({
    name: 'payment',
    query: {
      orderId: orderDetail.value.orderId
    }
  })
}

// 取消订单
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelOrder(orderDetail.value.orderId)
    ElMessage.success('订单已取消')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
    }
  }
}

// 确认收货
const handleConfirmReceipt = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '提示', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await confirmReceipt(orderDetail.value.orderId)
    ElMessage.success('确认收货成功')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
    }
  }
}

// 获取状态图标
const getStatusIcon = (status) => {
  const iconMap = {
    0: 'warning',    // 待支付
    1: 'info',       // 待发货
    2: 'info',       // 配送中
    3: 'success',    // 已送达
    4: 'error',      // 已取消
    5: 'warning',    // 退款中
    6: 'info'        // 已退款
  }
  return iconMap[status] || 'info'
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  return ORDER_STATUS_TAG_TYPE[status] || 'info'
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 格式化过期时间
const formatExpireTime = (createTime) => {
  if (!createTime) return ''
  const create = new Date(createTime)
  const expire = new Date(create.getTime() + 30 * 60 * 1000) // 30分钟后过期
  const hours = String(expire.getHours()).padStart(2, '0')
  const minutes = String(expire.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

// 初始化
onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail-page {
  min-height: 100vh;
  padding: 20px 0;
  background-color: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.back-button {
  margin-bottom: 16px;
}

/* 状态卡片 */
.status-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  padding: 30px 20px;
  margin-bottom: 24px;
  color: #fff;
}

.status-card :deep(.el-result__icon svg) {
  color: #fff;
}

.status-card :deep(.el-result__title p) {
  color: #fff;
  font-size: 24px;
}

.status-subtitle {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  margin-top: 8px;
}

.status-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 16px;
}

/* 区块 */
.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #409eff;
}

/* 商品单元格 */
.product-cell {
  display: flex;
  gap: 12px;
  align-items: center;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  flex-shrink: 0;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #c0c4cc;
  font-size: 20px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  margin-bottom: 4px;
}

.product-tag {
  margin-top: 4px;
}

.price {
  color: #f56c6c;
  font-weight: 500;
}

/* 金额信息 */
.amount-info {
  background-color: #f9fafc;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
}

.amount-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
}

.amount-row .label {
  color: #606266;
}

.amount-row .value {
  color: #303133;
  font-weight: 500;
}

.amount-row .discount {
  color: #67c23a;
}

.amount-row.total {
  padding-top: 16px;
  font-size: 16px;
}

.amount-row.total .value {
  color: #f56c6c;
  font-size: 24px;
  font-weight: bold;
}
</style>

