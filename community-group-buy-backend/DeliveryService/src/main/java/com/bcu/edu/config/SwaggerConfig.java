package com.bcu.edu.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI deliveryServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DeliveryService API文档")
                        .description("配送服务 - 智能路径规划、批量发货、配送监控")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("耿康瑞")
                                .email("20221204229@bcu.edu.cn")));
    }
}

