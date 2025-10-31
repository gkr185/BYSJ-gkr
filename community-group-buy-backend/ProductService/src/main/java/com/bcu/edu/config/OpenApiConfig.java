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
 * @version 1.0.0
 * @since 2025-10-31
 */
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ProductService API文档")
                        .description("社区团购系统 - 商品服务接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("耿康瑞")
                                .email("example@example.com")));
    }
}

