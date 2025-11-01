<template>
  <div class="store-settings-wrapper">
    <div class="store-settings-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>团点设置</h2>
        <p class="subtitle">管理您的团点信息</p>
      </div>

      <!-- 加载状态 -->
      <el-skeleton v-if="loading" :rows="8" animated />

      <!-- 团点信息 -->
      <div v-else-if="storeInfo">
        <!-- 基本信息卡片 -->
        <el-card shadow="hover" class="info-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Shop /></el-icon> 基本信息</span>
              <el-button 
                type="primary" 
                size="small"
                :disabled="editing"
                @click="handleEdit"
              >
                编辑信息
              </el-button>
            </div>
          </template>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="120px"
            :disabled="!editing"
          >
            <el-form-item label="团点名称" prop="storeName">
              <el-input v-model="form.storeName" placeholder="请输入团点名称" />
            </el-form-item>

            <el-form-item label="所在地区" required>
              <el-row :gutter="10">
                <el-col :span="8">
                  <el-form-item prop="province">
                    <el-input v-model="form.province" placeholder="省份" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item prop="city">
                    <el-input v-model="form.city" placeholder="城市" />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item prop="district">
                    <el-input v-model="form.district" placeholder="区/县" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form-item>

            <el-form-item label="详细地址" prop="address">
              <el-input 
                v-model="form.address" 
                type="textarea" 
                :rows="2"
                placeholder="请输入详细地址"
              />
            </el-form-item>

            <el-form-item label="团点简介" prop="description">
              <el-input 
                v-model="form.description" 
                type="textarea" 
                :rows="3"
                placeholder="请输入团点简介"
              />
            </el-form-item>

            <el-form-item v-if="editing">
              <el-button type="primary" @click="handleSubmit" :loading="submitting">
                保存修改
              </el-button>
              <el-button @click="handleCancel">
                取消
              </el-button>
            </el-form-item>
          </el-form>

          <el-divider />

          <!-- 只读信息 -->
          <el-descriptions :column="2" border>
            <el-descriptions-item label="归属社区">
              {{ storeInfo.communityName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="佣金比例">
              {{ storeInfo.commissionRate || 5 }}%
            </el-descriptions-item>
            <el-descriptions-item label="审核状态">
              <el-tag :type="getAuditStatusType(storeInfo.auditStatus)">
                {{ getAuditStatusText(storeInfo.auditStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="审核时间">
              {{ storeInfo.auditTime ? formatDateTime(storeInfo.auditTime) : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">
              {{ formatDateTime(storeInfo.createTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 温馨提示 -->
        <el-card shadow="hover" class="notice-card">
          <template #header>
            <div class="card-header">
              <el-icon><InfoFilled /></el-icon>
              <span>温馨提示</span>
            </div>
          </template>
          <ul class="notice-list">
            <li>⚠️ 修改团点信息后需要重新提交审核</li>
            <li>📌 佣金比例由系统统一设置，暂不支持单独修改</li>
            <li>🏘️ 归属社区信息不可修改，如需更换请联系管理员</li>
            <li>📞 如有任何问题，请联系客服：400-XXX-XXXX</li>
          </ul>
        </el-card>
      </div>

      <!-- 无数据 -->
      <el-empty v-else description="未找到团点信息">
        <el-button type="primary" @click="router.push('/leader/apply')">
          去申请团长
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Shop, InfoFilled } from '@element-plus/icons-vue'
import { getMyLeaderInfo, updateLeaderStore } from '@/api/leader'

const router = useRouter()
const userStore = useUserStore()

// 数据状态
const loading = ref(false)
const editing = ref(false)
const submitting = ref(false)
const storeInfo = ref(null)
const formRef = ref(null)

// 表单数据
const form = reactive({
  storeName: '',
  province: '',
  city: '',
  district: '',
  address: '',
  description: ''
})

// 验证规则
const rules = {
  storeName: [
    { required: true, message: '请输入团点名称', trigger: 'blur' },
    { min: 2, max: 50, message: '团点名称长度为 2-50 个字符', trigger: 'blur' }
  ],
  province: [
    { required: true, message: '请输入省份', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入城市', trigger: 'blur' }
  ],
  district: [
    { required: true, message: '请输入区/县', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 200, message: '地址长度为 5-200 个字符', trigger: 'blur' }
  ]
}

// 获取团点信息
const fetchStoreInfo = async () => {
  if (!userStore.userInfo?.userId) return
  
  loading.value = true
  try {
    const data = await getMyLeaderInfo(userStore.userInfo.userId)
    storeInfo.value = data
    
    // 填充表单
    form.storeName = data.storeName || ''
    form.province = data.province || ''
    form.city = data.city || ''
    form.district = data.district || ''
    form.address = data.address || data.detailAddress || ''
    form.description = data.description || ''
  } catch (error) {
    console.error('获取团点信息失败:', error)
    storeInfo.value = null
  } finally {
    loading.value = false
  }
}

// 编辑处理
const handleEdit = () => {
  editing.value = true
}

// 取消编辑
const handleCancel = () => {
  editing.value = false
  // 恢复原始数据
  if (storeInfo.value) {
    form.storeName = storeInfo.value.storeName || ''
    form.province = storeInfo.value.province || ''
    form.city = storeInfo.value.city || ''
    form.district = storeInfo.value.district || ''
    form.address = storeInfo.value.address || storeInfo.value.detailAddress || ''
    form.description = storeInfo.value.description || ''
  }
}

// 提交修改
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    // 验证表单
    await formRef.value.validate()

    // 二次确认
    await ElMessageBox.confirm(
      '修改团点信息后需要重新审核，确定要提交吗？',
      '确认修改',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    submitting.value = true

    const updateData = {
      storeId: storeInfo.value.storeId,
      storeName: form.storeName,
      province: form.province,
      city: form.city,
      district: form.district,
      address: form.address,
      description: form.description
    }

    await updateLeaderStore(updateData.storeId, updateData)

    ElMessage.success('信息已更新，等待管理员审核')
    editing.value = false
    
    // 重新获取数据
    await fetchStoreInfo()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新失败:', error)
      ElMessage.error(error.message || '更新失败')
    }
  } finally {
    submitting.value = false
  }
}

// 获取审核状态类型
const getAuditStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取审核状态文本
const getAuditStatusText = (status) => {
  const textMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return textMap[status] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 页面加载
onMounted(() => {
  if (!userStore.isLeader) {
    ElMessage.warning('仅团长可访问')
    router.push('/leader/apply')
    return
  }
  
  fetchStoreInfo()
})
</script>

<style scoped>
.store-settings-wrapper {
  min-height: 100vh;
  padding-top: 84px;
  background-color: #f5f5f5;
}

.store-settings-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #909399;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.card-header > span {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notice-card {
  background-color: #fffbf0;
}

.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-list li {
  padding: 8px 0;
  line-height: 1.6;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .store-settings-wrapper {
    padding-top: 76px;
  }

  .store-settings-container {
    padding: 10px;
  }

  .page-header h2 {
    font-size: 24px;
  }
}
</style>

