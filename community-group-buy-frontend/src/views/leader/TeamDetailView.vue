<template>
  <div class="team-detail-page">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
        <div class="header-content">
          <h1 class="page-title">
            <el-icon><UserFilled /></el-icon>
            拼团详情
          </h1>
          <p class="page-desc" v-if="teamDetail">团号: {{ teamDetail.teamNo }}</p>
        </div>
      </div>

      <div v-loading="loading" class="content-wrapper">
        <template v-if="teamDetail">
          <!-- 团队状态卡片 -->
          <el-card class="status-card" shadow="never">
            <div class="status-header">
              <div class="status-badge" :class="getStatusClass(teamDetail.teamStatus)">
                <el-icon v-if="teamDetail.teamStatus === 0"><Loading /></el-icon>
                <el-icon v-else-if="teamDetail.teamStatus === 1"><CircleCheck /></el-icon>
                <el-icon v-else><CircleClose /></el-icon>
                <span>{{ teamDetail.teamStatusDesc }}</span>
              </div>
              <div class="status-time">
                <el-icon><Clock /></el-icon>
                <span v-if="teamDetail.teamStatus === 0">
                  距离结束还剩: {{ getRemainTime(teamDetail.expireTime) }}
                </span>
                <span v-else-if="teamDetail.teamStatus === 1">
                  成团时间: {{ formatDateTime(teamDetail.successTime) }}
                </span>
                <span v-else>
                  失败时间: {{ formatDateTime(teamDetail.expireTime) }}
                </span>
              </div>
            </div>

            <div class="progress-section">
              <div class="progress-info">
                <span class="current">{{ teamDetail.currentNum }}</span>
                <span class="divider">/</span>
                <span class="required">{{ teamDetail.requiredNum }}</span>
                <span class="unit">人</span>
              </div>
              <el-progress 
                :percentage="getProgressPercentage(teamDetail)" 
                :status="teamDetail.teamStatus === 1 ? 'success' : teamDetail.teamStatus === 2 ? 'exception' : ''"
                :stroke-width="20"
                :show-text="false"
              />
              <div class="progress-desc">
                <span v-if="teamDetail.teamStatus === 0">
                  还差 <strong>{{ teamDetail.remainNum }}</strong> 人成团
                </span>
                <span v-else-if="teamDetail.teamStatus === 1">
                  拼团成功！
                </span>
                <span v-else>
                  拼团失败
                </span>
              </div>
            </div>
          </el-card>

          <!-- 商品信息卡片 -->
          <el-card class="product-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><ShoppingBag /></el-icon>
                <span>商品信息</span>
              </div>
            </template>
            <div class="product-content">
              <el-image 
                :src="productInfo?.coverImg || '/placeholder-product.png'" 
                fit="cover"
                class="product-image"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="product-info">
                <h3>{{ productInfo?.productName }}</h3>
                <div class="price-info">
                  <div class="price-item">
                    <span class="label">拼团价</span>
                    <span class="value group-price">¥{{ teamDetail.groupPrice }}</span>
                  </div>
                  <div class="price-item">
                    <span class="label">原价</span>
                    <span class="value original-price">¥{{ productInfo?.price }}</span>
                  </div>
                  <el-tag type="danger" effect="dark" size="large">
                    省¥{{ (productInfo?.price - teamDetail.groupPrice).toFixed(2) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 团队信息卡片 -->
          <el-card class="team-info-card" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><InfoFilled /></el-icon>
                <span>团队信息</span>
              </div>
            </template>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">团长</span>
                <span class="value">{{ teamDetail.leaderName }}</span>
              </div>
              <div class="info-item">
                <span class="label">所属社区</span>
                <span class="value">{{ teamDetail.communityName }}</span>
              </div>
              <div class="info-item">
                <span class="label">发起时间</span>
                <span class="value">{{ formatDateTime(teamDetail.createTime) }}</span>
              </div>
              <div class="info-item">
                <span class="label">活动ID</span>
                <span class="value">{{ teamDetail.activityId }}</span>
              </div>
            </div>
          </el-card>

          <!-- 成员列表卡片 -->
          <el-card class="members-card" shadow="never">
            <template #header>
              <div class="card-header">
                <div class="header-left">
                  <el-icon><User /></el-icon>
                  <span>拼团成员 ({{ teamDetail.members?.length || 0 }})</span>
                </div>
                <!-- 团长管理按钮 -->
                <div class="header-actions" v-if="isLeader && teamDetail.teamStatus === 0">
                  <el-button
                    v-if="canFinishEarly"
                    type="success"
                    size="small"
                    @click="handleFinishEarly"
                  >
                    <el-icon><CircleCheck /></el-icon>
                    提前结束
                  </el-button>
                  <el-button
                    type="danger"
                    size="small"
                    @click="handleCancelTeam"
                  >
                    <el-icon><Close /></el-icon>
                    取消拼团
                  </el-button>
                </div>
              </div>
            </template>
            
            <div class="members-list">
              <div 
                v-for="(member, index) in teamDetail.members" 
                :key="member.memberId"
                class="member-item"
              >
                <div class="member-index">
                  <el-tag v-if="member.isLauncher" type="warning" effect="dark">
                    <el-icon><Star /></el-icon>
                    团长
                  </el-tag>
                  <el-tag v-else type="info">
                    成员{{ index }}
                  </el-tag>
                </div>
                
                <div class="member-avatar">
                  <el-avatar :src="member.avatar" :size="50">
                    {{ member.realName?.charAt(0) || 'U' }}
                  </el-avatar>
                </div>

                <div class="member-info">
                  <div class="member-name">{{ member.realName || member.username }}</div>
                  <div class="member-meta">
                    <span>
                      <el-icon><Clock /></el-icon>
                      {{ formatDateTime(member.joinTime) }}
                    </span>
                  </div>
                </div>

                <div class="member-status">
                  <el-tag 
                    :type="getMemberStatusType(member.status)"
                    effect="plain"
                  >
                    {{ member.statusDesc }}
                  </el-tag>
                  <div class="member-amount">¥{{ member.payAmount }}</div>
                </div>

                <!-- 团长操作按钮 -->
                <div class="member-actions" v-if="isLeader && !member.isLauncher && teamDetail.teamStatus === 0 && member.status !== 2">
                  <el-button
                    type="danger"
                    text
                    size="small"
                    @click="handleRemoveMember(member)"
                  >
                    <el-icon><Delete /></el-icon>
                    移除
                  </el-button>
                </div>
              </div>

              <el-empty v-if="!teamDetail.members || teamDetail.members.length === 0" description="暂无成员" />
            </div>
          </el-card>

          <!-- 底部操作栏 -->
          <div class="actions-bar" v-if="showActions">
            <el-button
              v-if="canShare"
              type="primary"
              size="large"
              @click="handleShare"
            >
              <el-icon><Share /></el-icon>
              分享拼团
            </el-button>
            <el-button
              v-if="canQuit"
              type="warning"
              size="large"
              @click="handleQuit"
            >
              <el-icon><Close /></el-icon>
              退出拼团
            </el-button>
            <el-button
              v-if="canJoin"
              type="primary"
              size="large"
              @click="handleJoin"
            >
              <el-icon><Plus /></el-icon>
              立即参团
            </el-button>
          </div>
        </template>

        <el-empty v-else-if="!loading" description="未找到拼团信息" />
      </div>
    </div>

    <!-- 取消拼团对话框 -->
    <el-dialog
      v-model="cancelDialogVisible"
      title="取消拼团"
      width="500px"
    >
      <el-form :model="cancelForm" :rules="cancelRules" ref="cancelFormRef" label-width="100px">
        <el-alert
          title="警告"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          取消拼团后，系统将自动退款给所有已支付的成员，此操作不可撤销！
        </el-alert>
        <el-form-item label="取消原因" prop="reason">
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入取消原因（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmCancelTeam" :loading="cancelLoading">
          确认取消拼团
        </el-button>
      </template>
    </el-dialog>

    <!-- 移除成员对话框 -->
    <el-dialog
      v-model="removeDialogVisible"
      title="移除成员"
      width="500px"
    >
      <el-form :model="removeForm" :rules="removeRules" ref="removeFormRef" label-width="100px">
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          移除成员后，系统将自动退款给该成员
        </el-alert>
        <el-form-item label="被移除成员">
          <span>{{ currentMember?.realName || currentMember?.username }}</span>
        </el-form-item>
        <el-form-item label="移除原因" prop="reason">
          <el-input
            v-model="removeForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入移除原因（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="removeDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmRemoveMember" :loading="removeLoading">
          确认移除
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, UserFilled, Loading, CircleCheck, CircleClose, Clock,
  ShoppingBag, Picture, InfoFilled, User, Star, Delete, Share,
  Close, Plus
} from '@element-plus/icons-vue'
import { 
  getTeamDetail, 
  quitTeam,
  finishTeamEarly,
  cancelTeam,
  removeMember,
  getActivityDetail
} from '@/api/groupbuy'
import { getProductDetail } from '@/api/product'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 数据定义
const loading = ref(false)
const teamDetail = ref(null)
const productInfo = ref(null)
const cancelDialogVisible = ref(false)
const removeDialogVisible = ref(false)
const cancelLoading = ref(false)
const removeLoading = ref(false)
const currentMember = ref(null)
const cancelFormRef = ref(null)
const removeFormRef = ref(null)

const cancelForm = reactive({
  reason: ''
})

const removeForm = reactive({
  reason: ''
})

const cancelRules = {}

const removeRules = {}

// 计算属性
const isLeader = computed(() => {
  if (!teamDetail.value || !userStore.userInfo) return false
  return teamDetail.value.leaderId === userStore.userInfo.userId
})

const canFinishEarly = computed(() => {
  if (!teamDetail.value) return false
  return teamDetail.value.currentNum >= teamDetail.value.requiredNum
})

const canShare = computed(() => {
  return teamDetail.value && teamDetail.value.teamStatus === 0
})

const canQuit = computed(() => {
  if (!teamDetail.value || !userStore.userInfo) return false
  // 拼团中且当前用户在成员列表中且不是团长
  if (teamDetail.value.teamStatus !== 0) return false
  const isMember = teamDetail.value.members?.some(m => 
    m.userId === userStore.userInfo.userId && !m.isLauncher
  )
  return isMember
})

const canJoin = computed(() => {
  if (!teamDetail.value || !userStore.userInfo) return false
  // 拼团中且未满人且当前用户不在成员列表中
  if (teamDetail.value.teamStatus !== 0) return false
  if (teamDetail.value.currentNum >= teamDetail.value.requiredNum) return false
  const isMember = teamDetail.value.members?.some(m => 
    m.userId === userStore.userInfo.userId
  )
  return !isMember
})

const showActions = computed(() => {
  return canShare.value || canQuit.value || canJoin.value
})

// 方法定义
const loadTeamDetail = async () => {
  try {
    loading.value = true
    const teamId = route.params.teamId
    
    const res = await getTeamDetail(teamId)
    if (res.code === 200) {
      teamDetail.value = res.data
      
      // 通过活动ID获取活动详情，从中获取商品ID
      if (teamDetail.value.activityId) {
        await loadActivityAndProduct(teamDetail.value.activityId)
      }
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    console.error('加载拼团详情失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const loadActivityAndProduct = async (activityId) => {
  try {
    // 获取活动详情
    const activityRes = await getActivityDetail(activityId)
    if (activityRes.code === 200 && activityRes.data.productId) {
      // 获取商品详情
      await loadProductInfo(activityRes.data.productId)
    }
  } catch (error) {
    console.error('加载活动信息失败:', error)
  }
}

const loadProductInfo = async (productId) => {
  try {
    const res = await getProductDetail(productId)
    if (res.code === 200) {
      productInfo.value = res.data
    }
  } catch (error) {
    console.error('加载商品信息失败:', error)
  }
}

const getStatusClass = (status) => {
  const map = {
    0: 'status-joining',
    1: 'status-success',
    2: 'status-failed'
  }
  return map[status] || ''
}

const getMemberStatusType = (status) => {
  const map = {
    0: 'warning',  // 待支付
    1: 'success',  // 已支付
    2: 'success',  // 已成团
    3: 'info'      // 已退款
  }
  return map[status] || 'info'
}

const getProgressPercentage = (team) => {
  if (!team) return 0
  return Math.min(Math.round((team.currentNum / team.requiredNum) * 100), 100)
}

const getRemainTime = (expireTime) => {
  if (!expireTime) return '-'
  
  const now = new Date()
  const expire = new Date(expireTime)
  const diff = expire - now
  
  if (diff <= 0) return '已过期'
  
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  
  if (hours > 24) {
    const days = Math.floor(hours / 24)
    return `${days}天${hours % 24}小时`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

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

const handleBack = () => {
  router.back()
}

const handleShare = () => {
  const shareLink = teamDetail.value.shareLink || `${window.location.origin}/team/${teamDetail.value.teamId}`
  
  if (navigator.clipboard) {
    navigator.clipboard.writeText(shareLink).then(() => {
      ElMessage.success('链接已复制到剪贴板')
    }).catch(() => {
      showShareDialog(shareLink)
    })
  } else {
    showShareDialog(shareLink)
  }
}

const showShareDialog = (link) => {
  ElMessageBox.alert(
    `<div style="word-break: break-all;">${link}</div>`,
    '分享链接',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确定'
    }
  )
}

const handleQuit = () => {
  ElMessageBox.confirm(
    '确定要退出拼团吗？退出后系统将自动退款到您的余额',
    '退出拼团',
    {
      confirmButtonText: '确定退出',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await quitTeam(teamDetail.value.teamId)
      if (res.code === 200) {
        ElMessage.success('退出成功，已退款')
        loadTeamDetail()
      } else {
        ElMessage.error(res.message || '退出失败')
      }
    } catch (error) {
      console.error('退出拼团失败:', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  }).catch(() => {
    // 用户取消
  })
}

const handleJoin = () => {
  // 跳转到参团页面（可以在当前页面展开表单，或跳转到专门的参团页面）
  router.push(`/groupbuy/team/${teamDetail.value.teamId}/join`)
}

const handleFinishEarly = () => {
  ElMessageBox.confirm(
    `当前已有${teamDetail.value.currentNum}人参团，已达到起拼人数${teamDetail.value.requiredNum}人。确定要提前结束拼团吗？`,
    '提前结束拼团',
    {
      confirmButtonText: '确定结束',
      cancelButtonText: '取消',
      type: 'success'
    }
  ).then(async () => {
    try {
      const res = await finishTeamEarly(teamDetail.value.teamId)
      if (res.code === 200) {
        ElMessage.success('拼团已成功结束！')
        loadTeamDetail()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch (error) {
      console.error('提前结束失败:', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  }).catch(() => {
    // 用户取消
  })
}

const handleCancelTeam = () => {
  cancelForm.reason = ''
  cancelDialogVisible.value = true
}

const confirmCancelTeam = async () => {
  try {
    cancelLoading.value = true
    
    const res = await cancelTeam(teamDetail.value.teamId, {
      reason: cancelForm.reason || '团长取消拼团'
    })
    
    if (res.code === 200) {
      ElMessage.success('拼团已取消，已退款给所有成员')
      cancelDialogVisible.value = false
      loadTeamDetail()
    } else {
      ElMessage.error(res.message || '取消失败')
    }
  } catch (error) {
    console.error('取消拼团失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    cancelLoading.value = false
  }
}

const handleRemoveMember = (member) => {
  currentMember.value = member
  removeForm.reason = ''
  removeDialogVisible.value = true
}

const confirmRemoveMember = async () => {
  try {
    removeLoading.value = true
    
    const res = await removeMember(
      teamDetail.value.teamId, 
      currentMember.value.memberId,
      {
        reason: removeForm.reason || '团长移除成员'
      }
    )
    
    if (res.code === 200) {
      ElMessage.success('已移除成员，已退款给该成员')
      removeDialogVisible.value = false
      loadTeamDetail()
    } else {
      ElMessage.error(res.message || '移除失败')
    }
  } catch (error) {
    console.error('移除成员失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    removeLoading.value = false
  }
}

// 生命周期
onMounted(() => {
  loadTeamDetail()
})
</script>

<style scoped lang="scss">
.team-detail-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;

  .page-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px;
  }

  .page-header {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 24px;

    .header-content {
      flex: 1;

      .page-title {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 28px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px 0;

        .el-icon {
          font-size: 32px;
          color: var(--el-color-primary);
        }
      }

      .page-desc {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }

  .content-wrapper {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .status-card {
    .status-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      .status-badge {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px 24px;
        border-radius: 24px;
        font-size: 18px;
        font-weight: 600;

        &.status-joining {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
        }

        &.status-success {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          color: white;
        }

        &.status-failed {
          background: #f5f7fa;
          color: #909399;
        }
      }

      .status-time {
        display: flex;
        align-items: center;
        gap: 6px;
        color: #606266;
        font-size: 14px;
      }
    }

    .progress-section {
      .progress-info {
        text-align: center;
        margin-bottom: 16px;
        font-size: 48px;
        font-weight: 600;

        .current {
          color: var(--el-color-primary);
        }

        .divider {
          color: #dcdfe6;
          margin: 0 8px;
        }

        .required {
          color: #606266;
        }

        .unit {
          font-size: 20px;
          color: #909399;
          margin-left: 8px;
        }
      }

      .progress-desc {
        text-align: center;
        margin-top: 12px;
        font-size: 16px;
        color: #606266;

        strong {
          color: var(--el-color-primary);
          font-size: 20px;
        }
      }
    }
  }

  .product-card {
    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
    }

    .product-content {
      display: flex;
      gap: 20px;

      .product-image {
        width: 150px;
        height: 150px;
        border-radius: 8px;
        flex-shrink: 0;

        .image-error {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 100%;
          height: 100%;
          background: #f5f7fa;
          color: #c0c4cc;
          font-size: 48px;
        }
      }

      .product-info {
        flex: 1;

        h3 {
          font-size: 20px;
          color: #303133;
          margin: 0 0 16px 0;
        }

        .price-info {
          display: flex;
          align-items: center;
          gap: 24px;

          .price-item {
            display: flex;
            flex-direction: column;
            gap: 4px;

            .label {
              font-size: 13px;
              color: #909399;
            }

            .value {
              font-size: 24px;
              font-weight: 600;

              &.group-price {
                color: #f56c6c;
              }

              &.original-price {
                color: #909399;
                text-decoration: line-through;
                font-size: 18px;
              }
            }
          }
        }
      }
    }
  }

  .team-info-card {
    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 20px;

      .info-item {
        display: flex;
        flex-direction: column;
        gap: 8px;
        padding: 16px;
        background: #f5f7fa;
        border-radius: 8px;

        .label {
          font-size: 13px;
          color: #909399;
        }

        .value {
          font-size: 15px;
          color: #303133;
          font-weight: 500;
        }
      }
    }
  }

  .members-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .header-left {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
      }

      .header-actions {
        display: flex;
        gap: 8px;
      }
    }

    .members-list {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .member-item {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 16px;
        background: #f5f7fa;
        border-radius: 8px;
        transition: all 0.3s ease;

        &:hover {
          background: #e8eaf0;
        }

        .member-index {
          flex-shrink: 0;
        }

        .member-avatar {
          flex-shrink: 0;
        }

        .member-info {
          flex: 1;

          .member-name {
            font-size: 16px;
            font-weight: 500;
            color: #303133;
            margin-bottom: 4px;
          }

          .member-meta {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 13px;
            color: #909399;
          }
        }

        .member-status {
          display: flex;
          flex-direction: column;
          align-items: flex-end;
          gap: 8px;
          flex-shrink: 0;

          .member-amount {
            font-size: 16px;
            font-weight: 600;
            color: #f56c6c;
          }
        }

        .member-actions {
          flex-shrink: 0;
        }
      }
    }
  }

  .actions-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 16px;
    padding: 16px 24px;
    background: white;
    border-top: 1px solid #e4e7ed;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
    z-index: 100;
  }
}

@media (max-width: 768px) {
  .team-detail-page {
    .page-container {
      padding: 16px;
    }

    .product-card .product-content {
      flex-direction: column;

      .product-image {
        width: 100%;
        height: 200px;
      }
    }

    .team-info-card .info-grid {
      grid-template-columns: 1fr;
    }

    .members-card .members-list .member-item {
      flex-wrap: wrap;
    }
  }
}
</style>

