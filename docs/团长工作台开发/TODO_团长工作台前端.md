# TODO - 团长工作台前端待办事项

**任务名称**: 团长工作台前端开发  
**创建日期**: 2025-11-13  
**文档版本**: v1.0

---

## 🚀 立即测试

### 1. 启动前端服务

```bash
cd e:\E\BYSJ\community-group-buy-frontend
npm run dev
```

访问地址：`http://localhost:5173`

---

### 2. 后端服务检查

**需要启动的微服务**:
- ✅ LeaderService (端口: 8068)
- ✅ OrderService (端口: 8065)
- ✅ UserService (端口: 8061)
- ✅ API Gateway (端口: 9000)

**检查命令**:
```bash
# 检查LeaderService
curl http://localhost:8068/actuator/health

# 检查OrderService
curl http://localhost:8065/actuator/health

# 检查UserService
curl http://localhost:8061/actuator/health

# 检查API Gateway
curl http://localhost:9000/actuator/health
```

---

### 3. 测试账号准备

**需要准备的测试数据**:

#### 团长账号
```sql
-- 查询团长用户
SELECT * FROM user WHERE role = 2;

-- 如果没有，创建一个测试团长
INSERT INTO user (username, password, role, ...) 
VALUES ('test_leader', '...', 2, ...);
```

#### 测试数据
```sql
-- 佣金记录数据
SELECT * FROM commission WHERE leader_id = {团长ID};

-- 订单数据
SELECT * FROM order_main WHERE leader_id = {团长ID};
```

---

## ✅ 测试清单

### 功能测试

#### 佣金管理页面
- [ ] 访问 `http://localhost:5173/leader/commission`
- [ ] 检查统计卡片数据是否正确
- [ ] 检查佣金记录列表是否正确
- [ ] 测试状态筛选（全部/待结算/已结算）
- [ ] 测试分页功能
- [ ] 测试返回按钮

#### 订单管理页面
- [ ] 访问 `http://localhost:5173/leader/orders`
- [ ] 检查统计卡片数据是否正确
- [ ] 检查订单列表是否正确
- [ ] 测试状态筛选（全部/待发货/配送中/已送达）
- [ ] 测试分页功能
- [ ] 测试查看详情跳转
- [ ] 测试返回按钮

#### 工作台入口
- [ ] 访问 `http://localhost:5173/leader/dashboard`
- [ ] 点击"佣金管理"按钮
- [ ] 点击"我的订单"按钮

### 权限测试
- [ ] 未登录访问（应跳转登录页）
- [ ] 普通用户访问（应提示权限不足）
- [ ] 团长用户访问（应正常访问）

### 响应式测试
- [ ] 移动端（375px宽度）
- [ ] 平板（768px宽度）
- [ ] 桌面端（1920px宽度）

---

## ⚠️ 可能遇到的问题

### 问题1: API请求404

**原因**: 后端接口未启动或路径不正确

**解决方案**:
```bash
# 1. 检查后端服务是否启动
# 2. 检查API Gateway配置
# 3. 查看浏览器Console错误信息
```

---

### 问题2: 权限验证失败

**原因**: Token过期或用户角色不正确

**解决方案**:
```bash
# 1. 重新登录获取新Token
# 2. 检查用户role字段是否为2
# 3. 检查UserStore中isLeader的值
```

---

### 问题3: 数据不显示

**原因**: 没有测试数据

**解决方案**:
```sql
-- 插入测试佣金记录
INSERT INTO commission (...) VALUES (...);

-- 插入测试订单
INSERT INTO order_main (...) VALUES (...);
```

---

### 问题4: 图片不显示

**原因**: 商品图片路径错误

**解决方案**:
```bash
# 检查图片服务器是否启动
# 检查图片URL是否正确
# 检查CORS配置
```

---

## 📋 后续开发建议

### 短期优化（可选）
1. **添加订单搜索** - 按订单号/用户名搜索
2. **添加导出功能** - 导出Excel
3. **添加数据刷新按钮** - 手动刷新数据

### 中期优化（可选）
1. **添加图表统计** - 佣金趋势图、订单趋势图
2. **添加数据缓存** - Pinia缓存统计数据
3. **添加实时通知** - WebSocket订单状态推送

---

## 🎯 验收标准

### 基本功能
- ✅ 页面能正常访问
- ✅ 数据能正常显示
- ✅ 筛选和分页功能正常
- ✅ 跳转功能正常
- ✅ 权限验证正常

### 用户体验
- ✅ 界面美观
- ✅ 交互流畅
- ✅ 响应速度快
- ✅ 错误提示友好

---

## 📞 技术支持

如遇到问题，请检查：

1. **浏览器Console** - 查看错误信息
2. **Network面板** - 查看API请求状态
3. **Vue DevTools** - 查看组件状态
4. **后端日志** - 查看服务端错误

---

**完成状态**: ✅ 开发完成，等待测试  
**测试负责人**: 用户  
**预计测试时间**: 30分钟
