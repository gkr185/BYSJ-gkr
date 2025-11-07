<template>
  <div class="activity-create-page">
    <div class="page-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <el-button :icon="ArrowLeft" @click="handleBack">返回</el-button>
        <div class="header-content">
          <h1 class="page-title">
            <el-icon><Plus /></el-icon>
            创建拼团活动
          </h1>
          <p class="page-desc">通过三步向导创建拼团活动</p>
        </div>
      </div>

      <!-- 步骤导航 -->
      <el-card class="steps-card" shadow="never">
        <el-steps :active="currentStep" align-center finish-status="success">
          <el-step title="选择商品" description="从商品库中选择商品">
            <template #icon>
              <el-icon><ShoppingBag /></el-icon>
            </template>
          </el-step>
          <el-step title="设置活动" description="配置拼团价格和规则">
            <template #icon>
              <el-icon><Setting /></el-icon>
            </template>
          </el-step>
          <el-step title="确认创建" description="预览并确认活动信息">
            <template #icon>
              <el-icon><CircleCheck /></el-icon>
            </template>
          </el-step>
        </el-steps>
      </el-card>

      <!-- 步骤内容 -->
      <el-card class="content-card" shadow="never">
        <!-- 步骤1：选择商品 -->
        <div v-if="currentStep === 0" class="step-content">
          <div class="step-header">
            <h2>选择商品</h2>
            <p>请从商品库中选择一个商品来创建拼团活动</p>
          </div>

          <div class="search-bar">
            <el-input
              v-model="productSearch"
              placeholder="搜索商品名称或分类"
              clearable
              size="large"
              @input="handleProductSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <div class="products-section" v-loading="productsLoading">
            <el-empty v-if="products.length === 0" description="暂无商品" />
            
            <div class="products-grid" v-else>
              <div 
                v-for="product in products" 
                :key="product.productId"
                class="product-card"
                :class="{ selected: activityForm.productId === product.productId }"
                @click="selectProduct(product)"
              >
                <el-image 
                  :src="product.coverImg || '/placeholder-product.png'" 
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
                  <h3 class="product-name">{{ product.productName }}</h3>
                  <div class="product-meta">
                    <div class="price">¥{{ product.price }}</div>
                    <div class="stock">库存: {{ product.stock }}</div>
                  </div>
                </div>

                <div class="select-badge" v-if="activityForm.productId === product.productId">
                  <el-icon><CircleCheck /></el-icon>
                  <span>已选择</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 步骤2：设置活动 -->
        <div v-if="currentStep === 1" class="step-content">
          <div class="step-header">
            <h2>设置活动</h2>
            <p>配置拼团活动的价格和规则</p>
          </div>

          <div class="selected-product-preview">
            <h3>已选择的商品</h3>
            <div class="product-preview-card">
              <el-image 
                :src="selectedProduct?.coverImg" 
                fit="cover"
                class="preview-image"
              />
              <div class="preview-info">
                <h4>{{ selectedProduct?.productName }}</h4>
                <div class="preview-meta">
                  <span>商品ID: {{ selectedProduct?.productId }}</span>
                  <span>原价: ¥{{ selectedProduct?.price }}</span>
                  <span>库存: {{ selectedProduct?.stock }}件</span>
                </div>
              </div>
            </div>
          </div>

          <el-form 
            :model="activityForm" 
            :rules="activityRules" 
            ref="activityFormRef" 
            label-width="140px"
            class="activity-form"
          >
            <el-divider content-position="left">价格设置</el-divider>
            
            <el-form-item label="拼团价" prop="groupPrice">
              <div class="price-input-wrapper">
                <el-input-number 
                  v-model="activityForm.groupPrice" 
                  :min="selectedProduct?.groupPrice || 0.01" 
                  :max="selectedProduct?.price || 999999"
                  :precision="2"
                  :step="0.1"
                  controls-position="right"
                  size="large"
                  style="width: 200px;"
                />
                <el-button 
                  type="success" 
                  plain
                  @click="useRecommendedPrice"
                  style="margin-left: 12px;"
                >
                  <el-icon><PriceTag /></el-icon>
                  使用建议拼团价
                </el-button>
              </div>
              <div class="price-tips">
                <div class="tip-item">
                  <span class="tip-label">商品原价:</span>
                  <span class="tip-value">¥{{ selectedProduct?.price }}</span>
                </div>
                <div class="tip-item">
                  <span class="tip-label">建议拼团价:</span>
                  <span class="tip-value highlight">¥{{ selectedProduct?.groupPrice }}</span>
                </div>
                <div class="tip-item warning">
                  <el-icon><Warning /></el-icon>
                  拼团价不得低于建议拼团价 ¥{{ selectedProduct?.groupPrice }}
                </div>
              </div>
            </el-form-item>

            <el-form-item label="优惠金额">
              <div class="discount-display">
                <el-tag type="danger" size="large" effect="dark">
                  省¥{{ ((selectedProduct?.price || 0) - activityForm.groupPrice).toFixed(2) }}
                </el-tag>
                <span class="discount-rate">
                  ({{ Math.round(((selectedProduct?.price - activityForm.groupPrice) / selectedProduct?.price) * 100) }}% OFF)
                </span>
              </div>
            </el-form-item>

            <el-divider content-position="left">成团规则</el-divider>

            <el-form-item label="成团人数" prop="requiredNum">
              <el-input-number 
                v-model="activityForm.requiredNum" 
                :min="2" 
                :max="100"
                controls-position="right"
                size="large"
                style="width: 200px;"
              />
              <span class="form-tip">2-100人，达到此人数即可成团</span>
            </el-form-item>

            <el-form-item label="最大人数" prop="maxNum">
              <el-input-number 
                v-model="activityForm.maxNum" 
                :min="activityForm.requiredNum || 2" 
                :max="999999"
                controls-position="right"
                size="large"
                style="width: 200px;"
                clearable
              />
              <span class="form-tip">限制参与活动的总人数，留空表示无限制</span>
            </el-form-item>

            <el-divider content-position="left">活动时间</el-divider>

            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="activityForm.startTime"
                type="datetime"
                placeholder="选择活动开始时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DDTHH:mm:ss"
                size="large"
                style="width: 300px;"
              />
            </el-form-item>

            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="activityForm.endTime"
                type="datetime"
                placeholder="选择活动结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DDTHH:mm:ss"
                size="large"
                style="width: 300px;"
              />
              <span class="form-tip">
                活动时长: {{ getActivityDuration() }}
              </span>
            </el-form-item>

            <el-alert
              title="设置提示"
              type="info"
              :closable="false"
              show-icon
              style="margin-top: 20px;"
            >
              <ul style="margin: 0; padding-left: 20px;">
                <li>拼团价必须低于商品原价，建议设置为原价的60%-90%</li>
                <li>成团人数建议2-10人，人数过多会影响成团率</li>
                <li>活动时长建议1-30天，时间过短可能导致成团困难</li>
                <li>创建后可以编辑活动信息，但不能更换商品</li>
              </ul>
            </el-alert>
          </el-form>
        </div>

        <!-- 步骤3：确认创建 -->
        <div v-if="currentStep === 2" class="step-content">
          <div class="step-header">
            <h2>确认创建</h2>
            <p>请仔细核对活动信息，确认无误后点击创建</p>
          </div>

          <div class="confirm-section">
            <el-result icon="success" title="">
              <template #sub-title>
                <div class="confirm-content">
                  <!-- 商品信息 -->
                  <div class="confirm-block">
                    <div class="block-title">
                      <el-icon><ShoppingBag /></el-icon>
                      商品信息
                    </div>
                    <div class="block-content">
                      <div class="product-confirm">
                        <el-image 
                          :src="selectedProduct?.coverImg" 
                          fit="cover"
                          class="confirm-image"
                        />
                        <div class="product-details">
                          <h3>{{ selectedProduct?.productName }}</h3>
                          <div class="detail-row">
                            <span>商品ID:</span>
                            <span>{{ selectedProduct?.productId }}</span>
                          </div>
                          <div class="detail-row">
                            <span>商品价格:</span>
                            <span>¥{{ selectedProduct?.price }}</span>
                          </div>
                          <div class="detail-row">
                            <span>库存数量:</span>
                            <span>{{ selectedProduct?.stock }}件</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 价格信息 -->
                  <div class="confirm-block">
                    <div class="block-title">
                      <el-icon><Money /></el-icon>
                      价格信息
                    </div>
                    <div class="block-content">
                      <div class="price-confirm">
                        <div class="price-item">
                          <span class="label">拼团价</span>
                          <span class="value price-highlight">¥{{ activityForm.groupPrice }}</span>
                        </div>
                        <el-icon class="arrow"><Right /></el-icon>
                        <div class="price-item">
                          <span class="label">原价</span>
                          <span class="value">¥{{ selectedProduct?.price }}</span>
                        </div>
                        <div class="discount-tag">
                          <el-tag type="danger" size="large" effect="dark">
                            省¥{{ ((selectedProduct?.price || 0) - activityForm.groupPrice).toFixed(2) }}
                          </el-tag>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 成团规则 -->
                  <div class="confirm-block">
                    <div class="block-title">
                      <el-icon><User /></el-icon>
                      成团规则
                    </div>
                    <div class="block-content">
                      <div class="rules-grid">
                        <div class="rule-item">
                          <div class="rule-label">成团人数</div>
                          <div class="rule-value">{{ activityForm.requiredNum }}人</div>
                        </div>
                        <div class="rule-item">
                          <div class="rule-label">最大人数</div>
                          <div class="rule-value">{{ activityForm.maxNum || '无限制' }}</div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- 活动时间 -->
                  <div class="confirm-block">
                    <div class="block-title">
                      <el-icon><Clock /></el-icon>
                      活动时间
                    </div>
                    <div class="block-content">
                      <div class="time-confirm">
                        <div class="time-item">
                          <span class="time-label">开始时间</span>
                          <span class="time-value">{{ formatDateTime(activityForm.startTime) }}</span>
                        </div>
                        <el-icon class="time-arrow"><Right /></el-icon>
                        <div class="time-item">
                          <span class="time-label">结束时间</span>
                          <span class="time-value">{{ formatDateTime(activityForm.endTime) }}</span>
                        </div>
                      </div>
                      <div class="duration-info">
                        <el-tag type="info">活动时长: {{ getActivityDuration() }}</el-tag>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </el-result>

            <el-alert 
              type="warning" 
              :closable="false"
              show-icon
              style="margin-top: 20px;"
            >
              <template #title>
                <p style="margin: 0; font-weight: 600;">
                  确认无误后点击"立即创建"按钮，活动创建后将立即生效
                </p>
              </template>
            </el-alert>
          </div>
        </div>
      </el-card>

      <!-- 底部操作栏 -->
      <div class="actions-bar">
        <el-button size="large" @click="handleBack">
          取消创建
        </el-button>
        <div class="right-actions">
          <el-button 
            size="large"
            @click="handlePrevStep" 
            v-if="currentStep > 0"
          >
            <el-icon><ArrowLeft /></el-icon>
            上一步
          </el-button>
          <el-button 
            type="primary" 
            size="large"
            @click="handleNextStep" 
            v-if="currentStep < 2"
          >
            下一步
            <el-icon><ArrowRight /></el-icon>
          </el-button>
          <el-button 
            type="primary" 
            size="large"
            @click="handleSubmit" 
            :loading="submitLoading"
            v-if="currentStep === 2"
          >
            <el-icon><CircleCheck /></el-icon>
            立即创建
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Plus, ShoppingBag, Setting, CircleCheck, Search,
  Picture, Money, User, Clock, Right, ArrowRight, PriceTag, Warning
} from '@element-plus/icons-vue'
import { createActivity } from '@/api/groupbuy'
import { getProductList } from '@/api/product'

const router = useRouter()
const userStore = useUserStore()

// 数据定义
const currentStep = ref(0)
const productsLoading = ref(false)
const submitLoading = ref(false)
const products = ref([])
const productSearch = ref('')
const activityFormRef = ref(null)

const activityForm = reactive({
  productId: null,
  groupPrice: null,
  requiredNum: 3,
  maxNum: null,
  startTime: '',
  endTime: ''
})

const activityRules = {
  groupPrice: [
    { required: true, message: '请输入拼团价', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '拼团价必须大于0', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const product = selectedProduct.value
        if (product && value < product.groupPrice) {
          callback(new Error(`拼团价不得低于建议拼团价 ¥${product.groupPrice}`))
        } else if (product && value >= product.price) {
          callback(new Error('拼团价必须低于商品原价'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  requiredNum: [
    { required: true, message: '请输入成团人数', trigger: 'blur' },
    { type: 'number', min: 2, max: 100, message: '成团人数应在2-100之间', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
}

// 计算属性
const selectedProduct = computed(() => 
  products.value.find(p => p.productId === activityForm.productId)
)

// 方法定义
const loadProducts = async () => {
  try {
    productsLoading.value = true
    const params = { 
      page: 0, 
      size: 100,
      sort: 'create_time'
    }
    
    if (productSearch.value && productSearch.value.trim()) {
      params.keyword = productSearch.value.trim()
    }
    
    const res = await getProductList(params)
    if (res.code === 200) {
      products.value = res.data?.content || []
    }
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error('加载商品失败')
  } finally {
    productsLoading.value = false
  }
}

const handleProductSearch = () => {
  loadProducts()
}

const selectProduct = (product) => {
  activityForm.productId = product.productId
  // 自动填充拼团价（使用商品建议拼团价）
  activityForm.groupPrice = product.groupPrice
}

const useRecommendedPrice = () => {
  const product = selectedProduct.value
  if (product) {
    activityForm.groupPrice = product.groupPrice
    ElMessage.success(`已设置为建议拼团价 ¥${product.groupPrice}`)
  }
}

const handlePrevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const handleNextStep = async () => {
  if (currentStep.value === 0) {
    // 步骤1验证
    if (!activityForm.productId) {
      ElMessage.warning('请选择商品')
      return
    }
    currentStep.value = 1
  } else if (currentStep.value === 1) {
    // 步骤2验证
    const valid = await activityFormRef.value?.validate().catch(() => false)
    if (!valid) return
    
    // 验证拼团价
    const product = selectedProduct.value
    if (product && activityForm.groupPrice >= product.price) {
      ElMessage.warning('拼团价必须低于商品原价')
      return
    }
    
    // 验证时间
    if (activityForm.startTime >= activityForm.endTime) {
      ElMessage.warning('开始时间必须早于结束时间')
      return
    }
    
    currentStep.value = 2
  }
}

const handleSubmit = async () => {
  try {
    submitLoading.value = true
    
    const data = {
      productId: activityForm.productId,
      groupPrice: activityForm.groupPrice,
      requiredNum: activityForm.requiredNum,
      maxNum: activityForm.maxNum || null,
      startTime: activityForm.startTime,
      endTime: activityForm.endTime
    }
    
    const res = await createActivity(data)
    if (res.code === 200) {
      ElMessage.success('创建成功！')
      
      // 询问是否立即发起团
      ElMessageBox.confirm(
        '活动创建成功！是否立即发起拼团？',
        '创建成功',
        {
          confirmButtonText: '立即发起',
          cancelButtonText: '返回列表',
          type: 'success'
        }
      ).then(() => {
        // 跳转到发起团页面，并传递活动ID
        router.push({
          path: '/leader/groupbuy',
          query: { 
            tab: 'teams',
            activityId: res.data.activityId 
          }
        })
      }).catch(() => {
        // 返回活动列表
        router.push('/leader/groupbuy')
      })
    }
  } catch (error) {
    console.error('创建失败:', error)
    ElMessage.error(error.message || '创建失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

const handleBack = () => {
  ElMessageBox.confirm(
    '确定要取消创建吗？已填写的信息将丢失',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '继续创建',
      type: 'warning'
    }
  ).then(() => {
    router.push('/leader/groupbuy')
  }).catch(() => {
    // 用户取消
  })
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getActivityDuration = () => {
  if (!activityForm.startTime || !activityForm.endTime) return '-'
  
  const start = new Date(activityForm.startTime)
  const end = new Date(activityForm.endTime)
  const diff = end - start
  
  if (diff <= 0) return '无效'
  
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  
  if (days > 0) {
    return `${days}天${hours}小时`
  } else {
    return `${hours}小时`
  }
}

// 生命周期
onMounted(() => {
  loadProducts()
})
</script>

<style scoped lang="scss">
.activity-create-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 80px;

  .page-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 24px;
  }

  .page-header {
    display: flex;
    align-items: center;
    gap: 20px;
    margin-bottom: 24px;

    .header-content {
      flex: 1;

      .page-title {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 28px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px 0;

        .el-icon {
          font-size: 32px;
          color: var(--el-color-primary);
        }
      }

      .page-desc {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }
  }

  .steps-card {
    margin-bottom: 24px;
  }

  .content-card {
    min-height: 600px;
    margin-bottom: 24px;

    .step-content {
      .step-header {
        margin-bottom: 32px;

        h2 {
          font-size: 22px;
          color: #303133;
          margin: 0 0 8px 0;
        }

        p {
          margin: 0;
          color: #909399;
          font-size: 14px;
        }
      }

      .search-bar {
        margin-bottom: 24px;
      }

      .products-section {
        .products-grid {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
          gap: 20px;

          .product-card {
            position: relative;
            border: 2px solid #e4e7ed;
            border-radius: 12px;
            padding: 16px;
            cursor: pointer;
            transition: all 0.3s ease;

            &:hover {
              border-color: var(--el-color-primary-light-5);
              box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            }

            &.selected {
              border-color: var(--el-color-primary);
              background: var(--el-color-primary-light-9);
            }

            .product-image {
              width: 100%;
              height: 150px;
              border-radius: 8px;
              margin-bottom: 12px;

              .image-error {
                display: flex;
                align-items: center;
                justify-content: center;
                width: 100%;
                height: 100%;
                background: #f5f7fa;
                color: #c0c4cc;
                font-size: 32px;
              }
            }

            .product-info {
              .product-name {
                font-size: 15px;
                font-weight: 600;
                color: #303133;
                margin: 0 0 8px 0;
                overflow: hidden;
                text-overflow: ellipsis;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
              }

              .product-meta {
                display: flex;
                justify-content: space-between;
                align-items: center;
                font-size: 13px;

                .price {
                  color: #f56c6c;
                  font-weight: 600;
                  font-size: 16px;
                }

                .stock {
                  color: #909399;
                }
              }
            }

            .select-badge {
              position: absolute;
              top: 12px;
              right: 12px;
              display: flex;
              align-items: center;
              gap: 4px;
              padding: 4px 12px;
              background: var(--el-color-primary);
              color: white;
              border-radius: 12px;
              font-size: 12px;
              font-weight: 600;

              .el-icon {
                font-size: 14px;
              }
            }
          }
        }
      }

      .selected-product-preview {
        margin-bottom: 32px;

        h3 {
          font-size: 16px;
          color: #303133;
          margin: 0 0 16px 0;
        }

        .product-preview-card {
          display: flex;
          align-items: center;
          padding: 20px;
          background: linear-gradient(135deg, #f5f7fa 0%, #e8eaf0 100%);
          border-radius: 12px;

          .preview-image {
            width: 120px;
            height: 120px;
            border-radius: 8px;
            margin-right: 20px;
            flex-shrink: 0;
          }

          .preview-info {
            flex: 1;

            h4 {
              font-size: 18px;
              color: #303133;
              margin: 0 0 12px 0;
            }

            .preview-meta {
              display: flex;
              flex-direction: column;
              gap: 8px;
              font-size: 14px;
              color: #606266;

              span {
                display: flex;
                align-items: center;
                gap: 8px;

                &::before {
                  content: '•';
                  color: var(--el-color-primary);
                  font-weight: bold;
                }
              }
            }
          }
        }
      }

      .activity-form {
        .price-input-wrapper {
          display: flex;
          align-items: center;
        }

        .price-tips {
          margin-top: 12px;
          padding: 12px;
          background: #f5f7fa;
          border-radius: 8px;

          .tip-item {
            display: flex;
            align-items: center;
            margin-bottom: 8px;
            font-size: 13px;

            &:last-child {
              margin-bottom: 0;
            }

            .tip-label {
              color: #909399;
              margin-right: 8px;
            }

            .tip-value {
              color: #303133;
              font-weight: 500;

              &.highlight {
                color: #67c23a;
                font-weight: 600;
                font-size: 14px;
              }
            }

            &.warning {
              color: #e6a23c;
              font-weight: 500;

              .el-icon {
                margin-right: 6px;
              }
            }
          }
        }

        .form-tip {
          margin-left: 12px;
          font-size: 13px;
          color: #909399;
        }

        .discount-display {
          display: flex;
          align-items: center;
          gap: 12px;

          .discount-rate {
            font-size: 14px;
            color: #f56c6c;
            font-weight: 600;
          }
        }
      }

      .confirm-section {
        .confirm-content {
          .confirm-block {
            margin-bottom: 24px;
            padding: 24px;
            background: #f5f7fa;
            border-radius: 12px;

            &:last-child {
              margin-bottom: 0;
            }

            .block-title {
              display: flex;
              align-items: center;
              gap: 8px;
              font-size: 16px;
              font-weight: 600;
              color: #303133;
              margin-bottom: 16px;

              .el-icon {
                font-size: 20px;
                color: var(--el-color-primary);
              }
            }

            .block-content {
              .product-confirm {
                display: flex;
                align-items: flex-start;

                .confirm-image {
                  width: 100px;
                  height: 100px;
                  border-radius: 8px;
                  margin-right: 20px;
                  flex-shrink: 0;
                }

                .product-details {
                  flex: 1;

                  h3 {
                    font-size: 16px;
                    color: #303133;
                    margin: 0 0 12px 0;
                  }

                  .detail-row {
                    display: flex;
                    justify-content: space-between;
                    margin-bottom: 8px;
                    font-size: 14px;
                    color: #606266;

                    span:first-child {
                      color: #909399;
                    }

                    span:last-child {
                      font-weight: 500;
                    }
                  }
                }
              }

              .price-confirm {
                display: flex;
                align-items: center;
                justify-content: space-around;
                padding: 20px;
                background: white;
                border-radius: 8px;

                .price-item {
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  gap: 8px;

                  .label {
                    font-size: 13px;
                    color: #909399;
                  }

                  .value {
                    font-size: 20px;
                    font-weight: 600;
                    color: #303133;

                    &.price-highlight {
                      font-size: 28px;
                      color: #f56c6c;
                    }
                  }
                }

                .arrow {
                  font-size: 24px;
                  color: #dcdfe6;
                }

                .discount-tag {
                  display: flex;
                  align-items: center;
                }
              }

              .rules-grid {
                display: grid;
                grid-template-columns: repeat(2, 1fr);
                gap: 16px;

                .rule-item {
                  padding: 16px;
                  background: white;
                  border-radius: 8px;
                  text-align: center;

                  .rule-label {
                    font-size: 13px;
                    color: #909399;
                    margin-bottom: 8px;
                  }

                  .rule-value {
                    font-size: 20px;
                    font-weight: 600;
                    color: var(--el-color-primary);
                  }
                }
              }

              .time-confirm {
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 20px;
                padding: 20px;
                background: white;
                border-radius: 8px;
                margin-bottom: 12px;

                .time-item {
                  display: flex;
                  flex-direction: column;
                  align-items: center;
                  gap: 8px;

                  .time-label {
                    font-size: 13px;
                    color: #909399;
                  }

                  .time-value {
                    font-size: 14px;
                    font-weight: 500;
                    color: #303133;
                  }
                }

                .time-arrow {
                  font-size: 24px;
                  color: #dcdfe6;
                }
              }

              .duration-info {
                text-align: center;
              }
            }
          }
        }
      }
    }
  }

  .actions-bar {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: white;
    border-top: 1px solid #e4e7ed;
    box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
    z-index: 100;

    .right-actions {
      display: flex;
      gap: 12px;
    }
  }
}
</style>

