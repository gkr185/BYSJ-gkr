package com.bcu.edu.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("OrderService API文档")
                .description("社区团购系统 - 订单服务API")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("耿康瑞")
                    .email("student@bcu.edu.cn")));
    }
}

