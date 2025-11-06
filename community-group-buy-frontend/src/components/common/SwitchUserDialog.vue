<template>
  <el-dialog
    v-model="visible"
    title="快速切换测试用户"
    width="700px"
    :close-on-click-modal="false"
    :z-index="3000"
    append-to-body
    @close="handleClose"
  >
    <el-alert
      title="开发调试功能"
      type="warning"
      :closable="false"
      show-icon
      class="mb-4"
    >
      此功能仅在开发环境可用，用于快速切换不同角色用户进行测试。密码统一为：123123
    </el-alert>

    <div v-loading="loading || loadingUsers" class="user-list">
      <div
        v-for="testUser in testUsers"
        :key="testUser.username"
        class="user-card"
        :class="{ 'active': currentUserId === testUser.userId }"
        @click="handleSwitch(testUser)"
      >
        <div class="user-avatar">
          <el-avatar :size="64" :style="{ background: testUser.color }">
            <el-icon :size="32"><component :is="testUser.icon" /></el-icon>
          </el-avatar>
          <el-tag
            v-if="currentUserId === testUser.userId"
            type="success"
            size="small"
            class="current-tag"
          >
            当前用户
          </el-tag>
        </div>
        <div class="user-info">
          <div class="user-name">{{ testUser.realName }}</div>
          <div class="user-meta">
            <el-tag :type="testUser.roleType" size="small">
              {{ testUser.roleName }}
            </el-tag>
            <span class="username">@{{ testUser.username }}</span>
          </div>
          <div class="user-desc">{{ testUser.description }}</div>
          <div class="user-details">
            <span class="detail-item">
              <el-icon><Phone /></el-icon>
              {{ testUser.phone }}
            </span>
            <span class="detail-item" v-if="testUser.communityName">
              <el-icon><Location /></el-icon>
              {{ testUser.communityName }}
            </span>
          </div>
        </div>
        <div class="user-action">
          <el-button
            v-if="currentUserId !== testUser.userId"
            type="primary"
            :icon="Refresh"
            @click.stop="handleSwitch(testUser)"
          >
            切换
          </el-button>
          <el-tag v-else type="success" size="large">
            <el-icon><CircleCheck /></el-icon>
            已登录
          </el-tag>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, UserFilled, Star, Phone, Location, Refresh, CircleCheck } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getUsersByRole } from '@/api/user'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'switched'])

const userStore = useUserStore()
const visible = ref(false)
const loading = ref(false)
const loadingUsers = ref(false)

// 当前登录用户ID
const currentUserId = computed(() => userStore.userInfo?.userId)

// 用户列表
const testUsers = ref([])

// 默认测试密码（实际环境应该更安全）
const DEFAULT_TEST_PASSWORD = '123123'

// 角色配置映射
const roleConfig = {
  3: {
    roleName: '管理员',
    roleType: 'danger',
    description: '系统管理员，拥有最高权限，可管理所有用户、商品、订单',
    color: '#f56c6c',
    icon: Star
  },
  2: {
    roleName: '团长',
    roleType: 'warning',
    description: '社区团长，可发起拼团活动、管理团购订单、查看佣金',
    color: '#e6a23c',
    icon: UserFilled
  },
  1: {
    roleName: '普通用户',
    roleType: 'info',
    description: '社区居民，可浏览商品、参与拼团、下单购物',
    color: '#909399',
    icon: User
  }
}

// 加载用户列表
const loadUsers = async () => {
  loadingUsers.value = true
  const allUsers = []
  
  try {
    // 获取管理员用户
    const adminRes = await getUsersByRole(3)
    if (adminRes.code === 200 && adminRes.data) {
      allUsers.push(...adminRes.data.map(user => formatUser(user, 3)))
    }
  } catch (error) {
    console.warn('获取管理员列表失败:', error)
  }
  
  try {
    // 获取团长用户
    const leaderRes = await getUsersByRole(2)
    if (leaderRes.code === 200 && leaderRes.data) {
      allUsers.push(...leaderRes.data.map(user => formatUser(user, 2)))
    }
  } catch (error) {
    console.warn('获取团长列表失败:', error)
  }
  
  try {
    // 获取普通用户（限制前10个）
    const userRes = await getUsersByRole(1)
    if (userRes.code === 200 && userRes.data) {
      allUsers.push(...userRes.data.slice(0, 10).map(user => formatUser(user, 1)))
    }
  } catch (error) {
    console.warn('获取普通用户列表失败:', error)
  }
  
  // 如果没有获取到用户，使用默认测试用户
  if (allUsers.length === 0) {
    allUsers.push(...getDefaultTestUsers())
    ElMessage.warning('无法获取用户列表，使用默认测试账号')
  }
  
  testUsers.value = allUsers
  loadingUsers.value = false
}

// 格式化用户数据
const formatUser = (user, role) => {
  const config = roleConfig[role] || roleConfig[1]
  
  return {
    userId: user.userId,
    username: user.username,
    password: DEFAULT_TEST_PASSWORD,
    realName: user.realName || user.username,
    phone: user.phone,
    role: role,
    roleName: config.roleName,
    roleType: config.roleType,
    description: config.description,
    communityName: user.communityName || '未知社区',
    color: config.color,
    icon: config.icon
  }
}

// 获取默认测试用户（API调用失败时使用）
const getDefaultTestUsers = () => {
  return [
    {
      userId: 3,
      username: '获取默认测试用户（API调用失败）',
      password: DEFAULT_TEST_PASSWORD,
      realName: '系统管理员',
      phone: '13900000001',
      role: 3,
      roleName: '管理员',
      roleType: 'danger',
      description: '系统管理员，拥有最高权限，可管理所有用户、商品、订单',
      communityName: '全部社区',
      color: '#f56c6c',
      icon: Star
    },
    
  ]
}

// 监听modelValue变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  // 对话框打开时加载用户列表
  if (val && testUsers.value.length === 0) {
    loadUsers()
  }
})

// 监听visible变化
watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 组件挂载时加载用户列表
onMounted(() => {
  loadUsers()
})

// 切换用户
const handleSwitch = async (testUser) => {
  if (currentUserId.value === testUser.userId) {
    ElMessage.info('当前已是该用户')
    return
  }

  loading.value = true
  try {
    // 使用 userStore.login 方法登录
    await userStore.login({
      username: testUser.username,
      password: testUser.password
    })
    
    ElMessage.success(`已切换至 ${testUser.realName}（${testUser.roleName}）`)
    
    // 关闭对话框
    visible.value = false
    
    // 触发切换成功事件
    emit('switched')
  } catch (error) {
    console.error('❌ 切换用户失败:', error)
    ElMessage.error('切换失败，请检查后端服务是否正常')
  } finally {
    loading.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}
</script>

<style scoped>
/* 确保对话框在最上层 */
:deep(.el-dialog) {
  z-index: 3000 !important;
}

:deep(.el-overlay) {
  z-index: 2999 !important;
}

.mb-4 {
  margin-bottom: 20px;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-height: 600px;
  overflow-y: auto;
  padding: 8px;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fff;
  position: relative;
}

.user-card:hover {
  border-color: #409EFF;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.user-card.active {
  border-color: #67c23a;
  background: linear-gradient(135deg, #f0f9ff 0%, #e6f7ff 100%);
}

.user-avatar {
  position: relative;
  flex-shrink: 0;
}

.user-avatar :deep(.el-avatar) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 3px solid #fff;
}

.current-tag {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 11px;
  padding: 2px 8px;
  white-space: nowrap;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.username {
  font-size: 13px;
  color: #909399;
  font-family: 'Courier New', monospace;
}

.user-desc {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
  line-height: 1.5;
}

.user-details {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.detail-item .el-icon {
  font-size: 14px;
}

.user-action {
  flex-shrink: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

/* 滚动条样式 */
.user-list::-webkit-scrollbar {
  width: 8px;
}

.user-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.user-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.user-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式 */
@media (max-width: 768px) {
  .user-card {
    flex-direction: column;
    text-align: center;
  }

  .user-details {
    justify-content: center;
  }
}
</style>

