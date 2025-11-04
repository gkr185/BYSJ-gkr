<template>
  <div class="register-container">
    <div class="register-content">
      <!-- 左侧宣传区 -->
      <div class="promo-section">
        <div class="promo-content">
          <h1>欢迎加入</h1>
          <h2>社区团购平台</h2>
          <p class="promo-desc">新鲜优质·邻里互助·便捷实惠</p>
          
          <div class="features">
            <div class="feature-item">
              <el-icon :size="32" color="#409EFF"><Goods /></el-icon>
              <h3>优质商品</h3>
              <p>精选商品，品质保证</p>
            </div>
            <div class="feature-item">
              <el-icon :size="32" color="#67C23A"><Grid /></el-icon>
              <h3>拼团实惠</h3>
              <p>邻里拼团，价格更优</p>
            </div>
            <div class="feature-item">
              <el-icon :size="32" color="#E6A23C"><Van /></el-icon>
              <h3>便捷配送</h3>
              <p>社区配送，快速到家</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="form-section">
        <el-card class="register-card" shadow="never">
          <div class="card-header">
            <h2>用户注册</h2>
            <p>创建您的账号，开始团购之旅</p>
          </div>

          <el-form
            ref="formRef"
            :model="registerForm"
            :rules="rules"
            label-position="top"
            size="large"
          >
            <!-- 用户名 -->
            <el-form-item label="用户名" prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名（字母、数字、下划线，3-20位）"
                maxlength="20"
                show-word-limit
                clearable
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 密码 -->
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码（至少6位）"
                show-password
                clearable
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 确认密码 -->
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                show-password
                clearable
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 真实姓名（可选）-->
            <el-form-item label="真实姓名（可选）">
              <el-input
                v-model="registerForm.realName"
                placeholder="请输入真实姓名"
                maxlength="20"
                clearable
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 手机号（可选）-->
            <el-form-item label="手机号（可选）" prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号"
                maxlength="11"
                clearable
              >
                <template #prefix>
                  <el-icon><Iphone /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 用户协议 -->
            <el-form-item prop="agree">
              <el-checkbox v-model="registerForm.agree">
                我已阅读并同意
                <el-link type="primary" :underline="false">《用户协议》</el-link>
                和
                <el-link type="primary" :underline="false">《隐私政策》</el-link>
              </el-checkbox>
            </el-form-item>

            <!-- 注册按钮 -->
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                @click="handleRegister"
                :loading="loading"
                style="width: 100%"
              >
                立即注册
              </el-button>
            </el-form-item>

            <!-- 已有账号 -->
            <div class="login-link">
              已有账号？
              <el-link type="primary" @click="goToLogin">立即登录</el-link>
            </div>
          </el-form>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone, Goods, Grid, Van } from '@element-plus/icons-vue'
import { register } from '@/api/user'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

// 表单数据
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  agree: false
})

// 校验规则（简化，只保留必填）
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' }
  ]
  // phone 和 realName 是可选的，不需要验证
  // agree 不强制验证
}

// 注册处理
const handleRegister = async () => {
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    const { username, password, realName, phone } = registerForm
    
    const res = await register({
      username,
      password,
      realName: realName || null,
      phone: phone || null,
      role: 1 // 默认注册为普通用户
    })
    
    if (res.code === 200) {
      ElMessage.success('注册成功！请登录')
      
      // 跳转到登录页
      setTimeout(() => {
        router.push('/login')
      }, 1000)
    }
    
  } catch (error) {
    // 错误已在 request.js 中处理
    console.error('Register failed:', error)
  } finally {
    loading.value = false
  }
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.register-content {
  width: 100%;
  max-width: 1100px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  align-items: center;
}

/* 左侧宣传区 */
.promo-section {
  color: #fff;
}

.promo-content h1 {
  font-size: 48px;
  margin-bottom: 8px;
  font-weight: bold;
}

.promo-content h2 {
  font-size: 36px;
  margin-bottom: 16px;
  font-weight: bold;
}

.promo-desc {
  font-size: 18px;
  margin-bottom: 60px;
  opacity: 0.9;
}

.features {
  display: grid;
  gap: 30px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.feature-item h3 {
  font-size: 18px;
  margin-bottom: 6px;
  color: #fff;
}

.feature-item p {
  font-size: 14px;
  opacity: 0.8;
  margin: 0;
}

/* 右侧表单区 */
.form-section {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
}

.register-card {
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

.login-link {
  text-align: center;
  margin-top: 16px;
  color: #666;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .register-content {
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

