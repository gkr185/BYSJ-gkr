/**
 * 地图功能测试工具
 * 用于验证高德地图API的加载和基本功能
 */

// 检查高德地图API是否可用
export const checkAMapAvailability = () => {
  return new Promise((resolve) => {
    // 等待一段时间让API加载
    const checkInterval = setInterval(() => {
      if (typeof AMap !== 'undefined') {
        clearInterval(checkInterval)
        console.log('✅ 高德地图API加载成功')
        resolve(true)
      }
    }, 100)

    // 超时处理
    setTimeout(() => {
      clearInterval(checkInterval)
      console.warn('⚠️ 高德地图API加载超时')
      resolve(false)
    }, 5000)
  })
}

// 测试地图基本功能
export const testMapBasicFunction = () => {
  return new Promise((resolve, reject) => {
    try {
      if (typeof AMap === 'undefined') {
        reject(new Error('高德地图API未加载'))
        return
      }

      // 创建测试容器
      const testContainer = document.createElement('div')
      testContainer.id = 'test-map-container'
      testContainer.style.width = '1px'
      testContainer.style.height = '1px'
      testContainer.style.position = 'absolute'
      testContainer.style.left = '-9999px'
      document.body.appendChild(testContainer)

      // 创建地图实例
      const testMap = new AMap.Map('test-map-container', {
        zoom: 10,
        center: [116.397128, 39.916527]
      })

      // 监听地图加载完成
      testMap.on('complete', () => {
        console.log('✅ 地图实例创建成功')

        // 清理测试资源
        testMap.destroy()
        document.body.removeChild(testContainer)

        resolve(true)
      })

      // 监听地图加载失败
      testMap.on('error', (error) => {
        console.error('❌ 地图实例创建失败:', error)

        // 清理测试资源
        document.body.removeChild(testContainer)

        reject(error)
      })

    } catch (error) {
      console.error('❌ 地图测试失败:', error)
      reject(error)
    }
  })
}

// 运行完整测试
export const runMapTests = async () => {
  console.log('🚀 开始地图功能测试...')

  try {
    // 1. 检查API可用性
    const apiAvailable = await checkAMapAvailability()
    if (!apiAvailable) {
      throw new Error('高德地图API不可用')
    }

    // 2. 测试基本功能
    await testMapBasicFunction()

    console.log('🎉 所有地图测试通过！')
    return true

  } catch (error) {
    console.error('💥 地图测试失败:', error.message)
    return false
  }
}

// 默认导出测试函数
export default {
  checkAMapAvailability,
  testMapBasicFunction,
  runMapTests
}
