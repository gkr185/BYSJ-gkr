<template>
  <div class="activity-manage-wrapper">
    <div class="activity-manage-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>活动管理</h2>
        <p class="subtitle">创建和管理拼团活动</p>
      </div>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">我的活动</div>
              <div class="stat-value">{{ activities.length }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">进行中</div>
              <div class="stat-value success">{{ ongoingCount }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-item">
              <div class="stat-label">已结束</div>
              <div class="stat-value warning">{{ endedCount }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 活动列表卡片 -->
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>拼团活动</span>
            <div>
              <el-button type="primary" :icon="Plus" @click="router.push('/leader/activities/create')">
                创建活动
              </el-button>
              <el-button @click="fetchActivities">刷新</el-button>
            </div>
          </div>
        </template>

        <!-- 加载状态 -->
        <el-skeleton v-if="loading" :rows="5" animated />

        <!-- 活动列表 -->
        <div v-else-if="activities.length > 0" class="activities-list">
          <el-table :data="activities" border stripe>
            <el-table-column prop="activityId" label="活动ID" width="80" />
            <el-table-column label="商品信息" width="200">
              <template #default="{ row }">
                <div class="product-info">
                  <span class="product-name">商品ID: {{ row.productId }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="拼团价" width="120">
              <template #default="{ row }">
                <span class="price">¥{{ row.groupPrice }}</span>
              </template>
            </el-table-column>
            <el-table-column label="成团/限制" width="120">
              <template #default="{ row }">
                <div>{{ row.requiredNum }}人成团</div>
                <div v-if="row.maxNum" class="text-muted">限{{ row.maxNum }}人</div>
                <div v-else class="text-muted">无限制</div>
              </template>
            </el-table-column>
            <el-table-column label="活动时间" width="350">
              <template #default="{ row }">
                <div class="time-info">
                  <div><el-icon><Timer /></el-icon> 开始: {{ formatTime(row.startTime) }}</div>
                  <div><el-icon><Timer /></el-icon> 结束: {{ formatTime(row.endTime) }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="viewActivity(row)">
                  详情
                </el-button>
                <el-button size="small" type="primary" @click="router.push(`/leader/activities/edit/${row.activityId}`)">
                  编辑
                </el-button>
                <el-button size="small" type="danger" @click="handleDelete(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 无数据 -->
        <el-empty v-else description="暂无拼团活动">
          <el-button type="primary" @click="router.push('/leader/activities/create')">创建第一个活动</el-button>
        </el-empty>
      </el-card>

      <!-- 活动详情对话框 -->
      <el-dialog
        v-model="detailDialogVisible"
        title="活动详情"
        width="700px"
      >
        <el-descriptions :column="2" border v-if="currentActivity">
          <el-descriptions-item label="活动ID">
            {{ currentActivity.activityId }}
          </el-descriptions-item>
          <el-descriptions-item label="商品ID">
            {{ currentActivity.productId }}
          </el-descriptions-item>
          <el-descriptions-item label="拼团价">
            <span style="color: #f56c6c; font-weight: bold;">
              ¥{{ currentActivity.groupPrice }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="成团人数">
            <el-tag type="warning">{{ currentActivity.requiredNum }}人</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最大人数">
            <el-tag v-if="currentActivity.maxNum">{{ currentActivity.maxNum }}人</el-tag>
            <el-tag v-else type="info">无限制</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="活动状态">
            <el-tag :type="getStatusType(currentActivity.status)">
              {{ getStatusText(currentActivity.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开始时间" :span="2">
            {{ formatTime(currentActivity.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间" :span="2">
            {{ formatTime(currentActivity.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ currentActivity.createTime }}
          </el-descriptions-item>
        </el-descriptions>

        <template #footer>
          <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Timer } from '@element-plus/icons-vue'
import { deleteActivity, getActivities } from '@/api/groupbuy'

const router = useRouter()

// 状态
const loading = ref(false)
const detailDialogVisible = ref(false)

// 数据
const activities = ref([])
const currentActivity = ref(null)

// 计算属性
const ongoingCount = computed(() => {
  return activities.value.filter(a => a.status === 1).length
})

const endedCount = computed(() => {
  return activities.value.filter(a => a.status === 2).length
})

// 获取活动列表
const fetchActivities = async () => {
  loading.value = true
  try {
    // request拦截器已经解包了res.data，所以直接得到的就是数据数组
    const data = await getActivities()
    console.log('活动列表响应:', data)
    activities.value = data || []
    console.log('活动数量:', activities.value.length)
  } catch (error) {
    console.error('获取活动列表失败:', error)
    ElMessage.error('获取活动列表失败')
  } finally {
    loading.value = false
  }
}

// 查看活动详情
const viewActivity = (activity) => {
  currentActivity.value = activity
  detailDialogVisible.value = true
}

// 删除活动
const handleDelete = async (activity) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该活动吗？删除后不可恢复！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // request拦截器已经解包了res.data
    await deleteActivity(activity.activityId)
    ElMessage.success('删除成功')
    fetchActivities()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
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

// 初始化
onMounted(() => {
  fetchActivities()
})
</script>

<style scoped>
.activity-manage-wrapper {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 0;
}

.activity-manage-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  color: white;
  margin-bottom: 30px;
  padding: 30px 0;
}

.page-header h2 {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 10px 0;
}

.subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-item {
  text-align: center;
  padding: 10px 0;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.warning {
  color: #e6a23c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activities-list {
  margin-top: 20px;
}

.product-info {
  display: flex;
  flex-direction: column;
}

.product-name {
  font-weight: 500;
  color: #303133;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.text-muted {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.time-info {
  font-size: 13px;
  line-height: 1.8;
}

.time-info .el-icon {
  margin-right: 4px;
}
</style>
