<template>
  <div class="payment-manage">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #409EFF">
              <el-icon><CreditCard /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.total || 0 }}</div>
              <div class="stat-label">总记录数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #67C23A">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">¥{{ statistics.totalAmount || '0.00' }}</div>
              <div class="stat-label">总交易金额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #E6A23C">
              <el-icon><CirclePlus /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.rechargeCount || 0 }}</div>
              <div class="stat-label">充值记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-icon" style="background: #F56C6C">
              <el-icon><RefreshLeft /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.refundCount || 0 }}</div>
              <div class="stat-label">退款记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主内容卡片 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>支付管理</span>
          <div>
            <el-button @click="fetchPayments">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button type="success" @click="handleExport">
              <el-icon><Download /></el-icon>
              导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <el-row :gutter="20" class="search-row">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索订单号/交易号"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-col>

        <el-col :span="4">
          <el-select v-model="recordTypeFilter" placeholder="记录类型" @change="handleFilter" clearable>
            <el-option label="全部类型" :value="null" />
            <el-option label="充值" value="recharge" />
            <el-option label="支付" value="payment" />
            <el-option label="退款" value="refund" />
          </el-select>
        </el-col>

        <el-col :span="4">
          <el-select v-model="payTypeFilter" placeholder="支付方式" @change="handleFilter" clearable>
            <el-option label="全部方式" :value="null" />
            <el-option label="微信支付" :value="1" />
            <el-option label="支付宝支付" :value="2" />
            <el-option label="余额支付" :value="3" />
          </el-select>
        </el-col>

        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="支付状态" @change="handleFilter" clearable>
            <el-option label="全部状态" :value="null" />
            <el-option label="失败" :value="0" />
            <el-option label="成功" :value="1" />
          </el-select>
        </el-col>

        <el-col :span="6">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleFilter"
            clearable
          />
        </el-col>
      </el-row>

      <!-- 数据表格 -->
      <el-table
        v-loading="loading"
        :data="paymentList"
        stripe
        border
        style="width: 100%; margin-top: 20px;"
      >
        <el-table-column prop="payId" label="支付ID" width="80" />
        
        <el-table-column label="记录类型" width="100">
          <template #default="scope">
            <el-tag :type="getRecordTypeTagType(scope.row)">
              {{ getRecordTypeText(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="userId" label="用户ID" width="100" />

        <el-table-column label="订单号" width="180">
          <template #default="scope">
            <span v-if="scope.row.orderId">
              <el-link type="primary" @click="viewOrder(scope.row.orderId)">
                {{ scope.row.orderId }}
              </el-link>
            </span>
            <span v-else style="color: #999;">充值记录</span>
          </template>
        </el-table-column>

        <el-table-column label="支付方式" width="120">
          <template #default="scope">
            <el-tag :type="getPayTypeTagType(scope.row.payType)">
              {{ getPayTypeText(scope.row.payType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="金额" width="120">
          <template #default="scope">
            <span :style="{ color: getAmountColor(scope.row) }">
              {{ formatAmount(scope.row.amount) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.payStatus === 1 ? 'success' : 'danger'">
              {{ getPayStatusText(scope.row.payStatus) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="交易号" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            {{ scope.row.transactionId || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewDetail(scope.row)">
              查看详情
            </el-button>
            <el-button
              v-if="canRefund(scope.row)"
              type="danger"
              size="small"
              @click="handleRefund(scope.row)"
            >
              退款
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="支付记录详情"
      width="700px"
    >
      <el-descriptions :column="2" border v-if="currentPayment">
        <el-descriptions-item label="支付ID">
          {{ currentPayment.payId }}
        </el-descriptions-item>
        <el-descriptions-item label="用户ID">
          {{ currentPayment.userId }}
        </el-descriptions-item>
        <el-descriptions-item label="订单ID">
          <span v-if="currentPayment.orderId">
            <el-link type="primary" @click="viewOrder(currentPayment.orderId)">
              {{ currentPayment.orderId }}
            </el-link>
          </span>
          <span v-else style="color: #999;">无（充值记录）</span>
        </el-descriptions-item>
        <el-descriptions-item label="记录类型">
          <el-tag :type="getRecordTypeTagType(currentPayment)">
            {{ getRecordTypeText(currentPayment) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          <el-tag :type="getPayTypeTagType(currentPayment.payType)">
            {{ getPayTypeText(currentPayment.payType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="金额">
          <span :style="{ color: getAmountColor(currentPayment), fontWeight: 'bold', fontSize: '16px' }">
            {{ formatAmount(currentPayment.amount) }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="currentPayment.payStatus === 1 ? 'success' : 'danger'">
            {{ getPayStatusText(currentPayment.payStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="交易号" :span="2">
          {{ currentPayment.transactionId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="微信交易号" :span="2">
          {{ currentPayment.wxTransactionId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="加密签名" :span="2">
          <el-text line-clamp="2" style="font-family: monospace; font-size: 12px;">
            {{ currentPayment.encryptSign || '-' }}
          </el-text>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatDate(currentPayment.createTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="canRefund(currentPayment)"
          type="danger"
          @click="handleRefund(currentPayment)"
        >
          申请退款
        </el-button>
      </template>
    </el-dialog>

    <!-- 退款对话框 -->
    <el-dialog
      v-model="refundDialogVisible"
      title="申请退款"
      width="500px"
    >
      <el-form :model="refundForm" label-width="100px">
        <el-form-item label="订单ID">
          <el-input v-model="refundForm.orderId" disabled />
        </el-form-item>
        <el-form-item label="退款金额">
          <el-input v-model="refundForm.amount" disabled>
            <template #prepend>¥</template>
          </el-input>
        </el-form-item>
        <el-form-item label="退款原因">
          <el-input
            v-model="refundForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入退款原因"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="refunding" @click="confirmRefund">
          确认退款
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CreditCard,
  Money,
  CirclePlus,
  RefreshLeft,
  Refresh,
  Download,
  Search
} from '@element-plus/icons-vue'
import {
  getPaymentRecords,
  getPayTypeText,
  getPayStatusText,
  getRecordType,
  getRecordTypeText,
  refundPayment
} from '@/api/payment'

// 响应式数据
const loading = ref(false)
const paymentList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

// 搜索和筛选
const searchKeyword = ref('')
const recordTypeFilter = ref(null)
const payTypeFilter = ref(null)
const statusFilter = ref(null)
const dateRange = ref(null)

// 统计数据
const statistics = reactive({
  total: 0,
  totalAmount: '0.00',
  rechargeCount: 0,
  refundCount: 0
})

// 详情对话框
const detailDialogVisible = ref(false)
const currentPayment = ref(null)

// 退款对话框
const refundDialogVisible = ref(false)
const refunding = ref(false)
const refundForm = reactive({
  orderId: null,
  amount: 0,
  reason: ''
})

// 获取支付记录
const fetchPayments = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }

    // 添加筛选条件
    if (payTypeFilter.value !== null) {
      params.payType = payTypeFilter.value
    }
    if (statusFilter.value !== null) {
      params.payStatus = statusFilter.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0].toISOString().split('T')[0]
      params.endDate = dateRange.value[1].toISOString().split('T')[0]
    }

    // 调用真实API
    const res = await getPaymentRecords(params)
    
    if (res.code === 200) {
      // 处理返回数据
      if (Array.isArray(res.data)) {
        // 如果返回的是数组（非分页）
        let records = res.data
        
        // 前端筛选
        if (recordTypeFilter.value) {
          records = records.filter(item => {
            const type = getRecordType(item)
            return type === recordTypeFilter.value
          })
        }
        
        // 前端分页
        total.value = records.length
        const start = (currentPage.value - 1) * pageSize.value
        const end = start + pageSize.value
        paymentList.value = records.slice(start, end)
      } else if (res.data.content) {
        // 如果返回的是分页对象
        paymentList.value = res.data.content
        total.value = res.data.totalElements
      } else {
        // 兼容其他格式
        paymentList.value = res.data.list || []
        total.value = res.data.total || 0
      }
      
      // 计算统计数据
      calculateStatistics()
      
      console.log('✅ 支付记录加载成功:', {
        total: total.value,
        currentPage: paymentList.value.length
      })
    } else {
      ElMessage.error(res.message || '加载支付记录失败')
      paymentList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('❌ 加载支付记录失败:', error)
    ElMessage.error('加载支付记录失败，请检查网络连接')
    paymentList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 计算统计数据（基于当前页面数据）
const calculateStatistics = () => {
  statistics.total = total.value
  
  let totalAmount = 0
  let rechargeCount = 0
  let refundCount = 0
  
  paymentList.value.forEach(item => {
    if (item.payStatus === 1) {
      totalAmount += parseFloat(item.amount)
    }
    
    const type = getRecordType(item)
    if (type === 'recharge') rechargeCount++
    if (type === 'refund') refundCount++
  })
  
  statistics.totalAmount = Math.abs(totalAmount).toFixed(2)
  statistics.rechargeCount = rechargeCount
  statistics.refundCount = refundCount
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPayments()
}

// 筛选
const handleFilter = () => {
  currentPage.value = 1
  fetchPayments()
}

// 分页
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchPayments()
}

const handlePageChange = (page) => {
  currentPage.value = page
  fetchPayments()
}

// 查看详情
const viewDetail = (row) => {
  currentPayment.value = row
  detailDialogVisible.value = true
}

// 查看订单
const viewOrder = (orderId) => {
  // 跳转到订单管理页面
  ElMessage.info(`跳转到订单详情: ${orderId}`)
}

// 判断是否可以退款
const canRefund = (row) => {
  if (!row) return false
  // 只有支付成功的订单才能退款
  return row.orderId !== null && row.amount > 0 && row.payStatus === 1
}

// 申请退款
const handleRefund = (row) => {
  refundForm.orderId = row.orderId
  refundForm.amount = row.amount
  refundForm.reason = ''
  refundDialogVisible.value = true
}

// 确认退款
const confirmRefund = async () => {
  if (!refundForm.reason.trim()) {
    ElMessage.warning('请输入退款原因')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要对订单 ${refundForm.orderId} 申请退款吗？`,
      '确认退款',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    refunding.value = true
    
    const res = await refundPayment({
      orderId: refundForm.orderId,
      reason: refundForm.reason
    })

    if (res.code === 200) {
      ElMessage.success('退款申请成功')
      refundDialogVisible.value = false
      detailDialogVisible.value = false
      fetchPayments()
    } else {
      ElMessage.error(res.message || '退款申请失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退款失败:', error)
      ElMessage.error('退款失败，请稍后重试')
    }
  } finally {
    refunding.value = false
  }
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

// 工具函数
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString('zh-CN')
}

const formatAmount = (amount) => {
  const num = parseFloat(amount)
  const prefix = num >= 0 ? '+' : ''
  return `${prefix}¥${Math.abs(num).toFixed(2)}`
}

const getAmountColor = (row) => {
  const amount = parseFloat(row.amount)
  if (amount > 0) return '#67C23A'
  if (amount < 0) return '#F56C6C'
  return '#333'
}

const getRecordTypeTagType = (row) => {
  const type = getRecordType(row)
  const map = {
    'recharge': 'success',
    'payment': 'primary',
    'refund': 'danger'
  }
  return map[type] || ''
}

const getPayTypeTagType = (payType) => {
  const map = {
    1: 'success', // 微信
    2: 'primary', // 支付宝
    3: 'warning'  // 余额
  }
  return map[payType] || ''
}

onMounted(() => {
  fetchPayments()
})
</script>

<style scoped>
.payment-manage {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-item {
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
  font-size: 28px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 18px;
  font-weight: bold;
}

.search-row {
  margin-bottom: 20px;
}

:deep(.el-pagination) {
  display: flex;
  justify-content: center;
}
</style>

