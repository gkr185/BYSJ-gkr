<template>
  <div class="order-manage">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #409EFF">
              <el-icon><ShoppingCart /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.total || 0 }}</div>
              <div class="stat-label">总订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #E6A23C">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.pending || 0 }}</div>
              <div class="stat-label">待支付订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #67C23A">
              <el-icon><Finished /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.completed || 0 }}</div>
              <div class="stat-label">已完成订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #F56C6C">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.cancelled || 0 }}</div>
              <div class="stat-label">已取消订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主内容卡片 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <div>
            <el-button @click="fetchOrders">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <el-row :gutter="20" class="search-row">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索订单号/用户名"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-col>

        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="订单状态" @change="handleStatusFilter" clearable>
            <el-option label="全部状态" :value="null" />
            <el-option label="待支付" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="配送中" :value="2" />
            <el-option label="已送达" :value="3" />
            <el-option label="已取消" :value="4" />
            <el-option label="退款中" :value="5" />
            <el-option label="已退款" :value="6" />
          </el-select>
        </el-col>

        <el-col :span="4">
          <el-select v-model="payStatusFilter" placeholder="支付状态" @change="fetchOrders" clearable>
            <el-option label="全部" :value="null" />
            <el-option label="未支付" :value="0" />
            <el-option label="已支付" :value="1" />
          </el-select>
        </el-col>

        <el-col :span="6">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="fetchOrders"
          />
        </el-col>

        <el-col :span="4">
          <el-button type="primary" @click="resetFilters">重置筛选</el-button>
        </el-col>
      </el-row>

      <!-- 批量操作 -->
      <el-row class="batch-actions" v-if="selectedOrders.length > 0">
        <el-alert
          :title="`已选择 ${selectedOrders.length} 个订单`"
          type="info"
          :closable="false"
        />
        <div class="batch-buttons">
          <el-button type="success" size="small" @click="batchUpdateStatus(1)">
            批量发货
          </el-button>
          <el-button type="warning" size="small" @click="batchUpdateStatus(4)">
            批量取消
          </el-button>
        </div>
      </el-row>

      <!-- 订单列表 -->
      <el-table
        :data="orderList"
        v-loading="loading"
        border
        style="width: 100%; margin-top: 20px"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderId" label="订单ID" width="80" />
        <el-table-column prop="orderSn" label="订单编号" width="180">
          <template #default="{ row }">
            <el-button type="text" @click="showOrderDetail(row.orderId)">
              {{ row.orderSn }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="商品信息" width="300">
          <template #default="{ row }">
            <div v-if="row.items && row.items.length > 0" class="order-items">
              <div v-for="item in row.items" :key="item.itemId" class="order-item">
                <el-image
                  :src="item.productImg"
                  fit="cover"
                  style="width: 50px; height: 50px; border-radius: 4px; margin-right: 10px"
                />
                <div>
                  <div class="item-name">{{ item.productName }}</div>
                  <div class="item-info">
                    ¥{{ item.price }} × {{ item.quantity }}
                  </div>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="收货信息" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <div v-if="row.receiveAddress" class="address-cell">
              <div v-if="row.receiverName || row.receiverPhone" class="receiver-info">
                <el-tag size="small" type="success" effect="plain">
                  {{ row.receiverName || '收货人' }}
                </el-tag>
                <span v-if="row.receiverPhone" class="phone-text">
                  {{ row.receiverPhone }}
                </span>
              </div>
              <div class="address-text">
                <el-icon><LocationFilled /></el-icon>
                {{ row.receiveAddress }}
              </div>
            </div>
            <el-text v-else type="info" size="small">地址信息加载中...</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="payAmount" label="实付金额" width="120">
          <template #default="{ row }">
            <span style="color: #F56C6C; font-weight: bold">
              ¥{{ row.payAmount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getOrderStatusType(row.orderStatus)">
              {{ row.orderStatusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.payStatus === 1 ? 'success' : 'warning'">
              {{ row.payStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showOrderDetail(row.orderId)">
              详情
            </el-button>
            <el-dropdown @command="(command) => handleOrderAction(command, row)">
              <el-button size="small" type="primary">
                更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="ship" v-if="row.orderStatus === 1">
                    发货
                  </el-dropdown-item>
                  <el-dropdown-item command="deliver" v-if="row.orderStatus === 2">
                    确认送达
                  </el-dropdown-item>
                  <el-dropdown-item command="cancel" v-if="row.orderStatus <= 1">
                    取消订单
                  </el-dropdown-item>
                  <el-dropdown-item command="refund" v-if="row.payStatus === 1 && row.orderStatus !== 6">
                    退款
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchOrders"
        @current-change="fetchOrders"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="900px"
      :close-on-click-modal="false"
    >
      <div v-if="orderDetail" v-loading="detailLoading">
        <!-- 基本信息 -->
        <el-descriptions title="订单基本信息" :column="2" border>
          <el-descriptions-item label="订单号">
            {{ orderDetail.orderSn }}
          </el-descriptions-item>
          <el-descriptions-item label="订单ID">
            {{ orderDetail.orderId }}
          </el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getOrderStatusType(orderDetail.orderStatus)">
              {{ orderDetail.orderStatusText }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="orderDetail.payStatus === 1 ? 'success' : 'warning'">
              {{ orderDetail.payStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="商品总金额">
            ¥{{ orderDetail.totalAmount }}
          </el-descriptions-item>
          <el-descriptions-item label="优惠金额">
            ¥{{ orderDetail.discountAmount }}
          </el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span style="color: #F56C6C; font-weight: bold; font-size: 16px">
              ¥{{ orderDetail.payAmount }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="团长ID">
            {{ orderDetail.leaderId }}
          </el-descriptions-item>
          <el-descriptions-item label="下单时间">
            {{ orderDetail.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="支付时间">
            {{ orderDetail.payTime || '未支付' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 商品信息 -->
        <el-divider content-position="left">商品明细</el-divider>
        <el-table :data="orderDetail.items" border style="margin-bottom: 20px">
          <el-table-column label="商品图片" width="100">
            <template #default="{ row }">
              <el-image
                :src="row.productImg"
                fit="cover"
                style="width: 60px; height: 60px; border-radius: 4px"
              />
            </template>
          </el-table-column>
          <el-table-column prop="productName" label="商品名称" />
          <el-table-column prop="price" label="单价" width="120">
            <template #default="{ row }">
              ¥{{ row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="totalPrice" label="小计" width="120">
            <template #default="{ row }">
              ¥{{ row.totalPrice }}
            </template>
          </el-table-column>
          <el-table-column label="活动类型" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.activityId" type="success">拼团商品</el-tag>
              <el-tag v-else type="info">普通商品</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 收货地址 -->
        <el-descriptions title="收货信息" :column="1" border>
          <el-descriptions-item label="收货地址ID">
            {{ orderDetail.receiveAddressId }}
          </el-descriptions-item>
          <el-descriptions-item label="详细地址">
            {{ orderDetail.receiveAddress || '未获取地址信息' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="printOrder" v-if="orderDetail">
          <el-icon><Printer /></el-icon>
          打印订单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ShoppingCart,
  Clock,
  Finished,
  CircleClose,
  Refresh,
  Download,
  Search,
  ArrowDown,
  Printer,
  LocationFilled
} from '@element-plus/icons-vue'
import {
  getOrderList,
  getOrderDetail,
  updateOrderStatus,
  batchUpdateOrderStatus,
  cancelOrder,
  getOrderStatistics,
  searchOrders,
  exportOrders
} from '../api/order'

// 统计数据
const statistics = reactive({
  total: 0,
  pending: 0,
  completed: 0,
  cancelled: 0
})

// 列表数据
const orderList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索筛选
const searchKeyword = ref('')
const statusFilter = ref(null)
const payStatusFilter = ref(null)
const dateRange = ref(null)

// 批量操作
const selectedOrders = ref([])

// 订单详情
const detailDialogVisible = ref(false)
const orderDetail = ref(null)
const detailLoading = ref(false)

// 初始化
onMounted(() => {
  fetchStatistics()
  fetchOrders()
})

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const { data } = await getOrderStatistics()
    Object.assign(statistics, data)
  } catch (error) {
    console.error('获取订单统计失败:', error)
  }
}

// 获取订单列表
const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      status: statusFilter.value,
      payStatus: payStatusFilter.value
    }

    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const { data } = await getOrderList(params)
    orderList.value = data.list || data.content || []
    total.value = data.total || data.totalElements || 0
  } catch (error) {
    ElMessage.error('获取订单列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索订单
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    fetchOrders()
    return
  }

  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const { data } = await searchOrders(searchKeyword.value, params)
    orderList.value = data.list || data.content || []
    total.value = data.total || data.totalElements || 0
  } catch (error) {
    ElMessage.error('搜索失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 按状态筛选
const handleStatusFilter = () => {
  currentPage.value = 1
  fetchOrders()
}

// 重置筛选
const resetFilters = () => {
  searchKeyword.value = ''
  statusFilter.value = null
  payStatusFilter.value = null
  dateRange.value = null
  currentPage.value = 1
  fetchOrders()
}

// 选择变更
const handleSelectionChange = (selection) => {
  selectedOrders.value = selection
}

// 批量更新状态
const batchUpdateStatus = async (status) => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请先选择订单')
    return
  }

  const statusText = status === 1 ? '发货' : '取消'
  try {
    await ElMessageBox.confirm(
      `确定要批量${statusText} ${selectedOrders.value.length} 个订单吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const orderIds = selectedOrders.value.map(order => order.orderId)
    await batchUpdateOrderStatus(orderIds, status)
    ElMessage.success(`批量${statusText}成功`)
    selectedOrders.value = []
    fetchOrders()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`批量${statusText}失败`)
      console.error(error)
    }
  }
}

// 查看订单详情
const showOrderDetail = async (orderId) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  try {
    const { data } = await getOrderDetail(orderId)
    orderDetail.value = data
  } catch (error) {
    ElMessage.error('获取订单详情失败')
    console.error(error)
  } finally {
    detailLoading.value = false
  }
}

// 订单操作
const handleOrderAction = async (command, row) => {
  const actions = {
    ship: { status: 2, text: '发货', confirmText: '确定要标记该订单为配送中吗？' },
    deliver: { status: 3, text: '确认送达', confirmText: '确定该订单已送达吗？' },
    cancel: { status: 4, text: '取消', confirmText: '确定要取消该订单吗？' },
    refund: { status: 6, text: '退款', confirmText: '确定要退款该订单吗？' }
  }

  const action = actions[command]
  if (!action) return

  try {
    await ElMessageBox.confirm(action.confirmText, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    if (command === 'cancel') {
      await cancelOrder(row.orderId)
    } else {
      await updateOrderStatus(row.orderId, action.status)
    }

    ElMessage.success(`${action.text}成功`)
    fetchOrders()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action.text}失败`)
      console.error(error)
    }
  }
}

// 导出订单
const handleExport = async () => {
  try {
    const params = {
      status: statusFilter.value,
      payStatus: payStatusFilter.value
    }

    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const response = await exportOrders(params)
    const blob = new Blob([response], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `订单列表_${new Date().getTime()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
    console.error(error)
  }
}

// 打印订单
const printOrder = () => {
  window.print()
}

// 获取订单状态类型
const getOrderStatusType = (status) => {
  const types = {
    0: 'warning', // 待支付
    1: 'primary', // 待发货
    2: 'info',    // 配送中
    3: 'success', // 已送达
    4: 'danger',  // 已取消
    5: 'warning', // 退款中
    6: 'info'     // 已退款
  }
  return types[status] || 'info'
}
</script>

<style scoped>
.order-manage {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-row {
  margin-bottom: 20px;
}

.batch-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.batch-buttons {
  display: flex;
  gap: 10px;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-item {
  display: flex;
  align-items: center;
}

.item-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.item-info {
  font-size: 12px;
  color: #909399;
}

/* 收货信息单元格样式 */
.address-cell {
  line-height: 1.6;
}

.receiver-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.phone-text {
  font-size: 12px;
  color: #606266;
}

.address-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #303133;
}

.address-text .el-icon {
  color: #409eff;
  font-size: 14px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-pagination) {
  display: flex;
}
</style>

