<template>
  <div class="launch-team-wrapper">
    <div class="launch-team-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>发起拼团</h2>
        <p class="subtitle">选择活动并创建拼团</p>
      </div>

      <!-- 加载状态 -->
      <el-skeleton v-if="loading" :rows="5" animated />

      <!-- 活动列表 -->
      <div v-else-if="activities.length > 0" class="activities-grid">
        <el-card 
          v-for="activity in activities" 
          :key="activity.activityId"
          class="activity-card"
          shadow="hover"
          @click="selectActivity(activity)"
        >
          <div class="activity-image">
            <img :src="activity.productImage || '/default-product.png'" :alt="activity.productName" />
            <div class="activity-badge">
              <el-tag type="danger" size="small">拼团价</el-tag>
            </div>
          </div>
          <div class="activity-info">
            <h3 class="activity-name">{{ activity.productName }}</h3>
            <div class="activity-price">
              <span class="group-price">¥{{ activity.groupPrice }}</span>
              <span class="original-price">¥{{ activity.originalPrice || activity.groupPrice * 1.5 }}</span>
            </div>
            <div class="activity-meta">
              <span>{{ activity.requiredNum }}人成团</span>
              <span v-if="activity.maxNum">限{{ activity.maxNum }}人</span>
            </div>
            <div class="activity-time">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDate(activity.endTime) }}截止</span>
            </div>
          </div>
          <div class="activity-actions">
            <el-button type="primary" size="large" :icon="Plus" @click.stop="selectActivity(activity)">
              发起拼团
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 无数据 -->
      <el-empty v-else description="暂无可发起的拼团活动">
        <el-button type="primary" @click="router.push('/groupbuy')">查看拼团活动</el-button>
      </el-empty>

      <!-- 发起拼团对话框 -->
      <el-dialog
        v-model="launchDialogVisible"
        title="发起拼团"
        width="600px"
        :close-on-click-modal="false"
      >
        <div v-if="selectedActivity" class="launch-dialog-content">
          <el-alert type="info" :closable="false" style="margin-bottom: 20px;">
            <template #title>
              📢 作为团长，您发起拼团后用户可以看到并参与
            </template>
          </el-alert>

          <div class="selected-activity-info">
            <img :src="selectedActivity.productImage || '/default-product.png'" :alt="selectedActivity.productName" />
            <div class="info-content">
              <h3>{{ selectedActivity.productName }}</h3>
              <p class="price">拼团价：<span>¥{{ selectedActivity.groupPrice }}</span></p>
              <p class="meta">{{ selectedActivity.requiredNum }}人成团</p>
            </div>
          </div>

          <el-divider />

          <el-form :model="launchForm" label-width="120px">
            <el-form-item label="是否参与拼团">
              <el-radio-group v-model="launchForm.participate">
                <el-radio :label="true">参与（作为第一人）</el-radio>
                <el-radio :label="false">仅发起（不参与）</el-radio>
              </el-radio-group>
              <div class="form-tip">
                选择"参与"需要立即支付拼团金额
              </div>
            </el-form-item>

            <el-form-item label="选择收货地址" v-if="launchForm.participate">
              <el-select v-model="launchForm.addressId" placeholder="请选择收货地址" style="width: 100%;">
                <el-option
                  v-for="addr in addresses"
                  :key="addr.addressId"
                  :label="`${addr.receiver} ${addr.phone} ${addr.province}${addr.city}${addr.district}${addr.detail}`"
                  :value="addr.addressId"
                />
              </el-select>
              <div class="form-tip">
                <el-link type="primary" @click="router.push('/user/address')">管理收货地址</el-link>
              </div>
            </el-form-item>
          </el-form>
        </div>

        <template #footer>
          <el-button @click="launchDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleLaunch" :loading="launching">
            确认发起
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Plus } from '@element-plus/icons-vue'
import { getOngoingActivitiesWithProduct, launchTeam } from '@/api/groupbuy'
import { getUserAddresses } from '@/api/user'
import { formatDate } from '@/utils/formatter'

const router = useRouter()
const userStore = useUserStore()

// 数据状态
const loading = ref(false)
const activities = ref([])
const selectedActivity = ref(null)
const launchDialogVisible = ref(false)
const launching = ref(false)
const addresses = ref([])

// 发起表单
const launchForm = ref({
  participate: true,
  addressId: null
})

// 获取活动列表
const fetchActivities = async () => {
  loading.value = true
  try {
    const data = await getOngoingActivitiesWithProduct()
    activities.value = data || []
  } catch (error) {
    console.error('获取活动列表失败:', error)
    ElMessage.error('获取活动列表失败')
    activities.value = []
  } finally {
    loading.value = false
  }
}

// 获取收货地址
const fetchAddresses = async () => {
  if (!userStore.userInfo?.userId) return
  
  try {
    const data = await getUserAddresses(userStore.userInfo.userId)
    addresses.value = data || []
    // 默认选择默认地址
    const defaultAddr = addresses.value.find(a => a.isDefault === 1)
    if (defaultAddr) {
      launchForm.value.addressId = defaultAddr.addressId
    }
  } catch (error) {
    console.error('获取地址失败:', error)
  }
}

// 选择活动
const selectActivity = (activity) => {
  selectedActivity.value = activity
  launchDialogVisible.value = true
  fetchAddresses()
}

// 发起拼团
const handleLaunch = async () => {
  if (!selectedActivity.value) return
  
  // 参与拼团时需要选择地址
  if (launchForm.value.participate && !launchForm.value.addressId) {
    ElMessage.warning('请选择收货地址')
    return
  }

  try {
    await ElMessageBox.confirm(
      launchForm.value.participate 
        ? '确认发起拼团并参与？您需要立即支付。' 
        : '确认发起拼团？',
      '确认发起',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    launching.value = true

    const params = {
      activityId: selectedActivity.value.activityId,
      participate: launchForm.value.participate,
      addressId: launchForm.value.addressId
    }

    const result = await launchTeam(params)

    ElMessage.success('拼团发起成功！')
    launchDialogVisible.value = false

    // 如果参与拼团，跳转到支付页面
    if (launchForm.value.participate && result.orderId) {
      router.push(`/payment?orderId=${result.orderId}`)
    } else {
      // 否则跳转到团详情页
      router.push(`/groupbuy/team/${result.teamId}`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发起拼团失败:', error)
      ElMessage.error(error.message || '发起拼团失败')
    }
  } finally {
    launching.value = false
  }
}

// 页面加载
onMounted(() => {
  if (!userStore.isLeader) {
    ElMessage.warning('仅团长可发起拼团')
    router.push('/leader/apply')
    return
  }
  
  fetchActivities()
})
</script>

<style scoped>
.launch-team-wrapper {
  min-height: 100vh;
  padding-top: 84px;
  background-color: #f5f5f5;
}

.launch-team-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #909399;
}

/* 活动网格 */
.activities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.activity-card {
  cursor: pointer;
  transition: all 0.3s;
}

.activity-card:hover {
  transform: translateY(-4px);
}

.activity-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 8px 8px 0 0;
  background-color: #f5f7fa;
}

.activity-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-badge {
  position: absolute;
  top: 10px;
  left: 10px;
}

.activity-info {
  padding: 15px;
}

.activity-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-price {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 10px;
}

.group-price {
  font-size: 24px;
  font-weight: bold;
  color: #f56c6c;
}

.original-price {
  font-size: 14px;
  color: #909399;
  text-decoration: line-through;
}

.activity-meta {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 10px;
}

.activity-time {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #909399;
}

.activity-actions {
  padding: 0 15px 15px;
}

.activity-actions .el-button {
  width: 100%;
}

/* 发起对话框 */
.launch-dialog-content {
  padding: 10px;
}

.selected-activity-info {
  display: flex;
  gap: 15px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.selected-activity-info img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}

.info-content h3 {
  font-size: 16px;
  color: #333;
  margin-bottom: 10px;
}

.info-content .price {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.info-content .price span {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.info-content .meta {
  font-size: 13px;
  color: #909399;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .launch-team-wrapper {
    padding-top: 76px;
  }

  .launch-team-container {
    padding: 10px;
  }

  .activities-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 10px;
  }

  .activity-image {
    height: 150px;
  }

  .group-price {
    font-size: 20px;
  }

  :deep(.el-dialog) {
    width: 90% !important;
  }
}
</style>

