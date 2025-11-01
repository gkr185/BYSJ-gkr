<template>
  <div class="user-info-page-wrapper">
    <div class="user-info-page">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>个人信息</h2>
        <p class="subtitle">编辑您的个人资料</p>
      </div>
    
      <el-card shadow="hover">
        <!-- 头像区域 -->
        <div class="avatar-section">
          <el-avatar :size="100" :src="form.avatar" class="avatar">
            <el-icon :size="50"><User /></el-icon>
          </el-avatar>
          <div class="avatar-actions">
            <el-button type="primary" size="small" @click="ElMessage.info('头像上传功能待开发')">
              <el-icon><Upload /></el-icon>
              更换头像
            </el-button>
            <p class="avatar-tip">支持JPG、PNG格式，不超过2MB</p>
          </div>
        </div>

        <el-divider />

        <!-- 信息表单 -->
        <el-form 
          ref="formRef"
          :model="form" 
          :rules="rules"
          label-width="100px"
          :disabled="!editing"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" disabled>
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
            <div class="form-tip">用户名不可修改</div>
          </el-form-item>
          
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11">
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-divider />

          <!-- 只读信息 -->
          <el-descriptions :column="2" border>
            <el-descriptions-item label="账户角色">
              <el-tag :type="getRoleTagType(form.role)">{{ roleText }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="账户状态">
              <el-tag :type="form.status === 1 ? 'success' : 'danger'">
                {{ statusText }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="注册时间" :span="2">
              {{ formatDateTime(form.createdAt) }}
            </el-descriptions-item>
          </el-descriptions>

          <el-divider />
          
          <el-form-item>
            <template v-if="!editing">
              <el-button type="primary" @click="editing = true">
                <el-icon><Edit /></el-icon>
                编辑信息
              </el-button>
              <el-button @click="router.back()">
                <el-icon><Back /></el-icon>
                返回
              </el-button>
            </template>
            <template v-else>
              <el-button type="primary" @click="handleSave" :loading="loading">
                <el-icon><Check /></el-icon>
                保存修改
              </el-button>
              <el-button @click="handleCancel">
                <el-icon><Close /></el-icon>
                取消
              </el-button>
            </template>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 安全提示 -->
      <el-alert 
        type="info" 
        :closable="false" 
        style="margin-top: 20px;"
      >
        <template #title>
          🔐 安全提示
        </template>
        <ul style="margin: 10px 0 0 0; padding-left: 20px;">
          <li>请确保填写真实信息，以便订单配送和售后服务</li>
          <li>手机号用于接收订单通知和验证码</li>
          <li>如需修改密码或注销账户，请联系客服</li>
        </ul>
      </el-alert>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUserInfo } from '@/api/user'
import { ElMessage } from 'element-plus'
import {
  User,
  Phone,
  Upload,
  Edit,
  Check,
  Close,
  Back
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const editing = ref(false)
const formRef = ref(null)

const form = ref({
  username: '',
  realName: '',
  phone: '',
  avatar: '',
  role: 1,
  status: 1,
  createdAt: ''
})

// 原始数据备份
const originalForm = ref({})

// 表单验证规则
const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度为 2-20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

// 角色文本
const roleText = computed(() => {
  const roleMap = { 1: '普通用户', 2: '团长', 3: '管理员' }
  return roleMap[form.value.role] || '未知'
})

// 状态文本
const statusText = computed(() => {
  return form.value.status === 1 ? '正常' : '禁用'
})

// 获取角色标签类型
const getRoleTagType = (role) => {
  const typeMap = { 1: 'info', 2: 'warning', 3: 'danger' }
  return typeMap[role] || 'info'
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

// 获取用户信息
const fetchUserInfo = async () => {
  if (!userStore.userInfo?.userId) return
  
  try {
    const data = await getUserInfo(userStore.userInfo.userId)
    form.value = {
      username: data.username,
      realName: data.realName || '',
      phone: data.phone,
      avatar: data.avatar || '',
      role: data.role,
      status: data.status,
      createdAt: data.createdAt || data.createTime || ''
    }
    // 备份原始数据
    originalForm.value = JSON.parse(JSON.stringify(form.value))
  } catch (error) {
    console.error('Failed to fetch user info:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 取消编辑
const handleCancel = () => {
  editing.value = false
  // 恢复原始数据
  form.value = JSON.parse(JSON.stringify(originalForm.value))
  // 清空验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 保存修改
const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    // 验证表单
    await formRef.value.validate()

    if (!userStore.userInfo?.userId) return
    
    loading.value = true

    await updateUserInfo(userStore.userInfo.userId, {
      realName: form.value.realName,
      phone: form.value.phone,
      avatar: form.value.avatar
    })
    
    ElMessage.success('保存成功')
    editing.value = false
    
    // 更新store中的用户信息
    await userStore.updateUserInfo()
    
    // 更新备份数据
    originalForm.value = JSON.parse(JSON.stringify(form.value))
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to update user info:', error)
      ElMessage.error(error.message || '保存失败')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.user-info-page-wrapper {
  min-height: 100vh;
  padding-top: 84px;
  background-color: #f5f5f5;
}

.user-info-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px 20px 20px;
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
  margin: 0;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  align-items: center;
  gap: 30px;
  padding: 20px 0;
}

.avatar {
  border: 3px solid #e4e7ed;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.avatar-actions {
  flex: 1;
}

.avatar-tip {
  margin: 8px 0 0 0;
  font-size: 12px;
  color: #909399;
}

/* 表单提示 */
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-info-page-wrapper {
    padding-top: 76px;
  }

  .avatar-section {
    flex-direction: column;
    text-align: center;
  }

  .page-header h2 {
    font-size: 24px;
  }
}
</style>
