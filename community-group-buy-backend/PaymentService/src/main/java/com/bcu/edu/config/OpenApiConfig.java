package com.bcu.edu.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
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
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("PaymentService API文档")
                .description("支付服务 - 支付、充值、退款管理")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("耿康瑞")
                    .email("20221204229@example.com")));
    }
}

