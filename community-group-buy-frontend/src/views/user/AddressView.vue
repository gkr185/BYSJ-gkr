<template>
  <div class="address-page">
    <div class="header-bar">
      <h2>收货地址</h2>
      <el-button type="primary" @click="showAddressEdit(null)">新增地址</el-button>
    </div>
    
    <el-table :data="addressList" border>
      <el-table-column prop="receiver" label="收件人" width="100" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column label="地址">
        <template #default="{ row }">
          {{ row.province }}{{ row.city }}{{ row.district }}{{ row.detail }}
        </template>
      </el-table-column>
      <el-table-column label="位置坐标" width="180" align="center">
        <template #default="{ row }">
          <div v-if="row.longitude && row.latitude" style="font-size: 12px; color: #666;">
            <div>经度: {{ row.longitude }}°</div>
            <div>纬度: {{ row.latitude }}°</div>
          </div>
          <el-tag v-else type="info" size="small">未设置</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="默认" width="80" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isDefault === 1" type="success" size="small">默认</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" align="center">
        <template #default="{ row }">
          <el-button text type="primary" size="small" @click="showAddressEdit(row)">编辑</el-button>
          <el-button text type="primary" size="small" v-if="row.isDefault !== 1" @click="handleSetDefault(row)">设为默认</el-button>
          <el-button text type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="addressList.length === 0" description="暂无地址" />

    <!-- 新增/编辑地址对话框 -->
    <el-dialog
      v-model="showEditDialog"
      :title="editingAddressId ? '编辑地址' : '新增地址'"
      width="600px"
    >
      <el-alert 
        type="info" 
        :closable="false" 
        style="margin-bottom: 15px;"
      >
        <el-button link type="primary" size="small" @click="fillUserInfo">
          <el-icon><User /></el-icon> 从个人信息填充
        </el-button>
        <el-button link type="primary" size="small" @click="getCurrentLocation" style="margin-left: 10px;">
          <el-icon><Location /></el-icon> 获取当前位置
        </el-button>
      </el-alert>

      <el-form :model="addressForm" label-width="90px">
        <el-form-item label="收件人">
          <el-input v-model="addressForm.receiver" placeholder="请输入收件人姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        
        <el-divider content-position="left">地址信息</el-divider>
        
        <el-form-item label="省份">
          <el-input v-model="addressForm.province" placeholder="如：北京市" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="addressForm.city" placeholder="如：北京市" />
        </el-form-item>
        <el-form-item label="区/县">
          <el-input v-model="addressForm.district" placeholder="如：海淀区" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input 
            v-model="addressForm.detail" 
            type="textarea" 
            rows="3" 
            placeholder="请输入详细地址（街道、门牌号等）" 
          />
        </el-form-item>
        
        <el-divider content-position="left">位置坐标</el-divider>
        
        <el-form-item label="经度">
          <el-input 
            v-model="addressForm.longitude" 
            placeholder="自动获取或手动输入" 
            type="number"
          >
            <template #append>
              <span v-if="locationLoading">定位中...</span>
              <span v-else-if="addressForm.longitude">°E</span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="纬度">
          <el-input 
            v-model="addressForm.latitude" 
            placeholder="自动获取或手动输入" 
            type="number"
          >
            <template #append>
              <span v-if="addressForm.latitude">°N</span>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="默认地址">
          <el-switch v-model="addressForm.isDefault" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveAddress">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Location } from '@element-plus/icons-vue'
import {
  getAddressList,
  addAddress,
  updateAddress,
  deleteAddress,
  setDefaultAddress,
  getUserInfo
} from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const addressList = ref([])
const showEditDialog = ref(false)
const editingAddressId = ref(null)
const locationLoading = ref(false)

const addressForm = ref({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  longitude: '',
  latitude: '',
  isDefault: false
})

// 获取地址列表
const fetchAddressList = async () => {
  if (!userStore.userInfo?.userId) return
  
  try {
    const data = await getAddressList(userStore.userInfo.userId)
    addressList.value = data
  } catch (error) {
    console.error('Failed to fetch address list:', error)
  }
}

// 显示编辑对话框
const showAddressEdit = async (item) => {
  if (item) {
    // 编辑模式
    editingAddressId.value = item.addressId
    addressForm.value = {
      receiver: item.receiver,
      phone: item.phone,
      province: item.province,
      city: item.city,
      district: item.district,
      detail: item.detail,
      longitude: item.longitude || '',
      latitude: item.latitude || '',
      isDefault: item.isDefault === 1
    }
  } else {
    // 新增模式
    editingAddressId.value = null
    addressForm.value = {
      receiver: '',
      phone: '',
      province: '',
      city: '',
      district: '',
      detail: '',
      longitude: '',
      latitude: '',
      isDefault: false
    }
  }
  showEditDialog.value = true
}

// 从个人信息填充
const fillUserInfo = async () => {
  if (!userStore.userInfo?.userId) return
  
  try {
    const userInfo = await getUserInfo(userStore.userInfo.userId)
    
    // 填充收件人和手机号
    if (userInfo.realName) {
      addressForm.value.receiver = userInfo.realName
    } else if (userInfo.username) {
      addressForm.value.receiver = userInfo.username
    }
    
    if (userInfo.phone) {
      addressForm.value.phone = userInfo.phone
    }
    
    ElMessage.success('已填充个人信息')
  } catch (error) {
    console.error('Failed to fetch user info:', error)
    ElMessage.warning('获取个人信息失败')
  }
}

// 获取当前位置
const getCurrentLocation = () => {
  if (!navigator.geolocation) {
    ElMessage.error('您的浏览器不支持定位功能')
    return
  }
  
  locationLoading.value = true
  ElMessage.info('正在获取位置...')
  
  navigator.geolocation.getCurrentPosition(
    (position) => {
      addressForm.value.longitude = position.coords.longitude.toFixed(6)
      addressForm.value.latitude = position.coords.latitude.toFixed(6)
      locationLoading.value = false
      ElMessage.success(`定位成功！经度: ${addressForm.value.longitude}, 纬度: ${addressForm.value.latitude}`)
    },
    (error) => {
      locationLoading.value = false
      let errorMsg = '定位失败'
      switch(error.code) {
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
      console.error('Geolocation error:', error)
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    }
  )
}

// 保存地址
const handleSaveAddress = async () => {
  if (!userStore.userInfo?.userId) return
  
  try {
    const requestData = {
      userId: userStore.userInfo.userId,
      receiver: addressForm.value.receiver,
      phone: addressForm.value.phone,
      province: addressForm.value.province,
      city: addressForm.value.city,
      district: addressForm.value.district,
      detail: addressForm.value.detail,
      longitude: addressForm.value.longitude ? parseFloat(addressForm.value.longitude) : 0,
      latitude: addressForm.value.latitude ? parseFloat(addressForm.value.latitude) : 0,
      isDefault: addressForm.value.isDefault ? 1 : 0
    }
    
    if (editingAddressId.value) {
      await updateAddress(editingAddressId.value, requestData)
      ElMessage.success('修改成功')
    } else {
      await addAddress(requestData)
      ElMessage.success('添加成功')
    }
    
    showEditDialog.value = false
    await fetchAddressList()
  } catch (error) {
    console.error('Failed to save address:', error)
    ElMessage.error('保存失败')
  }
}

// 设为默认
const handleSetDefault = async (item) => {
  if (!userStore.userInfo?.userId) return
  
  try {
    await setDefaultAddress(userStore.userInfo.userId, item.addressId)
    ElMessage.success('已设为默认地址')
    await fetchAddressList()
  } catch (error) {
    console.error('Failed to set default address:', error)
    ElMessage.error('设置失败')
  }
}

// 删除地址
const handleDelete = async (item) => {
  if (!userStore.userInfo?.userId) return
  
  ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      await deleteAddress(userStore.userInfo.userId, item.addressId)
      ElMessage.success('删除成功')
      await fetchAddressList()
    } catch (error) {
      console.error('Failed to delete address:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchAddressList()
})
</script>

<style scoped>
.address-page {
  padding: 20px;
}

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h2 {
  color: #333;
  margin: 0;
}
</style>
