<template>
  <div class="community-application">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>社区申请审核</span>
          <el-button @click="fetchApplications">刷新</el-button>
        </div>
      </template>
      
      <!-- 状态Tabs -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待审核" name="pending">
          <el-badge :value="pendingCount" :hidden="pendingCount === 0" class="tab-badge" />
        </el-tab-pane>
        <el-tab-pane label="已通过" name="approved"></el-tab-pane>
        <el-tab-pane label="已拒绝" name="rejected"></el-tab-pane>
      </el-tabs>
      
      <!-- 申请列表表格 -->
      <el-table 
        :data="applicationList" 
        v-loading="loading"
        border
        style="width: 100%"
      >
        <el-table-column prop="applicationId" label="申请ID" width="80" />
        <el-table-column label="申请人" width="150">
          <template #default="{ row }">
            <div>{{ row.applicantName }}</div>
            <el-text type="info" size="small">{{ row.applicantPhone }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="communityName" label="社区名称" width="150" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="经纬度" width="180">
          <template #default="{ row }">
            {{ row.latitude }}, {{ row.longitude }}
          </template>
        </el-table-column>
        <el-table-column prop="applicationReason" label="申请理由" min-width="150" show-overflow-tooltip />
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
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetailDialog(row)">
              详情
            </el-button>
            <el-button 
              v-if="row.status === 0"
              size="small" 
              type="primary" 
              @click="showReviewDialog(row)"
            >
              审核
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 申请详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="社区申请详情"
      width="700px"
    >
      <el-descriptions :column="2" border v-if="currentApplication">
        <el-descriptions-item label="申请ID">
          {{ currentApplication.applicationId }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentApplication.status)">
            {{ getStatusText(currentApplication.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请人姓名">
          {{ currentApplication.applicantName }}
        </el-descriptions-item>
        <el-descriptions-item label="申请人手机">
          {{ currentApplication.applicantPhone }}
        </el-descriptions-item>
        <el-descriptions-item label="社区名称" :span="2">
          {{ currentApplication.communityName }}
        </el-descriptions-item>
        <el-descriptions-item label="详细地址" :span="2">
          {{ currentApplication.address }}
        </el-descriptions-item>
        <el-descriptions-item label="纬度">
          {{ currentApplication.latitude }}
        </el-descriptions-item>
        <el-descriptions-item label="经度">
          {{ currentApplication.longitude }}
        </el-descriptions-item>
        <el-descriptions-item label="服务半径">
          {{ currentApplication.serviceRadius }}米
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDate(currentApplication.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="社区简介" :span="2">
          {{ currentApplication.description || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="申请理由" :span="2">
          {{ currentApplication.applicationReason || '暂无' }}
        </el-descriptions-item>
        
        <!-- 审核信息（已审核的申请） -->
        <template v-if="currentApplication.status !== 0">
          <el-descriptions-item label="审核人ID">
            {{ currentApplication.reviewerId }}
          </el-descriptions-item>
          <el-descriptions-item label="审核时间">
            {{ formatDate(currentApplication.reviewedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="审核意见" :span="2">
            {{ currentApplication.reviewComment || '暂无' }}
          </el-descriptions-item>
          <el-descriptions-item 
            v-if="currentApplication.status === 1" 
            label="创建的社区ID"
            :span="2"
          >
            <el-link type="primary" @click="goToCommunity(currentApplication.createdCommunityId)">
              {{ currentApplication.createdCommunityId }}
            </el-link>
          </el-descriptions-item>
        </template>
      </el-descriptions>
    </el-dialog>
    
    <!-- 审核对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="审核社区申请"
      width="500px"
      @close="resetReviewForm"
    >
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewRules"
        label-width="100px"
      >
        <el-alert
          title="审核提示"
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <template v-if="currentApplication">
            <p>申请人：{{ currentApplication.applicantName }}</p>
            <p>社区名称：{{ currentApplication.communityName }}</p>
            <p style="color: #E6A23C; font-weight: bold; margin-top: 10px">
              ⚠️ 审核通过后将自动创建社区记录
            </p>
            <p style="color: #909399; font-size: 12px; margin-top: 5px">
              申请人需要单独申请成为该社区的团长
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
        <template v-if="reviewForm.approved">
          <el-alert
            type="warning"
            :closable="false"
            style="margin-bottom: 15px"
          >
            <template #title>
              <span style="font-weight: bold">📍 请补充社区经纬度信息</span>
            </template>
            <div style="margin-top: 5px; font-size: 13px">
              可使用<el-link type="primary" href="https://lbs.amap.com/tools/picker" target="_blank">高德地图坐标拾取工具</el-link>获取经纬度
            </div>
          </el-alert>
          
          <el-form-item label="纬度" prop="latitude">
            <el-input 
              v-model="reviewForm.latitude" 
              placeholder="例如：39.904200"
              clearable
            >
              <template #append>°N</template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="经度" prop="longitude">
            <el-input 
              v-model="reviewForm.longitude" 
              placeholder="例如：116.407400"
              clearable
            >
              <template #append>°E</template>
            </el-input>
          </el-form-item>
        </template>
        
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
        <el-button 
          type="primary" 
          @click="handleReviewSubmit" 
          :loading="submitting"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  getPendingCommunityApplications,
  getCommunityApplicationsByStatus,
  updateCommunityApplicationCoordinates,
  reviewCommunityApplication
} from '../api/leader'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

// 数据
const applicationList = ref([])
const loading = ref(false)
const activeTab = ref('pending')
const currentRequestId = ref(0) // 请求ID，用于防止竞态条件

// 对话框
const detailDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const currentApplication = ref(null)
const submitting = ref(false)

// 表单
const reviewFormRef = ref(null)
const reviewForm = ref({
  approved: true,
  reviewComment: '',
  latitude: '',
  longitude: ''
})

// 表单验证规则
const reviewRules = {
  approved: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
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
  ],
  reviewComment: [
    {
      validator: (rule, value, callback) => {
        if (reviewForm.value.approved === false && !value) {
          callback(new Error('拒绝时必须填写原因'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 计算属性
const pendingCount = computed(() => {
  return activeTab.value === 'pending' ? applicationList.value.length : 0
})

// 方法
const fetchApplications = async () => {
  loading.value = true
  
  // 生成新的请求ID
  const requestId = ++currentRequestId.value
  const currentTab = activeTab.value
  
  try {
    let res
    if (currentTab === 'pending') {
      res = await getPendingCommunityApplications()
    } else {
      const status = currentTab === 'approved' ? 1 : 2
      res = await getCommunityApplicationsByStatus(status)
    }
    
    // 只有当前请求是最新的，且标签未改变时才更新数据
    if (requestId === currentRequestId.value && currentTab === activeTab.value) {
      if (res.code === 200) {
        applicationList.value = res.data || []
      } else {
        ElMessage.error(res.message || '获取申请列表失败')
      }
    }
  } catch (error) {
    // 只有当前请求是最新的时才显示错误
    if (requestId === currentRequestId.value) {
      console.error('获取申请列表失败:', error)
      ElMessage.error('获取申请列表失败')
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
  fetchApplications()
}

const showDetailDialog = (row) => {
  currentApplication.value = row
  detailDialogVisible.value = true
}

const showReviewDialog = (row) => {
  currentApplication.value = row
  reviewForm.value = {
    approved: true,
    reviewComment: '',
    // 如果申请已有经纬度，自动填充
    latitude: row.latitude ? String(row.latitude) : '',
    longitude: row.longitude ? String(row.longitude) : ''
  }
  reviewDialogVisible.value = true
}

const handleReviewSubmit = async () => {
  if (!reviewFormRef.value) return
  
  await reviewFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const adminUserId = userStore.userInfo?.userId || 1 // 获取管理员ID
      
      // 如果审核通过，先补充经纬度信息
      if (reviewForm.value.approved && reviewForm.value.latitude && reviewForm.value.longitude) {
        const coordRes = await updateCommunityApplicationCoordinates(
          currentApplication.value.applicationId,
          parseFloat(reviewForm.value.latitude),
          parseFloat(reviewForm.value.longitude)
        )
        
        if (coordRes.code !== 200) {
          ElMessage.error(coordRes.message || '补充经纬度信息失败')
          return
        }
      }
      
      // 提交审核
      const res = await reviewCommunityApplication(
        currentApplication.value.applicationId,
        adminUserId,
        reviewForm.value.approved,
        reviewForm.value.reviewComment
      )
      
      if (res.code === 200) {
        ElMessage.success({
          message: reviewForm.value.approved 
            ? '审核通过！社区已创建' 
            : '已拒绝申请',
          duration: 3000
        })
        reviewDialogVisible.value = false
        fetchApplications()
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

const resetReviewForm = () => {
  if (reviewFormRef.value) {
    reviewFormRef.value.resetFields()
  }
  reviewForm.value = {
    approved: true,
    reviewComment: '',
    latitude: '',
    longitude: ''
  }
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'warning', // 待审核
    1: 'success', // 已通过
    2: 'danger'   // 已拒绝
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return textMap[status] || '未知'
}

const goToCommunity = (communityId) => {
  router.push({ name: 'community', query: { id: communityId } })
  detailDialogVisible.value = false
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  fetchApplications()
})
</script>

<style scoped>
.community-application {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tab-badge {
  margin-left: 8px;
}
</style>

