<template>
  <div class="community-apply-wrapper">
    <div class="community-apply-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>申请新社区</h2>
        <p class="subtitle">帮助更多社区加入团购服务</p>
      </div>

      <!-- 已申请提示 -->
      <el-alert
        v-if="hasApplied"
        title="您已提交社区申请"
        type="info"
        description="您的社区申请正在审核中，请耐心等待。审核通过后，该社区将出现在团长申请的社区列表中。"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />

      <!-- 申请表单 -->
      <el-card v-if="!hasApplied" class="form-card">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          @submit.prevent="handleSubmit"
        >
          <!-- 社区信息 -->
          <el-divider content-position="left">
            <el-icon><OfficeBuilding /></el-icon>
            社区信息
          </el-divider>

          <el-form-item label="社区名称" prop="communityName">
            <el-input
              v-model="form.communityName"
              placeholder="请输入社区名称，例如：阳光花园小区"
              maxlength="50"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="所在地区" required>
            <el-row :gutter="10">
              <el-col :span="8">
                <el-form-item prop="province">
                  <el-input
                    v-model="form.province"
                    placeholder="省份"
                    maxlength="20"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item prop="city">
                  <el-input
                    v-model="form.city"
                    placeholder="城市"
                    maxlength="20"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item prop="district">
                  <el-input
                    v-model="form.district"
                    placeholder="区/县"
                    maxlength="20"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <div style="color: #909399; font-size: 12px;">
              例如：北京市 / 北京市 / 朝阳区
            </div>
          </el-form-item>

          <el-form-item label="详细地址" prop="address">
            <el-input
              v-model="form.address"
              type="textarea"
              :rows="2"
              placeholder="请输入社区详细地址，例如：建国路88号"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="经纬度坐标" required>
            <el-row :gutter="10">
              <el-col :span="12">
                <el-form-item prop="latitude">
                  <el-input-number
                    v-model="form.latitude"
                    placeholder="纬度"
                    :precision="6"
                    :step="0.000001"
                    :min="-90"
                    :max="90"
                    style="width: 100%;"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item prop="longitude">
                  <el-input-number
                    v-model="form.longitude"
                    placeholder="经度"
                    :precision="6"
                    :step="0.000001"
                    :min="-180"
                    :max="180"
                    style="width: 100%;"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <div style="color: #909399; font-size: 12px;">
              例如：北京天安门 纬度 39.904200，经度 116.407400
            </div>
            <div style="color: #E6A23C; font-size: 12px; margin-top: 5px;">
              💡 提示：可以通过
              <el-link type="primary" href="https://lbs.amap.com/tools/picker" target="_blank">
                高德地图坐标拾取器
              </el-link>
              获取精确坐标
            </div>
          </el-form-item>

          <el-form-item label="服务半径" prop="serviceRadius">
            <el-input-number
              v-model="form.serviceRadius"
              :min="500"
              :max="10000"
              :step="100"
              style="width: 200px;"
            />
            <span style="margin-left: 10px; color: #606266;">米</span>
            <div style="color: #909399; font-size: 12px; margin-top: 5px;">
              建议服务半径为 1000-3000 米
            </div>
          </el-form-item>

          <el-form-item label="申请说明" prop="applicationReason">
            <el-input
              v-model="form.applicationReason"
              type="textarea"
              :rows="4"
              placeholder="说明您为什么要申请这个社区，以及您的优势"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <!-- 提交按钮 -->
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="submitting"
              @click="handleSubmit"
              style="width: 200px;"
            >
              提交申请
            </el-button>
            <el-button
              size="large"
              @click="router.back()"
              style="width: 120px; margin-left: 20px;"
            >
              返回
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 申请须知 -->
      <el-card class="notice-card">
        <template #header>
          <div class="card-header">
            <el-icon><InfoFilled /></el-icon>
            <span>申请须知</span>
          </div>
        </template>
        <ul class="notice-list">
          <li>📋 请如实填写社区信息，管理员会进行核实</li>
          <li>📍 经纬度坐标必须准确，用于团长服务范围计算</li>
          <li>⏰ 申请提交后，请耐心等待管理员审核（通常1-3个工作日）</li>
          <li>✅ 审核通过后，该社区会出现在团长申请的社区列表中</li>
          <li>👥 您可以在社区审核通过后，再申请成为该社区的团长</li>
          <li>📞 如有疑问，请联系客服：400-XXX-XXXX</li>
        </ul>
      </el-card>

      <!-- 后续步骤 -->
      <el-card class="steps-card" v-if="hasApplied">
        <template #header>
          <div class="card-header">
            <el-icon><Checked /></el-icon>
            <span>后续步骤</span>
          </div>
        </template>
        <el-steps :active="1" align-center>
          <el-step title="提交社区申请" icon="Check" status="finish" />
          <el-step title="等待管理员审核" icon="Clock" status="process" />
          <el-step title="审核通过" icon="Check" />
          <el-step title="申请成为团长" icon="User" />
        </el-steps>
        <div style="text-align: center; margin-top: 20px;">
          <el-button type="primary" @click="router.push('/profile')">
            返回个人中心
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  OfficeBuilding,
  InfoFilled,
  Checked
} from '@element-plus/icons-vue'
import {
  submitCommunityApplication,
  getMyCommunityApplications
} from '@/api/leader'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const submitting = ref(false)
const hasApplied = ref(false)

// 表单数据
const form = reactive({
  communityName: '',
  province: '',
  city: '',
  district: '',
  address: '',
  latitude: null,
  longitude: null,
  serviceRadius: 3000,
  description: '',
  applicationReason: ''
})

// 表单验证规则
const rules = {
  communityName: [
    { required: true, message: '请输入社区名称', trigger: 'blur' },
    { min: 2, max: 50, message: '社区名称长度为 2-50 个字符', trigger: 'blur' }
  ],
  province: [
    { required: true, message: '请输入省份', trigger: 'blur' }
  ],
  city: [
    { required: true, message: '请输入城市', trigger: 'blur' }
  ],
  district: [
    { required: true, message: '请输入区/县', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, max: 200, message: '地址长度为 5-200 个字符', trigger: 'blur' }
  ],
  latitude: [
    { required: true, message: '请输入纬度', trigger: 'blur' },
    { type: 'number', min: -90, max: 90, message: '纬度范围为 -90 到 90', trigger: 'blur' }
  ],
  longitude: [
    { required: true, message: '请输入经度', trigger: 'blur' },
    { type: 'number', min: -180, max: 180, message: '经度范围为 -180 到 180', trigger: 'blur' }
  ],
  applicationReason: [
    { required: true, message: '请填写申请说明', trigger: 'blur' }
  ]
}

// 检查是否已申请
const checkApplicationStatus = async () => {
  if (!userStore.userInfo?.userId) return

  try {
    const applications = await getMyCommunityApplications(userStore.userInfo.userId)
    if (applications && applications.length > 0) {
      // 检查是否有待审核的申请
      const pendingApp = applications.find(app => app.status === 0)
      if (pendingApp) {
        hasApplied.value = true
      }
    }
  } catch (error) {
    console.error('检查申请状态失败:', error)
  }
}

// 提交申请
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    // 验证表单
    await formRef.value.validate()

    // 二次确认
    await ElMessageBox.confirm(
      '确定要提交社区申请吗？提交后管理员会进行审核。',
      '确认提交',
      {
        confirmButtonText: '确定提交',
        cancelButtonText: '再检查一下',
        type: 'info'
      }
    )

    submitting.value = true

    // 提交社区申请
    const communityAppData = {
      applicantId: userStore.userInfo.userId,
      applicantName: userStore.userInfo.realName || userStore.userInfo.username,
      applicantPhone: userStore.userInfo.phone,
      communityName: form.communityName,
      province: form.province,
      city: form.city,
      district: form.district,
      address: form.address,
      latitude: form.latitude,
      longitude: form.longitude,
      serviceRadius: form.serviceRadius,
      description: form.description || '',
      applicationReason: form.applicationReason
    }

    await submitCommunityApplication(communityAppData)

    ElMessage.success('社区申请提交成功！请等待管理员审核')

    // 标记为已申请
    hasApplied.value = true
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交申请失败:', error)
      ElMessage.error(error.message || '提交申请失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

// 页面加载时检查申请状态
onMounted(async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  await checkApplicationStatus()
})
</script>

<style scoped>
.community-apply-wrapper {
  min-height: 100vh;
  padding-top: 84px;
  background-color: #f5f5f5;
}

.community-apply-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 14px;
  color: #909399;
}

.form-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  font-size: 16px;
}

.notice-card {
  background-color: #fffbf0;
  margin-bottom: 20px;
}

.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-list li {
  padding: 8px 0;
  line-height: 1.6;
  color: #606266;
}

.steps-card {
  margin-bottom: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .community-apply-wrapper {
    padding-top: 76px;
  }

  .community-apply-container {
    padding: 10px;
  }

  .page-header h2 {
    font-size: 24px;
  }

  :deep(.el-form-item__label) {
    width: 100px !important;
  }
}
</style>

