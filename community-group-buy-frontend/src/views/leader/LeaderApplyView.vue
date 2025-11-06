<template>
  <MainLayout>
    <div class="leader-apply-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <el-button :icon="ArrowLeft" @click="$router.back()">返回</el-button>
        <h2 class="page-title">
          <el-icon><User /></el-icon>
          申请成为团长
        </h2>
      </div>

      <!-- 申请状态卡片 -->
      <div v-if="applicationStatus" class="status-card" :class="`status-${applicationStatus.status}`">
        <div class="status-icon">
          <el-icon v-if="applicationStatus.status === 0"><Clock /></el-icon>
          <el-icon v-else-if="applicationStatus.status === 1"><CircleCheck /></el-icon>
          <el-icon v-else-if="applicationStatus.status === 2"><CircleClose /></el-icon>
        </div>
        <div class="status-content">
          <h3 class="status-title">
            {{ statusTextMap[applicationStatus.status]?.title }}
          </h3>
          <p class="status-desc">
            {{ statusTextMap[applicationStatus.status]?.desc }}
          </p>
          <div v-if="applicationStatus.reviewComment" class="status-comment">
            <el-text type="info">审核意见：{{ applicationStatus.reviewComment }}</el-text>
          </div>
          <div class="status-actions">
            <el-button
              v-if="applicationStatus.status === 0"
              type="primary"
              :icon="View"
              @click="viewApplication"
            >
              查看申请详情
            </el-button>
            <el-button
              v-if="applicationStatus.status === 2"
              type="danger"
              :icon="RefreshRight"
              @click="reapply"
            >
              重新申请
            </el-button>
            <el-button
              v-if="applicationStatus.status === 1"
              type="success"
              :icon="TrophyBase"
              @click="$router.push('/profile')"
            >
              前往个人中心
            </el-button>
          </div>
        </div>
      </div>

      <!-- 申请须知 -->
      <el-card class="notice-card" v-if="!applicationStatus || applicationStatus.status === 2">
        <template #header>
          <div class="card-header">
            <el-icon><InfoFilled /></el-icon>
            <span>申请须知</span>
          </div>
        </template>
        <ul class="notice-list">
          <li>✨ 团长需要为社区居民提供优质的团购服务</li>
          <li>📦 负责商品的接收、分拣和配送工作</li>
          <li>💰 每笔订单可获得相应比例的佣金（默认10%）</li>
          <li>🏘️ 一个社区可以有多个团长，但一个团长只能服务一个社区</li>
          <li>📱 需要提供真实的联系方式，方便用户联系</li>
          <li>⏰ 提交申请后，管理员将在1-3个工作日内审核</li>
        </ul>
      </el-card>

      <!-- 申请表单 -->
      <el-card class="form-card" v-if="!applicationStatus || applicationStatus.status === 2">
        <template #header>
          <div class="card-header">
            <el-icon><Edit /></el-icon>
            <span>填写申请信息</span>
          </div>
        </template>

        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="120px"
          label-position="left"
        >
          <!-- 团长基本信息 -->
          <div class="form-section">
            <h3 class="section-title">
              <el-icon><User /></el-icon>
              团长基本信息
            </h3>

            <el-form-item label="团长姓名" prop="leaderName">
              <el-input
                v-model="formData.leaderName"
                placeholder="请输入真实姓名"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>

            <el-form-item label="联系电话" prop="leaderPhone">
              <el-input
                v-model="formData.leaderPhone"
                placeholder="请输入手机号"
                :prefix-icon="Phone"
                maxlength="11"
                clearable
              />
            </el-form-item>
          </div>

          <!-- 团点信息 -->
          <div class="form-section">
            <h3 class="section-title">
              <el-icon><Shop /></el-icon>
              团点信息
            </h3>

            <el-form-item label="团点名称" prop="storeName">
              <el-input
                v-model="formData.storeName"
                placeholder="例如：张团长的团点"
                :prefix-icon="Shop"
                clearable
              />
              <template #extra>
                <el-text size="small" type="info">
                  团点名称将展示给用户，建议使用容易识别的名称
                </el-text>
              </template>
            </el-form-item>

            <el-form-item label="团点地址" prop="address">
              <el-input
                v-model="formData.address"
                type="textarea"
                :rows="2"
                placeholder="请输入团点详细地址（例如：小区1号楼101室）"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>

            <el-form-item label="团点简介">
              <el-input
                v-model="formData.description"
                type="textarea"
                :rows="3"
                placeholder="简单介绍一下您的团点优势（选填）"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </div>

          <!-- 社区选择 -->
          <div class="form-section">
            <h3 class="section-title">
              <el-icon><MapLocation /></el-icon>
              选择服务社区
            </h3>

            <el-form-item label="所属社区" prop="communityId">
              <div class="community-selection">
                <el-select
                  v-model="formData.communityId"
                  placeholder="请选择您要服务的社区"
                  filterable
                  clearable
                  style="flex: 1"
                  :loading="communityLoading"
                  @change="handleCommunityChange"
                >
                  <el-option
                    v-for="community in communityList"
                    :key="community.communityId"
                    :label="`${community.name} (${community.address})`"
                    :value="community.communityId"
                  >
                    <div class="community-option">
                      <span class="community-name">{{ community.name }}</span>
                      <span class="community-address">{{ community.address }}</span>
                    </div>
                  </el-option>
                </el-select>

                <div class="community-actions">
                  <el-button
                    size="small"
                    :icon="Location"
                    :loading="locatingCommunity"
                    @click="matchNearestCommunity"
                  >
                    定位最近社区
                  </el-button>
                  <el-button
                    size="small"
                    :icon="MapLocation"
                    :loading="matchingByAddress"
                    @click="matchByDefaultAddress"
                    :disabled="!hasDefaultAddress"
                  >
                    按收货地址匹配
                  </el-button>
                </div>

                <div class="community-tip">
                  <el-alert
                    type="info"
                    :closable="false"
                    show-icon
                  >
                    <template #title>
                      <div class="alert-content">
                        <span>💡 提示：您可以通过定位或收货地址自动匹配最近的社区</span>
                        <el-button
                          text
                          type="primary"
                          size="small"
                          :icon="Plus"
                          @click="showApplyCommunityDialog = true"
                        >
                          没有合适的社区？申请社区支持
                        </el-button>
                      </div>
                    </template>
                  </el-alert>
                </div>
              </div>
            </el-form-item>

            <!-- 显示选中社区的详细信息 -->
            <div v-if="selectedCommunity" class="selected-community-info">
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="社区名称">
                  {{ selectedCommunity.name }}
                </el-descriptions-item>
                <el-descriptions-item label="服务范围">
                  {{ selectedCommunity.serviceRadius }}米
                </el-descriptions-item>
                <el-descriptions-item label="社区地址" :span="2">
                  {{ selectedCommunity.address }}
                </el-descriptions-item>
                <el-descriptions-item label="社区简介" :span="2">
                  {{ selectedCommunity.description || '暂无简介' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>

          <!-- 提交按钮 -->
          <el-form-item>
            <div class="submit-section">
              <el-button
                type="primary"
                size="large"
                :loading="submitting"
                @click="handleSubmit"
                :icon="Check"
              >
                提交申请
              </el-button>
              <el-button
                size="large"
                @click="handleReset"
                :icon="RefreshLeft"
              >
                重置表单
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 社区申请对话框 -->
      <el-dialog
        v-model="showApplyCommunityDialog"
        title="申请社区支持"
        width="700px"
        :close-on-click-modal="false"
      >
        <el-alert
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        >
          如果您所在的社区还未开通服务，可以填写以下信息申请社区支持。管理员审核通过后，您的社区将会被添加到系统中。
        </el-alert>

        <el-form
          ref="communityFormRef"
          :model="communityFormData"
          :rules="communityRules"
          label-width="120px"
        >
          <el-form-item label="社区名称" prop="communityName">
            <el-input
              v-model="communityFormData.communityName"
              placeholder="请输入社区名称"
              clearable
            />
          </el-form-item>

          <el-form-item label="省份" prop="province">
            <el-input
              v-model="communityFormData.province"
              placeholder="例如：北京市"
              clearable
            />
          </el-form-item>

          <el-form-item label="城市" prop="city">
            <el-input
              v-model="communityFormData.city"
              placeholder="例如：北京市"
              clearable
            />
          </el-form-item>

          <el-form-item label="区县" prop="district">
            <el-input
              v-model="communityFormData.district"
              placeholder="例如：朝阳区"
              clearable
            />
          </el-form-item>

          <el-form-item label="详细地址" prop="address">
            <el-input
              v-model="communityFormData.address"
              type="textarea"
              :rows="2"
              placeholder="请输入社区详细地址"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="位置定位">
            <div class="location-section">
              <div class="location-input-group">
                <el-input
                  v-model="communityFormData.longitude"
                  placeholder="经度"
                  type="number"
                  step="0.000001"
                  style="width: 48%"
                >
                  <template #prepend>经度</template>
                </el-input>
                <el-input
                  v-model="communityFormData.latitude"
                  placeholder="纬度"
                  type="number"
                  step="0.000001"
                  style="width: 48%"
                >
                  <template #prepend>纬度</template>
                </el-input>
              </div>
              <el-button
                size="small"
                :icon="Location"
                :loading="gettingLocation"
                @click="getCommunityLocation"
              >
                获取当前位置
              </el-button>
            </div>
          </el-form-item>

          <el-form-item label="申请理由" prop="applicationReason">
            <el-input
              v-model="communityFormData.applicationReason"
              type="textarea"
              :rows="3"
              placeholder="请说明申请该社区的理由"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="showApplyCommunityDialog = false">取消</el-button>
          <el-button
            type="primary"
            :loading="submittingCommunity"
            @click="handleCommunitySubmit"
          >
            提交社区申请
          </el-button>
        </template>
      </el-dialog>
    </div>
  </MainLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  User,
  Edit,
  InfoFilled,
  Shop,
  MapLocation,
  Location,
  Plus,
  Check,
  RefreshLeft,
  Phone,
  Clock,
  CircleCheck,
  CircleClose,
  View,
  RefreshRight,
  TrophyBase
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import { getAddressList } from '@/api/user'
import {
  getCommunityList,
  getNearestCommunity,
  submitLeaderApplication,
  getMyLeaderInfo,
  submitCommunityApplication
} from '@/api/leader'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const communityFormRef = ref()

// 加载状态
const loading = ref(false)
const submitting = ref(false)
const communityLoading = ref(false)
const locatingCommunity = ref(false)
const matchingByAddress = ref(false)
const gettingLocation = ref(false)
const submittingCommunity = ref(false)

// 对话框
const showApplyCommunityDialog = ref(false)

// 社区列表和地址
const communityList = ref([])
const userAddresses = ref([])
const selectedCommunity = ref(null)
const applicationStatus = ref(null)

// 状态文本映射
const statusTextMap = {
  0: {
    title: '申请审核中',
    desc: '您的团长申请已提交，管理员正在审核中，请耐心等待...'
  },
  1: {
    title: '申请已通过',
    desc: '恭喜您！您的团长申请已通过审核，现在您可以发起拼团活动了！'
  },
  2: {
    title: '申请未通过',
    desc: '很遗憾，您的团长申请未通过审核。您可以查看审核意见后重新申请。'
  }
}

// 计算是否有默认地址
const hasDefaultAddress = computed(() => {
  return userAddresses.value.some(addr => addr.isDefault === 1 || addr.isDefault === true)
})

// 团长申请表单
const formData = reactive({
  leaderName: '',
  leaderPhone: '',
  communityId: null,
  storeName: '',
  address: '',
  description: ''
})

// 社区申请表单
const communityFormData = reactive({
  communityName: '',
  province: '',
  city: '',
  district: '',
  address: '',
  latitude: 0,
  longitude: 0,
  applicationReason: ''
})

// 表单验证规则
const rules = {
  leaderName: [
    { required: true, message: '请输入团长姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2-20个字符', trigger: 'blur' }
  ],
  leaderPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  communityId: [
    { required: true, message: '请选择服务社区', trigger: 'change' }
  ],
  storeName: [
    { required: true, message: '请输入团点名称', trigger: 'blur' },
    { min: 2, max: 50, message: '团点名称长度在2-50个字符', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入团点地址', trigger: 'blur' },
    { min: 5, max: 200, message: '地址长度在5-200个字符', trigger: 'blur' }
  ]
}

// 社区申请验证规则
const communityRules = {
  communityName: [
    { required: true, message: '请输入社区名称', trigger: 'blur' }
  ],
  province: [
    { required: true, message: '请输入省份', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入城市', trigger: 'blur' }
  ],
  district: [
    { required: true, message: '请输入区县', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ],
  applicationReason: [
    { required: true, message: '请说明申请理由', trigger: 'blur' }
  ]
}

// 初始化表单数据
const initFormData = () => {
  if (userStore.userInfo) {
    formData.leaderName = userStore.userInfo.realName || ''
    formData.leaderPhone = userStore.userInfo.phone || ''
    formData.communityId = userStore.userInfo.communityId || null
  }
}

// 加载社区列表
const loadCommunityList = async () => {
  communityLoading.value = true
  try {
    const res = await getCommunityList()
    if (res.code === 200) {
      communityList.value = res.data || []
    } else {
      ElMessage.error('加载社区列表失败')
    }
  } catch (error) {
    console.error('加载社区列表失败:', error)
    ElMessage.error('加载社区列表失败')
  } finally {
    communityLoading.value = false
  }
}

// 加载用户地址列表
const loadUserAddresses = async () => {
  if (!userStore.userInfo?.userId) return
  
  try {
    const res = await getAddressList(userStore.userInfo.userId)
    if (res.code === 200) {
      userAddresses.value = res.data || []
    }
  } catch (error) {
    console.error('加载地址列表失败:', error)
  }
}

// 加载申请状态
const loadApplicationStatus = async () => {
  if (!userStore.userInfo?.userId) return
  
  try {
    const res = await getMyLeaderInfo(userStore.userInfo.userId)
    if (res.code === 200 && res.data) {
      applicationStatus.value = res.data
      // 如果已经是团长，直接跳转
      if (res.data.status === 1) {
        ElMessage.success('您已经是团长了！')
      }
    }
  } catch (error) {
    console.error('加载申请状态失败:', error)
  }
}

// 社区改变时更新选中的社区信息
const handleCommunityChange = (communityId) => {
  if (communityId) {
    selectedCommunity.value = communityList.value.find(c => c.communityId === communityId)
  } else {
    selectedCommunity.value = null
  }
}

// 根据定位匹配最近的社区
const matchNearestCommunity = () => {
  if (!navigator.geolocation) {
    ElMessage.error('您的浏览器不支持地理定位')
    return
  }

  locatingCommunity.value = true
  navigator.geolocation.getCurrentPosition(
    async (position) => {
      try {
        const res = await getNearestCommunity({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude
        })
        
        if (res.code === 200 && res.data) {
          formData.communityId = res.data.communityId
          selectedCommunity.value = res.data
          ElMessage.success(`已自动匹配到最近的社区：${res.data.name}`)
        } else {
          ElMessage.warning('未找到附近的社区，请手动选择或申请社区支持')
        }
      } catch (error) {
        console.error('匹配社区失败:', error)
        ElMessage.error('匹配社区失败，请稍后重试')
      } finally {
        locatingCommunity.value = false
      }
    },
    (error) => {
      locatingCommunity.value = false
      let errorMsg = '定位失败'
      switch (error.code) {
        case error.PERMISSION_DENIED:
          errorMsg = '用户拒绝了定位请求，请手动选择社区'
          break
        case error.POSITION_UNAVAILABLE:
          errorMsg = '位置信息不可用'
          break
        case error.TIMEOUT:
          errorMsg = '定位请求超时'
          break
      }
      ElMessage.error(errorMsg)
    },
    {
      enableHighAccuracy: true,
      timeout: 5000,
      maximumAge: 0
    }
  )
}

// 根据默认收货地址匹配最近的社区
const matchByDefaultAddress = async () => {
  const defaultAddress = userAddresses.value.find(
    addr => addr.isDefault === 1 || addr.isDefault === true
  )
  
  if (!defaultAddress) {
    ElMessage.warning('您还没有设置默认收货地址')
    return
  }

  if (!defaultAddress.latitude || !defaultAddress.longitude) {
    ElMessage.warning('默认地址缺少经纬度信息，请重新编辑地址并获取定位')
    return
  }

  matchingByAddress.value = true
  try {
    const res = await getNearestCommunity({
      latitude: defaultAddress.latitude,
      longitude: defaultAddress.longitude
    })
    
    if (res.code === 200 && res.data) {
      formData.communityId = res.data.communityId
      selectedCommunity.value = res.data
      ElMessage.success(`已根据收货地址匹配到最近的社区：${res.data.name}`)
    } else {
      ElMessage.warning('未找到附近的社区，请手动选择或申请社区支持')
    }
  } catch (error) {
    console.error('匹配社区失败:', error)
    ElMessage.error('匹配社区失败，请稍后重试')
  } finally {
    matchingByAddress.value = false
  }
}

// 获取社区申请的位置
const getCommunityLocation = () => {
  if (!navigator.geolocation) {
    ElMessage.error('您的浏览器不支持地理定位')
    return
  }

  gettingLocation.value = true
  navigator.geolocation.getCurrentPosition(
    (position) => {
      communityFormData.latitude = position.coords.latitude
      communityFormData.longitude = position.coords.longitude
      ElMessage.success(`定位成功！经度: ${communityFormData.longitude.toFixed(6)}, 纬度: ${communityFormData.latitude.toFixed(6)}`)
      gettingLocation.value = false
    },
    (error) => {
      gettingLocation.value = false
      ElMessage.error('定位失败，请稍后重试')
    },
    {
      enableHighAccuracy: true,
      timeout: 5000,
      maximumAge: 0
    }
  )
}

// 提交团长申请
const handleSubmit = async () => {
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    // 二次确认
    const confirmed = await ElMessageBox.confirm(
      '提交后需要等待管理员审核，确认要提交申请吗？',
      '确认提交',
      {
        type: 'info',
        confirmButtonText: '确定提交',
        cancelButtonText: '再检查一下'
      }
    ).catch(() => false)

    if (!confirmed) return

    submitting.value = true
    try {
      const data = {
        leaderId: userStore.userInfo.userId,
        leaderName: formData.leaderName,
        leaderPhone: formData.leaderPhone,
        communityId: formData.communityId,
        storeName: formData.storeName,
        address: formData.address,
        description: formData.description || ''
      }

      const res = await submitLeaderApplication(data)
      if (res.code === 200) {
        ElMessage.success('申请提交成功！请等待管理员审核')
        // 重新加载申请状态
        await loadApplicationStatus()
      } else {
        ElMessage.error(res.message || '申请提交失败')
      }
    } catch (error) {
      console.error('提交申请失败:', error)
      ElMessage.error('申请提交失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

// 提交社区申请
const handleCommunitySubmit = async () => {
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    ElMessage.warning('请先登录')
    return
  }

  await communityFormRef.value.validate(async (valid) => {
    if (!valid) return

    submittingCommunity.value = true
    try {
      const data = {
        applicantId: userStore.userInfo.userId,
        applicantName: userStore.userInfo.realName || userStore.userInfo.username,
        applicantPhone: userStore.userInfo.phone,
        communityName: communityFormData.communityName,
        province: communityFormData.province,
        city: communityFormData.city,
        district: communityFormData.district,
        address: communityFormData.address,
        latitude: communityFormData.latitude,
        longitude: communityFormData.longitude,
        applicationReason: communityFormData.applicationReason
      }

      const res = await submitCommunityApplication(data)
      if (res.code === 200) {
        ElMessage.success('社区申请提交成功！管理员审核通过后您可以选择该社区')
        showApplyCommunityDialog.value = false
        // 清空表单
        Object.keys(communityFormData).forEach(key => {
          if (typeof communityFormData[key] === 'string') {
            communityFormData[key] = ''
          } else {
            communityFormData[key] = 0
          }
        })
      } else {
        ElMessage.error(res.message || '社区申请提交失败')
      }
    } catch (error) {
      console.error('提交社区申请失败:', error)
      ElMessage.error('社区申请提交失败，请稍后重试')
    } finally {
      submittingCommunity.value = false
    }
  })
}

// 重置表单
const handleReset = () => {
  formRef.value.resetFields()
  initFormData()
  selectedCommunity.value = null
}

// 查看申请详情
const viewApplication = () => {
  if (applicationStatus.value) {
    ElMessageBox.alert(
      `<div style="line-height: 1.8;">
        <p><strong>申请人：</strong>${applicationStatus.value.leaderName}</p>
        <p><strong>联系电话：</strong>${applicationStatus.value.leaderPhone}</p>
        <p><strong>团点名称：</strong>${applicationStatus.value.storeName}</p>
        <p><strong>团点地址：</strong>${applicationStatus.value.address}</p>
        <p><strong>所属社区：</strong>${applicationStatus.value.communityName || '未知'}</p>
        <p><strong>申请时间：</strong>${applicationStatus.value.createdAt || '未知'}</p>
        ${applicationStatus.value.reviewComment ? `<p><strong>审核意见：</strong>${applicationStatus.value.reviewComment}</p>` : ''}
      </div>`,
      '申请详情',
      {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '关闭'
      }
    )
  }
}

// 重新申请
const reapply = async () => {
  const confirmed = await ElMessageBox.confirm(
    '重新申请前建议您先查看审核意见，针对问题进行改进。确定要重新申请吗？',
    '重新申请',
    {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }
  ).catch(() => false)

  if (!confirmed) return

  // 清空申请状态，显示表单
  applicationStatus.value = null
  ElMessage.info('请重新填写申请信息')
}

onMounted(async () => {
  // 检查用户是否登录
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  // 加载数据
  initFormData()
  await Promise.all([
    loadCommunityList(),
    loadUserAddresses(),
    loadApplicationStatus()
  ])
})
</script>

<style scoped>
.leader-apply-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

/* 页面头部 */
.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 26px;
  font-weight: 700;
  margin: 0;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 申请状态卡片 */
.status-card {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-left: 4px solid;
}

.status-card.status-0 {
  border-left-color: #e6a23c;
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.05) 0%, rgba(255, 255, 255, 1) 100%);
}

.status-card.status-1 {
  border-left-color: #67c23a;
  background: linear-gradient(135deg, rgba(103, 194, 58, 0.05) 0%, rgba(255, 255, 255, 1) 100%);
}

.status-card.status-2 {
  border-left-color: #f56c6c;
  background: linear-gradient(135deg, rgba(245, 108, 108, 0.05) 0%, rgba(255, 255, 255, 1) 100%);
}

.status-icon {
  font-size: 48px;
  flex-shrink: 0;
}

.status-0 .status-icon {
  color: #e6a23c;
}

.status-1 .status-icon {
  color: #67c23a;
}

.status-2 .status-icon {
  color: #f56c6c;
}

.status-content {
  flex: 1;
}

.status-title {
  font-size: 20px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #333;
}

.status-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 16px 0;
  line-height: 1.6;
}

.status-comment {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}

.status-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.status-actions :deep(.el-button) {
  border-radius: 12px;
  padding: 10px 20px;
  font-weight: 600;
}

/* 卡片样式 */
.notice-card,
.form-card {
  margin-bottom: 24px;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

/* 申请须知 */
.notice-list {
  margin: 0;
  padding-left: 20px;
  line-height: 2;
  color: #666;
}

.notice-list li {
  margin-bottom: 8px;
}

/* 表单区域 */
.form-section {
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 1px dashed #e0e0e0;
}

.form-section:last-of-type {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  margin: 0 0 20px 0;
  color: #333;
  padding-left: 8px;
  border-left: 3px solid #f093fb;
}

/* 社区选择样式 */
.community-selection {
  width: 100%;
}

.community-option {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.community-name {
  font-weight: 600;
  color: #333;
}

.community-address {
  font-size: 12px;
  color: #999;
}

.community-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.community-actions :deep(.el-button) {
  border-radius: 8px;
  font-size: 13px;
}

.community-tip {
  margin-top: 12px;
}

.alert-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.selected-community-info {
  margin-top: 16px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 12px;
}

/* 位置定位样式 */
.location-section {
  width: 100%;
}

.location-input-group {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

/* 提交区域 */
.submit-section {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  padding-top: 20px;
}

.submit-section :deep(.el-button) {
  border-radius: 12px;
  padding: 14px 32px;
  font-weight: 600;
  font-size: 15px;
}

.submit-section :deep(.el-button--primary) {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.3);
}

.submit-section :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(240, 147, 251, 0.4);
}

/* 表单样式 */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.2);
}

:deep(.el-textarea__inner) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

:deep(.el-select) {
  width: 100%;
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 16px;
}

:deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .leader-apply-container {
    padding: 16px;
  }

  .page-title {
    font-size: 20px;
  }

  .status-card {
    flex-direction: column;
    padding: 20px;
  }

  .status-icon {
    font-size: 36px;
  }

  .form-card :deep(.el-card__body) {
    padding: 16px;
  }

  :deep(.el-form) {
    --el-form-label-width: 100px;
  }

  .submit-section {
    flex-direction: column;
  }

  .submit-section :deep(.el-button) {
    width: 100%;
  }

  .community-actions {
    flex-direction: column;
  }

  .community-actions :deep(.el-button) {
    width: 100%;
  }

  .location-input-group {
    flex-direction: column;
  }

  .location-input-group :deep(.el-input) {
    width: 100% !important;
  }
}
</style>

