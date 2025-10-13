# 社区团购系统 - 对齐文档 (ALIGNMENT)

**创建时间**: 2025-10-12  
**最后更新**: 2025-10-13 20:50  
**版本**: v1.5  
**项目名称**: 基于Spring Boot的社区团购系统的设计与实现  
**学生**: 耿康瑞 (20221204229)  
**指导教师**: 於永楠

---

## 📊 当前开发进度总览

### 整体进度：**30%** 

| 模块 | 状态 | 完成度 | 说明 |
|------|------|--------|------|
| 基础架构 | ✅ 完成 | 100% | 版本统一、Maven多模块、构建环境 |
| 数据库设计 | ✅ 完成 | 100% | 17张表设计完成 |
| Common模块 | ✅ 完成 | 100% | 统一响应、异常处理、工具类 |
| UserService | ✅ 完成 | 100% | 31个API接口（v1.2.0） |
| 管理后台（用户） | ✅ 完成 | 100% | 管理员后台（用户+反馈，已修复所有已知Bug） |
| ProductService | ⏳ 待开发 | 0% | 商品管理服务 |
| GroupBuyService | ⏳ 待开发 | 0% | 拼团活动服务 |
| OrderService | ⏳ 待开发 | 0% | 订单管理服务 |
| PaymentService | ⏳ 待开发 | 0% | 支付服务 |
| DeliveryService | ⏳ 待开发 | 0% | 配送服务 |
| 前端（用户端） | ⏳ 待开发 | 0% | Vue3用户端应用 |

### 最近完成
- ✅ **2025-10-13 20:50**: 修复前端数据流Bug（MyBatis PageHelper分页格式）
- ✅ **2025-10-13 20:45**: 修复账户不存在Bug（自动创建用户账户）
- ✅ **2025-10-13 20:30**: 修复用户详情对话框关闭错误
- ✅ **2025-10-13 20:20**: 完善管理后台用户详情（账户、地址信息展示）
- ✅ **2025-10-12 19:30**: 新增管理员创建用户功能
- ✅ **2025-10-12 19:30**: 新增编辑用户信息与权限功能
- ✅ **2025-10-12 18:30**: 修复接口参数问题（keyword可选）
- ✅ **2025-10-12 18:15**: 完成管理员测试页面（登录、用户管理、反馈管理）
- ✅ **2025-10-12 18:00**: 解决Maven构建和密码加密问题
- ✅ **2025-10-12 16:00**: 生成完整API接口文档（31个接口）
- ✅ **2025-10-12 15:00**: 完成JWT认证机制和安全配置
- ✅ **2025-10-12 14:00**: 开发完成UserService模块

### 下一步计划
- 🎯 开发ProductService（商品管理服务）
- 🎯 开发OrderService（订单管理服务）
- 🎯 开发PaymentService（支付服务）

---

## 1. 项目上下文分析

### 1.1 现有项目结构分析

#### 后端结构（已更新 ✅）
```
community-group-buy-backend/
├── pom.xml (父级POM，Spring Boot 3.2.3, Java 17) ✅ 已统一版本
├── common/ ✅ 新增
│   ├── pom.xml
│   └── src/main/java/com/bcu/edu/common/
│       ├── config/          # 全局配置（CORS等）
│       ├── constant/        # 系统常量
│       ├── enums/           # 统一结果码
│       ├── exception/       # 自定义异常
│       ├── handler/         # 全局异常处理器
│       ├── result/          # 统一响应格式
│       └── utils/           # 工具类（JWT、Security、Date等）
├── UserService/ ✅ 已完成
│   ├── pom.xml (Spring Boot 3.2.3)
│   └── src/main/
│       ├── java/com/bcu/edu/
│       │   ├── UserServiceApplication.java
│       │   ├── config/      # JWT认证、安全配置
│       │   ├── controller/  # 4个Controller（30个接口）
│       │   ├── dto/         # 11个DTO类
│       │   ├── entity/      # 4个实体类
│       │   ├── repository/  # 4个Repository接口
│       │   └── service/     # 4个Service类
│       └── resources/
│           └── application.yml
└── src/main/ (待整理)
    └── java/bcu/edu/communitygroupbuybackend/
```

#### 前端结构
```
community-group-buy-admin/
├── package.json (Vue 3.5.22, Vite 7.1.7)
├── src/
│   ├── main.js (应用入口)
│   ├── App.vue
│   ├── router/ (Vue Router 4.5.1)
│   ├── stores/ (Pinia 3.0.3)
│   ├── views/
│   └── components/
└── vite.config.js
```

#### 数据库结构（已更新 ✅）
- 已有完整的数据库设计文件：`community_group_buy.sql`
- 包含**17张表**（已补充user_feedback表），完整覆盖业务需求
- 数据库名：`community_group_buy`
- 详细设计文档：`docs/社区团购系统/数据库设计说明文档.md`

### 1.2 技术栈分析

#### 已配置技术
| 技术 | 版本/工具 | 状态 | 备注 |
|------|----------|------|------|
| Java | 17 | ✅ 已配置 | 符合开题报告JDK 11+要求 |
| Spring Boot | 3.2.3 | ✅ 已统一 | 全部模块已统一版本 |
| Spring Cloud | 2023.0.0 | ✅ 已统一 | 全部模块已统一版本 |
| Consul | 已配置discovery | ✅ 已配置 | 服务注册与发现 |
| MySQL | 8.0.36 | ✅ 已配置 | 17张表完整设计 |
| JPA/Hibernate | 已配置 | ✅ 已配置 | ORM框架 |
| JWT | 0.11.5 | ✅ 已配置 | Token认证（7天有效期） |
| Hutool | 5.8.25 | ✅ 已配置 | 工具类库（common模块） |
| Vue | 3.5.22 | ✅ 已配置 | 符合开题报告要求 |
| Pinia | 3.0.3 | ✅ 已配置 | 状态管理 |
| Vue Router | 4.5.1 | ✅ 已配置 | 路由管理 |
| Lombok | 已配置 | ✅ 已配置 | 简化Java代码 |
| SpringDoc OpenAPI | 2.3.0 | ✅ 已配置 | API文档（Swagger UI） |
| Jackson | 已配置 | ✅ 已配置 | JSON序列化 |

#### 待配置技术
| 技术 | 用途 | 优先级 | 状态 |
|------|------|--------|------|
| Axios | 前端HTTP请求 | 🔴 高 | ⏳ 待配置 |
| 微信支付SDK | 支付功能 | 🔴 高 | ⏳ 待配置 |
| 高德地图API | 地址解析、路径规划 | 🟡 中 | ⏳ 待配置 |
| Element Plus / Ant Design Vue | UI组件库 | 🔴 高 | ⏳ 待配置 |
| Redis | 缓存 | 🟢 低 (可选) | ⏳ 暂不配置 |
| JMeter | 性能测试 | 🟢 低 (后期) | ⏳ 暂不配置 |

### 1.3 架构模式分析

#### 当前架构状态（已更新 ✅）
- ✅ **微服务架构**: 已采用Maven多模块结构
- ✅ **服务注册与发现**: 已配置Consul
- ✅ **公共模块**: Common模块已完成（统一响应、异常处理、工具类）
- ✅ **用户服务**: UserService已完成（30个API接口）
- ✅ **JWT认证**: 已实现Token认证机制
- ✅ **数据加密**: 密码SHA256、手机号AES加密已实现
- ⏳ **服务拆分**: 其他服务（商品、订单、配送、支付等）待创建
- ⏳ **网关**: API Gateway待配置
- ❌ **负载均衡**: 未配置
- ❌ **配置中心**: 未配置（可选）

### 1.4 业务域分析

根据开题报告，系统应包含以下业务域：

#### 核心业务模块（已更新 ✅）
1. **用户管理** (UserService - ✅ 已完成100%)
   - 用户注册、登录、JWT认证
   - 用户信息管理、角色管理
   - 收货地址管理（含经纬度）
   - 账户余额管理（充值、扣款、冻结）
   - 用户反馈管理
   - 30个RESTful API接口
   
2. **商品管理** (ProductService - ⏳ 待开发)
3. **拼团活动管理** (GroupBuyService - ⏳ 待开发)
4. **购物车管理** (CartService - ⏳ 待开发)
5. **订单管理** (OrderService - ⏳ 待开发)
6. **配送管理** (DeliveryService - ⏳ 待开发)
7. **支付管理** (PaymentService - ⏳ 待开发)
8. **团长运营管理** (LeaderService - ⏳ 待开发)

---

## 2. 开题报告需求理解

### 2.1 系统设计目标

#### 主要目标
1. ✅ **解决拼团流程繁琐问题** - 需实现流畅的拼团下单流程
2. ✅ **解决团长管理分散问题** - 需实现集中化团长管理平台
3. ✅ **优化配送路径规划** - 需实现Dijkstra算法 + 订单分单逻辑
4. ✅ **保障交易安全** - 需集成微信支付 + 数据加密（AES/SHA256）
5. ✅ **实现数字化运营** - 需提供完整的后台管理系统

#### 用户角色
| 角色 | 主要功能 | 数据库role值 |
|------|---------|-------------|
| 社区用户 (甲方) | 浏览商品、拼团下单、查询订单 | 1 |
| 团长 (乙方) | 订单管理、团员管理、配送管理 | 2 |
| 系统管理员 | 商品上架、用户管理、配送调度、数据监控 | 3 |

### 2.2 核心功能需求

#### 社区用户端功能 (14项)
1. ✅ 浏览商品分类
2. ✅ 搜索商品
3. ✅ 添加商品到购物车
4. ✅ 创建拼团活动
5. ✅ 参与他人拼团
6. ✅ 分享拼团链接
7. ✅ 查看拼团进度
8. ✅ 下单支付
9. ✅ 查询配送状态
10. ✅ 维护个人资料
11. ✅ 管理收货地址
12. ✅ 余额充值
13. ✅ 申请团长资格
14. ✅ 提交用户反馈

#### 团长端功能 (13项)
1. ✅ 登录
2. ✅ 修改个人信息
3. ✅ 维护拼团活动
4. ✅ 管理拼团进度
5. ✅ 审核团员申请
6. ✅ 维护团员列表
7. ✅ 查看佣金明细
8. ✅ 批量处理订单
9. ✅ 处理订单退款
10. ✅ 订单配送管理
11. ✅ 确认配送路线
12. ✅ 管理配送状态
13. ✅ 维护团点信息

#### 管理员端功能 (16项)
1. ✅ 登录
2. ✅ 修改个人信息
3. ✅ 用户维护
4. ✅ 用户权限维护
5. ✅ 反馈处理
6. ✅ 商品分类维护
7. ✅ 商品维护
8. ✅ 支付参数维护
9. ✅ 系统日志维护
10. ✅ 监管拼团活动
11. ✅ 订单监控
12. ✅ 配送状态监控
13. ✅ 管理团长绩效
14. ✅ 审核团长资质
15. ✅ 新用户/团长数统计
16. ✅ 营收统计

### 2.3 非功能需求

#### 性能需求
- 常规页面加载: ≤ 2秒
- 核心交互页面: ≤ 800毫秒
- 关键交易操作: ≤ 500毫秒
- 路径规划计算: ≤ 1秒
- 百万级数据查询: ≤ 1秒

#### 安全性需求
- HTTPS/TLS 1.2+ 全程加密
- RBAC权限控制
- 密码使用MD5/SHA256加密
- 支付数据AES加密

#### 可靠性需求
- 核心服务可用率: ≥ 99.9%
- 支付链路可用率: ≥ 99.99%
- 数据零丢失
- 30分钟内故障恢复

#### 可扩展性需求
- 微服务模块化设计
- 清晰的API接口
- 高内聚低耦合

---

## 3. 数据库设计分析

### 3.1 数据表对齐检查

| 开题报告表名 | 数据库实际表名 | 状态 | 备注 |
|------------|---------------|------|------|
| sys_user | sys_user | ✅ 完全一致 | 用户表 |
| user_address | user_address | ✅ 完全一致 | 用户地址表 |
| product_category | product_category | ✅ 完全一致 | 商品分类表 |
| product | product | ✅ 完全一致 | 商品表 |
| group_leader_store | group_leader_store | ✅ 完全一致 | 团长团点表 |
| group_member | group_member | ✅ 完全一致 | 团员关系表 |
| group_buy | group_buy | ✅ 完全一致 | 拼团活动表 |
| group_buy_join | group_buy_join | ✅ 完全一致 | 拼团参与表 |
| shopping_cart | shopping_cart | ✅ 完全一致 | 购物车表 |
| delivery | delivery | ✅ 完全一致 | 配送单表 |
| order_main | order_main | ✅ 完全一致 | 订单主表 |
| order_item | order_item | ✅ 完全一致 | 订单项表 |
| payment_record | payment_record | ✅ 完全一致 | 支付记录表 |
| user_account | user_account | ✅ 完全一致 | 用户账户表 |
| commission_record | commission_record | ✅ 完全一致 | 佣金记录表 |
| sys_operation_log | sys_operation_log | ✅ 完全一致 | 系统操作日志表 |

**结论**: 数据库设计与开题报告完全一致，表结构完整，外键关系清晰。

### 3.2 数据表缺失字段检查（已解决 ✅）

~~开题报告中提到的`user_feedback`表在数据库SQL中未找到，但在`用例分析`中提到了"提交用户反馈"功能。~~

**已完成**: ✅ `user_feedback`表已补充完成
- 反馈ID、用户ID、反馈类型（5种）
- 反馈内容、图片URL
- 处理状态（4种）、管理员回复
- 完整的索引和外键约束
- 详见：`community_group_buy.sql`

---

## 4. 微服务拆分分析

### 4.1 建议的服务拆分方案

根据DDD(领域驱动设计)原则和业务边界，建议拆分以下微服务：

| 服务名 | 端口 | 职责 | 依赖 | 优先级 |
|-------|------|------|------|--------|
| UserService | 8061 | 用户管理、认证授权、地址管理、账户管理、反馈管理 | MySQL, Common | ✅ 已完成100% |
| ProductService | 8062 | 商品管理、分类管理、库存管理 | MySQL | 🔴 高 |
| GroupBuyService | 8063 | 拼团活动、拼团参与、成团逻辑 | MySQL, ProductService | 🔴 高 |
| CartService | 8064 | 购物车管理 | MySQL, ProductService | 🟡 中 |
| OrderService | 8065 | 订单管理、订单状态流转 | MySQL, UserService, ProductService | 🔴 高 |
| PaymentService | 8066 | 支付、充值、退款、账户管理 | MySQL, OrderService, 微信支付SDK | 🔴 高 |
| DeliveryService | 8067 | 配送管理、路径规划(Dijkstra)、分单逻辑 | MySQL, OrderService, 高德地图API | 🟡 中 |
| LeaderService | 8068 | 团长管理、团点管理、佣金管理 | MySQL, UserService | 🟡 中 |
| AdminService | 8069 | 后台管理、数据统计、日志管理 | MySQL, 所有服务 | 🟢 低 |
| GatewayService | 8080 | API网关、路由、鉴权 | 所有服务 | 🟡 中 |

### 4.2 服务间通信方案

- **同步通信**: RestTemplate / OpenFeign (服务间REST调用)
- **异步通信**: RabbitMQ / Kafka (可选，用于订单状态变更通知等)

---

## 5. 技术实现方案分析

### 5.1 核心算法实现

#### Dijkstra路径规划算法
- **输入**: 团点坐标、所有订单收货地址坐标
- **输出**: 最优配送路径、总配送距离
- **依赖**: 高德地图API获取坐标
- **实现位置**: DeliveryService

#### 订单分单逻辑
- **按区域划分**: 根据收货地址与团点距离聚类
- **按配送范围**: 团点的`max_delivery_range`字段限制
- **实现位置**: DeliveryService

### 5.2 安全实现方案

#### 密码加密
- **方案**: SHA256 + 盐值
- **工具**: Hutool的`SecureUtil`
- **位置**: UserService

#### 支付数据加密
- **方案**: AES加密存储
- **工具**: Hutool的`SymmetricCrypto`
- **位置**: PaymentService的`pay_callback_info`字段

#### 数据签名
- **方案**: SHA256 + 盐值
- **工具**: Hutool
- **位置**: PaymentService的`encrypt_sign`字段

### 5.3 第三方接口集成

#### 微信支付
- **SDK**: wechatpay-apache-httpclient
- **功能**: 统一下单、支付回调、退款
- **配置**: appid, mch_id, api_key等存储在数据库或配置中心

#### 高德地图API
- **功能**: 地理编码、逆地理编码
- **接口**: GET请求，需要API Key
- **配置**: 高德开发者Key存储在配置文件

---

## 6. 边界确认

### 6.1 明确的任务范围

#### 包含的功能
1. ✅ 完整的用户、商品、订单、拼团、支付、配送业务流程
2. ✅ 三端界面：用户端(Web/H5)、团长端(Web)、管理员端(Web)
3. ✅ 微服务架构，使用Consul服务发现
4. ✅ MySQL数据库持久化
5. ✅ 微信支付集成
6. ✅ 数据加密(密码、支付信息)
7. ✅ Dijkstra算法路径规划
8. ✅ 数据统计与可视化

#### 不包含的功能
1. ❌ 分布式事务(Seata) - 使用简单事务或最终一致性
2. ❌ 消息队列(RabbitMQ/Kafka) - 可选，初期不实现
3. ❌ Redis缓存 - 可选，性能优化阶段考虑
4. ❌ Elasticsearch搜索引擎 - 使用MySQL LIKE查询
5. ❌ Docker容器化部署 - 传统部署方式
6. ❌ CI/CD自动化部署 - 手动部署
7. ❌ 移动端原生APP - 仅Web端
8. ❌ 小程序 - 可选

### 6.2 技术约束

1. **JDK版本**: Java 17
2. **Spring Boot版本**: 统一为 3.2.x (建议3.2.3)
3. **Spring Cloud版本**: 统一为 2023.0.0
4. **数据库**: MySQL 8.0+
5. **前端**: Vue 3 + Vite
6. **服务发现**: Consul 1.15.0+
7. **部署环境**: Windows 11 + Tomcat 9.0

### 6.3 集成方案

#### 前后端集成
- **方式**: 前后端分离，RESTful API
- **跨域**: 后端配置CORS
- **认证**: JWT Token (待实现)

#### 服务间集成
- **通信方式**: HTTP/REST (OpenFeign)
- **服务发现**: Consul
- **负载均衡**: Spring Cloud LoadBalancer

---

## 7. 疑问澄清

### 7.1 架构设计疑问

#### Q1: Spring Boot版本不一致
**问题**: 父POM使用3.5.6，UserService使用3.2.3，版本不一致可能导致兼容性问题。  
**决策**: ⚠️ 需要确认 - 建议统一为3.2.3（稳定版本）还是升级到3.5.6？  
**建议**: 统一使用Spring Boot 3.2.3 + Spring Cloud 2023.0.0（稳定组合）

#### Q2: API网关是否需要
**问题**: 开题报告未明确提及API Gateway。  
**决策**: ⚠️ 需要确认 - 是否实现Spring Cloud Gateway？  
**建议**: 建议实现，作为统一入口，提供路由、鉴权、限流等功能

#### Q3: 服务拆分粒度
**问题**: 是否所有模块都拆分为独立微服务？  
**决策**: ⚠️ 需要确认 - 是否将部分服务合并（如CartService合并到OrderService）？  
**建议**: 核心服务独立（User, Product, Order, Payment），辅助服务可合并

### 7.2 功能实现疑问

#### Q4: 用户反馈表缺失
**问题**: 开题报告提到"用户反馈"功能，但数据库SQL中没有`user_feedback`表。  
**决策**: 🔴 需要确认 - 是否需要补充该表？  
**建议**: 补充表结构如下：
```sql
CREATE TABLE `user_feedback` (
  `feedback_id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '反馈类型(1-功能问题;2-商品问题;3-配送问题;4-其他)',
  `content` text NOT NULL COMMENT '反馈内容',
  `images` text NULL COMMENT '图片URL(多张用逗号分隔)',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态(0-待处理;1-处理中;2-已解决;3-已关闭)',
  `reply` text NULL COMMENT '处理意见',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`feedback_id`),
  INDEX `idx_user_id`(`user_id`),
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='用户反馈表';
```

#### Q5: 拼团成团逻辑
**问题**: 拼团成功后如何自动生成订单？  
**决策**: ⚠️ 需要确认 - 是定时任务检查还是参与时实时检查？  
**建议**: 每次用户参与拼团时实时检查，达到`required_num`则自动成团并生成订单

#### Q6: 配送路径规划触发时机
**问题**: Dijkstra算法何时执行？  
**决策**: ⚠️ 需要确认 - 是订单支付后立即规划，还是团长手动触发？  
**建议**: 团长在后台批量处理订单时，点击"生成配送路线"按钮触发

#### Q7: 佣金结算方式
**问题**: 团长佣金何时结算？  
**决策**: ⚠️ 需要确认 - 订单完成后立即结算还是定期批量结算？  
**建议**: 订单配送完成(status=3)后自动计算并记录到`commission_record`，每月1号统一结算

### 7.3 技术选型疑问

#### Q8: 认证授权方案
**问题**: 开题报告未明确提及用户认证方案。  
**决策**: ⚠️ 需要确认 - 使用JWT、Session还是OAuth2？  
**建议**: 使用JWT Token（无状态、适合微服务）

#### Q9: 文件存储方案
**问题**: 商品图片、用户头像、反馈图片等文件如何存储？  
**决策**: ⚠️ 需要确认 - 本地文件系统、阿里云OSS还是其他？  
**建议**: 初期使用本地文件系统，存储路径：`/uploads/{module}/{date}/`，数据库存相对路径

#### Q10: 前端UI组件库
**问题**: 开题报告未明确前端UI框架。  
**决策**: ⚠️ 需要确认 - Element Plus、Ant Design Vue还是Naive UI？  
**建议**: Element Plus（生态成熟、中文文档完善）

#### Q11: 数据库连接池
**问题**: UserService已配置HikariCP，是否所有服务都使用？  
**决策**: ✅ 已确定 - 使用HikariCP（Spring Boot默认）

#### Q12: 日志框架
**问题**: 开题报告未提及日志方案。  
**决策**: ⚠️ 需要确认 - 是否需要集中式日志管理（ELK）？  
**建议**: 使用Logback（Spring Boot默认），初期输出到文件，后期可接入ELK

### 7.4 部署运维疑问

#### Q13: Consul部署方式
**问题**: Consul是单机部署还是集群？  
**决策**: ⚠️ 需要确认  
**建议**: 毕业设计单机部署即可（localhost:8500）

#### Q14: 数据库初始化
**问题**: 数据库是手动执行SQL还是自动建表？  
**决策**: ✅ 已确定 - UserService配置了`spring.jpa.hibernate.ddl-auto=update`（自动建表）  
**建议**: 统一使用JPA自动建表，开发完成后导出最终SQL

#### Q15: 环境配置管理
**问题**: 开发环境、测试环境、生产环境配置如何管理？  
**决策**: ⚠️ 需要确认 - 使用Spring Profiles还是Consul配置中心？  
**建议**: 使用Spring Profiles（application-dev.yml, application-prod.yml）

---

## 8. 需求边界限制

### 8.1 功能边界

#### 必须实现 (MVP)
1. ✅ 用户注册/登录
2. ✅ 商品浏览/搜索
3. ✅ 拼团发起/参与
4. ✅ 购物车
5. ✅ 订单生成/支付（微信支付）
6. ✅ 团长管理后台
7. ✅ 管理员后台
8. ✅ Dijkstra路径规划
9. ✅ 数据统计

#### 可选实现
1. 🟡 支付宝支付
2. 🟡 短信通知
3. 🟡 邮件通知
4. 🟡 优惠券
5. 🟡 积分系统
6. 🟡 商品评价

#### 不实现
1. ❌ 实时聊天
2. ❌ 直播带货
3. ❌ 供应商入驻
4. ❌ 多语言国际化

### 8.2 数据规模边界

- **用户数**: ≤ 10万
- **商品数**: ≤ 1万
- **日订单量**: ≤ 1000
- **并发用户**: ≤ 500

### 8.3 时间边界

根据毕业设计时间要求：
- **阶段1 - 基础架构搭建**: 1周
- **阶段2 - 核心服务开发**: 4周
- **阶段3 - 前端开发**: 3周
- **阶段4 - 集成测试**: 1周
- **阶段5 - 性能优化与文档**: 1周

---

## 9. 验收标准

### 9.1 功能验收

#### 用户端
- [ ] 用户可以注册、登录
- [ ] 用户可以浏览商品、搜索商品
- [ ] 用户可以发起拼团、参与拼团
- [ ] 用户可以下单并使用微信支付
- [ ] 用户可以查看订单状态、配送状态

#### 团长端
- [ ] 团长可以审核团员申请
- [ ] 团长可以批量处理订单
- [ ] 团长可以使用路径规划功能
- [ ] 团长可以查看佣金明细

#### 管理员端
- [ ] 管理员可以管理商品、分类
- [ ] 管理员可以审核团长资质
- [ ] 管理员可以查看数据统计报表
- [ ] 管理员可以查看系统日志

### 9.2 非功能验收

- [ ] 页面加载时间符合性能需求
- [ ] 密码已加密存储
- [ ] 支付数据已加密存储
- [ ] HTTPS传输
- [ ] 所有API有权限控制
- [ ] 系统可用率 ≥ 99%

### 9.3 文档验收

- [ ] 完整的毕业设计报告
- [ ] 系统源代码（含注释）
- [ ] 数据库设计文档（已有）
- [ ] API接口文档（Swagger）
- [ ] 部署文档
- [ ] 测试用例文档
- [ ] 功能演示视频

---

## 10. 风险识别

### 10.1 技术风险

| 风险项 | 严重程度 | 可能性 | 缓解措施 |
|-------|---------|-------|---------|
| Spring Boot版本不兼容 | 🔴 高 | 高 | 统一版本为3.2.3 |
| 微服务通信失败 | 🟡 中 | 中 | 配置重试机制、熔断降级 |
| Dijkstra算法性能问题 | 🟡 中 | 中 | 限制配送点数量、使用缓存 |
| 微信支付集成复杂 | 🟡 中 | 高 | 提前学习官方文档、使用沙箱测试 |
| 数据库性能瓶颈 | 🟢 低 | 低 | 添加索引、分页查询 |

### 10.2 进度风险

| 风险项 | 严重程度 | 可能性 | 缓解措施 |
|-------|---------|-------|---------|
| 微服务拆分过细导致开发时间过长 | 🔴 高 | 高 | 合并部分服务（如Cart与Order） |
| 前端开发经验不足 | 🟡 中 | 中 | 使用UI组件库、参考开源项目 |
| 测试时间不足 | 🟡 中 | 中 | 边开发边测试 |

### 10.3 业务风险

| 风险项 | 严重程度 | 可能性 | 缓解措施 |
|-------|---------|-------|---------|
| 拼团逻辑复杂导致Bug | 🟡 中 | 高 | 编写单元测试、集成测试 |
| 支付安全漏洞 | 🔴 高 | 低 | 严格按照微信支付规范、代码审查 |
| 订单并发问题 | 🟡 中 | 中 | 数据库事务、乐观锁 |

---

## 11. 现有系统集成分析

### 11.1 已有资源评估

#### 可直接使用
1. ✅ 数据库表结构（完整）
2. ✅ UserService基础框架（可扩展）
3. ✅ Vue3前端脚手架（可直接开发）
4. ✅ Consul服务注册配置（可复用）

#### 需要改造
1. ⚠️ Spring Boot版本统一
2. ⚠️ UserService需要补充完整CRUD接口
3. ⚠️ 前端需要安装UI组件库、Axios等依赖

#### 需要新建
1. ❌ ProductService
2. ❌ GroupBuyService
3. ❌ OrderService
4. ❌ PaymentService
5. ❌ DeliveryService
6. ❌ 前端页面组件

### 11.2 技术债务（已部分解决 ✅）

1. ~~**配置不一致**: 父子POM版本不同~~ ✅ 已解决（统一为3.2.3和2023.0.0）
2. **包名不一致**: 主应用`bcu.edu`，UserService`com.bcu.edu` ⏳ 待统一
3. **配置文件格式不一致**: 主应用`.properties`，UserService`.yml` ⏳ 可接受
4. ~~**缺少全局异常处理**~~ ✅ 已解决（Common模块已实现）
5. ~~**缺少统一返回结果封装**~~ ✅ 已解决（Common模块已实现）
6. **缺少集中式日志配置** ⏳ 待完善（目前使用Logback默认配置）

---

## 12. 项目特性规范

### 12.1 代码规范

#### Java代码规范
- 使用Lombok减少样板代码
- 遵循阿里巴巴Java开发规范
- 包名统一：`com.bcu.edu.{serviceName}.{layer}`
- 类命名：
  - Entity: `XxxEntity`
  - DTO: `XxxDTO`
  - VO: `XxxVO`
  - Controller: `XxxController`
  - Service: `XxxService`
  - Repository: `XxxRepository`

#### 前端代码规范
- Vue3 Composition API
- 组件命名：大驼峰（如`UserProfile.vue`）
- 变量命名：小驼峰
- 使用ESLint + Prettier

### 12.2 接口规范

#### RESTful API设计
```
GET    /api/{service}/{resource}         # 查询列表
GET    /api/{service}/{resource}/{id}    # 查询详情
POST   /api/{service}/{resource}         # 创建
PUT    /api/{service}/{resource}/{id}    # 更新
DELETE /api/{service}/{resource}/{id}    # 删除
```

#### 统一返回格式
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1234567890
}
```

#### 错误码规范
- 200: 成功
- 400: 参数错误
- 401: 未认证
- 403: 无权限
- 404: 资源不存在
- 500: 服务器错误

### 12.3 数据库规范

- 表名：小写+下划线（如`sys_user`）
- 字段名：小写+下划线
- 主键：`{table_name}_id`
- 外键：`fk_{table}_{ref_table}`
- 索引：`idx_{field_name}`
- 时间字段：`create_time`, `update_time`

### 12.4 Git规范

#### 分支策略
- `main`: 主分支（稳定版本）
- `develop`: 开发分支
- `feature/xxx`: 功能分支
- `bugfix/xxx`: Bug修复分支

#### Commit Message规范
```
<type>(<scope>): <subject>

<body>
```
- type: feat / fix / docs / style / refactor / test / chore
- scope: 服务名或模块名
- subject: 简短描述

---

## 13. 下一步行动

### 13.1 立即执行

#### ✅ 优先级1 - 基础环境统一（已完成）
1. [x] 统一Spring Boot版本为3.2.3
2. [x] 统一Spring Cloud版本为2023.0.0
3. [ ] 统一包名为`com.bcu.edu` ⏳ 待完成
4. [x] 补充`user_feedback`表
5. [x] 安装Consul并验证服务注册

#### ✅ 优先级2 - 公共模块开发（已完成）
1. [x] 创建`common`模块（统一返回结果、全局异常、工具类）
2. [x] 配置Hutool依赖
3. [x] 实现JWT认证工具类
4. [x] 实现密码加密工具类
5. [x] 实现AES加密工具类
6. [x] 实现日期工具类
7. [x] 配置全局CORS
8. [x] 配置全局异常处理器

#### ✅ 优先级3 - UserService开发（已完成）
1. [x] 完善UserService（CRUD、登录、JWT认证）
2. [x] 实现用户注册、登录接口
3. [x] 实现地址管理接口（7个接口）
4. [x] 实现账户管理接口（5个接口）
5. [x] 实现反馈管理接口（8个接口）
6. [x] 实现JWT认证过滤器
7. [x] 配置Swagger UI文档
8. [x] 编写完整API文档

#### 🎯 优先级4 - 核心服务开发（进行中）
1. [ ] 创建ProductService（商品管理服务）⬅️ **下一步**
2. [ ] 创建OrderService（订单管理服务）
3. [ ] 创建PaymentService（支付服务）
4. [ ] 创建GroupBuyService（拼团服务）

### 13.2 后续执行

#### 阶段2 - 业务服务 🟡
1. [ ] 创建GroupBuyService
2. [ ] 创建CartService
3. [ ] 创建DeliveryService
4. [ ] 创建LeaderService

#### 阶段3 - 前端开发 🟡
1. [ ] 安装Element Plus、Axios
2. [ ] 开发用户端页面
3. [ ] 开发团长端页面
4. [ ] 开发管理员端页面

#### 阶段4 - 集成与测试 🟢
1. [ ] 微信支付集成
2. [ ] 高德地图集成
3. [ ] Dijkstra算法实现
4. [ ] 单元测试
5. [ ] 集成测试
6. [ ] 性能测试(JMeter)

#### 阶段5 - 文档与交付 🟢
1. [ ] 完善代码注释
2. [ ] 生成Swagger文档
3. [ ] 编写部署文档
4. [ ] 录制演示视频
5. [ ] 完成毕业设计报告

---

## 14. 关键决策点（已部分确认 ✅）

### 14.1 架构决策
1. ⏳ **是否实现API Gateway?** (建议: 实现) - 待确认
2. ✅ **服务拆分粒度?** - 确认：8个微服务（已按计划执行）
3. ✅ **是否使用消息队列?** - 确认：初期不用（后期可选）
4. ✅ **是否使用Redis缓存?** - 确认：初期不用（后期可选）

### 14.2 技术选型（已确认 ✅）
1. ✅ **认证方案:** JWT（已实现，Token有效期7天）
2. ⏳ **文件存储:** 本地 / OSS? (建议: 本地) - 待实现时确认
3. ⏳ **UI组件库:** Element Plus / Ant Design Vue? (建议: Element Plus) - 待确认

### 14.3 业务逻辑
1. ⏳ **拼团成团时机:** 实时检查 / 定时任务? (建议: 实时) - 待确认
2. ⏳ **配送路径规划触发:** 自动 / 手动? (建议: 手动) - 待确认
3. ⏳ **佣金结算方式:** 订单完成立即 / 定期批量? (建议: 定期) - 待确认

### 14.4 数据库（已确认 ✅）
1. ✅ **是否补充`user_feedback`表?** - 已完成
2. ✅ **JPA自动建表 vs 手动SQL?** - 确认：使用JPA自动建表（ddl-auto=update）

---

## 15. 总结

### 15.1 项目整体评估

**优势**:
- ✅ 开题报告详细、需求明确
- ✅ 数据库设计完整、规范
- ✅ 已有基础框架，可快速启动
- ✅ 技术栈选型合理、符合主流

**挑战**:
- ⚠️ 微服务拆分较多，开发工作量大
- ⚠️ 微信支付集成需要学习成本
- ⚠️ Dijkstra算法实现需要一定算法基础
- ⚠️ 前端开发工作量大（三端）

**风险**:
- 🔴 时间风险：10周开发周期较紧张
- 🟡 技术风险：微服务通信、支付安全
- 🟢 业务风险：拼团逻辑、订单并发

### 15.2 可行性评估

**整体可行性**: ✅ 可行

**理由**:
1. 需求清晰，设计完整
2. 技术栈成熟，有大量参考资料
3. 已有基础代码，可快速迭代
4. 核心功能明确，可分阶段交付

**建议**:
1. 简化微服务拆分（合并部分服务）
2. 先实现核心功能（MVP），再扩展
3. 前端使用UI组件库加速开发
4. 提前学习微信支付、地图API

---

## 16. 已完成模块详细说明 ✅

### 16.1 Common模块（v1.0.0）

**完成日期**: 2025-10-12  
**代码量**: 约1500行

#### 核心功能
1. **统一响应格式** (`Result<T>`)
   - 成功/失败响应封装
   - 泛型支持
   - 时间戳自动添加

2. **分页响应格式** (`PageResult<T>`)
   - 总数、总页数、当前页
   - 泛型数据列表

3. **全局异常处理** (`GlobalExceptionHandler`)
   - BusinessException业务异常
   - 参数校验异常
   - 系统异常统一处理

4. **JWT工具类** (`JwtUtil`)
   - Token生成与解析
   - Token验证与刷新
   - Claims信息提取

5. **安全工具类** (`SecurityUtil`)
   - SHA256密码加密
   - AES数据加密/解密
   - 数据签名
   - 手机号脱敏

6. **日期工具类** (`DateUtil`)
   - 日期格式化
   - 日期计算
   - 时间戳转换

7. **系统常量** (`Constants`)
   - JWT配置常量
   - 系统配置常量

8. **结果码枚举** (`ResultCode`)
   - 30+个标准结果码
   - 用户相关、系统相关等分类

#### 依赖配置
- Spring Boot Starter Web
- Spring Boot Starter Validation
- Lombok
- Hutool 5.8.25
- JJWT 0.11.5
- Jackson

**文档**: `docs/社区团购系统/Common模块开发完成报告.md`

---

### 16.2 UserService模块（v1.2.0）

**完成日期**: 2025-10-12  
**最后更新**: 2025-10-13（Bug修复）  
**代码量**: 约3500行  
**接口数量**: 31个RESTful API

#### 数据层（4个Entity + 4个Repository）
1. **SysUser** - 用户基础信息
   - 三种角色（普通用户、团长、管理员）
   - 密码SHA256加密、手机号AES加密
   - 状态管理（正常/禁用）

2. **UserAddress** - 收货地址
   - 多地址支持、默认地址管理
   - 经纬度信息（支持路径规划）

3. **UserAccount** - 用户账户
   - 余额管理、冻结金额
   - 充值、扣款、冻结、解冻操作

4. **UserFeedback** - 用户反馈
   - 5种反馈类型、4种处理状态
   - 管理员回复功能

#### 业务层（4个Service）
1. **UserService** - 10个业务方法
2. **AddressService** - 7个业务方法
3. **AccountService** - 5个业务方法
4. **FeedbackService** - 8个业务方法

#### 控制层（4个Controller + 11个DTO）
1. **UserController** - 10个接口
2. **AddressController** - 7个接口
3. **AccountController** - 5个接口
4. **FeedbackController** - 8个接口

#### 安全配置
- JWT认证过滤器
- 白名单机制（注册/登录免认证）
- CORS跨域配置
- Swagger JWT集成

#### 已知问题修复（v1.2.0）
1. ✅ **账户不存在问题** - 为老用户自动创建账户
2. ✅ **对话框关闭错误** - 延迟数据清理，增强条件渲染
3. ✅ **数据流问题** - 正确处理MyBatis PageHelper分页格式
4. ✅ **数据显示问题** - 统一提取`res.data`字段

**文档**: 
- API文档：`docs/社区团购系统/API_UserService.md`（v1.1.0，更新分页格式说明）
- 开发报告：`docs/社区团购系统/UserService开发完成报告.md`
- Bug修复：`docs/社区团购系统/前端数据流Bug修复报告.md`

**Swagger访问**: http://localhost:8061/swagger-ui.html

---

## 17. 当前项目文件清单

### 后端代码
```
community-group-buy-backend/
├── pom.xml (父POM)
├── common/ (公共模块)
│   ├── pom.xml
│   ├── README.md
│   └── src/main/java/com/bcu/edu/common/
│       ├── config/WebConfig.java
│       ├── constant/Constants.java
│       ├── enums/ResultCode.java
│       ├── exception/
│       │   ├── BaseException.java
│       │   └── BusinessException.java
│       ├── handler/GlobalExceptionHandler.java
│       ├── result/
│       │   ├── Result.java
│       │   └── PageResult.java
│       └── utils/
│           ├── DateUtil.java
│           ├── JwtUtil.java
│           └── SecurityUtil.java
└── UserService/ (用户服务)
    ├── pom.xml
    └── src/main/
        ├── java/com/bcu/edu/
        │   ├── UserServiceApplication.java
        │   ├── config/
        │   │   ├── JwtAuthenticationFilter.java
        │   │   ├── WebSecurityConfig.java
        │   │   └── OpenApiConfig.java
        │   ├── controller/ (4个)
        │   ├── dto/ (11个)
        │   ├── entity/ (4个)
        │   ├── repository/ (4个)
        │   └── service/ (4个)
        └── resources/application.yml
```

### 数据库文件
```
community-group-buy-backend/
└── community_group_buy.sql (17张表完整SQL)
```

### 文档
```
docs/社区团购系统/
├── ALIGNMENT_社区团购系统.md (本文档，v1.5)
├── 数据库设计说明文档.md (17张表完整设计)
├── Common模块开发完成报告.md
├── Common模块问题修复报告.md
├── UserService开发完成报告.md
├── API_UserService.md (v1.1.0，31个接口，已更新分页格式说明)
├── 管理员测试页面开发完成报告.md
├── 管理员后台功能完善报告.md (用户详情增强)
├── 前端数据流Bug修复报告.md (v1.0，2025-10-13)
├── 接口参数问题修复记录.md
├── 前端功能规划_用户模块.md
└── 密码加密说明.md
```

### 前端项目
```
community-group-buy-admin/
├── USAGE.md (管理后台使用说明)
├── src/
│   ├── api/ (API封装)
│   ├── views/ (页面组件)
│   ├── stores/ (状态管理)
│   └── router/ (路由配置)
└── package.json
```

---

## 18. 2025-10-13 Bug修复总结

### 18.1 修复的问题

#### 问题1: 用户详情对话框关闭错误
**现象**: `[Vue warn]: Unhandled error during execution of component update - TypeError: Cannot read properties of null (reading 'parentNode')`

**原因**: 
- 关闭对话框时立即清空 `currentUser`
- Vue 组件更新过程中尝试访问已清空的数据
- DOM 元素尚未完全卸载就访问了 null 引用

**解决方案**:
1. 添加 `@closed` 事件监听对话框完全关闭
2. 延迟 100ms 清理数据（`setTimeout`）
3. 增强条件渲染（`v-if="currentUser"`）
4. 确保 `el-table` 只在数组数据存在时渲染

**涉及文件**: `UserManageView.vue`, `FeedbackManageView.vue`

---

#### 问题2: 账户不存在业务异常
**现象**: `com.bcu.edu.common.exception.BusinessException: 账户不存在`

**原因**:
- 老用户在注册时未自动创建账户
- `AccountService.getAccount` 使用 `orElseThrow` 直接抛异常

**解决方案**:
```java
@Transactional
public AccountResponse getAccount(Long userId) {
    UserAccount account = accountRepository.findByUserId(userId)
            .orElseGet(() -> {
                log.warn("用户账户不存在，自动创建: userId={}", userId);
                UserAccount newAccount = new UserAccount();
                newAccount.setUserId(userId);
                return accountRepository.save(newAccount);
            });
    return AccountResponse.fromEntity(account);
}
```

**涉及文件**: `AccountService.java`

---

#### 问题3: 反馈列表数据不显示
**现象**: 
- 控制台报错：`Expected Array, got Object`
- 后端返回数据正常，但前端列表为空

**根本原因**: **MyBatis PageHelper 分页格式理解错误**

**实际分页响应**:
```javascript
{
  code: 200,
  data: {
    pageNum: 1,
    pageSize: 10,
    total: 2,
    pages: 1,
    list: [...]  // ← 数组在 list 字段，不是 data 字段！
  }
}
```

**错误代码**:
```javascript
// 错误：查找 res.data.data（不存在）
if (res.data.data && Array.isArray(res.data.data)) {
  feedbackList.value = res.data.data
}
```

**正确代码**:
```javascript
// 正确：查找 res.data.list
if (res.data.list && Array.isArray(res.data.list)) {
  feedbackList.value = res.data.list
  total.value = res.data.total
}
```

**涉及文件**: `FeedbackManageView.vue`, `UserManageView.vue`

---

#### 问题4: 用户详情数据不显示
**现象**: 用户详情、地址、账户信息不显示

**原因**: 
- API 返回 `{ code: 200, data: {...} }`
- 前端未提取 `data` 字段，直接使用整个响应对象

**解决方案**:
```javascript
// 修复前
const userRes = await getUserInfo(userId)
currentUser.value = userRes  // 错误

// 修复后
const userRes = await getUserInfo(userId)
currentUser.value = userRes.data  // 正确
```

**涉及文件**: `UserManageView.vue`

---

### 18.2 技术要点总结

#### Vue 组件生命周期管理
- 对话框关闭时需要延迟清理数据
- 使用 `@closed` 事件而不是 `@update:modelValue`
- 条件渲染 `v-if` 比 `v-show` 更安全

#### MyBatis PageHelper 分页格式
- **关键点**: 数据在 `list` 字段，不是 `data` 字段
- 完整字段：`pageNum`, `pageSize`, `total`, `pages`, `list`
- 需要兼容多种分页格式的防御性编程

#### 统一响应格式处理
- 后端统一返回：`{ code, message, data }`
- 前端必须提取 `data` 字段
- 分页接口需要二次提取：`res.data.list`

#### 数据初始化
- 引用类型初始化为 `null`
- 数组类型初始化为 `[]`
- 确保 `el-table` 的 `:data` 始终是数组

---

### 18.3 文档更新

1. ✅ 创建 `前端数据流Bug修复报告.md`
   - 详细记录问题原因和解决方案
   - 提供 MyBatis PageHelper 格式说明
   - 包含最佳实践和后续建议

2. ✅ 更新 `API_UserService.md`
   - 修正分页响应示例（使用 `list` 字段）
   - 添加 MyBatis PageHelper 格式说明
   - 标注重要字段

3. ✅ 更新 `ALIGNMENT_社区团购系统.md`
   - 记录 Bug 修复历史
   - 更新版本号为 v1.2.0
   - 添加已知问题修复清单

---

### 18.4 经验教训

1. **了解后端框架**: 必须知道后端使用的分页工具返回格式
2. **先调试再修复**: 使用 `console.log` 查看实际数据结构
3. **API 文档准确性**: 文档必须与实际实现一致
4. **防御性编程**: 始终检查数据类型，兼容多种格式
5. **组件生命周期**: 理解 Vue 响应式系统和 DOM 更新时机

---

### 18.5 后续建议

1. **前端**:
   - 创建统一的分页数据提取函数
   - 引入 TypeScript 增强类型检查
   - 编写单元测试覆盖数据处理逻辑

2. **后端**:
   - 考虑封装统一的分页响应格式
   - 在用户注册时自动创建账户（已在 v1.2.0 修复）
   - 添加数据完整性检查

3. **文档**:
   - 保持 API 文档与实际代码同步
   - 记录所有数据格式约定
   - 建立 Bug 跟踪和修复记录流程

---

**文档状态**: ✅ 已更新至最新进度（2025-10-13 20:50）  
**整体进度**: 30% 完成  
**管理后台状态**: ✅ 功能完善，已修复所有已知Bug  
**下一步**: 开发ProductService（商品管理服务）

---

## 📞 项目信息

**开发者**: 耿康瑞  
**学号**: 20221204229  
**院校**: 北京城市学院  
**指导教师**: 於永楠  
**项目路径**: E:\E\BYSJ\community-group-buy-backend

**技术支持**:
- Swagger UI: http://localhost:8061/swagger-ui.html
- Consul: http://localhost:8500
- GitHub参考: Spring Boot 3.x官方文档

