package com.bcu.edu.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 配置类
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI groupBuyServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GroupBuyService API")
                        .description("""
                                拼团服务API文档
                                
                                ## 核心功能
                                1. **拼团活动管理** - 管理员创建、查询、删除拼团活动
                                2. **团长发起拼团** - 团长发起拼团，自动关联社区（v3.0）
                                3. **用户参与拼团** - 用户参团，创建订单，行锁防并发
                                4. **支付回调处理** - 成团检查，幂等性保证
                                5. **查询功能** - 团详情、活动团列表（社区优先）、我的拼团
                                6. **退出拼团** - 成团前可退，自动退款
                                7. **定时任务** - 过期团检查，自动退款
                                
                                ## 技术亮点
                                - ⭐ 无Redis，使用数据库行锁（SELECT ... FOR UPDATE）
                                - ⭐ 幂等性设计（支付回调、成团逻辑、定时任务）
                                - ⭐ Saga模式跨服务调用（Feign + 补偿）
                                - ⭐ 社区优先推荐（SQL ORDER BY CASE）
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("耿康瑞")
                                .email("gkr@bcu.edu.cn"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:9000").description("Gateway（生产环境）"),
                        new Server().url("http://localhost:8063").description("GroupBuyService（直连）")
                ));
    }
}

