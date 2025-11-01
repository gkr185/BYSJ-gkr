<template>
  <div class="create-activity-wrapper">
    <div class="create-activity-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <el-button :icon="ArrowLeft" @click="goBack" class="back-button">返回</el-button>
        <div class="header-content">
          <h2>{{ isEdit ? '编辑活动' : '创建拼团活动' }}</h2>
          <p class="subtitle">{{ isEdit ? '修改活动信息' : '选择商品并设置拼团规则' }}</p>
        </div>
      </div>

      <!-- 步骤条 -->
      <el-card class="steps-card" shadow="hover" v-if="!isEdit">
        <el-steps :active="currentStep" finish-status="success" align-center>
          <el-step title="选择商品" />
          <el-step title="设置价格" />
          <el-step title="配置规则" />
          <el-step title="确认创建" />
        </el-steps>
      </el-card>

      <!-- 主内容区 -->
      <el-row :gutter="20">
        <!-- 左侧：表单区 -->
        <el-col :span="16">
          <el-card shadow="hover" class="form-card">
            <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" size="large">
              <!-- 步骤1: 选择商品 -->
              <div v-show="currentStep === 0 || isEdit">
                <div class="section-title">
                  <el-icon><ShoppingBag /></el-icon>
                  <span>选择商品</span>
                </div>

                <!-- 商品搜索 -->
                <el-form-item label="搜索商品">
                  <el-input
                    v-model="searchKeyword"
                    placeholder="输入商品名称搜索"
                    clearable
                    @input="handleSearch"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                </el-form-item>

                <!-- 商品列表 -->
                <el-form-item label="选择商品" prop="productId">
                  <el-skeleton v-if="loadingProducts" :rows="3" animated />
                  <div v-else-if="filteredProducts.length > 0" class="product-list">
                    <div
                      v-for="product in filteredProducts"
                      :key="product.productId"
                      class="product-item"
                      :class="{ selected: form.productId === product.productId }"
                      @click="selectProduct(product)"
                    >
                      <el-image
                        :src="product.coverImg"
                        fit="cover"
                        class="product-image"
                      >
                        <template #error>
                          <div class="image-error">
                            <el-icon><Picture /></el-icon>
                          </div>
                        </template>
                      </el-image>
                      <div class="product-info">
                        <h4>{{ product.productName }}</h4>
                        <div class="product-meta">
                          <span class="price">¥{{ product.price }}</span>
                          <el-tag :type="product.stock > 50 ? 'success' : 'warning'" size="small">
                            库存: {{ product.stock }}
                          </el-tag>
                          <el-tag type="success" size="small">
                            上架中
                          </el-tag>
                        </div>
                      </div>
                      <el-icon v-if="form.productId === product.productId" class="check-icon" color="#67c23a">
                        <CircleCheckFilled />
                      </el-icon>
                    </div>
                  </div>
                  <el-empty v-else description="暂无可选商品">
                    <template #description>
                      <p>暂无上架的商品</p>
                      <p style="font-size: 12px; color: #909399; margin-top: 8px;">
                        请先在商品管理中添加并上架商品
                      </p>
                    </template>
                  </el-empty>
                </el-form-item>

                <div class="step-actions" v-if="!isEdit">
                  <el-button type="primary" @click="nextStep" :disabled="!form.productId" size="large">
                    下一步
                  </el-button>
                </div>
              </div>

              <!-- 步骤2: 设置价格 -->
              <div v-show="currentStep === 1 || isEdit">
                <div class="section-title">
                  <el-icon><Money /></el-icon>
                  <span>设置拼团价</span>
                </div>

                <!-- 商品信息展示 -->
                <div v-if="selectedProduct" class="selected-product-info">
                  <el-image :src="selectedProduct.coverImg" fit="cover" class="product-thumb" />
                  <div class="product-detail">
                    <h4>{{ selectedProduct.productName }}</h4>
                    <div class="product-price-info">
                      <span class="label">商品原价：</span>
                      <span class="original-price">¥{{ selectedProduct.price }}</span>
                    </div>
                  </div>
                </div>

                <el-form-item label="拼团价格" prop="groupPrice">
                  <el-input-number
                    v-model="form.groupPrice"
                    :precision="2"
                    :step="0.1"
                    :min="0.01"
                    :max="selectedProduct ? selectedProduct.price - 0.01 : 99999"
                    style="width: 100%;"
                    @change="calculateDiscount"
                  >
                    <template #prefix>¥</template>
                  </el-input-number>
                  <div class="price-tips">
                    <div v-if="selectedProduct" class="tip-item">
                      <span>建议价格：¥{{ suggestedPrice }}</span>
                      <el-button link type="primary" @click="useSuggestedPrice">采用建议</el-button>
                    </div>
                    <div v-if="discountInfo" class="tip-item highlight">
                      <el-icon><TrendCharts /></el-icon>
                      <span>{{ discountInfo }}</span>
                    </div>
                  </div>
                </el-form-item>

                <div class="step-actions" v-if="!isEdit">
                  <el-button @click="prevStep" size="large">上一步</el-button>
                  <el-button type="primary" @click="nextStep" :disabled="!form.groupPrice" size="large">
                    下一步
                  </el-button>
                </div>
              </div>

              <!-- 步骤3: 配置规则 -->
              <div v-show="currentStep === 2 || isEdit">
                <div class="section-title">
                  <el-icon><Setting /></el-icon>
                  <span>配置拼团规则</span>
                </div>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="成团人数" prop="requiredNum">
                      <el-select v-model="form.requiredNum" style="width: 100%;">
                        <el-option :value="2">
                          <span>2人团</span>
                          <el-tag type="success" size="small" style="float: right;">推荐</el-tag>
                        </el-option>
                        <el-option :value="3">
                          <span>3人团</span>
                          <el-tag type="warning" size="small" style="float: right;">热门</el-tag>
                        </el-option>
                        <el-option :value="5" label="5人团" />
                        <el-option :value="10" label="10人团" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="人数限制">
                      <el-input-number
                        v-model="form.maxNum"
                        :min="0"
                        style="width: 100%;"
                        placeholder="0表示无限制"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item label="活动时间">
                  <!-- 快捷时间 -->
                  <div class="time-presets">
                    <el-radio-group v-model="timePreset" @change="handleTimePreset" size="small">
                      <el-radio-button label="custom">自定义</el-radio-button>
                      <el-radio-button label="3days">3天</el-radio-button>
                      <el-radio-button label="7days">7天</el-radio-button>
                      <el-radio-button label="30days">30天</el-radio-button>
                    </el-radio-group>
                  </div>
                </el-form-item>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="开始时间" prop="startTime">
                      <el-date-picker
                        v-model="form.startTime"
                        type="datetime"
                        placeholder="选择开始时间"
                        style="width: 100%;"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DDTHH:mm:ss"
                        :disabled-date="disabledStartDate"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="结束时间" prop="endTime">
                      <el-date-picker
                        v-model="form.endTime"
                        type="datetime"
                        placeholder="选择结束时间"
                        style="width: 100%;"
                        format="YYYY-MM-DD HH:mm:ss"
                        value-format="YYYY-MM-DDTHH:mm:ss"
                        :disabled-date="disabledEndDate"
                      />
                    </el-form-item>
                  </el-col>
                </el-row>

                <!-- 编辑时可修改状态 -->
                <el-form-item label="活动状态" v-if="isEdit">
                  <el-radio-group v-model="form.status">
                    <el-radio :value="0">未开始</el-radio>
                    <el-radio :value="1">进行中</el-radio>
                    <el-radio :value="2">已结束</el-radio>
                  </el-radio-group>
                </el-form-item>

                <div class="step-actions" v-if="!isEdit">
                  <el-button @click="prevStep" size="large">上一步</el-button>
                  <el-button type="primary" @click="nextStep" size="large">
                    下一步
                  </el-button>
                </div>
              </div>

              <!-- 步骤4: 确认创建 -->
              <div v-show="currentStep === 3 || isEdit">
                <div class="section-title">
                  <el-icon><Check /></el-icon>
                  <span>确认信息</span>
                </div>

                <div class="confirm-info">
                  <el-descriptions :column="2" border>
                    <el-descriptions-item label="商品名称" :span="2">
                      {{ selectedProduct?.productName || '-' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="商品原价">
                      ¥{{ selectedProduct?.price || 0 }}
                    </el-descriptions-item>
                    <el-descriptions-item label="拼团价格">
                      <span style="color: #f56c6c; font-weight: bold;">¥{{ form.groupPrice || 0 }}</span>
                    </el-descriptions-item>
                    <el-descriptions-item label="成团人数">
                      {{ form.requiredNum }}人
                    </el-descriptions-item>
                    <el-descriptions-item label="人数限制">
                      {{ form.maxNum ? `${form.maxNum}人` : '无限制' }}
                    </el-descriptions-item>
                    <el-descriptions-item label="开始时间" :span="2">
                      {{ formatTime(form.startTime) }}
                    </el-descriptions-item>
                    <el-descriptions-item label="结束时间" :span="2">
                      {{ formatTime(form.endTime) }}
                    </el-descriptions-item>
                  </el-descriptions>
                </div>

                <div class="step-actions">
                  <el-button @click="prevStep" size="large" v-if="!isEdit">上一步</el-button>
                  <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
                    <el-icon><Check /></el-icon>
                    {{ isEdit ? '保存修改' : '确认创建' }}
                  </el-button>
                </div>
              </div>
            </el-form>
          </el-card>
        </el-col>

        <!-- 右侧：预览区 -->
        <el-col :span="8">
          <div class="preview-area">
            <!-- 活动预览 -->
            <el-card shadow="always" class="preview-card">
              <template #header>
                <div class="preview-header">
                  <el-icon><View /></el-icon>
                  <span>活动预览</span>
                </div>
              </template>
              <div v-if="selectedProduct" class="activity-preview">
                <div class="preview-image">
                  <el-image :src="selectedProduct.coverImg" fit="cover" />
                  <div class="preview-badge">拼团价</div>
                </div>
                <h3 class="preview-title">{{ selectedProduct.productName }}</h3>
                <div class="preview-price">
                  <span class="group-price">¥{{ form.groupPrice || '0.00' }}</span>
                  <span class="original-price">¥{{ selectedProduct.price }}</span>
                </div>
                <div class="preview-meta">
                  <el-tag type="warning" size="small">{{ form.requiredNum }}人成团</el-tag>
                  <el-tag v-if="form.maxNum" type="info" size="small">限{{ form.maxNum }}人</el-tag>
                  <el-tag v-else type="success" size="small">无限制</el-tag>
                </div>
                <div class="preview-time" v-if="form.endTime">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatTime(form.endTime) }}截止</span>
                </div>
              </div>
              <el-empty v-else description="选择商品后显示预览" :image-size="100" />
            </el-card>

            <!-- 数据预估 -->
            <el-card v-if="selectedProduct && form.groupPrice" shadow="hover" class="estimate-card">
              <div class="estimate-title">
                <el-icon><DataAnalysis /></el-icon>
                <span>数据预估</span>
              </div>
              <div class="estimate-list">
                <div class="estimate-item">
                  <span class="label">单品优惠</span>
                  <span class="value">¥{{ savings }}</span>
                </div>
                <div class="estimate-item">
                  <span class="label">优惠力度</span>
                  <span class="value highlight">{{ discountInfo }}</span>
                </div>
                <div class="estimate-item">
                  <span class="label">预估佣金</span>
                  <span class="value commission">¥{{ estimatedCommission }}</span>
                </div>
              </div>
            </el-card>

            <!-- 注意事项 -->
            <el-card shadow="hover" class="tips-card">
              <div class="tips-title">
                <el-icon><Warning /></el-icon>
                <span>注意事项</span>
              </div>
              <ul class="tips-list">
                <li>拼团价必须低于商品原价</li>
                <li>成团人数建议设置为2-5人</li>
                <li>活动时间不宜过短或过长</li>
                <li>创建后活动将立即生效</li>
              </ul>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, ShoppingBag, Search, Picture, CircleCheckFilled,
  Money, TrendCharts, Setting, Check, View, Clock, DataAnalysis,
  Warning
} from '@element-plus/icons-vue'
import { getProductList } from '@/api/product'
import { createActivity, updateActivity, getActivityDetail } from '@/api/groupbuy'

const route = useRoute()
const router = useRouter()

// 状态
const loadingProducts = ref(false)
const submitting = ref(false)
const currentStep = ref(0)
const isEdit = computed(() => !!route.params.id)

// 数据
const productList = ref([])
const selectedProduct = ref(null)
const searchKeyword = ref('')
const timePreset = ref('7days')

// 表单
const formRef = ref(null)
const form = reactive({
  activityId: null,
  productId: null,
  groupPrice: null,
  requiredNum: 3,
  maxNum: null,
  startTime: '',
  endTime: '',
  status: 1
})

// 表单验证规则
const rules = {
  productId: [
    { required: true, message: '请选择商品', trigger: 'change' }
  ],
  groupPrice: [
    { required: true, message: '请输入拼团价格', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value && selectedProduct.value && value >= selectedProduct.value.price) {
          callback(new Error('拼团价必须低于商品原价'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  requiredNum: [
    { required: true, message: '请选择成团人数', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (rule, value, callback) => {
        if (value && form.startTime && new Date(value) <= new Date(form.startTime)) {
          callback(new Error('结束时间必须晚于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 计算属性
const filteredProducts = computed(() => {
  if (!searchKeyword.value) return productList.value
  return productList.value.filter(p =>
    p.productName.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

const suggestedPrice = computed(() => {
  if (!selectedProduct.value) return 0
  return (selectedProduct.value.price * 0.7).toFixed(2)
})

const discountInfo = computed(() => {
  if (!selectedProduct.value || !form.groupPrice) return ''
  const discount = ((selectedProduct.value.price - form.groupPrice) / selectedProduct.value.price * 10).toFixed(1)
  return `${discount}折优惠`
})

const savings = computed(() => {
  if (!selectedProduct.value || !form.groupPrice) return '0.00'
  return (selectedProduct.value.price - form.groupPrice).toFixed(2)
})

const estimatedCommission = computed(() => {
  if (!form.groupPrice) return '0.00'
  return (form.groupPrice * 0.05).toFixed(2)
})

// 方法
const goBack = () => {
  router.back()
}

const fetchProducts = async () => {
  loadingProducts.value = true
  try {
    console.log('开始获取商品列表...')
    // request拦截器已经解包了res.data，所以直接得到的就是数据对象
    const data = await getProductList({ page: 0, size: 100 })
    console.log('商品列表响应:', data)
    
    // 检查数据结构
    if (data && data.content) {
      const allProducts = data.content
      console.log('获取到商品总数:', allProducts.length)
      
      // 只显示上架的商品
      const onSaleProducts = allProducts.filter(p => p.status === 1)
      console.log('上架商品数:', onSaleProducts.length)
      
      productList.value = onSaleProducts
      
      if (onSaleProducts.length === 0 && allProducts.length > 0) {
        ElMessage.warning(`共有${allProducts.length}个商品，但都未上架。请先在商品管理中上架商品。`)
      } else if (onSaleProducts.length === 0) {
        ElMessage.warning('暂无商品数据，请先在商品管理中添加商品。')
      }
    } else {
      console.warn('响应数据格式不正确:', data)
      ElMessage.warning('商品数据格式不正确')
      productList.value = []
    }
  } catch (error) {
    console.error('获取商品列表异常:', error)
    ElMessage.error('获取商品列表失败：' + (error.message || '网络错误'))
  } finally {
    loadingProducts.value = false
  }
}

const handleSearch = () => {
  // 搜索过滤已通过计算属性实现
}

const selectProduct = (product) => {
  form.productId = product.productId
  selectedProduct.value = product
  
  // 自动设置建议价格（如果尚未设置）
  if (!form.groupPrice) {
    form.groupPrice = parseFloat(suggestedPrice.value)
  }
}

const useSuggestedPrice = () => {
  form.groupPrice = parseFloat(suggestedPrice.value)
}

const calculateDiscount = () => {
  // 触发计算属性更新
}

const handleTimePreset = (preset) => {
  if (preset === 'custom') return
  
  const now = new Date()
  const start = new Date(now)
  start.setHours(0, 0, 0, 0)
  
  let end = new Date(start)
  
  switch (preset) {
    case '3days':
      end.setDate(end.getDate() + 3)
      break
    case '7days':
      end.setDate(end.getDate() + 7)
      break
    case '30days':
      end.setDate(end.getDate() + 30)
      break
  }
  
  end.setHours(23, 59, 59, 0)
  
  form.startTime = start.toISOString().slice(0, 19)
  form.endTime = end.toISOString().slice(0, 19)
}

const disabledStartDate = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date < today
}

const disabledEndDate = (date) => {
  if (!form.startTime) return false
  return date <= new Date(form.startTime)
}

const nextStep = async () => {
  // 验证当前步骤
  let fieldsToValidate = []
  
  switch (currentStep.value) {
    case 0:
      fieldsToValidate = ['productId']
      break
    case 1:
      fieldsToValidate = ['groupPrice']
      break
    case 2:
      fieldsToValidate = ['requiredNum', 'startTime', 'endTime']
      break
  }
  
  if (fieldsToValidate.length > 0) {
    try {
      await formRef.value.validateField(fieldsToValidate)
      currentStep.value++
    } catch (error) {
      console.log('验证失败:', error)
    }
  } else {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    const data = {
      productId: form.productId,
      groupPrice: form.groupPrice,
      requiredNum: form.requiredNum,
      maxNum: form.maxNum || null,
      startTime: form.startTime,
      endTime: form.endTime
    }
    
    console.log('提交数据:', data)
    
    // request拦截器已经解包了res.data，所以直接得到的就是数据对象
    if (isEdit.value) {
      data.status = form.status
      await updateActivity(form.activityId, data)
      ElMessage.success('活动更新成功')
      router.push('/leader/activities')
    } else {
      await createActivity(data)
      ElMessage.success('活动创建成功')
      router.push('/leader/activities')
    }
  } catch (error) {
    console.error('提交失败:', error)
    if (error !== 'cancel') {
      ElMessage.error(isEdit.value ? '活动更新失败' : '活动创建失败')
    }
  } finally {
    submitting.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

const loadActivityDetail = async () => {
  try {
    // request拦截器已经解包了res.data，所以直接得到的就是数据对象
    const activity = await getActivityDetail(route.params.id)
    console.log('活动详情:', activity)
    
    Object.assign(form, {
      activityId: activity.activityId,
      productId: activity.productId,
      groupPrice: activity.groupPrice,
      requiredNum: activity.requiredNum,
      maxNum: activity.maxNum,
      startTime: activity.startTime,
      endTime: activity.endTime,
      status: activity.status
    })
    
    // 查找对应的商品
    selectedProduct.value = productList.value.find(p => p.productId === activity.productId) || null
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败')
  }
}

// 初始化
onMounted(async () => {
  await fetchProducts()
  
  if (isEdit.value) {
    await loadActivityDetail()
  } else {
    // 新建时设置默认时间
    handleTimePreset('7days')
  }
})
</script>

<style scoped>
.create-activity-wrapper {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px 0;
}

.create-activity-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  color: white;
  margin-bottom: 30px;
  padding: 20px 0;
}

.back-button {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.header-content h2 {
  font-size: 32px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.steps-card {
  margin-bottom: 20px;
}

.form-card {
  min-height: 600px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e4e7ed;
}

.product-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  max-height: 500px;
  overflow-y: auto;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  background: white;
}

.product-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
}

.product-item.selected {
  border-color: #67c23a;
  background: #f0f9ff;
}

.product-image {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  flex-shrink: 0;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 32px;
}

.product-info {
  flex: 1;
}

.product-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.product-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.product-meta .price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.check-icon {
  position: absolute;
  top: 50%;
  right: 15px;
  transform: translateY(-50%);
  font-size: 28px;
}

.selected-product-info {
  display: flex;
  gap: 15px;
  align-items: center;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.product-thumb {
  width: 80px;
  height: 80px;
  border-radius: 8px;
}

.product-detail h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.product-price-info {
  font-size: 14px;
}

.product-price-info .label {
  color: #909399;
}

.product-price-info .original-price {
  font-weight: bold;
  color: #303133;
}

.price-tips {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
}

.tip-item.highlight {
  color: #f56c6c;
  font-weight: 600;
}

.time-presets {
  margin-bottom: 15px;
}

.confirm-info {
  margin-bottom: 24px;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #e4e7ed;
}

/* 右侧预览区 */
.preview-area {
  position: sticky;
  top: 20px;
}

.preview-card {
  margin-bottom: 20px;
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.activity-preview {
  text-align: center;
}

.preview-image {
  position: relative;
  width: 100%;
  height: 220px;
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
}

.preview-image :deep(.el-image) {
  width: 100%;
  height: 100%;
}

.preview-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: linear-gradient(135deg, #f5af19 0%, #f12711 100%);
  color: white;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: bold;
}

.preview-title {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #303133;
}

.preview-price {
  display: flex;
  justify-content: center;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 16px;
}

.group-price {
  font-size: 32px;
  font-weight: bold;
  color: #f56c6c;
}

.original-price {
  font-size: 16px;
  color: #909399;
  text-decoration: line-through;
}

.preview-meta {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 16px;
}

.preview-time {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
  color: #909399;
}

/* 数据预估卡片 */
.estimate-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.estimate-card :deep(.el-card__body) {
  padding: 20px;
}

.estimate-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.estimate-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.estimate-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.estimate-item .label {
  font-size: 14px;
  opacity: 0.9;
}

.estimate-item .value {
  font-size: 18px;
  font-weight: bold;
}

.estimate-item .value.highlight {
  color: #ffd700;
}

.estimate-item .value.commission {
  color: #ffeb3b;
}

/* 注意事项卡片 */
.tips-card {
  background: #fff3cd;
  border: 1px solid #ffc107;
}

.tips-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #856404;
  margin-bottom: 12px;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  color: #856404;
  font-size: 13px;
  line-height: 2;
}

.tips-list li {
  list-style-type: disc;
}
</style>

