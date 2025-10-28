/**
 * 团长Mock数据
 * 符合数据库v3.0设计
 */

// ===== 1. 团长信息 =====
export const mockLeaderInfo = {
  storeId: 1,
  leaderId: 1001,
  storeName: '幸福社区团点',
  communityId: 1,
  communityName: '幸福社区',
  province: '北京市',
  city: '北京市',
  district: '朝阳区',
  detailAddress: '幸福路123号',
  longitude: 116.404269,
  latitude: 39.915119,
  maxDeliveryRange: 3000,
  commissionRate: 5.00,
  auditStatus: 1,
  auditTime: '2025-10-20 15:30:00',
  createTime: '2025-10-20 15:30:00'
}

// ===== 2. 团长工作台数据 =====
export const mockLeaderDashboard = {
  todayOrders: {
    newOrders: 15,
    toDeliver: 23,
    delivering: 8,
    todayCommission: 125.50
  },
  activeTeams: [
    {
      teamId: 5001,
      teamNo: 'T20251027001',
      activityId: 1,
      activityName: '新鲜苹果3人团',
      productImg: '/images/products/apple.jpg',
      currentNum: 2,
      requiredNum: 3,
      teamStatus: 0,
      remainingTime: '23小时15分',
      createTime: '2025-10-27 10:30:00'
    },
    {
      teamId: 5002,
      teamNo: 'T20251027002',
      activityId: 2,
      activityName: '有机香蕉5人团',
      productImg: '/images/products/banana.jpg',
      currentNum: 4,
      requiredNum: 5,
      teamStatus: 0,
      remainingTime: '18小时30分',
      createTime: '2025-10-27 12:00:00'
    },
    {
      teamId: 5003,
      teamNo: 'T20251026003',
      activityId: 3,
      activityName: '进口橙子4人团',
      productImg: '/images/products/orange.jpg',
      currentNum: 4,
      requiredNum: 4,
      teamStatus: 1,
      remainingTime: '已成团',
      createTime: '2025-10-26 14:20:00'
    }
  ],
  pendingTasks: [
    '5个订单待发货',
    '2个拼团即将过期（24小时内）',
    '1个社区申请待提交'
  ]
}

// ===== 3. 发起的团列表 =====
export const mockLeaderTeams = [
  {
    teamId: 5001,
    teamNo: 'T20251027001',
    activityId: 1,
    activityName: '新鲜苹果3人团',
    productId: 101,
    productName: '新鲜苹果',
    productImg: '/images/products/apple.jpg',
    groupPrice: 29.90,
    price: 39.90,
    currentNum: 2,
    requiredNum: 3,
    teamStatus: 0,  // 0-拼团中
    launcherId: 1001,
    leaderId: 1001,
    communityId: 1,
    expireTime: '2025-10-28 10:30:00',
    createTime: '2025-10-27 10:30:00'
  },
  {
    teamId: 5002,
    teamNo: 'T20251027002',
    activityId: 2,
    activityName: '有机香蕉5人团',
    productId: 102,
    productName: '有机香蕉',
    productImg: '/images/products/banana.jpg',
    groupPrice: 19.90,
    price: 25.90,
    currentNum: 4,
    requiredNum: 5,
    teamStatus: 0,  // 0-拼团中
    launcherId: 1001,
    leaderId: 1001,
    communityId: 1,
    expireTime: '2025-10-28 12:00:00',
    createTime: '2025-10-27 12:00:00'
  },
  {
    teamId: 5003,
    teamNo: 'T20251026003',
    activityId: 3,
    activityName: '进口橙子4人团',
    productId: 103,
    productName: '进口橙子',
    productImg: '/images/products/orange.jpg',
    groupPrice: 35.00,
    price: 45.00,
    currentNum: 4,
    requiredNum: 4,
    teamStatus: 1,  // 1-已成团
    launcherId: 1001,
    leaderId: 1001,
    communityId: 1,
    expireTime: '2025-10-27 14:20:00',
    createTime: '2025-10-26 14:20:00',
    successTime: '2025-10-26 18:30:00'
  },
  {
    teamId: 5004,
    teamNo: 'T20251025004',
    activityId: 4,
    activityName: '新鲜草莓3人团',
    productId: 104,
    productName: '新鲜草莓',
    productImg: '/images/products/strawberry.jpg',
    groupPrice: 45.00,
    price: 58.00,
    currentNum: 1,
    requiredNum: 3,
    teamStatus: 2,  // 2-已失败
    launcherId: 1001,
    leaderId: 1001,
    communityId: 1,
    expireTime: '2025-10-26 10:00:00',
    createTime: '2025-10-25 10:00:00',
    failTime: '2025-10-26 10:00:00'
  },
  {
    teamId: 5005,
    teamNo: 'T20251027005',
    activityId: 5,
    activityName: '鲜牛奶6人团',
    productId: 105,
    productName: '鲜牛奶',
    productImg: '/images/products/milk.jpg',
    groupPrice: 25.90,
    price: 32.00,
    currentNum: 5,
    requiredNum: 6,
    teamStatus: 0,  // 0-拼团中
    launcherId: 1001,
    leaderId: 1001,
    communityId: 1,
    expireTime: '2025-10-28 15:00:00',
    createTime: '2025-10-27 15:00:00'
  }
]

// ===== 4. 团成员列表（按teamId索引）=====
export const mockTeamMembers = {
  5001: [
    {
      memberId: 1,
      teamId: 5001,
      userId: 1001,
      username: '张团长',
      avatar: '/avatars/user1.jpg',
      phone: '138****8000',
      orderId: 8001,
      isLauncher: true,
      status: 1,  // 1-已支付
      joinTime: '2025-10-27 10:30:00',
      payTime: '2025-10-27 10:31:00'
    },
    {
      memberId: 2,
      teamId: 5001,
      userId: 2002,
      username: '李四',
      avatar: '/avatars/user2.jpg',
      phone: '139****9001',
      orderId: 8002,
      isLauncher: false,
      status: 1,  // 1-已支付
      joinTime: '2025-10-27 11:15:00',
      payTime: '2025-10-27 11:16:00'
    }
  ],
  5002: [
    {
      memberId: 3,
      teamId: 5002,
      userId: 1001,
      username: '张团长',
      avatar: '/avatars/user1.jpg',
      phone: '138****8000',
      orderId: 8003,
      isLauncher: true,
      status: 1,
      joinTime: '2025-10-27 12:00:00',
      payTime: '2025-10-27 12:01:00'
    },
    {
      memberId: 4,
      teamId: 5002,
      userId: 2003,
      username: '王五',
      avatar: '/avatars/user3.jpg',
      phone: '137****7002',
      orderId: 8004,
      isLauncher: false,
      status: 1,
      joinTime: '2025-10-27 12:30:00',
      payTime: '2025-10-27 12:31:00'
    },
    {
      memberId: 5,
      teamId: 5002,
      userId: 2004,
      username: '赵六',
      avatar: '/avatars/user4.jpg',
      phone: '136****6003',
      orderId: 8005,
      isLauncher: false,
      status: 1,
      joinTime: '2025-10-27 13:00:00',
      payTime: '2025-10-27 13:01:00'
    },
    {
      memberId: 6,
      teamId: 5002,
      userId: 2005,
      username: '孙七',
      avatar: '/avatars/user5.jpg',
      phone: '135****5004',
      orderId: 8006,
      isLauncher: false,
      status: 1,
      joinTime: '2025-10-27 14:00:00',
      payTime: '2025-10-27 14:01:00'
    }
  ],
  5003: [
    {
      memberId: 7,
      teamId: 5003,
      userId: 1001,
      username: '张团长',
      avatar: '/avatars/user1.jpg',
      phone: '138****8000',
      orderId: 8007,
      isLauncher: true,
      status: 1,
      joinTime: '2025-10-26 14:20:00',
      payTime: '2025-10-26 14:21:00'
    },
    {
      memberId: 8,
      teamId: 5003,
      userId: 2006,
      username: '周八',
      avatar: '/avatars/user6.jpg',
      phone: '134****4005',
      orderId: 8008,
      isLauncher: false,
      status: 1,
      joinTime: '2025-10-26 15:00:00',
      payTime: '2025-10-26 15:01:00'
    },
    {
      memberId: 9,
      teamId: 5003,
      userId: 2007,
      username: '吴九',
      avatar: '/avatars/user7.jpg',
      phone: '133****3006',
      orderId: 8009,
      isLauncher: false,
      status: 1,
      joinTime: '2025-10-26 16:00:00',
      payTime: '2025-10-26 16:01:00'
    },
    {
      memberId: 10,
      teamId: 5003,
      userId: 2008,
      username: '郑十',
      avatar: '/avatars/user8.jpg',
      phone: '132****2007',
      orderId: 8010,
      isLauncher: false,
      status: 1,
      joinTime: '2025-10-26 18:20:00',
      payTime: '2025-10-26 18:21:00'
    }
  ]
}

// ===== 5. 配送订单列表 =====
export const mockDeliveryOrders = [
  {
    orderId: 8007,
    orderSn: 'GB20251026001',
    userId: 1001,
    userName: '张团长',
    phone: '138****8000',
    teamId: 5003,
    teamNo: 'T20251026003',
    productName: '进口橙子',
    quantity: 1,
    totalAmount: 35.00,
    orderStatus: 1,  // 1-待发货
    province: '北京市',
    city: '北京市',
    district: '朝阳区',
    address: '幸福小区1号楼101室',
    orderTime: '2025-10-26 14:21:00'
  },
  {
    orderId: 8008,
    orderSn: 'GB20251026002',
    userId: 2006,
    userName: '周八',
    phone: '134****4005',
    teamId: 5003,
    teamNo: 'T20251026003',
    productName: '进口橙子',
    quantity: 1,
    totalAmount: 35.00,
    orderStatus: 1,
    province: '北京市',
    city: '北京市',
    district: '朝阳区',
    address: '幸福小区3号楼202室',
    orderTime: '2025-10-26 15:01:00'
  },
  {
    orderId: 8009,
    orderSn: 'GB20251026003',
    userId: 2007,
    userName: '吴九',
    phone: '133****3006',
    teamId: 5003,
    teamNo: 'T20251026003',
    productName: '进口橙子',
    quantity: 1,
    totalAmount: 35.00,
    orderStatus: 1,
    province: '北京市',
    city: '北京市',
    district: '朝阳区',
    address: '幸福小区5号楼303室',
    orderTime: '2025-10-26 16:01:00'
  },
  {
    orderId: 8010,
    orderSn: 'GB20251026004',
    userId: 2008,
    userName: '郑十',
    phone: '132****2007',
    teamId: 5003,
    teamNo: 'T20251026003',
    productName: '进口橙子',
    quantity: 1,
    totalAmount: 35.00,
    orderStatus: 1,
    province: '北京市',
    city: '北京市',
    district: '朝阳区',
    address: '幸福小区1号楼501室',
    orderTime: '2025-10-26 18:21:00'
  }
]

// ===== 6. 配送路径参考 =====
export const mockDeliveryRoute = {
  routeId: 1,
  deliveryDate: '2025-10-27',
  points: [
    {
      pointId: 0,
      location: '幸福社区团点（起点）',
      address: '幸福路123号',
      orders: [],
      distance: 0,
      sequence: 1
    },
    {
      pointId: 1,
      location: '幸福小区1号楼',
      address: '幸福小区1号楼101室、501室',
      orders: [8007, 8010],
      distance: 0.5,  // 距离上一点的距离（km）
      sequence: 2
    },
    {
      pointId: 2,
      location: '幸福小区3号楼',
      address: '幸福小区3号楼202室',
      orders: [8008],
      distance: 0.3,
      sequence: 3
    },
    {
      pointId: 3,
      location: '幸福小区5号楼',
      address: '幸福小区5号楼303室',
      orders: [8009],
      distance: 0.4,
      sequence: 4
    }
  ],
  totalDistance: 1.2,
  estimatedTime: 25,  // 预计配送时间（分钟）
  orderCount: 4
}

// ===== 7. 佣金数据 =====
export const mockCommission = {
  balance: 1258.50,           // 可提现余额
  frozen: 125.00,             // 冻结金额（未结算）
  totalEarned: 5280.00,       // 累计佣金
  withdrawnTotal: 3896.50,    // 已提现总额
  records: [
    {
      commissionId: 1,
      orderId: 8007,
      orderSn: 'GB20251026001',
      productName: '进口橙子',
      orderAmount: 35.00,
      commissionRate: 5.00,
      amount: 1.75,            // 佣金金额
      status: 1,               // 0-未结算；1-已结算
      settleTime: '2025-10-27 10:00:00',
      createTime: '2025-10-26 14:21:00'
    },
    {
      commissionId: 2,
      orderId: 8008,
      orderSn: 'GB20251026002',
      productName: '进口橙子',
      orderAmount: 35.00,
      commissionRate: 5.00,
      amount: 1.75,
      status: 1,
      settleTime: '2025-10-27 10:00:00',
      createTime: '2025-10-26 15:01:00'
    },
    {
      commissionId: 3,
      orderId: 8009,
      orderSn: 'GB20251026003',
      productName: '进口橙子',
      orderAmount: 35.00,
      commissionRate: 5.00,
      amount: 1.75,
      status: 1,
      settleTime: '2025-10-27 10:00:00',
      createTime: '2025-10-26 16:01:00'
    },
    {
      commissionId: 4,
      orderId: 8010,
      orderSn: 'GB20251026004',
      productName: '进口橙子',
      orderAmount: 35.00,
      commissionRate: 5.00,
      amount: 1.75,
      status: 1,
      settleTime: '2025-10-27 10:00:00',
      createTime: '2025-10-26 18:21:00'
    },
    {
      commissionId: 5,
      orderId: 8001,
      orderSn: 'GB20251027001',
      productName: '新鲜苹果',
      orderAmount: 29.90,
      commissionRate: 5.00,
      amount: 1.50,
      status: 0,               // 未结算（拼团中）
      settleTime: null,
      createTime: '2025-10-27 10:31:00'
    },
    {
      commissionId: 6,
      orderId: 8002,
      orderSn: 'GB20251027002',
      productName: '新鲜苹果',
      orderAmount: 29.90,
      commissionRate: 5.00,
      amount: 1.50,
      status: 0,
      settleTime: null,
      createTime: '2025-10-27 11:16:00'
    }
  ]
}

// ===== 8. 社区申请记录 =====
export const mockCommunityApplications = [
  {
    applicationId: 1,
    applicantId: 1001,
    applicantName: '张团长',
    communityName: '阳光花园社区',
    province: '北京市',
    city: '北京市',
    district: '海淀区',
    address: '阳光花园小区',
    longitude: 116.301269,
    latitude: 39.975119,
    description: '该社区住户较多，团购需求大，期待开设团点',
    status: 0,              // 0-待审核；1-已通过；2-已拒绝
    createTime: '2025-10-27 14:30:00',
    updateTime: '2025-10-27 14:30:00',
    auditTime: null,
    rejectReason: null
  },
  {
    applicationId: 2,
    applicantId: 1001,
    applicantName: '张团长',
    communityName: '绿色家园',
    province: '北京市',
    city: '北京市',
    district: '朝阳区',
    address: '绿色家园小区',
    longitude: 116.501269,
    latitude: 39.925119,
    description: '新建小区，住户入住率高',
    status: 1,
    createTime: '2025-10-15 10:00:00',
    updateTime: '2025-10-16 09:30:00',
    auditTime: '2025-10-16 09:30:00',
    rejectReason: null
  },
  {
    applicationId: 3,
    applicantId: 1001,
    applicantName: '张团长',
    communityName: '老小区改造',
    province: '北京市',
    city: '北京市',
    district: '东城区',
    address: '老旧小区',
    longitude: 116.401269,
    latitude: 39.915119,
    description: '老旧小区，住户较少',
    status: 2,
    createTime: '2025-10-10 15:20:00',
    updateTime: '2025-10-11 10:00:00',
    auditTime: '2025-10-11 10:00:00',
    rejectReason: '该区域住户密度不足，暂不开设团点'
  }
]

// ===== 9. 活动列表（供发起拼团使用）=====
export const mockGroupBuyActivities = [
  {
    activityId: 1,
    activityName: '新鲜苹果3人团',
    productId: 101,
    productName: '新鲜苹果',
    productImg: '/images/products/apple.jpg',
    price: 39.90,
    groupPrice: 29.90,
    requiredNum: 3,
    timeLimit: 48,           // 成团时限（小时）
    activityStatus: 1,        // 1-进行中
    startTime: '2025-10-25 00:00:00',
    endTime: '2025-11-25 23:59:59',
    stock: 500,
    sales: 120
  },
  {
    activityId: 2,
    activityName: '有机香蕉5人团',
    productId: 102,
    productName: '有机香蕉',
    productImg: '/images/products/banana.jpg',
    price: 25.90,
    groupPrice: 19.90,
    requiredNum: 5,
    timeLimit: 48,
    activityStatus: 1,
    startTime: '2025-10-26 00:00:00',
    endTime: '2025-11-26 23:59:59',
    stock: 300,
    sales: 85
  },
  {
    activityId: 3,
    activityName: '进口橙子4人团',
    productId: 103,
    productName: '进口橙子',
    productImg: '/images/products/orange.jpg',
    price: 45.00,
    groupPrice: 35.00,
    requiredNum: 4,
    timeLimit: 48,
    activityStatus: 1,
    startTime: '2025-10-24 00:00:00',
    endTime: '2025-11-24 23:59:59',
    stock: 200,
    sales: 60
  },
  {
    activityId: 5,
    activityName: '鲜牛奶6人团',
    productId: 105,
    productName: '鲜牛奶',
    productImg: '/images/products/milk.jpg',
    price: 32.00,
    groupPrice: 25.90,
    requiredNum: 6,
    timeLimit: 48,
    activityStatus: 1,
    startTime: '2025-10-27 00:00:00',
    endTime: '2025-11-27 23:59:59',
    stock: 400,
    sales: 95
  }
]

export default {
  mockLeaderInfo,
  mockLeaderDashboard,
  mockLeaderTeams,
  mockTeamMembers,
  mockDeliveryOrders,
  mockDeliveryRoute,
  mockCommission,
  mockCommunityApplications,
  mockGroupBuyActivities
}
