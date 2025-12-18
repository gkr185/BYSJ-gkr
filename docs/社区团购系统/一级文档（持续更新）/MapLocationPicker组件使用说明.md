# MapLocationPicker 地图选点组件使用说明

## 组件概述

`MapLocationPicker` 是一个基于高德地图API的Vue 3组件，提供地图选点功能，支持地址搜索、点击选点、标记拖拽等功能。

## 组件位置

```
community-group-buy-admin/src/components/MapLocationPicker.vue
```

## 功能特性

1. **地图选点对话框**
   - 900px宽度的对话框
   - 500px高度的地图显示区域
   - 使用说明提示

2. **三种定位方式**
   - 地址搜索定位
   - 点击地图选点
   - 拖拽标记微调

3. **智能功能**
   - 自动逆地理编码（经纬度→地址）
   - 自动获取省市区信息
   - 实时显示当前位置信息

4. **内存管理**
   - 对话框关闭时自动销毁地图实例
   - 避免内存泄漏

## Props 属性

| 属性名 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| modelValue | Boolean | false | 控制对话框显示/隐藏（支持v-model） |
| longitude | Number | 116.397428 | 初始经度（北京天安门） |
| latitude | Number | 39.90923 | 初始纬度（北京天安门） |
| address | String | '' | 初始地址（用于搜索框） |
| zoom | Number | 15 | 地图缩放级别（3-18） |

## Events 事件

| 事件名 | 参数 | 说明 |
|--------|------|------|
| update:modelValue | Boolean | 对话框显示状态变化时触发 |
| confirm | Object | 用户确认选择位置时触发 |
| cancel | - | 用户取消选择时触发 |

### confirm 事件参数

```javascript
{
  longitude: 116.397428,    // 经度（保留6位小数）
  latitude: 39.90923,       // 纬度（保留6位小数）
  address: '北京市东城区...',  // 详细地址
  province: '北京市',        // 省份
  city: '北京市',           // 城市
  district: '东城区'         // 区/县
}
```

## 基本使用

### 1. 导入组件

```vue
<script setup>
import MapLocationPicker from '../components/MapLocationPicker.vue'
import { ref } from 'vue'

const showMapDialog = ref(false)
const formData = ref({
  longitude: null,
  latitude: null,
  address: '',
  province: '',
  city: '',
  district: ''
})
</script>
```

### 2. 使用组件

```vue
<template>
  <div>
    <!-- 触发按钮 -->
    <el-button type="primary" @click="showMapDialog = true">
      <el-icon><Location /></el-icon>
      打开地图选择位置
    </el-button>
    
    <!-- 地图选点组件 -->
    <MapLocationPicker
      v-model="showMapDialog"
      :longitude="formData.longitude"
      :latitude="formData.latitude"
      :address="formData.address"
      @confirm="handleMapConfirm"
      @cancel="handleMapCancel"
    />
  </div>
</template>
```

### 3. 处理事件

```vue
<script setup>
const handleMapConfirm = (location) => {
  // 将选择的位置信息填入表单
  formData.value.longitude = location.longitude
  formData.value.latitude = location.latitude
  formData.value.address = location.address
  formData.value.province = location.province
  formData.value.city = location.city
  formData.value.district = location.district
  
  console.log('选择的位置:', location)
}

const handleMapCancel = () => {
  console.log('取消选择')
}
</script>
```

## 完整示例

### 示例1：社区管理（已实现）

```vue
<template>
  <el-form>
    <el-form-item label="经度">
      <el-input-number v-model="communityForm.longitude" />
    </el-form-item>
    
    <el-form-item label="纬度">
      <el-input-number v-model="communityForm.latitude" />
    </el-form-item>
    
    <el-form-item label="地图选点">
      <el-button type="primary" @click="mapDialogVisible = true">
        <el-icon><Location /></el-icon>
        打开地图选择位置
      </el-button>
    </el-form-item>
  </el-form>
  
  <!-- 地图选点组件 -->
  <MapLocationPicker
    v-model="mapDialogVisible"
    :longitude="communityForm.longitude"
    :latitude="communityForm.latitude"
    :address="communityForm.address"
    @confirm="handleMapConfirm"
  />
</template>

<script setup>
import { ref } from 'vue'
import MapLocationPicker from '../components/MapLocationPicker.vue'

const mapDialogVisible = ref(false)
const communityForm = ref({
  name: '',
  longitude: null,
  latitude: null,
  address: '',
  province: '',
  city: '',
  district: ''
})

const handleMapConfirm = (location) => {
  communityForm.value.longitude = location.longitude
  communityForm.value.latitude = location.latitude
  
  // 自动填充省市区（仅在字段为空时）
  if (!communityForm.value.province) {
    communityForm.value.province = location.province
  }
  if (!communityForm.value.city) {
    communityForm.value.city = location.city
  }
  if (!communityForm.value.district) {
    communityForm.value.district = location.district
  }
  
  // 自动填充地址（仅在字段为空时）
  if (!communityForm.value.address) {
    communityForm.value.address = location.address
  }
}
</script>
```

### 示例2：用户地址管理

```vue
<template>
  <el-dialog title="添加收货地址">
    <el-form>
      <el-form-item label="收货人">
        <el-input v-model="addressForm.receiver" />
      </el-form-item>
      
      <el-form-item label="手机号">
        <el-input v-model="addressForm.phone" />
      </el-form-item>
      
      <el-form-item label="详细地址">
        <el-input v-model="addressForm.detail" type="textarea" />
        <el-button @click="showMap = true" style="margin-top: 10px">
          地图选点
        </el-button>
      </el-form-item>
    </el-form>
  </el-dialog>
  
  <MapLocationPicker
    v-model="showMap"
    :longitude="addressForm.longitude"
    :latitude="addressForm.latitude"
    @confirm="handleLocationSelect"
  />
</template>

<script setup>
import { ref } from 'vue'
import MapLocationPicker from '../components/MapLocationPicker.vue'

const showMap = ref(false)
const addressForm = ref({
  receiver: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  longitude: null,
  latitude: null
})

const handleLocationSelect = (location) => {
  addressForm.value.province = location.province
  addressForm.value.city = location.city
  addressForm.value.district = location.district
  addressForm.value.detail = location.address
  addressForm.value.longitude = location.longitude
  addressForm.value.latitude = location.latitude
}
</script>
```

### 示例3：配送点管理

```vue
<template>
  <el-button @click="selectDeliveryPoint">
    选择配送点位置
  </el-button>
  
  <MapLocationPicker
    v-model="showMapPicker"
    :longitude="deliveryPoint.lng"
    :latitude="deliveryPoint.lat"
    :zoom="16"
    @confirm="handleDeliveryPointSelect"
  />
</template>

<script setup>
import { ref } from 'vue'
import MapLocationPicker from '../components/MapLocationPicker.vue'

const showMapPicker = ref(false)
const deliveryPoint = ref({
  name: '',
  lng: 116.397428,
  lat: 39.90923,
  address: ''
})

const selectDeliveryPoint = () => {
  showMapPicker.value = true
}

const handleDeliveryPointSelect = (location) => {
  deliveryPoint.value.lng = location.longitude
  deliveryPoint.value.lat = location.latitude
  deliveryPoint.value.address = location.address
  
  console.log('配送点位置:', deliveryPoint.value)
}
</script>
```

## 高级用法

### 1. 自定义缩放级别

```vue
<MapLocationPicker
  v-model="showMap"
  :zoom="18"
  @confirm="handleConfirm"
/>
```

缩放级别说明：
- 3-5：国家级
- 6-9：省级
- 10-12：市级
- 13-15：区县级
- 16-18：街道级

### 2. 动态更新初始位置

```vue
<template>
  <MapLocationPicker
    v-model="showMap"
    :longitude="currentLng"
    :latitude="currentLat"
    :address="currentAddress"
    @confirm="handleConfirm"
  />
</template>

<script setup>
import { ref, watch } from 'vue'

const currentLng = ref(116.397428)
const currentLat = ref(39.90923)
const currentAddress = ref('')

// 监听用户选择的社区，更新地图初始位置
watch(selectedCommunity, (community) => {
  if (community) {
    currentLng.value = community.longitude
    currentLat.value = community.latitude
    currentAddress.value = community.address
  }
})
</script>
```

### 3. 条件性自动填充

```vue
<script setup>
const handleMapConfirm = (location) => {
  // 只在编辑模式下自动填充地址
  if (isEditMode.value) {
    formData.value.address = location.address
  }
  
  // 总是更新经纬度
  formData.value.longitude = location.longitude
  formData.value.latitude = location.latitude
}
</script>
```

## 注意事项

1. **高德地图API依赖**
   - 确保 `index.html` 中已引入高德地图API
   - 需要包含 `AMap.Geocoder` 插件

2. **网络要求**
   - 需要能够访问高德地图API服务
   - 地理编码功能需要网络连接

3. **浏览器兼容性**
   - 支持现代浏览器（Chrome、Firefox、Edge等）
   - 不支持IE浏览器

4. **精度说明**
   - 经纬度保留6位小数
   - 精度约11厘米

5. **内存管理**
   - 组件会在对话框关闭时自动销毁地图实例
   - 无需手动管理内存

## 组件优势

1. **可复用性强**
   - 封装完整，开箱即用
   - 支持多处使用，互不干扰

2. **接口简洁**
   - Props和Events设计清晰
   - 易于理解和使用

3. **功能完整**
   - 支持三种定位方式
   - 自动获取地址信息

4. **用户体验好**
   - 使用说明清晰
   - 实时反馈位置信息

5. **维护方便**
   - 代码集中在一个文件
   - 修改不影响其他页面

## 相关文件

- **组件文件**：`community-group-buy-admin/src/components/MapLocationPicker.vue`
- **使用示例**：`community-group-buy-admin/src/views/CommunityManageView.vue`
- **API配置**：`community-group-buy-admin/index.html`

## 后续优化建议

1. 添加地图工具栏（缩放、平移等）
2. 支持绘制服务半径圆形区域
3. 添加地图图层切换（卫星图、路网图等）
4. 支持批量选点功能
5. 添加位置历史记录
6. 支持自定义地图样式
7. 添加加载状态提示
8. 支持离线地图缓存
