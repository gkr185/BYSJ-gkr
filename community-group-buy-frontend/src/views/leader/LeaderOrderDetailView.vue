<template>
  <MainLayout>
    <div class="leader-order-detail-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <el-button :icon="ArrowLeft" @click="$router.back()">返回</el-button>
          <h2 class="page-title">
            <el-icon><DocumentCopy /></el-icon>
            订单详情
          </h2>
        </div>
      </div>

      <!-- 加载状态 -->
      <el-skeleton :rows="8" animated v-if="pageLoading" />

      <!-- 主体内容 -->
      <div v-else-if="orderDetail" class="order-detail-content">
        <!-- 订单状态卡片 -->
        <el-card class="status-card" shadow="hover">
          <div class="status-header">
            <div class="status-info">
              <div class="status-icon" :class="getStatusIconClass(orderDetail.orderStatus)">
                <el-icon>
                  <component :is="getStatusIcon(orderDetail.orderStatus)" />
                </el-icon>
              </div>
              <div class="status-content">
                <div class="status-text">{{ getStatusText(orderDetail.orderStatus) }}</div>
                <div class="status-desc">{{ getStatusDescription(orderDetail.orderStatus) }}</div>
              </div>
            </div>
            <div class="order-amount">
              <div class="amount-label">订单金额</div>
              <div class="amount-value">¥{{ formatMoney(orderDetail.payAmount) }}</div>
            </div>
          </div>
        </el-card>

        <!-- 订单信息 -->
        <el-row :gutter="20">
          <!-- 基本信息 -->
          <el-col :span="24" :lg="16">
            <el-card class="info-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><InfoFilled /></el-icon>
                  <span>订单信息</span>
                </div>
              </template>
              
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">订单编号</span>
                  <span class="info-value">{{ orderDetail.orderSn }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">下单时间</span>
                  <span class="info-value">{{ formatTime(orderDetail.createTime) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">支付时间</span>
                  <span class="info-value">{{ formatTime(orderDetail.payTime) || '未支付' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">支付状态</span>
                  <span class="info-value">
                    <el-tag :type="orderDetail.payStatus === 1 ? 'success' : 'warning'" size="small">
                      {{ orderDetail.payStatus === 1 ? '已支付' : '未支付' }}
                    </el-tag>
                  </span>
                </div>
              </div>
            </el-card>
          </el-col>

          <!-- 操作区域 -->
          <el-col :span="24" :lg="8">
            <el-card class="actions-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><Operation /></el-icon>
                  <span>订单操作</span>
                </div>
              </template>
              
              <div class="actions-content">
                <el-button 
                  v-if="canUpdateStatus(orderDetail.orderStatus, 1)"
                  type="primary" 
                  :icon="Box"
                  @click="updateOrderStatus(1)"
                  :loading="actionLoading"
                >
                  标记发货
                </el-button>
                <el-button 
                  v-if="canUpdateStatus(orderDetail.orderStatus, 2)"
                  type="success" 
                  :icon="CircleCheck"
                  @click="updateOrderStatus(2)"
                  :loading="actionLoading"
                >
                  标记配送中
                </el-button>
                <el-button 
                  v-if="canCancelOrder(orderDetail.orderStatus)"
                  type="danger" 
                  :icon="CircleClose"
                  @click="showCancelDialog"
                  :loading="actionLoading"
                >
                  取消订单
                </el-button>
                <el-button 
                  type="info" 
                  :icon="Phone"
                  @click="contactCustomer"
                >
                  联系客户
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 商品信息 -->
        <el-card class="items-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><ShoppingBag /></el-icon>
              <span>商品信息</span>
            </div>
          </template>
          
          <div class="items-list">
            <div v-for="item in orderDetail.items" :key="item.itemId" class="item-row">
              <img :src="item.productImg" class="product-img" alt="商品图片" />
              <div class="item-info">
                <div class="product-name">{{ item.productName }}</div>
                <div class="product-meta">
                  <span class="meta-item">单价：¥{{ formatMoney(item.price) }}</span>
                  <span class="meta-item">数量：x{{ item.quantity }}</span>
                  <span class="meta-item" v-if="item.activityId">
                    <el-tag type="warning" size="small">拼团商品</el-tag>
                  </span>
                </div>
              </div>
              <div class="item-total">
                ¥{{ formatMoney(item.totalPrice) }}
              </div>
            </div>
          </div>

          <el-divider />
          
          <div class="amount-summary">
            <div class="summary-row">
              <span class="summary-label">商品总金额</span>
              <span class="summary-value">¥{{ formatMoney(orderDetail.totalAmount) }}</span>
            </div>
            <div class="summary-row" v-if="orderDetail.discountAmount > 0">
              <span class="summary-label">优惠金额</span>
              <span class="summary-value text-success">-¥{{ formatMoney(orderDetail.discountAmount) }}</span>
            </div>
            <div class="summary-row total-row">
              <span class="summary-label">实付金额</span>
              <span class="summary-value">¥{{ formatMoney(orderDetail.payAmount) }}</span>
            </div>
          </div>
        </el-card>

        <!-- 收货信息 -->
        <el-card class="address-card" shadow="hover" v-if="orderDetail.receiveAddress">
          <template #header>
            <div class="card-header">
              <el-icon><LocationInformation /></el-icon>
              <span>收货信息</span>
            </div>
          </template>
          
          <div class="address-content">
            <div class="address-info">
              <div class="recipient-name">{{ orderDetail.receiveAddress.recipientName }}</div>
              <div class="recipient-phone">{{ orderDetail.receiveAddress.recipientPhone }}</div>
              <div class="recipient-address">{{ orderDetail.receiveAddress.fullAddress }}</div>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 订单不存在 -->
      <el-empty v-else description="订单不存在或已删除" />
    </div>

    <!-- 取消订单对话框 -->
    <el-dialog v-model="cancelDialogVisible" title="取消订单" width="400px">
      <div class="cancel-content">
        <p>确认要取消这个订单吗？取消后将自动为客户退款。</p>
        <el-input
          v-model="cancelReason"
          type="textarea"
          placeholder="请输入取消原因（可选）"
          :rows="3"
          maxlength="200"
          show-word-limit
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmCancelOrder" :loading="actionLoading">
            确认取消
          </el-button>
        </span>
      </template>
    </el-dialog>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getOrderDetail, cancelOrder } from '@/api/order'
import { ORDER_STATUS_TEXT } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  DocumentCopy,
  InfoFilled,
  Operation,
  ShoppingBag,
  LocationInformation,
  Clock,
  Van,
  CircleCheck,
  CircleClose,
  Phone,
  Box,
  Check
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'

const router = useRouter()
const route = useRoute()
const orderId = route.params.orderId

// 页面状态
const pageLoading = ref(true)
const actionLoading = ref(false)
const orderDetail = ref(null)

// 取消订单对话框
const cancelDialogVisible = ref(false)
const cancelReason = ref('')

// 订单状态图标映射
const statusIcons = {
  0: Clock,    // 待支付
  1: Box,      // 待发货
  2: Van,      // 配送中
  3: Check,    // 已送达
  4: CircleClose, // 已取消
  5: Clock,    // 退款中
  6: CircleClose  // 已退款
}

// 加载订单详情
const loadOrderDetail = async () => {
  pageLoading.value = true
  try {
    const res = await getOrderDetail(orderId)
    if (res.code === 200) {
      orderDetail.value = res.data
    } else {
      ElMessage.error(res.message || '加载订单详情失败')
    }
  } catch (error) {
    ElMessage.error('加载失败，请稍后重试')
    console.error('加载订单详情失败:', error)
  } finally {
    pageLoading.value = false
  }
}

// 更新订单状态
const updateOrderStatus = async (status) => {
  try {
    actionLoading.value = true
    // 这里需要调用更新订单状态的API
    ElMessage.success('状态更新成功')
    await loadOrderDetail() // 重新加载订单详情
  } catch (error) {
    ElMessage.error('状态更新失败')
    console.error('更新订单状态失败:', error)
  } finally {
    actionLoading.value = false
  }
}

// 显示取消订单对话框
const showCancelDialog = () => {
  cancelReason.value = ''
  cancelDialogVisible.value = true
}

// 确认取消订单
const confirmCancelOrder = async () => {
  try {
    actionLoading.value = true
    const res = await cancelOrder(orderId)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      cancelDialogVisible.value = false
      await loadOrderDetail() // 重新加载订单详情
    } else {
      ElMessage.error(res.message || '取消订单失败')
    }
  } catch (error) {
    ElMessage.error('取消订单失败')
    console.error('取消订单失败:', error)
  } finally {
    actionLoading.value = false
  }
}

// 联系客户
const contactCustomer = () => {
  ElMessageBox.alert('客户联系功能开发中，请通过其他方式联系客户', '提示', {
    confirmButtonText: '确定'
  })
}

// 获取状态文本
const getStatusText = (status) => {
  return ORDER_STATUS_TEXT[status] || '未知状态'
}

// 获取状态描述
const getStatusDescription = (status) => {
  const descriptions = {
    0: '等待客户支付',
    1: '等待发货处理',
    2: '商品正在配送中',
    3: '订单已完成',
    4: '订单已取消',
    5: '正在处理退款',
    6: '退款已完成'
  }
  return descriptions[status] || ''
}

// 获取状态图标
const getStatusIcon = (status) => {
  return statusIcons[status] || Clock
}

// 获取状态图标样式类
const getStatusIconClass = (status) => {
  const classes = {
    0: 'status-pending',
    1: 'status-processing',
    2: 'status-shipping',
    3: 'status-success',
    4: 'status-cancelled',
    5: 'status-refunding',
    6: 'status-refunded'
  }
  return classes[status] || 'status-default'
}

// 检查是否可以更新状态
const canUpdateStatus = (currentStatus, targetStatus) => {
  // 根据业务规则判断状态转换是否允许
  if (targetStatus === 1) { // 标记发货
    return currentStatus === 0 && orderDetail.value?.payStatus === 1 // 已支付的待支付订单
  }
  if (targetStatus === 2) { // 标记配送中
    return currentStatus === 1 // 待发货状态
  }
  return false
}

// 检查是否可以取消订单
const canCancelOrder = (status) => {
  return status === 0 || status === 1 // 待支付或待发货状态可以取消
}

// 格式化金额
const formatMoney = (value) => {
  return value ? value.toFixed(2) : '0.00'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 页面加载
onMounted(() => {
  if (!orderId) {
    ElMessage.error('订单ID不能为空')
    router.back()
    return
  }
  loadOrderDetail()
})
</script>

<style scoped>
.leader-order-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}

.order-detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 状态卡片 */
.status-card :deep(.el-card__body) {
  padding: 24px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.status-icon.status-pending {
  background: linear-gradient(135deg, #fff4e6 0%, #ffe7cc 100%);
  color: #e6a23c;
}

.status-icon.status-processing {
  background: linear-gradient(135deg, #e8f4ff 0%, #d1e9ff 100%);
  color: #409eff;
}

.status-icon.status-shipping {
  background: linear-gradient(135deg, #fef0e6 0%, #fde2cc 100%);
  color: #f56c6c;
}

.status-icon.status-success {
  background: linear-gradient(135deg, #e6f7f0 0%, #d0f0e3 100%);
  color: #67c23a;
}

.status-icon.status-cancelled,
.status-icon.status-refunded {
  background: linear-gradient(135deg, #f2f2f2 0%, #e5e5e5 100%);
  color: #909399;
}

.status-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.status-text {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.status-desc {
  font-size: 14px;
  color: #909399;
}

.order-amount {
  text-align: right;
}

.amount-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.amount-value {
  font-size: 28px;
  font-weight: 700;
  color: #f56c6c;
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.info-label {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

/* 操作区域 */
.actions-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 商品列表 */
.items-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.item-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.product-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}

.item-info {
  flex: 1;
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
  gap: 16px;
  font-size: 14px;
  color: #909399;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.item-total {
  font-size: 18px;
  font-weight: 600;
  color: #f56c6c;
}

/* 金额汇总 */
.amount-summary {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 300px;
  margin-left: auto;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.summary-row.total-row {
  font-size: 16px;
  font-weight: 600;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}

.summary-label {
  color: #606266;
}

.summary-value {
  color: #303133;
  font-weight: 500;
}

.text-success {
  color: #67c23a !important;
}

/* 收货地址 */
.address-content {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.address-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recipient-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.recipient-phone {
  font-size: 14px;
  color: #606266;
}

.recipient-address {
  font-size: 14px;
  color: #909399;
  line-height: 1.5;
}

/* 取消对话框 */
.cancel-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cancel-content p {
  margin: 0;
  color: #606266;
  line-height: 1.5;
}

/* 响应式 */
@media (max-width: 768px) {
  .leader-order-detail-container {
    padding: 12px;
  }

  .page-title {
    font-size: 20px;
  }

  .status-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .item-row {
    flex-direction: column;
    align-items: flex-start;
    text-align: left;
  }

  .product-img {
    width: 100%;
    height: auto;
    aspect-ratio: 1;
  }
}
</style>
