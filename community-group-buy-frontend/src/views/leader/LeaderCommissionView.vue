<template>
  <MainLayout>
    <div class="leader-commission-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <el-button :icon="ArrowLeft" @click="$router.back()">返回</el-button>
          <h2 class="page-title">
            <el-icon><Coin /></el-icon>
            佣金管理
          </h2>
        </div>
      </div>

      <!-- 加载状态 -->
      <el-skeleton :rows="6" animated v-if="pageLoading" />

      <!-- 主体内容 -->
      <div v-else class="commission-content">
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="statistics-row">
          <!-- 待结算佣金 -->
          <el-col :xs="24" :sm="8">
            <el-card class="stat-card pending-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon pending">
                  <el-icon><Clock /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-label">待结算佣金</div>
                  <div class="stat-value">¥{{ formatMoney(commissionSummary.pendingCommission) }}</div>
                  <div class="stat-tip">每月1号自动结算</div>
                </div>
              </div>
            </el-card>
          </el-col>

          <!-- 已结算佣金 -->
          <el-col :xs="24" :sm="8">
            <el-card class="stat-card settled-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon settled">
                  <el-icon><CircleCheck /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-label">已结算佣金</div>
                  <div class="stat-value">¥{{ formatMoney(commissionSummary.settledCommission) }}</div>
                  <div class="stat-tip">可提现金额</div>
                </div>
              </div>
            </el-card>
          </el-col>

          <!-- 累计佣金 -->
          <el-col :xs="24" :sm="8">
            <el-card class="stat-card total-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon total">
                  <el-icon><TrendCharts /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-label">累计佣金</div>
                  <div class="stat-value">¥{{ formatMoney(commissionSummary.totalCommission) }}</div>
                  <div class="stat-tip">总收入</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 筛选器 -->
        <el-card class="filter-card" shadow="hover">
          <div class="filter-header">
            <span class="filter-title">佣金记录</span>
            <el-radio-group v-model="filterStatus" size="default">
              <el-radio-button :label="null">全部</el-radio-button>
              <el-radio-button :label="0">待结算</el-radio-button>
              <el-radio-button :label="1">已结算</el-radio-button>
            </el-radio-group>
          </div>
        </el-card>

        <!-- 数据表格 -->
        <el-card class="table-card" shadow="hover">
          <el-table :data="commissionList" v-loading="loading" stripe>
            <el-table-column prop="orderSn" label="订单编号" min-width="180" />
            <el-table-column prop="orderAmount" label="订单金额" min-width="120">
              <template #default="{ row }">
                <span class="amount-text">¥{{ formatMoney(row.orderAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="commissionRate" label="佣金比例" min-width="100">
              <template #default="{ row }">
                <el-tag type="info" size="small">{{ (row.commissionRate * 100).toFixed(1) }}%</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="commissionAmount" label="佣金金额" min-width="120">
              <template #default="{ row }">
                <span class="commission-amount">¥{{ formatMoney(row.commissionAmount) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="结算状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'warning'">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="settlementTime" label="结算时间" min-width="180">
              <template #default="{ row }">
                <span v-if="row.settlementTime">{{ formatTime(row.settlementTime) }}</span>
                <span v-else class="empty-text">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="settlementBatch" label="结算批次" min-width="120">
              <template #default="{ row }">
                <span v-if="row.settlementBatch">{{ row.settlementBatch }}</span>
                <span v-else class="empty-text">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" min-width="180">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 空数据 -->
          <el-empty v-if="!loading && commissionList.length === 0" description="暂无佣金记录" />
          
          <!-- 分页 -->
          <div v-if="commissionList.length > 0" class="pagination-wrapper">
            <el-pagination
              :current-page="currentPage"
              :total="total"
              :page-size="pageSize"
              layout="total, prev, pager, next, jumper"
              @current-change="handlePageChange"
            />
          </div>
        </el-card>
      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyCommissionSummary, getMyCommissionRecords } from '@/api/leader'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Clock, CircleCheck, TrendCharts, Coin } from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'

// 用户信息
const userStore = useUserStore()
const leaderId = userStore.userInfo?.userId

// 页面加载状态
const pageLoading = ref(true)

// 统计数据
const commissionSummary = ref({
  pendingCommission: 0,
  settledCommission: 0,
  totalCommission: 0
})

// 列表数据
const commissionList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref(null)

// 佣金状态映射
const COMMISSION_STATUS_TEXT = {
  0: '待结算',
  1: '已结算',
  2: '结算失败'
}

// 获取状态文本
const getStatusText = (status) => {
  return COMMISSION_STATUS_TEXT[status] || '未知'
}

// 加载统计数据
const loadSummary = async () => {
  try {
    const res = await getMyCommissionSummary(leaderId)
    if (res.code === 200) {
      commissionSummary.value = res.data
    }
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

// 加载佣金列表
const loadCommissionList = async () => {
  loading.value = true
  try {
    const res = await getMyCommissionRecords({
      leaderId,
      status: filterStatus.value,
      page: currentPage.value - 1,  // 后端从0开始
      limit: pageSize.value
    })
    if (res.code === 200) {
      commissionList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载失败，请稍后重试')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  loadCommissionList()
}

// 监听筛选条件
watch(filterStatus, () => {
  currentPage.value = 1
  loadCommissionList()
})

// 页面加载
onMounted(async () => {
  if (!leaderId) {
    ElMessage.error('请先登录')
    pageLoading.value = false
    return
  }
  
  // 并行加载统计和列表
  await Promise.all([
    loadSummary(),
    loadCommissionList()
  ])
  
  pageLoading.value = false
})

// 金额格式化
const formatMoney = (value) => {
  return value ? value.toFixed(2) : '0.00'
}

// 时间格式化
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
</script>

<style scoped>
.leader-commission-container {
  padding: 20px;
  max-width: 1400px;
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

.commission-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.statistics-row {
  margin-bottom: 4px;
}

.stat-card {
  margin-bottom: 16px;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  flex-shrink: 0;
}

.stat-icon.pending {
  background: linear-gradient(135deg, #fef0e6 0%, #fde2cc 100%);
  color: #f56c6c;
}

.stat-icon.settled {
  background: linear-gradient(135deg, #e6f7f0 0%, #d0f0e3 100%);
  color: #67c23a;
}

.stat-icon.total {
  background: linear-gradient(135deg, #e6f0ff 0%, #d0e3ff 100%);
  color: #409eff;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1;
}

.stat-tip {
  font-size: 12px;
  color: #c0c4cc;
}

.filter-card :deep(.el-card__body) {
  padding: 16px 20px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.amount-text {
  color: #606266;
  font-weight: 500;
}

.commission-amount {
  color: #f56c6c;
  font-weight: 600;
  font-size: 15px;
}

.empty-text {
  color: #c0c4cc;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px;
  border-top: 1px solid #ebeef5;
}

/* 响应式 */
@media (max-width: 768px) {
  .leader-commission-container {
    padding: 12px;
  }

  .page-title {
    font-size: 20px;
  }

  .stat-value {
    font-size: 24px;
  }

  .filter-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .el-table {
    font-size: 12px;
  }
}
</style>
