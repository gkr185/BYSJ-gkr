<template>
  <MainLayout>
    <div class="groups-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">
          <el-icon><Grid /></el-icon>
          我的拼团
        </h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/profile' }">个人中心</el-breadcrumb-item>
          <el-breadcrumb-item>我的拼团</el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 拼团状态筛选 -->
      <div class="status-tabs">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="全部拼团" :name="'all'">
            <template #label>
              <span class="tab-label">
                <el-icon><List /></el-icon>
                全部拼团
                <el-badge v-if="stats.total > 0" :value="stats.total" class="status-badge" />
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane :label="`拼团中 (${stats.joining || 0})`" :name="TEAM_STATUS.JOINING">
            <template #label>
              <span class="tab-label">
                <el-icon><Clock /></el-icon>
                拼团中
                <el-badge v-if="stats.joining > 0" :value="stats.joining" type="warning" class="status-badge" />
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane :label="`已成团 (${stats.success || 0})`" :name="TEAM_STATUS.SUCCESS">
            <template #label>
              <span class="tab-label">
                <el-icon><CircleCheck /></el-icon>
                已成团
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane :label="`已失败 (${stats.failed || 0})`" :name="TEAM_STATUS.FAILED">
            <template #label>
              <span class="tab-label">
                <el-icon><CircleClose /></el-icon>
                已失败
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <!-- 拼团列表 -->
      <div v-else-if="filteredGroups.length > 0" class="groups-list">
        <div v-for="group in filteredGroups" :key="group.memberId" class="group-card">
          <!-- 拼团头部 -->
          <div class="group-header">
            <div class="group-info">
              <span class="team-no">团号: {{ group.teamNo }}</span>
              <el-tag
                :type="TEAM_STATUS_TAG_TYPE[group.teamStatus]"
                effect="dark"
                size="small"
              >
                {{ TEAM_STATUS_TEXT[group.teamStatus] }}
              </el-tag>
              <el-tag v-if="group.isLauncher === 1" type="warning" size="small">
                <el-icon><Star /></el-icon>
                团长
              </el-tag>
            </div>
            <div class="group-time">
              <el-icon><Clock /></el-icon>
              {{ formatDateTime(group.joinTime) }}
            </div>
          </div>

          <!-- 拼团内容 -->
          <div class="group-content">
            <!-- 商品信息 -->
            <div class="product-section">
              <el-image
                :src="group.productCoverImg || group.productImg || '/placeholder-product.png'"
                fit="cover"
                class="product-image"
                :preview-src-list="group.productCoverImg ? [group.productCoverImg] : (group.productImg ? [group.productImg] : [])"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="product-info">
                <div class="product-name">{{ group.productName || '商品名称' }}</div>
                <div class="product-meta">
                  <span class="product-price">¥{{ group.payAmount?.toFixed(2) }}</span>
                  <span class="activity-name">{{ group.activityName }}</span>
                </div>
                <div class="community-info">
                  <el-icon><Location /></el-icon>
                  {{ group.communityName || '社区' }}
                </div>
              </div>
            </div>

            <!-- 拼团进度 -->
            <div class="progress-section">
              <div class="progress-header">
                <span class="progress-title">拼团进度</span>
                <span class="progress-text">
                  {{ group.currentNum }} / {{ group.requiredNum }}人
                </span>
              </div>
              <el-progress
                :percentage="Math.round((group.currentNum / group.requiredNum) * 100)"
                :status="group.teamStatus === TEAM_STATUS.SUCCESS ? 'success' : group.teamStatus === TEAM_STATUS.FAILED ? 'exception' : undefined"
                :stroke-width="10"
              />
              <div v-if="group.teamStatus === TEAM_STATUS.JOINING" class="expire-info">
                <el-icon><InfoFilled /></el-icon>
                <span v-if="isExpiringSoon(group.expireTime)" class="expire-warning">
                  即将过期: {{ formatDateTime(group.expireTime) }}
                </span>
                <span v-else class="expire-normal">
                  过期时间: {{ formatDateTime(group.expireTime) }}
                </span>
              </div>
              <div v-else-if="group.teamStatus === TEAM_STATUS.SUCCESS && group.successTime" class="success-info">
                <el-icon><CircleCheck /></el-icon>
                成团时间: {{ formatDateTime(group.successTime) }}
              </div>
            </div>
          </div>

          <!-- 拼团底部 -->
          <div class="group-footer">
            <div class="member-info">
              <el-avatar-group :max="5" size="small">
                <el-avatar
                  v-for="member in group.members || []"
                  :key="member.userId"
                  :src="member.avatar"
                  :title="member.username"
                >
                  {{ member.username?.charAt(0).toUpperCase() }}
                </el-avatar>
              </el-avatar-group>
              <span class="member-count">
                {{ group.currentNum }}人已参团
                <span v-if="group.teamStatus === TEAM_STATUS.JOINING" class="remain-count">
                  (还差{{ group.requiredNum - group.currentNum }}人)
                </span>
              </span>
            </div>
            <div class="group-actions">
              <!-- 拼团中状态 -->
              <template v-if="group.teamStatus === TEAM_STATUS.JOINING">
                <el-button
                  size="small"
                  type="primary"
                  @click="handleInviteFriends(group.teamId)"
                >
                  邀请好友
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  plain
                  @click="handleQuitTeam(group.teamId)"
                >
                  退出拼团
                </el-button>
              </template>

              <!-- 已成团状态 -->
              <template v-else-if="group.teamStatus === TEAM_STATUS.SUCCESS">
                <el-button
                  size="small"
                  type="success"
                  plain
                  @click="handleViewOrder(group.orderId)"
                >
                  查看订单
                </el-button>
              </template>

              <!-- 已失败状态 -->
              <template v-else-if="group.teamStatus === TEAM_STATUS.FAILED">
                <el-button size="small" type="info" plain disabled>
                  拼团失败
                </el-button>
              </template>

              <!-- 通用按钮 -->
              <el-button size="small" @click="handleViewDetail(group.teamId)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-else description="暂无拼团记录" class="empty-state">
        <el-button type="primary" @click="$router.push('/products')">
          立即参团
        </el-button>
      </el-empty>
    </div>

    <!-- 拼团详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="拼团详情"
      width="900px"
      :close-on-click-modal="false"
    >
      <div v-if="currentTeam" class="team-detail">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h3 class="section-title">拼团信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="团号">
              {{ currentTeam.teamNo }}
            </el-descriptions-item>
            <el-descriptions-item label="拼团状态">
              <el-tag :type="TEAM_STATUS_TAG_TYPE[currentTeam.teamStatus]">
                {{ TEAM_STATUS_TEXT[currentTeam.teamStatus] }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="活动名称">
              {{ currentTeam.activityName }}
            </el-descriptions-item>
            <el-descriptions-item label="拼团价格">
              <span style="color: #f56c6c; font-weight: 600;">
                ¥{{ currentTeam.groupPrice?.toFixed(2) }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="成团人数">
              {{ currentTeam.requiredNum }}人
            </el-descriptions-item>
            <el-descriptions-item label="当前人数">
              {{ currentTeam.currentNum }}人
              <span v-if="currentTeam.teamStatus === TEAM_STATUS.JOINING" style="color: #f56c6c;">
                (还差{{ currentTeam.requiredNum - currentTeam.currentNum }}人)
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="社区">
              {{ currentTeam.communityName }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatDateTime(currentTeam.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="currentTeam.expireTime" label="过期时间">
              {{ formatDateTime(currentTeam.expireTime) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="currentTeam.successTime" label="成团时间">
              {{ formatDateTime(currentTeam.successTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 成员列表 -->
        <div class="detail-section">
          <h3 class="section-title">团队成员 ({{ currentTeam.members?.length || 0 }}人)</h3>
          <el-table :data="currentTeam.members" border stripe>
            <el-table-column label="序号" type="index" width="60" align="center" />
            <el-table-column label="用户" width="200">
              <template #default="{ row }">
                <div style="display: flex; align-items: center; gap: 12px;">
                  <el-avatar :src="row.avatar" :size="40">
                    {{ row.username?.charAt(0).toUpperCase() }}
                  </el-avatar>
                  <div>
                    <div style="font-weight: 500;">{{ row.realName || row.username }}</div>
                    <div style="font-size: 12px; color: #909399;">{{ row.username }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="角色" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.isLauncher === 1" type="warning" size="small">
                  <el-icon><Star /></el-icon>
                  团长
                </el-tag>
                <el-tag v-else type="info" size="small">成员</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="支付金额" width="120">
              <template #default="{ row }">
                ¥{{ row.payAmount?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="参团时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.joinTime) }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="MEMBER_STATUS_TAG_TYPE[row.status]" size="small">
                  {{ row.statusDesc }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分享链接 -->
        <div v-if="currentTeam.teamStatus === TEAM_STATUS.JOINING" class="detail-section">
          <h3 class="section-title">邀请好友</h3>
          <el-alert
            title="分享给好友，一起拼团更优惠！"
            type="success"
            :closable="false"
          >
            <template #default>
              <div style="margin-top: 12px;">
                <el-input
                  :model-value="currentTeam.shareLink || `${window.location.origin}/team/${currentTeam.teamId}`"
                  readonly
                >
                  <template #append>
                    <el-button @click="handleCopyLink(currentTeam.shareLink || `${window.location.origin}/team/${currentTeam.teamId}`)">
                      <el-icon><CopyDocument /></el-icon>
                      复制链接
                    </el-button>
                  </template>
                </el-input>
              </div>
            </template>
          </el-alert>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </MainLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Grid,
  Clock,
  CircleCheck,
  CircleClose,
  List,
  Picture,
  Location,
  InfoFilled,
  Star,
  CopyDocument
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { getMyTeams, quitTeam, getTeamDetail } from '@/api/groupbuy'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 拼团状态枚举
const TEAM_STATUS = {
  JOINING: 0,  // 拼团中
  SUCCESS: 1,  // 已成团
  FAILED: 2    // 已失败
}

const TEAM_STATUS_TEXT = {
  [TEAM_STATUS.JOINING]: '拼团中',
  [TEAM_STATUS.SUCCESS]: '已成团',
  [TEAM_STATUS.FAILED]: '已失败'
}

const TEAM_STATUS_TAG_TYPE = {
  [TEAM_STATUS.JOINING]: 'warning',
  [TEAM_STATUS.SUCCESS]: 'success',
  [TEAM_STATUS.FAILED]: 'info'
}

// 成员状态标签类型
const MEMBER_STATUS_TAG_TYPE = {
  0: 'warning',  // 待支付
  1: 'primary',  // 已支付
  2: 'success',  // 已成团
  3: 'info'      // 已取消
}

// 状态
const loading = ref(false)
const groups = ref([])
const activeTab = ref('all')
const stats = reactive({
  total: 0,
  joining: 0,
  success: 0,
  failed: 0
})

// 拼团详情对话框
const detailDialogVisible = ref(false)
const currentTeam = ref(null)

// 计算属性：过滤后的拼团列表
const filteredGroups = computed(() => {
  if (activeTab.value === 'all') {
    return groups.value
  }
  const status = parseInt(activeTab.value)
  return groups.value.filter(g => g.teamStatus === status)
})

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 判断是否即将过期（24小时内）
const isExpiringSoon = (expireTime) => {
  if (!expireTime) return false
  const expire = new Date(expireTime)
  const now = new Date()
  const diff = expire - now
  return diff > 0 && diff < 24 * 60 * 60 * 1000
}

// 加载拼团列表
const loadGroups = async () => {
  loading.value = true
  try {
    const response = await getMyTeams()
    
    if (response.code === 200) {
      // 确保data是数组
      if (Array.isArray(response.data)) {
        groups.value = response.data
      } else if (response.data && Array.isArray(response.data.list)) {
        // 如果是分页格式
        groups.value = response.data.list
      } else {
        groups.value = []
      }
      
      // 计算统计数据
      stats.total = groups.value.length
      stats.joining = groups.value.filter(g => g.teamStatus === TEAM_STATUS.JOINING).length
      stats.success = groups.value.filter(g => g.teamStatus === TEAM_STATUS.SUCCESS).length
      stats.failed = groups.value.filter(g => g.teamStatus === TEAM_STATUS.FAILED).length
    } else {
      ElMessage.error(response.message || '获取拼团列表失败')
    }
  } catch (error) {
    console.error('加载拼团列表失败:', error)
    ElMessage.error('加载拼团列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 标签页切换
const handleTabChange = () => {
  // 切换标签页时无需重新加载，filteredGroups会自动过滤
}

// 邀请好友
const handleInviteFriends = (teamId) => {
  handleViewDetail(teamId)
}

// 退出拼团
const handleQuitTeam = async (teamId) => {
  try {
    await ElMessageBox.confirm(
      '退出拼团后，您支付的金额将退还到账户余额。确定要退出吗？',
      '退出拼团',
      {
        confirmButtonText: '确定退出',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await quitTeam(teamId)
    
    if (response.code === 200) {
      ElMessage.success('已退出拼团，款项将退还到账户余额')
      loadGroups()
    } else {
      ElMessage.error(response.message || '退出拼团失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出拼团失败:', error)
      ElMessage.error('退出拼团失败，请稍后重试')
    }
  }
}

// 查看订单
const handleViewOrder = (orderId) => {
  if (orderId) {
    router.push('/user/orders')
  } else {
    ElMessage.warning('订单信息不可用')
  }
}

// 查看拼团详情
const handleViewDetail = async (teamId) => {
  try {
    const response = await getTeamDetail(teamId)
    
    if (response.code === 200) {
      currentTeam.value = response.data
      detailDialogVisible.value = true
    } else {
      ElMessage.error(response.message || '获取拼团详情失败')
    }
  } catch (error) {
    console.error('获取拼团详情失败:', error)
    ElMessage.error('获取拼团详情失败，请稍后重试')
  }
}

// 复制链接
const handleCopyLink = async (link) => {
  try {
    await navigator.clipboard.writeText(link)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    console.error('复制链接失败:', error)
    ElMessage.error('复制链接失败，请手动复制')
  }
}

// 页面加载
onMounted(() => {
  loadGroups()
})
</script>

<style scoped>
.groups-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

/* 状态标签 */
.status-tabs {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-badge {
  margin-left: 4px;
}

/* 加载状态 */
.loading-container {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 拼团列表 */
.groups-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.group-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.group-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* 拼团头部 */
.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.group-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.team-no {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.group-time {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

/* 拼团内容 */
.group-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 16px;
}

/* 商品信息 */
.product-section {
  display: flex;
  gap: 16px;
}

.product-image {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  flex-shrink: 0;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  color: #c0c4cc;
  font-size: 32px;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.product-name {
  font-size: 16px;
  color: #303133;
  margin-bottom: 10px;
  font-weight: 600;
}

.product-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.product-price {
  font-size: 20px;
  color: #f56c6c;
  font-weight: 700;
}

.activity-name {
  font-size: 13px;
  color: #909399;
}

.community-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}

/* 拼团进度 */
.progress-section {
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.progress-title {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.progress-text {
  font-size: 18px;
  color: #409eff;
  font-weight: 600;
}

.expire-info,
.success-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  font-size: 13px;
}

.expire-warning {
  color: #f56c6c;
  font-weight: 500;
}

.expire-normal {
  color: #909399;
}

.success-info {
  color: #67c23a;
  font-weight: 500;
}

/* 拼团底部 */
.group-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.member-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-count {
  font-size: 14px;
  color: #606266;
}

.remain-count {
  color: #f56c6c;
  font-weight: 500;
}

.group-actions {
  display: flex;
  gap: 12px;
}

/* 空状态 */
.empty-state {
  background: white;
  border-radius: 8px;
  padding: 60px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 拼团详情对话框 */
.team-detail {
  padding: 10px 0;
}

.detail-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-left: 12px;
  border-left: 4px solid #409eff;
}

/* 响应式 */
@media (max-width: 1024px) {
  .group-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .groups-container {
    padding: 16px;
  }
  
  .page-title {
    font-size: 22px;
  }
  
  .group-header,
  .group-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .group-actions {
    width: 100%;
    flex-direction: column;
  }
  
  .group-actions .el-button {
    width: 100%;
  }
  
  .product-section {
    flex-direction: column;
  }
  
  .product-image {
    width: 80px;
    height: 80px;
  }
}
</style>

