<template>
  <div class="delivery-manage">
    <!-- 顶部导航 -->
    <TopNav />
    
    <div class="delivery-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1>配送管理</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/leader/dashboard' }">工作台</el-breadcrumb-item>
          <el-breadcrumb-item>配送管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <!-- 待配送订单 -->
      <el-card>
        <template #header>
          <div class="card-header">
            <span class="card-title">
              <el-icon><Box /></el-icon>
              待配送订单（{{ deliveryOrders.length }}）
            </span>
            <el-button 
              type="primary"
              :disabled="selectedOrders.length === 0"
              @click="generateRoute"
            >
              <el-icon><Location /></el-icon>
              生成配送路线（{{ selectedOrders.length }}）
            </el-button>
          </div>
        </template>
        
        <el-table 
          v-if="deliveryOrders.length"
          :data="deliveryOrders" 
          stripe
          @selection-change="handleSelectionChange"
          style="width: 100%"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="orderSn" label="订单编号" width="150" />
          <el-table-column prop="teamNo" label="团号" width="140" />
          <el-table-column prop="userName" label="用户" width="100" />
          <el-table-column prop="productName" label="商品" width="150" />
          <el-table-column label="数量" width="80" align="center">
            <template #default="{ row }">
              {{ row.quantity }}
            </template>
          </el-table-column>
          <el-table-column label="金额" width="100">
            <template #default="{ row }">
              ¥{{ row.totalAmount }}
            </template>
          </el-table-column>
          <el-table-column label="配送地址" min-width="250">
            <template #default="{ row }">
              <div class="address-cell">
                <el-icon><Location /></el-icon>
                {{ row.address }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="orderTime" label="下单时间" width="180" />
        </el-table>
        
        <el-empty 
          v-else 
          description="暂无待配送订单" 
          :image-size="100"
        />
      </el-card>
      
      <!-- 配送路线对话框 -->
      <el-dialog 
        v-model="routeDialogVisible" 
        title="配送路线参考" 
        width="70%"
        :close-on-click-modal="false"
      >
        <div class="route-container">
          <!-- 提示信息 -->
          <el-alert 
            type="info" 
            :closable="false"
            class="route-alert"
          >
            <template #title>
              <div class="alert-content">
                <el-icon><InfoFilled /></el-icon>
                <span>以下为系统推荐的配送路径，实际配送可根据情况调整</span>
              </div>
            </template>
          </el-alert>
          
          <!-- 统计信息卡片 -->
          <el-row :gutter="20" class="route-stats">
            <el-col :span="8">
              <el-card shadow="hover" class="stat-card">
                <el-statistic title="总距离" :value="routeData?.totalDistance" suffix="km" />
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="stat-card">
                <el-statistic title="预计时间" :value="routeData?.estimatedTime" suffix="分钟" />
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="stat-card">
                <el-statistic title="订单数" :value="routeData?.orderCount" suffix="个" />
              </el-card>
            </el-col>
          </el-row>
          
          <!-- 路径时间线 -->
          <el-card class="route-timeline-card">
            <template #header>
              <span class="card-title">
                <el-icon><Guide /></el-icon>
                配送路径
              </span>
            </template>
            
            <el-timeline>
              <el-timeline-item 
                v-for="(point, index) in routeData?.points" 
                :key="index"
                :type="index === 0 ? 'primary' : 'success'"
                :hollow="index === 0"
                :size="index === 0 ? 'large' : 'normal'"
              >
                <div class="route-point">
                  <div class="point-header">
                    <h4>{{ point.location }}</h4>
                    <el-tag v-if="index === 0" type="primary" size="small">起点</el-tag>
                    <el-tag v-else type="success" size="small">第{{ index }}站</el-tag>
                  </div>
                  <div class="point-address">{{ point.address }}</div>
                  <div v-if="point.orders.length > 0" class="point-orders">
                    <span class="orders-label">订单：</span>
                    <el-tag 
                      v-for="orderId in point.orders" 
                      :key="orderId"
                      size="small"
                      class="order-tag"
                    >
                      #{{ orderId }}
                    </el-tag>
                  </div>
                  <div v-if="index > 0" class="point-distance">
                    <el-icon><Navigation /></el-icon>
                    距离上一站：{{ point.distance }}km
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
          </el-card>
        </div>
        
        <template #footer>
          <el-button @click="routeDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="startDelivery">
            <el-icon><Van /></el-icon>
            开始配送
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLeaderStore } from '@/stores/leader'
import TopNav from '@/components/common/TopNav.vue'
import { ElMessage } from 'element-plus'
import {
  Box,
  Location,
  InfoFilled,
  Guide,
  Navigation,
  Van
} from '@element-plus/icons-vue'

const router = useRouter()
const leaderStore = useLeaderStore()

const deliveryOrders = ref([])
const selectedOrders = ref([])
const routeDialogVisible = ref(false)
const routeData = ref(null)

// 加载配送订单
onMounted(async () => {
  try {
    deliveryOrders.value = await leaderStore.fetchDeliveryOrders()
  } catch (error) {
    console.error('加载配送订单失败', error)
  }
})

// 选择变化
const handleSelectionChange = (selection) => {
  selectedOrders.value = selection
}

// 生成配送路线
const generateRoute = async () => {
  if (selectedOrders.value.length === 0) {
    ElMessage.warning('请选择需要配送的订单')
    return
  }
  
  try {
    routeData.value = await leaderStore.generateRoute(selectedOrders.value)
    routeDialogVisible.value = true
  } catch (error) {
    console.error('生成路线失败', error)
  }
}

// 开始配送
const startDelivery = () => {
  ElMessage.success('配送路线已确认，请按推荐路径进行配送')
  routeDialogVisible.value = false
  // 实际项目中，这里应该调用API更新订单状态
}
</script>

<style scoped>
.delivery-container {
  max-width: 1400px;
  margin: 80px auto 20px;
  padding: 20px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

/* 地址单元格 */
.address-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 路线对话框 */
.route-container {
  padding: 20px 0;
}

.route-alert {
  margin-bottom: 20px;
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

/* 统计卡片 */
.route-stats {
  margin-bottom: 30px;
}

.stat-card {
  text-align: center;
}

.stat-card :deep(.el-statistic__head) {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-card :deep(.el-statistic__content) {
  font-size: 24px;
  font-weight: 600;
  color: #409EFF;
}

/* 路径时间线卡片 */
.route-timeline-card {
  margin-top: 20px;
}

/* 路径点 */
.route-point {
  padding: 8px 0;
}

.point-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.point-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.point-address {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.point-orders {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.orders-label {
  font-size: 13px;
  color: #909399;
}

.order-tag {
  margin: 2px;
}

.point-distance {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #67C23A;
  font-weight: 500;
}

/* 空状态 */
:deep(.el-empty) {
  padding: 60px 0;
}

/* 对话框优化 */
:deep(.el-dialog__body) {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}
</style>
