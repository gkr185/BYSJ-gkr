<template>
  <div class="login-container">
    <div class="login-content">
      <!-- 左侧宣传区 -->
      <div class="promo-section">
        <div class="promo-content">
          <h1>社区团购</h1>
          <p class="promo-tagline">新鲜优质·邻里互助·便捷实惠</p>
          
          <div class="features">
            <div class="feature-item">
              <el-icon :size="24"><CircleCheck /></el-icon>
              <div>
                <h4>品质保证</h4>
                <p>精选商品，源头直采</p>
              </div>
            </div>
            <div class="feature-item">
              <el-icon :size="24"><CircleCheck /></el-icon>
              <div>
                <h4>价格实惠</h4>
                <p>邻里拼团，团购更优惠</p>
              </div>
            </div>
            <div class="feature-item">
              <el-icon :size="24"><CircleCheck /></el-icon>
              <div>
                <h4>便捷配送</h4>
                <p>社区自提，快速到家</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-section">
        <el-card class="login-card" shadow="never">
          <div class="card-header">
            <h2>欢迎回来</h2>
            <p>登录您的账号，开始团购之旅</p>
          </div>

          <el-form
            ref="formRef"
            :model="loginForm"
            :rules="rules"
            label-position="top"
            size="large"
            @keyup.enter="handleLogin"
          >
            <!-- 用户名 -->
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                clearable
                autofocus
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 密码 -->
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                show-password
                clearable
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 记住我 -->
            <el-form-item>
              <div class="login-options">
                <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
                <el-link type="primary" :underline="false">忘记密码？</el-link>
              </div>
            </el-form-item>

            <!-- 登录按钮 -->
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                @click="handleLogin"
                :loading="loading"
                style="width: 100%"
              >
                登 录
              </el-button>
            </el-form-item>

            <!-- 快速登录 -->
            <div class="quick-login">
              <el-divider>快速登录</el-divider>
              <div class="quick-login-buttons">
                <el-button circle disabled>
                  <el-icon><Message /></el-icon>
                </el-button>
                <el-button circle disabled>
                  <el-icon><Iphone /></el-icon>
                </el-button>
              </div>
            </div>

            <!-- 没有账号 -->
            <div class="register-link">
              还没有账号？
              <el-link type="primary" @click="goToRegister">立即注册</el-link>
            </div>
          </el-form>

          <!-- 测试账号提示 -->
          <el-alert
            type="info"
            :closable="false"
            style="margin-top: 20px"
          >
            <template #title>
              <div style="font-size: 12px">
                测试账号：<strong>22</strong> / 密码：<strong>123123</strong>
              </div>
            </template>
          </el-alert>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, CircleCheck, Message, Iphone } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

// 表单数据
const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

// 校验规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

// 从localStorage恢复记住的密码
onMounted(() => {
  const savedUsername = localStorage.getItem('saved_username')
  const savedPassword = localStorage.getItem('saved_password')
  if (savedUsername && savedPassword) {
    loginForm.username = savedUsername
    loginForm.password = savedPassword
    loginForm.remember = true
  }
})

// 登录处理
const handleLogin = async () => {
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    await userStore.login({
      username: loginForm.username,
      password: loginForm.password
    })
    
    // 记住密码
    if (loginForm.remember) {
      localStorage.setItem('saved_username', loginForm.username)
      localStorage.setItem('saved_password', loginForm.password)
    } else {
      localStorage.removeItem('saved_username')
      localStorage.removeItem('saved_password')
    }
    
    ElMessage.success('登录成功')
    
    // 跳转到首页
    setTimeout(() => {
      router.push('/')
    }, 500)
    
  } catch (error) {
    // 错误已在 request.js 中处理
    console.error('Login failed:', error)
  } finally {
    loading.value = false
  }
}

// 跳转到注册页
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-content {
  width: 100%;
  max-width: 1000px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

/* 左侧宣传区 */
.promo-section {
  color: #fff;
}

.promo-content h1 {
  font-size: 56px;
  margin-bottom: 16px;
  font-weight: bold;
  letter-spacing: 2px;
}

.promo-tagline {
  font-size: 20px;
  margin-bottom: 60px;
  opacity: 0.9;
}

.features {
  display: grid;
  gap: 24px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.feature-item h4 {
  font-size: 16px;
  margin: 0 0 4px 0;
  color: #fff;
}

.feature-item p {
  font-size: 13px;
  opacity: 0.8;
  margin: 0;
}

/* 右侧表单区 */
.form-section {
  background: #fff;
  border-radius: 20px;
  padding: 20px;
}

.login-card {
  border: none;
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.card-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.card-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.quick-login {
  margin: 24px 0;
}

.quick-login-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
}

.register-link {
  text-align: center;
  margin-top: 16px;
  color: #666;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .login-content {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .promo-section {
    display: none;
  }
  
  .form-section {
    padding: 10px;
  }
}
</style>

