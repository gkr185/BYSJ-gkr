<template>
  <div class="leader-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>团长管理</span>
          <el-button @click="fetchLeaders">刷新</el-button>
        </div>
      </template>
      
      <!-- 状态Tabs -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待审核" name="pending">
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="tab-badge" />
        </el-tab-pane>
        <el-tab-pane label="正常运营" name="active"></el-tab-pane>
        <el-tab-pane label="已拒绝/已停用" name="disabled"></el-tab-pane>
      </el-tabs>
      
      <!-- 团长列表表格 -->
      <el-table 
        :data="leaderList" 
        v-loading="loading"
        border
        style="width: 100%"
      >
        <el-table-column prop="storeId" label="团点ID" width="80" />
        <el-table-column label="团长信息" width="180">
          <template #default="{ row }">
            <div>{{ row.leaderName }}</div>
            <el-text type="info" size="small">{{ row.leaderPhone }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="communityName" label="所属社区" width="150">
          <template #default="{ row }">
            <el-link type="primary" @click="viewCommunity(row.communityId)">
              {{ row.communityName }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="storeName" label="团点名称" width="150" />
        <el-table-column prop="address" label="团点地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="佣金比例" width="100">
          <template #default="{ row }">
            {{ row.commissionRate }}%
          </template>
        </el-table-column>
        <el-table-column label="累计佣金" width="120">
          <template #default="{ row }">
            ¥{{ row.totalCommission || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetailDialog(row)">详情</el-button>
            <el-button 
              v-if="row.status === 0"
              size="small" 
              type="primary" 
              @click="showReviewDialog(row)"
            >
              审核
            </el-button>
            <el-button 
              v-if="row.status === 1"
              size="small" 
              type="danger"
              @click="handleDisable(row)"
            >
              停用
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 团长详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="团长详情" width="700px">
      <el-descriptions :column="2" border v-if="currentLeader">
        <el-descriptions-item label="团点ID">{{ currentLeader.storeId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentLeader.status)">
            {{ getStatusText(currentLeader.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="团长姓名">{{ currentLeader.leaderName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ currentLeader.leaderPhone }}</el-descriptions-item>
        <el-descriptions-item label="所属社区">
          <el-link type="primary" @click="viewCommunity(currentLeader.communityId)">
            {{ currentLeader.communityName }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="团点名称">{{ currentLeader.storeName }}</el-descriptions-item>
        <el-descriptions-item label="团点地址" :span="2">{{ currentLeader.address }}</el-descriptions-item>
        <el-descriptions-item label="佣金比例">{{ currentLeader.commissionRate }}%</el-descriptions-item>
        <el-descriptions-item label="累计佣金">¥{{ currentLeader.totalCommission || 0 }}</el-descriptions-item>
        <el-descriptions-item label="团点简介" :span="2">{{ currentLeader.description || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ formatDate(currentLeader.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(currentLeader.updatedAt) }}</el-descriptions-item>
        <template v-if="currentLeader.status !== 0">
          <el-descriptions-item label="审核人ID">{{ currentLeader.reviewerId }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ formatDate(currentLeader.reviewedAt) }}</el-descriptions-item>
          <el-descriptions-item label="审核意见" :span="2">{{ currentLeader.reviewComment || '暂无' }}</el-descriptions-item>
        </template>
      </el-descriptions>
    </el-dialog>
    
    <!-- 审核对话框 -->
    <el-dialog v-model="reviewDialogVisible" title="审核团长申请" width="500px" @close="resetReviewForm">
      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="100px">
        <el-alert title="审核提示" type="info" :closable="false" style="margin-bottom: 20px">
          <template v-if="currentLeader">
            <p>申请人：{{ currentLeader.leaderName }}</p>
            <p>所属社区：{{ currentLeader.communityName }}</p>
            <p style="color: #E6A23C; font-weight: bold; margin-top: 10px">
              ⚠️ 审核通过后将自动更新用户角色为"团长"！
            </p>
          </template>
        </el-alert>
        <el-form-item label="审核结果" prop="approved">
          <el-radio-group v-model="reviewForm.approved">
            <el-radio :label="true">通过</el-radio>
            <el-radio :label="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 经纬度输入（审核通过时必填） -->
        <el-form-item label="纬度" prop="latitude" v-if="reviewForm.approved">
          <el-input-number
            v-model="reviewForm.latitude"
            :precision="6"
            :step="0.000001"
            :min="-90"
            :max="90"
            placeholder="请输入纬度"
            style="width: 100%"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            纬度范围：-90 到 90，例如：北京天安门 39.904200
          </div>
        </el-form-item>
        
        <el-form-item label="经度" prop="longitude" v-if="reviewForm.approved">
          <el-input-number
            v-model="reviewForm.longitude"
            :precision="6"
            :step="0.000001"
            :min="-180"
            :max="180"
            placeholder="请输入经度"
            style="width: 100%"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 5px;">
            经度范围：-180 到 180，例如：北京天安门 116.407400
          </div>
          <div style="color: #E6A23C; font-size: 12px; margin-top: 5px;">
            💡 提示：可以通过
            <el-link type="primary" href="https://lbs.amap.com/tools/picker" target="_blank">
              高德地图坐标拾取器
            </el-link>
            获取精确坐标
          </div>
        </el-form-item>
        
        <el-form-item label="审核意见" prop="reviewComment">
          <el-input 
            v-model="reviewForm.reviewComment" 
            type="textarea"
            :rows="4"
            :placeholder="reviewForm.approved ? '审核通过！' : '请填写拒绝原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReviewSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { getPendingLeaderApplications, getLeadersByStatus, reviewLeaderApplication, disableLeader, updateLeaderStoreCoordinates } from '../api/leader'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const leaderList = ref([])
const loading = ref(false)
const activeTab = ref('pending')
const detailDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const currentLeader = ref(null)
const submitting = ref(false)
const reviewFormRef = ref(null)
const reviewForm = ref({
  approved: true,
  reviewComment: '',
  latitude: '',
  longitude: ''
})
const currentRequestId = ref(0) // 请求ID，用于防止竞态条件
const reviewRules = {
  approved: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
  reviewComment: [
    { validator: (rule, value, callback) => {
      if (reviewForm.value.approved === false && !value) {
        callback(new Error('拒绝时必须填写原因'))
      } else { callback() }
    }, trigger: 'blur' }
  ],
  latitude: [
    {
      validator: (rule, value, callback) => {
        if (reviewForm.value.approved === true && !value) {
          callback(new Error('审核通过时必须填写纬度'))
        } else if (value && (isNaN(value) || value < -90 || value > 90)) {
          callback(new Error('纬度必须在 -90 到 90 之间'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  longitude: [
    {
      validator: (rule, value, callback) => {
        if (reviewForm.value.approved === true && !value) {
          callback(new Error('审核通过时必须填写经度'))
        } else if (value && (isNaN(value) || value < -180 || value > 180)) {
          callback(new Error('经度必须在 -180 到 180 之间'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const pendingCount = computed(() => activeTab.value === 'pending' ? leaderList.value.length : 0)

const fetchLeaders = async () => {
  loading.value = true
  
  // 生成新的请求ID
  const requestId = ++currentRequestId.value
  const currentTab = activeTab.value
  
  try {
    let res
    if (currentTab === 'pending') {
      res = await getPendingLeaderApplications()
    } else {
      const status = currentTab === 'active' ? 1 : 2
      res = await getLeadersByStatus(status)
    }
    
    // 只有当前请求是最新的，且标签未改变时才更新数据
    if (requestId === currentRequestId.value && currentTab === activeTab.value) {
      if (res.code === 200) {
        leaderList.value = res.data || []
      } else {
        ElMessage.error(res.message || '获取团长列表失败')
      }
    }
  } catch (error) {
    // 只有当前请求是最新的时才显示错误
    if (requestId === currentRequestId.value) {
      console.error('获取团长列表失败:', error)
      ElMessage.error('获取团长列表失败')
    }
  } finally {
    // 只有当前请求是最新的时才关闭loading
    if (requestId === currentRequestId.value) {
      loading.value = false
    }
  }
}

const handleTabChange = (tabName) => {
  console.log('标签切换到:', tabName)
  fetchLeaders()
}
const showDetailDialog = (row) => { currentLeader.value = row; detailDialogVisible.value = true }
const showReviewDialog = (row) => {
  currentLeader.value = row
  reviewForm.value = {
    approved: true,
    reviewComment: '',
    latitude: row.latitude || '',
    longitude: row.longitude || ''
  }
  reviewDialogVisible.value = true
}

const handleReviewSubmit = async () => {
  if (!reviewFormRef.value) return
  await reviewFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const adminUserId = userStore.userInfo?.userId || 1
      
      // 如果审核通过，先补充经纬度信息
      if (reviewForm.value.approved && reviewForm.value.latitude && reviewForm.value.longitude) {
        const coordRes = await updateLeaderStoreCoordinates(
          currentLeader.value.storeId,
          parseFloat(reviewForm.value.latitude),
          parseFloat(reviewForm.value.longitude)
        )
        if (coordRes.code !== 200) {
          ElMessage.error(coordRes.message || '补充经纬度信息失败')
          return
        }
      }
      
      // 提交审核
      const res = await reviewLeaderApplication(
        currentLeader.value.storeId,
        adminUserId,
        reviewForm.value.approved,
        reviewForm.value.reviewComment
      )
      if (res.code === 200) {
        ElMessage.success({
          message: reviewForm.value.approved ? '审核通过！用户角色已更新为团长' : '已拒绝申请',
          duration: 3000
        })
        reviewDialogVisible.value = false
        fetchLeaders()
      } else {
        ElMessage.error(res.message || '审核失败')
      }
    } catch (error) {
      console.error('审核失败:', error)
      ElMessage.error('审核失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDisable = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要停用团长"${row.leaderName}"吗？`, '确认停用', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    const res = await disableLeader(row.storeId)
    if (res.code === 200) {
      ElMessage.success('团长已停用')
      fetchLeaders()
    } else {
      ElMessage.error(res.message || '停用失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('停用失败:', error)
      ElMessage.error('停用失败')
    }
  }
}

const resetReviewForm = () => {
  if (reviewFormRef.value) reviewFormRef.value.resetFields()
  reviewForm.value = {
    approved: true,
    reviewComment: '',
    latitude: '',
    longitude: ''
  }
}

const viewCommunity = (communityId) => {
  router.push({ name: 'community', query: { id: communityId } })
  detailDialogVisible.value = false
}

const getStatusType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[status] || 'info')
const getStatusText = (status) => ({ 0: '待审核', 1: '正常运营', 2: '已拒绝/已停用' }[status] || '未知')
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

onMounted(() => fetchLeaders())
</script>

<style scoped>
.leader-manage { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.tab-badge { margin-left: 8px; }
</style>

