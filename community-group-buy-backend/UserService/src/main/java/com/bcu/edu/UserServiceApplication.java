package com.bcu.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients  // 启用Feign客户端
@EnableJpaRepositories(basePackages = {"com.bcu.edu.repository", "com.bcu.edu.common.repository"})
@EntityScan(basePackages = {"com.bcu.edu.entity", "com.bcu.edu.common.entity"})
@ComponentScan(basePackages = {"com.bcu.edu", "com.bcu.edu.common"})
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
