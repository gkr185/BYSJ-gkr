<template>
  <div class="warehouse-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>仓库管理</span>
          <div>
            <el-button type="primary" @click="showCreateDialog">
              <el-icon><Plus /></el-icon>
              创建仓库
            </el-button>
            <el-button @click="fetchWarehouses">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 统计信息 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="8">
          <el-text>共 {{ warehouseList.length }} 个仓库</el-text>
        </el-col>
        <el-col :span="8">
          <el-text type="success">启用: {{ activeWarehouseCount }} 个</el-text>
        </el-col>
        <el-col :span="8">
          <el-text type="danger">禁用: {{ inactiveWarehouseCount }} 个</el-text>
        </el-col>
      </el-row>

      <!-- 仓库列表 -->
      <el-table
        :data="warehouseList"
        v-loading="loading"
        border
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="id" label="仓库ID" width="80" />
        <el-table-column prop="warehouseName" label="仓库名称" width="150">
          <template #default="{ row }">
            {{ row.warehouseName }}
            <el-tag v-if="row.isDefault === 1" type="danger" size="small" style="margin-left: 8px">
              默认
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" min-width="250" show-overflow-tooltip />
        <el-table-column label="经纬度" width="200">
          <template #default="{ row }">
            {{ row.longitude }}, {{ row.latitude }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="联系人" width="120">
          <template #default="{ row }">
            {{ row.contactPerson || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="联系电话" width="130">
          <template #default="{ row }">
            {{ row.contactPhone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="primary"
              @click="handleSetDefault(row)"
              v-if="row.isDefault !== 1 && row.status === 1"
            >
              设为默认
            </el-button>
            <el-button size="small" @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="danger"
              @click="handleDelete(row)"
              v-if="row.isDefault !== 1"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建/编辑仓库对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '创建仓库' : '编辑仓库'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="warehouseFormRef"
        :model="warehouseForm"
        :rules="warehouseRules"
        label-width="100px"
      >
        <el-form-item label="仓库名称" prop="warehouseName">
          <el-input v-model="warehouseForm.warehouseName" placeholder="请输入仓库名称" />
        </el-form-item>

        <el-form-item label="仓库地址" prop="address">
          <el-input 
            v-model="warehouseForm.address" 
            type="textarea" 
            :rows="2"
            placeholder="请输入仓库详细地址"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number 
                v-model="warehouseForm.longitude" 
                :precision="6"
                :min="0"
                :max="180"
                placeholder="经度"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number 
                v-model="warehouseForm.latitude" 
                :precision="6"
                :min="0"
                :max="90"
                placeholder="纬度"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="warehouseForm.contactPerson" placeholder="请输入联系人姓名" />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="warehouseForm.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="warehouseForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入仓库描述（可选）"
          />
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="warehouseForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="设为默认" prop="isDefault" v-if="dialogMode === 'create'">
          <el-switch v-model="isDefaultSwitch" />
          <el-text size="small" type="info" style="margin-left: 10px">
            {{ isDefaultSwitch ? '将设为默认仓库' : '不设为默认仓库' }}
          </el-text>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ dialogMode === 'create' ? '创建' : '保存' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { 
  getWarehouseList,
  createWarehouse,
  updateWarehouse,
  deleteWarehouse,
  setDefaultWarehouse
} from '@/api/delivery'

// 数据
const loading = ref(false)
const submitting = ref(false)
const warehouseList = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const isDefaultSwitch = ref(false)

// 表单
const warehouseFormRef = ref(null)
const warehouseForm = ref({
  warehouseName: '',
  address: '',
  longitude: null,
  latitude: null,
  contactPerson: '',
  contactPhone: '',
  description: '',
  status: 1,
  isDefault: 0
})

// 表单验证规则
const warehouseRules = {
  warehouseName: [
    { required: true, message: '请输入仓库名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入仓库地址', trigger: 'blur' }
  ],
  longitude: [
    { required: true, message: '请输入经度', trigger: 'blur' }
  ],
  latitude: [
    { required: true, message: '请输入纬度', trigger: 'blur' }
  ],
  contactPerson: [
    { min: 2, max: 20, message: '长度在2到20个字符', trigger: 'blur' }
  ],
  contactPhone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 计算属性
const activeWarehouseCount = computed(() => 
  warehouseList.value.filter(w => w.status === 1).length
)

const inactiveWarehouseCount = computed(() => 
  warehouseList.value.filter(w => w.status === 0).length
)

// 方法
const fetchWarehouses = async () => {
  try {
    loading.value = true
    const res = await getWarehouseList()
    
    if (res.code === 200) {
      warehouseList.value = res.data || []
    }
  } catch (error) {
    console.error('获取仓库列表失败:', error)
    ElMessage.error('获取仓库列表失败')
  } finally {
    loading.value = false
  }
}

const showCreateDialog = () => {
  dialogMode.value = 'create'
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  dialogMode.value = 'edit'
  warehouseForm.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    await warehouseFormRef.value.validate()

    submitting.value = true

    // 如果是创建模式且开启了默认开关
    if (dialogMode.value === 'create' && isDefaultSwitch.value) {
      warehouseForm.value.isDefault = 1
    }

    let res
    if (dialogMode.value === 'create') {
      res = await createWarehouse(warehouseForm.value)
    } else {
      res = await updateWarehouse(warehouseForm.value.id, warehouseForm.value)
    }

    if (res.code === 200) {
      ElMessage.success(dialogMode.value === 'create' ? '仓库创建成功' : '仓库更新成功')
      dialogVisible.value = false
      fetchWarehouses()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交失败:', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

const handleSetDefault = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要将"${row.warehouseName}"设为默认仓库吗？`,
      '设置默认仓库',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await setDefaultWarehouse(row.id)
    if (res.code === 200) {
      ElMessage.success('默认仓库设置成功')
      fetchWarehouses()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('设置默认仓库失败:', error)
      ElMessage.error('设置默认仓库失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除仓库"${row.warehouseName}"吗？此操作不可恢复！`,
      '删除仓库确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'error'
      }
    )

    const res = await deleteWarehouse(row.id)
    if (res.code === 200) {
      ElMessage.success('仓库删除成功')
      fetchWarehouses()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除仓库失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const resetForm = () => {
  warehouseForm.value = {
    warehouseName: '',
    address: '',
    longitude: null,
    latitude: null,
    contactPerson: '',
    contactPhone: '',
    description: '',
    status: 1,
    isDefault: 0
  }
  isDefaultSwitch.value = false
  warehouseFormRef.value?.resetFields()
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  fetchWarehouses()
})
</script>

<style scoped>
.warehouse-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-row {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>

