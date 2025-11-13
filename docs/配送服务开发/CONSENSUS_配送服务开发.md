# 配送服务开发 - 共识文档

**文档类型**: CONSENSUS - 6A工作流共识阶段  
**创建日期**: 2025-11-13  
**任务名称**: DeliveryService微服务开发  
**技术方案**: 基于高德地图API的智能配送路径规划  
**预估工期**: 2-3天  
**前置依赖**: OrderService, UserService, AdminService

---

## 📋 明确的需求描述

### 核心功能需求
基于用户确认的业务流程，配送服务需要实现以下核心功能：

#### 1. **管理端批量发货流程**
```
管理员选择待发货订单
    ↓
系统生成dispatch_group（时间戳+批次号）
    ↓
批量更新订单状态为"配送中"
    ↓
自动调用DeliveryService生成配送路径
    ↓
返回配送单ID和路径信息
```

#### 2. **智能路径规划服务**
- **技术方案**: 集成高德地图路径规划API
- **起点确定**: 统一仓库/配送中心地址（系统配置）
- **终点集合**: 同一dispatch_group的所有收货地址
- **路径优化**: 最短时间或最短距离（可配置）

#### 3. **配送状态管理**
```
待发货 → 配送中 → 已送达 → 已收货
   ↑        ↑        ↑       ↑
管理端   自动生成   团长确认  用户确认
```

#### 4. **团长配送监控**
- 查看当前负责的配送任务列表
- 查看配送路径地图可视化
- 更新配送状态（已送达）
- 监控配送进度

#### 5. **地图可视化展示**
- 在前端地图上显示配送路径
- 标记起点（仓库）和各个配送点
- 支持路径导航功能

---

## 🏗️ 技术实现方案

### 系统架构设计

#### 微服务组件架构
```
DeliveryService (8067)
├── controller/
│   ├── DeliveryController.java      - 配送单管理
│   ├── RouteController.java         - 路径规划
│   └── MonitorController.java       - 配送监控
├── service/
│   ├── DeliveryService.java         - 配送业务逻辑
│   ├── GaodeRouteService.java       - 高德API集成
│   ├── BatchService.java            - 批量发货处理
│   └── MonitorService.java          - 配送监控服务
├── feign/
│   ├── OrderServiceClient.java      - 订单服务调用
│   ├── UserServiceClient.java       - 用户服务调用
│   └── AdminServiceClient.java      - 管理服务调用（新增）
├── config/
│   ├── GaodeConfig.java            - 高德API配置
│   └── WarehouseConfig.java        - 仓库地址配置
└── entity/
    ├── Delivery.java               - 配送单实体
    ├── RoutePoint.java             - 路径点实体
    └── DeliveryStatus.java         - 配送状态枚举
```

#### 高德API集成方案
**路径规划API**:
```java
@Service
public class GaodeRouteService {
    
    @Value("${gaode.api.key}")
    private String apiKey;
    
    @Value("${gaode.api.url}")
    private String apiUrl;
    
    /**
     * 计算多点配送路径
     * @param origin 起点坐标（仓库地址）
     * @param waypoints 配送点列表
     * @return 优化后的路径方案
     */
    public RouteResult planDeliveryRoute(GeoPoint origin, List<GeoPoint> waypoints) {
        // 调用高德路径规划API
        // 支持多种优化策略：最短时间、最短距离、避开拥堵
        // 返回路径坐标、距离、预估时间
    }
}
```

**地图可视化数据**:
```java
public class RouteResult {
    private String routePath;           // 路径坐标串
    private Double totalDistance;       // 总距离（公里）
    private Integer totalDuration;      // 预估时间（分钟）
    private List<RoutePoint> waypoints; // 优化后的配送点顺序
    private String mapDisplayData;      // 前端地图展示数据
}
```

### 数据库设计更新

#### 配送单表优化
```sql
-- 更新delivery表结构，适配高德API
ALTER TABLE delivery 
MODIFY optimal_route TEXT COMMENT '高德API返回的路径数据（JSON格式）',
ADD COLUMN route_strategy TINYINT DEFAULT 0 COMMENT '路径策略（0-最短时间；1-最短距离；2-避开拥堵）',
ADD COLUMN estimated_duration INT COMMENT '预估配送时间（分钟）',
ADD COLUMN actual_start_time DATETIME COMMENT '实际开始配送时间',
ADD COLUMN route_display_data TEXT COMMENT '前端地图展示数据';
```

#### 仓库地址配置表（新增）
```sql
CREATE TABLE warehouse_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_name VARCHAR(100) NOT NULL COMMENT '仓库名称',
    address VARCHAR(255) NOT NULL COMMENT '仓库地址',
    longitude DECIMAL(10,6) NOT NULL COMMENT '经度',
    latitude DECIMAL(10,6) NOT NULL COMMENT '纬度',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认仓库',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用；1-启用）'
) COMMENT = '仓库配置表';
```

---

## 🔗 技术约束和集成方案

### API集成约束
**高德地图API要求**:
- **API密钥管理**: 使用.env文件存储，不提交到Git
- **请求频次限制**: 考虑API调用配额，实现合理缓存
- **错误处理**: 处理API调用失败的降级方案
- **数据格式**: 遵循高德API的坐标系统（GCJ-02）

### 微服务集成方案

#### 1. **与AdminService集成**
```java
// 管理端批量发货接口
@PostMapping("/api/admin/orders/batch-ship")
public Result<BatchShipResult> batchShipOrders(@RequestBody BatchShipRequest request) {
    // 1. 生成dispatch_group
    // 2. 批量更新订单状态
    // 3. 调用DeliveryService.createDeliveryRoute()
    // 4. 返回配送单信息
}
```

#### 2. **与OrderService集成**
```java
// 订单状态同步接口
public interface OrderServiceClient {
    @PutMapping("/api/orders/batch-update-status")
    Result<Void> batchUpdateOrderStatus(@RequestBody BatchUpdateRequest request);
    
    @GetMapping("/api/orders/by-dispatch-group/{dispatchGroup}")
    Result<List<OrderInfo>> getOrdersByDispatchGroup(@PathVariable String dispatchGroup);
}
```

#### 3. **与UserService集成**
```java
// 地址信息获取接口
public interface UserServiceClient {
    @GetMapping("/api/addresses/{addressId}")
    Result<AddressInfo> getAddressById(@PathVariable Long addressId);
    
    @PostMapping("/api/addresses/batch")
    Result<List<AddressInfo>> batchGetAddresses(@RequestBody List<Long> addressIds);
}
```

### 前端集成方案

#### 管理端集成
```javascript
// 批量发货功能
async function batchShipOrders(orderIds) {
    const response = await adminApi.batchShip({
        orderIds: orderIds,
        warehouseId: defaultWarehouseId
    });
    
    // 显示配送路径信息
    showDeliveryRouteInfo(response.data);
}
```

#### 团长端集成
```javascript
// 配送监控页面
async function loadDeliveryTasks() {
    const tasks = await deliveryApi.getMyDeliveryTasks();
    tasks.forEach(task => {
        // 在地图上显示配送路径
        displayRouteOnMap(task.routeData);
    });
}

// 地图可视化（高德地图JavaScript API）
function displayRouteOnMap(routeData) {
    const map = new AMap.Map('mapContainer');
    const routePath = JSON.parse(routeData.routePath);
    
    // 绘制配送路径
    const polyline = new AMap.Polyline({
        path: routePath,
        strokeColor: '#3366FF',
        strokeWeight: 4
    });
    map.add(polyline);
    
    // 添加起点和配送点标记
    addDeliveryMarkers(map, routeData.waypoints);
}
```

---

## 📊 任务边界限制

### 功能边界
**本次开发范围**:
- ✅ DeliveryService微服务开发
- ✅ 高德API路径规划集成
- ✅ 配送单管理功能
- ✅ 管理端批量发货接口
- ✅ 团长端配送监控接口
- ✅ 基础地图可视化数据提供

**不在本次范围**:
- ❌ 前端地图组件开发（由前端团队负责）
- ❌ 实时GPS跟踪功能
- ❌ 配送员管理系统
- ❌ 高级路径优化算法

### 技术边界
**必须遵循的约束**:
- 使用Spring Boot 3.2.3框架
- 数据库连接池使用HikariCP
- API文档使用Swagger
- 日志系统集成现有双轨制日志
- JWT鉴权通过Gateway

### 性能边界
**性能指标**:
- 路径规划API响应时间 ≤ 2秒
- 配送单查询响应时间 ≤ 500ms
- 支持并发批量发货 ≤ 10个
- 单次路径规划最大支持30个配送点

---

## ✅ 验收标准

### 功能验收标准

#### 1. **配送单管理功能**
- [x] 创建配送单：输入dispatch_group和路径规划结果
- [x] 查询配送单：支持按ID、团长ID、状态查询
- [x] 更新配送状态：支持状态流转（配送中→已送达→已收货）
- [x] 批量操作：支持批量创建和状态更新

#### 2. **路径规划功能**
- [x] 高德API集成：成功调用路径规划API
- [x] 多点优化：支持最短时间和最短距离策略
- [x] 数据格式：返回标准的路径数据格式
- [x] 错误处理：API调用失败时的降级处理

#### 3. **业务流程验收**
- [x] 管理端批量发货：完整的发货→生成配送路径流程
- [x] 团长配送监控：查看配送任务和路径信息
- [x] 状态同步：配送状态与订单状态正确同步
- [x] 地图数据：提供前端地图展示所需的数据

### 技术验收标准

#### 1. **代码质量标准**
- 代码规范：遵循现有项目的代码规范
- 异常处理：完整的异常处理和日志记录
- 单元测试：核心业务逻辑测试覆盖率≥80%
- 接口文档：完整的Swagger API文档

#### 2. **集成验收标准**
- 服务注册：成功注册到Consul
- Gateway集成：通过网关正常访问
- Feign调用：与OrderService、UserService正常通信
- 数据库：正确的数据持久化和查询

#### 3. **安全验收标准**
- API密钥：高德API密钥正确配置在.env文件
- 权限控制：团长只能查看自己负责的配送任务
- 数据脱敏：日志中敏感信息正确脱敏

---

## ⚠️ 风险评估与缓解

### 技术风险

#### 1. **高德API依赖风险**
**风险描述**: 高德API服务不可用或调用超限
**影响程度**: 高 - 影响核心路径规划功能
**缓解措施**:
- 实现API调用失败的降级方案（返回直线距离排序）
- 配置API调用重试机制
- 监控API调用次数，预警接近限额

#### 2. **数据一致性风险**
**风险描述**: 配送状态与订单状态不同步
**影响程度**: 中 - 影响业务数据准确性
**缓解措施**:
- 实现事务性状态更新
- 添加状态校验和自动修复机制
- 记录详细的状态变更日志

#### 3. **性能风险**
**风险描述**: 大批量订单路径规划性能问题
**影响程度**: 中 - 影响用户体验
**缓解措施**:
- 限制单次路径规划的配送点数量（≤30个）
- 实现异步路径规划，避免阻塞用户操作
- 添加路径结果缓存机制

### 业务风险

#### 1. **仓库地址配置风险**
**风险描述**: 仓库地址配置错误导致路径规划不准确
**影响程度**: 高 - 影响所有配送路径
**缓解措施**:
- 提供仓库地址配置验证功能
- 支持多仓库配置和切换
- 添加地址准确性校验

#### 2. **业务流程风险**
**风险描述**: 批量发货逻辑与现有业务流程冲突
**影响程度**: 中 - 可能需要调整其他模块
**缓解措施**:
- 与相关服务保持接口兼容性
- 实现渐进式部署，支持新旧流程并存
- 提供回滚机制

---

## 📅 实施计划

### 开发里程碑

#### **Phase 1: 基础设施搭建**（0.5天）
- [x] 创建DeliveryService模块结构
- [ ] 配置高德API集成环境
- [ ] 创建数据库表和实体类
- [ ] 配置Feign客户端

#### **Phase 2: 核心功能开发**（1天）
- [ ] 实现配送单管理功能
- [ ] 集成高德路径规划API
- [ ] 实现批量发货处理逻辑
- [ ] 开发配送监控接口

#### **Phase 3: 业务集成**（0.5天）
- [ ] 与AdminService集成批量发货
- [ ] 与OrderService集成状态同步
- [ ] 与UserService集成地址获取
- [ ] Gateway和Consul注册

#### **Phase 4: 测试和优化**（0.5-1天）
- [ ] 单元测试和集成测试
- [ ] 性能测试和API压力测试
- [ ] 错误处理和边界情况测试
- [ ] 文档完善和API文档生成

### 关键节点交付物
- **Day 1**: 基础框架搭建完成，高德API集成测试通过
- **Day 2**: 核心功能开发完成，业务流程可演示
- **Day 3**: 完整集成测试通过，功能达到生产就绪状态

---

## 🔧 配置清单

### 环境配置要求

#### 1. **高德地图API配置**
```properties
# .env文件配置
GAODE_API_KEY=your_gaode_api_key_here
GAODE_API_URL=https://restapi.amap.com/v3/direction/driving

# application.yml配置
gaode:
  api:
    key: ${GAODE_API_KEY}
    url: ${GAODE_API_URL}
    timeout: 5000
    retry-count: 3
```

#### 2. **仓库地址配置**
```properties
# 默认仓库配置
warehouse:
  default:
    name: "总仓库"
    address: "示例地址"
    longitude: 116.397128
    latitude: 39.916527
```

#### 3. **数据库连接配置**
```properties
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/delivery_service_db
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 依赖项清单
- 高德地图API账号和密钥
- MySQL数据库（delivery_service_db）
- Consul服务注册中心
- OrderService、UserService、AdminService正常运行

---

**创建日期**: 2025-11-13  
**确认状态**: ✅ 需求共识已达成  
**技术方案**: ✅ 高德API集成方案确定  
**验收标准**: ✅ 明确且可测试  
**下一步**: 进入Architecture阶段，设计详细技术架构
