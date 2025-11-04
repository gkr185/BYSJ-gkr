<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div>
            <el-button type="primary" @click="showCreateDialog">
              <el-icon><Plus /></el-icon>
              创建用户
            </el-button>
            <el-button @click="fetchUsers">刷新</el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索和筛选 -->
      <el-row :gutter="20" class="search-row">
        <el-col :span="8">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名或手机号"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-col>
        
        <el-col :span="4">
          <el-select v-model="roleFilter" placeholder="角色筛选" @change="handleRoleFilter">
            <el-option label="全部角色" :value="0" />
            <el-option label="普通用户" :value="1" />
            <el-option label="团长" :value="2" />
            <el-option label="管理员" :value="3" />
          </el-select>
        </el-col>
        
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="状态筛选" @change="handleStatusFilter">
            <el-option label="全部状态" :value="-1" />
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-col>
      </el-row>
      
      <!-- 用户列表表格 -->
      <el-table 
        :data="userList" 
        v-loading="loading"
        border
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column label="头像" width="80" align="center">
          <template #default="{ row }">
            <el-avatar :size="40" :src="row.avatar">
              {{ row.username?.charAt(0).toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleName(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showUserDetail(row.userId)">
              详情
            </el-button>
            <el-button size="small" type="primary" @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button 
              size="small" 
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="toggleUserStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="用户详情"
      width="800px"
      @closed="handleDetailDialogClosed"
    >
      <el-tabs v-model="detailActiveTab" v-if="currentUser">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="info">
          <el-descriptions :column="2" border v-if="currentUser">
            <el-descriptions-item label="用户ID">
              {{ currentUser.userId }}
            </el-descriptions-item>
            <el-descriptions-item label="用户名">
              {{ currentUser.username }}
            </el-descriptions-item>
            <el-descriptions-item label="真实姓名">
              {{ currentUser.realName || '未填写' }}
            </el-descriptions-item>
            <el-descriptions-item label="手机号">
              {{ currentUser.phone }}
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag :type="getRoleType(currentUser.role)">
                {{ getRoleName(currentUser.role) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentUser.status === 1 ? 'success' : 'danger'">
                {{ currentUser.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="头像">
              <el-image 
                v-if="currentUser.avatar" 
                :src="currentUser.avatar" 
                style="width: 50px; height: 50px; border-radius: 50%;"
                :preview-src-list="[currentUser.avatar]"
              />
              <span v-else>未设置</span>
            </el-descriptions-item>
            <el-descriptions-item label="微信OpenID">
              {{ currentUser.wxOpenid || '未绑定' }}
            </el-descriptions-item>
            <el-descriptions-item label="归属社区" :span="2">
              <el-tag v-if="currentUser.communityId" type="success">
                {{ getCommunityName(currentUser.communityId) }}
              </el-tag>
              <span v-else>未分配</span>
            </el-descriptions-item>
            <el-descriptions-item label="注册时间" :span="2">
              {{ currentUser.createTime }}
            </el-descriptions-item>
            <el-descriptions-item label="最后更新时间" :span="2">
              {{ currentUser.updateTime }}
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
        
        <!-- 账户信息 -->
        <el-tab-pane label="账户信息" name="account">
          <el-descriptions :column="2" border v-if="userAccount">
            <el-descriptions-item label="账户ID">
              {{ userAccount.accountId }}
            </el-descriptions-item>
            <el-descriptions-item label="用户ID">
              {{ userAccount.userId }}
            </el-descriptions-item>
            <el-descriptions-item label="可用余额">
              <span style="color: #67C23A; font-weight: bold;">
                ¥{{ userAccount.balance }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="冻结金额">
              <span style="color: #E6A23C; font-weight: bold;">
                ¥{{ userAccount.freezeAmount }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="总金额" :span="2">
              <span style="color: #409EFF; font-weight: bold; font-size: 16px;">
                ¥{{ userAccount.totalAmount }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="最后更新时间" :span="2">
              {{ userAccount.updateTime }}
            </el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="暂无账户信息" />
        </el-tab-pane>
        
        <!-- 收货地址 -->
        <el-tab-pane label="收货地址" name="address">
          <el-table 
            v-if="Array.isArray(userAddresses) && userAddresses.length > 0" 
            :data="userAddresses" 
            border 
            style="width: 100%"
          >
            <el-table-column prop="receiver" label="收件人" width="100" />
            <el-table-column prop="phone" label="手机号" width="120" />
            <el-table-column label="地址">
              <template #default="{ row }">
                {{ row.province }}{{ row.city }}{{ row.district }}{{ row.detail }}
              </template>
            </el-table-column>
            <el-table-column label="默认" width="80" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.isDefault === 1" type="success" size="small">默认</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无收货地址" />
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
    
    <!-- 创建用户对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建新用户"
      width="500px"
    >
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="createForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="createForm.realName" placeholder="请输入真实姓名（可选）" />
        </el-form-item>
        <el-form-item label="用户角色" prop="role">
          <el-select v-model="createForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" :value="1" />
            <el-option label="团长" :value="2" />
            <el-option label="管理员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="归属社区" prop="communityId">
          <el-select 
            v-model="createForm.communityId" 
            placeholder="请选择社区（可选）" 
            style="width: 100%" 
            clearable
            filterable
          >
            <el-option 
              v-for="community in communityList" 
              :key="community.communityId" 
              :label="community.communityName" 
              :value="community.communityId" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="微信OpenID" prop="wxOpenid">
          <el-input v-model="createForm.wxOpenid" placeholder="请输入微信OpenID（可选）" />
        </el-form-item>
        <el-form-item label="头像URL" prop="avatar">
          <div style="display: flex; gap: 8px; align-items: start; flex-direction: column;">
            <el-input v-model="createForm.avatar" placeholder="请输入头像URL（可选）" />
            <el-upload
              :action="uploadUrl"
              :headers="getUploadHeaders()"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :on-success="(response) => handleAvatarSuccess(response, 'create')"
              accept=".jpg,.jpeg,.png,.gif,.webp"
            >
              <el-button size="small" type="primary">
                <el-icon><Upload /></el-icon>
                上传头像
              </el-button>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="createLoading">
          确定创建
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑用户信息"
      width="500px"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="100px">
        <el-form-item label="用户ID">
          <el-input v-model="editForm.userId" disabled />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="editForm.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input 
            v-model="editForm.password" 
            type="password" 
            placeholder="不修改请留空" 
            show-password 
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="用户角色" prop="role">
          <el-select v-model="editForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" :value="1" />
            <el-option label="团长" :value="2" />
            <el-option label="管理员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="归属社区" prop="communityId">
          <el-select 
            v-model="editForm.communityId" 
            placeholder="请选择社区（可选）" 
            style="width: 100%" 
            clearable
            filterable
          >
            <el-option 
              v-for="community in communityList" 
              :key="community.communityId" 
              :label="community.communityName" 
              :value="community.communityId" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="微信OpenID" prop="wxOpenid">
          <el-input v-model="editForm.wxOpenid" placeholder="请输入微信OpenID（可选）" />
        </el-form-item>
        <el-form-item label="头像URL" prop="avatar">
          <div style="display: flex; gap: 8px; align-items: start; flex-direction: column;">
            <el-input v-model="editForm.avatar" placeholder="请输入头像URL（可选）" />
            <el-upload
              :action="uploadUrl"
              :headers="getUploadHeaders()"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :on-success="(response) => handleAvatarSuccess(response, 'edit')"
              accept=".jpg,.jpeg,.png,.gif,.webp"
            >
              <el-button size="small" type="primary">
                <el-icon><Upload /></el-icon>
                上传头像
              </el-button>
            </el-upload>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEdit" :loading="editLoading">
          保存修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { 
  searchUsers, 
  getUsersByRole, 
  getUserInfo, 
  changeUserStatus, 
  adminCreateUser, 
  updateUserInfo, 
  changeUserRole,
  getUserAddresses,
  getUserAccount,
  uploadAvatar
} from '../api/user'
import { getCommunityList } from '../api/leader'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload } from '@element-plus/icons-vue'

// 使用user store
const userStore = useUserStore()

// 文件上传相关
const uploadUrl = 'http://localhost:9000/api/upload/avatar'

// 获取上传headers（每次上传时动态获取最新token）
const getUploadHeaders = () => {
  // 从userStore或localStorage获取admin_token
  const token = userStore.token || localStorage.getItem('admin_token')
  
  if (!token) {
    ElMessage.error('未登录，请重新登录')
  }
  
  return {
    'Authorization': `Bearer ${token || ''}`
  }
}

const loading = ref(false)
const userList = ref([])
const searchKeyword = ref('')
const roleFilter = ref(0)
const statusFilter = ref(-1)
const detailDialogVisible = ref(false)
const currentUser = ref(null)
const detailActiveTab = ref('info')
const userAddresses = ref([])  // 确保初始化为空数组
const userAccount = ref(null)
const communityList = ref([])  // 社区列表

// 创建用户
const createDialogVisible = ref(false)
const createFormRef = ref(null)
const createLoading = ref(false)
const createForm = reactive({
  username: '',
  password: '',
  phone: '',
  realName: '',
  role: 1,
  wxOpenid: '',
  avatar: '',
  communityId: null
})

const createRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度2-50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度6-50个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { len: 11, message: '手机号必须为11位', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 编辑用户
const editDialogVisible = ref(false)
const editFormRef = ref(null)
const editLoading = ref(false)
const editForm = reactive({
  userId: null,
  username: '',
  password: '',
  phone: '',
  realName: '',
  role: 1,
  originalRole: 1,
  wxOpenid: '',
  avatar: '',
  communityId: null
})

const editRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { len: 11, message: '手机号必须为11位', trigger: 'blur' }
  ],
  password: [
    { min: 6, max: 50, message: '密码长度6-50个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    let res
    if (searchKeyword.value) {
      res = await searchUsers({ keyword: searchKeyword.value })
    } else if (roleFilter.value > 0) {
      res = await getUsersByRole(roleFilter.value)
    } else {
      res = await searchUsers({})
    }
    
    userList.value = res.data || []
    
    // 如果有状态筛选，进行过滤
    if (statusFilter.value >= 0) {
      userList.value = userList.value.filter(user => user.status === statusFilter.value)
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索用户
const handleSearch = () => {
  roleFilter.value = 0
  statusFilter.value = -1
  fetchUsers()
}

// 角色筛选
const handleRoleFilter = () => {
  searchKeyword.value = ''
  fetchUsers()
}

// 状态筛选
const handleStatusFilter = () => {
  fetchUsers()
}

// 获取角色名称
const getRoleName = (role) => {
  const roleMap = {
    1: '普通用户',
    2: '团长',
    3: '管理员'
  }
  return roleMap[role] || '未知'
}

// 获取角色标签类型
const getRoleType = (role) => {
  const typeMap = {
    1: 'info',
    2: 'warning',
    3: 'danger'
  }
  return typeMap[role] || 'info'
}

// 获取社区名称
const getCommunityName = (communityId) => {
  const community = communityList.value.find(c => c.communityId === communityId)
  return community ? community.communityName : '未知社区'
}

// 查看用户详情
const showUserDetail = async (userId) => {
  try {
    // 重置Tab
    detailActiveTab.value = 'info'
    
    // 获取用户基本信息
    const userRes = await getUserInfo(userId)
    currentUser.value = userRes.data
    
    // 获取用户地址列表
    try {
      const addressRes = await getUserAddresses(userId)
      userAddresses.value = addressRes.data || []
    } catch (err) {
      userAddresses.value = []
      console.error('获取地址列表失败:', err)
    }
    
    // 获取用户账户信息
    try {
      const accountRes = await getUserAccount(userId)
      userAccount.value = accountRes.data || null
    } catch (err) {
      userAccount.value = null
      console.error('获取账户信息失败:', err)
    }
    
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取用户详情失败')
    console.error('Error:', error)
  }
}

// 对话框关闭后清理数据
const handleDetailDialogClosed = () => {
  // 延迟清理，确保DOM已经完全销毁
  setTimeout(() => {
    currentUser.value = null
    userAddresses.value = []
    userAccount.value = null
  }, 100)
}

// 切换用户状态
const toggleUserStatus = async (user) => {
  const action = user.status === 1 ? '禁用' : '启用'
  const newStatus = user.status === 1 ? 0 : 1
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 ${user.username} 吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await changeUserStatus(user.userId, newStatus)
    
    ElMessage.success(`${action}成功`)
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  // 重置表单
  Object.assign(createForm, {
    username: '',
    password: '',
    phone: '',
    realName: '',
    role: 1,
    wxOpenid: '',
    avatar: '',
    communityId: null
  })
  createDialogVisible.value = true
}

// 处理创建用户
const handleCreate = async () => {
  if (!createFormRef.value) return
  
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      createLoading.value = true
      try {
        await adminCreateUser(createForm)
        ElMessage.success('创建用户成功')
        createDialogVisible.value = false
        fetchUsers()
      } catch (error) {
        ElMessage.error(error.message || '创建用户失败')
      } finally {
        createLoading.value = false
      }
    }
  })
}

// 显示编辑对话框
const showEditDialog = (user) => {
  Object.assign(editForm, {
    userId: user.userId,
    username: user.username,
    password: '',
    phone: user.phone,
    realName: user.realName || '',
    role: user.role,
    originalRole: user.role,
    wxOpenid: user.wxOpenid || '',
    avatar: user.avatar || '',
    communityId: user.communityId || null
  })
  editDialogVisible.value = true
}

// 处理编辑用户
const handleEdit = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editLoading.value = true
      try {
        // 1. 更新基本信息
        const updateData = {
          phone: editForm.phone,
          realName: editForm.realName,
          wxOpenid: editForm.wxOpenid,
          avatar: editForm.avatar,
          communityId: editForm.communityId
        }
        
        // 如果填写了新密码，则更新密码
        if (editForm.password) {
          updateData.password = editForm.password
        }
        
        await updateUserInfo(editForm.userId, updateData)
        
        // 2. 如果角色改变了，更新角色
        if (editForm.role !== editForm.originalRole) {
          await changeUserRole(editForm.userId, editForm.role)
        }
        
        ElMessage.success('更新用户信息成功')
        editDialogVisible.value = false
        fetchUsers()
      } catch (error) {
        ElMessage.error(error.message || '更新用户信息失败')
      } finally {
        editLoading.value = false
      }
    }
  })
}

// 获取社区列表
const fetchCommunityList = async () => {
  try {
    const res = await getCommunityList()
    communityList.value = res.data || []
  } catch (error) {
    console.error('获取社区列表失败:', error)
  }
}

// 上传头像前的验证
const beforeAvatarUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!isValidType) {
    ElMessage.error('仅支持jpg、png、gif、webp格式的图片')
    return false
  }
  
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过2MB')
    return false
  }
  
  return true
}

// 头像上传成功
const handleAvatarSuccess = (response, formType) => {
  if (response.code === 200) {
    if (formType === 'create') {
      createForm.avatar = response.data
    } else if (formType === 'edit') {
      editForm.avatar = response.data
    }
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

onMounted(() => {
  fetchUsers()
  fetchCommunityList()
})
</script>

<style scoped>
.user-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-row {
  margin-bottom: 20px;
}
</style>
