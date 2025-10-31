<template>
  <div class="category-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>分类管理</span>
          <div>
            <el-button type="primary" @click="showCreateDialog(null)">
              <el-icon><Plus /></el-icon>
              添加顶级分类
            </el-button>
            <el-button @click="fetchCategories">刷新</el-button>
          </div>
        </div>
      </template>
      
      <!-- 分类树形表格 -->
      <el-table
        :data="categoryTreeData"
        v-loading="loading"
        row-key="categoryId"
        border
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        style="width: 100%"
      >
        <el-table-column prop="categoryId" label="分类ID" width="100" />
        <el-table-column prop="categoryName" label="分类名称" width="200" />
        <el-table-column label="父分类" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.parentId === 0" type="info">顶级分类</el-tag>
            <span v-else>{{ getParentName(row.parentId) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100">
          <template #default="{ row }">
            <el-input-number
              v-model="row.sort"
              :min="0"
              size="small"
              @change="handleSortChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="商品数量" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.productCount || 0 }}个</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button 
              size="small" 
              type="primary" 
              @click="showCreateDialog(row)"
              v-if="row.parentId === 0"
            >
              添加子分类
            </el-button>
            <el-button size="small" @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button 
              size="small" 
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 创建/编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="resetForm"
    >
      <el-form :model="categoryForm" :rules="formRules" ref="categoryFormRef" label-width="100px">
        <el-form-item label="父分类">
          <el-tag v-if="categoryForm.parentId === 0" type="info">顶级分类</el-tag>
          <span v-else>{{ getParentName(categoryForm.parentId) }}</span>
        </el-form-item>
        
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="categoryForm.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        
        <el-form-item label="排序" prop="sort">
          <el-input-number 
            v-model="categoryForm.sort" 
            :min="0" 
            style="width: 100%;"
          />
          <div class="form-tip">数值越小越靠前</div>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="categoryForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import {
  getCategoryTree,
  getCategoryList,
  createCategory,
  updateCategory,
  deleteCategory,
  updateCategorySort
} from '../api/product'

// 分类树数据
const categoryTreeData = ref([])
const categoryList = ref([])
const loading = ref(false)

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加分类')
const isEdit = ref(false)
const submitLoading = ref(false)

// 分类表单
const categoryFormRef = ref(null)
const categoryForm = reactive({
  categoryId: null,
  parentId: 0,
  categoryName: '',
  sort: 0,
  status: 1
})

// 表单校验规则
const formRules = {
  categoryName: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 获取父分类名称
const getParentName = (parentId) => {
  if (parentId === 0) return '顶级分类'
  const parent = categoryList.value.find(c => c.categoryId === parentId)
  return parent ? parent.categoryName : '未知'
}

// 加载分类列表（用于获取父分类名称）
const fetchCategoryList = async () => {
  try {
    const res = await getCategoryList()
    if (res.code === 200) {
      categoryList.value = res.data
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 加载分类树
const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await getCategoryTree()
    if (res.code === 200) {
      categoryTreeData.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取分类列表失败')
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = (parentRow) => {
  isEdit.value = false
  if (parentRow) {
    dialogTitle.value = `添加子分类（${parentRow.categoryName}）`
    categoryForm.parentId = parentRow.categoryId
  } else {
    dialogTitle.value = '添加顶级分类'
    categoryForm.parentId = 0
  }
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑分类'
  Object.assign(categoryForm, {
    categoryId: row.categoryId,
    parentId: row.parentId,
    categoryName: row.categoryName,
    sort: row.sort,
    status: row.status
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  await categoryFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      const data = {
        parentId: categoryForm.parentId,
        categoryName: categoryForm.categoryName,
        sort: categoryForm.sort,
        status: categoryForm.status
      }
      
      let res
      if (isEdit.value) {
        res = await updateCategory(categoryForm.categoryId, data)
      } else {
        res = await createCategory(data)
      }
      
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        fetchCategories()
        fetchCategoryList()
      }
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  categoryFormRef.value?.resetFields()
  Object.assign(categoryForm, {
    categoryId: null,
    parentId: 0,
    categoryName: '',
    sort: 0,
    status: 1
  })
}

// 调整排序
const handleSortChange = async (row) => {
  try {
    const res = await updateCategorySort(row.categoryId, row.sort)
    if (res.code === 200) {
      ElMessage.success('排序更新成功')
      fetchCategories()
    }
  } catch (error) {
    ElMessage.error('排序更新失败')
    fetchCategories() // 恢复原值
  }
}

// 删除分类
const handleDelete = async (row) => {
  // 检查是否有子分类
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该分类下有子分类，请先删除子分类')
    return
  }
  
  // 检查是否有商品
  if (row.productCount > 0) {
    ElMessage.warning('该分类下有商品，无法删除')
    return
  }
  
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？删除后不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    const res = await deleteCategory(row.categoryId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchCategories()
      fetchCategoryList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  fetchCategories()
  fetchCategoryList()
})
</script>

<style scoped>
.category-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>

