package com.bcu.edu.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源配置类
 * 配置文件上传的静态资源映射
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Slf4j
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:E:/E/BYSJ/community-group-buy-backend/uploads/product/}")
    private String uploadPath;

    /**
     * 配置静态资源映射
     * 将 /uploads/** 映射到文件系统的上传目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置商品图片静态资源映射
        String resourceLocation = "file:" + uploadPath;
        registry.addResourceHandler("/uploads/product/**")
                .addResourceLocations(resourceLocation);
        
        log.info("静态资源映射配置完成: /uploads/product/** -> {}", resourceLocation);
    }
}

