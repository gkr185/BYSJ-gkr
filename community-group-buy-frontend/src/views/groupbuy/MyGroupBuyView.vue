<template>
  <div class="my-groupbuy-view">
    <div class="page-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>我的拼团</h2>
        <p class="subtitle">查看我参与的所有拼团记录</p>
      </div>
      
      <!-- 状态筛选 -->
      <el-card class="filter-card">
        <el-radio-group v-model="statusFilter" @change="handleFilterChange">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="0">拼团中</el-radio-button>
          <el-radio-button label="1">已成团</el-radio-button>
          <el-radio-button label="3">已取消</el-radio-button>
        </el-radio-group>
      </el-card>
      
      <!-- 拼团列表 -->
      <div v-loading="loading" class="team-list">
        <el-empty
          v-if="!loading && filteredTeams.length === 0"
          description="暂无拼团记录"
        />
        
        <el-card
          v-for="team in filteredTeams"
          :key="team.member_id"
          class="team-card"
          shadow="hover"
        >
          <!-- 团头信息 -->
          <div class="team-header">
            <div class="team-status">
              <el-tag :type="getStatusType(team.status)" size="small">
                {{ getStatusText(team.status) }}
              </el-tag>
              <el-tag v-if="team.is_launcher" type="warning" size="small">
                我发起的
              </el-tag>
            </div>
            <div class="team-no">团号：{{ team.team_no }}</div>
          </div>
          
          <!-- 商品信息 -->
          <div class="product-section">
            <el-image
              :src="team.product_img"
              fit="cover"
              class="product-img"
            />
            <div class="product-info">
              <div class="product-name">{{ team.product_name }}</div>
              <div class="product-price">¥{{ team.group_price }}</div>
            </div>
          </div>
          
          <!-- 拼团进度 -->
          <div class="team-progress">
            <div class="progress-info">
              <span>拼团进度</span>
              <span class="progress-text">
                {{ team.current_num }}/{{ team.required_num }}人
              </span>
            </div>
            <el-progress
              :percentage="(team.current_num / team.required_num) * 100"
              :status="team.team_status === 1 ? 'success' : 'primary'"
            />
          </div>
          
          <!-- 时间信息 -->
          <div class="team-time">
            <div class="time-item">
              <el-icon><Clock /></el-icon>
              <span>参团时间：{{ formatTime(team.join_time) }}</span>
            </div>
            <div v-if="team.team_status === 0 && team.expire_time" class="time-item expire">
              <el-icon><Timer /></el-icon>
              <span>{{ getRemaining(team.expire_time) }}</span>
            </div>
            <div v-if="team.success_time" class="time-item success">
              <el-icon><CircleCheckFilled /></el-icon>
              <span>成团时间：{{ formatTime(team.success_time) }}</span>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="team-actions">
            <el-button
              size="small"
              @click="goToTeamDetail(team.team_id)"
            >
              查看详情
            </el-button>
            
            <el-button
              v-if="team.team_status === 0"
              size="small"
              type="primary"
              @click="shareTeam(team)"
            >
              <el-icon><Share /></el-icon>
              邀请好友
            </el-button>
            
            <el-button
              v-if="team.team_status === 0 && team.status === 1"
              size="small"
              type="danger"
              plain
              @click="handleQuitTeam(team)"
            >
              退出拼团
            </el-button>
            
            <el-button
              v-if="team.order_id"
              size="small"
              @click="goToOrderDetail(team.order_id)"
            >
              查看订单
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Clock,
  Timer,
  CircleCheckFilled,
  Share
} from '@element-plus/icons-vue'
import { apiGetMyTeams, apiQuitTeam, calculateRemainingTime } from '@/api/groupbuy'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 数据
const statusFilter = ref('all')
const loading = ref(true)
const myTeams = ref([])

// 过滤后的拼团列表
const filteredTeams = computed(() => {
  if (statusFilter.value === 'all') {
    return myTeams.value
  }
  return myTeams.value.filter(team => team.status === parseInt(statusFilter.value))
})

// 加载我的拼团
const loadMyTeams = async () => {
  try {
    loading.value = true
    const data = await apiGetMyTeams()
    myTeams.value = data
  } catch (error) {
    console.error('加载拼团列表失败:', error)
    ElMessage.error('加载拼团列表失败')
  } finally {
    loading.value = false
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '已支付',
    2: '已成团',
    3: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'primary',
    2: 'success',
    3: 'info'
  }
  return typeMap[status] || 'info'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  // 1小时内显示"刚刚"
  if (diff < 3600000) {
    return '刚刚'
  }
  
  // 24小时内显示"X小时前"
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }
  
  // 7天内显示"X天前"
  if (diff < 604800000) {
    const days = Math.floor(diff / 86400000)
    return `${days}天前`
  }
  
  // 否则显示完整日期
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取剩余时间
const getRemaining = (expireTime) => {
  const remaining = calculateRemainingTime(expireTime)
  
  if (remaining.expired) {
    return '已过期'
  }
  
  if (remaining.hours > 0) {
    return `剩余 ${remaining.hours}小时${remaining.minutes}分钟`
  }
  
  return `剩余 ${remaining.minutes}分钟`
}

// 筛选变化
const handleFilterChange = () => {
  // 筛选逻辑已由computed处理
}

// 跳转到团详情
const goToTeamDetail = (teamId) => {
  router.push(`/groupbuy/team/${teamId}`)
}

// 跳转到订单详情
const goToOrderDetail = (orderId) => {
  router.push(`/user/orders?order_id=${orderId}`)
}

// 分享拼团
const shareTeam = (team) => {
  const shareUrl = `${window.location.origin}/groupbuy/team/${team.team_id}`
  
  ElMessageBox.alert(
    `
    <div style="text-align: center; padding: 20px;">
      <p style="margin-bottom: 12px; font-size: 14px; color: #606266;">分享链接给好友，邀请他们参团：</p>
      <div style="padding: 12px; background-color: #f5f7fa; border-radius: 4px; word-break: break-all; font-size: 13px; color: #303133;">
        ${shareUrl}
      </div>
      <p style="margin-top: 12px; font-size: 12px; color: #909399;">点击下方按钮复制链接</p>
    </div>
    `,
    '邀请好友参团',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '复制链接',
      callback: () => {
        // 复制到剪贴板
        navigator.clipboard.writeText(shareUrl).then(() => {
          ElMessage.success('链接已复制到剪贴板')
        }).catch(() => {
          ElMessage.warning('复制失败，请手动复制')
        })
      }
    }
  )
}

// 退出拼团
const handleQuitTeam = async (team) => {
  try {
    await ElMessageBox.confirm(
      `确定要退出拼团吗？退款将原路返回到您的支付账户。`,
      '退出拼团',
      {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await apiQuitTeam(team.team_id)
    ElMessage.success('已退出拼团，退款将在1-3个工作日内到账')
    
    // 重新加载列表
    loadMyTeams()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出拼团失败:', error)
      ElMessage.error(error.message || '退出拼团失败')
    }
  }
}

// 页面加载
onMounted(() => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  loadMyTeams()
})
</script>

<style scoped lang="scss">
.my-groupbuy-view {
  min-height: 100vh;
  padding-top: 84px; /* 64px导航栏 + 20px间隔 */
  background-color: #f5f5f5;
  padding-bottom: 40px;
}

.page-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
  
  h2 {
    font-size: 24px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }
  
  .subtitle {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.filter-card {
  margin-bottom: 20px;
  
  :deep(.el-card__body) {
    padding: 16px;
  }
}

.team-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.team-card {
  :deep(.el-card__body) {
    padding: 20px;
  }
  
  .team-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 1px dashed #e4e7ed;
    
    .team-status {
      display: flex;
      gap: 8px;
    }
    
    .team-no {
      font-size: 13px;
      color: #909399;
    }
  }
  
  .product-section {
    display: flex;
    gap: 16px;
    margin-bottom: 16px;
    
    .product-img {
      width: 80px;
      height: 80px;
      border-radius: 8px;
      flex-shrink: 0;
    }
    
    .product-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
      
      .product-name {
        font-size: 15px;
        color: #303133;
        margin-bottom: 8px;
        font-weight: 500;
      }
      
      .product-price {
        font-size: 18px;
        color: #f56c6c;
        font-weight: 600;
      }
    }
  }
  
  .team-progress {
    margin-bottom: 16px;
    
    .progress-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      font-size: 14px;
      
      .progress-text {
        color: #409eff;
        font-weight: 600;
      }
    }
  }
  
  .team-time {
    margin-bottom: 16px;
    
    .time-item {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #606266;
      margin-bottom: 6px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .el-icon {
        font-size: 14px;
      }
      
      &.expire {
        color: #f56c6c;
      }
      
      &.success {
        color: #67c23a;
      }
    }
  }
  
  .team-actions {
    display: flex;
    gap: 8px;
    justify-content: flex-end;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-groupbuy-view {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }
  
  .page-container {
    padding: 12px;
  }
  
  .page-header {
    h2 {
      font-size: 20px;
    }
  }
  
  .filter-card {
    :deep(.el-radio-button__inner) {
      font-size: 13px;
      padding: 8px 12px;
    }
  }
  
  .team-card {
    .team-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
    }
    
    .product-section {
      .product-img {
        width: 60px;
        height: 60px;
      }
      
      .product-info {
        .product-name {
          font-size: 14px;
        }
        
        .product-price {
          font-size: 16px;
        }
      }
    }
    
    .team-actions {
      flex-wrap: wrap;
      
      .el-button {
        flex: 1;
        min-width: calc(50% - 4px);
      }
    }
  }
}
</style>

