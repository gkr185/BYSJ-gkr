<template>
  <div class="order-manage-wrapper">
    <div class="order-manage-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>订单管理</h2>
        <p class="subtitle">管理归属您的所有订单</p>
      </div>

      <!-- 功能说明 -->
      <el-alert type="info" :closable="false" style="margin-bottom: 20px;">
        <template #title>
          📢 此功能需要OrderService后端支持，当前显示测试布局结构
        </template>
        <div style="margin-top: 8px; font-size: 13px;">
          <p>• 需要开发OrderService服务</p>
          <p>• API接口：GET /api/order/leader/orders</p>
          <p>• 组件UI结构已完整开发，后端完成后可直接对接</p>
        </div>
      </el-alert>

      <!-- 订单统计 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6" :xs="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">测试</div>
              <div class="stat-label">待发货</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">测试</div>
              <div class="stat-label">配送中</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">测试</div>
              <div class="stat-label">已送达</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-value">测试</div>
              <div class="stat-label">总订单</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 筛选器 -->
      <el-card class="filter-card">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="订单状态">
            <el-select v-model="filterForm.status" placeholder="全部状态">
              <el-option label="全部" :value="null" />
              <el-option label="待发货" :value="1" />
              <el-option label="配送中" :value="2" />
              <el-option label="已送达" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="订单编号">
            <el-input v-model="filterForm.orderSn" placeholder="请输入订单编号" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" disabled>查询</el-button>
            <el-button :icon="Refresh" disabled>重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 订单列表 -->
      <el-card class="order-list-card">
        <template #header>
          <div class="card-header">
            <span><el-icon><Document /></el-icon> 订单列表</span>
          </div>
        </template>

        <el-table :data="testOrderData" border stripe>
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="order-detail">
                <h4>订单商品</h4>
                <el-table :data="row.items" border size="small">
                  <el-table-column prop="productName" label="商品名称" />
                  <el-table-column prop="quantity" label="数量" width="80" />
                  <el-table-column prop="price" label="单价" width="100" />
                  <el-table-column prop="totalPrice" label="小计" width="100" />
                </el-table>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="orderSn" label="订单编号" width="180" />
          <el-table-column prop="userName" label="用户" width="120" />
          <el-table-column prop="totalAmount" label="订单金额" width="100">
            <template #default="{ row }">
              <span class="amount-text">¥{{ row.totalAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="订单状态" width="100">
            <template #default="{ row }">
              <el-tag type="info">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="address" label="收货地址" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createTime" label="下单时间" width="160" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default>
              <el-button size="small" disabled>查看详情</el-button>
              <el-button size="small" type="success" disabled>标记发货</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          :current-page="filterForm.page"
          :page-size="filterForm.limit"
          :total="50"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          style="margin-top: 20px; justify-content: center;"
          disabled
        />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Search, Refresh, Document } from '@element-plus/icons-vue'

// 筛选表单
const filterForm = reactive({
  status: null,
  orderSn: '',
  page: 1,
  limit: 10
})

// 测试订单数据
const testOrderData = ref([
  {
    orderId: '测试1',
    orderSn: '测试订单编号001',
    userName: '测试用户A',
    totalAmount: 0,
    status: '测试状态',
    address: '测试地址 - 北京市朝阳区测试社区',
    createTime: '2025-11-01 10:00:00',
    items: [
      { productName: '测试商品1', quantity: 1, price: 0, totalPrice: 0 },
      { productName: '测试商品2', quantity: 2, price: 0, totalPrice: 0 }
    ]
  },
  {
    orderId: '测试2',
    orderSn: '测试订单编号002',
    userName: '测试用户B',
    totalAmount: 0,
    status: '测试状态',
    address: '测试地址 - 北京市海淀区测试社区',
    createTime: '2025-11-01 11:00:00',
    items: [
      { productName: '测试商品3', quantity: 1, price: 0, totalPrice: 0 }
    ]
  },
  {
    orderId: '测试3',
    orderSn: '测试订单编号003',
    userName: '测试用户C',
    totalAmount: 0,
    status: '测试状态',
    address: '测试地址 - 北京市西城区测试社区',
    createTime: '2025-11-01 12:00:00',
    items: [
      { productName: '测试商品4', quantity: 3, price: 0, totalPrice: 0 }
    ]
  }
])
</script>

<style scoped>
.order-manage-wrapper {
  min-height: 100vh;
  padding-top: 84px;
  background-color: #f5f5f5;
}

.order-manage-container {
  max-width: 1600px;
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

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #606266;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.filter-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
}

.amount-text {
  color: #f56c6c;
  font-weight: bold;
}

.order-detail {
  padding: 15px;
  background-color: #f5f7fa;
}

.order-detail h4 {
  margin: 0 0 10px 0;
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .order-manage-wrapper {
    padding-top: 76px;
  }

  .order-manage-container {
    padding: 10px;
  }
}
</style>

