# T字开发 - 模块化拆解计划 Part2

**接续**: T字开发_模块化拆解计划.md  
**本部分内容**: M04-M13（用户端剩余5个 + 团长端5个）

---

## M04: 拼团活动模块

### 基本信息
- **所属端**: 用户端
- **优先级**: 🔴 高
- **预计时间**: 1天
- **依赖**: M03（商品详情）

### 功能描述
展示拼团活动详情、参与拼团、拼团进度、成团倒计时

### 数据准备

**文件**: `src/mock/groupbuy.js`

```javascript
// 拼团活动数据
export const mockGroupBuyActivities = [
  {
    id: 1,
    productId: 1,
    productName: '新鲜红富士苹果',
    productCover: 'https://via.placeholder.com/300x300/FF6B6B/FFFFFF?text=Apple',
    groupPrice: 9.90,
    originalPrice: 12.80,
    requiredNum: 3, // 成团人数
    currentNum: 2, // 当前人数
    status: 1, // 0-未开始 1-进行中 2-已成团 3-已结束
    startTime: '2025-10-20 10:00:00',
    endTime: '2025-10-23 10:00:00',
    createTime: '2025-10-20 10:00:00',
    launcherId: 1001,
    launcherName: '张三',
    launcherAvatar: 'https://via.placeholder.com/60x60',
    members: [
      {
        id: 1,
        userId: 1001,
        userName: '张三',
        avatar: 'https://via.placeholder.com/60x60',
        isLauncher: true,
        joinTime: '2025-10-20 10:00:00'
      },
      {
        id: 2,
        userId: 1002,
        userName: '李四',
        avatar: 'https://via.placeholder.com/60x60',
        isLauncher: false,
        joinTime: '2025-10-20 12:30:00'
      }
    ]
  },
  {
    id: 2,
    productId: 1,
    productName: '新鲜红富士苹果',
    productCover: 'https://via.placeholder.com/300x300/FF6B6B/FFFFFF?text=Apple',
    groupPrice: 9.90,
    originalPrice: 12.80,
    requiredNum: 3,
    currentNum: 3,
    status: 2, // 已成团
    startTime: '2025-10-19 10:00:00',
    endTime: '2025-10-22 10:00:00',
    createTime: '2025-10-19 10:00:00',
    launcherId: 1003,
    launcherName: '王五',
    launcherAvatar: 'https://via.placeholder.com/60x60',
    members: [
      {
        id: 3,
        userId: 1003,
        userName: '王五',
        avatar: 'https://via.placeholder.com/60x60',
        isLauncher: true,
        joinTime: '2025-10-19 10:00:00'
      },
      {
        id: 4,
        userId: 1004,
        userName: '赵六',
        avatar: 'https://via.placeholder.com/60x60',
        isLauncher: false,
        joinTime: '2025-10-19 14:20:00'
      },
      {
        id: 5,
        userId: 1005,
        userName: '钱七',
        avatar: 'https://via.placeholder.com/60x60',
        isLauncher: false,
        joinTime: '2025-10-19 16:45:00'
      }
    ]
  }
]

// 获取商品的所有拼团活动
export const getProductGroupBuyActivities = (productId) => {
  return mockGroupBuyActivities
    .filter(activity => activity.productId === productId)
    .filter(activity => activity.status === 1) // 只返回进行中的
    .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
}

// 获取拼团活动详情
export const getGroupBuyDetail = (activityId) => {
  return mockGroupBuyActivities.find(a => a.id === activityId)
}

// 计算剩余时间（秒）
export const calculateRemainingTime = (endTime) => {
  const end = new Date(endTime).getTime()
  const now = Date.now()
  const diff = Math.floor((end - now) / 1000)
  return Math.max(0, diff)
}
```

### 页面开发

**文件**: `src/views/user/GroupBuyView.vue`

```vue
<template>
  <div class="groupbuy-container">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/products' }">商品列表</el-breadcrumb-item>
      <el-breadcrumb-item>拼团活动</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-if="activity" class="activity-main">
      <!-- 商品信息 -->
      <div class="product-section">
        <img :src="activity.productCover" class="product-image" />
        <div class="product-info">
          <h2 class="product-name">{{ activity.productName }}</h2>
          <div class="price-info">
            <span class="group-price">¥{{ activity.groupPrice }}</span>
            <span class="original-price">¥{{ activity.originalPrice }}</span>
            <span class="save-amount">
              立省¥{{ (activity.originalPrice - activity.groupPrice).toFixed(2) }}
            </span>
          </div>
        </div>
      </div>

      <!-- 拼团进度 -->
      <div class="groupbuy-progress">
        <div class="progress-header">
          <h3>拼团进度</h3>
          <div class="countdown">
            <span class="label">剩余时间：</span>
            <span class="time">{{ formattedTime }}</span>
          </div>
        </div>

        <div class="progress-bar-wrapper">
          <el-progress
            :percentage="progressPercentage"
            :status="activity.status === 2 ? 'success' : undefined"
            :show-text="false"
          />
          <div class="progress-text">
            {{ activity.currentNum }} / {{ activity.requiredNum }}人
          </div>
        </div>

        <!-- 成员列表 -->
        <div class="members-list">
          <div
            v-for="member in activity.members"
            :key="member.id"
            class="member-item"
          >
            <img :src="member.avatar" class="member-avatar" />
            <div class="member-name">{{ member.userName }}</div>
            <el-tag v-if="member.isLauncher" type="danger" size="small">
              团长
            </el-tag>
          </div>
          <!-- 空位 -->
          <div
            v-for="i in emptySlots"
            :key="`empty-${i}`"
            class="member-item empty"
          >
            <div class="empty-avatar">
              <el-icon><User /></el-icon>
            </div>
            <div class="member-name">待加入</div>
          </div>
        </div>

        <!-- 状态提示 -->
        <div v-if="activity.status === 2" class="status-tip success">
          <el-icon><SuccessFilled /></el-icon>
          <span>拼团成功！</span>
        </div>
        <div v-else-if="remainingTime === 0" class="status-tip warning">
          <el-icon><WarningFilled /></el-icon>
          <span>拼团已结束</span>
        </div>

        <!-- 操作按钮 -->
        <div class="action-buttons">
          <el-button
            v-if="activity.status === 1 && remainingTime > 0"
            type="primary"
            size="large"
            class="join-btn"
            @click="handleJoin"
          >
            立即参团（¥{{ activity.groupPrice }}）
          </el-button>
          <el-button
            v-if="activity.status === 2"
            type="success"
            size="large"
            disabled
          >
            拼团已完成
          </el-button>
          <el-button
            size="large"
            @click="handleShareWechat"
          >
            <el-icon><Share /></el-icon>
            分享给好友
          </el-button>
        </div>
      </div>

      <!-- 拼团规则 -->
      <div class="groupbuy-rules">
        <h3>拼团规则</h3>
        <ul>
          <li>1. 拼团有效期内，邀请好友参团，满{{ activity.requiredNum }}人即可成团</li>
          <li>2. 成团后商品将统一配送，请留意订单状态</li>
          <li>3. 拼团失败将自动退款到您的账户余额</li>
          <li>4. 同一用户不能重复参加同一拼团活动</li>
        </ul>
      </div>

      <!-- 其他拼团 -->
      <div v-if="otherActivities.length > 0" class="other-activities">
        <h3>其他拼团</h3>
        <div class="activity-list">
          <div
            v-for="item in otherActivities"
            :key="item.id"
            class="activity-card"
            @click="switchActivity(item.id)"
          >
            <img :src="item.productCover" class="card-image" />
            <div class="card-info">
              <div class="card-progress">
                差{{ item.requiredNum - item.currentNum }}人成团
              </div>
              <div class="card-launcher">{{ item.launcherName }}的拼团</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-else description="拼团活动不存在" />

    <!-- 参团对话框 -->
    <el-dialog v-model="joinDialogVisible" title="确认参团" width="500px">
      <div class="join-dialog-content">
        <div class="dialog-row">
          <span class="label">商品名称：</span>
          <span class="value">{{ activity?.productName }}</span>
        </div>
        <div class="dialog-row">
          <span class="label">拼团价格：</span>
          <span class="value price">¥{{ activity?.groupPrice }}</span>
        </div>
        <div class="dialog-row">
          <span class="label">购买数量：</span>
          <el-input-number v-model="joinQuantity" :min="1" :max="10" />
        </div>
        <div class="dialog-row">
          <span class="label">总金额：</span>
          <span class="value total">
            ¥{{ (activity?.groupPrice * joinQuantity).toFixed(2) }}
          </span>
        </div>
      </div>
      <template #footer>
        <el-button @click="joinDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmJoin">确认参团</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, SuccessFilled, WarningFilled, Share } from '@element-plus/icons-vue'
import {
  getGroupBuyDetail,
  getProductGroupBuyActivities,
  calculateRemainingTime
} from '@/mock/groupbuy'

const route = useRoute()
const router = useRouter()

// 数据
const activity = ref(null)
const otherActivities = ref([])
const remainingTime = ref(0)
const joinDialogVisible = ref(false)
const joinQuantity = ref(1)

let timer = null

// 计算属性
const progressPercentage = computed(() => {
  if (!activity.value) return 0
  return Math.floor((activity.value.currentNum / activity.value.requiredNum) * 100)
})

const emptySlots = computed(() => {
  if (!activity.value) return 0
  return activity.value.requiredNum - activity.value.currentNum
})

const formattedTime = computed(() => {
  const seconds = remainingTime.value
  if (seconds <= 0) return '已结束'
  
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  let result = ''
  if (days > 0) result += `${days}天`
  if (hours > 0) result += `${hours}时`
  if (minutes > 0) result += `${minutes}分`
  result += `${secs}秒`
  
  return result
})

// 方法
const loadActivity = () => {
  const activityId = parseInt(route.params.id)
  activity.value = getGroupBuyDetail(activityId)
  
  if (activity.value) {
    remainingTime.value = calculateRemainingTime(activity.value.endTime)
    
    // 加载其他拼团
    otherActivities.value = getProductGroupBuyActivities(activity.value.productId)
      .filter(a => a.id !== activityId)
      .slice(0, 3)
    
    // 启动倒计时
    startCountdown()
  }
}

const startCountdown = () => {
  if (timer) clearInterval(timer)
  
  timer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    } else {
      clearInterval(timer)
    }
  }, 1000)
}

const handleJoin = () => {
  joinDialogVisible.value = true
}

const confirmJoin = () => {
  // TODO: 实际应调用参团API
  ElMessage.success('参团成功！请前往订单页面查看')
  joinDialogVisible.value = false
  
  // 模拟参团后更新数据
  activity.value.currentNum++
  activity.value.members.push({
    id: Date.now(),
    userId: 9999,
    userName: '当前用户',
    avatar: 'https://via.placeholder.com/60x60',
    isLauncher: false,
    joinTime: new Date().toLocaleString()
  })
}

const handleShareWechat = () => {
  // TODO: 实际应生成分享海报或调用微信分享SDK
  const shareUrl = window.location.href
  navigator.clipboard.writeText(shareUrl)
  ElMessage.success('链接已复制到剪贴板，快去分享吧！')
}

const switchActivity = (activityId) => {
  router.push(`/group-buy/${activityId}`)
}

// 生命周期
onMounted(() => {
  loadActivity()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.groupbuy-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.el-breadcrumb {
  margin-bottom: 20px;
}

.activity-main {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
}

.product-section {
  display: flex;
  gap: 20px;
  padding-bottom: 30px;
  border-bottom: 1px solid #DCDFE6;
  margin-bottom: 30px;
}

.product-image {
  width: 150px;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 16px;
}

.price-info {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.group-price {
  font-size: 32px;
  font-weight: bold;
  color: #F56C6C;
}

.original-price {
  font-size: 18px;
  color: #909399;
  text-decoration: line-through;
}

.save-amount {
  font-size: 14px;
  color: #F56C6C;
  background: #FEF0F0;
  padding: 4px 12px;
  border-radius: 4px;
}

.groupbuy-progress {
  margin-bottom: 30px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.progress-header h3 {
  font-size: 18px;
  font-weight: bold;
}

.countdown {
  font-size: 14px;
}

.countdown .label {
  color: #606266;
}

.countdown .time {
  color: #F56C6C;
  font-weight: bold;
  font-size: 16px;
}

.progress-bar-wrapper {
  position: relative;
  margin-bottom: 30px;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 14px;
  font-weight: bold;
  color: #409EFF;
}

.members-list {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.member-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.member-avatar,
.empty-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.empty-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F5F7FA;
  color: #909399;
  font-size: 24px;
}

.member-name {
  font-size: 12px;
  color: #606266;
}

.member-item.empty .member-name {
  color: #C0C4CC;
}

.status-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 4px;
  margin-bottom: 20px;
  font-size: 14px;
  font-weight: bold;
}

.status-tip.success {
  background: #F0F9FF;
  color: #67C23A;
}

.status-tip.warning {
  background: #FDF6EC;
  color: #E6A23C;
}

.action-buttons {
  display: flex;
  gap: 16px;
}

.join-btn {
  flex: 1;
  height: 50px;
  font-size: 16px;
}

.groupbuy-rules {
  padding: 20px;
  background: #F5F7FA;
  border-radius: 8px;
  margin-bottom: 30px;
}

.groupbuy-rules h3 {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 12px;
}

.groupbuy-rules ul {
  padding-left: 20px;
}

.groupbuy-rules li {
  line-height: 2;
  color: #606266;
}

.other-activities h3 {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
}

.activity-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.activity-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid #DCDFE6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.activity-card:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.card-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.card-progress {
  font-size: 14px;
  font-weight: bold;
  color: #F56C6C;
  margin-bottom: 8px;
}

.card-launcher {
  font-size: 12px;
  color: #909399;
}

.join-dialog-content {
  padding: 20px 0;
}

.dialog-row {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.dialog-row .label {
  width: 100px;
  color: #606266;
}

.dialog-row .value {
  flex: 1;
  color: #303133;
}

.dialog-row .value.price {
  font-size: 20px;
  font-weight: bold;
  color: #F56C6C;
}

.dialog-row .value.total {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}
</style>
```

### 路由配置

```javascript
{
  path: '/group-buy/:id',
  name: 'group-buy',
  component: () => import('@/views/user/GroupBuyView.vue'),
  meta: { title: '拼团活动' }
}
```

### 验收标准

- [ ] 拼团活动信息展示完整
- [ ] 拼团进度条正确显示
- [ ] 成员列表显示正确（包括空位）
- [ ] 倒计时功能正常（实时更新）
- [ ] 参团对话框正常工作
- [ ] 数量选择器正常
- [ ] 分享功能（复制链接）正常
- [ ] 其他拼团列表显示
- [ ] 切换拼团功能正常
- [ ] 已成团/已结束状态显示正确
- [ ] 拼团规则说明清晰

---

## M05: 购物车模块

### 基本信息
- **所属端**: 用户端
- **优先级**: 🟡 中
- **预计时间**: 0.5天
- **依赖**: M03（商品详情）

### 功能描述
展示购物车商品列表、数量调整、删除、结算

### 数据准备

**使用LocalStorage存储**:

```javascript
// src/utils/cart.js

// 购物车数据结构
// {
//   productId: number,
//   productName: string,
//   price: number,
//   quantity: number,
//   cover: string,
//   stock: number
// }

// 获取购物车
export const getCart = () => {
  const cart = localStorage.getItem('cart')
  return cart ? JSON.parse(cart) : []
}

// 保存购物车
export const saveCart = (cart) => {
  localStorage.setItem('cart', JSON.stringify(cart))
}

// 添加商品到购物车
export const addToCart = (product, quantity = 1) => {
  const cart = getCart()
  const existingItem = cart.find(item => item.productId === product.id)
  
  if (existingItem) {
    existingItem.quantity += quantity
  } else {
    cart.push({
      productId: product.id,
      productName: product.name,
      price: product.groupPrice,
      quantity: quantity,
      cover: product.cover,
      stock: product.stock
    })
  }
  
  saveCart(cart)
  return cart
}

// 更新商品数量
export const updateCartItemQuantity = (productId, quantity) => {
  const cart = getCart()
  const item = cart.find(i => i.productId === productId)
  
  if (item) {
    item.quantity = quantity
    saveCart(cart)
  }
  
  return cart
}

// 删除商品
export const removeCartItem = (productId) => {
  let cart = getCart()
  cart = cart.filter(item => item.productId !== productId)
  saveCart(cart)
  return cart
}

// 清空购物车
export const clearCart = () => {
  localStorage.removeItem('cart')
}

// 计算总价
export const calculateTotal = (cart) => {
  return cart.reduce((total, item) => total + item.price * item.quantity, 0)
}
```

### 页面开发

**文件**: `src/views/user/CartView.vue`

```vue
<template>
  <div class="cart-container">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>购物车</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="cart-main">
      <h2 class="page-title">我的购物车</h2>

      <div v-if="cartItems.length > 0" class="cart-content">
        <!-- 购物车列表 -->
        <div class="cart-list">
          <div class="cart-header">
            <el-checkbox
              v-model="selectAll"
              @change="handleSelectAll"
            >
              全选
            </el-checkbox>
            <span class="header-item">商品信息</span>
            <span class="header-item">单价</span>
            <span class="header-item">数量</span>
            <span class="header-item">小计</span>
            <span class="header-item">操作</span>
          </div>

          <div
            v-for="item in cartItems"
            :key="item.productId"
            class="cart-item"
          >
            <el-checkbox
              v-model="selectedItems"
              :label="item.productId"
            />
            <div class="item-info" @click="goToDetail(item.productId)">
              <img :src="item.cover" class="item-image" />
              <span class="item-name">{{ item.productName }}</span>
            </div>
            <div class="item-price">¥{{ item.price.toFixed(2) }}</div>
            <div class="item-quantity">
              <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="item.stock"
                size="small"
                @change="handleQuantityChange(item)"
              />
            </div>
            <div class="item-subtotal">
              ¥{{ (item.price * item.quantity).toFixed(2) }}
            </div>
            <div class="item-actions">
              <el-button
                type="danger"
                text
                @click="handleRemove(item.productId)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>

        <!-- 购物车底部 -->
        <div class="cart-footer">
          <div class="footer-left">
            <el-checkbox
              v-model="selectAll"
              @change="handleSelectAll"
            >
              全选
            </el-checkbox>
            <el-button text @click="handleClearCart">清空购物车</el-button>
          </div>
          <div class="footer-right">
            <div class="total-info">
              <span class="label">
                已选商品 <span class="count">{{ selectedCount }}</span> 件
              </span>
              <span class="label">合计：</span>
              <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <el-button
              type="primary"
              size="large"
              :disabled="selectedCount === 0"
              @click="handleCheckout"
            >
              去结算
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空购物车 -->
      <div v-else class="empty-cart">
        <el-empty description="购物车是空的">
          <el-button type="primary" @click="goToProducts">
            去逛逛
          </el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  getCart,
  updateCartItemQuantity,
  removeCartItem,
  clearCart,
  calculateTotal
} from '@/utils/cart'

const router = useRouter()

// 数据
const cartItems = ref([])
const selectedItems = ref([])

// 计算属性
const selectAll = computed({
  get() {
    return cartItems.value.length > 0 &&
           selectedItems.value.length === cartItems.value.length
  },
  set(value) {
    if (value) {
      selectedItems.value = cartItems.value.map(item => item.productId)
    } else {
      selectedItems.value = []
    }
  }
})

const selectedCount = computed(() => {
  return selectedItems.value.length
})

const totalPrice = computed(() => {
  const selectedCartItems = cartItems.value.filter(item =>
    selectedItems.value.includes(item.productId)
  )
  return calculateTotal(selectedCartItems)
})

// 方法
const loadCart = () => {
  cartItems.value = getCart()
  // 默认全选
  selectedItems.value = cartItems.value.map(item => item.productId)
}

const handleSelectAll = (value) => {
  if (value) {
    selectedItems.value = cartItems.value.map(item => item.productId)
  } else {
    selectedItems.value = []
  }
}

const handleQuantityChange = (item) => {
  updateCartItemQuantity(item.productId, item.quantity)
}

const handleRemove = (productId) => {
  ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    cartItems.value = removeCartItem(productId)
    selectedItems.value = selectedItems.value.filter(id => id !== productId)
    ElMessage.success('已删除')
  }).catch(() => {})
}

const handleClearCart = () => {
  ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    clearCart()
    cartItems.value = []
    selectedItems.value = []
    ElMessage.success('已清空')
  }).catch(() => {})
}

const handleCheckout = () => {
  const selectedCartItems = cartItems.value.filter(item =>
    selectedItems.value.includes(item.productId)
  )
  
  // 将选中的商品传递到订单确认页面
  router.push({
    path: '/order/confirm',
    query: {
      from: 'cart',
      items: JSON.stringify(selectedCartItems.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      })))
    }
  })
}

const goToDetail = (productId) => {
  router.push(`/products/${productId}`)
}

const goToProducts = () => {
  router.push('/products')
}

// 生命周期
onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.el-breadcrumb {
  margin-bottom: 20px;
}

.cart-main {
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  min-height: 500px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 30px;
}

.cart-header {
  display: grid;
  grid-template-columns: 50px 2fr 1fr 1fr 1fr 1fr;
  align-items: center;
  padding: 16px 20px;
  background: #F5F7FA;
  border-radius: 4px;
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: bold;
  color: #606266;
}

.cart-item {
  display: grid;
  grid-template-columns: 50px 2fr 1fr 1fr 1fr 1fr;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #DCDFE6;
}

.item-info {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
}

.item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.item-name {
  flex: 1;
  font-size: 14px;
  color: #303133;
}

.item-price,
.item-subtotal {
  font-size: 16px;
  font-weight: bold;
  color: #F56C6C;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-top: 2px solid #409EFF;
  margin-top: 20px;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.total-info {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.total-info .label {
  font-size: 14px;
  color: #606266;
}

.total-info .count {
  color: #409EFF;
  font-weight: bold;
}

.total-price {
  font-size: 24px;
  font-weight: bold;
  color: #F56C6C;
}

.empty-cart {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>
```

### 路由配置

```javascript
{
  path: '/cart',
  name: 'cart',
  component: () => import('@/views/user/CartView.vue'),
  meta: { title: '购物车' }
}
```

### 验收标准

- [ ] 购物车列表展示正确
- [ ] 全选/取消全选功能正常
- [ ] 单个商品选择功能正常
- [ ] 数量调整功能正常（不超过库存）
- [ ] 删除商品功能正常（带确认）
- [ ] 清空购物车功能正常（带确认）
- [ ] 商品小计计算正确
- [ ] 总价计算正确（只计算选中商品）
- [ ] 去结算按钮正常（传递选中商品到订单页）
- [ ] 点击商品跳转到详情页
- [ ] 空购物车状态显示正确

---

**文档将继续包含**:
- M06: 订单确认模块
- M07: 订单列表模块
- M08: 个人中心模块
- M09-M13: 团长端5个模块

---

**下一部分**: T字开发_模块化拆解计划_Part3.md

