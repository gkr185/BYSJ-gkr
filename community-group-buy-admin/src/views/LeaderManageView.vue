<template>
  <div class="leader-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="header-title">
            <el-icon><UserFilled /></el-icon>
            团长管理
          </span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索团长姓名、手机号、团点名称..."
              style="width: 300px"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" :icon="Refresh" @click="fetchLeaders">刷新</el-button>
          </div>
        </div>
      </template>
      
      <!-- 状态统计卡片 -->
      <el-row :gutter="16" style="margin-bottom: 20px">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="待审核" :value="statistics.pending">
              <template #prefix>
                <el-icon color="#E6A23C"><Clock /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="正常运营" :value="statistics.active">
              <template #prefix>
                <el-icon color="#67C23A"><CircleCheck /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="已停用" :value="statistics.disabled">
              <template #prefix>
                <el-icon color="#F56C6C"><CircleClose /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <el-statistic title="累计佣金" :value="statistics.totalCommission" :precision="2" prefix="¥">
              <template #prefix>
                <el-icon color="#409EFF"><Money /></el-icon>
              </template>
            </el-statistic>
          </el-card>
        </el-col>
      </el-row>

      <!-- 状态Tabs -->
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane name="pending">
          <template #label>
            <el-badge :value="statistics.pending" :hidden="statistics.pending === 0">
              待审核
            </el-badge>
          </template>
        </el-tab-pane>
        <el-tab-pane label="正常运营" name="active"></el-tab-pane>
        <el-tab-pane label="已停用" name="disabled"></el-tab-pane>
        <el-tab-pane label="全部" name="all"></el-tab-pane>
      </el-tabs>
      
      <!-- 团长列表表格 -->
      <el-table 
        :data="filteredLeaderList" 
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        :empty-text="emptyText"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="storeId" label="团点ID" width="80" sortable="custom" />
        <el-table-column label="团长信息" width="180" fixed="left">
          <template #default="{ row }">
            <div class="leader-info">
              <div class="leader-name">{{ row.leaderName }}</div>
              <el-text type="info" size="small">{{ row.leaderPhone }}</el-text>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="communityName" label="所属社区" width="150">
          <template #default="{ row }">
            <el-link 
              type="primary" 
              @click="viewCommunity(row.communityId)"
              :underline="false"
            >
              <el-icon><Location /></el-icon>
              {{ row.communityName }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="storeName" label="团点名称" width="150" show-overflow-tooltip />
        <el-table-column prop="address" label="团点地址" min-width="200" show-overflow-tooltip />
        <el-table-column label="经纬度" width="180">
          <template #default="{ row }">
            <div v-if="row.latitude && row.longitude" class="coordinates">
              <div>纬度: {{ row.latitude }}</div>
              <div>经度: {{ row.longitude }}</div>
            </div>
            <el-text v-else type="info" size="small">未设置</el-text>
          </template>
        </el-table-column>
        <el-table-column label="佣金比例" width="100" sortable="custom" prop="commissionRate">
          <template #default="{ row }">
            <el-tag type="success" effect="plain">{{ row.commissionRate }}%</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="累计佣金" width="120" sortable="custom" prop="totalCommission">
          <template #default="{ row }">
            <span class="commission-amount">¥{{ (row.totalCommission || 0).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" fixed="right">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="dark">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180" sortable="custom">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <!-- 待审核状态：审核 + 更多 -->
            <template v-if="row.status === 0">
              <el-button 
                size="small" 
                type="primary" 
                :icon="Select"
                @click="showReviewDialog(row)"
              >
                审核
              </el-button>
              <el-dropdown @command="(cmd) => handleCommand(cmd, row)">
                <el-button size="small" :icon="MoreFilled">更多</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="detail" :icon="View">
                      查看详情
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            
            <!-- 正常运营状态：编辑 + 更多 -->
            <template v-else-if="row.status === 1">
              <el-button 
                size="small" 
                type="warning"
                :icon="Edit"
                @click="showEditDialog(row)"
              >
                编辑
              </el-button>
              <el-dropdown @command="(cmd) => handleCommand(cmd, row)">
                <el-button size="small" :icon="MoreFilled">更多</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="detail" :icon="View">
                      查看详情
                    </el-dropdown-item>
                    <el-dropdown-item command="disable" :icon="Close" divided>
                      停用团长
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            
            <!-- 已停用状态：启用 + 更多 -->
            <template v-else-if="row.status === 2">
              <el-button 
                size="small" 
                type="success"
                :icon="Check"
                @click="handleEnable(row)"
              >
                启用
              </el-button>
              <el-dropdown @command="(cmd) => handleCommand(cmd, row)">
                <el-button size="small" :icon="MoreFilled">更多</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="detail" :icon="View">
                      查看详情
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 团长详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="团长详情" 
      width="800px"
      destroy-on-close
      v-loading="detailLoading"
    >
      <!-- 用户信息区块 -->
      <el-card shadow="hover" style="margin-bottom: 20px" v-if="currentUserInfo">
        <template #header>
          <div style="display: flex; align-items: center; gap: 8px">
            <el-icon color="#409EFF"><UserFilled /></el-icon>
            <span style="font-weight: 600">关联用户信息</span>
            <el-tag size="small" :type="getUserRoleType(currentUserInfo.role)" style="margin-left: auto">
              {{ getUserRoleText(currentUserInfo.role) }}
            </el-tag>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">
            {{ currentUserInfo.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ currentUserInfo.username }}
          </el-descriptions-item>
          <el-descriptions-item label="真实姓名">
            {{ currentUserInfo.realName || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ currentUserInfo.phone }}
          </el-descriptions-item>
          <el-descriptions-item label="账户状态">
            <el-tag :type="currentUserInfo.status === 1 ? 'success' : 'danger'" size="small">
              {{ currentUserInfo.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ formatDate(currentUserInfo.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="头像" :span="2" v-if="currentUserInfo.avatar">
            <el-image 
              :src="currentUserInfo.avatar" 
              style="width: 60px; height: 60px; border-radius: 50%"
              fit="cover"
            />
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 团点信息区块 -->
      <el-card shadow="hover" v-if="currentLeader">
        <template #header>
          <div style="display: flex; align-items: center; gap: 8px">
            <el-icon color="#67C23A"><Location /></el-icon>
            <span style="font-weight: 600">团点信息</span>
            <el-tag size="small" :type="getStatusType(currentLeader.status)" style="margin-left: auto">
              {{ getStatusText(currentLeader.status) }}
            </el-tag>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="团点ID" label-class-name="custom-label">
            {{ currentLeader.storeId }}
          </el-descriptions-item>
          <el-descriptions-item label="团点名称" label-class-name="custom-label">
            {{ currentLeader.storeName }}
          </el-descriptions-item>
        <el-descriptions-item label="团点地址" label-class-name="custom-label" :span="2">
          {{ currentLeader.address }}
        </el-descriptions-item>
        <el-descriptions-item label="所属社区" label-class-name="custom-label" :span="2">
          <el-link type="primary" @click="viewCommunity(currentLeader.communityId)">
            {{ currentLeader.communityName }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item label="纬度" label-class-name="custom-label">
          {{ currentLeader.latitude || '未设置' }}
        </el-descriptions-item>
        <el-descriptions-item label="经度" label-class-name="custom-label">
          {{ currentLeader.longitude || '未设置' }}
        </el-descriptions-item>
        <el-descriptions-item label="佣金比例" label-class-name="custom-label">
          <el-tag type="success">{{ currentLeader.commissionRate }}%</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="累计佣金" label-class-name="custom-label">
          <span class="commission-amount">¥{{ (currentLeader.totalCommission || 0).toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="团点简介" label-class-name="custom-label" :span="2">
          {{ currentLeader.description || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间" label-class-name="custom-label">
          {{ formatDate(currentLeader.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间" label-class-name="custom-label">
          {{ formatDate(currentLeader.updatedAt) }}
        </el-descriptions-item>
        <template v-if="currentLeader.status !== 0">
          <el-descriptions-item label="审核人ID" label-class-name="custom-label">
            {{ currentLeader.reviewerId }}
          </el-descriptions-item>
          <el-descriptions-item label="审核时间" label-class-name="custom-label">
            {{ formatDate(currentLeader.reviewedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="审核意见" label-class-name="custom-label" :span="2">
            {{ currentLeader.reviewComment || '暂无' }}
          </el-descriptions-item>
        </template>
      </el-descriptions>
      </el-card>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button 
          v-if="currentUserInfo"
          type="primary" 
          :icon="UserFilled"
          @click="goToUserManage"
        >
          查看用户详情
        </el-button>
        <el-button 
          v-if="currentLeader && currentLeader.status === 1"
          type="warning" 
          :icon="Edit"
          @click="showEditDialog(currentLeader); detailDialogVisible = false"
        >
          编辑团点信息
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 审核对话框 -->
    <el-dialog 
      v-model="reviewDialogVisible" 
      title="审核团长申请" 
      width="600px" 
      @close="resetReviewForm"
      destroy-on-close
    >
      <el-form 
        ref="reviewFormRef" 
        :model="reviewForm" 
        :rules="reviewRules" 
        label-width="100px"
      >
        <el-alert 
          title="审核提示" 
          type="info" 
          :closable="false" 
          style="margin-bottom: 20px"
        >
          <template v-if="currentLeader">
            <p><strong>申请人：</strong>{{ currentLeader.leaderName }}</p>
            <p><strong>所属社区：</strong>{{ currentLeader.communityName }}</p>
            <p style="color: #E6A23C; font-weight: bold; margin-top: 10px">
              ⚠️ 审核通过后将自动更新用户角色为"团长"！
            </p>
          </template>
        </el-alert>
        
        <el-form-item label="审核结果" prop="approved">
          <el-radio-group v-model="reviewForm.approved">
            <el-radio :label="true">
              <el-icon color="#67C23A"><CircleCheck /></el-icon> 通过
            </el-radio>
            <el-radio :label="false">
              <el-icon color="#F56C6C"><CircleClose /></el-icon> 拒绝
            </el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 经纬度输入（审核通过时必填） -->
        <el-divider v-if="reviewForm.approved">经纬度信息（必填）</el-divider>
        
        <el-form-item label="纬度" prop="latitude" v-if="reviewForm.approved">
          <el-input-number
            v-model="reviewForm.latitude"
            :precision="6"
            :step="0.000001"
            :min="-90"
            :max="90"
            placeholder="请输入纬度"
            style="width: 100%"
            :controls="false"
          />
          <div class="form-tip">
            纬度范围：-90 到 90，例如：北京天安门 39.904200
          </div>
        </el-form-item>
        
        <el-form-item label="经度" prop="longitude" v-if="reviewForm.approved">
          <el-input-number
            v-model="reviewForm.longitude"
            :precision="6"
            :step="0.000001"
            :min="-180"
            :max="180"
            placeholder="请输入经度"
            style="width: 100%"
            :controls="false"
          />
          <div class="form-tip">
            经度范围：-180 到 180，例如：北京天安门 116.407400
          </div>
          <div class="form-tip-link">
            💡 提示：可以通过
            <el-link 
              type="primary" 
              href="https://lbs.amap.com/tools/picker" 
              target="_blank"
            >
              高德地图坐标拾取器
            </el-link>
            获取精确坐标
          </div>
        </el-form-item>
        
        <el-divider />
        
        <el-form-item label="审核意见" prop="reviewComment">
          <el-input 
            v-model="reviewForm.reviewComment" 
            type="textarea"
            :rows="4"
            :placeholder="reviewForm.approved ? '审核通过！' : '请填写拒绝原因'"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleReviewSubmit" 
          :loading="submitting"
          :icon="Select"
        >
          提交审核
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑团点信息对话框 -->
    <el-dialog 
      v-model="editDialogVisible" 
      title="编辑团点信息" 
      width="600px" 
      @close="resetEditForm"
      destroy-on-close
    >
      <el-alert 
        title="权限说明" 
        type="warning" 
        :closable="false" 
        style="margin-bottom: 20px"
      >
        <template #default>
          <p style="margin: 0 0 8px 0">
            <strong>团长姓名和手机号</strong>为用户个人信息，属于 UserService 管理范畴。
          </p>
          <p style="margin: 0">
            如需修改，请点击右侧按钮跳转到【用户管理】页面 
            <el-button 
              type="primary" 
              size="small" 
              :icon="UserFilled"
              @click="goToUserManage"
              style="margin-left: 8px"
            >
              前往用户管理
            </el-button>
          </p>
        </template>
      </el-alert>
      
      <el-form 
        ref="editFormRef" 
        :model="editForm" 
        :rules="editRules" 
        label-width="100px"
      >
        <!-- 只读展示团长信息 -->
        <el-form-item label="团长ID">
          <el-input v-model="editForm.leaderId" disabled>
            <template #append>
              <el-button 
                :icon="CopyDocument" 
                @click="copyLeaderId"
              />
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="团长姓名">
          <el-input v-model="editForm.leaderName" disabled>
            <template #append>
              <span style="color: #909399; font-size: 12px">用户信息</span>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="手机号">
          <el-input v-model="editForm.leaderPhone" disabled>
            <template #append>
              <span style="color: #909399; font-size: 12px">用户信息</span>
            </template>
          </el-input>
        </el-form-item>
        
        <el-divider content-position="left">团点信息（可编辑）</el-divider>
        
        <el-form-item label="所属社区" prop="communityId">
          <el-select 
            v-model="editForm.communityId" 
            placeholder="请选择所属社区"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="community in communityList"
              :key="community.communityId"
              :label="community.name"
              :value="community.communityId"
            >
              <span>{{ community.name }}</span>
              <span style="color: #8492a6; font-size: 12px; margin-left: 8px">
                (ID: {{ community.communityId }})
              </span>
            </el-option>
          </el-select>
          <div class="form-tip">
            可以更换团长所属的社区
          </div>
        </el-form-item>
        
        <el-form-item label="团点名称" prop="storeName">
          <el-input v-model="editForm.storeName" placeholder="请输入团点名称" />
        </el-form-item>
        
        <el-form-item label="团点地址" prop="address">
          <el-input 
            v-model="editForm.address" 
            type="textarea"
            :rows="2"
            placeholder="请输入团点地址"
          />
        </el-form-item>
        
        <el-form-item label="纬度" prop="latitude">
          <el-input-number
            v-model="editForm.latitude"
            :precision="6"
            :step="0.000001"
            :min="-90"
            :max="90"
            placeholder="请输入纬度"
            style="width: 100%"
            :controls="false"
          />
        </el-form-item>
        
        <el-form-item label="经度" prop="longitude">
          <el-input-number
            v-model="editForm.longitude"
            :precision="6"
            :step="0.000001"
            :min="-180"
            :max="180"
            placeholder="请输入经度"
            style="width: 100%"
            :controls="false"
          />
          <div class="form-tip-link">
            💡 通过
            <el-link 
              type="primary" 
              href="https://lbs.amap.com/tools/picker" 
              target="_blank"
            >
              高德地图坐标拾取器
            </el-link>
            获取精确坐标
          </div>
        </el-form-item>
        
        <el-form-item label="佣金比例" prop="commissionRate">
          <el-input-number
            v-model="editForm.commissionRate"
            :precision="2"
            :step="0.5"
            :min="0"
            :max="100"
            placeholder="请输入佣金比例"
            style="width: 100%"
          />
          <div class="form-tip">
            佣金比例范围：0% - 100%，建议：5% - 15%
          </div>
        </el-form-item>
        
        <el-form-item label="团点简介" prop="description">
          <el-input 
            v-model="editForm.description" 
            type="textarea"
            :rows="3"
            placeholder="请输入团点简介"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleEditSubmit" 
          :loading="submitting"
          :icon="Check"
        >
          保存修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import {
  UserFilled,
  Search,
  Refresh,
  Clock,
  CircleCheck,
  CircleClose,
  Money,
  Location,
  View,
  Select,
  Edit,
  Check,
  Close,
  MoreFilled,
  CopyDocument
} from '@element-plus/icons-vue'
import {
  getPendingLeaderApplications,
  getLeadersByStatus,
  reviewLeaderApplication,
  updateLeaderStore,
  updateLeaderStoreCoordinates,
  enableLeader,
  disableLeader,
  getCommunityList
} from '../api/leader'
import { getUserInfo } from '../api/user'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

// ==================== 数据定义 ====================
const leaderList = ref([])
const communityList = ref([])
const loading = ref(false)
const detailLoading = ref(false)
const activeTab = ref('pending')
const searchKeyword = ref('')

// 对话框控制
const detailDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentLeader = ref(null)
const currentUserInfo = ref(null)
const submitting = ref(false)

// 表单引用
const reviewFormRef = ref(null)
const editFormRef = ref(null)

// 请求ID（防止竞态条件）
const currentRequestId = ref(0)

// 排序
const sortField = ref('')
const sortOrder = ref('')

// ==================== 统计数据 ====================
const statistics = computed(() => {
  const all = leaderList.value
  return {
    pending: all.filter(l => l.status === 0).length,
    active: all.filter(l => l.status === 1).length,
    disabled: all.filter(l => l.status === 2).length,
    totalCommission: all.reduce((sum, l) => sum + (l.totalCommission || 0), 0)
  }
})

// ==================== 审核表单 ====================
const reviewForm = ref({
  approved: true,
  reviewComment: '',
  latitude: null,
  longitude: null
})

const reviewRules = {
  approved: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
  reviewComment: [
    {
      validator: (rule, value, callback) => {
        if (reviewForm.value.approved === false && !value) {
          callback(new Error('拒绝时必须填写原因'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  latitude: [
    {
      validator: (rule, value, callback) => {
        // 审核通过时必须填写经纬度
        if (reviewForm.value.approved === true) {
          if (value === null || value === undefined || value === '') {
            callback(new Error('审核通过时必须填写纬度'))
            return
          }
          // 验证纬度范围
          const numValue = Number(value)
          if (isNaN(numValue) || numValue < -90 || numValue > 90) {
            callback(new Error('纬度必须在 -90 到 90 之间'))
            return
          }
        }
        callback()
      },
      trigger: ['blur', 'change']
    }
  ],
  longitude: [
    {
      validator: (rule, value, callback) => {
        // 审核通过时必须填写经纬度
        if (reviewForm.value.approved === true) {
          if (value === null || value === undefined || value === '') {
            callback(new Error('审核通过时必须填写经度'))
            return
          }
          // 验证经度范围
          const numValue = Number(value)
          if (isNaN(numValue) || numValue < -180 || numValue > 180) {
            callback(new Error('经度必须在 -180 到 180 之间'))
            return
          }
        }
        callback()
      },
      trigger: ['blur', 'change']
    }
  ]
}

// ==================== 编辑表单 ====================
const editForm = ref({
  leaderId: null,
  leaderName: '',
  leaderPhone: '',
  communityId: null,
  communityName: '',
  storeName: '',
  address: '',
  latitude: '',
  longitude: '',
  commissionRate: 10,
  description: ''
})

const editRules = {
  // leaderName 和 leaderPhone 为只读字段，不需要验证
  communityId: [{ required: true, message: '请选择所属社区', trigger: 'change' }],
  storeName: [{ required: true, message: '请输入团点名称', trigger: 'blur' }],
  address: [{ required: true, message: '请输入团点地址', trigger: 'blur' }],
  commissionRate: [
    { required: true, message: '请输入佣金比例', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '佣金比例必须在0-100之间', trigger: 'blur' }
  ]
}

// ==================== 计算属性 ====================
const filteredLeaderList = computed(() => {
  let list = leaderList.value

  // 根据activeTab筛选
  if (activeTab.value !== 'all') {
    const statusMap = { pending: 0, active: 1, disabled: 2 }
    list = list.filter(l => l.status === statusMap[activeTab.value])
  }

  // 根据搜索关键词筛选
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    list = list.filter(
      l =>
        l.leaderName?.toLowerCase().includes(keyword) ||
        l.leaderPhone?.includes(keyword) ||
        l.storeName?.toLowerCase().includes(keyword) ||
        l.communityName?.toLowerCase().includes(keyword)
    )
  }

  // 排序
  if (sortField.value && sortOrder.value) {
    list = [...list].sort((a, b) => {
      const aVal = a[sortField.value] || 0
      const bVal = b[sortField.value] || 0
      return sortOrder.value === 'ascending' ? aVal - bVal : bVal - aVal
    })
  }

  return list
})

const emptyText = computed(() => {
  if (loading.value) return '加载中...'
  if (searchKeyword.value) return '暂无符合条件的团长'
  if (activeTab.value === 'pending') return '暂无待审核申请'
  if (activeTab.value === 'active') return '暂无正常运营的团长'
  if (activeTab.value === 'disabled') return '暂无已停用的团长'
  return '暂无数据'
})

// ==================== 方法定义 ====================

// 加载社区列表
const fetchCommunities = async () => {
  try {
    const res = await getCommunityList()
    if (res.code === 200) {
      communityList.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取社区列表失败')
    }
  } catch (error) {
    console.error('获取社区列表失败:', error)
    ElMessage.error('获取社区列表失败')
  }
}

// 加载团长列表
const fetchLeaders = async () => {
  loading.value = true
  const requestId = ++currentRequestId.value
  const currentTab = activeTab.value

  try {
    let allLeaders = []

    if (currentTab === 'all') {
      // 加载全部：并发请求3个状态
      const [pendingRes, activeRes, disabledRes] = await Promise.all([
        getPendingLeaderApplications(),
        getLeadersByStatus(1),
        getLeadersByStatus(2)
      ])
      if (pendingRes.code === 200) allLeaders.push(...(pendingRes.data || []))
      if (activeRes.code === 200) allLeaders.push(...(activeRes.data || []))
      if (disabledRes.code === 200) allLeaders.push(...(disabledRes.data || []))
    } else {
      // 加载单个状态
      let res
      if (currentTab === 'pending') {
        res = await getPendingLeaderApplications()
      } else {
        const status = currentTab === 'active' ? 1 : 2
        res = await getLeadersByStatus(status)
      }

      if (res.code === 200) {
        allLeaders = res.data || []
      } else {
        ElMessage.error(res.message || '获取团长列表失败')
      }
    }

    // 只有当前请求是最新的才更新数据
    if (requestId === currentRequestId.value && currentTab === activeTab.value) {
      leaderList.value = allLeaders
    }
  } catch (error) {
    if (requestId === currentRequestId.value) {
      console.error('获取团长列表失败:', error)
      ElMessage.error('获取团长列表失败')
    }
  } finally {
    if (requestId === currentRequestId.value) {
      loading.value = false
    }
  }
}

// Tab切换
const handleTabChange = (tabName) => {
  console.log('标签切换到:', tabName)
  fetchLeaders()
}

// 搜索
const handleSearch = () => {
  // 实时搜索由computed自动处理
}

// 排序
const handleSortChange = ({ prop, order }) => {
  sortField.value = prop
  sortOrder.value = order
}

// ==================== 对话框操作 ====================

// 显示详情
const showDetailDialog = async (row) => {
  currentLeader.value = row
  currentUserInfo.value = null
  detailDialogVisible.value = true
  detailLoading.value = true
  
  try {
    // 获取关联的用户信息
    const res = await getUserInfo(row.leaderId)
    if (res.code === 200) {
      currentUserInfo.value = res.data
    } else {
      ElMessage.warning('获取用户信息失败：' + (res.message || '未知错误'))
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    detailLoading.value = false
  }
}

// 显示审核对话框
const showReviewDialog = (row) => {
  currentLeader.value = row
  reviewForm.value = {
    approved: true,
    reviewComment: '',
    latitude: row.latitude ? Number(row.latitude) : null,
    longitude: row.longitude ? Number(row.longitude) : null
  }
  reviewDialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  currentLeader.value = row
  editForm.value = {
    leaderId: row.leaderId,
    leaderName: row.leaderName || '',
    leaderPhone: row.leaderPhone || '',
    communityId: row.communityId || null,
    communityName: row.communityName || '',
    storeName: row.storeName || '',
    address: row.address || '',
    latitude: row.latitude || '',
    longitude: row.longitude || '',
    commissionRate: row.commissionRate || 10,
    description: row.description || ''
  }
  editDialogVisible.value = true
}

// ==================== 审核提交 ====================
const handleReviewSubmit = async () => {
  if (!reviewFormRef.value) return
  await reviewFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true

    try {
      const adminUserId = userStore.userInfo?.userId || 1

      // 如果审核通过，先补充经纬度信息
      if (reviewForm.value.approved) {
        const lat = reviewForm.value.latitude
        const lng = reviewForm.value.longitude
        
        // 确保经纬度有效
        if (lat === null || lat === undefined || lng === null || lng === undefined) {
          ElMessage.error('审核通过时必须填写经纬度信息')
          submitting.value = false
          return
        }

        const coordRes = await updateLeaderStoreCoordinates(
          currentLeader.value.storeId,
          Number(lat),
          Number(lng)
        )
        if (coordRes.code !== 200) {
          ElMessage.error(coordRes.message || '补充经纬度信息失败')
          submitting.value = false
          return
        }
      }

      // 提交审核
      const res = await reviewLeaderApplication(
        currentLeader.value.storeId,
        adminUserId,
        reviewForm.value.approved,
        reviewForm.value.reviewComment
      )

      if (res.code === 200) {
        ElMessage.success({
          message: reviewForm.value.approved
            ? '审核通过！用户角色已更新为团长'
            : '已拒绝申请',
          duration: 3000
        })
        reviewDialogVisible.value = false
        fetchLeaders()
      } else {
        ElMessage.error(res.message || '审核失败')
      }
    } catch (error) {
      console.error('审核失败:', error)
      ElMessage.error('审核失败')
    } finally {
      submitting.value = false
    }
  })
}

// ==================== 编辑提交 ====================
const handleEditSubmit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true

    try {
      const res = await updateLeaderStore(currentLeader.value.storeId, editForm.value)

      if (res.code === 200) {
        ElMessage.success('团长信息更新成功')
        editDialogVisible.value = false
        fetchLeaders()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } catch (error) {
      console.error('更新失败:', error)
      ElMessage.error('更新失败')
    } finally {
      submitting.value = false
    }
  })
}

// ==================== 启用/停用 ====================
const handleEnable = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要启用团长"${row.leaderName}"吗？`,
      '确认启用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    const res = await enableLeader(row.storeId)
    if (res.code === 200) {
      ElMessage.success('团长已启用')
      fetchLeaders()
    } else {
      ElMessage.error(res.message || '启用失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('启用失败:', error)
      ElMessage.error('启用失败')
    }
  }
}

const handleDisable = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要停用团长"${row.leaderName}"吗？停用后将无法发起拼团。`,
      '确认停用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const res = await disableLeader(row.storeId)
    if (res.code === 200) {
      ElMessage.success('团长已停用')
      fetchLeaders()
    } else {
      ElMessage.error(res.message || '停用失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('停用失败:', error)
      ElMessage.error('停用失败')
    }
  }
}

// 处理下拉菜单命令
const handleCommand = (command, row) => {
  switch (command) {
    case 'detail':
      showDetailDialog(row)
      break
    case 'disable':
      handleDisable(row)
      break
    default:
      break
  }
}

// ==================== 其他操作 ====================
const resetReviewForm = () => {
  if (reviewFormRef.value) reviewFormRef.value.resetFields()
  reviewForm.value = {
    approved: true,
    reviewComment: '',
    latitude: null,
    longitude: null
  }
}

const resetEditForm = () => {
  if (editFormRef.value) editFormRef.value.resetFields()
  editForm.value = {
    leaderId: null,
    leaderName: '',
    leaderPhone: '',
    communityId: null,
    communityName: '',
    storeName: '',
    address: '',
    latitude: '',
    longitude: '',
    commissionRate: 10,
    description: ''
  }
}

const viewCommunity = (communityId) => {
  router.push({ name: 'community', query: { id: communityId } })
  detailDialogVisible.value = false
}

// ==================== 工具函数 ====================
const getStatusType = (status) =>
  ({ 0: 'warning', 1: 'success', 2: 'danger' }[status] || 'info')

const getStatusText = (status) =>
  ({ 0: '待审核', 1: '正常运营', 2: '已停用' }[status] || '未知')

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const getUserRoleType = (role) => {
  const roleTypeMap = { 1: '', 2: 'success', 3: 'danger' }
  return roleTypeMap[role] || 'info'
}

const getUserRoleText = (role) => {
  const roleTextMap = { 1: '普通用户', 2: '团长', 3: '管理员' }
  return roleTextMap[role] || '未知角色'
}

const goToUserManage = () => {
  if (currentUserInfo.value) {
    router.push({
      name: 'user',
      query: { userId: currentUserInfo.value.userId }
    })
    detailDialogVisible.value = false
  }
}

const copyLeaderId = () => {
  if (editForm.value.leaderId) {
    navigator.clipboard.writeText(editForm.value.leaderId.toString())
    ElMessage.success('团长ID已复制')
  }
}

// ==================== 生命周期 ====================
onMounted(() => {
  fetchLeaders()
  fetchCommunities()
})
</script>

<style scoped>
.leader-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.card-header .header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 统计卡片 */
.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
}

.stat-card :deep(.el-statistic__head) {
  font-size: 14px;
  color: #909399;
}

.stat-card :deep(.el-statistic__content) {
  font-size: 24px;
  font-weight: 600;
}

/* 表格样式 */
.leader-info .leader-name {
  font-weight: 600;
  margin-bottom: 4px;
}

.coordinates {
  font-size: 12px;
  line-height: 1.6;
}

.commission-amount {
  color: #67C23A;
  font-weight: 600;
  font-size: 14px;
}

/* 对话框样式 */
:deep(.el-descriptions) .custom-label {
  font-weight: 600;
  color: #606266;
  background: #f5f7fa;
}

/* 表单提示 */
.form-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
  line-height: 1.5;
}

.form-tip-link {
  color: #E6A23C;
  font-size: 12px;
  margin-top: 5px;
  line-height: 1.5;
}

/* 响应式 */
@media (max-width: 768px) {
  .leader-manage {
    padding: 12px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .card-header .header-actions {
    width: 100%;
    flex-direction: column;
  }

  .card-header .header-actions :deep(.el-input) {
    width: 100% !important;
  }

  :deep(.el-table) {
    font-size: 12px;
  }

  :deep(.el-table) .el-button {
    padding: 5px 10px;
    font-size: 12px;
  }
}
</style>
