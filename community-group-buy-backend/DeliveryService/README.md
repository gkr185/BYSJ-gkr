# DeliveryService - 智能配送路径规划服务

## 📋 服务概述

DeliveryService是社区团购系统的配送管理微服务，提供智能路径规划、配送单管理、批量发货等核心功能。

### 🌟 核心特性

- **双引擎路径规划**: Dijkstra算法 + 高德地图API智能切换
- **批量发货处理**: 支持管理端批量发货操作
- **配送状态管理**: 完整的配送状态流转控制
- **地图可视化**: 提供前端地图展示所需的数据
- **智能降级**: API失败时自动降级到本地算法

## 🏗️ 技术架构

### 微服务信息
- **服务名称**: delivery-service
- **端口**: 8067
- **数据库**: delivery_service_db
- **注册中心**: Consul

### 技术栈
- **框架**: Spring Boot 3.2.3
- **数据访问**: Spring Data JPA
- **服务调用**: OpenFeign
- **API文档**: Swagger/OpenAPI 3
- **外部API**: 高德地图路径规划API

### 数据库表
- **delivery**: 配送单主表
- **warehouse_config**: 仓库配置表

## 🚀 快速开始

### 1. 环境准备
```bash
# Java 17+
java -version

# MySQL 8.0+
mysql --version

# Consul (服务注册中心)
consul --version
```

### 2. 数据库初始化
```sql
-- 创建数据库
CREATE DATABASE delivery_service_db;

-- 执行初始化脚本
source sql/delivery_service_db.sql;

-- 执行更新脚本（如果需要）
source sql/delivery_service_db_update.sql;
```

### 3. 配置文件
```bash
# 复制环境配置模板
cp .env.example .env

# 编辑配置文件，填写真实的API密钥
vim .env
```

### 4. 启动服务
```bash
# 开发环境
mvn spring-boot:run

# 生产环境
java -jar target/DeliveryService-0.0.1-SNAPSHOT.jar
```

### 5. 验证服务
```bash
# 健康检查
curl http://localhost:8067/api/delivery/monitor/health

# API文档
open http://localhost:8067/swagger-ui.html
```

## 📊 核心功能

### 1. 双引擎路径规划

#### Dijkstra算法引擎
- 基于图论的最短路径算法
- 纯本地计算，无外部依赖
- 适用于降级场景

#### 高德地图API引擎
- 实时路况信息
- 专业的路径优化
- 多种策略支持（最短时间/最短距离/避开拥堵）

#### 智能切换机制
```java
// 优先使用高德API
if (gaodeApiAvailable) {
    result = gaodeRouteService.calculateRoute(request);
    if (!result.success && enableFallback) {
        result = dijkstraService.calculateRoute(request);
    }
} else if (enableFallback) {
    result = dijkstraService.calculateRoute(request);
}
```

### 2. 批量发货流程
```
管理员选择订单 → 生成分单组 → 批量更新状态 → 创建配送单 → 生成路径 → 返回结果
```

### 3. 配送状态管理
```
待分配(0) → 配送中(1) → 已完成(2)
```

## 🔌 API接口

### 配送单管理
```http
POST   /api/delivery                    # 创建配送单
GET    /api/delivery/{id}               # 查询配送单
PUT    /api/delivery/{id}/status        # 更新状态
DELETE /api/delivery/{id}               # 删除配送单
```

### 路径规划
```http
POST   /api/delivery/route/plan         # 规划路径
GET    /api/delivery/route/status       # 算法状态
POST   /api/delivery/route/test/{alg}   # 测试算法
```

### 批量发货
```http
POST   /api/delivery/batch/ship         # 批量发货
POST   /api/delivery/batch/retry        # 重新发货
POST   /api/delivery/batch/cancel       # 取消发货
```

### 仓库管理
```http
GET    /api/delivery/warehouse/default  # 默认仓库
POST   /api/delivery/warehouse          # 创建仓库
PUT    /api/delivery/warehouse/{id}     # 更新仓库
```

### 监控接口
```http
GET    /api/delivery/monitor/health     # 健康检查
GET    /api/delivery/monitor/status     # 状态检查
GET    /api/delivery/monitor/version    # 版本信息
```

## ⚙️ 配置说明

### application.yml
```yaml
# 高德地图API配置
gaode:
  api:
    key: ${GAODE_API_KEY:your_api_key_here}
    base-url: https://restapi.amap.com/v3
    timeout: 5000
    retry-count: 3

# 配送服务配置
delivery:
  route:
    max-waypoints: 30
    default-strategy: shortest-time
    enable-dijkstra-fallback: true

# 仓库配置
warehouse:
  default:
    name: "中心仓库"
    longitude: 116.397128
    latitude: 39.916527
```

### 环境变量 (.env)
```bash
GAODE_API_KEY=your_real_api_key_here
```

## 🔗 服务集成

### Feign客户端

#### OrderService集成
```java
@FeignClient(name = "order-service")
public interface OrderServiceClient {
    // 获取分单组订单
    Result<List<OrderInfoDTO>> getOrdersByDispatchGroup(String dispatchGroup);
    
    // 批量更新订单状态
    Result<BatchUpdateResult> batchUpdateToShipping(BatchShipUpdateRequest request);
}
```

#### UserService集成
```java
@FeignClient(name = "user-service")
public interface UserServiceClient {
    // 获取地址坐标信息
    Result<AddressWithCoordinatesDTO> getAddressWithCoordinates(Long addressId);
}
```

### 降级处理
所有Feign调用都配置了降级处理，确保服务的高可用性。

## 🧪 测试

### 单元测试
```bash
mvn test
```

### 集成测试
```bash
# 测试Dijkstra算法
curl -X POST http://localhost:8067/api/delivery/route/test/dijkstra

# 测试高德API（需要配置API Key）
curl -X POST http://localhost:8067/api/delivery/route/test/gaode
```

### 性能测试
- 10个配送点：≤20ms
- 30个配送点：≤100ms
- API响应时间：≤500ms

## 📈 监控

### 健康检查
```bash
curl http://localhost:8067/api/delivery/monitor/health
```

### 服务状态
```bash
curl http://localhost:8067/api/delivery/monitor/status
```

### 关键指标
- 路径规划成功率
- 算法使用分布
- 配送单处理量
- API响应时间

## 🚨 故障排除

### 常见问题

#### 1. 高德API调用失败
**症状**: 路径规划失败，日志显示API错误
**解决方案**: 
- 检查API Key配置
- 确认网络连接
- 验证API调用次数限额

#### 2. 服务注册失败
**症状**: Consul中看不到服务
**解决方案**:
- 检查Consul连接配置
- 确认端口是否被占用
- 查看应用日志

#### 3. 数据库连接失败
**症状**: 应用启动时数据库连接错误
**解决方案**:
- 检查数据库连接参数
- 确认数据库权限
- 验证表结构是否正确

### 日志分析
```bash
# 查看应用日志
tail -f logs/delivery-service.log

# 查看错误日志
grep ERROR logs/delivery-service.log
```

## 📚 开发指南

### 添加新的路径规划算法
1. 实现`RouteAlgorithm`接口
2. 在`RouteService`中注册新算法
3. 添加配置选项
4. 编写单元测试

### 扩展配送状态
1. 更新`DeliveryStatus`枚举
2. 修改状态流转逻辑
3. 更新数据库脚本
4. 同步前端状态显示

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

---

**项目作者**: 耿康瑞 (20221204229)  
**创建日期**: 2025-11-13  
**版本**: 1.0.0
