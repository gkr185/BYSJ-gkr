/**
 * 团长Store
 * 状态管理和业务逻辑
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import leaderApi from '@/api/leader'
import { ElMessage } from 'element-plus'

export const useLeaderStore = defineStore('leader', () => {
  // ===== 状态 =====
  
  /**
   * 团长信息
   */
  const leaderInfo = ref(null)
  
  /**
   * 工作台数据
   */
  const dashboard = ref(null)
  
  /**
   * 发起的团列表
   */
  const teams = ref([])
  
  /**
   * 拼团活动列表
   */
  const activities = ref([])
  
  /**
   * 配送订单列表
   */
  const deliveryOrders = ref([])
  
  /**
   * 佣金数据
   */
  const commission = ref(null)
  
  /**
   * 社区申请记录
   */
  const applications = ref([])
  
  // ===== Actions =====
  
  /**
   * 获取团长信息
   */
  const fetchLeaderInfo = async () => {
    try {
      const response = await leaderApi.getLeaderInfo()
      if (response.code === 200) {
        leaderInfo.value = response.data
        return response.data
      } else {
        throw new Error(response.msg)
      }
    } catch (error) {
      console.error('获取团长信息失败', error)
      ElMessage.error('获取团长信息失败')
      throw error
    }
  }
  
  /**
   * 获取工作台数据
   */
  const fetchDashboard = async () => {
    try {
      const response = await leaderApi.getDashboard()
      if (response.code === 200) {
        dashboard.value = response.data
        return response.data
      } else {
        throw new Error(response.msg)
      }
    } catch (error) {
      console.error('获取工作台数据失败', error)
      ElMessage.error('加载数据失败，请稍后重试')
      // 返回默认数据
      return {
        todayOrders: { newOrders: 0, toDeliver: 0, delivering: 0, todayCommission: 0 },
        activeTeams: [],
        pendingTasks: []
      }
    }
  }
  
  /**
   * 获取拼团活动列表
   */
  const fetchActivities = async () => {
    try {
      const response = await leaderApi.getActivities()
      if (response.code === 200) {
        activities.value = response.data
        return response.data
      } else {
        throw new Error(response.msg)
      }
    } catch (error) {
      console.error('获取活动列表失败', error)
      ElMessage.error('加载活动失败')
      throw error
    }
  }
  
  /**
   * 发起拼团
   * @param {Object} params - 发起参数
   */
  const launchTeam = async (params) => {
    try {
      const response = await leaderApi.launchTeam(params)
      if (response.code === 200) {
        ElMessage.success('发起成功')
        // 刷新团列表
        await fetchTeams()
        return response.data
      } else {
        throw new Error(response.msg)
      }
    } catch (error) {
      console.error('发起拼团失败', error)
      ElMessage.error('发起失败，请重试')
      throw error
    }
  }
  
  /**
   * 获取发起的团列表
   * @param {Boolean} forceRefresh - 是否强制刷新
   * @param {Object} params - 查询参数
   */
  const fetchTeams = async (forceRefresh = false, params = {}) => {
    // 如果已有数据且不强制刷新，直接返回
    if (teams.value.length > 0 && !forceRefresh) {
      return teams.value
    }
    
    try {
      const response = await leaderApi.getTeams(params)
      if (response.code === 200) {
        teams.value = response.data
        return response.data
      } else {
        throw new Error(response.msg)
      }
    } catch (error) {
      console.error('获取团列表失败', error)
      ElMessage.error('加载团列表失败')
      throw error
    }
  }
  
  /**
   * 获取团成员
   * @param {Number} teamId - 团ID
   */
  const fetchMembers = async (teamId) => {
    try {
      const response = await leaderApi.getMembers(teamId)
      if (response.code === 200) {
        return response.data
      } else {
        throw new Error(response.msg)
      }
    } catch (error) {
      console.error('获取团成员失败', error)
      ElMessage.error('加载成员失败')
      throw error
    }
  }
  
  /**
   * 获取配送订单
   */
  const fetchDeliveryOrders = async () => {
    try {
      const response = await leaderApi.getDeliveryOrders()
      if (response.code === 200) {
        deliveryOrders.value = response.data
        return response.data
      } else {
        throw new Error(response.msg)
      }
    } catch (error) {
      console.error('获取配送订单失败', error)
      ElMessage.error('加载订单失败')
      throw error
    }
  }
  
  /**
   * 生成配送路径
   * @param {Array} orders - 订单列表
   */
  const generateRoute = async (orders) => {
    try {
      const orderIds = orders.map(o => o.orderId)
      const response = await leaderApi.generateRoute(orderIds)
      if (response.code === 200) {
        return response.data
      } else {
        throw new Error(response.msg)
      }
    } catch (error) {
      console.error('生成配送路径失败', error)
      ElMessage.error('生成路径失败')
      throw error
    }
  }
  
  /**
   * 获取佣金数据
   */
  const fetchCommission = async () => {
    try {
      const response = await leaderApi.getCommission()
      if (response.code === 200) {
        commission.value = response.data
        return response.data
      } else {
        throw new Error(response.msg)
      }
    } catch (error) {
      console.error('获取佣金数据失败', error)
      ElMessage.error('加载佣金数据失败')
      throw error
    }
  }
  
  /**
   * 申请社区
   * @param {Object} params - 申请参数
   */
  const applyCommunity = async (params) => {
    try {
      const response = await leaderApi.applyCommunity(params)
      if (response.code === 200) {
        ElMessage.success(response.msg)
        // 刷新申请记录
        await fetchApplications()
      } else {
        throw new Error(response.msg)
      }
    } catch (error) {
      console.error('申请社区失败', error)
      ElMessage.error('申请失败，请重试')
      throw error
    }
  }
  
  /**
   * 获取社区申请记录
   */
  const fetchApplications = async () => {
    try {
      const response = await leaderApi.getApplications()
      if (response.code === 200) {
        applications.value = response.data
        return response.data
      } else {
        throw new Error(response.msg)
      }
    } catch (error) {
      console.error('获取申请记录失败', error)
      ElMessage.error('加载申请记录失败')
      throw error
    }
  }
  
  // ===== 返回 =====
  
  return {
    // 状态
    leaderInfo,
    dashboard,
    teams,
    activities,
    deliveryOrders,
    commission,
    applications,
    
    // Actions
    fetchLeaderInfo,
    fetchDashboard,
    fetchActivities,
    launchTeam,
    fetchTeams,
    fetchMembers,
    fetchDeliveryOrders,
    generateRoute,
    fetchCommission,
    applyCommunity,
    fetchApplications
  }
})
