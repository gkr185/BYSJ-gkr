<template>
  <div class="log-manage">
    <h2 class="page-title">系统日志管理</h2>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="操作人">
          <el-input
            v-model="queryForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 180px"
          />
        </el-form-item>

        <el-form-item label="操作模块">
          <el-select
            v-model="queryForm.module"
            placeholder="请选择模块"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="module in modules"
              :key="module"
              :label="module"
              :value="module"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>

        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="操作内容关键词"
            clearable
            style="width: 180px"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleQuery" :icon="Search">查询</el-button>
          <el-button @click="handleReset" :icon="Refresh">重置</el-button>
          <el-button type="success" @click="handleExport" :icon="Download">导出Excel</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="logList"
        stripe
        border
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="createTime" label="操作时间" width="180" sortable />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="operation" label="操作内容" width="150" />
        <el-table-column prop="module" label="操作模块" width="120" />
        <el-table-column prop="result" label="操作结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.result === 'SUCCESS' ? 'success' : 'danger'">
              {{ row.result === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="duration" label="耗时(ms)" width="100" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页器 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryForm.page"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handleQuery"
          @size-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="操作日志详情"
      width="700px"
      @closed="handleDialogClosed"
    >
      <el-descriptions v-if="currentLog" :column="2" border>
        <el-descriptions-item label="操作时间">
          {{ currentLog.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="操作人">
          {{ currentLog.username }}
        </el-descriptions-item>
        <el-descriptions-item label="操作内容">
          {{ currentLog.operation }}
        </el-descriptions-item>
        <el-descriptions-item label="操作模块">
          {{ currentLog.module }}
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">
          {{ currentLog.ip }}
        </el-descriptions-item>
        <el-descriptions-item label="执行时长">
          {{ currentLog.duration }}ms
        </el-descriptions-item>
        <el-descriptions-item label="操作结果" :span="2">
          <el-tag :type="currentLog.result === 'SUCCESS' ? 'success' : 'danger'">
            {{ currentLog.result === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="方法名" :span="2">
          <el-text size="small" type="info">{{ currentLog.method }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="json-pre">{{ formatJson(currentLog.params) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentLog.errorMsg" label="错误信息" :span="2">
          <el-text type="danger">{{ currentLog.errorMsg }}</el-text>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { getOperationLogs, exportOperationLogs, getLogModules } from '@/api/log'

// 响应式数据
const loading = ref(false)
const logList = ref([])
const total = ref(0)
const modules = ref([])
const dateRange = ref([])
const detailVisible = ref(false)
const currentLog = ref(null)

// 查询表单
const queryForm = reactive({
  username: '',
  module: '',
  keyword: '',
  page: 1,
  size: 10
})

// 页面加载时初始化
onMounted(() => {
  loadModules()
  loadLogs()
})

// 加载操作模块列表
const loadModules = async () => {
  try {
    const res = await getLogModules()
    if (res.data && Array.isArray(res.data)) {
      modules.value = res.data
    }
  } catch (error) {
    console.error('加载模块列表失败:', error)
  }
}

// 加载日志列表
const loadLogs = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      page: queryForm.page,
      size: queryForm.size,
      username: queryForm.username || undefined,
      module: queryForm.module || undefined,
      keyword: queryForm.keyword || undefined,
      startDate: dateRange.value && dateRange.value[0] ? dateRange.value[0] : undefined,
      endDate: dateRange.value && dateRange.value[1] ? dateRange.value[1] : undefined
    }

    const res = await getOperationLogs(params)
    
    // 处理响应数据
    if (res.data) {
      // 兼容 MyBatis PageHelper 格式
      if (res.data.list && Array.isArray(res.data.list)) {
        logList.value = res.data.list
        total.value = res.data.total || 0
      } else if (Array.isArray(res.data)) {
        logList.value = res.data
        total.value = res.data.length
      } else {
        logList.value = []
        total.value = 0
      }
    }
  } catch (error) {
    console.error('加载日志失败:', error)
    ElMessage.error('加载日志失败')
    logList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 查询按钮
const handleQuery = () => {
  queryForm.page = 1 // 重置到第一页
  loadLogs()
}

// 重置按钮
const handleReset = () => {
  queryForm.username = ''
  queryForm.module = ''
  queryForm.keyword = ''
  queryForm.page = 1
  queryForm.size = 10
  dateRange.value = []
  loadLogs()
}

// 查看详情
const handleDetail = (row) => {
  currentLog.value = { ...row }
  detailVisible.value = true
}

// 对话框关闭事件
const handleDialogClosed = () => {
  setTimeout(() => {
    currentLog.value = null
  }, 100)
}

// 导出Excel
const handleExport = async () => {
  try {
    loading.value = true
    
    // 构建导出参数
    const params = {
      module: queryForm.module || undefined,
      startDate: dateRange.value && dateRange.value[0] ? dateRange.value[0] : undefined,
      endDate: dateRange.value && dateRange.value[1] ? dateRange.value[1] : undefined
    }

    const res = await exportOperationLogs(params)
    
    // 创建下载链接
    const blob = new Blob([res], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    
    // 生成文件名
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-').substring(0, 19)
    link.download = `operation_logs_${timestamp}.xlsx`
    
    // 触发下载
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 格式化JSON
const formatJson = (jsonStr) => {
  if (!jsonStr) return '无'
  try {
    const obj = JSON.parse(jsonStr)
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return jsonStr
  }
}
</script>

<style scoped>
.log-manage {
  padding: 20px;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 24px;
  font-weight: 500;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  margin-bottom: -18px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.json-pre {
  background-color: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  max-height: 300px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-descriptions__label) {
  font-weight: 500;
}
</style>

