<template>
  <MainLayout>
    <div class="user-info-container">
      <div class="page-header">
        <el-button :icon="ArrowLeft" @click="$router.back()">返回</el-button>
        <h2 class="page-title">
          <el-icon><Edit /></el-icon>
          个人资料
        </h2>
      </div>

      <div class="info-card">
        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="100px"
          label-position="left"
        >
          <!-- 头像 -->
          <el-form-item label="头像">
            <div class="avatar-section">
              <el-avatar :size="100" :src="formData.avatar" class="current-avatar">
                {{ formData.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :headers="getUploadHeaders()"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :on-success="handleAvatarSuccess"
                :on-error="handleUploadError"
                accept=".jpg,.jpeg,.png,.gif,.webp"
              >
                <el-button type="primary" :loading="avatarLoading">
                  <el-icon><Upload /></el-icon>
                  上传头像
                </el-button>
              </el-upload>
              <div class="avatar-tip">支持jpg、png、gif、webp格式，大小不超过2MB</div>
            </div>
          </el-form-item>

          <!-- 用户名 -->
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="formData.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              disabled
            >
              <template #append>
                <span class="input-tip">不可修改</span>
              </template>
            </el-input>
          </el-form-item>

          <!-- 真实姓名 -->
          <el-form-item label="真实姓名" prop="realName">
            <el-input
              v-model="formData.realName"
              placeholder="请输入真实姓名"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <!-- 手机号 -->
          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="formData.phone"
              placeholder="请输入手机号"
              :prefix-icon="Phone"
              maxlength="11"
              clearable
            />
          </el-form-item>

          <!-- 所属社区 -->
          <el-form-item label="所属社区" prop="communityId">
            <div class="community-selection">
              <el-select
                v-model="formData.communityId"
                placeholder="请选择所属社区"
                filterable
                clearable
                style="flex: 1"
                :loading="communityLoading"
              >
                <el-option
                  v-for="community in communityList"
                  :key="community.communityId"
                  :label="`${community.name} (${community.address})`"
                  :value="community.communityId"
                >
                  <div class="community-option">
                    <span class="community-name">{{ community.name }}</span>
                    <span class="community-address">{{ community.address }}</span>
                  </div>
                </el-option>
              </el-select>
              <div class="community-actions">
                <el-button
                  size="small"
                  :icon="Location"
                  :loading="locatingCommunity"
                  @click="matchNearestCommunity"
                >
                  定位最近社区
                </el-button>
                <el-button
                  size="small"
                  :icon="MapLocation"
                  :loading="matchingByAddress"
                  @click="matchByDefaultAddress"
                  :disabled="!hasDefaultAddress"
                >
                  按收货地址匹配
                </el-button>
              </div>
              <div class="community-tip">
                <el-text size="small" type="info">
                  💡 提示：可通过定位或收货地址自动匹配最近的社区
                </el-text>
              </div>
            </div>
          </el-form-item>

          <!-- 按钮组 -->
          <el-form-item>
            <div class="button-group">
              <el-button type="primary" :loading="loading" @click="handleSubmit">
                <el-icon><Select /></el-icon>
                保存修改
              </el-button>
              <el-button @click="handleReset">
                <el-icon><RefreshLeft /></el-icon>
                重置
              </el-button>
              <el-button type="danger" plain @click="showPasswordDialog = true">
                <el-icon><Lock /></el-icon>
                修改密码
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 修改密码对话框 -->
      <el-dialog
        v-model="showPasswordDialog"
        title="修改密码"
        width="500px"
        :close-on-click-modal="false"
      >
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="100px"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入原密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" :loading="passwordLoading" @click="handlePasswordSubmit">
            确定
          </el-button>
        </template>
      </el-dialog>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Edit,
  User,
  Phone,
  Location,
  Select,
  RefreshLeft,
  Lock,
  Upload,
  MapLocation
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { updateUserInfo, getAddressList } from '@/api/user'
import { getCommunityList, getNearestCommunity } from '@/api/leader'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const passwordFormRef = ref()
const loading = ref(false)
const passwordLoading = ref(false)
const showPasswordDialog = ref(false)
const avatarLoading = ref(false)
const communityLoading = ref(false)
const locatingCommunity = ref(false)
const matchingByAddress = ref(false)

// 社区相关
const communityList = ref([])
const userAddresses = ref([])
const hasDefaultAddress = computed(() => {
  return userAddresses.value.some(addr => addr.isDefault === 1 || addr.isDefault === true)
})

// 文件上传相关
const uploadUrl = 'http://localhost:9000/api/upload/avatar'

// 获取上传headers（每次上传时动态获取最新token）
const getUploadHeaders = () => {
  const token = userStore.token
  
  if (!token) {
    ElMessage.error('Token不存在，请重新登录')
  }
  
  return {
    'Authorization': `Bearer ${token || ''}`
  }
}

// 表单数据
const formData = reactive({
  username: '',
  realName: '',
  phone: '',
  communityId: '',
  avatar: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证规则（简化，只保留基本验证）
const rules = {
  // 无必填验证，可选填
}

// 密码验证规则（简化）
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' }
  ]
}

// 初始化表单数据
const initFormData = () => {
  if (!userStore.userInfo) {
    return
  }
  const userInfo = userStore.userInfo
  formData.username = userInfo.username || ''
  formData.realName = userInfo.realName || ''
  formData.phone = userInfo.phone || ''
  formData.communityId = userInfo.communityId || null
  formData.avatar = userInfo.avatar || ''
}

// 加载社区列表
const loadCommunityList = async () => {
  communityLoading.value = true
  try {
    const res = await getCommunityList()
    if (res.code === 200) {
      communityList.value = res.data || []
    } else {
      ElMessage.error('加载社区列表失败')
    }
  } catch (error) {
    console.error('加载社区列表失败:', error)
  } finally {
    communityLoading.value = false
  }
}

// 加载用户地址列表
const loadUserAddresses = async () => {
  if (!userStore.userInfo?.userId) return
  
  try {
    const res = await getAddressList(userStore.userInfo.userId)
    if (res.code === 200) {
      userAddresses.value = res.data || []
    }
  } catch (error) {
    console.error('加载地址列表失败:', error)
  }
}

// 根据定位匹配最近的社区
const matchNearestCommunity = () => {
  if (!navigator.geolocation) {
    ElMessage.error('您的浏览器不支持地理定位')
    return
  }

  locatingCommunity.value = true
  navigator.geolocation.getCurrentPosition(
    async (position) => {
      try {
        const res = await getNearestCommunity({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude
        })
        
        if (res.code === 200 && res.data) {
          formData.communityId = res.data.communityId
          ElMessage.success(`已自动匹配到最近的社区：${res.data.name}`)
        } else {
          ElMessage.warning('未找到附近的社区，请手动选择')
        }
      } catch (error) {
        console.error('匹配社区失败:', error)
        ElMessage.error('匹配社区失败，请稍后重试')
      } finally {
        locatingCommunity.value = false
      }
    },
    (error) => {
      locatingCommunity.value = false
      let errorMsg = '定位失败'
      switch (error.code) {
        case error.PERMISSION_DENIED:
          errorMsg = '用户拒绝了定位请求，请手动选择社区'
          break
        case error.POSITION_UNAVAILABLE:
          errorMsg = '位置信息不可用'
          break
        case error.TIMEOUT:
          errorMsg = '定位请求超时'
          break
      }
      ElMessage.error(errorMsg)
    },
    {
      enableHighAccuracy: true,
      timeout: 5000,
      maximumAge: 0
    }
  )
}

// 根据默认收货地址匹配最近的社区
const matchByDefaultAddress = async () => {
  const defaultAddress = userAddresses.value.find(
    addr => addr.isDefault === 1 || addr.isDefault === true
  )
  
  if (!defaultAddress) {
    ElMessage.warning('您还没有设置默认收货地址')
    return
  }

  if (!defaultAddress.latitude || !defaultAddress.longitude) {
    ElMessage.warning('默认地址缺少经纬度信息，请重新编辑地址并获取定位')
    return
  }

  matchingByAddress.value = true
  try {
    const res = await getNearestCommunity({
      latitude: defaultAddress.latitude,
      longitude: defaultAddress.longitude
    })
    
    if (res.code === 200 && res.data) {
      formData.communityId = res.data.communityId
      ElMessage.success(`已根据收货地址匹配到最近的社区：${res.data.name}`)
    } else {
      ElMessage.warning('未找到附近的社区，请手动选择')
    }
  } catch (error) {
    console.error('匹配社区失败:', error)
    ElMessage.error('匹配社区失败，请稍后重试')
  } finally {
    matchingByAddress.value = false
  }
}

// 上传头像前的验证
const beforeAvatarUpload = (file) => {
  // 验证登录状态
  if (!userStore.token || !userStore.isLogin) {
    ElMessage.error('未登录，请先登录')
    router.push('/login')
    return false
  }
  
  // 验证文件类型
  const isValidType = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!isValidType) {
    ElMessage.error('仅支持jpg、png、gif、webp格式的图片')
    return false
  }
  
  // 验证文件大小（2MB）
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过2MB')
    return false
  }
  
  avatarLoading.value = true
  return true
}

// 头像上传成功
const handleAvatarSuccess = (response) => {
  avatarLoading.value = false
  if (response.code === 200) {
    formData.avatar = response.data
    ElMessage.success('头像上传成功')
    
    // 自动保存头像URL到用户信息
    if (userStore.userInfo?.userId) {
      updateUserInfo(userStore.userInfo.userId, {
        avatar: response.data
      }).then(() => {
        // 更新本地用户信息
        const updatedUserInfo = {
          ...userStore.userInfo,
          avatar: response.data
        }
        userStore.userInfo = updatedUserInfo
        localStorage.setItem('user_info', JSON.stringify(updatedUserInfo))
      }).catch(err => {
        console.error('保存头像失败:', err)
      })
    }
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 上传失败
const handleUploadError = () => {
  avatarLoading.value = false
  ElMessage.error('上传失败，请稍后重试')
}

// 提交表单
const handleSubmit = async () => {
  // 检查用户是否登录
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await updateUserInfo(userStore.userInfo.userId, {
        realName: formData.realName,
        phone: formData.phone,
        communityId: formData.communityId
      })

      if (res.code === 200) {
        ElMessage.success('保存成功')
        // 更新本地用户信息
        const updatedUserInfo = {
          ...userStore.userInfo,
          realName: formData.realName,
          phone: formData.phone,
          communityId: formData.communityId
        }
        // 直接更新 store 中的 userInfo
        userStore.userInfo = updatedUserInfo
        localStorage.setItem('user_info', JSON.stringify(updatedUserInfo))
      } else {
        ElMessage.error(res.message || '保存失败')
      }
    } catch (error) {
      ElMessage.error('保存失败，请稍后重试')
      console.error('保存用户信息失败:', error)
    } finally {
      loading.value = false
    }
  })
}

// 重置表单
const handleReset = () => {
  initFormData()
  formRef.value.clearValidate()
}

// 提交密码修改
const handlePasswordSubmit = async () => {
  // 检查用户是否登录
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    ElMessage.warning('请先登录')
    showPasswordDialog.value = false
    router.push('/login')
    return
  }

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    passwordLoading.value = true
    try {
      // TODO: 调用修改密码API
      // const res = await updatePassword(userStore.userInfo.userId, {
      //   oldPassword: passwordForm.oldPassword,
      //   newPassword: passwordForm.newPassword
      // })
      
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      ElMessage.success('密码修改成功，请重新登录')
      showPasswordDialog.value = false
      
      // 退出登录
      setTimeout(() => {
        userStore.logout()
        router.push('/login')
      }, 1500)
    } catch (error) {
      ElMessage.error('密码修改失败，请检查原密码是否正确')
      console.error('修改密码失败:', error)
    } finally {
      passwordLoading.value = false
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
  initFormData()
  loadCommunityList()
  loadUserAddresses()
})
</script>

<style scoped>
.user-info-container {
  max-width: 800px;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.info-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

/* 头像部分 */
.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.current-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  font-size: 36px;
  font-weight: 700;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.avatar-tip {
  font-size: 13px;
  color: #999;
  margin-top: 8px;
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 输入框提示 */
.input-tip {
  font-size: 12px;
  color: #999;
}

/* 按钮组 */
.button-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.button-group :deep(.el-button) {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  transition: all 0.3s;
}

.button-group :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.button-group :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
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

:deep(.el-radio) {
  margin-right: 24px;
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
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

/* 社区选择样式 */
.community-selection {
  width: 100%;
}

.community-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.community-name {
  font-weight: 600;
  color: #333;
}

.community-address {
  font-size: 12px;
  color: #999;
}

.community-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;
}

.community-actions :deep(.el-button) {
  border-radius: 8px;
  font-size: 13px;
}

.community-tip {
  margin-top: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .user-info-container {
    padding: 16px;
  }

  .info-card {
    padding: 20px;
  }

  :deep(.el-form) {
    --el-form-label-width: 80px;
  }

  .button-group {
    flex-direction: column;
  }

  .button-group :deep(.el-button) {
    width: 100%;
  }
  
  .community-actions {
    flex-direction: column;
  }
  
  .community-actions :deep(.el-button) {
    width: 100%;
  }
}
</style>

