<template>
  <MainLayout>
    <div class="launch-team-page">
      <div class="container">
        <el-page-header @back="router.back()" title="返回">
          <template #content>
            <h1 class="page-title">发起拼团</h1>
          </template>
        </el-page-header>

        <!-- 加载状态 -->
        <el-skeleton v-if="loading" :rows="8" animated />

        <!-- 活动信息 -->
        <el-card v-else-if="activity" class="activity-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>活动信息</span>
              <el-tag :type="getStatusType(activity.status)" size="large">
                {{ getStatusText(activity.status) }}
              </el-tag>
            </div>
          </template>

          <div class="activity-info">
            <div class="info-row">
              <span class="label">活动ID：</span>
              <span class="value">{{ activity.activityId }}</span>
            </div>
            <div class="info-row">
              <span class="label">商品ID：</span>
              <span class="value">{{ activity.productId }}</span>
            </div>
            <div class="info-row">
              <span class="label">拼团价：</span>
              <span class="price">¥{{ activity.groupPrice }}</span>
            </div>
            <div class="info-row">
              <span class="label">成团人数：</span>
              <span class="value">{{ activity.requiredNum }}人</span>
            </div>
            <div class="info-row">
              <span class="label">活动时间：</span>
              <span class="time">
                {{ formatDate(activity.startTime) }} 至 {{ formatDate(activity.endTime) }}
              </span>
            </div>
          </div>
        </el-card>

        <!-- 发起表单 -->
        <el-card v-if="activity" class="form-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Edit /></el-icon>
              <span>拼团设置</span>
            </div>
          </template>

          <el-form 
            ref="formRef" 
            :model="form" 
            :rules="rules" 
            label-width="120px"
          >
            <el-form-item label="是否参与" prop="joinImmediately">
              <el-switch 
                v-model="form.joinImmediately"
                active-text="立即参与拼团"
                inactive-text="仅发起不参与"
              />
              <div class="form-tip">
                <el-icon><InfoFilled /></el-icon>
                <span>如果选择立即参与，您将作为第一个成员加入该团</span>
              </div>
            </el-form-item>

            <template v-if="form.joinImmediately">
              <el-form-item label="收货地址" prop="addressId">
                <el-select 
                  v-model="form.addressId" 
                  placeholder="请选择收货地址"
                  style="width: 100%"
                >
                  <el-option
                    v-for="addr in addressList"
                    :key="addr.addressId"
                    :label="`${addr.receiver} ${addr.phone} | ${addr.province}${addr.city}${addr.district}${addr.detail}`"
                    :value="addr.addressId"
                  />
                </el-select>
                <el-button 
                  link 
                  type="primary" 
                  @click="router.push('/user/address')"
                  style="margin-top: 8px"
                >
                  <el-icon><Plus /></el-icon>
                  添加新地址
                </el-button>
              </el-form-item>

              <el-form-item label="购买数量" prop="quantity">
                <el-input-number 
                  v-model="form.quantity" 
                  :min="1" 
                  :max="10"
                />
                <span class="unit">件</span>
              </el-form-item>

              <el-form-item label="支付金额">
                <span class="pay-amount">¥{{ totalAmount }}</span>
              </el-form-item>
            </template>

            <el-divider />

            <el-form-item label="温馨提示">
              <el-alert
                type="info"
                :closable="false"
                show-icon
              >
                <template #title>
                  <div class="alert-content">
                    <p>✅ 作为团长，您发起的团将自动关联到您的社区</p>
                    <p>✅ 团号将自动生成，方便成员查找和加入</p>
                    <p>✅ 团有效期为24小时，过期未成团将自动退款</p>
                    <p v-if="form.joinImmediately">✅ 您将立即创建订单，请及时支付</p>
                    <p v-else>⚠️ 您不参与拼团，只作为发起人</p>
                  </div>
                </template>
              </el-alert>
            </el-form-item>

            <el-form-item>
              <el-button 
                type="primary" 
                size="large" 
                @click="handleLaunch"
                :loading="launching"
              >
                <el-icon><Check /></el-icon>
                确认发起拼团
              </el-button>
              <el-button size="large" @click="router.back()">
                取消
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 错误状态 -->
        <el-empty v-else description="活动不存在或已结束" />
      </div>

      <!-- 成功提示对话框 -->
      <el-dialog
        v-model="successDialogVisible"
        title="拼团发起成功"
        width="500px"
        :close-on-click-modal="false"
        :show-close="false"
      >
        <div class="success-content">
          <el-result
            icon="success"
            title="拼团已成功发起！"
          >
            <template #sub-title>
              <div class="result-info">
                <p>团号：<span class="highlight">{{ launchResult.teamNo }}</span></p>
                <p v-if="launchResult.orderId">
                  订单号：<span class="highlight">{{ launchResult.orderId }}</span>
                </p>
                <p>还需：<span class="highlight">{{ launchResult.remainNum }}人</span>成团</p>
              </div>
            </template>
            <template #extra>
              <el-button 
                type="primary" 
                @click="viewTeam"
              >
                查看团详情
              </el-button>
              <el-button @click="backToList">
                返回活动列表
              </el-button>
            </template>
          </el-result>
        </div>
      </el-dialog>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Edit, InfoFilled, Plus, Check 
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { getActivityDetail, launchTeam } from '@/api/groupbuy'
import { getAddressList } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { formatDate as formatDateUtil } from '@/utils/formatter'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据
const loading = ref(true)
const activity = ref(null)
const addressList = ref([])
const launching = ref(false)
const successDialogVisible = ref(false)
const launchResult = ref(null)

const formRef = ref(null)
const form = ref({
  joinImmediately: true,
  addressId: null,
  quantity: 1
})

const rules = {
  addressId: [
    { 
      validator: (rule, value, callback) => {
        if (form.value.joinImmediately && !value) {
          callback(new Error('请选择收货地址'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  quantity: [
    { required: true, message: '请输入购买数量', trigger: 'blur' }
  ]
}

// 计算属性
const totalAmount = computed(() => {
  if (!activity.value) return '0.00'
  return (activity.value.groupPrice * form.value.quantity).toFixed(2)
})

// 方法
const fetchActivity = async () => {
  const activityId = route.params.activityId
  if (!activityId) {
    router.push('/groupbuy')
    return
  }

  loading.value = true
  try {
    const data = await getActivityDetail(activityId)
    activity.value = data
  } catch (error) {
    console.error('获取活动详情失败:', error)
    ElMessage.error('获取活动详情失败')
  } finally {
    loading.value = false
  }
}

const fetchAddressList = async () => {
  if (!userStore.isLogin) return
  
  try {
    const data = await getAddressList(userStore.userInfo.userId)
    addressList.value = data || []
    
    // 自动选择默认地址
    const defaultAddr = addressList.value.find(addr => addr.isDefault === 1)
    if (defaultAddr) {
      form.value.addressId = defaultAddr.addressId
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
  }
}

const handleLaunch = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    launching.value = true
    try {
      const params = {
        activityId: activity.value.activityId,
        joinImmediately: form.value.joinImmediately
      }

      if (form.value.joinImmediately) {
        params.addressId = form.value.addressId
        params.quantity = form.value.quantity
      }

      const result = await launchTeam(params)
      launchResult.value = result
      successDialogVisible.value = true
    } catch (error) {
      console.error('发起拼团失败:', error)
      ElMessage.error(error.message || '发起拼团失败，请重试')
    } finally {
      launching.value = false
    }
  })
}

const viewTeam = () => {
  if (launchResult.value?.teamId) {
    router.push(`/groupbuy/team/${launchResult.value.teamId}`)
  }
}

const backToList = () => {
  router.push('/groupbuy')
}

const getStatusType = (status) => {
  const types = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    0: '未开始',
    1: '进行中',
    2: '已结束',
    3: '异常'
  }
  return texts[status] || '未知'
}

const formatDate = (dateStr) => {
  return formatDateUtil(dateStr)
}

// 生命周期
onMounted(() => {
  fetchActivity()
  fetchAddressList()
})
</script>

<style scoped>
.launch-team-page {
  min-height: 100vh;
  padding: 20px 0;
  background: #f5f7fa;
}

.container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.activity-card,
.form-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 18px;
  font-weight: bold;
}

.card-header > span {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 活动信息 */
.activity-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  font-size: 15px;
}

.info-row .label {
  color: #909399;
  min-width: 100px;
}

.info-row .value {
  color: #303133;
  font-weight: 500;
}

.info-row .price {
  font-size: 24px;
  font-weight: bold;
  color: #F56C6C;
}

.info-row .time {
  color: #606266;
  font-size: 14px;
}

/* 表单 */
.form-tip {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 13px;
  margin-top: 8px;
}

.unit {
  margin-left: 8px;
  color: #909399;
}

.pay-amount {
  font-size: 24px;
  font-weight: bold;
  color: #F56C6C;
}

.alert-content {
  line-height: 1.8;
}

.alert-content p {
  margin: 4px 0;
}

/* 成功对话框 */
.success-content {
  text-align: center;
}

.result-info {
  margin: 20px 0;
  line-height: 1.8;
}

.result-info p {
  margin: 8px 0;
  font-size: 15px;
  color: #606266;
}

.result-info .highlight {
  font-weight: bold;
  color: #409EFF;
  font-size: 16px;
}

/* 响应式 */
@media (max-width: 768px) {
  .container {
    max-width: 100%;
  }

  .info-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-row .label {
    margin-bottom: 4px;
  }
}
</style>

