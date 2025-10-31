package com.bcu.edu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ProductService Web配置
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 * 
 * 注意：
 * 1. 类名改为ProductWebConfig避免与Common模块的WebConfig冲突
 * 2. CORS配置已删除，统一在API Gateway层处理
 * 3. 保留静态资源映射功能（图片上传）
 */
@Configuration
public class ProductWebConfig implements WebMvcConfigurer {
    
    /**
     * 静态资源映射（图片上传）
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
    
    /**
     * ⚠️ CORS配置已删除
     * 使用API Gateway后，CORS统一在Gateway层配置
     * 业务服务不需要配置CORS，避免响应头重复导致跨域失败
     */
}

