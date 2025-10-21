<template>
  <div class="user-info-page-wrapper">
    <div class="user-info-page">
    <h2>个人信息</h2>
    
    <el-card>
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="角色">
          <el-input v-model="roleText" disabled />
        </el-form-item>
        
        <el-form-item label="状态">
          <el-input v-model="statusText" disabled />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="loading">保存修改</el-button>
          <el-button @click="router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserInfo, updateUserInfo } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)

const form = ref({
  username: '',
  realName: '',
  phone: '',
  avatar: '',
  role: 1,
  status: 1
})

// 角色文本
const roleText = computed(() => {
  const roleMap = { 1: '普通用户', 2: '团长', 3: '管理员' }
  return roleMap[form.value.role] || '未知'
})

// 状态文本
const statusText = computed(() => {
  return form.value.status === 1 ? '正常' : '禁用'
})

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
      status: data.status
    }
  } catch (error) {
    console.error('Failed to fetch user info:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 保存修改
const handleSave = async () => {
  if (!userStore.userInfo?.userId) return
  
  loading.value = true
  try {
    await updateUserInfo(userStore.userInfo.userId, {
      realName: form.value.realName,
      phone: form.value.phone,
      avatar: form.value.avatar
    })
    
    ElMessage.success('保存成功')
    await userStore.updateUserInfo()
    
    setTimeout(() => {
      router.back()
    }, 1000)
  } catch (error) {
    console.error('Failed to update user info:', error)
    ElMessage.error('保存失败')
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
  padding-top: 84px; /* 64px导航栏 + 20px间隔 */
  background-color: #f5f5f5;
}

.user-info-page {
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
  .user-info-page-wrapper {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }
}
</style>
