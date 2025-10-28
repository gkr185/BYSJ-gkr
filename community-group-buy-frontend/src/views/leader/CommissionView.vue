<template>
  <div class="commission-view">
    <!-- 顶部导航 -->
    <TopNav />
    
    <div class="commission-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1>佣金中心</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/leader/dashboard' }">工作台</el-breadcrumb-item>
          <el-breadcrumb-item>佣金中心</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <!-- 余额卡片 -->
      <el-row :gutter="20" class="balance-row">
        <el-col :span="6">
          <el-card shadow="hover" class="balance-card balance-available">
            <el-statistic title="可提现余额" :value="commission?.balance || 0" :precision="2" prefix="¥">
              <template #prefix>
                <el-icon :size="24" color="#67C23A"><Wallet /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="balance-card balance-frozen">
            <el-statistic title="冻结金额" :value="commission?.frozen || 0" :precision="2" prefix="¥">
              <template #prefix>
                <el-icon :size="24" color="#E6A23C"><Lock /></el-icon>
              </template>
            </el-statistic>
            <el-text type="info" size="small" style="display: block; margin-top: 8px;">
              拼团中的订单佣金
            </el-text>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="balance-card balance-total">
            <el-statistic title="累计佣金" :value="commission?.totalEarned || 0" :precision="2" prefix="¥">
              <template #prefix>
                <el-icon :size="24" color="#409EFF"><TrendCharts /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="balance-card balance-withdrawn">
            <el-statistic title="已提现" :value="commission?.withdrawnTotal || 0" :precision="2" prefix="¥">
              <template #prefix>
                <el-icon :size="24" color="#909399"><Download /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 提现说明 -->
      <el-alert 
        type="warning" 
        :closable="false"
        class="withdraw-alert"
      >
        <template #title>
          <div class="alert-content">
            <el-icon :size="18"><Phone /></el-icon>
            <span>如需提现，请联系客服（电话：<strong>400-XXX-XXXX</strong>）</span>
          </div>
        </template>
        <template #default>
          <div class="alert-desc">
            为保障您的资金安全，佣金提现采用线下审核方式。客服会在1-3个工作日内完成审核并转账。
          </div>
        </template>
      </el-alert>
      
      <!-- 佣金明细 -->
      <el-card>
        <template #header>
          <div class="card-header">
            <span class="card-title">
              <el-icon><List /></el-icon>
              佣金明细（{{ commission?.records?.length || 0 }}）
            </span>
            <el-radio-group v-model="recordFilter" size="small" @change="filterRecords">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="settled">已结算</el-radio-button>
              <el-radio-button value="pending">未结算</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        
        <el-table 
          v-if="filteredRecords.length"
          :data="filteredRecords" 
          stripe
          style="width: 100%"
        >
          <el-table-column prop="orderSn" label="订单编号" width="150" />
          <el-table-column prop="productName" label="商品名称" width="150" />
          <el-table-column label="订单金额" width="120">
            <template #default="{ row }">
              ¥{{ row.orderAmount }}
            </template>
          </el-table-column>
          <el-table-column label="佣金比例" width="100" align="center">
            <template #default="{ row }">
              {{ row.commissionRate }}%
            </template>
          </el-table-column>
          <el-table-column label="佣金金额" width="120">
            <template #default="{ row }">
              <span class="commission-amount">¥{{ row.amount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="large">
                {{ row.status === 1 ? '已结算' : '未结算' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="订单时间" width="180" />
          <el-table-column prop="settleTime" label="结算时间" width="180">
            <template #default="{ row }">
              {{ row.settleTime || '-' }}
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty 
          v-else 
          description="暂无佣金记录" 
          :image-size="100"
        />
      </el-card>
      
      <!-- 佣金说明 -->
      <el-card class="info-card">
        <template #header>
          <span class="card-title">
            <el-icon><InfoFilled /></el-icon>
            佣金说明
          </span>
        </template>
        
        <div class="info-content">
          <div class="info-item">
            <h4>1. 佣金计算</h4>
            <p>佣金 = 订单实付金额 × 佣金比例</p>
            <p>您的佣金比例为：<strong>{{ leaderStore.leaderInfo?.commissionRate || 5.00 }}%</strong></p>
          </div>
          <div class="info-item">
            <h4>2. 结算规则</h4>
            <p>• 拼团成功后，佣金自动结算到可提现余额</p>
            <p>• 拼团中的订单佣金为冻结状态</p>
            <p>• 拼团失败后，冻结佣金自动释放</p>
          </div>
          <div class="info-item">
            <h4>3. 提现说明</h4>
            <p>• 可提现余额≥50元时可申请提现</p>
            <p>• 提现采用线下审核方式，联系客服办理</p>
            <p>• 审核通过后1-3个工作日内到账</p>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLeaderStore } from '@/stores/leader'
import TopNav from '@/components/common/TopNav.vue'
import {
  Wallet,
  Lock,
  TrendCharts,
  Download,
  Phone,
  List,
  InfoFilled
} from '@element-plus/icons-vue'

const router = useRouter()
const leaderStore = useLeaderStore()

const commission = ref(null)
const recordFilter = ref('all')

// 过滤后的记录
const filteredRecords = computed(() => {
  if (!commission.value?.records) return []
  if (recordFilter.value === 'all') return commission.value.records
  if (recordFilter.value === 'settled') return commission.value.records.filter(r => r.status === 1)
  if (recordFilter.value === 'pending') return commission.value.records.filter(r => r.status === 0)
  return commission.value.records
})

// 加载佣金数据
onMounted(async () => {
  try {
    // 加载团长信息
    if (!leaderStore.leaderInfo) {
      await leaderStore.fetchLeaderInfo()
    }
    
    // 加载佣金数据
    commission.value = await leaderStore.fetchCommission()
  } catch (error) {
    console.error('加载佣金数据失败', error)
  }
})

// 筛选记录
const filterRecords = () => {
  // 筛选逻辑已通过computed实现
}
</script>

<style scoped>
.commission-container {
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

/* 余额卡片 */
.balance-row {
  margin-bottom: 30px;
}

.balance-card {
  text-align: center;
  transition: all 0.3s;
}

.balance-card:hover {
  transform: translateY(-5px);
}

.balance-card :deep(.el-statistic__head) {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.balance-card :deep(.el-statistic__content) {
  font-size: 32px;
  font-weight: 600;
}

.balance-available :deep(.el-statistic__content) {
  color: #67C23A;
}

.balance-frozen :deep(.el-statistic__content) {
  color: #E6A23C;
}

.balance-total :deep(.el-statistic__content) {
  color: #409EFF;
}

.balance-withdrawn :deep(.el-statistic__content) {
  color: #909399;
}

/* 提现说明 */
.withdraw-alert {
  margin-bottom: 30px;
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.alert-desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
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

/* 佣金金额 */
.commission-amount {
  font-size: 16px;
  font-weight: 600;
  color: #67C23A;
}

/* 佣金说明 */
.info-card {
  margin-top: 30px;
  background: linear-gradient(135deg, #fff9e6 0%, #fff3d6 100%);
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-item h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.info-item p {
  margin: 6px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.info-item strong {
  color: #F56C6C;
  font-size: 16px;
}

/* 空状态 */
:deep(.el-empty) {
  padding: 60px 0;
}
</style>
