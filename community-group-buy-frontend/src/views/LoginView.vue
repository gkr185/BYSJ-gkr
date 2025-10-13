<template>
  <div class="login-page">
    <div class="login-box">
      <!-- 标题 -->
      <h2>社区团购系统 - {{ isLogin ? '用户登录' : '用户注册' }}</h2>
      
      <!-- 切换按钮 -->
      <el-tabs v-model="activeTab" class="form-tabs">
        <el-tab-pane label="登录" name="login"></el-tab-pane>
        <el-tab-pane label="注册" name="register"></el-tab-pane>
      </el-tabs>
      
      <!-- 登录表单 -->
      <el-form v-if="isLogin" :model="loginForm" @submit.prevent="handleLogin">
        <el-form-item label="用户名">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入用户名" 
            clearable
          />
        </el-form-item>
        
        <el-form-item label="密码">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleLogin" 
            :loading="loading" 
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <!-- 注册表单 -->
      <el-form v-else :model="registerForm" @submit.prevent="handleRegister" label-width="80px">
        <el-form-item label="用户名">
          <el-input 
            v-model="registerForm.username" 
            placeholder="2-50个字符" 
            clearable
            maxlength="50"
          />
        </el-form-item>
        
        <el-form-item label="密码">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="6-50个字符" 
            show-password
            clearable
            maxlength="50"
          />
        </el-form-item>
        
        <el-form-item label="确认密码">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入密码" 
            show-password
            clearable
          />
        </el-form-item>
        
        <el-form-item label="手机号">
          <el-input 
            v-model="registerForm.phone" 
            placeholder="11位手机号" 
            clearable
            maxlength="11"
          />
        </el-form-item>
        
        <el-form-item label="真实姓名">
          <el-input 
            v-model="registerForm.realName" 
            placeholder="选填" 
            clearable
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleRegister" 
            :loading="loading" 
            style="width: 100%"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="tips" v-if="isLogin">测试账号：用户名 22  密码 123123</div>
      <div class="tips" v-else>
        <el-icon><InfoFilled /></el-icon> 注册成功后将自动登录
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activeTab = ref('login')

// 是否为登录模式
const isLogin = computed(() => activeTab.value === 'login')

// 登录表单
const loginForm = ref({
  username: '',
  password: ''
})

// 注册表单
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  realName: '',
  role: 1  // 默认普通用户
})

// 切换tab时清空表单
watch(activeTab, () => {
  loginForm.value = { username: '', password: '' }
  registerForm.value = {
    username: '',
    password: '',
    confirmPassword: '',
    phone: '',
    realName: '',
    role: 1
  }
})

// 登录
const handleLogin = async () => {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  
  loading.value = true
  try {
    await userStore.login(loginForm.value)
    ElMessage.success('登录成功')
    router.push('/profile')
  } catch (error) {
    console.error('Login failed:', error)
  } finally {
    loading.value = false
  }
}

// 注册
const handleRegister = async () => {
  // 表单验证
  if (!registerForm.value.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (registerForm.value.username.length < 2 || registerForm.value.username.length > 50) {
    ElMessage.warning('用户名长度为2-50个字符')
    return
  }
  if (!registerForm.value.password) {
    ElMessage.warning('请输入密码')
    return
  }
  if (registerForm.value.password.length < 6 || registerForm.value.password.length > 50) {
    ElMessage.warning('密码长度为6-50个字符')
    return
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  if (!registerForm.value.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (registerForm.value.phone.length !== 11) {
    ElMessage.warning('手机号必须为11位')
    return
  }
  
  loading.value = true
  try {
    // 调用注册接口
    const data = {
      username: registerForm.value.username,
      password: registerForm.value.password,
      phone: registerForm.value.phone,
      role: registerForm.value.role,
      realName: registerForm.value.realName || undefined
    }
    
    await register(data)
    ElMessage.success('注册成功！正在自动登录...')
    
    // 自动登录
    setTimeout(async () => {
      try {
        await userStore.login({
          username: registerForm.value.username,
          password: registerForm.value.password
        })
        router.push('/profile')
      } catch (error) {
        console.error('Auto login failed:', error)
        ElMessage.info('请手动登录')
        activeTab.value = 'login'
      }
    }, 500)
    
  } catch (error) {
    console.error('Register failed:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f0f2f5;
}

.login-box {
  background: white;
  padding: 40px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  width: 450px;
}

.login-box h2 {
  margin-bottom: 20px;
  text-align: center;
  color: #333;
}

.form-tabs {
  margin-bottom: 20px;
}

.form-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.tips {
  margin-top: 20px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  color: #909399;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}
</style>
