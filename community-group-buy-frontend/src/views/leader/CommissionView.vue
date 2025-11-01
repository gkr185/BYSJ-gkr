<template>
  <div class="commission-wrapper">
    <div class="commission-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>佣金管理</h2>
        <p class="subtitle">查看您的佣金收益</p>
      </div>

      <!-- 佣金概览 -->
      <el-row :gutter="20" class="summary-row">
        <el-col :span="6" :xs="12">
          <el-card class="summary-card" shadow="hover">
            <div class="summary-content">
              <div class="summary-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
                <el-icon :size="32"><Wallet /></el-icon>
              </div>
              <div class="summary-info">
                <div class="summary-value">
                  <el-skeleton v-if="loading" :rows="1" animated />
                  <span v-else>¥{{ summary?.balance || 0 }}</span>
                </div>
                <div class="summary-label">账户余额</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6" :xs="12">
          <el-card class="summary-card" shadow="hover">
            <div class="summary-content">
              <div class="summary-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
                <el-icon :size="32"><Money /></el-icon>
              </div>
              <div class="summary-info">
                <div class="summary-value">
                  <el-skeleton v-if="loading" :rows="1" animated />
                  <span v-else>¥{{ summary?.monthly || 0 }}</span>
                </div>
                <div class="summary-label">本月佣金</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6" :xs="12">
          <el-card class="summary-card" shadow="hover">
            <div class="summary-content">
              <div class="summary-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);">
                <el-icon :size="32"><TrendCharts /></el-icon>
              </div>
              <div class="summary-info">
                <div class="summary-value">
                  <el-skeleton v-if="loading" :rows="1" animated />
                  <span v-else>¥{{ summary?.total || 0 }}</span>
                </div>
                <div class="summary-label">累计佣金</div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6" :xs="12">
          <el-card class="summary-card" shadow="hover">
            <div class="summary-content">
              <div class="summary-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);">
                <el-icon :size="32"><Clock /></el-icon>
              </div>
              <div class="summary-info">
                <div class="summary-value">
                  <el-skeleton v-if="loading" :rows="1" animated />
                  <span v-else>¥{{ summary?.unsettled || 0 }}</span>
                </div>
                <div class="summary-label">未结算</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 提现说明 -->
      <el-alert type="info" :closable="false" style="margin-bottom: 20px;">
        <template #title>
          💡 提现说明
        </template>
        <div style="margin-top: 10px;">
          <p>• 佣金每月1号自动结算到账户余额</p>
          <p>• 如需提现，请联系客服微信：<strong>wxid_community_groupbuy</strong></p>
          <p>• 提现说明：系统暂不支持线上提现，需人工处理</p>
        </div>
      </el-alert>

      <!-- 佣金明细 -->
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span><el-icon><List /></el-icon> 佣金明细</span>
          </div>
        </template>

        <!-- 筛选器 -->
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="结算状态">
            <el-select v-model="filterForm.status" placeholder="全部" @change="handleFilter">
              <el-option label="全部" :value="null" />
              <el-option label="未结算" :value="0" />
              <el-option label="已结算" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleFilter">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 加载状态 -->
        <el-skeleton v-if="loadingRecords" :rows="5" animated />

        <!-- 佣金记录表格 -->
        <el-table 
          v-else-if="records.length > 0"
          :data="records" 
          border
          stripe
        >
          <el-table-column prop="commissionId" label="佣金ID" width="100" />
          <el-table-column prop="orderSn" label="订单号" width="180" />
          <el-table-column prop="amount" label="佣金金额" width="120">
            <template #default="{ row }">
              <span class="amount-text">¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
                {{ row.status === 1 ? '已结算' : '未结算' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="生成时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="settleTime" label="结算时间" width="180">
            <template #default="{ row }">
              {{ row.settleTime ? formatDateTime(row.settleTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="150">
            <template #default="{ row }">
              {{ row.remark || '-' }}
            </template>
          </el-table-column>
        </el-table>

        <!-- 无数据 -->
        <el-empty v-else description="暂无佣金记录" />

        <!-- 分页 -->
        <el-pagination
          v-if="total > 0"
          :current-page="filterForm.page"
          :page-size="filterForm.limit"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="val => { filterForm.limit = val; handleFilter() }"
          @current-change="val => { filterForm.page = val; handleFilter() }"
          style="margin-top: 20px; justify-content: center;"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  Wallet,
  Money,
  TrendCharts,
  Clock,
  List,
  Search,
  Refresh
} from '@element-plus/icons-vue'
import { getMyCommissionSummary, getMyCommissionRecords } from '@/api/leader'

const router = useRouter()
const userStore = useUserStore()

// 数据状态
const loading = ref(false)
const loadingRecords = ref(false)
const summary = ref(null)
const records = ref([])
const total = ref(0)

// 筛选表单
const filterForm = ref({
  status: null,
  page: 1,
  limit: 10
})

// 获取佣金概览
const fetchSummary = async () => {
  if (!userStore.userInfo?.userId) return
  
  loading.value = true
  try {
    const data = await getMyCommissionSummary(userStore.userInfo.userId)
    summary.value = data
  } catch (error) {
    console.error('获取佣金概览失败:', error)
    summary.value = null
  } finally {
    loading.value = false
  }
}

// 获取佣金明细
const fetchRecords = async () => {
  if (!userStore.userInfo?.userId) return
  
  loadingRecords.value = true
  try {
    const params = {
      leaderId: userStore.userInfo.userId,
      status: filterForm.value.status,
      page: filterForm.value.page,
      limit: filterForm.value.limit
    }
    
    const data = await getMyCommissionRecords(params)
    records.value = data?.list || []
    total.value = data?.total || 0
  } catch (error) {
    console.error('获取佣金明细失败:', error)
    records.value = []
    total.value = 0
  } finally {
    loadingRecords.value = false
  }
}

// 筛选处理
const handleFilter = () => {
  filterForm.value.page = 1
  fetchRecords()
}

// 重置筛选
const handleReset = () => {
  filterForm.value = {
    status: null,
    page: 1,
    limit: 10
  }
  fetchRecords()
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 页面加载
onMounted(() => {
  if (!userStore.isLeader) {
    ElMessage.warning('仅团长可访问')
    router.push('/leader/apply')
    return
  }
  
  fetchSummary()
  fetchRecords()
})
</script>

<style scoped>
.commission-wrapper {
  min-height: 100vh;
  padding-top: 84px;
  background-color: #f5f5f5;
}

.commission-container {
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

/* 概览卡片 */
.summary-row {
  margin-bottom: 20px;
}

.summary-card {
  cursor: default;
  transition: all 0.3s;
}

.summary-card:hover {
  transform: translateY(-4px);
}

.summary-card :deep(.el-card__body) {
  padding: 20px;
}

.summary-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.summary-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.summary-info {
  flex: 1;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.summary-label {
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
}

.filter-form {
  margin-bottom: 20px;
}

.amount-text {
  color: #f56c6c;
  font-weight: bold;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .commission-wrapper {
    padding-top: 76px;
  }

  .commission-container {
    padding: 10px;
  }

  .summary-value {
    font-size: 20px;
  }

  :deep(.el-table) {
    font-size: 12px;
  }
}
</style>

