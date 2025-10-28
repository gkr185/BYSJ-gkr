# TODO - 团长端界面开发待办事项

**任务名称**: 团长端界面原型开发（合并到用户端）  
**创建日期**: 2025-10-28  
**文档版本**: v1.0  
**状态**: 📝 待办清单

---

## 🎯 当前状态

✅ **原型开发完成** - 6个页面、Mock数据、路由配置全部完成  
⏳ **后端API待对接** - 等待后端开发完成  
⏳ **测试环境待配置** - 需要配置团长测试用户

---

## 📋 必要配置清单

### 1. ✅ 测试用户配置（优先级：高）

为了测试团长功能，需要配置一个`role=2`的测试用户：

**数据库配置**：
```sql
-- 更新现有用户为团长角色
UPDATE sys_user SET role = 2 WHERE user_id = 1001;

-- 或者创建新的团长用户
INSERT INTO sys_user (
  username, password, real_name, phone, role, 
  community_id, create_time
) VALUES (
  'leaderTest', 
  '$2a$10$...', -- BCrypt加密后的密码
  '测试团长', 
  '13800138000', 
  2, -- 团长角色
  1, -- 社区ID
  NOW()
);

-- 创建团长店铺信息
INSERT INTO group_leader_store (
  leader_id, store_name, community_id, province, city, district,
  detail_address, max_delivery_range, commission_rate, 
  audit_status, create_time
) VALUES (
  1001, -- 团长用户ID
  '幸福社区团点',
  1, -- 社区ID
  '北京市', '北京市', '朝阳区',
  '幸福路123号',
  3000, -- 最大配送范围（米）
  5.00, -- 佣金比例（%）
  1, -- 审核状态（1-已通过）
  NOW()
);
```

**登录测试**：
- 用户名：`leaderTest`
- 密码：（需要从数据库获取或设置）
- 预期：登录后`role=2`，个人中心显示团长菜单

---

### 2. ✅ 环境变量配置（优先级：高）

**前端配置** - `community-group-buy-frontend/.env`:
```env
# API基础地址（开发环境）
VITE_API_BASE_URL=http://localhost:8080

# 是否启用Mock数据（原型阶段为true，对接后改为false）
VITE_USE_MOCK=true
```

---

### 3. ⏳ 后端API开发（优先级：高）

需要后端开发以下API接口：

#### 3.1 团长信息接口
```
GET /api/leader/info
Response: {
  storeId, leaderId, storeName, communityId, communityName,
  maxDeliveryRange, commissionRate, auditStatus
}
```

#### 3.2 工作台数据接口
```
GET /api/leader/dashboard
Response: {
  todayOrders: { newOrders, toDeliver, delivering, todayCommission },
  activeTeams: [...],
  pendingTasks: [...]
}
```

#### 3.3 拼团活动列表
```
GET /api/leader/activities
Response: [ { activityId, activityName, productId, groupPrice, ... } ]
```

#### 3.4 发起拼团
```
POST /api/leader/team/launch
Body: { activityId, joinTeam, description }
Response: { teamId, teamNo, shareLink }
```

#### 3.5 团列表
```
GET /api/leader/teams?status=0
Response: [ { teamId, teamNo, activityName, currentNum, requiredNum, teamStatus, ... } ]
```

#### 3.6 团成员列表
```
GET /api/leader/team/{teamId}/members
Response: [ { memberId, userId, username, orderId, isLauncher, status, ... } ]
```

#### 3.7 配送订单
```
GET /api/leader/delivery/orders
Response: [ { orderId, orderSn, userName, productName, address, ... } ]
```

#### 3.8 生成配送路线
```
POST /api/leader/delivery/route
Body: { orderIds: [8001, 8002, 8003] }
Response: {
  points: [...],
  totalDistance: 2.5,
  estimatedTime: 30
}
```

#### 3.9 佣金数据
```
GET /api/leader/commission
Response: {
  balance, frozen, totalEarned, withdrawnTotal,
  records: [...]
}
```

#### 3.10 社区申请
```
POST /api/leader/community/apply
Body: { communityName, province, city, district, address, description }

GET /api/leader/community/applications
Response: [ { applicationId, communityName, status, ... } ]
```

---

### 4. ⏳ API对接修改（优先级：中）

当后端API开发完成后，修改 `src/api/leader.js`：

**修改前（Mock）**：
```javascript
getDashboard() {
  return Promise.resolve(mockLeaderDashboard)
}
```

**修改后（真实API）**：
```javascript
getDashboard() {
  return request.get('/api/leader/dashboard')
}
```

**批量替换**：
- 保留Mock数据文件（用于单元测试）
- 修改API文件的所有方法
- 添加错误处理
- 添加Loading状态

---

### 5. ⏳ 功能增强（优先级：低）

#### 5.1 图片上传

**影响页面**：发起拼团、社区申请

**实现方式**：
- 使用`el-upload`组件
- 对接后端图片上传接口
- 显示上传进度
- 图片压缩（可选）

#### 5.2 实时数据刷新

**影响页面**：团长工作台、团员管理

**实现方式**：
- 定时轮询（`setInterval`）
- WebSocket推送（推荐）
- 手动刷新按钮

#### 5.3 数据统计图表

**影响页面**：团长工作台

**实现方式**：
- 集成ECharts
- 佣金趋势图
- 订单统计图

---

## 🐛 已知问题

### 问题1：图片占位符

**描述**：Mock数据中的图片路径为占位符（`/images/products/apple.jpg`）

**影响**：图片无法显示

**解决方案**：
1. 临时方案：添加默认占位图
2. 长期方案：对接真实图片CDN

**优先级**：低

---

### 问题2：权限测试

**描述**：需要`role=2`的用户测试团长功能

**影响**：无法完整测试权限守卫

**解决方案**：
1. 创建测试用户（见上方配置）
2. 临时修改现有用户的role

**优先级**：高

---

## 📝 文档待完善

### 1. API文档

需要后端提供详细的API文档：
- 请求参数
- 响应格式
- 错误码
- 示例

### 2. 部署文档

需要编写前端部署文档：
- 构建命令
- 环境变量配置
- Nginx配置
- HTTPS配置

### 3. 用户手册

需要编写团长用户手册：
- 功能说明
- 操作步骤
- 常见问题

---

## 🚀 下一步行动

### 立即行动（本周）

1. ✅ **配置测试用户**
   - 在数据库中创建`role=2`的用户
   - 创建对应的团长店铺信息
   - 测试登录和权限

2. ✅ **联系后端**
   - 提供API需求清单
   - 确认数据格式
   - 约定联调时间

### 短期计划（1-2周）

1. ⏳ **后端API开发**
   - 团长相关10个接口
   - 按优先级开发

2. ⏳ **API对接**
   - 修改`src/api/leader.js`
   - 测试所有接口
   - 错误处理优化

3. ⏳ **集成测试**
   - 端到端测试
   - 权限测试
   - 性能测试

### 中期计划（3-4周）

1. ⏳ **功能增强**
   - 图片上传
   - 实时数据刷新
   - 数据统计图表

2. ⏳ **体验优化**
   - 骨架屏
   - 图片懒加载
   - 动画效果

3. ⏳ **文档完善**
   - API文档
   - 部署文档
   - 用户手册

---

## 📞 联系方式

### 需要支持时联系

**前端开发**：[您的联系方式]  
**后端开发**：[后端联系方式]  
**产品经理**：[产品联系方式]

---

## ✅ 检查清单

### 开发环境检查

- [ ] Node.js版本正确（18+）
- [ ] npm/pnpm安装正常
- [ ] 依赖包安装完整
- [ ] 开发服务器正常启动
- [ ] 无ESLint错误

### 测试环境检查

- [ ] 测试用户已创建（`role=2`）
- [ ] 团长店铺信息已配置
- [ ] 登录功能正常
- [ ] 权限守卫正常
- [ ] Mock数据正常

### 生产准备检查

- [ ] 后端API全部完成
- [ ] API对接完成
- [ ] 端到端测试通过
- [ ] 性能测试通过
- [ ] 文档完善
- [ ] 部署配置完成

---

**创建时间**: 2025-10-28  
**更新时间**: 2025-10-28  
**状态**: 📝 持续更新

