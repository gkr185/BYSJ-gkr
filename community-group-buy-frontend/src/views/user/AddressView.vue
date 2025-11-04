<template>
  <MainLayout>
    <div class="address-container">
      <div class="page-header">
        <el-button :icon="ArrowLeft" @click="$router.back()">返回</el-button>
        <h2 class="page-title">
          <el-icon><Location /></el-icon>
          收货地址
        </h2>
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          添加地址
        </el-button>
      </div>

      <div v-loading="loading" class="address-list">
        <el-empty v-if="!loading && addressList.length === 0" description="暂无收货地址">
          <el-button type="primary" @click="handleAdd">添加第一个地址</el-button>
        </el-empty>

        <div
          v-for="address in addressList"
          :key="address.addressId"
          class="address-card"
          :class="{ 'is-default': address.isDefault }"
        >
          <el-tag v-if="address.isDefault" class="default-tag" type="danger" effect="dark">
            默认地址
          </el-tag>

          <div class="address-content">
            <div class="address-header">
              <div class="user-info">
                <span class="name">{{ address.receiver || address.receiverName }}</span>
                <span class="phone">{{ address.phone || address.receiverPhone }}</span>
              </div>
            </div>

            <div class="address-detail">
              <el-icon><Location /></el-icon>
              <span>{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detail || address.detailAddress }}</span>
            </div>

            <div v-if="address.label" class="address-label">
              <el-tag size="small" type="info">{{ address.label }}</el-tag>
            </div>
          </div>

          <div class="address-actions">
            <el-button
              v-if="!address.isDefault"
              type="primary"
              text
              :icon="Star"
              @click="handleSetDefault(address)"
            >
              设为默认
            </el-button>
            <el-button
              type="primary"
              text
              :icon="Edit"
              @click="handleEdit(address)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              text
              :icon="Delete"
              @click="handleDelete(address)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 添加/编辑地址对话框 -->
      <el-dialog
        v-model="showDialog"
        :title="dialogTitle"
        width="600px"
        :close-on-click-modal="false"
      >
        <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="100px"
        >
          <el-form-item label="收货人" prop="receiverName">
            <el-input
              v-model="formData.receiverName"
              placeholder="请输入收货人姓名"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>

          <el-form-item label="手机号" prop="receiverPhone">
            <el-input
              v-model="formData.receiverPhone"
              placeholder="请输入手机号"
              :prefix-icon="Phone"
              maxlength="11"
              clearable
            />
          </el-form-item>

          <el-form-item label="省份" prop="province">
            <el-input
              v-model="formData.province"
              placeholder="请输入省份"
              clearable
            />
          </el-form-item>

          <el-form-item label="城市" prop="city">
            <el-input
              v-model="formData.city"
              placeholder="请输入城市"
              clearable
            />
          </el-form-item>

          <el-form-item label="区县" prop="district">
            <el-input
              v-model="formData.district"
              placeholder="请输入区县"
              clearable
            />
          </el-form-item>

          <el-form-item label="详细地址" prop="detailAddress">
            <el-input
              v-model="formData.detailAddress"
              type="textarea"
              :rows="3"
              placeholder="请输入详细地址（街道、门牌号等）"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <el-form-item label="位置定位">
            <div class="location-section">
              <div class="location-input-group">
                <el-input
                  v-model="formData.longitude"
                  placeholder="经度"
                  type="number"
                  step="0.000001"
                  style="width: 48%"
                >
                  <template #prepend>经度</template>
                </el-input>
                <el-input
                  v-model="formData.latitude"
                  placeholder="纬度"
                  type="number"
                  step="0.000001"
                  style="width: 48%"
                >
                  <template #prepend>纬度</template>
                </el-input>
              </div>
              <div class="location-buttons">
                <el-button
                  size="small"
                  :icon="Location"
                  :loading="gettingLocation"
                  @click="getCurrentLocation"
                >
                  获取当前位置
                </el-button>
                <el-button
                  size="small"
                  :icon="MapLocation"
                  @click="geocodeAddress"
                  :disabled="!canGeocode"
                >
                  根据地址解析
                </el-button>
                <el-button
                  size="small"
                  type="primary"
                  :icon="Pointer"
                  @click="showMapDialog = true"
                >
                  地图选点
                </el-button>
              </div>
              <div class="location-tip">
                <el-text size="small" type="info">
                  提示：经纬度用于计算配送距离，建议使用"获取当前位置"或"地图选点"
                </el-text>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="地址标签">
            <el-radio-group v-model="formData.label">
              <el-radio label="家">家</el-radio>
              <el-radio label="公司">公司</el-radio>
              <el-radio label="学校">学校</el-radio>
              <el-radio label="">其他</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="设为默认">
            <el-switch v-model="formData.isDefault" />
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="showDialog = false">取消</el-button>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
            确定
          </el-button>
        </template>
      </el-dialog>

      <!-- 地图选点对话框 -->
      <el-dialog
        v-model="showMapDialog"
        title="地图选点"
        width="800px"
        :close-on-click-modal="false"
      >
        <div class="map-container">
          <div id="amap-container" style="width: 100%; height: 500px;"></div>
          <div class="map-info">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="经度">{{ mapLocation.longitude }}</el-descriptions-item>
              <el-descriptions-item label="纬度">{{ mapLocation.latitude }}</el-descriptions-item>
              <el-descriptions-item label="地址" :span="2">{{ mapLocation.address }}</el-descriptions-item>
            </el-descriptions>
          </div>
        </div>
        <template #footer>
          <el-button @click="showMapDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmMapLocation">
            确认位置
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
  Location,
  Plus,
  Star,
  Edit,
  Delete,
  User,
  Phone,
  MapLocation,
  Pointer
} from '@element-plus/icons-vue'
import MainLayout from '@/components/common/MainLayout.vue'
import {
  getAddressList,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress
} from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const submitLoading = ref(false)
const showDialog = ref(false)
const dialogTitle = ref('添加地址')
const addressList = ref([])
const editingId = ref(null)
const gettingLocation = ref(false)
const showMapDialog = ref(false)

// 地图相关
let amapInstance = null
let amapMarker = null
const mapLocation = reactive({
  longitude: 0,
  latitude: 0,
  address: ''
})

// 表单数据
const formData = reactive({
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  label: '',
  isDefault: false,
  longitude: 0,
  latitude: 0
})

// 计算属性：是否可以进行地理编码
const canGeocode = computed(() => {
  return formData.province && formData.city && formData.district && formData.detailAddress
})

// 验证规则（简化，只保留必填）
const rules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' }
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
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

// 加载地址列表
const loadAddressList = async () => {
  // 检查用户是否登录
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    return
  }

  loading.value = true
  try {
    const res = await getAddressList(userStore.userInfo.userId)
    if (res.code === 200) {
      addressList.value = res.data || []
    } else {
      ElMessage.error(res.message || '加载地址列表失败')
    }
  } catch (error) {
    ElMessage.error('加载地址列表失败')
    console.error('加载地址列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.receiverName = ''
  formData.receiverPhone = ''
  formData.province = ''
  formData.city = ''
  formData.district = ''
  formData.detailAddress = ''
  formData.label = ''
  formData.isDefault = false
  formData.longitude = 0
  formData.latitude = 0
  editingId.value = null
  formRef.value?.clearValidate()
}

// 获取当前位置（使用浏览器定位）
const getCurrentLocation = () => {
  if (!navigator.geolocation) {
    ElMessage.error('您的浏览器不支持地理定位')
    return
  }

  gettingLocation.value = true
  navigator.geolocation.getCurrentPosition(
    (position) => {
      formData.latitude = position.coords.latitude
      formData.longitude = position.coords.longitude
      ElMessage.success(`定位成功！经度: ${formData.longitude.toFixed(6)}, 纬度: ${formData.latitude.toFixed(6)}`)
      gettingLocation.value = false
      
      // 尝试逆地理编码获取地址
      reverseGeocode(formData.longitude, formData.latitude)
    },
    (error) => {
      gettingLocation.value = false
      let errorMsg = '定位失败'
      switch (error.code) {
        case error.PERMISSION_DENIED:
          errorMsg = '用户拒绝了定位请求'
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

// 根据地址解析经纬度（地理编码）
const geocodeAddress = async () => {
  if (!canGeocode.value) {
    ElMessage.warning('请先填写完整的省市区和详细地址')
    return
  }

  const address = `${formData.province}${formData.city}${formData.district}${formData.detailAddress}`
  
  try {
    // 使用高德地图Web服务API进行地理编码
    // 注意：这里使用了JSONP方式，实际项目中建议在后端调用
    const key = 'YOUR_AMAP_KEY' // 需要替换为实际的高德地图key
    const url = `https://restapi.amap.com/v3/geocode/geo?key=${key}&address=${encodeURIComponent(address)}`
    
    const response = await fetch(url)
    const data = await response.json()
    
    if (data.status === '1' && data.geocodes && data.geocodes.length > 0) {
      const location = data.geocodes[0].location.split(',')
      formData.longitude = parseFloat(location[0])
      formData.latitude = parseFloat(location[1])
      ElMessage.success('地址解析成功！')
    } else {
      ElMessage.error('地址解析失败，请检查地址是否正确')
    }
  } catch (error) {
    console.error('地理编码失败:', error)
    ElMessage.error('地址解析失败，请稍后重试')
  }
}

// 逆地理编码（根据经纬度获取地址）
const reverseGeocode = async (lng, lat) => {
  try {
    const key = 'YOUR_AMAP_KEY'
    const url = `https://restapi.amap.com/v3/geocode/regeo?key=${key}&location=${lng},${lat}`
    
    const response = await fetch(url)
    const data = await response.json()
    
    if (data.status === '1' && data.regeocode) {
      const addressComponent = data.regeocode.addressComponent
      if (!formData.province) formData.province = addressComponent.province
      if (!formData.city) formData.city = addressComponent.city || addressComponent.province
      if (!formData.district) formData.district = addressComponent.district
      
      ElMessage.info('已自动填充地址信息')
    }
  } catch (error) {
    console.error('逆地理编码失败:', error)
  }
}

// 初始化地图
const initMap = () => {
  // 这里使用简单的提示，实际需要引入高德地图SDK
  if (typeof AMap === 'undefined') {
    ElMessage.warning('地图服务加载中，请稍后重试...')
    return
  }
  
  // 创建地图实例
  amapInstance = new AMap.Map('amap-container', {
    zoom: 15,
    center: [formData.longitude || 116.397428, formData.latitude || 39.90923]
  })
  
  // 创建标记
  amapMarker = new AMap.Marker({
    position: amapInstance.getCenter(),
    draggable: true
  })
  amapMarker.setMap(amapInstance)
  
  // 更新位置信息
  updateMapLocation(amapInstance.getCenter())
  
  // 监听标记拖拽
  amapMarker.on('dragend', (e) => {
    updateMapLocation(e.target.getPosition())
  })
  
  // 监听地图点击
  amapInstance.on('click', (e) => {
    amapMarker.setPosition(e.lnglat)
    updateMapLocation(e.lnglat)
  })
}

// 更新地图位置信息
const updateMapLocation = async (position) => {
  mapLocation.longitude = position.lng
  mapLocation.latitude = position.lat
  
  // 逆地理编码获取地址
  try {
    const key = 'YOUR_AMAP_KEY'
    const url = `https://restapi.amap.com/v3/geocode/regeo?key=${key}&location=${position.lng},${position.lat}`
    const response = await fetch(url)
    const data = await response.json()
    
    if (data.status === '1' && data.regeocode) {
      mapLocation.address = data.regeocode.formatted_address
    }
  } catch (error) {
    console.error('获取地址失败:', error)
  }
}

// 确认地图位置
const confirmMapLocation = () => {
  formData.longitude = mapLocation.longitude
  formData.latitude = mapLocation.latitude
  
  // 如果地址未填写，尝试自动填充
  reverseGeocode(mapLocation.longitude, mapLocation.latitude)
  
  showMapDialog.value = false
  ElMessage.success('位置已选定')
}

// 添加地址
const handleAdd = () => {
  resetForm()
  dialogTitle.value = '添加地址'
  showDialog.value = true
}

// 编辑地址
const handleEdit = (address) => {
  formData.receiverName = address.receiver || address.receiverName
  formData.receiverPhone = address.phone || address.receiverPhone
  formData.province = address.province
  formData.city = address.city
  formData.district = address.district
  formData.detailAddress = address.detail || address.detailAddress
  formData.label = address.label || ''
  formData.isDefault = address.isDefault === 1 || address.isDefault === true
  formData.longitude = address.longitude || 0
  formData.latitude = address.latitude || 0
  editingId.value = address.addressId
  dialogTitle.value = '编辑地址'
  showDialog.value = true
}

// 设为默认
const handleSetDefault = async (address) => {
  // 检查用户是否登录
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const res = await setDefaultAddress(userStore.userInfo.userId, address.addressId)
    if (res.code === 200) {
      ElMessage.success('设置成功')
      await loadAddressList()
    } else {
      ElMessage.error(res.message || '设置失败')
    }
  } catch (error) {
    ElMessage.error('设置失败')
    console.error('设置默认地址失败:', error)
  }
}

// 删除地址
const handleDelete = (address) => {
  // 检查用户是否登录
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  ElMessageBox.confirm(
    `确定要删除收货地址「${address.receiver || address.receiverName}，${address.phone || address.receiverPhone}」吗？`,
    '删除地址',
    {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    }
  ).then(async () => {
    try {
      const res = await deleteAddress(userStore.userInfo.userId, address.addressId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        await loadAddressList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败')
      console.error('删除地址失败:', error)
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  // 检查用户是否登录
  if (!userStore.isLogin || !userStore.userInfo?.userId) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const data = {
        userId: userStore.userInfo.userId,
        receiver: formData.receiverName,
        phone: formData.receiverPhone,
        province: formData.province,
        city: formData.city,
        district: formData.district,
        detail: formData.detailAddress,
        latitude: formData.latitude || 0,
        longitude: formData.longitude || 0,
        isDefault: formData.isDefault ? 1 : 0
      }

      let res
      if (editingId.value) {
        // 编辑
        res = await updateAddress(editingId.value, data)
      } else {
        // 添加
        res = await addAddress(data)
      }

      if (res.code === 200) {
        ElMessage.success(editingId.value ? '修改成功' : '添加成功')
        showDialog.value = false
        await loadAddressList()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch (error) {
      ElMessage.error('操作失败')
      console.error('保存地址失败:', error)
    } finally {
      submitLoading.value = false
    }
  })
}

onMounted(() => {
  // 检查用户是否登录
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  loadAddressList()
})
</script>

<style scoped>
.address-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px;
}

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
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header :deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.page-header :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

/* 地址列表 */
.address-list {
  display: grid;
  gap: 20px;
  min-height: 300px;
}

.address-card {
  position: relative;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  border: 2px solid transparent;
}

.address-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.address-card.is-default {
  border-color: #f5576c;
  background: linear-gradient(135deg, rgba(245, 87, 108, 0.02) 0%, rgba(240, 147, 251, 0.02) 100%);
}

.default-tag {
  position: absolute;
  top: 16px;
  right: 16px;
  border-radius: 8px;
}

.address-content {
  margin-bottom: 16px;
}

.address-header {
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.name {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.phone {
  font-size: 15px;
  color: #666;
}

.address-detail {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.address-detail .el-icon {
  color: #667eea;
  flex-shrink: 0;
  margin-top: 2px;
}

.address-label {
  margin-top: 8px;
}

.address-actions {
  display: flex;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.address-actions :deep(.el-button) {
  border-radius: 8px;
  font-weight: 600;
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
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

:deep(.el-textarea__inner) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

:deep(.el-radio) {
  margin-right: 24px;
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
}

:deep(.el-switch.is-checked .el-switch__core) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .address-container {
    padding: 16px;
  }

  .page-header {
    flex-wrap: wrap;
  }

  .address-card {
    padding: 16px;
  }

  .user-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .address-actions {
    flex-wrap: wrap;
  }
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

.location-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.location-buttons :deep(.el-button) {
  border-radius: 8px;
  font-size: 13px;
}

.location-tip {
  margin-top: 8px;
}

/* 地图容器样式 */
.map-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.map-info {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

#amap-container {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
</style>

