<template>
  <div class="community-apply">
    <!-- 顶部导航 -->
    <TopNav />
    
    <div class="apply-container">
      <!-- 页面头部 -->
      <div class="page-header">
        <h1>申请社区</h1>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/leader/dashboard' }">工作台</el-breadcrumb-item>
          <el-breadcrumb-item>申请社区</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      
      <!-- 申请表单 -->
      <el-card v-if="!submitted" class="apply-form-card">
        <template #header>
          <span class="card-title">
            <el-icon><OfficeBuilding /></el-icon>
            申请新社区
          </span>
        </template>
        
        <el-form 
          :model="applyForm" 
          :rules="applyRules"
          ref="applyFormRef"
          label-width="120px"
          class="apply-form"
        >
          <el-form-item label="社区名称" prop="communityName">
            <el-input 
              v-model="applyForm.communityName" 
              placeholder="请输入社区名称"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="省份" prop="province">
            <el-select 
              v-model="applyForm.province" 
              placeholder="请选择省份"
              @change="handleProvinceChange"
            >
              <el-option label="北京市" value="北京市" />
              <el-option label="上海市" value="上海市" />
              <el-option label="天津市" value="天津市" />
              <el-option label="重庆市" value="重庆市" />
              <el-option label="河北省" value="河北省" />
              <el-option label="山东省" value="山东省" />
              <el-option label="广东省" value="广东省" />
              <!-- 更多省份... -->
            </el-select>
          </el-form-item>
          
          <el-form-item label="城市" prop="city">
            <el-select 
              v-model="applyForm.city" 
              placeholder="请选择城市"
              :disabled="!applyForm.province"
              @change="handleCityChange"
            >
              <el-option 
                v-for="city in availableCities" 
                :key="city"
                :label="city" 
                :value="city" 
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="区/县" prop="district">
            <el-select 
              v-model="applyForm.district" 
              placeholder="请选择区/县"
              :disabled="!applyForm.city"
            >
              <el-option 
                v-for="district in availableDistricts" 
                :key="district"
                :label="district" 
                :value="district" 
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="详细地址" prop="address">
            <el-input 
              v-model="applyForm.address" 
              type="textarea"
              :rows="3"
              placeholder="请输入详细地址，如小区名称、门牌号等"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="申请说明" prop="description">
            <el-input 
              v-model="applyForm.description" 
              type="textarea"
              :rows="4"
              placeholder="请说明申请理由，如社区规模、团购需求等"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="submitApply" size="large">
              {{ submitting ? '提交中...' : '提交申请' }}
            </el-button>
            <el-button @click="resetForm" size="large">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <!-- 申请成功 -->
      <el-card v-else class="success-card">
        <el-result 
          icon="success" 
          title="申请已提交" 
          sub-title="等待管理员审核，请耐心等待"
        >
          <template #extra>
            <el-button type="primary" @click="viewStatus">
              <el-icon><View /></el-icon>
              查看申请状态
            </el-button>
            <el-button @click="applyAgain">
              <el-icon><Plus /></el-icon>
              继续申请
            </el-button>
          </template>
        </el-result>
      </el-card>
      
      <!-- 申请记录 -->
      <el-card class="records-card">
        <template #header>
          <span class="card-title">
            <el-icon><List /></el-icon>
            我的申请记录（{{ applications.length }}）
          </span>
        </template>
        
        <el-table 
          v-if="applications.length"
          :data="applications" 
          stripe
          style="width: 100%"
        >
          <el-table-column prop="communityName" label="社区名称" width="180" />
          <el-table-column label="地址" min-width="300">
            <template #default="{ row }">
              {{ row.province }} {{ row.city }} {{ row.district }} {{ row.address }}
            </template>
          </el-table-column>
          <el-table-column label="申请说明" min-width="200">
            <template #default="{ row }">
              <el-text line-clamp="2">{{ row.description }}</el-text>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="large">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="申请时间" width="180" />
          <el-table-column label="操作" width="100" fixed="right" align="center">
            <template #default="{ row }">
              <el-button 
                v-if="row.status === 2"
                size="small" 
                type="info"
                link
                @click="viewRejectReason(row)"
              >
                <el-icon><View /></el-icon>
                查看原因
              </el-button>
              <el-text v-else type="info" size="small">-</el-text>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty 
          v-else 
          description="暂无申请记录" 
          :image-size="100"
        >
          <el-button type="primary" @click="scrollToTop">
            立即申请
          </el-button>
        </el-empty>
      </el-card>
      
      <!-- 拒绝原因对话框 -->
      <el-dialog 
        v-model="rejectDialogVisible" 
        title="拒绝原因" 
        width="500px"
      >
        <div class="reject-content">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="社区名称">
              {{ selectedApplication?.communityName }}
            </el-descriptions-item>
            <el-descriptions-item label="申请时间">
              {{ selectedApplication?.createTime }}
            </el-descriptions-item>
            <el-descriptions-item label="审核时间">
              {{ selectedApplication?.auditTime }}
            </el-descriptions-item>
            <el-descriptions-item label="拒绝原因">
              <el-text type="danger">{{ selectedApplication?.rejectReason }}</el-text>
            </el-descriptions-item>
          </el-descriptions>
        </div>
        <template #footer>
          <el-button @click="rejectDialogVisible = false">关闭</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useLeaderStore } from '@/stores/leader'
import TopNav from '@/components/common/TopNav.vue'
import { ElMessage } from 'element-plus'
import {
  OfficeBuilding,
  View,
  Plus,
  List
} from '@element-plus/icons-vue'

const router = useRouter()
const leaderStore = useLeaderStore()

const submitted = ref(false)
const submitting = ref(false)
const applications = ref([])
const applyFormRef = ref(null)
const rejectDialogVisible = ref(false)
const selectedApplication = ref(null)

const applyForm = ref({
  communityName: '',
  province: '',
  city: '',
  district: '',
  address: '',
  description: ''
})

const applyRules = {
  communityName: [
    { required: true, message: '请输入社区名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2-50个字符', trigger: 'blur' }
  ],
  province: [
    { required: true, message: '请选择省份', trigger: 'change' }
  ],
  city: [
    { required: true, message: '请选择城市', trigger: 'change' }
  ],
  district: [
    { required: true, message: '请选择区/县', trigger: 'change' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在5-200个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入申请说明', trigger: 'blur' },
    { min: 10, max: 500, message: '长度在10-500个字符', trigger: 'blur' }
  ]
}

// Mock地区数据
const cityData = {
  '北京市': ['北京市'],
  '上海市': ['上海市'],
  '天津市': ['天津市'],
  '重庆市': ['重庆市'],
  '河北省': ['石家庄市', '唐山市', '秦皇岛市'],
  '山东省': ['济南市', '青岛市', '烟台市'],
  '广东省': ['广州市', '深圳市', '珠海市']
}

const districtData = {
  '北京市': ['东城区', '西城区', '朝阳区', '海淀区', '丰台区'],
  '上海市': ['黄浦区', '徐汇区', '长宁区', '静安区', '浦东新区'],
  '济南市': ['历下区', '市中区', '槐荫区', '天桥区', '历城区'],
  '广州市': ['越秀区', '荔湾区', '天河区', '白云区', '海珠区'],
  '深圳市': ['罗湖区', '福田区', '南山区', '宝安区', '龙岗区']
}

const availableCities = computed(() => {
  return cityData[applyForm.value.province] || []
})

const availableDistricts = computed(() => {
  return districtData[applyForm.value.city] || []
})

// 加载申请记录
onMounted(async () => {
  try {
    applications.value = await leaderStore.fetchApplications()
  } catch (error) {
    console.error('加载申请记录失败', error)
  }
})

// 省份变化
const handleProvinceChange = () => {
  applyForm.value.city = ''
  applyForm.value.district = ''
}

// 城市变化
const handleCityChange = () => {
  applyForm.value.district = ''
}

// 提交申请
const submitApply = async () => {
  if (!applyFormRef.value) return
  
  try {
    await applyFormRef.value.validate()
    submitting.value = true
    
    await leaderStore.applyCommunity(applyForm.value)
    
    // 刷新申请记录
    applications.value = await leaderStore.fetchApplications()
    
    submitted.value = true
  } catch (error) {
    if (!error.errors) {
      console.error('提交申请失败', error)
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  applyFormRef.value?.resetFields()
}

// 查看申请状态
const viewStatus = () => {
  submitted.value = false
  scrollToRecords()
}

// 继续申请
const applyAgain = () => {
  submitted.value = false
  resetForm()
  scrollToTop()
}

// 查看拒绝原因
const viewRejectReason = (row) => {
  selectedApplication.value = row
  rejectDialogVisible.value = true
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',  // 待审核
    1: 'success',  // 已通过
    2: 'danger'    // 已拒绝
  }
  return typeMap[status] || ''
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return textMap[status] || '未知'
}

// 滚动到顶部
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 滚动到记录
const scrollToRecords = () => {
  const recordsCard = document.querySelector('.records-card')
  if (recordsCard) {
    recordsCard.scrollIntoView({ behavior: 'smooth' })
  }
}
</script>

<style scoped>
.apply-container {
  max-width: 1200px;
  margin: 80px auto 20px;
  padding: 20px;
}

/* 页面头部 */
.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
}

/* 卡片标题 */
.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

/* 申请表单卡片 */
.apply-form-card {
  margin-bottom: 30px;
}

.apply-form {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px 0;
}

/* 成功卡片 */
.success-card {
  margin-bottom: 30px;
}

/* 记录卡片 */
.records-card {
  margin-top: 30px;
}

/* 拒绝原因对话框 */
.reject-content {
  padding: 10px 0;
}

/* 空状态 */
:deep(.el-empty) {
  padding: 60px 0;
}

/* 表单优化 */
:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-select) {
  width: 100%;
}
</style>
