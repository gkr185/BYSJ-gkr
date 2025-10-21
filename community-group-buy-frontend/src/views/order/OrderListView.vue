<template>
  <div class="order-list-page">
    <div class="order-list-container">
      <h2 class="page-title">我的订单</h2>

      <!-- 订单状态筛选 -->
      <el-card class="filter-card" shadow="never">
        <el-radio-group v-model="activeStatus" @change="handleStatusChange">
          <el-radio-button :label="null">全部</el-radio-button>
          <el-radio-button :label="ORDER_STATUS.UNPAID">待支付</el-radio-button>
          <el-radio-button :label="ORDER_STATUS.TO_DELIVER">待发货</el-radio-button>
          <el-radio-button :label="ORDER_STATUS.DELIVERING">配送中</el-radio-button>
          <el-radio-button :label="ORDER_STATUS.DELIVERED">已送达</el-radio-button>
          <el-radio-button :label="ORDER_STATUS.CANCELLED">已取消</el-radio-button>
        </el-radio-group>

        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索订单号或商品名称"
            :prefix-icon="Search"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </div>
      </el-card>

      <!-- 订单列表 -->
      <template v-if="loading">
        <el-skeleton :rows="8" animated />
      </template>

      <template v-else-if="filteredOrders.length === 0">
        <el-empty description="暂无订单">
          <el-button type="primary" @click="router.push('/products')">
            去购物
          </el-button>
        </el-empty>
      </template>

      <template v-else>
        <div class="order-list">
          <el-card
            v-for="order in filteredOrders"
            :key="order.order_id"
            class="order-card"
            shadow="hover"
          >
            <!-- 订单头部 -->
            <div class="order-header">
              <div class="order-info-left">
                <span class="order-sn">订单号：{{ order.order_sn }}</span>
                <span class="order-time">{{ formatDateTime(order.create_time) }}</span>
              </div>
              <div class="order-info-right">
                <el-tag :type="getOrderStatusType(order.order_status)" size="large">
                  {{ getOrderStatusText(order.order_status) }}
                </el-tag>
              </div>
            </div>

            <el-divider />

            <!-- 订单商品列表 -->
            <div class="order-items">
              <div
                v-for="item in order.items"
                :key="item.item_id"
                class="order-item"
              >
                <el-image
                  :src="item.product_img"
                  fit="cover"
                  class="item-image"
                  @click="router.push(`/products/${item.product_id}`)"
                />
                <div class="item-info">
                  <div class="item-name">
                    {{ item.product_name }}
                    <el-tag v-if="item.activity_id" type="danger" size="small">
                      拼团
                    </el-tag>
                  </div>
                  <div class="item-price">¥{{ item.price }}</div>
                  <div class="item-quantity">× {{ item.quantity }}</div>
                </div>
                <div class="item-total">
                  ¥{{ item.total_price.toFixed(2) }}
                </div>
              </div>
            </div>

            <el-divider />

            <!-- 订单金额和操作 -->
            <div class="order-footer">
              <div class="order-amount">
                <span class="amount-label">订单金额：</span>
                <span class="amount-value">¥{{ order.pay_amount.toFixed(2) }}</span>
              </div>

              <div class="order-actions">
                <!-- 待支付状态 -->
                <template v-if="order.order_status === ORDER_STATUS.UNPAID">
                  <el-button size="default" @click="handleCancelOrder(order)">
                    取消订单
                  </el-button>
                  <el-button type="primary" size="default" @click="handlePayOrder(order)">
                    去支付
                  </el-button>
                </template>

                <!-- 待发货状态 -->
                <template v-else-if="order.order_status === ORDER_STATUS.TO_DELIVER">
                  <el-button size="default" @click="handleContactService(order)">
                    联系客服
                  </el-button>
                  <el-button size="default" @click="handleRequestRefund(order)">
                    申请退款
                  </el-button>
                </template>

                <!-- 配送中状态 -->
                <template v-else-if="order.order_status === ORDER_STATUS.DELIVERING">
                  <el-button size="default" @click="handleViewLogistics(order)">
                    查看物流
                  </el-button>
                  <el-button type="primary" size="default" @click="handleConfirmReceipt(order)">
                    确认收货
                  </el-button>
                </template>

                <!-- 已送达状态 -->
                <template v-else-if="order.order_status === ORDER_STATUS.DELIVERED">
                  <el-button size="default" @click="handleBuyAgain(order)">
                    再来一单
                  </el-button>
                  <el-button size="default" @click="handleEvaluate(order)">
                    评价
                  </el-button>
                </template>

                <!-- 已取消状态 -->
                <template v-else-if="order.order_status === ORDER_STATUS.CANCELLED">
                  <el-button size="default" @click="handleDeleteOrder(order)">
                    删除订单
                  </el-button>
                  <el-button size="default" @click="handleBuyAgain(order)">
                    再来一单
                  </el-button>
                </template>

                <!-- 退款相关状态 -->
                <template v-else-if="order.order_status === ORDER_STATUS.REFUNDING || order.order_status === ORDER_STATUS.REFUNDED">
                  <el-button size="default" @click="handleViewRefundDetail(order)">
                    退款详情
                  </el-button>
                </template>

                <!-- 查看详情按钮（所有状态都显示） -->
                <el-button size="default" @click="handleViewDetail(order)">
                  订单详情
                </el-button>
              </div>
            </div>
          </el-card>
        </div>

        <!-- 分页 -->
        <div v-if="totalOrders > pageSize" class="pagination-wrapper">
          <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :total="totalOrders"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import {
  getUserOrders,
  ORDER_STATUS,
  getOrderStatusText,
  getOrderStatusType,
  payOrder,
  cancelOrder,
  formatDateTime
} from '@/mock/orders'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const orders = ref([])
const activeStatus = ref(null) // 当前选中的状态筛选
const searchKeyword = ref('') // 搜索关键词

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    // 先尝试获取当前用户的订单
    const userId = userStore.userInfo?.userId || 1
    let data = getUserOrders(userId, activeStatus.value)
    
    // 如果当前用户没有订单数据，使用默认测试数据（userId=1）
    if (data.length === 0 && userId !== 1) {
      console.log('当前用户无订单，使用默认测试订单数据（userId=1）')
      data = getUserOrders(1, activeStatus.value)
    }
    
    orders.value = data
    
    if (data.length > 0) {
      console.log(`成功加载${data.length}个订单`)
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 筛选后的订单列表
const filteredOrders = computed(() => {
  let result = orders.value

  // 关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    result = result.filter(order => {
      // 搜索订单号
      if (order.order_sn.toLowerCase().includes(keyword)) return true
      // 搜索商品名称
      return order.items.some(item =>
        item.product_name.toLowerCase().includes(keyword)
      )
    })
  }

  // 分页
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return result.slice(start, end)
})

// 总订单数
const totalOrders = computed(() => {
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.trim().toLowerCase()
    return orders.value.filter(order => {
      if (order.order_sn.toLowerCase().includes(keyword)) return true
      return order.items.some(item =>
        item.product_name.toLowerCase().includes(keyword)
      )
    }).length
  }
  return orders.value.length
})

// 状态筛选变化
const handleStatusChange = () => {
  currentPage.value = 1
  fetchOrders()
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
}

// 分页变化
const handlePageChange = (page) => {
  currentPage.value = page
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

// 取消订单
const handleCancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消该订单吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const result = cancelOrder(order.order_id)
    if (result.success) {
      ElMessage.success('订单已取消')
      fetchOrders()
    } else {
      ElMessage.error(result.message || '取消失败')
    }
  } catch (error) {
    // 用户取消操作
  }
}

// 去支付
const handlePayOrder = (order) => {
  ElMessageBox.confirm(
    `订单金额：¥${order.pay_amount.toFixed(2)}，请选择支付方式`,
    '确认支付',
    {
      confirmButtonText: '微信支付',
      cancelButtonText: '取消',
      distinguishCancelAndClose: true
    }
  ).then(() => {
    // 模拟支付
    const result = payOrder(order.order_id, 1)
    if (result.success) {
      ElMessage.success('支付成功！')
      fetchOrders()
    }
  }).catch(() => {
    // 用户取消
  })
}

// 联系客服
const handleContactService = (order) => {
  ElMessage.info('客服功能开发中')
}

// 申请退款
const handleRequestRefund = (order) => {
  ElMessage.info('退款功能开发中')
}

// 查看物流
const handleViewLogistics = (order) => {
  ElMessage.info('物流查询功能开发中')
}

// 确认收货
const handleConfirmReceipt = async (order) => {
  try {
    await ElMessageBox.confirm(
      '确认已收到货物吗？',
      '确认收货',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    ElMessage.success('确认收货成功！')
    // 更新订单状态
    order.order_status = ORDER_STATUS.DELIVERED
    fetchOrders()
  } catch (error) {
    // 用户取消
  }
}

// 再来一单
const handleBuyAgain = (order) => {
  // 将订单商品重新加入购物车
  ElMessage.success('商品已加入购物车')
  router.push('/cart')
}

// 评价
const handleEvaluate = (order) => {
  ElMessage.info('评价功能开发中')
}

// 删除订单
const handleDeleteOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该订单吗？删除后无法恢复。',
      '删除订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    ElMessage.success('订单已删除')
    fetchOrders()
  } catch (error) {
    // 用户取消
  }
}

// 查看退款详情
const handleViewRefundDetail = (order) => {
  ElMessage.info('退款详情功能开发中')
}

// 查看订单详情
const handleViewDetail = (order) => {
  ElMessageBox.alert(
    `
    <div style="line-height: 2;">
      <p><strong>订单号：</strong>${order.order_sn}</p>
      <p><strong>创建时间：</strong>${formatDateTime(order.create_time)}</p>
      <p><strong>收货人：</strong>${order.address.receiver} ${order.address.phone}</p>
      <p><strong>收货地址：</strong>${order.address.province}${order.address.city}${order.address.district}${order.address.detail}</p>
      <p><strong>商品总额：</strong>¥${order.total_amount.toFixed(2)}</p>
      <p><strong>优惠金额：</strong>¥${order.discount_amount.toFixed(2)}</p>
      <p><strong>实付金额：</strong><span style="color: #f56c6c; font-weight: bold;">¥${order.pay_amount.toFixed(2)}</span></p>
      ${order.pay_time ? `<p><strong>支付时间：</strong>${formatDateTime(order.pay_time)}</p>` : ''}
    </div>
    `,
    '订单详情',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-list-page {
  min-height: 100vh;
  padding-top: 84px; /* 64px导航栏 + 20px间隔 */
  background-color: #f5f5f5;
}

.order-list-container {
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

/* 筛选卡片 */
.filter-card {
  margin-bottom: 20px;
  padding: 20px;
}

.filter-card .el-radio-group {
  margin-bottom: 16px;
}

.search-box {
  max-width: 400px;
}

/* 订单列表 */
.order-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-card {
  animation: fadeInUp 0.4s ease-out;
}

/* 订单头部 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-sn {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.order-time {
  font-size: 13px;
  color: #999;
}

/* 订单商品列表 */
.order-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 8px;
  transition: all 0.3s;
}

.order-item:hover {
  background-color: #f0f0f0;
}

.item-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  flex-shrink: 0;
  cursor: pointer;
  transition: transform 0.3s;
}

.item-image:hover {
  transform: scale(1.05);
}

.item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.item-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  font-size: 14px;
  color: #F56C6C;
}

.item-quantity {
  font-size: 13px;
  color: #999;
}

.item-total {
  font-size: 16px;
  font-weight: bold;
  color: #F56C6C;
  flex-shrink: 0;
}

/* 订单底部 */
.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-amount {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.amount-label {
  font-size: 14px;
  color: #666;
}

.amount-value {
  font-size: 20px;
  font-weight: bold;
  color: #F56C6C;
}

.order-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
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
  .order-list-page {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }

  .page-title {
    font-size: 20px;
  }

  .filter-card {
    padding: 16px;
  }

  .filter-card .el-radio-group {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .search-box {
    max-width: 100%;
  }

  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .order-item {
    flex-wrap: wrap;
  }

  .item-image {
    width: 60px;
    height: 60px;
  }

  .item-total {
    width: 100%;
    text-align: right;
  }

  .order-footer {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .order-actions {
    justify-content: stretch;
  }

  .order-actions .el-button {
    flex: 1;
  }

  .pagination-wrapper :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
