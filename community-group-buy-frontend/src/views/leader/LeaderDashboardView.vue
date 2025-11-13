<template>
  <MainLayout>
    <div class="leader-dashboard-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <el-button :icon="ArrowLeft" @click="$router.back()">返回</el-button>
          <h2 class="page-title">
            <el-icon><Briefcase /></el-icon>
            团长工作台
          </h2>
        </div>
        <div class="header-right">
          <el-tag :type="storeStatusType" size="large" effect="dark">
            {{ storeStatusText }}
          </el-tag>
        </div>
      </div>

      <!-- 加载状态 -->
      <el-skeleton :rows="6" animated v-if="pageLoading" />

      <!-- 主体内容 -->
      <div v-else class="dashboard-content">
        <!-- 数据概览卡片 -->
        <el-row :gutter="20" class="statistics-row">
          <!-- 待结算佣金 -->
          <el-col :xs="24" :sm="12" :md="8">
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
          <el-col :xs="24" :sm="12" :md="8">
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
          <el-col :xs="24" :sm="12" :md="8">
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

        <!-- 团点信息卡片 -->
        <el-card class="store-info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon><Shop /></el-icon>
                <span>团点信息</span>
              </div>
              <el-button
                type="primary"
                size="small"
                :icon="Edit"
                @click="showEditStoreDialog = true"
              >
                编辑信息
              </el-button>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="团点名称">
              <el-text tag="b">{{ storeInfo.storeName }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="团长姓名">
              {{ storeInfo.leaderName }}
            </el-descriptions-item>
            <el-descriptions-item label="联系电话">
              <el-text type="primary">{{ storeInfo.leaderPhone }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="所属社区">
              {{ storeInfo.communityName }}
            </el-descriptions-item>
            <el-descriptions-item label="团点地址" :span="2">
              <el-text>
                <el-icon><Location /></el-icon>
                {{ storeInfo.address }}
              </el-text>
            </el-descriptions-item>
            <el-descriptions-item label="佣金比例">
              <el-tag type="success" effect="dark">{{ storeInfo.commissionRate }}%</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="运营状态">
              <el-tag :type="storeStatusType">{{ storeStatusText }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">
              {{ formatDateTime(storeInfo.createdAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="团点简介" :span="2" v-if="storeInfo.description">
              <el-text type="info">{{ storeInfo.description }}</el-text>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 快捷操作 -->
        <el-card class="quick-actions-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Operation /></el-icon>
              <span>快捷操作</span>
            </div>
          </template>
          <div class="actions-grid">
            <div class="action-item" @click="handleViewGroupBuy">
              <el-icon class="action-icon"><Grid /></el-icon>
              <div class="action-text">拼团活动</div>
            </div>
            <div class="action-item" @click="handleViewCommission">
              <el-icon class="action-icon"><Coin /></el-icon>
              <div class="action-text">佣金管理</div>
            </div>
            <div class="action-item" @click="handleViewOrders">
              <el-icon class="action-icon"><DocumentCopy /></el-icon>
              <div class="action-text">我的订单</div>
            </div>
            <div class="action-item" @click="handleViewCommunity">
              <el-icon class="action-icon"><OfficeBuilding /></el-icon>
              <div class="action-text">社区详情</div>
            </div>    
            <div class="action-item" @click="handleViewStatistics">
              <el-icon class="action-icon"><DataAnalysis /></el-icon>
              <div class="action-text">数据统计</div>
            </div>
          </div>
        </el-card>

        <!-- 佣金记录 -->
        <el-card class="commission-records-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <div class="header-left">
                <el-icon><Money /></el-icon>
                <span>佣金记录</span>
              </div>
              <el-radio-group v-model="commissionStatusFilter" size="small" @change="handleFilterChange">
                <el-radio-button :label="null">全部</el-radio-button>
                <el-radio-button :label="0">待结算</el-radio-button>
                <el-radio-button :label="1">已结算</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <el-table
            :data="commissionRecords"
            v-loading="tableLoading"
            stripe
            :header-cell-style="{ background: '#f5f7fa' }"
          >
            <el-table-column prop="recordId" label="记录ID" width="80" align="center" />
            <el-table-column prop="orderId" label="订单ID" width="100" align="center">
              <template #default="{ row }">
                <el-link type="primary" @click="handleViewOrder(row.orderId)">
                  #{{ row.orderId }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="orderAmount" label="订单金额" width="120" align="right">
              <template #default="{ row }">
                <el-text type="success">¥{{ formatMoney(row.orderAmount) }}</el-text>
              </template>
            </el-table-column>
            <el-table-column prop="commissionRate" label="佣金比例" width="100" align="center">
              <template #default="{ row }">
                {{ row.commissionRate }}%
              </template>
            </el-table-column>
            <el-table-column prop="commissionAmount" label="佣金金额" width="120" align="right">
              <template #default="{ row }">
                <el-text tag="b" type="danger">¥{{ formatMoney(row.commissionAmount) }}</el-text>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="结算状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
                  {{ row.status === 1 ? '已结算' : '待结算' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="settlementBatch" label="结算批次" width="120" align="center">
              <template #default="{ row }">
                <el-text v-if="row.settlementBatch" size="small">{{ row.settlementBatch }}</el-text>
                <el-text v-else type="info" size="small">-</el-text>
              </template>
            </el-table-column>
            <el-table-column prop="settledAt" label="结算时间" width="180" align="center">
              <template #default="{ row }">
                <el-text v-if="row.settledAt" size="small">{{ formatDateTime(row.settledAt) }}</el-text>
                <el-text v-else type="info" size="small">-</el-text>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="生成时间" width="180" align="center">
              <template #default="{ row }">
                <el-text size="small">{{ formatDateTime(row.createdAt) }}</el-text>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="150">
              <template #default="{ row }">
                <el-text v-if="row.remark" size="small" type="info">{{ row.remark }}</el-text>
                <el-text v-else size="small" type="info">-</el-text>
              </template>
            </el-table-column>
          </el-table>

          <!-- 空数据提示 -->
          <el-empty 
            v-if="!tableLoading && commissionRecords.length === 0" 
            description="暂无佣金记录"
            :image-size="120"
          />

          <!-- 分页 -->
          <div v-if="commissionRecords.length > 0" class="pagination-container">
            <el-pagination
              :current-page="pagination.page"
              :page-size="pagination.limit"
              :total="pagination.total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </el-card>
      </div>

      <!-- 编辑团点信息对话框 -->
      <el-dialog
        v-model="showEditStoreDialog"
        title="编辑团点信息"
        width="600px"
        :close-on-click-modal="false"
      >
        <el-form
          ref="storeFormRef"
          :model="storeForm"
          :rules="storeFormRules"
          label-width="100px"
        >
          <el-form-item label="团点名称" prop="storeName">
            <el-input v-model="storeForm.storeName" placeholder="请输入团点名称" clearable />
          </el-form-item>
          <el-form-item label="联系电话" prop="leaderPhone">
            <el-input v-model="storeForm.leaderPhone" placeholder="请输入联系电话" maxlength="11" clearable />
          </el-form-item>
          <el-form-item label="团点地址" prop="address">
            <el-input v-model="storeForm.address" placeholder="请输入团点详细地址" clearable />
          </el-form-item>
          <el-form-item label="团点简介" prop="description">
            <el-input
              v-model="storeForm.description"
              type="textarea"
              :rows="4"
              placeholder="请输入团点简介（可选，最多500字）"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="showEditStoreDialog = false">取消</el-button>
          <el-button type="primary" :loading="saveLoading" @click="handleSaveStoreInfo">
            保存
          </el-button>
        </template>
      </el-dialog>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Briefcase,
  Shop,
  Edit,
  Location,
  Clock,
  CircleCheck,
  TrendCharts,
  Money,
  Operation,
  Grid,
  OfficeBuilding,
  DocumentCopy,
  Goods,
  DataAnalysis,
  Coin
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import {
  getMyLeaderInfo,
  getMyCommissionRecords,
  getMyCommissionSummary,
  updateLeaderStore,
  getCommunityDetail
} from '@/api/leader'

const router = useRouter()
const userStore = useUserStore()

// 页面状态
const pageLoading = ref(true)
const tableLoading = ref(false)
const saveLoading = ref(false)
const showEditStoreDialog = ref(false)

// 团点信息
const storeInfo = ref({
  storeId: null,
  leaderId: null,
  leaderName: '',
  leaderPhone: '',
  communityId: null,
  communityName: '',
  storeName: '',
  address: '',
  description: '',
  commissionRate: 0,
  totalCommission: 0,
  status: 0,
  createdAt: null
})

// 佣金统计
const commissionSummary = ref({
  pendingCommission: 0,
  settledCommission: 0,
  totalCommission: 0
})

// 佣金记录
const commissionRecords = ref([])
const commissionStatusFilter = ref(null)

// 分页
const pagination = reactive({
  page: 1,
  limit: 10,
  total: 0
})

// 编辑表单
const storeFormRef = ref(null)
const storeForm = reactive({
  storeName: '',
  leaderPhone: '',
  address: '',
  description: ''
})

const storeFormRules = {
  storeName: [
    { required: true, message: '请输入团点名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  leaderPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入团点地址', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ]
}

// 团点状态相关
const storeStatusText = computed(() => {
  switch (storeInfo.value.status) {
    case 0:
      return '待审核'
    case 1:
      return '正常运营'
    case 2:
      return '已停用'
    default:
      return '未知'
  }
})

const storeStatusType = computed(() => {
  switch (storeInfo.value.status) {
    case 0:
      return 'warning'
    case 1:
      return 'success'
    case 2:
      return 'danger'
    default:
      return 'info'
  }
})

// 加载团长信息
const loadStoreInfo = async () => {
  if (!userStore.userInfo?.userId) {
    ElMessage.error('用户信息不存在')
    return
  }

  try {
    const res = await getMyLeaderInfo(userStore.userInfo.userId)
    if (res.code === 200 && res.data) {
      storeInfo.value = res.data
    } else {
      ElMessage.error('您还不是团长，无法访问工作台')
      router.push('/leader/apply')
    }
  } catch (error) {
    console.error('加载团长信息失败:', error)
    ElMessage.error('加载团长信息失败')
  }
}

// 加载佣金统计
const loadCommissionSummary = async () => {
  if (!storeInfo.value.leaderId) return

  try {
    const res = await getMyCommissionSummary(storeInfo.value.leaderId)
    if (res.code === 200 && res.data) {
      commissionSummary.value = res.data
    }
  } catch (error) {
    console.error('加载佣金统计失败:', error)
  }
}

// 加载佣金记录
const loadCommissionRecords = async () => {
  if (!storeInfo.value.leaderId) {
    console.warn('leaderId不存在，跳过佣金记录加载')
    return
  }

  tableLoading.value = true
  try {
    const params = {
      leaderId: storeInfo.value.leaderId,
      page: pagination.page - 1,  // 后端从0开始
      limit: pagination.limit
    }
    
    if (commissionStatusFilter.value !== null) {
      params.status = commissionStatusFilter.value
    }

    console.log('正在加载佣金记录，参数:', params)
    const res = await getMyCommissionRecords(params)
    console.log('佣金记录API响应:', res)
    
    if (res.code === 200 && res.data) {
      commissionRecords.value = res.data.list || []
      pagination.total = res.data.total || 0
      console.log('佣金记录加载成功:', {
        recordsCount: commissionRecords.value.length,
        total: pagination.total
      })
    } else {
      console.error('佣金记录API返回错误:', res)
      ElMessage.error(res.message || '加载佣金记录失败')
    }
  } catch (error) {
    console.error('加载佣金记录失败:', error)
    ElMessage.error('加载佣金记录失败')
  } finally {
    tableLoading.value = false
  }
}

// 筛选变化
const handleFilterChange = () => {
  pagination.page = 1
  loadCommissionRecords()
}

// 分页变化
const handlePageChange = (page) => {
  pagination.page = page
  loadCommissionRecords()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.page = 1
  pagination.limit = size
  loadCommissionRecords()
}

// 编辑团点信息
const handleSaveStoreInfo = async () => {
  await storeFormRef.value.validate(async (valid) => {
    if (!valid) return

    saveLoading.value = true
    try {
      const res = await updateLeaderStore(storeInfo.value.storeId, storeForm)
      if (res.code === 200) {
        ElMessage.success('团点信息更新成功')
        showEditStoreDialog.value = false
        await loadStoreInfo()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } catch (error) {
      console.error('更新团点信息失败:', error)
      ElMessage.error('更新团点信息失败')
    } finally {
      saveLoading.value = false
    }
  })
}

// 快捷操作
const handleViewGroupBuy = () => {
  router.push('/leader/groupbuy')
}

const handleViewCommunity = async () => {
  if (!storeInfo.value.communityId) {
    ElMessage.warning('社区信息不存在')
    return
  }

  try {
    const res = await getCommunityDetail(storeInfo.value.communityId)
    if (res.code === 200 && res.data) {
      ElMessageBox.alert(
        `
          <div style="line-height: 1.8;">
            <p><strong>社区名称：</strong>${res.data.communityName}</p>
            <p><strong>详细地址：</strong>${res.data.address}</p>
            <p><strong>服务半径：</strong>${res.data.serviceRadius}米</p>
            <p><strong>运营状态：</strong>${res.data.status === 1 ? '正常运营' : '其他'}</p>
          </div>
        `,
        '社区详情',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '关闭'
        }
      )
    }
  } catch (error) {
    console.error('加载社区详情失败:', error)
    ElMessage.error('加载社区详情失败')
  }
}

const handleViewOrders = () => {
  router.push('/leader/orders')
}

const handleViewCommission = () => {
  router.push('/leader/commission')
}

const handleViewProducts = () => {
  ElMessage.info('商品管理功能开发中，敬请期待')
  // TODO: 跳转到商品管理页面
}

const handleViewStatistics = () => {
  ElMessage.info('数据统计功能开发中，敬请期待')
  // TODO: 跳转到数据统计页面
}

const handleViewOrder = (orderId) => {
  ElMessage.info(`订单详情功能开发中，订单ID: ${orderId}`)
  // TODO: 跳转到订单详情页面
}

// 格式化金额
const formatMoney = (value) => {
  if (!value && value !== 0) return '0.00'
  return Number(value).toFixed(2)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 打开编辑对话框时填充数据
const openEditDialog = () => {
  storeForm.storeName = storeInfo.value.storeName
  storeForm.leaderPhone = storeInfo.value.leaderPhone
  storeForm.address = storeInfo.value.address
  storeForm.description = storeInfo.value.description || ''
}

// 监听对话框打开
const watchDialogOpen = () => {
  if (showEditStoreDialog.value) {
    openEditDialog()
  }
}

// 初始化
onMounted(async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (!userStore.isLeader) {
    ElMessage.warning('您还不是团长，请先申请')
    router.push('/leader/apply')
    return
  }

  pageLoading.value = true
  try {
    await loadStoreInfo()
    await Promise.all([
      loadCommissionSummary(),
      loadCommissionRecords()
    ])
  } finally {
    pageLoading.value = false
  }
})

// 监听编辑对话框打开
const unwatchDialog = () => {
  if (showEditStoreDialog.value) {
    openEditDialog()
  }
}

// Watch for dialog changes
import { watch } from 'vue'
watch(showEditStoreDialog, unwatchDialog)
</script>

<style scoped>
.leader-dashboard-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 统计卡片 */
.statistics-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 16px;
  transition: all 0.3s;
  border: none;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  flex-shrink: 0;
}

.stat-icon.pending {
  background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  color: #d63031;
}

.stat-icon.settled {
  background: linear-gradient(135deg, #a8e6cf 0%, #55efc4 100%);
  color: #00b894;
}

.stat-icon.total {
  background: linear-gradient(135deg, #a29bfe 0%, #6c5ce7 100%);
  color: #fff;
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
}

.stat-tip {
  font-size: 12px;
  color: #c0c4cc;
}

/* 团点信息卡片 */
.store-info-card,
.quick-actions-card,
.commission-records-card {
  margin-bottom: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.card-header .header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 快捷操作 */
.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  border-radius: 12px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  cursor: pointer;
  transition: all 0.3s;
}

.action-item:hover {
  background: linear-gradient(135deg, #409eff 0%, #5da8ff 100%);
  color: #fff;
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(64, 158, 255, 0.3);
}

.action-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.action-text {
  font-size: 14px;
  font-weight: 600;
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 12px;
}

:deep(.el-table th) {
  font-weight: 700;
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .leader-dashboard-container {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header-left,
  .header-right {
    width: 100%;
    justify-content: space-between;
  }

  .stat-value {
    font-size: 24px;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  :deep(.el-table) {
    font-size: 12px;
  }
}
</style>

