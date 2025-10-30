<template>
  <div class="commission-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>佣金管理</span>
          <div>
            <el-button type="danger" @click="showSettleDialog" :disabled="pendingTotal === 0">
              <el-icon><Money /></el-icon>
              手动结算佣金
            </el-button>
            <el-button @click="fetchCommissions">刷新</el-button>
          </div>
        </div>
      </template>
      
      <!-- 统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="待结算佣金" :value="pendingTotal" :precision="2" prefix="¥">
              <template #suffix>
                <el-text type="warning" size="small">（{{ pendingCount }}笔）</el-text>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover">
            <el-statistic title="本月已结算" :value="settledMonthTotal" :precision="2" prefix="¥">
              <template #suffix>
                <el-text type="success" size="small">（本月）</el-text>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 状态Tabs -->
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="待结算" name="pending">
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="tab-badge" />
        </el-tab-pane>
        <el-tab-pane label="已结算" name="settled"></el-tab-pane>
      </el-tabs>
      
      <!-- 佣金列表表格 -->
      <el-table :data="commissionList" v-loading="loading" border style="width: 100%">
        <el-table-column prop="recordId" label="记录ID" width="80" />
        <el-table-column label="团长" width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="viewLeader(row.leaderId)">{{ row.leaderName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="orderId" label="订单ID" width="100" />
        <el-table-column label="订单金额" width="120">
          <template #default="{ row }">¥{{ row.orderAmount }}</template>
        </el-table-column>
        <el-table-column label="佣金比例" width="100">
          <template #default="{ row }">{{ row.commissionRate }}%</template>
        </el-table-column>
        <el-table-column label="佣金金额" width="120">
          <template #default="{ row }">
            <el-text type="danger" style="font-weight: bold">¥{{ row.commissionAmount }}</el-text>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="结算批次" width="140" v-if="activeTab === 'settled'">
          <template #default="{ row }">
            <el-link type="primary" @click="viewBatch(row.settlementBatch)">{{ row.settlementBatch }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="settledAt" label="结算时间" width="180" v-if="activeTab === 'settled'">
          <template #default="{ row }">{{ formatDate(row.settledAt) }}</template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 结算确认对话框 -->
    <el-dialog v-model="settleDialogVisible" title="手动结算佣金" width="500px">
      <el-alert title="结算确认" type="warning" :closable="false" style="margin-bottom: 20px">
        <p>即将结算所有待结算佣金，共 <strong>{{ pendingCount }}</strong> 笔，总金额 <strong style="color: #F56C6C">¥{{ pendingTotal }}</strong></p>
        <p style="margin-top: 10px">结算后将：</p>
        <ul style="margin-left: 20px">
          <li>调用UserService为团长增加余额</li>
          <li>更新佣金记录状态为"已结算"</li>
          <li>生成结算批次号</li>
        </ul>
      </el-alert>
      <template #footer>
        <el-button @click="settleDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleSettle" :loading="settling">确定结算</el-button>
      </template>
    </el-dialog>
    
    <!-- 结算批次详情对话框 -->
    <el-dialog v-model="batchDialogVisible" title="结算批次详情" width="900px">
      <div v-if="batchDetails">
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="结算批次号">{{ currentBatch }}</el-descriptions-item>
          <el-descriptions-item label="结算笔数">{{ batchDetails.length }}</el-descriptions-item>
          <el-descriptions-item label="结算总额" :span="2">
            <el-text type="danger" style="font-weight: bold; font-size: 16px">
              ¥{{ batchTotal }}
            </el-text>
          </el-descriptions-item>
        </el-descriptions>
        
        <el-table :data="batchDetails" border style="width: 100%">
          <el-table-column prop="recordId" label="记录ID" width="80" />
          <el-table-column prop="leaderName" label="团长" width="120" />
          <el-table-column prop="orderId" label="订单ID" width="100" />
          <el-table-column label="订单金额" width="120">
            <template #default="{ row }">¥{{ row.orderAmount }}</template>
          </el-table-column>
          <el-table-column label="佣金金额" width="120">
            <template #default="{ row }">¥{{ row.commissionAmount }}</template>
          </el-table-column>
          <el-table-column prop="settledAt" label="结算时间" width="180">
            <template #default="{ row }">{{ formatDate(row.settledAt) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Money } from '@element-plus/icons-vue'
import { getPendingCommissions, settleCommissions, getCommissionsByBatch } from '../api/leader'

const commissionList = ref([])
const loading = ref(false)
const activeTab = ref('pending')
const settleDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const settling = ref(false)
const batchDetails = ref(null)
const currentBatch = ref('')

const pendingCount = computed(() => activeTab.value === 'pending' ? commissionList.value.length : 0)
const pendingTotal = computed(() => {
  if (activeTab.value !== 'pending') return 0
  return commissionList.value.reduce((sum, item) => sum + parseFloat(item.commissionAmount || 0), 0).toFixed(2)
})
const settledMonthTotal = computed(() => {
  if (activeTab.value !== 'settled') return 0
  const thisMonth = new Date().getMonth()
  return commissionList.value.filter(item => new Date(item.settledAt).getMonth() === thisMonth)
    .reduce((sum, item) => sum + parseFloat(item.commissionAmount || 0), 0).toFixed(2)
})
const batchTotal = computed(() => {
  if (!batchDetails.value) return 0
  return batchDetails.value.reduce((sum, item) => sum + parseFloat(item.commissionAmount || 0), 0).toFixed(2)
})

const fetchCommissions = async () => {
  loading.value = true
  try {
    const res = await getPendingCommissions()
    if (res.code === 200) {
      commissionList.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取佣金列表失败')
    }
  } catch (error) {
    console.error('获取佣金列表失败:', error)
    ElMessage.error('获取佣金列表失败')
  } finally {
    loading.value = false
  }
}

const handleTabClick = () => fetchCommissions()
const showSettleDialog = () => { settleDialogVisible.value = true }

const handleSettle = async () => {
  settling.value = true
  try {
    const res = await settleCommissions()
    if (res.code === 200) {
      ElMessage.success({ message: `佣金结算成功！结算批次：${res.data.settlementBatch}，共结算 ${res.data.settledCount} 笔`, duration: 5000 })
      settleDialogVisible.value = false
      fetchCommissions()
    } else {
      ElMessage.error(res.message || '结算失败')
    }
  } catch (error) {
    console.error('结算失败:', error)
    ElMessage.error('结算失败')
  } finally {
    settling.value = false
  }
}

const viewBatch = async (batchNo) => {
  currentBatch.value = batchNo
  try {
    const res = await getCommissionsByBatch(batchNo)
    if (res.code === 200) {
      batchDetails.value = res.data || []
      batchDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取批次详情失败')
    }
  } catch (error) {
    console.error('获取批次详情失败:', error)
    ElMessage.error('获取批次详情失败')
  }
}

const viewLeader = (leaderId) => {
  // TODO: 跳转到团长详情
  console.log('查看团长:', leaderId)
}

const getStatusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[status] || 'info')
const getStatusText = (status) => ({ 0: '待结算', 1: '已结算', 2: '结算失败' }[status] || '未知')
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

onMounted(() => fetchCommissions())
</script>

<style scoped>
.commission-manage { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.tab-badge { margin-left: 8px; }
</style>

