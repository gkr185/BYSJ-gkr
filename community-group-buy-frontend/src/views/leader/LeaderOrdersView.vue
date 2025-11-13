<template>
  <MainLayout>
    <div class="leader-orders-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-left">
          <el-button :icon="ArrowLeft" @click="$router.back()">返回</el-button>
          <h2 class="page-title">
            <el-icon><DocumentCopy /></el-icon>
            团长订单管理
          </h2>
        </div>
      </div>

      <!-- 加载状态 -->
      <el-skeleton :rows="6" animated v-if="pageLoading" />

      <!-- 主体内容 -->
      <div v-else class="orders-content">
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="statistics-row">
          <el-col :xs="12" :sm="6">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon teams">
                  <el-icon><CollectionTag /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-label">我的团数</div>
                  <div class="stat-value">{{ teamsSummary.totalTeams || 0 }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon active">
                  <el-icon><Timer /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-label">进行中</div>
                  <div class="stat-value">{{ teamsSummary.activeTeams || 0 }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon success">
                  <el-icon><CircleCheck /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-label">已成团</div>
                  <div class="stat-value">{{ teamsSummary.successTeams || 0 }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="6">
            <el-card class="stat-card" shadow="hover">
              <div class="stat-content">
                <div class="stat-icon orders">
                  <el-icon><Memo /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-label">订单总数</div>
                  <div class="stat-value">{{ orderSummary.totalCount || 0 }}</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 团状态筛选 -->
        <el-card class="filter-card" shadow="hover">
          <div class="filter-header">
            <span class="filter-title">我的团购订单</span>
            <el-radio-group v-model="filterTeamStatus" size="default">
              <el-radio-button :label="null">全部团</el-radio-button>
              <el-radio-button :label="0">拼团中</el-radio-button>
              <el-radio-button :label="1">已成团</el-radio-button>
              <el-radio-button :label="2">已失败</el-radio-button>
            </el-radio-group>
          </div>
        </el-card>

        <!-- 团列表 -->
        <div v-loading="loading" class="teams-container">
          <div v-if="teamsList.length > 0" class="teams-list">
            <div v-for="team in teamsList" :key="team.teamId" class="team-card">
              <el-card shadow="hover">
                <!-- 团信息头部 -->
                <div class="team-header">
                  <div class="team-info">
                    <div class="team-title">
                      <span class="team-name">{{ team.activityName || team.productName }}</span>
                      <el-tag :type="getTeamStatusTagType(team.teamStatus)" size="small">
                        {{ getTeamStatusText(team.teamStatus) }}
                      </el-tag>
                    </div>
                    <div class="team-meta">
                      <span class="team-no">团号：{{ team.teamNo }}</span>
                      <span class="team-progress">{{ team.currentNum }}/{{ team.requiredNum }}人</span>
                      <span class="team-time" v-if="team.teamStatus === 0">
                        {{ formatExpireTime(team.expireTime) }}
                      </span>
                    </div>
                  </div>
                  <div class="team-actions">
                    <el-button 
                      type="primary" 
                      size="small" 
                      :icon="View"
                      @click="toggleTeamOrders(team.teamId)"
                    >
                      {{ expandedTeams.has(team.teamId) ? '收起订单' : '查看订单' }}
                    </el-button>
                  </div>
                </div>

                <!-- 团订单列表 -->
                <div v-if="expandedTeams.has(team.teamId)" class="team-orders">
                  <el-divider content-position="left">
                    <span class="orders-title">
                      <el-icon><List /></el-icon>
                      团内订单 ({{ teamOrders[team.teamId]?.length || 0 }})
                    </span>
                  </el-divider>
                  
                  <div v-loading="teamOrdersLoading[team.teamId]" class="orders-loading-container">
                    <div v-if="teamOrders[team.teamId]?.length > 0" class="orders-list">
                      <div 
                        v-for="order in teamOrders[team.teamId]" 
                        :key="order.orderId" 
                        class="order-item"
                      >
                        <div class="order-content">
                          <img :src="order.productImg || order.items?.[0]?.productImg" class="product-img" alt="商品图片" />
                          <div class="order-info">
                            <div class="product-name">{{ order.productName || order.items?.[0]?.productName }}</div>
                            <div class="order-meta">
                              <span class="meta-item">
                                <el-icon><User /></el-icon>
                                {{ order.userName || '用户' }}
                              </span>
                              <span class="meta-item">
                                <el-icon><ShoppingCart /></el-icon>
                                x{{ order.quantity || order.items?.[0]?.quantity }}
                              </span>
                              <span class="meta-item">
                                <el-icon><Calendar /></el-icon>
                                {{ formatTime(order.createTime) }}
                              </span>
                            </div>
                          </div>
                          <div class="order-right">
                            <div class="order-amount">¥{{ formatMoney(order.payAmount || order.totalAmount) }}</div>
                            <div class="order-actions">
                              <el-tag :type="getStatusTagType(order.orderStatus)" size="small">
                                {{ getStatusText(order.orderStatus) }}
                              </el-tag>
                              <el-button 
                                type="primary" 
                                size="small" 
                                link
                                @click="viewOrderDetail(order.orderId)"
                              >
                                详情
                              </el-button>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    <el-empty 
                      v-else-if="!teamOrdersLoading[team.teamId]" 
                      description="该团暂无订单" 
                      :image-size="80"
                    />
                  </div>
                </div>
              </el-card>
            </div>
          </div>
          
          <!-- 空数据 -->
          <el-empty v-if="!loading && teamsList.length === 0" description="暂无团购记录" />
          
          <!-- 分页 -->
          <div v-if="teamsList.length > 0" class="pagination-wrapper">
            <el-pagination
              :current-page="currentPage"
              :total="total"
              :page-size="pageSize"
              layout="total, prev, pager, next, jumper"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getLeaderOrdersSummary, getTeamOrders } from '@/api/order'
import { ORDER_STATUS_TEXT, ORDER_STATUS_TAG_TYPE } from '@/api/order'
import { getLeaderTeams } from '@/api/groupbuy'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, 
  DocumentCopy, 
  User,
  ShoppingCart,
  Calendar,
  CollectionTag,
  Timer,
  CircleCheck,
  Memo,
  View,
  List
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'

const router = useRouter()
const userStore = useUserStore()
const leaderId = userStore.userInfo?.userId

// 页面加载状态
const pageLoading = ref(true)

// 统计数据
const orderSummary = ref({
  totalCount: 0,
  todayCount: 0,
  pendingCount: 0,
  deliveringCount: 0
})

const teamsSummary = ref({
  totalTeams: 0,
  activeTeams: 0,
  successTeams: 0
})

// 团列表数据
const teamsList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterTeamStatus = ref(null)

// 展开的团和团订单
const expandedTeams = ref(new Set())
const teamOrders = ref({}) // teamId -> orders[]
const teamOrdersLoading = reactive({}) // teamId -> loading state

// 团状态枚举
const TEAM_STATUS = {
  ACTIVE: 0,    // 拼团中
  SUCCESS: 1,   // 已成团
  FAILED: 2     // 已失败
}

// 团状态文本映射
const TEAM_STATUS_TEXT = {
  [TEAM_STATUS.ACTIVE]: '拼团中',
  [TEAM_STATUS.SUCCESS]: '已成团',
  [TEAM_STATUS.FAILED]: '已失败'
}

// 团状态标签类型
const TEAM_STATUS_TAG_TYPE = {
  [TEAM_STATUS.ACTIVE]: 'warning',
  [TEAM_STATUS.SUCCESS]: 'success',
  [TEAM_STATUS.FAILED]: 'danger'
}

// 加载订单统计数据
const loadOrderSummary = async () => {
  try {
    const res = await getLeaderOrdersSummary(leaderId)
    if (res.code === 200) {
      orderSummary.value = res.data
    }
  } catch (error) {
    console.error('加载订单统计失败', error)
  }
}

// 加载团长团队列表
const loadTeamsList = async () => {
  loading.value = true
  try {
    const res = await getLeaderTeams({
      leaderId,
      status: filterTeamStatus.value,
      page: currentPage.value - 1,
      limit: pageSize.value
    })

    if (res.code === 200) {
      teamsList.value = res.data.list || []
      total.value = res.data.total || 0

      // 计算团统计
      calculateTeamsSummary(res.data.list || [])
    }
  } catch (error) {
    ElMessage.error('加载团列表失败，请稍后重试')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 加载团订单
const loadTeamOrders = async (teamId) => {
  teamOrdersLoading[teamId] = true
  try {
    const res = await getTeamOrders(teamId)
    if (res.code === 200) {
      teamOrders.value[teamId] = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载团订单失败')
    console.error(`加载团${teamId}订单失败:`, error)
    teamOrders.value[teamId] = []
  } finally {
    teamOrdersLoading[teamId] = false
  }
}

// 计算团统计数据
const calculateTeamsSummary = (teams) => {
  const summary = {
    totalTeams: teams.length,
    activeTeams: 0,
    successTeams: 0
  }

  teams.forEach(team => {
    switch (team.teamStatus) {
      case TEAM_STATUS.ACTIVE:
        summary.activeTeams++
        break
      case TEAM_STATUS.SUCCESS:
        summary.successTeams++
        break
    }
  })

  teamsSummary.value = summary
}

// 切换团订单展示
const toggleTeamOrders = async (teamId) => {
  if (expandedTeams.value.has(teamId)) {
    expandedTeams.value.delete(teamId)
    return
  }
  
  expandedTeams.value.add(teamId)
  
  // 如果已经加载过该团的订单，直接展示
  if (teamOrders.value[teamId]) {
    return
  }
  
  // 加载团订单
  await loadTeamOrders(teamId)
}

// 查看订单详情（跳转到团长订单详情页）
const viewOrderDetail = (orderId) => {
  router.push(`/leader/order-detail/${orderId}`)
}

// 团状态文本
const getTeamStatusText = (status) => {
  return TEAM_STATUS_TEXT[status] || '未知'
}

// 团状态标签类型
const getTeamStatusTagType = (status) => {
  return TEAM_STATUS_TAG_TYPE[status] || 'info'
}

// 订单状态文本
const getStatusText = (status) => {
  return ORDER_STATUS_TEXT[status] || '未知'
}

// 订单状态标签类型
const getStatusTagType = (status) => {
  return ORDER_STATUS_TAG_TYPE[status] || 'info'
}

// 格式化金额
const formatMoney = (value) => {
  return value ? value.toFixed(2) : '0.00'
}

// 格式化时间
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

// 格式化过期时间
const formatExpireTime = (expireTime) => {
  if (!expireTime) return ''
  const now = new Date()
  const expire = new Date(expireTime)
  const diff = expire - now
  
  if (diff <= 0) return '已过期'
  
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  
  return `剩余 ${hours}小时${minutes}分钟`
}

// 处理分页变化
const handlePageChange = (page) => {
  currentPage.value = page
  loadTeamsList()
}

// 监听筛选条件
watch(filterTeamStatus, () => {
  currentPage.value = 1
  loadTeamsList()
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
    loadOrderSummary(),
    loadTeamsList()
  ])
  
  pageLoading.value = false
})
</script>

<style scoped>
.leader-orders-container {
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

.orders-content {
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

.stat-icon.teams {
  background: linear-gradient(135deg, #e8f4ff 0%, #d1e9ff 100%);
  color: #409eff;
}

.stat-icon.active {
  background: linear-gradient(135deg, #fff4e6 0%, #ffe7cc 100%);
  color: #e6a23c;
}

.stat-icon.success {
  background: linear-gradient(135deg, #e6f7f0 0%, #d0f0e3 100%);
  color: #67c23a;
}

.stat-icon.orders {
  background: linear-gradient(135deg, #fef0e6 0%, #fde2cc 100%);
  color: #f56c6c;
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
  line-height: 1;
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

/* 团列表样式 */
.teams-container {
  min-height: 400px;
}

.teams-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 20px;
}

.team-card {
  transition: transform 0.2s;
}

.team-card:hover {
  transform: translateY(-2px);
}

.team-card :deep(.el-card__body) {
  padding: 20px;
}

.team-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.team-info {
  flex: 1;
}

.team-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.team-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.team-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #606266;
  flex-wrap: wrap;
}

.team-no {
  font-weight: 500;
}

.team-progress {
  color: #409eff;
  font-weight: 500;
}

.team-time {
  color: #e6a23c;
  font-weight: 500;
}

.team-actions {
  flex-shrink: 0;
}

/* 团订单列表样式 */
.team-orders {
  margin-top: 16px;
}

.orders-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.orders-loading-container {
  min-height: 100px;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.2s;
}

.order-item:hover {
  background: #f0f9ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.order-content {
  display: flex;
  gap: 16px;
  align-items: center;
}

.product-img {
  width: 70px;
  height: 70px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}

.order-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.order-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
}

.order-amount {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
}

.order-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

/* 响应式 */
@media (max-width: 768px) {
  .leader-orders-container {
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

  .team-header {
    flex-direction: column;
    gap: 12px;
  }

  .team-meta {
    flex-direction: column;
    gap: 8px;
  }

  .order-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .order-right {
    width: 100%;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
  }

  .product-img {
    width: 60px;
    height: 60px;
  }

  .order-meta {
    flex-direction: column;
    gap: 4px;
  }
}
</style>
