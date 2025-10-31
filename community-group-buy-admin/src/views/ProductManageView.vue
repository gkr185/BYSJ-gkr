<template>
  <div class="product-manage">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">商品总数</div>
            <div class="stat-value">{{ statistics.totalProducts || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">上架商品</div>
            <div class="stat-value success">{{ statistics.onSaleProducts || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">下架商品</div>
            <div class="stat-value warning">{{ statistics.offSaleProducts || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">低库存商品</div>
            <div class="stat-value danger">{{ statistics.lowStockProducts || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 商品列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品管理</span>
          <div>
            <el-button type="primary" @click="showCreateDialog">
              <el-icon><Plus /></el-icon>
              添加商品
            </el-button>
            <el-button @click="fetchProducts">刷新</el-button>
          </div>
        </div>
      </template>
      
      <!-- 搜索和筛选 -->
      <el-row :gutter="20" class="search-row">
        <el-col :span="6">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索商品名称"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button icon="Search" @click="handleSearch" />
            </template>
          </el-input>
        </el-col>
        
        <el-col :span="4">
          <el-select v-model="categoryFilter" placeholder="分类筛选" @change="handleCategoryFilter" clearable>
            <el-option label="全部分类" :value="null" />
            <el-option 
              v-for="cat in categoryList" 
              :key="cat.categoryId" 
              :label="cat.categoryName" 
              :value="cat.categoryId" 
            />
          </el-select>
        </el-col>
        
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="状态筛选" @change="handleStatusFilter" clearable>
            <el-option label="全部状态" :value="null" />
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-col>
      </el-row>
      
      <!-- 商品表格 -->
      <el-table 
        :data="productList" 
        v-loading="loading"
        border
        style="width: 100%; margin-top: 20px"
      >
        <el-table-column prop="productId" label="商品ID" width="80" />
        <el-table-column label="商品图片" width="100">
          <template #default="{ row }">
            <el-image 
              :src="row.coverImg" 
              fit="cover" 
              style="width: 60px; height: 60px; border-radius: 4px;"
              :preview-src-list="[row.coverImg]"
            />
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" width="200" />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            <div>原价: ¥{{ row.price }}</div>
            <div style="color: #f56c6c;">拼团: ¥{{ row.groupPrice }}</div>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="100">
          <template #default="{ row }">
            <el-tag :type="row.stock <= 10 ? 'danger' : 'success'">
              {{ row.stock }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showProductDetail(row)">
              详情
            </el-button>
            <el-button size="small" type="primary" @click="showEditDialog(row)">
              编辑
            </el-button>
            <el-button 
              size="small" 
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleProductStatus(row)"
            >
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" @click="showStockDialog(row)">
              调库存
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
      
      <!-- 分页 -->
      <el-pagination
        :current-page="pagination.page"
        :page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 创建/编辑商品对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="resetForm"
    >
      <el-form :model="productForm" :rules="formRules" ref="productFormRef" label-width="100px">
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="productForm.productName" placeholder="请输入商品名称" />
        </el-form-item>
        
        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类" style="width: 100%;">
            <el-option 
              v-for="cat in categoryList" 
              :key="cat.categoryId" 
              :label="cat.categoryName" 
              :value="cat.categoryId" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="商品封面" prop="coverImg">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :http-request="handleUpload"
            :before-upload="beforeUpload"
          >
            <img v-if="productForm.coverImg" :src="productForm.coverImg" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">支持jpg、png格式，大小不超过5MB</div>
        </el-form-item>
        
        <el-form-item label="商品详情" prop="detail">
          <el-input 
            v-model="productForm.detail" 
            type="textarea" 
            :rows="4"
            placeholder="请输入商品详情"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="原价" prop="price">
              <el-input-number 
                v-model="productForm.price" 
                :precision="2" 
                :step="0.1" 
                :min="0" 
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="拼团价" prop="groupPrice">
              <el-input-number 
                v-model="productForm.groupPrice" 
                :precision="2" 
                :step="0.1" 
                :min="0" 
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="初始库存" prop="stock" v-if="!isEdit">
          <el-input-number 
            v-model="productForm.stock" 
            :min="0" 
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 调整库存对话框 -->
    <el-dialog
      v-model="stockDialogVisible"
      title="调整库存"
      width="400px"
    >
      <el-form label-width="100px">
        <el-form-item label="商品名称">
          <span>{{ currentProduct.productName }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <span>{{ currentProduct.stock }}</span>
        </el-form-item>
        <el-form-item label="调整数量">
          <el-input-number 
            v-model="stockAdjustment" 
            placeholder="正数增加，负数减少"
            style="width: 100%;"
          />
          <div class="stock-tip">正数增加库存，负数减少库存</div>
        </el-form-item>
        <el-form-item label="调整后库存">
          <span :style="{ color: getNewStock() < 0 ? 'red' : 'green' }">
            {{ getNewStock() }}
          </span>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStockSubmit" :disabled="getNewStock() < 0">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 商品详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="商品详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="商品ID">{{ detailProduct.productId }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ detailProduct.productName }}</el-descriptions-item>
        <el-descriptions-item label="商品分类">
          {{ getCategoryName(detailProduct.categoryId) }}
        </el-descriptions-item>
        <el-descriptions-item label="商品状态">
          <el-tag :type="detailProduct.status === 1 ? 'success' : 'info'">
            {{ detailProduct.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="原价">¥{{ detailProduct.price }}</el-descriptions-item>
        <el-descriptions-item label="拼团价">¥{{ detailProduct.groupPrice }}</el-descriptions-item>
        <el-descriptions-item label="库存">{{ detailProduct.stock }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailProduct.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailProduct.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="商品封面" :span="2">
          <el-image :src="detailProduct.coverImg" style="width: 200px; height: 200px;" fit="cover" />
        </el-descriptions-item>
        <el-descriptions-item label="商品详情" :span="2">
          <div style="white-space: pre-wrap;">{{ detailProduct.detail }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import {
  getAdminProductList,
  getProductDetail,
  createProduct,
  updateProduct,
  deleteProduct,
  updateProductStatus,
  adjustProductStock,
  uploadProductImage,
  getProductStatistics,
  getCategoryList
} from '../api/product'

// 统计数据
const statistics = ref({
  totalProducts: 0,
  onSaleProducts: 0,
  offSaleProducts: 0,
  lowStockProducts: 0
})

// 商品列表
const productList = ref([])
const loading = ref(false)

// 分类列表
const categoryList = ref([])

// 搜索和筛选
const searchKeyword = ref('')
const categoryFilter = ref(null)
const statusFilter = ref(null)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加商品')
const isEdit = ref(false)
const submitLoading = ref(false)

// 商品表单
const productFormRef = ref(null)
const productForm = reactive({
  productId: null,
  productName: '',
  categoryId: null,
  coverImg: '',
  detail: '',
  price: 0,
  groupPrice: 0,
  stock: 0
})

// 表单校验规则
const formRules = {
  productName: [
    { required: true, message: '请输入商品名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  coverImg: [
    { required: true, message: '请上传商品封面', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入原价', trigger: 'blur' }
  ],
  groupPrice: [
    { required: true, message: '请输入拼团价', trigger: 'blur' }
  ]
}

// 库存调整对话框
const stockDialogVisible = ref(false)
const currentProduct = ref({})
const stockAdjustment = ref(0)

// 商品详情对话框
const detailDialogVisible = ref(false)
const detailProduct = ref({})

// 加载统计数据
const fetchStatistics = async () => {
  try {
    const res = await getProductStatistics()
    if (res.code === 200) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 加载分类列表
const fetchCategories = async () => {
  try {
    const res = await getCategoryList()
    if (res.code === 200) {
      categoryList.value = res.data
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  const category = categoryList.value.find(c => c.categoryId === categoryId)
  return category ? category.categoryName : '未分类'
}

// 加载商品列表
const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1, // 后端从0开始
      size: pagination.size
    }
    
    const res = await getAdminProductList(params)
    if (res.code === 200) {
      productList.value = res.data.content
      pagination.total = res.data.totalElements
    }
  } catch (error) {
    ElMessage.error('获取商品列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchProducts()
}

// 分类筛选
const handleCategoryFilter = () => {
  pagination.page = 1
  fetchProducts()
}

// 状态筛选
const handleStatusFilter = () => {
  pagination.page = 1
  fetchProducts()
}

// 分页
const handleSizeChange = (val) => {
  pagination.size = val
  pagination.page = 1
  fetchProducts()
}

const handleCurrentChange = (val) => {
  pagination.page = val
  fetchProducts()
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '添加商品'
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑商品'
  Object.assign(productForm, row)
  dialogVisible.value = true
}

// 显示商品详情
const showProductDetail = async (row) => {
  try {
    const res = await getProductDetail(row.productId)
    if (res.code === 200) {
      detailProduct.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取商品详情失败')
  }
}

// 上传图片前校验
const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只支持jpg、png格式的图片！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB！')
    return false
  }
  return true
}

// 自定义上传
const handleUpload = async (options) => {
  const loadingMsg = ElMessage({
    message: '图片上传中...',
    type: 'info',
    duration: 0
  })
  
  try {
    console.log('开始上传文件:', options.file.name)
    const res = await uploadProductImage(options.file)
    console.log('上传响应:', res)
    
    loadingMsg.close()
    
    if (res.code === 200) {
      // 兼容 data 和 message 两种返回格式
      const imageUrl = res.data || res.message
      if (imageUrl) {
        productForm.coverImg = imageUrl
        console.log('图片URL已设置:', productForm.coverImg)
        ElMessage.success('图片上传成功！')
      } else {
        ElMessage.error('上传失败：未返回图片URL')
      }
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (error) {
    loadingMsg.close()
    console.error('上传失败:', error)
    ElMessage.error(error.message || '上传失败，请重试')
  }
}

// 提交表单
const handleSubmit = async () => {
  await productFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      const data = {
        categoryId: productForm.categoryId,
        productName: productForm.productName,
        coverImg: productForm.coverImg,
        detail: productForm.detail,
        price: productForm.price,
        groupPrice: productForm.groupPrice,
        stock: productForm.stock
      }
      
      let res
      if (isEdit.value) {
        res = await updateProduct(productForm.productId, data)
      } else {
        res = await createProduct(data)
      }
      
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        fetchProducts()
        fetchStatistics()
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
  productFormRef.value?.resetFields()
  Object.assign(productForm, {
    productId: null,
    productName: '',
    categoryId: null,
    coverImg: '',
    detail: '',
    price: 0,
    groupPrice: 0,
    stock: 0
  })
}

// 上下架
const toggleProductStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'
  
  try {
    await ElMessageBox.confirm(`确定要${action}该商品吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await updateProductStatus(row.productId, newStatus)
    if (res.code === 200) {
      ElMessage.success(`${action}成功`)
      fetchProducts()
      fetchStatistics()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 显示库存调整对话框
const showStockDialog = (row) => {
  currentProduct.value = row
  stockAdjustment.value = 0
  stockDialogVisible.value = true
}

// 计算调整后库存
const getNewStock = () => {
  return (currentProduct.value.stock || 0) + (stockAdjustment.value || 0)
}

// 提交库存调整
const handleStockSubmit = async () => {
  if (!stockAdjustment.value) {
    ElMessage.warning('请输入调整数量')
    return
  }
  
  try {
    const res = await adjustProductStock(currentProduct.value.productId, stockAdjustment.value)
    if (res.code === 200) {
      ElMessage.success('库存调整成功')
      stockDialogVisible.value = false
      fetchProducts()
      fetchStatistics()
    }
  } catch (error) {
    ElMessage.error('库存调整失败')
  }
}

// 删除商品
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？删除后不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    const res = await deleteProduct(row.productId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchProducts()
      fetchStatistics()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 初始化
onMounted(() => {
  fetchStatistics()
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped>
.product-manage {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.danger {
  color: #f56c6c;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-row {
  margin-bottom: 20px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 178px;
  height: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.stock-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>

