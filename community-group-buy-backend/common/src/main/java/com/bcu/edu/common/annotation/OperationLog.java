package com.bcu.edu.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 * 
 * @author AI Assistant
 * @since 2025-10-14
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    
    /**
     * 操作内容描述
     * 示例: "创建用户", "删除订单", "审核团长资质"
     * 
     * @return 操作内容
     */
    String value();
    
    /**
     * 操作模块
     * 示例: "用户管理", "订单管理", "反馈管理"
     * 
     * @return 操作模块名称
     */
    String module() default "";
    
    /**
     * 是否记录请求参数
     * 默认true，会将方法参数序列化为JSON保存到数据库
     * 
     * @return true-记录参数，false-不记录参数
     */
    boolean recordParams() default true;
    
    /**
     * 是否记录返回结果
     * 默认false，仅在操作失败时记录错误信息
     * 
     * @return true-记录返回结果，false-不记录
     */
    boolean recordResult() default false;
    
    /**
     * 敏感参数字段名
     * 这些字段在记录日志时会被脱敏处理（替换为***）
     * 默认包含: password, token, secret
     * 
     * @return 敏感字段名数组
     */
    String[] sensitiveFields() default {"password", "token", "secret", "pwd", "apiKey", "privateKey"};
}

