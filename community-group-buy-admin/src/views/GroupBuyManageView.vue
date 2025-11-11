<template>
  <div class="groupbuy-manage">
    <!-- 顶部统计面板 -->
    <div class="stats-panel">
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon blue">
              <el-icon :size="24"><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">活动总数</div>
              <div class="stat-value">{{ statistics.totalActivities || 0 }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon green">
              <el-icon :size="24"><SuccessFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">进行中</div>
              <div class="stat-value">{{ statistics.ongoingActivities || 0 }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon orange">
              <el-icon :size="24"><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">已结束</div>
              <div class="stat-value">{{ statistics.endedActivities || 0 }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon purple">
              <el-icon :size="24"><UserFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">活跃团长</div>
              <div class="stat-value">{{ statistics.activeLeaders || 0 }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 活动列表 -->
    <el-card class="activity-list-card" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h3 class="card-title">拼团活动管理</h3>
            <el-text type="info" size="small">共 {{ filteredActivities.length }} 个活动</el-text>
          </div>
          <div class="header-right">
            <el-button type="primary" @click="showCreateDialog" :icon="Plus" size="default">
              创建活动
            </el-button>
            <el-button @click="fetchActivities" :icon="RefreshRight" size="default">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 筛选工具栏 -->
      <div class="filter-toolbar">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品ID或活动ID..."
          :prefix-icon="Search"
          clearable
          style="width: 280px;"
        />
        <el-select v-model="filterStatus" placeholder="活动状态" clearable style="width: 140px; margin-left: 12px;">
          <el-option label="全部状态" :value="null" />
          <el-option label="未开始" :value="0" />
          <el-option label="进行中" :value="1" />
          <el-option label="已结束" :value="2" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 240px; margin-left: 12px;"
          clearable
        />
      </div>

      <!-- 列表视图 -->
      <div class="list-view">
        <div 
          v-for="activity in filteredActivities" 
          :key="activity.activityId" 
          class="activity-list-item"
          :class="'status-' + activity.status"
        >
          <!-- 左侧：活动ID和状态 -->
          <div class="item-left">
            <div class="activity-id-badge">
              <span class="id-label">ID</span>
              <span class="id-value">{{ activity.activityId }}</span>
            </div>
            <el-tag :type="getStatusType(activity.status)" size="large" effect="plain">
              {{ getStatusText(activity.status) }}
            </el-tag>
          </div>

          <!-- 中间：核心信息 -->
          <div class="item-center">
            <!-- 商品信息区域 -->
            <div class="product-area">
              <div class="product-image-wrapper">
                <el-image 
                  :src="getProductImage(activity.productId)" 
                  fit="cover"
                  class="product-image"
                  :preview-src-list="[getProductImage(activity.productId)]"
                >
                  <template #error>
                    <div class="image-slot">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
              </div>
              <div class="product-info">
                <div class="product-name">{{ getProductName(activity.productId) }}</div>
                <div class="product-meta">
                  <el-text type="info" size="small">ID: {{ activity.productId }}</el-text>
                </div>
              </div>
            </div>

            <!-- 价格区域 -->
            <div class="price-area">
              <div class="price-label">拼团价</div>
              <div class="price-value">¥{{ activity.groupPrice }}</div>
            </div>

            <!-- 信息网格 -->
            <div class="info-list">
              <div class="info-row">
                <div class="info-cell">
                  <el-icon class="info-icon"><User /></el-icon>
                  <span class="info-label">成团人数</span>
                  <span class="info-value">{{ activity.requiredNum }}人</span>
                </div>
                <div class="info-cell">
                  <el-icon class="info-icon"><User /></el-icon>
                  <span class="info-label">人数限制</span>
                  <span class="info-value">{{ activity.maxNum ? activity.maxNum + '人' : '无限制' }}</span>
                </div>
              </div>
              <div class="info-row">
                <div class="info-cell">
                  <el-icon class="info-icon"><Calendar /></el-icon>
                  <span class="info-label">开始时间</span>
                  <span class="info-value">{{ formatShortTime(activity.startTime) }}</span>
                </div>
                <div class="info-cell">
                  <el-icon class="info-icon"><Clock /></el-icon>
                  <span class="info-label">结束时间</span>
                  <span class="info-value">{{ formatShortTime(activity.endTime) }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 右侧：操作按钮 -->
          <div class="item-right">
            <el-button size="small" plain @click="showActivityDetail(activity)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button size="small" plain @click="showTeamList(activity)">
              <el-icon><UserFilled /></el-icon>
              团列表
            </el-button>
            <el-button size="small" type="primary" @click="showEditDialog(activity)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(activity)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>

        <!-- 空状态 -->
        <el-empty v-if="filteredActivities.length === 0" description="暂无活动数据" />
      </div>
    </el-card>
    
    <!-- 创建/编辑活动对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form :model="activityForm" :rules="formRules" ref="activityFormRef" label-width="120px">
        <el-form-item label="商品ID" prop="productId">
          <el-input-number 
            v-model="activityForm.productId" 
            :min="1" 
            style="width: 100%;"
            placeholder="请输入商品ID"
          />
          <div class="form-tip">请输入有效的商品ID</div>
        </el-form-item>
        
        <el-form-item label="拼团价" prop="groupPrice">
          <el-input-number 
            v-model="activityForm.groupPrice" 
            :precision="2" 
            :step="0.1" 
            :min="0" 
            style="width: 100%;"
            placeholder="请输入拼团价"
          />
          <div class="form-tip">拼团价必须小于商品原价</div>
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="成团人数" prop="requiredNum">
              <el-input-number 
                v-model="activityForm.requiredNum" 
                :min="2" 
                :max="10" 
                style="width: 100%;"
              />
              <div class="form-tip">2-10人</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大人数">
              <el-input-number 
                v-model="activityForm.maxNum" 
                :min="0" 
                style="width: 100%;"
                placeholder="不填则无限制"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="活动开始时间" prop="startTime">
          <el-date-picker
            v-model="activityForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%;"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="活动结束时间" prop="endTime">
          <el-date-picker
            v-model="activityForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%;"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="活动状态" prop="status" v-if="isEdit">
          <el-radio-group v-model="activityForm.status">
            <el-radio :value="0">未开始</el-radio>
            <el-radio :value="1">进行中</el-radio>
            <el-radio :value="2">已结束</el-radio>
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
    
    <!-- 活动详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="活动详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="活动ID">{{ detailActivity.activityId }}</el-descriptions-item>
        <el-descriptions-item label="商品ID">{{ detailActivity.productId }}</el-descriptions-item>
        <el-descriptions-item label="拼团价">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ detailActivity.groupPrice }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="成团人数">
          <el-tag type="warning">{{ detailActivity.requiredNum }}人</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最大人数">
          <el-tag v-if="detailActivity.maxNum">{{ detailActivity.maxNum }}人</el-tag>
          <el-tag v-else type="info">无限制</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="活动状态">
          <el-tag :type="getStatusType(detailActivity.status)">
            {{ getStatusText(detailActivity.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间" :span="2">
          {{ formatTime(detailActivity.startTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间" :span="2">
          {{ formatTime(detailActivity.endTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ detailActivity.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="二维码URL" :span="2" v-if="detailActivity.qrcodeUrl">
          {{ detailActivity.qrcodeUrl }}
        </el-descriptions-item>
        <el-descriptions-item label="分享链接" :span="2" v-if="detailActivity.link">
          {{ detailActivity.link }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
    
    <!-- 团列表对话框 -->
    <el-dialog
      v-model="teamListDialogVisible"
      title="团列表"
      width="1200px"
      top="5vh"
    >
      <div v-if="currentActivity.activityId">
        <div style="margin-bottom: 15px;">
          <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 12px;">
            <el-tag type="primary">活动ID: {{ currentActivity.activityId }}</el-tag>
            <el-tag type="success">商品: {{ getProductName(currentActivity.productId) }}</el-tag>
            <el-tag type="warning">拼团价: ¥{{ currentActivity.groupPrice }}</el-tag>
          </div>
          
          <!-- 团状态筛选工具栏 -->
          <div style="display: flex; align-items: center; gap: 12px; padding: 12px; background: #f5f7fa; border-radius: 6px;">
            <span style="font-size: 14px; color: #606266; font-weight: 500;">筛选条件：</span>
            <el-select v-model="teamStatusFilter" placeholder="团状态" clearable style="width: 140px;" @change="handleTeamStatusChange">
              <el-option label="全部状态" :value="null" />
              <el-option label="拼团中" :value="0" />
              <el-option label="已成团" :value="1" />
              <el-option label="已失败" :value="2" />
            </el-select>
            <el-checkbox v-model="includeExpiredTeams" @change="handleTeamStatusChange">
              包含已过期的团
            </el-checkbox>
            <div style="flex: 1"></div>
            <el-text type="info" size="small">
              共 {{ teamList.length }} 个团
            </el-text>
          </div>
        </div>
        
        <el-table 
          :data="teamList" 
          v-loading="teamLoading"
          border
          style="width: 100%"
        >
          <el-table-column prop="teamId" label="团ID" width="80" />
          <el-table-column prop="teamNo" label="团号" width="150" />
          <el-table-column label="团长" width="100">
            <template #default="{ row }">
              {{ row.leaderName || '未知' }}
            </template>
          </el-table-column>
          <el-table-column label="社区" width="120">
            <template #default="{ row }">
              {{ row.communityName || '未指定' }}
            </template>
          </el-table-column>
          <el-table-column label="进度" width="150">
            <template #default="{ row }">
              <el-progress 
                :percentage="Math.floor((row.currentNum / row.requiredNum) * 100)" 
                :status="row.teamStatus === 1 ? 'success' : ''"
              />
              <div style="text-align: center; font-size: 12px; margin-top: 4px;">
                {{ row.currentNum }}/{{ row.requiredNum }}人
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getTeamStatusType(row.teamStatus)">
                {{ row.teamStatusDesc || getTeamStatusText(row.teamStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="过期时间" width="180">
            <template #default="{ row }">
              {{ formatTime(row.expireTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button size="small" @click="showTeamDetail(row.teamId)">
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
    
    <!-- 团详情对话框 -->
    <el-dialog
      v-model="teamDetailDialogVisible"
      title="团详情"
      width="1000px"
      top="5vh"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="团ID">{{ teamDetail.teamId }}</el-descriptions-item>
        <el-descriptions-item label="团号">{{ teamDetail.teamNo }}</el-descriptions-item>
        <el-descriptions-item label="活动ID">{{ teamDetail.activityId }}</el-descriptions-item>
        <el-descriptions-item label="活动名称">{{ teamDetail.activityName }}</el-descriptions-item>
        <el-descriptions-item label="团长">{{ teamDetail.leaderName }}</el-descriptions-item>
        <el-descriptions-item label="社区">{{ teamDetail.communityName || '未指定' }}</el-descriptions-item>
        <el-descriptions-item label="拼团价">
          <span style="color: #f56c6c; font-weight: bold;">¥{{ teamDetail.groupPrice }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="团状态">
          <el-tag :type="getTeamStatusType(teamDetail.teamStatus)">
            {{ teamDetail.teamStatusDesc || getTeamStatusText(teamDetail.teamStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前/需要人数">
          {{ teamDetail.currentNum }}/{{ teamDetail.requiredNum }}人
        </el-descriptions-item>
        <el-descriptions-item label="还差人数">
          <el-tag type="warning">{{ teamDetail.remainNum }}人</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatTime(teamDetail.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="过期时间" :span="2">
          {{ formatTime(teamDetail.expireTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="成团时间" :span="2" v-if="teamDetail.successTime">
          {{ formatTime(teamDetail.successTime) }}
        </el-descriptions-item>
      </el-descriptions>
      
      <div style="margin-top: 24px;">
        <h4 style="margin-bottom: 16px; font-size: 16px; color: #303133;">
          <el-icon style="vertical-align: middle;"><User /></el-icon>
          成员列表 ({{ (teamDetail.members || []).length }}人)
        </h4>
        
        <!-- 成员卡片列表 -->
        <div class="member-grid">
          <div 
            v-for="member in teamDetail.members || []" 
            :key="member.memberId"
            class="member-card"
            :class="{ 'is-launcher': member.isLauncher === 1 }"
          >
            <!-- 发起人标识 -->
            <div v-if="member.isLauncher === 1" class="launcher-badge">
              <el-icon><Star /></el-icon>
              发起人
            </div>
            
            <div class="member-card-content">
              <!-- 头像 -->
              <div class="member-avatar">
                <el-avatar :size="60" :src="member.avatar">
                  <el-icon :size="30"><UserFilled /></el-icon>
                </el-avatar>
              </div>
              
              <!-- 信息 -->
              <div class="member-info">
                <div class="member-name">{{ member.realName || member.username }}</div>
                <div class="member-username">@{{ member.username }}</div>
                
        <div class="member-meta">
          <div class="meta-item">
            <el-icon><Clock /></el-icon>
            <span>{{ formatRelativeTime(member.joinTime) }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Wallet /></el-icon>
            <span>¥{{ member.payAmount }}</span>
          </div>
          <div class="meta-item" v-if="member.productQuantity && member.productQuantity > 0">
            <el-icon><ShoppingCart /></el-icon>
            <span>{{ member.productQuantity }}件商品</span>
          </div>
        </div>
                
                <div class="member-status">
                  <el-tag 
                    :type="getMemberStatusType(member.status)" 
                    size="small"
                    effect="plain"
                  >
                    {{ member.statusDesc || getMemberStatusText(member.status) }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, RefreshRight, Search, View, Edit, Delete,
  TrendCharts, SuccessFilled, CircleCheck, UserFilled,
  Goods, User, Calendar, Clock, Picture, Star, Wallet, InfoFilled, ShoppingCart
} from '@element-plus/icons-vue'
import {
  getActivityList,
  getActivityDetail,
  createActivity,
  updateActivity,
  deleteActivity,
  getActivityTeams,
  getTeamDetail
} from '../api/groupbuy'
import { getProductDetail } from '../api/product'

// 商品信息缓存
const productCache = ref({})

// 筛选条件
const searchKeyword = ref('')
const filterStatus = ref(null)
const dateRange = ref(null)

// 统计数据
const statistics = computed(() => {
  const total = activityList.value.length
  const ongoing = activityList.value.filter(a => a.status === 1).length
  const ended = activityList.value.filter(a => a.status === 2).length
  
  return {
    totalActivities: total,
    ongoingActivities: ongoing,
    endedActivities: ended,
    activeLeaders: 12 // 模拟数据，后续可从API获取
  }
})

// 过滤后的活动列表
const filteredActivities = computed(() => {
  let list = activityList.value

  // 关键词筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(item => 
      String(item.activityId).includes(keyword) ||
      String(item.productId).includes(keyword)
    )
  }

  // 状态筛选
  if (filterStatus.value !== null) {
    list = list.filter(item => item.status === filterStatus.value)
  }

  // 日期范围筛选
  if (dateRange.value && dateRange.value.length === 2) {
    const [start, end] = dateRange.value
    list = list.filter(item => {
      const createTime = new Date(item.createTime)
      return createTime >= start && createTime <= end
    })
  }

  return list
})

// 活动列表
const activityList = ref([])
const loading = ref(false)

// 获取商品信息
const fetchProductInfo = async (productId) => {
  // 如果已经缓存，直接返回
  if (productCache.value[productId]) {
    return productCache.value[productId]
  }
  
  try {
    const response = await getProductDetail(productId)
    if (response.code === 200 && response.data) {
      productCache.value[productId] = response.data
      return response.data
    }
  } catch (error) {
    console.error('获取商品信息失败:', error)
  }
  
  return null
}

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('创建活动')
const isEdit = ref(false)
const submitLoading = ref(false)

// 活动表单
const activityFormRef = ref(null)
const activityForm = reactive({
  activityId: null,
  productId: null,
  groupPrice: 0,
  requiredNum: 3,
  maxNum: null,
  startTime: '',
  endTime: '',
  status: 1
})

// 表单校验规则
const formRules = {
  productId: [
    { required: true, message: '请输入商品ID', trigger: 'blur' }
  ],
  groupPrice: [
    { required: true, message: '请输入拼团价', trigger: 'blur' }
  ],
  requiredNum: [
    { required: true, message: '请输入成团人数', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ]
}

// 活动详情对话框
const detailDialogVisible = ref(false)
const detailActivity = ref({})

// 团列表对话框
const teamListDialogVisible = ref(false)
const currentActivity = ref({})
const teamList = ref([])
const teamLoading = ref(false)
const teamStatusFilter = ref(0) // 默认显示拼团中
const includeExpiredTeams = ref(false) // 默认不包含已过期

// 团详情对话框
const teamDetailDialogVisible = ref(false)
const teamDetail = ref({})

// 加载活动列表
const fetchActivities = async () => {
  loading.value = true
  try {
    const res = await getActivityList()
    if (res.code === 200) {
      activityList.value = res.data || []
      
      // 预加载所有商品信息
      const productIds = [...new Set(activityList.value.map(a => a.productId))]
      await Promise.all(productIds.map(id => fetchProductInfo(id)))
    }
  } catch (error) {
    ElMessage.error('获取活动列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 显示创建对话框
const showCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '创建活动'
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑活动'
  Object.assign(activityForm, row)
  dialogVisible.value = true
}

// 显示活动详情
const showActivityDetail = async (row) => {
  try {
    const res = await getActivityDetail(row.activityId)
    if (res.code === 200) {
      detailActivity.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取活动详情失败')
  }
}

// 显示团列表
const showTeamList = async (row) => {
  currentActivity.value = row
  
  // 确保有商品信息
  if (!productCache.value[row.productId]) {
    await fetchProductInfo(row.productId)
  }
  
  // 重置筛选条件为默认值
  teamStatusFilter.value = 0 // 默认显示拼团中
  includeExpiredTeams.value = false // 默认不包含已过期
  
  teamListDialogVisible.value = true
  
  // 加载团列表
  await loadTeamList(row.activityId)
}

// 加载团列表（支持筛选）
const loadTeamList = async (activityId) => {
  teamLoading.value = true
  try {
    const params = {
      status: teamStatusFilter.value,
      includeExpired: includeExpiredTeams.value
    }
    
    const res = await getActivityTeams(activityId, params)
    if (res.code === 200) {
      teamList.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('获取团列表失败')
    console.error(error)
  } finally {
    teamLoading.value = false
  }
}

// 团状态筛选变化处理
const handleTeamStatusChange = () => {
  if (currentActivity.value.activityId) {
    loadTeamList(currentActivity.value.activityId)
  }
}

// 显示团详情
const showTeamDetail = async (teamId) => {
  try {
    const res = await getTeamDetail(teamId)
    if (res.code === 200) {
      teamDetail.value = res.data
      teamDetailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取团详情失败')
  }
}

// 提交表单
const handleSubmit = async () => {
  await activityFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 校验时间
    if (new Date(activityForm.startTime) >= new Date(activityForm.endTime)) {
      ElMessage.error('结束时间必须晚于开始时间')
      return
    }
    
    submitLoading.value = true
    try {
      const data = {
        productId: activityForm.productId,
        groupPrice: activityForm.groupPrice,
        requiredNum: activityForm.requiredNum,
        maxNum: activityForm.maxNum || null,
        startTime: activityForm.startTime,
        endTime: activityForm.endTime
      }
      
      if (isEdit.value) {
        data.status = activityForm.status
      }
      
      let res
      if (isEdit.value) {
        res = await updateActivity(activityForm.activityId, data)
      } else {
        res = await createActivity(data)
      }
      
      if (res.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        fetchActivities()
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
  activityFormRef.value?.resetFields()
  Object.assign(activityForm, {
    activityId: null,
    productId: null,
    groupPrice: 0,
    requiredNum: 3,
    maxNum: null,
    startTime: '',
    endTime: '',
    status: 1
  })
}

// 删除活动
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该活动吗？删除后不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    const res = await deleteActivity(row.activityId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchActivities()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ')
}

// 格式化简短时间（只显示日期）
const formatShortTime = (time) => {
  if (!time) return '-'
  return time.split('T')[0]
}

// 格式化相对时间
const formatRelativeTime = (time) => {
  if (!time) return '-'
  const date = new Date(time.replace('T', ' '))
  const now = new Date()
  const diff = now - date
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor(diff / (1000 * 60))
  
  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  if (minutes > 0) return `${minutes}分钟前`
  return '刚刚'
}

// 获取商品图片
const getProductImage = (productId) => {
  const product = productCache.value[productId]
  if (product && product.coverImg) {
    return product.coverImg
  }
  return '/placeholder-product.png'
}

// 获取商品名称
const getProductName = (productId) => {
  const product = productCache.value[productId]
  if (product && product.productName) {
    return product.productName
  }
  return `商品${productId}`
}

// 获取成员状态类型
const getMemberStatusType = (status) => {
  const typeMap = {
    0: 'warning',  // 待支付
    1: 'primary',  // 已支付
    2: 'success',  // 已成团
    3: 'info'      // 已取消
  }
  return typeMap[status] || 'info'
}

// 获取成员状态文本
const getMemberStatusText = (status) => {
  const textMap = {
    0: '待支付',
    1: '已支付',
    2: '已成团',
    3: '已取消'
  }
  return textMap[status] || '未知'
}

// 获取活动状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',     // 未开始
    1: 'success',  // 进行中
    2: 'warning',  // 已结束
    3: 'danger'    // 异常
  }
  return typeMap[status] || 'info'
}

// 获取活动状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '未开始',
    1: '进行中',
    2: '已结束',
    3: '异常'
  }
  return textMap[status] || '未知'
}

// 获取团状态类型
const getTeamStatusType = (status) => {
  const typeMap = {
    0: 'warning',  // 拼团中
    1: 'success',  // 已成团
    2: 'danger'    // 已失败
  }
  return typeMap[status] || 'info'
}

// 获取团状态文本
const getTeamStatusText = (status) => {
  const textMap = {
    0: '拼团中',
    1: '已成团',
    2: '已失败'
  }
  return textMap[status] || '未知'
}

// 初始化
onMounted(() => {
  fetchActivities()
})
</script>

<style scoped>
.groupbuy-manage {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

/* 统计面板样式 - 简洁版 */
.stats-panel {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border-left: 3px solid #dcdfe6;
}

.stat-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.blue {
  background: #e3f2fd;
  color: #2196f3;
  border: 1px solid #90caf9;
}

.stat-icon.green {
  background: #e8f5e9;
  color: #4caf50;
  border: 1px solid #81c784;
}

.stat-icon.orange {
  background: #fff3e0;
  color: #ff9800;
  border: 1px solid #ffb74d;
}

.stat-icon.purple {
  background: #f3e5f5;
  color: #9c27b0;
  border: 1px solid #ba68c8;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 26px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

/* 活动列表卡片 */
.activity-list-card {
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.card-title {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 筛选工具栏 */
.filter-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 14px 16px;
  background: #fafafa;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

/* 列表视图 */
.list-view {
  margin-top: 16px;
}

.activity-list-item {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 24px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
  position: relative;
}

.activity-list-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: #dcdfe6;
  border-radius: 8px 0 0 8px;
  transition: all 0.3s ease;
}

.activity-list-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transform: translateX(2px);
}

.activity-list-item.status-1::before {
  background: #67c23a;
}

.activity-list-item.status-0::before {
  background: #909399;
}

.activity-list-item.status-2::before {
  background: #e6a23c;
}

/* 左侧区域 */
.item-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  min-width: 100px;
}

.activity-id-badge {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 8px 16px;
  text-align: center;
  border: 1px solid #e4e7ed;
}

.id-label {
  display: block;
  font-size: 11px;
  color: #909399;
  margin-bottom: 4px;
}

.id-value {
  display: block;
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

/* 中间区域 */
.item-center {
  flex: 1;
  display: flex;
  gap: 24px;
  align-items: center;
}

/* 商品信息区域 */
.product-area {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-right: 20px;
  border-right: 1px solid #e4e7ed;
  min-width: 200px;
}

.product-image-wrapper {
  flex-shrink: 0;
}

.product-image {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

.product-image :deep(img) {
  transition: transform 0.3s ease;
}

.product-image:hover :deep(img) {
  transform: scale(1.1);
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.product-meta {
  font-size: 12px;
  color: #909399;
}

.price-area {
  text-align: center;
  padding: 0 20px;
  border-right: 1px solid #e4e7ed;
}

.price-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.price-value {
  font-size: 32px;
  font-weight: bold;
  color: #f56c6c;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  line-height: 1;
}

.info-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  gap: 24px;
}

.info-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
  min-width: 220px;
}

.info-icon {
  color: #909399;
  font-size: 16px;
}

.info-label {
  color: #909399;
  min-width: 60px;
}

.info-value {
  color: #303133;
  font-weight: 500;
}

/* 右侧操作区 */
.item-right {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 200px;
}

.item-right .el-button {
  justify-content: flex-start;
}

.item-right .el-button .el-icon {
  margin-right: 4px;
}

/* 表单提示 */
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

/* 空状态 */
.list-view .el-empty {
  padding: 60px 0;
}

/* 成员卡片网格 */
.member-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.member-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  position: relative;
}

.member-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.member-card.is-launcher {
  border-color: #f56c6c;
  background: linear-gradient(135deg, #fff 0%, #fff5f5 100%);
}

.launcher-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: linear-gradient(135deg, #ff6b6b 0%, #f56c6c 100%);
  color: white;
  padding: 4px 12px;
  font-size: 12px;
  border-radius: 0 0 0 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
  box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3);
}

.launcher-badge .el-icon {
  font-size: 14px;
}

.member-card-content {
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.member-avatar {
  flex-shrink: 0;
}

.member-avatar .el-avatar {
  border: 2px solid #e4e7ed;
  transition: all 0.3s ease;
}

.member-card.is-launcher .member-avatar .el-avatar {
  border-color: #f56c6c;
}

.member-avatar:hover .el-avatar {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-username {
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.meta-item .el-icon {
  color: #909399;
  font-size: 14px;
}

.member-status {
  display: flex;
  align-items: center;
}

/* 响应式优化 */
@media (max-width: 1600px) {
  .item-center {
    gap: 16px;
  }

  .info-cell {
    min-width: 180px;
  }
  
  .product-area {
    min-width: 180px;
  }
}

@media (max-width: 1200px) {
  .member-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}

/* 动画效果 */
@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.activity-list-item {
  animation: slideInUp 0.3s ease-out;
}

/* 对话框优化 */
:deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  padding: 18px 24px;
}

:deep(.el-dialog__title) {
  color: #303133;
  font-weight: 600;
  font-size: 16px;
}

/* 按钮组优化 */
:deep(.el-button-group) {
  display: inline-flex;
}

:deep(.el-button-group .el-button) {
  border-radius: 0;
}

:deep(.el-button-group .el-button:first-child) {
  border-radius: 4px 0 0 4px;
}

:deep(.el-button-group .el-button:last-child) {
  border-radius: 0 4px 4px 0;
}
</style>

