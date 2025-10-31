package com.bcu.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * UserService 启动类
 * 
 * 注意：UserService 作为日志服务提供方，不需要启用 @EnableFeignClients
 * 因为它不会调用自己的日志接口，而是直接使用本地 Repository
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaRepositories(basePackages = {"com.bcu.edu.repository", "com.bcu.edu.common.repository"})
@EntityScan(basePackages = {"com.bcu.edu.entity", "com.bcu.edu.common.entity"})
@ComponentScan(basePackages = {"com.bcu.edu", "com.bcu.edu.common"})
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
