<template>
  <div class="delivery-manage-wrapper">
    <div class="delivery-manage-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>配送管理</h2>
        <p class="subtitle">管理配送路径和配送进度</p>
      </div>

      <!-- 功能说明 -->
      <el-alert type="info" :closable="false" style="margin-bottom: 20px;">
        <template #title>
          📢 此功能需要DeliveryService后端支持，当前显示测试布局结构
        </template>
        <div style="margin-top: 8px; font-size: 13px;">
          <p>• 需要开发DeliveryService服务</p>
          <p>• API接口：GET /api/delivery/pending, POST /api/delivery/route</p>
          <p>• 配送路径规划使用贪心算法或Dijkstra算法</p>
        </div>
      </el-alert>

      <!-- 配送概览 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6" :xs="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #409eff;">
                <el-icon :size="24"><Van /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">测试</div>
                <div class="stat-label">待配送订单</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #67c23a;">
                <el-icon :size="24"><Position /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">测试</div>
                <div class="stat-label">预计距离(km)</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #e6a23c;">
                <el-icon :size="24"><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">测试</div>
                <div class="stat-label">预计耗时(分钟)</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: #f56c6c;">
                <el-icon :size="24"><Location /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">测试</div>
                <div class="stat-label">配送地点</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 日期选择和操作 -->
      <el-card class="action-card">
        <el-form :inline="true">
          <el-form-item label="配送日期">
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              disabled
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Guide" disabled>生成配送路线</el-button>
            <el-button type="success" :icon="Download" disabled>导出配送单</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 配送路径展示 -->
      <el-card class="route-card">
        <template #header>
          <div class="card-header">
            <span><el-icon><Map /></el-icon> 配送路径参考（贪心算法）</span>
          </div>
        </template>

        <!-- 地图占位 -->
        <div class="map-placeholder">
          <el-empty description="地图功能待开发">
            <template #image>
              <el-icon :size="100" color="#c0c4cc"><Map /></el-icon>
            </template>
            <p style="color: #909399; font-size: 14px;">
              此处将显示配送路径地图<br/>
              使用高德地图API + 贪心算法优化配送路线
            </p>
          </el-empty>
        </div>

        <!-- 路径列表 -->
        <el-divider />
        
        <div class="route-list">
          <h4>配送路径详情</h4>
          <el-timeline>
            <el-timeline-item
              v-for="(point, index) in testRouteData"
              :key="index"
              :icon="point.icon"
              :type="point.type"
              :color="point.color"
            >
              <div class="route-point">
                <div class="point-header">
                  <span class="point-index">{{ point.index }}</span>
                  <span class="point-name">{{ point.name }}</span>
                  <el-tag size="small" type="info">{{ point.distance }}</el-tag>
                </div>
                <div class="point-info">
                  <p>地址：{{ point.address }}</p>
                  <p>订单：{{ point.orders }}</p>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-card>

      <!-- 配送列表 -->
      <el-card class="delivery-list-card">
        <template #header>
          <div class="card-header">
            <span><el-icon><List /></el-icon> 待配送订单</span>
          </div>
        </template>

        <el-table :data="testDeliveryData" border stripe>
          <el-table-column type="index" label="#" width="50" />
          <el-table-column prop="orderSn" label="订单号" width="180" />
          <el-table-column prop="userName" label="收货人" width="100" />
          <el-table-column prop="phone" label="联系电话" width="120" />
          <el-table-column prop="address" label="收货地址" min-width="200" show-overflow-tooltip />
          <el-table-column prop="productInfo" label="商品信息" width="150" />
          <el-table-column label="操作" width="150">
            <template #default>
              <el-button size="small" disabled>查看</el-button>
              <el-button size="small" type="success" disabled>完成</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { 
  Van, 
  Position, 
  Clock, 
  Location, 
  Guide, 
  Download, 
  Map, 
  List 
} from '@element-plus/icons-vue'

const selectedDate = ref(new Date())

// 测试路径数据
const testRouteData = ref([
  {
    index: '起点',
    name: '测试团点',
    address: '测试地址 - 起点位置',
    orders: '-',
    distance: '0km',
    icon: 'Shop',
    type: 'primary',
    color: '#409eff'
  },
  {
    index: '第1站',
    name: '测试用户A',
    address: '测试地址 - 配送点A',
    orders: '订单001, 订单002',
    distance: '+0.5km',
    icon: 'User',
    type: 'success'
  },
  {
    index: '第2站',
    name: '测试用户B',
    address: '测试地址 - 配送点B',
    orders: '订单003',
    distance: '+0.3km',
    icon: 'User',
    type: 'success'
  },
  {
    index: '第3站',
    name: '测试用户C',
    address: '测试地址 - 配送点C',
    orders: '订单004, 订单005',
    distance: '+0.8km',
    icon: 'User',
    type: 'success'
  }
])

// 测试配送数据
const testDeliveryData = ref([
  {
    orderSn: '测试订单001',
    userName: '测试用户A',
    phone: '138****1234',
    address: '测试地址 - 北京市朝阳区测试社区88号',
    productInfo: '测试商品 x2'
  },
  {
    orderSn: '测试订单002',
    userName: '测试用户B',
    phone: '139****5678',
    address: '测试地址 - 北京市海淀区测试小区15号',
    productInfo: '测试商品 x1'
  },
  {
    orderSn: '测试订单003',
    userName: '测试用户C',
    phone: '137****9012',
    address: '测试地址 - 北京市西城区测试花园23号',
    productInfo: '测试商品 x3'
  }
])
</script>

<style scoped>
.delivery-manage-wrapper {
  min-height: 100vh;
  padding-top: 84px;
  background-color: #f5f5f5;
}

.delivery-manage-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #909399;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card :deep(.el-card__body) {
  padding: 15px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.action-card,
.route-card,
.delivery-list-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
}

.map-placeholder {
  height: 400px;
  background-color: #f5f7fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.route-list h4 {
  margin: 0 0 15px 0;
  color: #333;
}

.route-point {
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.point-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.point-index {
  font-weight: bold;
  color: #409eff;
}

.point-name {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.point-info {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.point-info p {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .delivery-manage-wrapper {
    padding-top: 76px;
  }

  .delivery-manage-container {
    padding: 10px;
  }

  .map-placeholder {
    height: 300px;
  }
}
</style>

