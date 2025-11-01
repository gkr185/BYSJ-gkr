<template>
  <div class="statistics-wrapper">
    <div class="statistics-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>数据统计</h2>
        <p class="subtitle">查看您的经营数据统计</p>
      </div>

      <!-- 功能说明 -->
      <el-alert type="info" :closable="false" style="margin-bottom: 20px;">
        <template #title>
          📢 此功能需要多个后端服务聚合数据支持，当前显示测试布局结构
        </template>
        <div style="margin-top: 8px; font-size: 13px;">
          <p>• 需要OrderService、CommissionService等多个服务数据聚合</p>
          <p>• API接口：GET /api/leader/statistics</p>
          <p>• 图表组件使用ECharts实现</p>
        </div>
      </el-alert>

      <!-- 时间范围选择 -->
      <el-card class="filter-card">
        <el-form :inline="true">
          <el-form-item label="统计时间">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              disabled
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" disabled>查询</el-button>
            <el-button :icon="Refresh" disabled>重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 核心数据统计 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6" :xs="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                <el-icon :size="28"><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">测试</div>
                <div class="stat-label">总订单数</div>
                <div class="stat-change positive">测试 ↑</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6" :xs="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                <el-icon :size="28"><Money /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">¥0</div>
                <div class="stat-label">成交金额</div>
                <div class="stat-change positive">测试 ↑</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6" :xs="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                <el-icon :size="28"><Coin /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">¥0</div>
                <div class="stat-label">佣金收入</div>
                <div class="stat-change positive">测试 ↑</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6" :xs="12">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);">
                <el-icon :size="28"><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">测试</div>
                <div class="stat-label">服务用户</div>
                <div class="stat-change positive">测试 ↑</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 趋势图表 -->
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span><el-icon><TrendCharts /></el-icon> 销售趋势</span>
              </div>
            </template>
            <div class="chart-placeholder">
              <el-empty description="图表功能待开发">
                <template #image>
                  <el-icon :size="80" color="#c0c4cc"><DataLine /></el-icon>
                </template>
                <p style="color: #909399; font-size: 14px;">
                  此处将显示销售额趋势折线图<br/>
                  使用ECharts实现数据可视化
                </p>
              </el-empty>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span><el-icon><PieChart /></el-icon> 订单状态分布</span>
              </div>
            </template>
            <div class="chart-placeholder small">
              <el-empty description="图表功能待开发">
                <template #image>
                  <el-icon :size="60" color="#c0c4cc"><PieChart /></el-icon>
                </template>
                <p style="color: #909399; font-size: 12px;">
                  订单状态饼图
                </p>
              </el-empty>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 热门商品排行 -->
      <el-card class="ranking-card">
        <template #header>
          <div class="card-header">
            <span><el-icon><Trophy /></el-icon> 热门商品TOP 10</span>
          </div>
        </template>

        <el-table :data="testProductRanking" border stripe>
          <el-table-column type="index" label="排名" width="80">
            <template #default="{ $index }">
              <span class="rank-badge" :class="getRankClass($index)">
                {{ $index + 1 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="productName" label="商品名称" min-width="200" />
          <el-table-column prop="sales" label="销量" width="100" sortable />
          <el-table-column prop="amount" label="销售额" width="120" sortable>
            <template #default="{ row }">
              <span class="amount-text">¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="commission" label="佣金收入" width="120">
            <template #default="{ row }">
              <span class="commission-text">¥{{ row.commission }}</span>
            </template>
          </el-table-column>
          <el-table-column label="销售占比" width="150">
            <template #default="{ row }">
              <el-progress :percentage="row.percentage" :color="getProgressColor(row.percentage)" />
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 拼团活动统计 -->
      <el-card class="activity-card">
        <template #header>
          <div class="card-header">
            <span><el-icon><DataAnalysis /></el-icon> 拼团活动统计</span>
          </div>
        </template>

        <el-table :data="testActivityStats" border stripe>
          <el-table-column prop="activityName" label="活动名称" min-width="200" />
          <el-table-column prop="totalTeams" label="发起团数" width="100" />
          <el-table-column prop="successTeams" label="成功团数" width="100" />
          <el-table-column prop="successRate" label="成团率" width="100">
            <template #default="{ row }">
              <el-tag :type="row.successRate >= 80 ? 'success' : row.successRate >= 50 ? 'warning' : 'danger'">
                {{ row.successRate }}%
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalOrders" label="订单数" width="100" />
          <el-table-column prop="totalAmount" label="成交额" width="120">
            <template #default="{ row }">
              ¥{{ row.totalAmount }}
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
  Search,
  Refresh,
  Document,
  Money,
  Coin,
  User,
  TrendCharts,
  DataLine,
  PieChart,
  Trophy,
  DataAnalysis
} from '@element-plus/icons-vue'

const dateRange = ref([])

// 测试商品排行数据
const testProductRanking = ref([
  { productName: '测试商品A', sales: 0, amount: 0, commission: 0, percentage: 100 },
  { productName: '测试商品B', sales: 0, amount: 0, commission: 0, percentage: 80 },
  { productName: '测试商品C', sales: 0, amount: 0, commission: 0, percentage: 60 },
  { productName: '测试商品D', sales: 0, amount: 0, commission: 0, percentage: 40 },
  { productName: '测试商品E', sales: 0, amount: 0, commission: 0, percentage: 20 }
])

// 测试活动统计数据
const testActivityStats = ref([
  {
    activityName: '测试拼团活动A',
    totalTeams: 0,
    successTeams: 0,
    successRate: 0,
    totalOrders: 0,
    totalAmount: 0
  },
  {
    activityName: '测试拼团活动B',
    totalTeams: 0,
    successTeams: 0,
    successRate: 0,
    totalOrders: 0,
    totalAmount: 0
  },
  {
    activityName: '测试拼团活动C',
    totalTeams: 0,
    successTeams: 0,
    successRate: 0,
    totalOrders: 0,
    totalAmount: 0
  }
])

// 获取排名徽章样式
const getRankClass = (index) => {
  if (index === 0) return 'rank-first'
  if (index === 1) return 'rank-second'
  if (index === 2) return 'rank-third'
  return ''
}

// 获取进度条颜色
const getProgressColor = (percentage) => {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 50) return '#409eff'
  if (percentage >= 30) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped>
.statistics-wrapper {
  min-height: 100vh;
  padding-top: 84px;
  background-color: #f5f5f5;
}

.statistics-container {
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

.filter-card {
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-change {
  font-size: 12px;
}

.stat-change.positive {
  color: #67c23a;
}

.stat-change.negative {
  color: #f56c6c;
}

.chart-card,
.ranking-card,
.activity-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
}

.chart-placeholder {
  height: 350px;
  background-color: #f5f7fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder.small {
  height: 350px;
}

.rank-badge {
  display: inline-block;
  width: 28px;
  height: 28px;
  line-height: 28px;
  text-align: center;
  border-radius: 50%;
  font-weight: bold;
  color: white;
  background-color: #909399;
}

.rank-badge.rank-first {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  color: #8b4513;
}

.rank-badge.rank-second {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  color: #666;
}

.rank-badge.rank-third {
  background: linear-gradient(135deg, #cd7f32 0%, #daa520 100%);
  color: white;
}

.amount-text {
  color: #f56c6c;
  font-weight: bold;
}

.commission-text {
  color: #67c23a;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .statistics-wrapper {
    padding-top: 76px;
  }

  .statistics-container {
    padding: 10px;
  }

  .chart-placeholder {
    height: 250px;
  }

  .stat-value {
    font-size: 20px;
  }
}
</style>

