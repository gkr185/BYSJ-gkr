<template>
  <div class="group-buy-page">
    <div class="group-buy-container">
      <template v-if="loading">
        <el-skeleton :rows="10" animated />
      </template>

      <template v-else-if="!activityData">
        <el-empty description="拼团活动不存在">
          <el-button type="primary" @click="router.push('/products')">返回商品列表</el-button>
        </el-empty>
      </template>

      <template v-else>
        <!-- 商品信息卡片 -->
        <el-card class="product-card" shadow="hover">
          <div class="product-info">
            <div class="product-image">
              <el-image :src="activityData.product_img" fit="cover" />
              <el-tag v-if="activityData.status === 2" type="success" class="status-tag" effect="dark">
                已成团
              </el-tag>
              <el-tag v-else-if="remainingTime.expired" type="info" class="status-tag" effect="dark">
                已结束
              </el-tag>
              <el-tag v-else type="danger" class="status-tag" effect="dark">
                拼团中
              </el-tag>
            </div>

            <div class="product-details">
              <h2 class="product-name">{{ activityData.product_name }}</h2>
              
              <div class="price-section">
                <div class="group-price-wrapper">
                  <span class="label">拼团价</span>
                  <span class="group-price">¥{{ activityData.group_price }}</span>
                </div>
                <div class="original-price-wrapper">
                  <span class="label">原价</span>
                  <span class="original-price">¥{{ activityData.original_price }}</span>
                </div>
                <div class="save-amount">
                  立省 ¥{{ (activityData.original_price - activityData.group_price).toFixed(2) }}
                </div>
              </div>

              <div class="group-info">
                <el-descriptions :column="2" border>
                  <el-descriptions-item label="成团人数">
                    <el-tag>{{ activityData.required_num }}人</el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="当前人数">
                    <el-tag :type="activityData.current_num >= activityData.required_num ? 'success' : 'warning'">
                      {{ activityData.current_num }}人
                    </el-tag>
                  </el-descriptions-item>
                  <el-descriptions-item label="活动时间" :span="2">
                    {{ formatDateTime(activityData.start_time) }} 至 {{ formatDateTime(activityData.end_time) }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>

              <!-- 倒计时 -->
              <div v-if="activityData.status === 1 && !remainingTime.expired" class="countdown-section">
                <el-icon class="clock-icon"><Clock /></el-icon>
                <span class="countdown-label">距离结束还剩</span>
                <div class="countdown-time">
                  <span class="time-unit">{{ String(remainingTime.hours).padStart(2, '0') }}</span>
                  <span class="separator">:</span>
                  <span class="time-unit">{{ String(remainingTime.minutes).padStart(2, '0') }}</span>
                  <span class="separator">:</span>
                  <span class="time-unit">{{ String(remainingTime.seconds).padStart(2, '0') }}</span>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="action-buttons">
                <el-button 
                  v-if="activityData.status === 1 && !remainingTime.expired && activityData.current_num < activityData.required_num"
                  type="primary" 
                  size="large" 
                  :icon="UserFilled"
                  @click="handleJoinGroup"
                >
                  立即参团（还差{{ activityData.required_num - activityData.current_num }}人）
                </el-button>
                <el-button 
                  v-else-if="activityData.status === 2"
                  type="success" 
                  size="large"
                  disabled
                >
                  拼团已成功
                </el-button>
                <el-button 
                  v-else
                  type="info" 
                  size="large"
                  disabled
                >
                  活动已结束
                </el-button>
                <el-button size="large" @click="router.push(`/products/${activityData.product_id}`)">
                  查看商品详情
                </el-button>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 拼团进度 -->
        <el-card class="progress-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><UserFilled /></el-icon>
                拼团进度
              </span>
            </div>
          </template>

          <div class="progress-wrapper">
            <el-progress 
              :percentage="progressPercentage" 
              :stroke-width="20"
              :color="progressColor"
            >
              <template #default="{ percentage }">
                <span class="progress-text">{{ activityData.current_num }}/{{ activityData.required_num }}人 ({{ percentage }}%)</span>
              </template>
            </el-progress>
          </div>

          <div class="members-list">
            <h3 class="list-title">参团成员 ({{ activityData.members.length }}人)</h3>
            <el-row :gutter="16">
              <el-col 
                v-for="member in activityData.members" 
                :key="member.join_id" 
                :xs="24" 
                :sm="12" 
                :md="8" 
                :lg="6"
              >
                <div class="member-card">
                  <el-avatar :size="60" :src="member.avatar" />
                  <div class="member-info">
                    <div class="member-name">
                      {{ member.user_name }}
                      <el-tag v-if="member.is_launcher === 1" type="warning" size="small" effect="dark">
                        团长
                      </el-tag>
                    </div>
                    <div class="join-time">{{ formatDateTime(member.join_time) }}</div>
                  </div>
                  <el-icon v-if="member.status === 2" class="success-icon" color="#67C23A">
                    <CircleCheckFilled />
                  </el-icon>
                </div>
              </el-col>

              <!-- 空位提示 -->
              <el-col 
                v-for="i in (activityData.required_num - activityData.current_num)" 
                :key="'empty-' + i"
                :xs="24" 
                :sm="12" 
                :md="8" 
                :lg="6"
              >
                <div class="member-card empty">
                  <el-avatar :size="60">
                    <el-icon><Plus /></el-icon>
                  </el-avatar>
                  <div class="member-info">
                    <div class="member-name">等待参团</div>
                    <div class="join-time">虚位以待</div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>

        <!-- 拼团规则 -->
        <el-card class="rules-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Document /></el-icon>
                拼团规则
              </span>
            </div>
          </template>

          <div class="rules-content">
            <el-alert type="info" :closable="false" show-icon>
              <ul class="rules-list">
                <li>拼团价格：¥{{ activityData.group_price }}，成团人数：{{ activityData.required_num }}人</li>
                <li>在活动时间内，邀请好友参团，人数达到{{ activityData.required_num }}人即可成团</li>
                <li>成团后系统将自动生成订单，请及时支付</li>
                <li>如果在活动结束前未达成团人数，本次拼团失败，已支付金额将原路退回</li>
                <li>每个用户在同一拼团活动中只能参与一次</li>
                <li>拼团商品不支持单独购买，如需单买请前往商品详情页</li>
              </ul>
            </el-alert>
          </div>
        </el-card>

        <!-- 分享二维码 -->
        <el-card v-if="activityData.qrcode_url && activityData.status === 1" class="qrcode-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Share /></el-icon>
                分享给好友
              </span>
            </div>
          </template>

          <div class="qrcode-content">
            <el-image :src="activityData.qrcode_url" style="width: 200px; height: 200px;" fit="contain" />
            <p class="qrcode-tip">扫描二维码或复制链接分享给好友</p>
            <el-input v-model="shareLink" readonly>
              <template #append>
                <el-button @click="copyLink">复制链接</el-button>
              </template>
            </el-input>
          </div>
        </el-card>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  UserFilled, Clock, CircleCheckFilled, Plus, Document, Share
} from '@element-plus/icons-vue'
import { getGroupBuyDetail, calculateRemainingTime } from '@/mock/groupbuy'

const router = useRouter()
const route = useRoute()

const loading = ref(true)
const activityData = ref(null)
const remainingTime = ref({ hours: 0, minutes: 0, seconds: 0, expired: false })
let countdownTimer = null

const shareLink = computed(() => {
  if (!activityData.value) return ''
  return `${window.location.origin}${activityData.value.link}`
})

const progressPercentage = computed(() => {
  if (!activityData.value) return 0
  return Math.round((activityData.value.current_num / activityData.value.required_num) * 100)
})

const progressColor = computed(() => {
  const percentage = progressPercentage.value
  if (percentage >= 100) return '#67C23A'
  if (percentage >= 50) return '#409EFF'
  return '#E6A23C'
})

// 获取拼团活动详情
const fetchActivityDetail = async () => {
  loading.value = true
  try {
    // 默认使用activity_id=1（测试用）
    const activityId = route.query.id || route.params.id || 1
    
    // 使用Mock数据
    const data = getGroupBuyDetail(activityId)
    if (data) {
      activityData.value = data
      updateCountdown()
    } else {
      activityData.value = null
      ElMessage.warning('拼团活动不存在')
    }
  } catch (error) {
    console.error('获取拼团详情失败:', error)
    ElMessage.error('获取拼团详情失败')
    activityData.value = null
  } finally {
    loading.value = false
  }
}

// 更新倒计时
const updateCountdown = () => {
  if (activityData.value && activityData.value.end_time) {
    remainingTime.value = calculateRemainingTime(activityData.value.end_time)
  }
}

// 参加拼团
const handleJoinGroup = () => {
  ElMessage.success('参团成功！订单已生成，请前往订单页面支付')
  setTimeout(() => {
    router.push('/user/orders')
  }, 1500)
}

// 复制链接
const copyLink = () => {
  navigator.clipboard.writeText(shareLink.value).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', { 
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  fetchActivityDetail()
  
  // 每秒更新倒计时
  countdownTimer = setInterval(() => {
    updateCountdown()
  }, 1000)
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.group-buy-page {
  min-height: 100vh;
  padding-top: 84px; /* 64px导航栏 + 20px间隔 */
  background-color: #f5f5f5;
}

.group-buy-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 40px 20px;
}

/* 商品信息卡片 */
.product-card {
  margin-bottom: 20px;
  animation: fadeInUp 0.5s ease-out;
}

.product-info {
  display: flex;
  gap: 30px;
}

.product-image {
  position: relative;
  flex-shrink: 0;
  width: 400px;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
}

.product-image .el-image {
  width: 100%;
  height: 100%;
}

.status-tag {
  position: absolute;
  top: 16px;
  left: 16px;
  font-size: 14px;
  padding: 6px 12px;
}

.product-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.product-name {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 0;
  line-height: 1.4;
}

/* 价格区域 */
.price-section {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
}

.group-price-wrapper,
.original-price-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.group-price-wrapper .label,
.original-price-wrapper .label {
  font-size: 13px;
  opacity: 0.9;
}

.group-price {
  font-size: 36px;
  font-weight: bold;
}

.original-price {
  font-size: 20px;
  text-decoration: line-through;
  opacity: 0.8;
}

.save-amount {
  margin-left: auto;
  padding: 8px 16px;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  font-size: 16px;
  font-weight: 500;
}

/* 倒计时 */
.countdown-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 8px;
  color: white;
  font-weight: 500;
}

.clock-icon {
  font-size: 24px;
}

.countdown-label {
  font-size: 16px;
}

.countdown-time {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
}

.time-unit {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 45px;
  height: 45px;
  padding: 0 8px;
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 6px;
  font-size: 24px;
  font-weight: bold;
}

.separator {
  font-size: 24px;
  font-weight: bold;
  margin: 0 2px;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
}

.action-buttons .el-button {
  flex: 1;
}

/* 拼团进度卡片 */
.progress-card,
.rules-card,
.qrcode-card {
  margin-bottom: 20px;
  animation: fadeInUp 0.5s ease-out 0.1s both;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.progress-wrapper {
  margin-bottom: 30px;
}

.progress-text {
  font-size: 14px;
  font-weight: 500;
}

/* 成员列表 */
.members-list {
  margin-top: 20px;
}

.list-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
}

.member-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 12px;
  transition: all 0.3s;
}

.member-card:hover {
  background-color: #f0f0f0;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.member-card.empty {
  opacity: 0.6;
  border: 2px dashed #ddd;
  background-color: transparent;
}

.member-card.empty:hover {
  background-color: #f9f9f9;
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.join-time {
  font-size: 13px;
  color: #999;
}

.success-icon {
  font-size: 24px;
}

/* 规则内容 */
.rules-content {
  animation: fadeInUp 0.5s ease-out 0.2s both;
}

.rules-list {
  margin: 0;
  padding-left: 20px;
  line-height: 2;
}

.rules-list li {
  color: #606266;
  font-size: 14px;
}

/* 二维码内容 */
.qrcode-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  animation: fadeInUp 0.5s ease-out 0.3s both;
}

.qrcode-tip {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.qrcode-content .el-input {
  width: 100%;
  max-width: 400px;
}

/* 动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .group-buy-page {
    padding-top: 76px; /* 56px导航栏 + 20px间隔 */
  }

  .product-info {
    flex-direction: column;
  }

  .product-image {
    width: 100%;
    height: 300px;
  }

  .product-name {
    font-size: 22px;
  }

  .price-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .save-amount {
    margin-left: 0;
  }

  .group-price {
    font-size: 28px;
  }

  .countdown-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .countdown-time {
    margin-left: 0;
  }

  .time-unit {
    min-width: 36px;
    height: 36px;
    font-size: 20px;
  }

  .action-buttons {
    flex-direction: column;
  }

  .member-card {
    flex-direction: column;
    text-align: center;
  }
}
</style>

