<template>
  <div class="delivery-route-map">
    <el-row :gutter="20">
      <el-col :span="16">
        <div id="route-map-container" style="width: 100%; height: 600px;"></div>
      </el-col>
      <el-col :span="8">
        <div id="route-panel" style="width: 100%; height: 600px; overflow-y: auto; background-color: white; border: 1px solid #ddd; border-radius: 4px;"></div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  // 起点（仓库）
  startPoint: {
    type: Object,
    required: true,
    // { longitude: 116.397428, latitude: 39.90923, name: '中心仓库' }
  },
  // 途经点列表
  waypoints: {
    type: Array,
    default: () => []
    // [{ longitude: 116.379028, latitude: 39.885042, name: '途经点1' }]
  },
  // 终点（可选）
  endPoint: {
    type: Object,
    default: null
  },
  // 路径策略
  strategy: {
    type: Number,
    default: 1
    // 0-最短时间，1-最短距离，2-避开拥堵
  },
  // 是否使用高德API（如果false则只显示直线）
  useGaodeAPI: {
    type: Boolean,
    default: false
  }
})

let map = null
let driving = null

const initMap = () => {
  // 创建地图实例
  map = new AMap.Map('route-map-container', {
    resizeEnable: true,
    center: [props.startPoint.longitude, props.startPoint.latitude],
    zoom: 12
  })

  // 添加起点标记
  const startMarker = new AMap.Marker({
    position: [props.startPoint.longitude, props.startPoint.latitude],
    title: props.startPoint.name || '起点',
    label: {
      content: '起',
      direction: 'top'
    },
    icon: new AMap.Icon({
      size: new AMap.Size(25, 34),
      image: '//a.amap.com/jsapi_demos/static/demo-center/icons/dir-marker.png',
      imageSize: new AMap.Size(135, 40),
      imageOffset: new AMap.Pixel(-9, -3)
    })
  })
  map.add(startMarker)

  if (props.useGaodeAPI) {
    // 使用高德地图驾车路径规划API
    initGaodeDriving()
  } else {
    // 使用简单直线显示路径（基于Dijkstra算法结果）
    drawStraightRoute()
  }
}

const initGaodeDriving = () => {
  // 构造路线导航类（关键：设置panel参数）
  driving = new AMap.Driving({
    map: map,
    panel: 'route-panel',  // 关键：显示详细导航信息的容器
    policy: getGaodeStrategy(),
    hideMarkers: false     // 显示标记
  })

  // 准备途经点（高德API最多支持16个途经点）
  let waypointLngLats = props.waypoints.map(wp => 
    new AMap.LngLat(wp.longitude, wp.latitude)
  )

  // 限制途经点数量
  if (waypointLngLats.length > 16) {
    console.warn('高德API最多支持16个途经点，当前' + waypointLngLats.length + '个，已截取前16个')
    waypointLngLats = waypointLngLats.slice(0, 16)
  }

  // 确定终点
  let endLngLat
  let finalWaypoints

  if (props.endPoint) {
    // 有明确终点
    endLngLat = new AMap.LngLat(props.endPoint.longitude, props.endPoint.latitude)
    finalWaypoints = waypointLngLats
  } else {
    // 无终点，最后一个途经点作为终点
    if (waypointLngLats.length > 0) {
      endLngLat = waypointLngLats[waypointLngLats.length - 1]
      finalWaypoints = waypointLngLats.slice(0, -1)
    } else {
      endLngLat = new AMap.LngLat(props.startPoint.longitude, props.startPoint.latitude)
      finalWaypoints = []
    }
  }

  console.log('规划路径:', {
    起点: [props.startPoint.longitude, props.startPoint.latitude],
    途经点数量: finalWaypoints.length,
    终点: [endLngLat.getLng(), endLngLat.getLat()],
    策略: getGaodeStrategy()
  })

  // 规划路线（关键：会在panel中显示详细导航信息）
  driving.search(
    new AMap.LngLat(props.startPoint.longitude, props.startPoint.latitude),
    endLngLat,
    { waypoints: finalWaypoints },
    (status, result) => {
      if (status === 'complete') {
        console.log('✅ 绘制驾车路线完成')
        console.log('路径信息:', result)
        // result包含详细导航信息：
        // - routes[0].distance: 总距离（米）
        // - routes[0].time: 总时间（秒）
        // - routes[0].steps[]: 详细路段信息（向东南行驶103米左转等）
      } else {
        console.error('❌ 获取驾车数据失败：', result)
        ElMessage.error('路径规划失败，请检查坐标是否正确')
      }
    }
  )
}

const drawStraightRoute = () => {
  // 基于Dijkstra算法结果，绘制简单直线路径
  const allPoints = [
    [props.startPoint.longitude, props.startPoint.latitude],
    ...props.waypoints.map(wp => [wp.longitude, wp.latitude])
  ]

  if (props.endPoint) {
    allPoints.push([props.endPoint.longitude, props.endPoint.latitude])
  }

  // 绘制折线
  const polyline = new AMap.Polyline({
    path: allPoints,
    borderWeight: 2,
    strokeColor: '#3366FF',
    strokeWeight: 6,
    strokeOpacity: 0.8,
    lineJoin: 'round'
  })
  map.add(polyline)

  // 添加途经点标记
  props.waypoints.forEach((wp, index) => {
    const marker = new AMap.Marker({
      position: [wp.longitude, wp.latitude],
      title: wp.name || `途经点${index + 1}`,
      label: {
        content: `${index + 1}`,
        direction: 'top'
      }
    })
    map.add(marker)
  })

  // 添加终点标记
  if (props.endPoint) {
    const endMarker = new AMap.Marker({
      position: [props.endPoint.longitude, props.endPoint.latitude],
      title: props.endPoint.name || '终点',
      label: {
        content: '终',
        direction: 'top'
      },
      icon: new AMap.Icon({
        size: new AMap.Size(25, 34),
        image: '//a.amap.com/jsapi_demos/static/demo-center/icons/dir-marker.png',
        imageSize: new AMap.Size(135, 40),
        imageOffset: new AMap.Pixel(-95, -3)
      })
    })
    map.add(endMarker)
  }

  // 自动调整视野
  map.setFitView()
}

const getGaodeStrategy = () => {
  // 路径策略映射
  // 参考：https://lbs.amap.com/api/javascript-api/reference/route-search#m_DrivingPolicy
  const strategyMap = {
    0: AMap.DrivingPolicy.LEAST_TIME,      // 0-最短时间（速度最快）
    1: AMap.DrivingPolicy.LEAST_DISTANCE,  // 1-最短距离（距离最短）
    2: AMap.DrivingPolicy.REAL_TRAFFIC     // 2-避开拥堵（躲避拥堵）
  }
  return strategyMap[props.strategy] || AMap.DrivingPolicy.LEAST_DISTANCE
}

// 监听props变化，重新绘制路径
watch(
  () => [props.startPoint, props.waypoints, props.endPoint],
  () => {
    if (map) {
      map.destroy()
    }
    initMap()
  },
  { deep: true }
)

onMounted(() => {
  // 确保高德地图API已加载
  if (window.AMap) {
    initMap()
  } else {
    console.error('高德地图API未加载，请检查index.html中的script标签')
  }
})
</script>

<style scoped>
.delivery-route-map {
  width: 100%;
}

#route-map-container {
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

#route-panel {
  padding: 10px;
}

/* 高德地图导航面板样式 */
#route-panel :deep(.amap-lib-driving) {
  border-radius: 4px;
  overflow: hidden;
}

/* 导航信息标题样式 */
#route-panel :deep(.amap-lib-driving-title) {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  padding: 10px;
  background: #f5f7fa;
  border-bottom: 1px solid #eee;
}

/* 路段详情样式 */
#route-panel :deep(.amap-lib-driving-detail) {
  font-size: 13px;
  line-height: 1.8;
  padding: 10px;
}

/* 路段步骤样式 */
#route-panel :deep(.amap-lib-driving-detail-item) {
  padding: 8px;
  border-bottom: 1px dashed #eee;
}

#route-panel :deep(.amap-lib-driving-detail-item:hover) {
  background-color: #f5f7fa;
}
</style>

