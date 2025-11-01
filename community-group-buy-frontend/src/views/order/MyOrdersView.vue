<template>
  <MainLayout>
    <div class="orders-page">
      <div class="container">
        <h1 class="page-title">我的订单</h1>

        <!-- 订单列表 -->
        <el-card v-loading="loading">
          <!-- 空状态 -->
          <div v-if="!loading && orderList.length === 0" class="empty-state">
            <el-icon :size="80" color="#909399"><Document /></el-icon>
            <p>暂无订单</p>
            <el-button type="primary" @click="router.push('/products')">
              去购物
            </el-button>
          </div>

          <!-- 订单列表 -->
          <div v-else class="order-list">
            <div 
              v-for="order in orderList" 
              :key="order.orderId" 
              class="order-card"
            >
              <!-- 订单头部 -->
              <div class="order-header">
                <div class="order-info">
                  <span class="order-sn">订单号：{{ order.orderSn }}</span>
                  <span class="order-time">{{ formatDate(order.createTime) }}</span>
                </div>
                <el-tag 
                  :type="getStatusTagType(order.orderStatus)"
                  size="large"
                >
                  {{ order.orderStatusText }}
                </el-tag>
              </div>

              <!-- 订单商品列表 -->
              <div class="order-items">
                <div 
                  v-for="item in order.items" 
                  :key="item.itemId" 
                  class="order-item"
                >
                  <el-image
                    :src="item.productImg"
                    fit="cover"
                    class="item-image"
                  >
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div class="item-info">
                    <div class="item-name">{{ item.productName }}</div>
                    <div class="item-meta">
                      <span class="item-price">¥{{ item.price }}</span>
                      <span class="item-quantity">x{{ item.quantity }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 订单金额和操作 -->
              <div class="order-footer">
                <div class="order-amount">
                  <span class="amount-label">实付金额：</span>
                  <span class="amount-value">¥{{ order.payAmount }}</span>
                </div>
                <div class="order-actions">
                  <el-button 
                    size="small" 
                    @click="viewDetail(order.orderId)"
                  >
                    查看详情
                  </el-button>
                  <el-button 
                    v-if="order.orderStatus === 0"
                    type="primary" 
                    size="small"
                  >
                    去支付
                  </el-button>
                  <el-button 
                    v-if="order.orderStatus === 0 || order.orderStatus === 1"
                    size="small"
                    @click="handleCancel(order.orderId)"
                  >
                    取消订单
                  </el-button>
                  <el-button 
                    v-if="order.orderStatus === 2"
                    type="primary" 
                    size="small"
                    @click="handleConfirmReceipt(order.orderId)"
                  >
                    确认收货
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="total > 0" class="pagination">
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 30, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </el-card>
      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Picture } from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { 
  getMyOrders, 
  cancelOrder, 
  confirmReceipt,
  ORDER_STATUS_TAG_TYPE
} from '@/api/order'

const router = useRouter()

// 状态
const loading = ref(false)
const orderList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取订单列表
const fetchOrders = async () => {
  try {
    loading.value = true
    const data = await getMyOrders(currentPage.value - 1, pageSize.value)
    
    // 处理分页数据
    if (data.list) {
      orderList.value = data.list
      total.value = data.total || 0
    } else {
      // 兼容简化版PageResult（只有total和list）
      orderList.value = data.list || data || []
      total.value = data.total || orderList.value.length
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = (orderId) => {
  router.push(`/user/orders/${orderId}`)
}

// 取消订单
const handleCancel = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要取消这个订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelOrder(orderId)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
    }
  }
}

// 确认收货
const handleConfirmReceipt = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '提示', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await confirmReceipt(orderId)
    ElMessage.success('确认收货成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
    }
  }
}

// 分页改变
const handlePageChange = (page) => {
  currentPage.value = page
  fetchOrders()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchOrders()
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  return ORDER_STATUS_TAG_TYPE[status] || 'info'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 初始化
onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-page {
  min-height: 100vh;
  padding: 20px 0;
  background-color: #f5f7fa;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-state p {
  font-size: 16px;
  color: #909399;
  margin: 20px 0;
}

/* 订单列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background-color: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  transition: box-shadow 0.3s;
}

.order-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.order-sn {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.order-time {
  font-size: 13px;
  color: #909399;
}

/* 订单商品 */
.order-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.order-item {
  display: flex;
  gap: 12px;
}

.item-image {
  width: 80px;
  height: 80px;
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
  font-size: 24px;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.item-name {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-price {
  font-size: 16px;
  color: #f56c6c;
  font-weight: 500;
}

.item-quantity {
  font-size: 14px;
  color: #909399;
}

/* 订单底部 */
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.order-amount {
  display: flex;
  align-items: center;
  gap: 8px;
}

.amount-label {
  font-size: 14px;
  color: #606266;
}

.amount-value {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
}

.order-actions {
  display: flex;
  gap: 12px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}
</style>

