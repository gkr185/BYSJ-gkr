<template>
  <div class="feedback-page-wrapper">
    <div class="feedback-page">
    <h2>意见反馈</h2>
    
    <el-tabs v-model="activeTab">
      <!-- 提交反馈 -->
      <el-tab-pane label="提交反馈" name="submit">
        <el-form :model="feedbackForm" label-width="100px">
          <el-form-item label="反馈类型">
            <el-select v-model="feedbackForm.type" placeholder="请选择反馈类型">
              <el-option label="功能问题" :value="1" />
              <el-option label="商品问题" :value="2" />
              <el-option label="配送问题" :value="3" />
              <el-option label="支付问题" :value="4" />
              <el-option label="其他" :value="5" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="反馈内容">
            <el-input
              v-model="feedbackForm.content"
              type="textarea"
              rows="5"
              placeholder="请详细描述您的问题或建议"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSubmit" :loading="submitting">提交反馈</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 我的反馈 -->
      <el-tab-pane label="我的反馈" name="my">
        <el-table :data="feedbackList" border>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="类型" width="120">
            <template #default="{ row }">
              {{ getTypeText(row.type) }}
            </template>
          </el-table-column>
          <el-table-column prop="content" label="反馈内容" show-overflow-tooltip />
          <el-table-column prop="reply" label="管理员回复" show-overflow-tooltip />
          <el-table-column label="提交时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.createTime) }}
            </template>
          </el-table-column>
        </el-table>

        <el-empty v-if="feedbackList.length === 0" description="暂无反馈记录" />
      </el-tab-pane>
    </el-tabs>
  </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { submitFeedback, getMyFeedback } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('submit')
const submitting = ref(false)

const feedbackForm = ref({
  type: null,
  content: ''
})

const feedbackList = ref([])

// 提交反馈
const handleSubmit = async () => {
  if (!userStore.userInfo?.userId) return
  if (!feedbackForm.value.type) {
    ElMessage.warning('请选择反馈类型')
    return
  }
  if (!feedbackForm.value.content) {
    ElMessage.warning('请输入反馈内容')
    return
  }
  
  submitting.value = true
  try {
    await submitFeedback({
      userId: userStore.userInfo.userId,
      type: feedbackForm.value.type,
      content: feedbackForm.value.content
    })
    
    ElMessage.success('提交成功')
    feedbackForm.value = {
      type: null,
      content: ''
    }
    
    // 切换到我的反馈标签
    activeTab.value = 'my'
    await loadMyFeedback()
  } catch (error) {
    console.error('Failed to submit feedback:', error)
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

// 加载我的反馈
const loadMyFeedback = async () => {
  if (!userStore.userInfo?.userId) return
  
  try {
    const data = await getMyFeedback(userStore.userInfo.userId)
    feedbackList.value = data
  } catch (error) {
    console.error('Failed to load feedback:', error)
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = { 0: '待处理', 1: '处理中', 2: '已解决', 3: '已关闭' }
  return textMap[status] || '未知'
}

// 获取类型文本
const getTypeText = (type) => {
  const textMap = { 1: '功能问题', 2: '商品问题', 3: '配送问题', 4: '支付问题', 5: '其他' }
  return textMap[type] || '未知'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

// 监听tab切换
watch(activeTab, (newVal) => {
  if (newVal === 'my') {
    loadMyFeedback()
  }
})

onMounted(() => {
  if (activeTab.value === 'my') {
    loadMyFeedback()
  }
})
</script>

<style scoped>
.feedback-page-wrapper {
  min-height: 100vh;
  padding-top: 84px; /* 64px导航栏 + 20px间隔 */
  background-color: #f5f5f5;
}

.feedback-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 20px 20px;
}

h2 {
  margin-bottom: 20px;
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .feedback-page-wrapper {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }
}
</style>
