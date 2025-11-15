<template>
  <div class="order-ship">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单发货管理</span>
          <div>
            <el-button 
              type="primary" 
              :disabled="selectedOrders.length === 0"
              @click="showBatchShipDialog"
            >
              <el-icon><Van /></el-icon>
              批量发货（{{ selectedOrders.length }}）
            </el-button>
            <el-button @click="fetchPendingOrders">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-statistic title="待发货订单" :value="pendingOrderCount">
            <template #suffix>个</template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="已选订单" :value="selectedOrders.length">
            <template #suffix>个</template>
          </el-statistic>
        </el-col>
      </el-row>

      <!-- 筛选条件 -->
      <el-row :gutter="20" class="search-row">
        <el-col :span="6">
          <el-input 
            v-model="searchOrderSn" 
            placeholder="订单编号搜索" 
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="filterLeaderId" placeholder="团长筛选" clearable @change="handleSearch">
            <el-option label="全部团长" :value="null" />
            <!-- 动态加载团长列表 -->
          </el-select>
        </el-col>
      </el-row>

      <!-- 订单列表 -->
      <el-table
        ref="orderTableRef"
        :data="filteredOrders"
        v-loading="loading"
        border
        style="width: 100%; margin-top: 20px"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderId" label="订单ID" width="80" />
        <el-table-column prop="orderSn" label="订单编号" width="180" />
        <el-table-column label="商品信息" min-width="180">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image
                v-if="row.productImg"
                :src="row.productImg"
                fit="cover"
                style="width: 40px; height: 40px; border-radius: 4px; margin-right: 8px"
              />
              <div class="product-info">
                <div class="product-name">{{ row.productName || '未知商品' }}</div>
                <div class="product-quantity">x{{ row.quantity || 1 }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户信息" width="120">
          <template #default="{ row }">
            <div>
              <div>ID: {{ row.userId }}</div>
              <div v-if="row.userName" style="color: #606266; font-size: 12px">
                {{ row.userName }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="团长信息" width="100">
          <template #default="scope">
            <div>ID: {{ scope.row.leaderId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="收货信息" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="address-cell">
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
                {{ row.receiveAddress || '地址信息加载中...' }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" width="120">
          <template #default="scope">
            <el-text type="danger">¥{{ scope.row.payAmount }}</el-text>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="100">
          <template #default>
            <el-tag type="warning">待发货</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model="currentPage"
        :page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="fetchPendingOrders"
        @current-change="fetchPendingOrders"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 批量发货配置对话框 -->
    <el-dialog
      v-model="batchShipDialogVisible"
      title="批量发货配置"
      width="600px"
      @close="resetBatchShipForm"
    >
      <el-form
        ref="batchShipFormRef"
        :model="batchShipForm"
        :rules="batchShipRules"
        label-width="120px"
      >
        <el-form-item label="发货方式" prop="deliveryMode">
          <el-radio-group v-model="batchShipForm.deliveryMode">
            <el-radio :label="1">
              <el-icon><OfficeBuilding /></el-icon>
              团长团点模式
            </el-radio>
            <el-radio :label="2">
              <el-icon><Location /></el-icon>
              用户地址模式
            </el-radio>
          </el-radio-group>
          <div class="form-help-text">
            <el-text size="small" type="info">
              团长团点：货物送到团长处，团长分发（适合拼团订单）<br/>
              用户地址：货物直接送到用户家（适合紧急订单）
            </el-text>
          </div>
        </el-form-item>

        <el-form-item label="起点仓库" prop="warehouseId">
          <el-select v-model="batchShipForm.warehouseId" placeholder="选择起点仓库" style="width: 100%">
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            >
              <span>{{ warehouse.warehouseName }}</span>
              <span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">
                {{ warehouse.address }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="终点仓库" prop="endWarehouseId">
          <el-select 
            v-model="batchShipForm.endWarehouseId" 
            placeholder="选择终点仓库（可选，不回仓库则不选）" 
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="warehouse in warehouseList"
              :key="warehouse.id"
              :label="warehouse.warehouseName"
              :value="warehouse.id"
            />
          </el-select>
          <div class="form-help-text">
            <el-text size="small" type="info">
              选择终点仓库后，配送路径将形成回路
            </el-text>
          </div>
        </el-form-item>

        <el-form-item label="使用高德API">
          <el-switch v-model="useGaodeMapAPI" />
          <el-text size="small" type="info" style="margin-left: 10px">
            开启后使用高德地图路径规划（途经点≤16个，显示详细导航信息）
          </el-text>
        </el-form-item>

        <el-form-item label="路径策略" prop="routeStrategy">
          <el-radio-group v-model="batchShipForm.routeStrategy">
            <el-radio :label="1">最短距离</el-radio>
            <el-radio :label="0">最短时间（速度最快）</el-radio>
            <el-radio :label="2">避开拥堵</el-radio>
          </el-radio-group>
          <div class="form-help-text" v-if="useGaodeMapAPI">
            <el-text size="small" type="warning">
              高德API模式：会显示详细导航信息（如"向东南行驶103米左转"）
            </el-text>
          </div>
        </el-form-item>

        <el-form-item label="订单数量">
          <el-text>{{ selectedOrders.length }} 个订单</el-text>
        </el-form-item>

        <el-form-item label="预估途经点">
          <el-text type="primary">
            {{ estimatedWaypoints }} 个途经点
          </el-text>
          <el-text 
            v-if="useGaodeMapAPI && estimatedWaypoints > 16" 
            type="danger" 
            style="margin-left: 10px"
          >
            （高德API最多16个，请分批或使用Dijkstra）
          </el-text>
          <el-text 
            v-else-if="!useGaodeMapAPI && estimatedWaypoints > 30" 
            type="danger" 
            style="margin-left: 10px"
          >
            （超过30个，建议分批发货）
          </el-text>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchShipDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleBatchShip" :loading="submitting">
            确认发货
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 发货结果对话框 -->
    <el-dialog
      v-model="resultDialogVisible"
      title="发货结果"
      width="1200px"
    >
      <div v-if="shipResult">
        <el-result
          icon="success"
          title="批量发货成功"
          :sub-title="`已生成配送单，配送单号：${shipResult.dispatchGroup}`"
        />

        <!-- 地图展示配送路径 -->
        <DeliveryRouteMap
          v-if="mapResultData.startPoint"
          :startPoint="mapResultData.startPoint"
          :waypoints="mapResultData.waypoints"
          :endPoint="mapResultData.endPoint"
          :strategy="batchShipForm.routeStrategy"
          :useGaodeAPI="useGaodeMapAPI"
        />
        
        <el-divider>配送信息</el-divider>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="配送单ID">
            {{ shipResult.deliveryId }}
          </el-descriptions-item>
          <el-descriptions-item label="发货方式">
            {{ shipResult.deliveryMode === 1 ? '团长团点模式' : '用户地址模式' }}
          </el-descriptions-item>
          <el-descriptions-item label="途经点数量">
            {{ shipResult.waypointCount }} 个
          </el-descriptions-item>
          <el-descriptions-item label="总距离">
            {{ (shipResult.totalDistance / 1000).toFixed(2) }} 公里
          </el-descriptions-item>
          <el-descriptions-item label="预估时间">
            {{ shipResult.estimatedDuration }} 分钟
          </el-descriptions-item>
          <el-descriptions-item label="使用算法">
            {{ shipResult.algorithmUsed === 'dijkstra' ? 'Dijkstra算法' : '高德API' }}
          </el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 20px">
          <el-text type="info">订单状态已更新为"配送中"</el-text>
        </div>
      </div>

      <template #footer>
        <el-button type="primary" @click="handleViewDeliveryDetail">查看配送单详情</el-button>
        <el-button @click="resultDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search, Van, OfficeBuilding, Location, LocationFilled } from '@element-plus/icons-vue'
import { getPendingDeliveryOrders, batchShipOrders, getActiveWarehouses } from '@/api/delivery'
import { useRouter } from 'vue-router'
import DeliveryRouteMap from '@/components/DeliveryRouteMap.vue'

const router = useRouter()

// 数据
const loading = ref(false)
const pendingOrders = ref([])
const selectedOrders = ref([])
const warehouseList = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchOrderSn = ref('')
const filterLeaderId = ref(null)

// 对话框
const batchShipDialogVisible = ref(false)
const resultDialogVisible = ref(false)
const submitting = ref(false)
const batchShipFormRef = ref(null)
const orderTableRef = ref(null)

// 批量发货表单
const batchShipForm = ref({
  orderIds: [],
  deliveryMode: 1,
  warehouseId: null,
  endWarehouseId: null,
  routeStrategy: 1,
  createdBy: null
})

// 发货结果
const shipResult = ref(null)

// 是否使用高德API
const useGaodeMapAPI = ref(false)

// 表单验证规则
const batchShipRules = {
  deliveryMode: [
    { required: true, message: '请选择发货方式', trigger: 'change' }
  ],
  warehouseId: [
    { required: true, message: '请选择起点仓库', trigger: 'change' }
  ]
}

// 计算属性
const pendingOrderCount = computed(() => pendingOrders.value.length)

const filteredOrders = computed(() => {
  let orders = pendingOrders.value

  // 按订单编号搜索
  if (searchOrderSn.value) {
    orders = orders.filter(order => 
      order.orderSn.includes(searchOrderSn.value)
    )
  }

  // 按团长筛选
  if (filterLeaderId.value) {
    orders = orders.filter(order => 
      order.leaderId === filterLeaderId.value
    )
  }

  return orders
})

const estimatedWaypoints = computed(() => {
  if (batchShipForm.value.deliveryMode === 1) {
    // 团长团点模式：去重团长ID
    const uniqueLeaders = new Set(selectedOrders.value.map(o => o.leaderId))
    return uniqueLeaders.size
  } else {
    // 用户地址模式：去重地址ID
    const uniqueAddresses = new Set(selectedOrders.value.map(o => o.receiveAddressId))
    return uniqueAddresses.size
  }
})

// 地图数据（发货结果）
const mapResultData = computed(() => {
  if (!shipResult.value) return {}
  
  // 从仓库列表中找到起点仓库
  const startWarehouse = warehouseList.value.find(w => w.id === batchShipForm.value.warehouseId)
  const endWarehouse = batchShipForm.value.endWarehouseId 
    ? warehouseList.value.find(w => w.id === batchShipForm.value.endWarehouseId)
    : null
  
  return {
    startPoint: startWarehouse ? {
      longitude: startWarehouse.longitude,
      latitude: startWarehouse.latitude,
      name: startWarehouse.warehouseName
    } : null,
    waypoints: (shipResult.value.waypoints || []).map(wp => ({
      longitude: wp.longitude,
      latitude: wp.latitude,
      name: wp.address
    })),
    endPoint: endWarehouse ? {
      longitude: endWarehouse.longitude,
      latitude: endWarehouse.latitude,
      name: endWarehouse.warehouseName
    } : null
  }
})

// 方法
const fetchPendingOrders = async () => {
  try {
    loading.value = true
    const res = await getPendingDeliveryOrders({
      page: currentPage.value - 1,
      size: pageSize.value
    })

    if (res.code === 200 && res.data) {
      pendingOrders.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取待发货订单失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const fetchWarehouses = async () => {
  try {
    const res = await getActiveWarehouses()
    if (res.code === 200) {
      warehouseList.value = res.data || []
      
      // 自动选择默认仓库
      const defaultWarehouse = warehouseList.value.find(w => w.isDefault === 1)
      if (defaultWarehouse) {
        batchShipForm.value.warehouseId = defaultWarehouse.id
      }
    }
  } catch (error) {
    console.error('获取仓库列表失败:', error)
  }
}

const handleSelectionChange = (selection) => {
  selectedOrders.value = selection
}

const handleSearch = () => {
  // 触发计算属性重新计算
}

const showBatchShipDialog = () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请先选择要发货的订单')
    return
  }

  batchShipForm.value.orderIds = selectedOrders.value.map(o => o.orderId)
  batchShipDialogVisible.value = true
}

const handleBatchShip = async () => {
  try {
    // 表单验证
    await batchShipFormRef.value.validate()

    // 确认对话框
    await ElMessageBox.confirm(
      `确定要发货 ${selectedOrders.value.length} 个订单吗？`,
      '批量发货确认',
      {
        confirmButtonText: '确定发货',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true

    // 调用批量发货接口
    const res = await batchShipOrders(batchShipForm.value)

    if (res.code === 200) {
      ElMessage.success('批量发货成功！')
      shipResult.value = res.data
      
      batchShipDialogVisible.value = false
      resultDialogVisible.value = true

      // 刷新订单列表
      fetchPendingOrders()

      // 清空选中
      selectedOrders.value = []
      orderTableRef.value.clearSelection()
    } else {
      ElMessage.error(res.message || '批量发货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量发货失败:', error)
      ElMessage.error(error.message || '批量发货失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

const handleViewDeliveryDetail = () => {
  resultDialogVisible.value = false
  router.push({
    name: 'DeliveryList',
    params: { deliveryId: shipResult.value.deliveryId }
  })
}

const resetBatchShipForm = () => {
  batchShipFormRef.value?.resetFields()
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchPendingOrders()
  fetchWarehouses()
})
</script>

<style scoped>
.order-ship {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-row {
  margin-bottom: 20px;
}

.search-row {
  margin-top: 20px;
}

.form-help-text {
  margin-top: 8px;
  line-height: 1.6;
}

/* 商品信息单元格 */
.product-cell {
  display: flex;
  align-items: center;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-quantity {
  font-size: 12px;
  color: #909399;
}

/* 收货信息单元格 */
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
</style>

