<template>
  <div class="groupbuy-manage">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">活动总数</div>
            <div class="stat-value">{{ statistics.totalActivities || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">进行中</div>
            <div class="stat-value success">{{ statistics.ongoingActivities || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">已结束</div>
            <div class="stat-value warning">{{ statistics.endedActivities || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 活动列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>拼团活动管理</span>
          <div>
            <el-button type="primary" @click="showCreateDialog">
              <el-icon><Plus /></el-icon>
              创建活动
            </el-button>
            <el-button @click="fetchActivities">刷新</el-button>
          </div>
        </div>
      </template>
      
      <!-- 活动表格 -->
      <el-table 
        :data="activityList" 
        v-loading="loading"
        border
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="activityId" label="活动ID" width="80" />
        <el-table-column prop="productId" label="商品ID" width="100" />
        <el-table-column label="拼团价" width="120">
          <template #default="{ row }">
            <span style="color: #f56c6c; font-weight: bold;">¥{{ row.groupPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成团人数" width="100">
          <template #default="{ row }">
            <el-tag type="warning">{{ row.requiredNum }}人</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="人数限制" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.maxNum">{{ row.maxNum }}人</el-tag>
            <el-tag v-else type="info">无限制</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="活动时间" width="350">
          <template #default="{ row }">
            <div>开始: {{ formatTime(row.startTime) }}</div>
            <div>结束: {{ formatTime(row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showActivityDetail(row)">
              详情
            </el-button>
            <el-button size="small" @click="showTeamList(row)">
              团列表
            </el-button>
            <el-button size="small" type="primary" @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 创建/编辑活动对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="activityForm" :rules="formRules" ref="activityFormRef" label-width="120px">
        <el-form-item label="商品ID" prop="productId">
          <el-input-number 
            v-model="activityForm.productId" 
            :min="1" 
            style="width: 100%;"
            placeholder="请输入商品ID"
          />
          <div class="form-tip">请输入有效的商品ID</div>
        </el-form-item>
        
        <el-form-item label="拼团价" prop="groupPrice">
          <el-input-number 
            v-model="activityForm.groupPrice" 
            :precision="2" 
            :step="0.1" 
            :min="0" 
            style="width: 100%;"
            placeholder="请输入拼团价"
          />
          <div class="form-tip">拼团价必须小于商品原价</div>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="成团人数" prop="requiredNum">
              <el-input-number 
                v-model="activityForm.requiredNum" 
                :min="2" 
                :max="10" 
                style="width: 100%;"
              />
              <div class="form-tip">2-10人</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大人数">
              <el-input-number 
                v-model="activityForm.maxNum" 
                :min="0" 
                style="width: 100%;"
                placeholder="不填则无限制"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="活动开始时间" prop="startTime">
          <el-date-picker
            v-model="activityForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%;"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="活动结束时间" prop="endTime">
          <el-date-picker
            v-model="activityForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%;"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="活动状态" prop="status" v-if="isEdit">
          <el-radio-group v-model="activityForm.status">
            <el-radio :value="0">未开始</el-radio>
            <el-radio :value="1">进行中</el-radio>
            <el-radio :value="2">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 活动详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="活动详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="活动ID">{{ detailActivity.activityId }}</el-descriptions-item>
        <el-descriptions-item label="商品ID">{{ detailActivity.productId }}</el-descriptions-item>
        <el-descriptions-item label="拼团价">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ detailActivity.groupPrice }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="成团人数">
          <el-tag type="warning">{{ detailActivity.requiredNum }}人</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最大人数">
          <el-tag v-if="detailActivity.maxNum">{{ detailActivity.maxNum }}人</el-tag>
          <el-tag v-else type="info">无限制</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="活动状态">
          <el-tag :type="getStatusType(detailActivity.status)">
            {{ getStatusText(detailActivity.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间" :span="2">
          {{ formatTime(detailActivity.startTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间" :span="2">
          {{ formatTime(detailActivity.endTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ detailActivity.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="二维码URL" :span="2" v-if="detailActivity.qrcodeUrl">
          {{ detailActivity.qrcodeUrl }}
        </el-descriptions-item>
        <el-descriptions-item label="分享链接" :span="2" v-if="detailActivity.link">
          {{ detailActivity.link }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    
    <!-- 团列表对话框 -->
    <el-dialog
      v-model="teamListDialogVisible"
      title="团列表"
      width="900px"
    >
      <div v-if="currentActivity.activityId">
        <div style="margin-bottom: 15px;">
          <el-tag type="primary">活动ID: {{ currentActivity.activityId }}</el-tag>
          <el-tag type="success" style="margin-left: 10px;">商品ID: {{ currentActivity.productId }}</el-tag>
          <el-tag type="warning" style="margin-left: 10px;">拼团价: ¥{{ currentActivity.groupPrice }}</el-tag>
        </div>
        
        <el-table 
          :data="teamList" 
          v-loading="teamLoading"
          border
          style="width: 100%"
        >
          <el-table-column prop="teamId" label="团ID" width="80" />
          <el-table-column prop="teamNo" label="团号" width="150" />
          <el-table-column label="团长" width="100">
            <template #default="{ row }">
              {{ row.leaderName || '未知' }}
            </template>
          </el-table-column>
          <el-table-column label="社区" width="120">
            <template #default="{ row }">
              {{ row.communityName || '未指定' }}
            </template>
          </el-table-column>
          <el-table-column label="进度" width="150">
            <template #default="{ row }">
              <el-progress 
                :percentage="Math.floor((row.currentNum / row.requiredNum) * 100)" 
                :status="row.teamStatus === 1 ? 'success' : ''"
              />
              <div style="text-align: center; font-size: 12px; margin-top: 4px;">
                {{ row.currentNum }}/{{ row.requiredNum }}人
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getTeamStatusType(row.teamStatus)">
                {{ row.teamStatusDesc }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="过期时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.expireTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" @click="showTeamDetail(row.teamId)">
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
    
    <!-- 团详情对话框 -->
    <el-dialog
      v-model="teamDetailDialogVisible"
      title="团详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="团ID">{{ teamDetail.teamId }}</el-descriptions-item>
        <el-descriptions-item label="团号">{{ teamDetail.teamNo }}</el-descriptions-item>
        <el-descriptions-item label="活动ID">{{ teamDetail.activityId }}</el-descriptions-item>
        <el-descriptions-item label="活动名称">{{ teamDetail.activityName }}</el-descriptions-item>
        <el-descriptions-item label="团长">{{ teamDetail.leaderName }}</el-descriptions-item>
        <el-descriptions-item label="社区">{{ teamDetail.communityName || '未指定' }}</el-descriptions-item>
        <el-descriptions-item label="拼团价">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ teamDetail.groupPrice }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="团状态">
          <el-tag :type="getTeamStatusType(teamDetail.teamStatus)">
            {{ teamDetail.teamStatusDesc }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前/需要人数">
          {{ teamDetail.currentNum }}/{{ teamDetail.requiredNum }}人
        </el-descriptions-item>
        <el-descriptions-item label="还差人数">
          <el-tag type="warning">{{ teamDetail.remainNum }}人</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatTime(teamDetail.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="过期时间" :span="2">
          {{ formatTime(teamDetail.expireTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="成团时间" :span="2" v-if="teamDetail.successTime">
          {{ formatTime(teamDetail.successTime) }}
        </el-descriptions-item>
      </el-descriptions>
      
      <div style="margin-top: 20px;">
        <h4>成员列表</h4>
        <el-table :data="teamDetail.members || []" border>
          <el-table-column prop="userId" label="用户ID" width="80" />
          <el-table-column prop="realName" label="姓名" width="120" />
          <el-table-column prop="username" label="用户名" width="150" />
          <el-table-column label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isLauncher === 1 ? 'danger' : 'info'">
                {{ row.isLauncher === 1 ? '发起人' : '普通成员' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="支付金额" width="120">
            <template #default="{ row }">
              ¥{{ row.payAmount }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              {{ row.statusDesc }}
            </template>
          </el-table-column>
          <el-table-column label="参团时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.joinTime) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getActivityList,
  getActivityDetail,
  createActivity,
  updateActivity,
  deleteActivity,
  getActivityTeams,
  getTeamDetail
} from '../api/groupbuy'

// 统计数据
const statistics = computed(() => {
  const total = activityList.value.length
  const ongoing = activityList.value.filter(a => a.status === 1).length
  const ended = activityList.value.filter(a => a.status === 2).length
  
  return {
    totalActivities: total,
    ongoingActivities: ongoing,
    endedActivities: ended
  }
})

// 活动列表
const activityList = ref([])
const loading = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('创建活动')
const isEdit = ref(false)
const submitLoading = ref(false)

// 活动表单
const activityFormRef = ref(null)
const activityForm = reactive({
  activityId: null,
  productId: null,
  groupPrice: 0,
  requiredNum: 3,
  maxNum: null,
  startTime: '',
  endTime: '',
  status: 1
})

// 表单校验规则
const formRules = {
  productId: [
    { required: true, message: '请输入商品ID', trigger: 'blur' }
  ],
  groupPrice: [
    { required: true, message: '请输入拼团价', trigger: 'blur' }
  ],
  requiredNum: [
    { required: true, message: '请输入成团人数', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
}

// 活动详情对话框
const detailDialogVisible = ref(false)
const detailActivity = ref({})

// 团列表对话框
const teamListDialogVisible = ref(false)
const currentActivity = ref({})
const teamList = ref([])
const teamLoading = ref(false)

// 团详情对话框
const teamDetailDialogVisible = ref(false)
const teamDetail = ref({})

// 加载活动列表
const fetchActivities = async () => {
  loading.value = true
  try {
    const res = await getActivityList()
    if (res.code === 200) {
      activityList.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取活动列表失败')
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '创建活动'
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑活动'
  Object.assign(activityForm, row)
  dialogVisible.value = true
}

// 显示活动详情
const showActivityDetail = async (row) => {
  try {
    const res = await getActivityDetail(row.activityId)
    if (res.code === 200) {
      detailActivity.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取活动详情失败')
  }
}

// 显示团列表
const showTeamList = async (row) => {
  currentActivity.value = row
  teamListDialogVisible.value = true
  
  teamLoading.value = true
  try {
    const res = await getActivityTeams(row.activityId)
    if (res.code === 200) {
      teamList.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取团列表失败')
  } finally {
    teamLoading.value = false
  }
}

// 显示团详情
const showTeamDetail = async (teamId) => {
  try {
    const res = await getTeamDetail(teamId)
    if (res.code === 200) {
      teamDetail.value = res.data
      teamDetailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取团详情失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  await activityFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 校验时间
    if (new Date(activityForm.startTime) >= new Date(activityForm.endTime)) {
      ElMessage.error('结束时间必须晚于开始时间')
      return
    }
    
    submitLoading.value = true
    try {
      const data = {
        productId: activityForm.productId,
        groupPrice: activityForm.groupPrice,
        requiredNum: activityForm.requiredNum,
        maxNum: activityForm.maxNum || null,
        startTime: activityForm.startTime,
        endTime: activityForm.endTime
      }
      
      if (isEdit.value) {
        data.status = activityForm.status
      }
      
      let res
      if (isEdit.value) {
        res = await updateActivity(activityForm.activityId, data)
      } else {
        res = await createActivity(data)
      }
      
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        fetchActivities()
      }
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  activityFormRef.value?.resetFields()
  Object.assign(activityForm, {
    activityId: null,
    productId: null,
    groupPrice: 0,
    requiredNum: 3,
    maxNum: null,
    startTime: '',
    endTime: '',
    status: 1
  })
}

// 删除活动
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该活动吗？删除后不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    const res = await deleteActivity(row.activityId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchActivities()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return textMap[status] || '未知'
}

// 获取团状态类型
const getTeamStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || 'info'
}

// 初始化
onMounted(() => {
  fetchActivities()
})
</script>

<style scoped>
.groupbuy-manage {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.danger {
  color: #f56c6c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>

