<template>
  <div class="leader-apply-wrapper">
    <div class="leader-apply-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>申请成为团长</h2>
        <p class="subtitle">加入我们，成为社区团购的服务点负责人</p>
      </div>

      <!-- 已申请或已是团长的状态提示 -->
      <el-alert
        v-if="leaderInfo && leaderInfo.status === 0"
        title="您已提交团长申请"
        type="info"
        description="您的申请正在审核中，请耐心等待管理员审核。"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />

      <el-alert
        v-else-if="leaderInfo && leaderInfo.status === 1"
        title="您已经是团长了"
        type="success"
        description="您的团长申请已通过审核，现在可以发起拼团活动了！"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />

      <el-alert
        v-else-if="leaderInfo && leaderInfo.status === 2"
        title="您的团长申请已被拒绝"
        type="error"
        :description="`拒绝原因：${leaderInfo.reviewComment || '未提供'}`"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      >
        <template #default>
          <p>{{ `拒绝原因：${leaderInfo.reviewComment || '未提供'}` }}</p>
          <el-button type="primary" size="small" @click="resetAndReapply" style="margin-top: 10px;">
            重新申请
          </el-button>
        </template>
      </el-alert>

      <!-- 申请表单 -->
      <el-card v-if="!leaderInfo || leaderInfo.status === 2" class="form-card">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          @submit.prevent="handleSubmit"
        >
          <!-- 团点信息 -->
          <el-divider content-position="left">
            <el-icon><Shop /></el-icon>
            团点信息
          </el-divider>

          <el-form-item label="团点名称" prop="storeName">
            <el-input
              v-model="form.storeName"
              placeholder="请输入您的团购服务点名称，例如：张三团购点"
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
              :rows="3"
              placeholder="请输入团点详细地址，例如：阳光小区1号楼101室"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="团点简介" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="简单介绍您的团点，例如：服务周边社区，提供优质团购服务"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <!-- 社区选择 -->
          <el-divider content-position="left">
            <el-icon><Location /></el-icon>
            服务社区
          </el-divider>

          <el-form-item label="选择社区" prop="communityId">
            <el-select
              v-model="form.communityId"
              placeholder="请选择您要服务的社区"
              filterable
              style="width: 100%;"
              :loading="loadingCommunities"
            >
              <el-option
                v-for="community in communities"
                :key="community.communityId"
                :label="community.name"
                :value="community.communityId"
              >
                <span>{{ community.name }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px;">
                  {{ community.address }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>

          <!-- 社区申请提示 -->
          <el-alert
            type="info"
            :closable="false"
            style="margin-bottom: 20px;"
          >
            <template #title>
              <div style="display: flex; align-items: center; gap: 8px;">
                <el-icon><InfoFilled /></el-icon>
                <span>找不到您的社区？</span>
              </div>
            </template>
            <div style="margin-top: 8px;">
              如果列表中没有您所在的社区，请先
              <el-link type="primary" @click="router.push('/community/apply')">
                申请新社区
              </el-link>
              ，等待审核通过后再申请成为团长。
            </div>
          </el-alert>

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
              取消
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 申请须知 -->
      <el-card class="notice-card" v-if="!leaderInfo || leaderInfo.status === 2">
        <template #header>
          <div class="card-header">
            <el-icon><InfoFilled /></el-icon>
            <span>申请须知</span>
          </div>
        </template>
        <ul class="notice-list">
          <li>📋 请如实填写团点信息，管理员会进行核实</li>
          <li>🏘️ 申请团长前，必须先选择一个已存在的社区</li>
          <li>🆕 如果列表中没有您的社区，请先申请新社区并等待审核通过</li>
          <li>⚡ 审核通过后，您将获得发起拼团的权限</li>
          <li>💰 每笔成功的团购订单，您将获得相应的佣金</li>
          <li>📦 您需要负责本社区订单的配送或协调配送</li>
          <li>⏰ 申请提交后，请耐心等待管理员审核（通常1-3个工作日）</li>
          <li>📞 如有疑问，请联系客服：400-XXX-XXXX</li>
        </ul>
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
  Shop,
  Location,
  InfoFilled
} from '@element-plus/icons-vue'
import {
  submitLeaderApplication,
  getMyLeaderInfo,
  getCommunityList
} from '@/api/leader'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const submitting = ref(false)
const loadingCommunities = ref(false)
const leaderInfo = ref(null)
const communities = ref([])
// 表单数据
const form = reactive({
  storeName: '',
  province: '',
  city: '',
  district: '',
  address: '',
  description: '',
  communityId: null
})

// 表单验证规则
const rules = {
  storeName: [
    { required: true, message: '请输入团点名称', trigger: 'blur' },
    { min: 2, max: 50, message: '团点名称长度为 2-50 个字符', trigger: 'blur' }
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
  communityId: [
    { required: true, message: '请选择服务社区', trigger: 'change' }
  ]
}

// 获取社区列表
const fetchCommunities = async () => {
  loadingCommunities.value = true
  try {
    const data = await getCommunityList()
    communities.value = data || []
  } catch (error) {
    console.error('获取社区列表失败:', error)
    ElMessage.error('获取社区列表失败')
  } finally {
    loadingCommunities.value = false
  }
}

// 获取我的团长信息
const fetchMyLeaderInfo = async () => {
  if (!userStore.userInfo?.userId) return

  try {
    const data = await getMyLeaderInfo(userStore.userInfo.userId)
    leaderInfo.value = data
  } catch (error) {
    // 如果返回404或提示"不是团长"，说明用户还不是团长，可以申请
    if (error.response?.status === 404 || error.message?.includes('不是团长')) {
      leaderInfo.value = null
    } else {
      console.error('获取团长信息失败:', error)
    }
  }
}

// 重新申请
const resetAndReapply = () => {
  leaderInfo.value = null
  // 重置表单
  formRef.value?.resetFields()
}

// 提交申请
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    // 验证表单
    await formRef.value.validate()

    // 二次确认
    await ElMessageBox.confirm(
      '确定要提交团长申请吗？提交后管理员会进行审核。',
      '确认提交',
      {
        confirmButtonText: '确定提交',
        cancelButtonText: '再检查一下',
        type: 'info'
      }
    )

    submitting.value = true

    // 提交团长申请
    const leaderAppData = {
      leaderId: userStore.userInfo.userId,
      leaderName: userStore.userInfo.realName || userStore.userInfo.username,
      leaderPhone: userStore.userInfo.phone,
      communityId: form.communityId,
      storeName: form.storeName,
      province: form.province,
      city: form.city,
      district: form.district,
      address: form.address,
      description: form.description
    }

    await submitLeaderApplication(leaderAppData)

    ElMessage.success('申请提交成功！请等待管理员审核')

    // 刷新团长信息
    await fetchMyLeaderInfo()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交申请失败:', error)
      ElMessage.error(error.message || '提交申请失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

// 页面加载时获取数据
onMounted(async () => {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  await fetchMyLeaderInfo()
  await fetchCommunities()
})
</script>

<style scoped>
.leader-apply-wrapper {
  min-height: 100vh;
  padding-top: 84px;
  background-color: #f5f5f5;
}

.leader-apply-container {
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

/* 响应式设计 */
@media (max-width: 768px) {
  .leader-apply-wrapper {
    padding-top: 76px;
  }

  .leader-apply-container {
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

