<template>
  <div class="feedback-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>反馈管理</span>
          <el-button type="primary" @click="fetchFeedback">刷新</el-button>
        </div>
      </template>
      
      <!-- 筛选 -->
      <el-row :gutter="20" class="filter-row">
        <el-col :span="6">
          <el-select v-model="statusFilter" placeholder="状态筛选" @change="handleStatusFilter">
            <el-option label="全部状态" :value="-1" />
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已解决" :value="2" />
            <el-option label="已关闭" :value="3" />
          </el-select>
        </el-col>
        
        <el-col :span="6">
          <el-input v-model="pageSize" placeholder="每页显示数量" type="number">
            <template #prepend>每页</template>
            <template #append>条</template>
          </el-input>
        </el-col>
      </el-row>
      
      <!-- 反馈列表表格 -->
      <el-table 
        :data="feedbackList" 
        v-loading="loading"
        border
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="feedbackId" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column label="图片" width="100" align="center">
          <template #default="{ row }">
            <div v-if="row.images" class="table-images">
              <el-image
                v-for="(img, index) in parseImages(row.images).slice(0, 2)"
                :key="index"
                :src="img"
                :preview-src-list="parseImages(row.images)"
                :initial-index="index"
                fit="cover"
                class="table-img"
              />
              <span v-if="parseImages(row.images).length > 2" class="more-images">
                +{{ parseImages(row.images).length - 2 }}
              </span>
            </div>
            <span v-else class="no-image">无</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">
              {{ getTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="反馈内容" :show-overflow-tooltip="true" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusName(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="180" />
        <el-table-column prop="replyTime" label="回复时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">
              详情
            </el-button>
            <el-button 
              v-if="row.status === 0 || row.status === 1" 
              size="small" 
              type="primary"
              @click="showReplyDialog(row)"
            >
              回复
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: center"
        @current-change="handlePageChange"
      />
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="反馈详情"
      width="600px"
      @closed="handleDetailDialogClosed"
    >
      <el-descriptions :column="1" border v-if="currentFeedback">
        <el-descriptions-item label="反馈ID">
          {{ currentFeedback.feedbackId }}
        </el-descriptions-item>
        <el-descriptions-item label="用户ID">
          {{ currentFeedback.userId }}
        </el-descriptions-item>
        <el-descriptions-item label="反馈类型">
          <el-tag :type="getTypeColor(currentFeedback.type)">
            {{ getTypeName(currentFeedback.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="反馈内容">
          {{ currentFeedback.content }}
        </el-descriptions-item>
        <el-descriptions-item label="反馈图片" v-if="currentFeedback.images">
          <div class="feedback-images">
            <el-image
              v-for="(img, index) in parseImages(currentFeedback.images)"
              :key="index"
              :src="img"
              :preview-src-list="parseImages(currentFeedback.images)"
              :initial-index="index"
              fit="cover"
              class="feedback-img"
            />
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentFeedback.status)">
            {{ getStatusName(currentFeedback.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ currentFeedback.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="回复内容" v-if="currentFeedback.reply">
          {{ currentFeedback.reply }}
        </el-descriptions-item>
        <el-descriptions-item label="回复时间" v-if="currentFeedback.replyTime">
          {{ currentFeedback.replyTime }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    
    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复反馈"
      width="500px"
    >
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="反馈内容">
          <el-input 
            :value="currentFeedback?.content" 
            type="textarea" 
            :rows="3"
            disabled
          />
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input 
            v-model="replyForm.reply" 
            type="textarea" 
            :rows="4"
            placeholder="请输入回复内容"
          />
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="replyForm.status" style="width: 100%">
            <el-option label="处理中" :value="1" />
            <el-option label="已解决" :value="2" />
            <el-option label="已关闭" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReply" :loading="replyLoading">
          提交回复
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAllFeedback, getFeedbackByStatus, getFeedbackDetail, replyFeedback } from '../api/feedback'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const feedbackList = ref([])
const statusFilter = ref(-1)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const detailDialogVisible = ref(false)
const replyDialogVisible = ref(false)
const replyLoading = ref(false)
const currentFeedback = ref(null)

const replyForm = reactive({
  feedbackId: null,
  reply: '',
  status: 2  // 默认"已解决"
})

// 获取反馈列表
const fetchFeedback = async () => {
  loading.value = true
  try {
    let res
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (statusFilter.value >= 0) {
      res = await getFeedbackByStatus(statusFilter.value, params)
    } else {
      res = await getAllFeedback(params)
    }
    
    // 处理分页响应
    if (res && res.data) {
      // MyBatis PageHelper 分页格式: { pageNum, pageSize, total, pages, list: [...] }
      if (res.data.list && Array.isArray(res.data.list)) {
        feedbackList.value = res.data.list
        total.value = res.data.total || res.data.list.length
      } else if (res.data.data && Array.isArray(res.data.data)) {
        // 兼容其他分页格式 { total, data: [...] }
        feedbackList.value = res.data.data
        total.value = res.data.total || res.data.data.length
      } else if (Array.isArray(res.data)) {
        // 兼容直接返回数组的情况
        feedbackList.value = res.data
        total.value = res.data.length
      } else {
        feedbackList.value = []
        total.value = 0
      }
    } else if (Array.isArray(res)) {
      feedbackList.value = res
      total.value = res.length
    } else {
      feedbackList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取反馈列表失败:', error)
    ElMessage.error('获取反馈列表失败')
    feedbackList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 状态筛选
const handleStatusFilter = () => {
  currentPage.value = 1
  fetchFeedback()
}

// 分页变化
const handlePageChange = () => {
  fetchFeedback()
}

// 获取类型名称
const getTypeName = (type) => {
  const typeMap = {
    1: '功能问题', 2: '商品问题', 3: '配送问题', 4: '支付问题', 5: '其他'
  }
  return typeMap[type] || '其他'
}

// 获取类型颜色
const getTypeColor = (type) => {
  const colorMap = {
    1: 'warning',
    2: 'danger',
    3: 'info',
    4: 'danger',
    5: 'success',
    6: 'info'
  }
  return colorMap[type] || 'info'
}

// 获取状态名称
const getStatusName = (status) => {
  const statusMap = {
    0: '待处理',
    1: '处理中',
    2: '已解决',
    3: '已关闭'
  }
  return statusMap[status] || '未知'
}

// 获取状态标签类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'danger',
    1: 'warning',
    2: 'success',
    3: 'info'
  }
  return typeMap[status] || 'info'
}

// 解析图片URL（多张用逗号分隔）
const parseImages = (imagesStr) => {
  if (!imagesStr) return []
  return imagesStr.split(',').map(url => url.trim()).filter(url => url)
}

// 查看详情
const showDetail = async (feedback) => {
  try {
    const res = await getFeedbackDetail(feedback.feedbackId)
    currentFeedback.value = res.data || feedback
    detailDialogVisible.value = true
  } catch (error) {
    console.error('获取反馈详情失败:', error)
    // 如果获取详情失败，使用列表中的数据
    currentFeedback.value = feedback
    detailDialogVisible.value = true
  }
}

// 显示回复对话框
const showReplyDialog = (feedback) => {
  currentFeedback.value = feedback
  replyForm.feedbackId = feedback.feedbackId
  replyForm.reply = ''
  replyForm.status = 2  // 默认"已解决"
  replyDialogVisible.value = true
}

// 提交回复
const handleReply = async () => {
  if (!replyForm.reply.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  replyLoading.value = true
  try {
    await replyFeedback({
      feedbackId: replyForm.feedbackId,
      reply: replyForm.reply,
      status: replyForm.status
    })
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    fetchFeedback()
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error(error.message || '回复失败')
  } finally {
    replyLoading.value = false
  }
}

// 详情对话框关闭后清理数据
const handleDetailDialogClosed = () => {
  setTimeout(() => {
    currentFeedback.value = null
  }, 100)
}

onMounted(() => {
  fetchFeedback()
})
</script>

<style scoped>
.feedback-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-row {
  margin-bottom: 20px;
}

/* 反馈图片 */
.feedback-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feedback-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  cursor: pointer;
}

/* 表格图片样式 */
.table-images {
  display: flex;
  gap: 4px;
  align-items: center;
  justify-content: center;
}

.table-img {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  cursor: pointer;
}

.more-images {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}

.no-image {
  color: #c0c4cc;
  font-size: 12px;
}
</style>

