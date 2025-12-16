<template>
  <div class="delivery-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>配送单管理</span>
          <div>
            <el-button @click="fetchDeliveries">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="待分配" :value="statistics.pendingDeliveries || 0">
              <template #suffix>单</template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="配送中" :value="statistics.shippingDeliveries || 0">
              <template #suffix>单</template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="已完成" :value="statistics.completedDeliveries || 0">
              <template #suffix>单</template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic 
              title="累计距离" 
              :value="Number(((statistics.totalDistance || 0) / 1000).toFixed(2))"
              :precision="2"
            >
              <template #suffix>公里</template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>

      <!-- 筛选条件 -->
      <el-row :gutter="20" class="search-row">
        <el-col :span="6">
          <el-select v-model="statusFilter" placeholder="状态筛选" @change="fetchDeliveries">
            <el-option label="全部状态" :value="null" />
            <el-option label="待分配" :value="0" />
            <el-option label="配送中" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-input 
            v-model="searchDispatchGroup" 
            placeholder="分单组编号搜索" 
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
      </el-row>

      <!-- 配送单列表 -->
      <el-table
        :data="deliveryList"
        v-loading="loading"
        border
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="deliveryId" label="配送单ID" width="100" />
        <el-table-column prop="dispatchGroup" label="分单组编号" width="200" />
        <el-table-column label="发货方式" width="120">
          <template #default="{ row }">
            <el-tag :type="row.deliveryMode === 1 ? 'primary' : 'success'">
              {{ row.deliveryMode === 1 ? '团长团点' : '用户地址' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="途经点" width="100">
          <template #default="{ row }">
            {{ row.waypointCount }} 个
          </template>
        </el-table-column>
        <el-table-column label="总距离" width="120">
          <template #default="{ row }">
            {{ (row.distance / 1000).toFixed(2) }} 公里
          </template>
        </el-table-column>
        <el-table-column label="预估时间" width="100">
          <template #default="{ row }">
            {{ row.estimatedDuration }} 分钟
          </template>
        </el-table-column>
        <el-table-column label="算法" width="120">
          <template #default="{ row }">
            <el-tag size="small">
              {{ row.algorithmUsed === 'dijkstra' ? 'Dijkstra' : '高德API' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="600" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetailDialog(row)">
              详情
            </el-button>
            <el-button 
              size="small" 
              type="warning"
              @click="handleReplan(row)"
              v-if="row.status === 1"
            >
              重新规划
            </el-button>
            <el-button 
              size="small" 
              type="success"
              @click="handleComplete(row)"
              v-if="row.status === 1"
            >
              完成配送
            </el-button>
            <el-button 
              size="small" 
              type="danger"
              @click="handleCancel(row)"
              v-if="row.status !== 2"
            >
              取消
            </el-button>
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
        @size-change="fetchDeliveries"
        @current-change="fetchDeliveries"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 配送单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="配送单详情"
      width="1200px"
    >
      <div v-if="currentDelivery">
        <!-- 地图展示（开启高德API导航） -->
        <DeliveryRouteMap
          v-if="mapData.startPoint"
          :startPoint="mapData.startPoint"
          :waypoints="mapData.waypoints"
          :endPoint="mapData.endPoint"
          :strategy="currentDelivery.routeStrategy"
          :useGaodeAPI="true"
        />
        
        <el-alert 
          type="info" 
          :closable="false"
          style="margin-top: 10px"
        >
          <template #title>
            <span>使用高德地图驾车导航规划，右侧显示详细导航信息</span>
          </template>
        </el-alert>
        
        <el-divider>配送单信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="配送单ID">
            {{ currentDelivery.deliveryId }}
          </el-descriptions-item>
          <el-descriptions-item label="分单组编号">
            {{ currentDelivery.dispatchGroup }}
          </el-descriptions-item>
          <el-descriptions-item label="发货方式">
            {{ currentDelivery.deliveryMode === 1 ? '团长团点模式' : '用户地址模式' }}
          </el-descriptions-item>
          <el-descriptions-item label="配送状态">
            <el-tag :type="getStatusType(currentDelivery.status)">
              {{ getStatusText(currentDelivery.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="途经点数量">
            {{ currentDelivery.waypointCount }} 个
          </el-descriptions-item>
          <el-descriptions-item label="总距离">
            {{ (currentDelivery.totalDistance / 1000).toFixed(2) }} 公里
          </el-descriptions-item>
          <el-descriptions-item label="预估时间">
            {{ currentDelivery.estimatedDuration }} 分钟
          </el-descriptions-item>
          <el-descriptions-item label="使用算法">
            {{ currentDelivery.algorithmUsed === 'dijkstra' ? 'Dijkstra算法' : '高德API' }}
          </el-descriptions-item>
          <el-descriptions-item label="起点仓库">
            {{ currentDelivery.startWarehouse?.warehouseName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="终点仓库">
            {{ currentDelivery.endWarehouse?.warehouseName || '不回仓库' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatDate(currentDelivery.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="完成时间">
            {{ formatDate(currentDelivery.endTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 途经点列表 -->
        <el-divider>途经点列表</el-divider>
        <el-table 
          :data="currentDelivery.waypoints" 
          border 
          size="small"
          max-height="300"
        >
          <el-table-column prop="sequence" label="序号" width="80" />
          <el-table-column prop="orderId" label="订单ID" width="100" />
          <el-table-column prop="address" label="地址" min-width="200" />
          <el-table-column prop="receiverName" label="收件人" width="100" />
          <el-table-column prop="receiverPhone" label="电话" width="130" />
          <el-table-column prop="type" label="类型" width="120">
            <template #default="{ row }">
              <el-tag size="small">
                {{ row.type === 'leader_store' ? '团长团点' : '用户地址' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <!-- 订单列表 -->
        <el-divider>关联订单列表</el-divider>
        <el-table 
          :data="currentDelivery.orders" 
          border 
          size="small"
          max-height="300"
        >
          <el-table-column prop="orderId" label="订单ID" width="100" />
          <el-table-column prop="orderSn" label="订单编号" width="180" />
          <el-table-column prop="userId" label="用户ID" width="100" />
          <el-table-column prop="payAmount" label="订单金额" width="120">
            <template #default="{ row }">
              ¥{{ row.payAmount }}
            </template>
          </el-table-column>
          <el-table-column label="订单状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.orderStatus === 2 ? 'primary' : 'success'">
                {{ row.orderStatus === 2 ? '配送中' : '已送达' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import { 
  getDeliveryList, 
  getDeliveryDetail,
  replanRoute,
  completeDelivery,
  cancelDelivery,
  getStatisticsOverview
} from '@/api/delivery'
import DeliveryRouteMap from '@/components/DeliveryRouteMap.vue'

// 数据
const loading = ref(false)
const deliveryList = ref([])
const currentDelivery = ref(null)
const statistics = ref({})
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const statusFilter = ref(null)
const searchDispatchGroup = ref('')

// 对话框
const detailDialogVisible = ref(false)

// 地图数据
const mapData = computed(() => {
  if (!currentDelivery.value) return {}
  
  return {
    startPoint: currentDelivery.value.startWarehouse ? {
      longitude: currentDelivery.value.startWarehouse.longitude,
      latitude: currentDelivery.value.startWarehouse.latitude,
      name: currentDelivery.value.startWarehouse.warehouseName
    } : null,
    waypoints: (currentDelivery.value.waypoints || []).map(wp => ({
      longitude: wp.longitude,
      latitude: wp.latitude,
      name: wp.address
    })),
    endPoint: currentDelivery.value.endWarehouse ? {
      longitude: currentDelivery.value.endWarehouse.longitude,
      latitude: currentDelivery.value.endWarehouse.latitude,
      name: currentDelivery.value.endWarehouse.warehouseName
    } : null
  }
})

// 方法
const fetchDeliveries = async () => {
  try {
    loading.value = true
    const res = await getDeliveryList({
      status: statusFilter.value,
      page: currentPage.value - 1,
      size: pageSize.value
    })

    if (res.code === 200 && res.data) {
      deliveryList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取配送单列表失败:', error)
    ElMessage.error('获取配送单列表失败')
  } finally {
    loading.value = false
  }
}

const fetchStatistics = async () => {
  try {
    const res = await getStatisticsOverview()
    if (res.code === 200) {
      statistics.value = res.data || {}
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const showDetailDialog = async (row) => {
  try {
    const res = await getDeliveryDetail(row.deliveryId)
    if (res.code === 200) {
      currentDelivery.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取配送单详情失败:', error)
    ElMessage.error('获取配送单详情失败')
  }
}

const handleReplan = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要重新规划配送路径吗？',
      '重新规划确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await replanRoute(row.deliveryId)
    if (res.code === 200) {
      ElMessage.success({
        message: '路径重新规划成功！',
        duration: 2000
      })
      
      // 显示新路径信息
      ElMessageBox.alert(
        `新距离：${(res.data.totalDistance / 1000).toFixed(2)} 公里\n` +
        `新时间：${res.data.estimatedDuration} 分钟`,
        '规划结果',
        { confirmButtonText: '确定' }
      )
      
      fetchDeliveries()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重新规划失败:', error)
      ElMessage.error('重新规划失败')
    }
  }
}

const handleComplete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要完成配送吗？订单状态将更新为"已送达"',
      '完成配送确认',
      {
        confirmButtonText: '确定完成',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    const res = await completeDelivery(row.deliveryId)
    if (res.code === 200) {
      ElMessage.success('配送已完成')
      fetchDeliveries()
      fetchStatistics()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('完成配送失败:', error)
      ElMessage.error('完成配送失败')
    }
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要取消配送吗？配送单将被删除',
      '取消配送确认',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '返回',
        type: 'error'
      }
    )

    const res = await cancelDelivery(row.deliveryId)
    if (res.code === 200) {
      ElMessage.success('配送已取消')
      fetchDeliveries()
      fetchStatistics()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消配送失败:', error)
      ElMessage.error('取消配送失败')
    }
  }
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'primary',
    2: 'success'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    0: '待分配',
    1: '配送中',
    2: '已完成'
  }
  return textMap[status] || '未知'
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchDeliveries()
  fetchStatistics()
})
</script>

<style scoped>
.delivery-list {
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
</style>

