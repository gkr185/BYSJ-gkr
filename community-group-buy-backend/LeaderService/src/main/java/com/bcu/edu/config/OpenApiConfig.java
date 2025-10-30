package com.bcu.edu.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 配置
 *
 * @author 耿康瑞
 * @date 2025-10-30
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI leaderServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LeaderService API 文档")
                        .description("社区团购系统 - 团长服务接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("耿康瑞")
                                .email("example@bcu.edu.cn"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}

