<template>
  <MainLayout>
    <div class="orders-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">
          <el-icon><Document /></el-icon>
          我的订单
        </h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/profile' }">个人中心</el-breadcrumb-item>
          <el-breadcrumb-item>我的订单</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 订单状态筛选 -->
      <div class="status-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部订单" :name="'all'">
            <template #label>
              <span class="tab-label">
                <el-icon><List /></el-icon>
                全部订单
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane :label="`待支付 (${stats.pendingPayment || 0})`" :name="ORDER_STATUS.PENDING_PAYMENT">
            <template #label>
              <span class="tab-label">
                <el-icon><Clock /></el-icon>
                待支付
                <el-badge v-if="stats.pendingPayment > 0" :value="stats.pendingPayment" class="status-badge" />
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane :label="`待发货 (${stats.pendingDelivery || 0})`" :name="ORDER_STATUS.PENDING_DELIVERY">
            <template #label>
              <span class="tab-label">
                <el-icon><Box /></el-icon>
                待发货
                <el-badge v-if="stats.pendingDelivery > 0" :value="stats.pendingDelivery" class="status-badge" />
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane :label="`配送中 (${stats.inDelivery || 0})`" :name="ORDER_STATUS.IN_DELIVERY">
            <template #label>
              <span class="tab-label">
                <el-icon><Van /></el-icon>
                配送中
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane :label="`已送达 (${stats.delivered || 0})`" :name="ORDER_STATUS.DELIVERED">
            <template #label>
              <span class="tab-label">
                <el-icon><CircleCheck /></el-icon>
                已送达
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 订单列表 -->
      <div v-else-if="filteredOrders.length > 0" class="orders-list">
        <div v-for="order in filteredOrders" :key="order.orderId" class="order-card">
          <!-- 订单头部 -->
          <div class="order-header">
            <div class="order-info">
              <span class="order-sn">订单号: {{ order.orderSn }}</span>
              <el-tag
                :type="ORDER_STATUS_TAG_TYPE[order.orderStatus]"
                effect="dark"
                size="small"
              >
                {{ order.orderStatusText || ORDER_STATUS_TEXT[order.orderStatus] }}
              </el-tag>
            </div>
            <div class="order-time">
              <el-icon><Clock /></el-icon>
              {{ formatDateTime(order.createTime) }}
            </div>
          </div>

          <!-- 订单商品 -->
          <div class="order-items">
            <div v-for="item in order.items" :key="item.itemId" class="order-item">
              <el-image
                :src="item.productImg || '/placeholder-product.png'"
                fit="cover"
                class="product-image"
                :preview-src-list="item.productImg ? [item.productImg] : []"
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
                  <span class="item-price">¥{{ item.price?.toFixed(2) }}</span>
                  <span class="item-quantity">×{{ item.quantity }}</span>
                </div>
                <div v-if="item.activityId" class="item-tag">
                  <el-tag type="warning" size="small">拼团商品</el-tag>
                </div>
              </div>
            </div>
          </div>

          <!-- 订单底部 -->
          <div class="order-footer">
            <div class="order-total">
              <span class="total-label">订单金额:</span>
              <span class="total-amount">¥{{ order.payAmount?.toFixed(2) }}</span>
            </div>
            <div class="order-actions">
              <!-- 待支付状态 -->
              <template v-if="order.orderStatus === ORDER_STATUS.PENDING_PAYMENT">
                <el-button size="small" @click="handleCancelOrder(order.orderId)">
                  取消订单
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  @click="handlePayOrder(order.orderId)"
                >
                  立即支付
                </el-button>
              </template>

              <!-- 待发货状态 -->
              <template v-else-if="order.orderStatus === ORDER_STATUS.PENDING_DELIVERY">
                <el-button size="small" disabled>
                  等待发货
                </el-button>
              </template>

              <!-- 配送中状态 -->
              <template v-else-if="order.orderStatus === ORDER_STATUS.IN_DELIVERY">
                <el-button
                  type="success"
                  size="small"
                  @click="handleConfirmReceipt(order.orderId)"
                >
                  确认收货
                </el-button>
              </template>

              <!-- 已送达状态 -->
              <template v-else-if="order.orderStatus === ORDER_STATUS.DELIVERED">
                <el-button size="small" type="success" plain disabled>
                  交易完成
                </el-button>
              </template>

              <!-- 通用按钮 -->
              <el-button size="small" @click="handleViewDetail(order.orderId)">
                订单详情
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty 
        v-else 
        :description="activeTab === 'all' ? '暂无订单' : `暂无${ORDER_STATUS_TEXT[statusFilter]}订单`" 
        class="empty-state"
      >
        <el-button type="primary" @click="$router.push('/products')">
          去逛逛
        </el-button>
      </el-empty>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-container">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          background
        />
      </div>
    </div>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="currentOrder" class="order-detail">
        <!-- 订单信息 -->
        <div class="detail-section">
          <h3 class="section-title">订单信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单编号">
              {{ currentOrder.orderSn }}
            </el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="ORDER_STATUS_TAG_TYPE[currentOrder.orderStatus]">
                {{ currentOrder.orderStatusText || ORDER_STATUS_TEXT[currentOrder.orderStatus] }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(currentOrder.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="支付状态">
              <el-tag :type="currentOrder.payStatus === 1 ? 'success' : 'warning'">
                {{ currentOrder.payStatus === 1 ? '已支付' : '未支付' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="currentOrder.payTime" label="支付时间">
              {{ formatDateTime(currentOrder.payTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="订单金额">
              <span class="amount-text">¥{{ currentOrder.payAmount?.toFixed(2) }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 商品列表 -->
        <div class="detail-section">
          <h3 class="section-title">商品信息</h3>
          <el-table :data="currentOrder.items" border>
            <el-table-column label="商品" width="350">
              <template #default="{ row }">
                <div class="product-cell">
                  <el-image
                    :src="row.productImg || '/placeholder-product.png'"
                    fit="cover"
                    style="width: 60px; height: 60px; border-radius: 4px;"
                  />
                  <span style="margin-left: 12px;">{{ row.productName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" prop="price" width="120">
              <template #default="{ row }">
                ¥{{ row.price?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="数量" prop="quantity" width="100" align="center" />
            <el-table-column label="小计" width="120">
              <template #default="{ row }">
                ¥{{ (row.price * row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </MainLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  Clock,
  Box,
  Van,
  CircleCheck,
  List,
  Picture
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { getMyOrders, cancelOrder, confirmReceipt, ORDER_STATUS, ORDER_STATUS_TEXT, ORDER_STATUS_TAG_TYPE } from '@/api/order'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 状态
const loading = ref(false)
const orders = ref([])
const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const stats = reactive({
  pendingPayment: 0,
  pendingDelivery: 0,
  inDelivery: 0,
  delivered: 0
})

// 订单详情对话框
const detailDialogVisible = ref(false)
const currentOrder = ref(null)

// 计算属性 - 状态过滤器
const statusFilter = computed(() => {
  if (activeTab.value === 'all') return null
  return parseInt(activeTab.value)
})

// 计算属性 - 过滤后的订单列表
const filteredOrders = computed(() => {
  if (statusFilter.value === null) {
    return orders.value
  }
  return orders.value.filter(order => order.orderStatus === statusFilter.value)
})

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const response = await getMyOrders(currentPage.value - 1, pageSize.value)
    
    if (response.code === 200) {
      const data = response.data
      
      // 兼容不同的响应格式
      if (Array.isArray(data)) {
        // 如果data直接是数组
        orders.value = data
        total.value = data.length
      } else if (data && typeof data === 'object') {
        // 如果data是对象（PageResult格式），包含list和total
        orders.value = data.list || []
        total.value = data.total || 0
      } else {
        orders.value = []
        total.value = 0
      }
      
      // 计算各状态订单数量
      calculateStats(orders.value)
    } else {
      ElMessage.error(response.message || '获取订单列表失败')
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 计算统计数据
const calculateStats = (orderList) => {
  stats.pendingPayment = orderList.filter(o => o.orderStatus === ORDER_STATUS.PENDING_PAYMENT).length
  stats.pendingDelivery = orderList.filter(o => o.orderStatus === ORDER_STATUS.PENDING_DELIVERY).length
  stats.inDelivery = orderList.filter(o => o.orderStatus === ORDER_STATUS.IN_DELIVERY).length
  stats.delivered = orderList.filter(o => o.orderStatus === ORDER_STATUS.DELIVERED).length
}

// 标签页切换（客户端过滤，无需重新加载）
const handleTabChange = (tabName) => {
  // filteredOrders 计算属性会自动更新
  console.log('切换到标签:', tabName, '过滤后订单数:', filteredOrders.value.length)
}

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  loadOrders()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadOrders()
}

// 取消订单
const handleCancelOrder = async (orderId) => {
  try {
    await ElMessageBox.confirm('确定要取消此订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await cancelOrder(orderId)
    
    if (response.code === 200) {
      ElMessage.success('订单已取消')
      loadOrders()
    } else {
      ElMessage.error(response.message || '取消订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败，请稍后重试')
    }
  }
}

// 支付订单
const handlePayOrder = (orderId) => {
  // 找到对应的订单数据
  const order = orders.value.find(o => o.orderId === orderId)
  if (!order) {
    ElMessage.error('订单数据不存在')
    return
  }

  router.push({
    path: '/payment',
    query: {
      orderId,
      amount: order.payAmount,
      type: order.orderType === 1 ? 'groupbuy' : 'normal' // 1-拼团订单
    }
  })
}

// 确认收货
const handleConfirmReceipt = async (orderId) => {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '取消',
      type: 'success'
    })
    
    const response = await confirmReceipt(orderId)
    
    if (response.code === 200) {
      ElMessage.success('确认收货成功')
      loadOrders()
    } else {
      ElMessage.error(response.message || '确认收货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败，请稍后重试')
    }
  }
}

// 查看订单详情
const handleViewDetail = async (orderId) => {
  const order = orders.value.find(o => o.orderId === orderId)
  if (order) {
    currentOrder.value = order
    detailDialogVisible.value = true
  }
}

// 页面加载
onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

/* 状态标签 */
.status-tabs {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-badge {
  margin-left: 4px;
}

/* 加载状态 */
.loading-container {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 订单列表 */
.orders-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.order-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
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
  align-items: center;
  gap: 12px;
}

.order-sn {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.order-time {
  display: flex;
  align-items: center;
  gap: 6px;
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
  gap: 16px;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  flex-shrink: 0;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 24px;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.item-name {
  font-size: 15px;
  color: #303133;
  margin-bottom: 8px;
  font-weight: 500;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 6px;
}

.item-price {
  font-size: 16px;
  color: #f56c6c;
  font-weight: 600;
}

.item-quantity {
  font-size: 14px;
  color: #909399;
}

.item-tag {
  margin-top: 4px;
}

/* 订单底部 */
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.order-total {
  display: flex;
  align-items: center;
  gap: 8px;
}

.total-label {
  font-size: 14px;
  color: #606266;
}

.total-amount {
  font-size: 20px;
  color: #f56c6c;
  font-weight: 700;
}

.order-actions {
  display: flex;
  gap: 12px;
}

/* 空状态 */
.empty-state {
  background: white;
  border-radius: 8px;
  padding: 60px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 订单详情对话框 */
.order-detail {
  padding: 10px 0;
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #409eff;
}

.amount-text {
  font-size: 18px;
  color: #f56c6c;
  font-weight: 600;
}

.product-cell {
  display: flex;
  align-items: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .orders-container {
    padding: 16px;
  }
  
  .page-title {
    font-size: 22px;
  }
  
  .order-header,
  .order-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .order-actions {
    width: 100%;
    flex-direction: column;
  }
  
  .order-actions .el-button {
    width: 100%;
  }
  
  .product-image {
    width: 60px;
    height: 60px;
  }
}
</style>

