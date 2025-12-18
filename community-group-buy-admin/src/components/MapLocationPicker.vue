<template>
  <div class="map-location-picker">
    <el-dialog
      v-model="dialogVisible"
      title="地图选点 - 点击地图选择位置"
      width="900px"
      @opened="initMap"
      @closed="handleClose"
    >
      <div class="map-dialog-content">
        <el-alert 
          title="使用说明" 
          type="info" 
          :closable="false"
          style="margin-bottom: 15px"
        >
          <p>1. 在下方输入框输入地址，点击"搜索定位"按钮</p>
          <p>2. 或直接点击地图选择位置</p>
          <p>3. 拖动标记可微调位置</p>
          <p>4. 蓝色圆圈表示服务范围覆盖区域</p>
        </el-alert>
        
        <div class="map-search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="输入地址进行搜索，如：北京市海淀区中关村"
            clearable
            @keyup.enter="searchLocation"
            style="width: 50%"
          >
            <template #prepend>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="searchLocation" style="margin-left: 10px">
            搜索定位
          </el-button>
          <el-input-number
            v-model="serviceRadius"
            :min="500"
            :max="10000"
            :step="100"
            @change="updateCircle"
            style="margin-left: 10px; width: 150px"
          />
          <span style="margin-left: 5px">米</span>
          <el-button 
            :type="showCircle ? 'success' : 'info'" 
            @click="toggleCircle" 
            style="margin-left: 10px"
          >
            {{ showCircle ? '隐藏' : '显示' }}服务范围
          </el-button>
        </div>
        
        <div id="map-picker-container" style="width: 100%; height: 500px; margin-top: 15px;"></div>
        
        <el-descriptions :column="2" border style="margin-top: 15px" size="small">
          <el-descriptions-item label="当前经度">
            {{ currentLocation.longitude.toFixed(6) }}
          </el-descriptions-item>
          <el-descriptions-item label="当前纬度">
            {{ currentLocation.latitude.toFixed(6) }}
          </el-descriptions-item>
          <el-descriptions-item label="服务半径">
            {{ serviceRadius }} 米
          </el-descriptions-item>
          <el-descriptions-item label="覆盖面积">
            {{ (Math.PI * Math.pow(serviceRadius / 1000, 2)).toFixed(2) }} 平方公里
          </el-descriptions-item>
          <el-descriptions-item label="详细地址" :span="2">
            {{ currentLocation.address || '点击地图获取地址信息' }}
          </el-descriptions-item>
          <el-descriptions-item label="省份" v-if="currentLocation.province">
            {{ currentLocation.province }}
          </el-descriptions-item>
          <el-descriptions-item label="城市" v-if="currentLocation.city">
            {{ currentLocation.city }}
          </el-descriptions-item>
          <el-descriptions-item label="区/县" v-if="currentLocation.district" :span="2">
            {{ currentLocation.district }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleConfirm">
          确认选择此位置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  // 是否显示对话框
  modelValue: {
    type: Boolean,
    default: false
  },
  // 初始经度
  longitude: {
    type: Number,
    default: 116.397428
  },
  // 初始纬度
  latitude: {
    type: Number,
    default: 39.90923
  },
  // 初始地址（用于搜索框）
  address: {
    type: String,
    default: ''
  },
  // 地图缩放级别
  zoom: {
    type: Number,
    default: 15
  },
  // 服务半径（米）
  radius: {
    type: Number,
    default: 3000
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

// 对话框显示状态
const dialogVisible = ref(false)
const searchKeyword = ref('')

// 服务范围相关
const serviceRadius = ref(3000)
const showCircle = ref(true)

// 地图相关变量
let mapInstance = null
let mapMarker = null
let mapCircle = null
let geocoder = null

// 当前位置信息
const currentLocation = reactive({
  longitude: 116.397428,
  latitude: 39.90923,
  address: '',
  province: '',
  city: '',
  district: ''
})

// 监听 modelValue 变化
watch(() => props.modelValue, (newVal) => {
  dialogVisible.value = newVal
  if (newVal) {
    // 打开对话框时，初始化位置信息
    currentLocation.longitude = props.longitude || 116.397428
    currentLocation.latitude = props.latitude || 39.90923
    searchKeyword.value = props.address || ''
    serviceRadius.value = props.radius || 3000
  }
})

// 监听 dialogVisible 变化
watch(dialogVisible, (newVal) => {
  emit('update:modelValue', newVal)
})

// 初始化地图
const initMap = () => {
  if (!window.AMap) {
    ElMessage.error('地图服务未加载，请刷新页面重试')
    return
  }
  
  // 创建地图实例
  mapInstance = new AMap.Map('map-picker-container', {
    resizeEnable: true,
    center: [currentLocation.longitude, currentLocation.latitude],
    zoom: props.zoom
  })
  
  // 创建可拖拽的标记
  mapMarker = new AMap.Marker({
    position: [currentLocation.longitude, currentLocation.latitude],
    draggable: true,
    cursor: 'move',
    raiseOnDrag: true
  })
  mapInstance.add(mapMarker)
  
  // 创建服务范围圆圈
  mapCircle = new AMap.Circle({
    center: [currentLocation.longitude, currentLocation.latitude],
    radius: serviceRadius.value,
    strokeColor: '#1890ff',
    strokeOpacity: 0.8,
    strokeWeight: 2,
    fillColor: '#1890ff',
    fillOpacity: 0.2
  })
  if (showCircle.value) {
    mapInstance.add(mapCircle)
  }
  
  // 异步加载地理编码插件
  AMap.plugin('AMap.Geocoder', () => {
    // 创建地理编码实例
    geocoder = new AMap.Geocoder({
      radius: 1000,
      extensions: 'all'
    })
    
    // 初始化时获取地址
    reverseGeocode(currentLocation.longitude, currentLocation.latitude)
  })
  
  // 监听地图点击事件
  mapInstance.on('click', (e) => {
    const lng = e.lnglat.getLng()
    const lat = e.lnglat.getLat()
    mapMarker.setPosition([lng, lat])
    updateLocation(lng, lat)
  })
  
  // 监听标记拖拽结束事件
  mapMarker.on('dragend', (e) => {
    const position = e.target.getPosition()
    updateLocation(position.lng, position.lat)
  })
}

// 更新位置信息
const updateLocation = (lng, lat) => {
  currentLocation.longitude = lng
  currentLocation.latitude = lat
  reverseGeocode(lng, lat)
  
  // 更新圆圈位置
  if (mapCircle) {
    mapCircle.setCenter([lng, lat])
  }
}

// 逆地理编码：经纬度 -> 地址
const reverseGeocode = (lng, lat) => {
  if (!geocoder) return
  
  geocoder.getAddress([lng, lat], (status, result) => {
    if (status === 'complete' && result.regeocode) {
      currentLocation.address = result.regeocode.formattedAddress
      
      // 获取省市区信息
      const addressComponent = result.regeocode.addressComponent
      if (addressComponent) {
        currentLocation.province = addressComponent.province || ''
        currentLocation.city = addressComponent.city || ''
        currentLocation.district = addressComponent.district || ''
      }
    } else {
      currentLocation.address = '无法获取地址信息'
      currentLocation.province = ''
      currentLocation.city = ''
      currentLocation.district = ''
    }
  })
}

// 地址搜索：地址 -> 经纬度
const searchLocation = () => {
  if (!searchKeyword.value) {
    ElMessage.warning('请输入搜索地址')
    return
  }
  
  if (!geocoder) {
    ElMessage.error('地图服务未就绪')
    return
  }
  
  // 地理编码
  geocoder.getLocation(searchKeyword.value, (status, result) => {
    if (status === 'complete' && result.geocodes.length > 0) {
      const location = result.geocodes[0].location
      const lng = location.lng
      const lat = location.lat
      
      // 更新地图中心和标记位置
      mapInstance.setCenter([lng, lat])
      mapMarker.setPosition([lng, lat])
      
      // 更新位置信息
      updateLocation(lng, lat)
      
      ElMessage.success('定位成功')
    } else {
      ElMessage.error('未找到该地址，请尝试更详细的地址')
    }
  })
}

// 更新圆圈半径
const updateCircle = () => {
  if (mapCircle) {
    mapCircle.setRadius(serviceRadius.value)
    // 自动调整地图视野以完整显示圆圈
    mapInstance.setFitView([mapCircle])
  }
}

// 切换圆圈显示/隐藏
const toggleCircle = () => {
  showCircle.value = !showCircle.value
  if (mapCircle) {
    if (showCircle.value) {
      mapInstance.add(mapCircle)
      mapInstance.setFitView([mapCircle])
    } else {
      mapInstance.remove(mapCircle)
    }
  }
}

// 确认选择
const handleConfirm = () => {
  emit('confirm', {
    longitude: parseFloat(currentLocation.longitude.toFixed(6)),
    latitude: parseFloat(currentLocation.latitude.toFixed(6)),
    address: currentLocation.address,
    province: currentLocation.province,
    city: currentLocation.city,
    district: currentLocation.district,
    serviceRadius: serviceRadius.value
  })
  dialogVisible.value = false
}

// 取消选择
const handleCancel = () => {
  emit('cancel')
  dialogVisible.value = false
}

// 关闭对话框时销毁地图
const handleClose = () => {
  if (mapInstance) {
    mapInstance.destroy()
    mapInstance = null
  }
  mapMarker = null
  mapCircle = null
  geocoder = null
}
</script>

<style scoped>
.map-location-picker {
  display: inline-block;
}

.map-dialog-content {
  padding: 0;
}

.map-search-bar {
  display: flex;
  align-items: center;
}

#map-picker-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

/* 高德地图控件样式优化 */
#map-picker-container :deep(.amap-logo),
#map-picker-container :deep(.amap-copyright) {
  opacity: 0.5;
}

#map-picker-container :deep(.amap-marker) {
  cursor: move;
}
</style>
