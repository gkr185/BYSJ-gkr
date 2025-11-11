<template>
  <MainLayout>
    <div class="feedback-container">
      <div class="page-header">
        <el-button :icon="ArrowLeft" @click="$router.back()">返回</el-button>
        <h2 class="page-title">
          <el-icon><Message /></el-icon>
          意见反馈
        </h2>
        <el-button type="primary" :icon="Plus" @click="showSubmitDialog = true">
          提交反馈
        </el-button>
      </div>

      <!-- 反馈列表 -->
      <div v-loading="loading" class="feedback-list">
        <el-empty v-if="!loading && feedbackList.length === 0" description="暂无反馈记录">
          <el-button type="primary" @click="showSubmitDialog = true">提交第一个反馈</el-button>
        </el-empty>

        <div
          v-for="feedback in feedbackList"
          :key="feedback.feedbackId"
          class="feedback-card"
        >
          <div class="feedback-header">
            <div class="feedback-meta">
              <el-tag :type="getStatusType(feedback.status)" effect="dark">
                {{ getStatusText(feedback.status) }}
              </el-tag>
              <span class="feedback-time">{{ formatTime(feedback.createTime) }}</span>
            </div>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, feedback)">
              <el-button :icon="More" circle text />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="view" :icon="View">
                    查看详情
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="feedback.status === 0"
                    command="delete"
                    :icon="Delete"
                  >
                    删除反馈
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <div class="feedback-content">
            <div class="feedback-type-tag">
              <el-tag size="small">{{ feedback.typeName || getTypeText(feedback.type) }}</el-tag>
            </div>
            <div class="feedback-desc">{{ feedback.content }}</div>
            
            <!-- 图片展示 -->
            <div v-if="feedback.images" class="feedback-images">
              <el-image
                v-for="(img, idx) in parseImages(feedback.images)"
                :key="idx"
                :src="img"
                :preview-src-list="parseImages(feedback.images)"
                fit="cover"
                class="feedback-img"
              />
            </div>
            
            <!-- 已回复内容 -->
            <div v-if="feedback.reply" class="feedback-reply">
              <div class="reply-header">
                <el-icon><Service /></el-icon>
                <span>管理员回复</span>
              </div>
              <div class="reply-content">{{ feedback.reply }}</div>
              <div v-if="feedback.replyTime" class="reply-time">
                {{ formatTime(feedback.replyTime) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination">
        <el-pagination
          :current-page="page"
          :page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>

      <!-- 提交反馈对话框 -->
      <el-dialog
        v-model="showSubmitDialog"
        title="提交反馈"
        width="600px"
        :close-on-click-modal="false"
      >
        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="80px"
        >
          <el-form-item label="反馈类型" prop="type">
            <el-radio-group v-model="formData.type">
              <el-radio :label="1">功能问题</el-radio>
              <el-radio :label="2">商品问题</el-radio>
              <el-radio :label="3">配送问题</el-radio>
              <el-radio :label="4">支付问题</el-radio>
              <el-radio :label="5">其他</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="详细内容" prop="content">
            <el-input
              v-model="formData.content"
              type="textarea"
              :rows="8"
              placeholder="请详细描述您遇到的问题或建议...&#10;例如：无法正常下单，系统一直提示错误"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="问题截图">
            <el-upload
              :file-list="fileList"
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :limit="3"
              accept="image/*"
              :on-preview="handlePictureCardPreview"
              :on-remove="handleRemove"
              :on-change="handleFileChange"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">最多上传3张图片，支持jpg、png格式</div>
          </el-form-item>

          <el-alert
            title="温馨提示"
            type="info"
            :closable="false"
            show-icon
          >
            <p>1. 请详细描述您遇到的问题或建议</p>
            <p>2. 我们会尽快处理您的反馈</p>
            <p>3. 处理完成后会在此页面显示回复</p>
          </el-alert>
        </el-form>

        <template #footer>
          <el-button @click="showSubmitDialog = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            提交反馈
          </el-button>
        </template>
      </el-dialog>

      <!-- 查看详情对话框 -->
      <el-dialog
        v-model="showDetailDialog"
        title="反馈详情"
        width="600px"
      >
        <div v-if="currentFeedback" class="detail-content">
          <div class="detail-item">
            <div class="detail-label">处理状态</div>
            <div class="detail-value">
              <el-tag :type="getStatusType(currentFeedback.status)">
                {{ currentFeedback.statusName || getStatusText(currentFeedback.status) }}
              </el-tag>
            </div>
          </div>

          <div class="detail-item">
            <div class="detail-label">提交时间</div>
            <div class="detail-value">{{ formatTime(currentFeedback.createTime) }}</div>
          </div>

          <div class="detail-item">
            <div class="detail-label">反馈类型</div>
            <div class="detail-value">
              <el-tag>{{ currentFeedback.typeName || getTypeText(currentFeedback.type) }}</el-tag>
            </div>
          </div>

          <div class="detail-item">
            <div class="detail-label">详细内容</div>
            <div class="detail-value">{{ currentFeedback.content }}</div>
          </div>

          <div v-if="currentFeedback.images" class="detail-item">
            <div class="detail-label">问题截图</div>
            <div class="detail-value">
              <el-image
                v-for="(img, idx) in parseImages(currentFeedback.images)"
                :key="idx"
                :src="img"
                :preview-src-list="parseImages(currentFeedback.images)"
                fit="cover"
                style="width: 100px; height: 100px; margin-right: 8px; border-radius: 8px;"
              />
            </div>
          </div>

          <div v-if="currentFeedback.reply" class="detail-item">
            <div class="detail-label">管理员回复</div>
            <div class="detail-value reply-box">
              <div class="reply-content">{{ currentFeedback.reply }}</div>
              <div class="reply-time">{{ formatTime(currentFeedback.replyTime) }}</div>
            </div>
          </div>
        </div>

        <template #footer>
          <el-button type="primary" @click="showDetailDialog = false">关闭</el-button>
        </template>
      </el-dialog>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Message,
  Plus,
  More,
  View,
  Delete,
  Service
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { submitFeedback, getMyFeedbackPage, deleteFeedback, uploadFeedbackImage } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const submitLoading = ref(false)
const showSubmitDialog = ref(false)
const showDetailDialog = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const feedbackList = ref([])
const currentFeedback = ref(null)

// 表单数据
const formData = reactive({
  type: 1,
  content: '',
  images: '' // 图片URL，多张用逗号分隔
})

// 文件列表（用于上传组件）
const fileList = ref([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)

// 验证规则
const rules = {
  type: [
    { required: true, message: '请选择反馈类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入详细内容', trigger: 'blur' },
    { min: 10, message: '请至少输入10个字符', trigger: 'blur' }
  ]
}

// 加载反馈列表
const loadFeedbackList = async () => {
  // 检查用户是否登录
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    return
  }

  loading.value = true
  try {
    const res = await getMyFeedbackPage(userStore.userInfo.userId, {
      page: page.value,
      size: size.value
    })
    
    console.log('📝 获取反馈列表响应:', res)
    
    if (res.code === 200) {
      // 后端返回格式：{ pageNum, pageSize, total, pages, list }
      feedbackList.value = res.data?.list || []
      total.value = res.data?.total || 0
      
      console.log('✅ 反馈列表加载成功:', {
        total: total.value,
        currentPage: feedbackList.value.length
      })
    } else {
      ElMessage.error(res.message || '加载反馈列表失败')
      feedbackList.value = []
      total.value = 0
    }
  } catch (error) {
    // 如果API还未实现或报错，使用空数据
    console.error('❌ 加载反馈列表失败:', error)
    feedbackList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: ''
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '待处理',
    1: '处理中',
    2: '已解决',
    3: '已关闭'
  }
  return textMap[status] || '未知'
}

// 获取类型文本
const getTypeText = (type) => {
  const typeMap = {
    1: '功能问题',
    2: '商品问题',
    3: '配送问题',
    4: '支付问题',
    5: '其他'
  }
  return typeMap[type] || '未知'
}

// 解析图片URL（多张用逗号分隔）
const parseImages = (imagesStr) => {
  if (!imagesStr) return []
  return imagesStr.split(',').map(url => url.trim()).filter(url => url)
}

// 图片预览
const handlePictureCardPreview = (file) => {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}

// 文件变化
const handleFileChange = (file, files) => {
  fileList.value = files
}

// 移除图片
const handleRemove = (file, files) => {
  fileList.value = files
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 处理命令
const handleCommand = (command, feedback) => {
  if (command === 'view') {
    currentFeedback.value = feedback
    showDetailDialog.value = true
  } else if (command === 'delete') {
    handleDelete(feedback)
  }
}

// 删除反馈
const handleDelete = (feedback) => {
  const contentPreview = feedback.content.length > 20 
    ? feedback.content.substring(0, 20) + '...' 
    : feedback.content
    
  ElMessageBox.confirm(
    `确定要删除反馈「${contentPreview}」吗？`,
    '删除反馈',
    {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    try {
      const res = await deleteFeedback(feedback.feedbackId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        await loadFeedbackList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
      console.error('删除反馈失败:', error)
    }
  }).catch(() => {})
}

// 重置表单
const resetForm = () => {
  formData.type = 1
  formData.content = ''
  formData.images = ''
  fileList.value = []
  formRef.value?.clearValidate()
}

// 分页处理
const handleSizeChange = (newSize) => {
  size.value = newSize
  loadFeedbackList()
}

const handlePageChange = (newPage) => {
  page.value = newPage
  loadFeedbackList()
}

// 提交反馈
const handleSubmit = async () => {
  // 检查用户是否登录
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    ElMessage.warning('请先登录')
    showSubmitDialog.value = false
    router.push('/login')
    return
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      // 处理图片上传
      let imagesStr = ''
      if (fileList.value.length > 0) {
        const uploadPromises = fileList.value.map(async (fileItem) => {
          try {
            // 上传图片到服务器
            const res = await uploadFeedbackImage(fileItem.raw)
            if (res.code === 200) {
              return res.data // 返回图片URL
            } else {
              throw new Error(res.message || '图片上传失败')
            }
          } catch (error) {
            console.error('上传图片失败:', error)
            throw error
          }
        })
        
        // 等待所有图片上传完成
        const imageUrls = await Promise.all(uploadPromises)
        imagesStr = imageUrls.join(',')
      }

      const res = await submitFeedback({
        userId: userStore.userInfo.userId,
        type: formData.type,
        content: formData.content,
        images: imagesStr
      })

      if (res.code === 200) {
        ElMessage.success('提交成功，我们会尽快处理')
        showSubmitDialog.value = false
        resetForm()
        await loadFeedbackList()
      } else {
        ElMessage.error(res.message || '提交失败')
      }
    } catch (error) {
      ElMessage.error(error.message || '提交失败，请稍后重试')
      console.error('提交反馈失败:', error)
    } finally {
      submitLoading.value = false
    }
  })
}

onMounted(() => {
  // 检查用户是否登录
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loadFeedbackList()
})
</script>

<style scoped>
.feedback-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.page-header :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

/* 反馈列表 */
.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 300px;
  margin-bottom: 24px;
}

.feedback-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.feedback-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

.feedback-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.feedback-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.feedback-time {
  font-size: 13px;
  color: #999;
}

.feedback-content {
  margin-top: 12px;
}

.feedback-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
  margin-bottom: 12px;
}

.feedback-type-tag {
  margin-bottom: 12px;
}

.feedback-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 16px;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 图片展示 */
.feedback-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.feedback-img {
  width: 100px;
  height: 100px;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.feedback-img:hover {
  transform: scale(1.05);
}

/* 回复部分 */
.feedback-reply {
  margin-top: 20px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-left: 3px solid #667eea;
  border-radius: 8px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #667eea;
  margin-bottom: 12px;
}

.reply-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 8px;
}

.reply-time {
  font-size: 12px;
  color: #999;
  text-align: right;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

:deep(.el-pagination) {
  gap: 8px;
}

:deep(.el-pagination button),
:deep(.el-pager li) {
  border-radius: 8px;
}

:deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 16px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

/* 表单样式 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

:deep(.el-textarea__inner) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

:deep(.el-radio) {
  margin-right: 24px;
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
}

:deep(.el-alert) {
  border-radius: 10px;
  margin-top: 16px;
}

:deep(.el-alert__content p) {
  margin: 4px 0;
  font-size: 13px;
}

/* 上传组件 */
:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  border-radius: 8px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
  border-radius: 8px;
}

.upload-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

/* 详情对话框 */
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-label {
  font-size: 13px;
  font-weight: 600;
  color: #999;
}

.detail-value {
  font-size: 15px;
  color: #333;
  line-height: 1.6;
}

.reply-box {
  padding: 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  border-left: 3px solid #667eea;
  border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .feedback-container {
    padding: 16px;
  }

  .page-header {
    flex-wrap: wrap;
  }

  .feedback-card {
    padding: 16px;
  }

  .feedback-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>

