<template>
  <div class="community-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>社区管理</span>
          <div>
            <el-button type="primary" @click="showCreateDialog">
              <el-icon><Plus /></el-icon>
              创建社区
            </el-button>
            <el-button @click="fetchCommunities">刷新</el-button>
          </div>
        </div>
      </template>
      
      <!-- 筛选 -->
      <el-row :gutter="20" class="search-row">
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="状态筛选" @change="handleStatusFilter">
            <el-option label="全部状态" :value="-1" />
            <el-option label="正常运营" :value="1" />
            <el-option label="已关闭" :value="2" />
          </el-select>
        </el-col>
        
        <el-col :span="16">
          <el-text type="info" style="margin-left: 20px">
            共 {{ communityList.length }} 个社区
          </el-text>
        </el-col>
      </el-row>
      
      <!-- 社区列表表格 -->
      <el-table 
        :data="communityList" 
        v-loading="loading"
        border
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="communityId" label="社区ID" width="80" />
        <el-table-column prop="name" label="社区名称" width="150" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="经纬度" width="180">
          <template #default="{ row }">
            {{ row.longitude }}, {{ row.latitude }}
          </template>
        </el-table-column>
        <el-table-column label="服务半径" width="100">
          <template #default="{ row }">
            {{ row.serviceRadius }}米
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常运营' : '已关闭' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showDetailDialog(row)">
              详情
            </el-button>
            <el-button size="small" type="primary" @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="danger"
              @click="handleDelete(row)"
              v-if="row.status === 1"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 创建/编辑社区对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '创建社区' : '编辑社区'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="communityFormRef"
        :model="communityForm"
        :rules="communityRules"
        label-width="100px"
      >
        <el-form-item label="社区名称" prop="name">
          <el-input v-model="communityForm.name" placeholder="请输入社区名称" />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="省份" prop="province">
              <el-input v-model="communityForm.province" placeholder="如：北京市" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="城市" prop="city">
              <el-input v-model="communityForm.city" placeholder="如：北京市" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="区/县" prop="district">
              <el-input v-model="communityForm.district" placeholder="如：海淀区" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="详细地址" prop="address">
          <el-input 
            v-model="communityForm.address" 
            type="textarea"
            :rows="2"
            placeholder="请输入详细地址"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number 
                v-model="communityForm.longitude" 
                :precision="6"
                :min="-180"
                :max="180"
                :step="0.000001"
                controls-position="right"
                style="width: 100%"
                placeholder="经度 (Longitude)"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number 
                v-model="communityForm.latitude" 
                :precision="6"
                :min="-90"
                :max="90"
                :step="0.000001"
                controls-position="right"
                style="width: 100%"
                placeholder="纬度 (Latitude)"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="服务半径" prop="serviceRadius">
          <el-input-number 
            v-model="communityForm.serviceRadius" 
            :min="500"
            :max="10000"
            :step="100"
            controls-position="right"
            style="width: 100%"
          />
          <el-text type="info" size="small">单位：米，默认3000米（3公里）</el-text>
        </el-form-item>
        
        <el-form-item label="社区简介" prop="description">
          <el-input 
            v-model="communityForm.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入社区简介（选填）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 社区详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="社区详情"
      width="700px"
    >
      <el-descriptions :column="2" border v-if="currentCommunity">
        <el-descriptions-item label="社区ID">
          {{ currentCommunity.communityId }}
        </el-descriptions-item>
        <el-descriptions-item label="社区名称">
          {{ currentCommunity.name }}
        </el-descriptions-item>
        <el-descriptions-item label="省份">
          {{ currentCommunity.province || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="城市">
          {{ currentCommunity.city || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="区/县">
          {{ currentCommunity.district || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="详细地址" :span="2">
          {{ currentCommunity.address }}
        </el-descriptions-item>
        <el-descriptions-item label="经度">
          {{ currentCommunity.longitude }}
        </el-descriptions-item>
        <el-descriptions-item label="纬度">
          {{ currentCommunity.latitude }}
        </el-descriptions-item>
        <el-descriptions-item label="服务半径">
          {{ currentCommunity.serviceRadius }}米
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentCommunity.status === 1 ? 'success' : 'danger'">
            {{ currentCommunity.status === 1 ? '正常运营' : '已关闭' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="社区简介" :span="2">
          {{ currentCommunity.description || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(currentCommunity.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatDate(currentCommunity.updatedAt) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getCommunityList,
  getCommunitiesByStatus,
  createCommunity,
  updateCommunity,
  deleteCommunity
} from '../api/leader'

// 数据
const communityList = ref([])
const loading = ref(false)
const statusFilter = ref(-1)

// 对话框
const dialogVisible = ref(false)
const dialogMode = ref('create') // 'create' | 'edit'
const detailDialogVisible = ref(false)
const currentCommunity = ref(null)
const submitting = ref(false)

// 表单
const communityFormRef = ref(null)
const communityForm = ref({
  name: '',
  province: '',
  city: '',
  district: '',
  address: '',
  latitude: null,
  longitude: null,
  serviceRadius: 3000,
  description: ''
})

// 表单验证规则
const communityRules = {
  name: [
    { required: true, message: '请输入社区名称', trigger: 'blur' },
    { min: 2, max: 100, message: '社区名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 255, message: '地址长度在 5 到 255 个字符', trigger: 'blur' }
  ],
  latitude: [
    { required: true, message: '请输入纬度', trigger: 'blur' },
    { type: 'number', min: -90, max: 90, message: '纬度范围为 -90 到 90', trigger: 'blur' }
  ],
  longitude: [
    { required: true, message: '请输入经度', trigger: 'blur' },
    { type: 'number', min: -180, max: 180, message: '经度范围为 -180 到 180', trigger: 'blur' }
  ],
  serviceRadius: [
    { required: true, message: '请输入服务半径', trigger: 'blur' },
    { type: 'number', min: 500, max: 10000, message: '服务半径范围为 500 到 10000 米', trigger: 'blur' }
  ]
}

// 方法
const fetchCommunities = async () => {
  loading.value = true
  try {
    const res = await getCommunityList()
    if (res.code === 200) {
      communityList.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取社区列表失败')
    }
  } catch (error) {
    console.error('获取社区列表失败:', error)
    ElMessage.error('获取社区列表失败')
  } finally {
    loading.value = false
  }
}

const handleStatusFilter = async () => {
  if (statusFilter.value === -1) {
    fetchCommunities()
  } else {
    loading.value = true
    try {
      const res = await getCommunitiesByStatus(statusFilter.value)
      if (res.code === 200) {
        communityList.value = res.data || []
      } else {
        ElMessage.error(res.message || '筛选失败')
      }
    } catch (error) {
      console.error('筛选失败:', error)
      ElMessage.error('筛选失败')
    } finally {
      loading.value = false
    }
  }
}

const showCreateDialog = () => {
  dialogMode.value = 'create'
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  dialogMode.value = 'edit'
  currentCommunity.value = { ...row }
  communityForm.value = {
    name: row.name,
    province: row.province || '',
    city: row.city || '',
    district: row.district || '',
    address: row.address,
    latitude: row.latitude,
    longitude: row.longitude,
    serviceRadius: row.serviceRadius,
    description: row.description || ''
  }
  dialogVisible.value = true
}

const showDetailDialog = (row) => {
  currentCommunity.value = row
  detailDialogVisible.value = true
}

const handleSubmit = async () => {
  if (!communityFormRef.value) return
  
  await communityFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      let res
      if (dialogMode.value === 'create') {
        res = await createCommunity(communityForm.value)
      } else {
        res = await updateCommunity(currentCommunity.value.communityId, communityForm.value)
      }
      
      if (res.code === 200) {
        ElMessage.success(dialogMode.value === 'create' ? '创建成功' : '更新成功')
        dialogVisible.value = false
        fetchCommunities()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除社区"${row.name}"吗？删除后该社区将被标记为已关闭。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteCommunity(row.communityId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchCommunities()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const resetForm = () => {
  if (communityFormRef.value) {
    communityFormRef.value.resetFields()
  }
  communityForm.value = {
    name: '',
    province: '',
    city: '',
    district: '',
    address: '',
    latitude: null,
    longitude: null,
    serviceRadius: 3000,
    description: ''
  }
  currentCommunity.value = null
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 生命周期
onMounted(() => {
  fetchCommunities()
})
</script>

<style scoped>
.community-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-row {
  margin-bottom: 10px;
}
</style>

