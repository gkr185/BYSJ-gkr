package com.bcu.edu.common.aspect;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.dto.OperationLogDTO;
import com.bcu.edu.common.feign.LogFeignClient;
import com.bcu.edu.common.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.Executor;

/**
 * 操作日志切面
 * 拦截带@OperationLog注解的方法，通过Feign调用UserService记录日志
 * 
 * 改造说明（2025-10-31）:
 * - 原方案: 直接使用Repository保存到本地数据库（导致跨库问题）
 * - 新方案: 通过Feign调用UserService的日志API（符合微服务架构）
 */
@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    @Autowired(required = false)
    private LogFeignClient logFeignClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("logExecutor")
    private Executor logExecutor;

    /**
     * 环绕通知：拦截带@OperationLog注解的方法
     */
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        String errorMsg = null;
        String resultStatus = "SUCCESS";

        try {
            // 执行业务方法
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            // 记录失败状态
            resultStatus = "FAIL";
            errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500); // 限制错误信息长度
            }
            throw e;
        } finally {
            // 异步保存日志（无论成功失败都记录）
            try {
                long duration = System.currentTimeMillis() - startTime;
                saveLogAsync(joinPoint, operationLog, resultStatus, errorMsg, duration);
            } catch (Exception e) {
                log.error("保存操作日志失败", e);
                // 日志记录失败不影响业务
            }
        }
    }

    /**
     * 异步保存日志（通过Feign调用UserService）
     */
    @Async("logExecutor")
    public void saveLogAsync(ProceedingJoinPoint joinPoint, OperationLog operationLog,
                             String resultStatus, String errorMsg, long duration) {
        try {
            // 如果没有配置LogFeignClient（UserService自身），跳过
            if (logFeignClient == null) {
                log.debug("LogFeignClient未配置，跳过日志记录");
                return;
            }

            // 构建日志DTO对象
            OperationLogDTO logDTO = OperationLogDTO.builder()
                    .userId(getCurrentUserId())
                    .username(getCurrentUsername() != null ? getCurrentUsername() : "unknown")
                    .operation(operationLog.value())
                    .module(operationLog.module())
                    .method(joinPoint.getSignature().toLongString())
                    .result(resultStatus)
                    .errorMsg(errorMsg)
                    .duration((int) duration)
                    .ip(getClientIp())
                    .build();

            // 序列化参数（支持脱敏）
            if (operationLog.recordParams()) {
                String params = serializeParams(joinPoint.getArgs(), operationLog.sensitiveFields());
                logDTO.setParams(params);
            }

            // 通过Feign调用UserService保存日志
            logFeignClient.saveLog(logDTO);
            log.debug("操作日志已通过Feign记录: module={}, operation={}", 
                    logDTO.getModule(), logDTO.getOperation());
        } catch (Exception e) {
            // 日志记录失败不影响业务，仅记录本地日志
            log.error("异步保存日志失败: module={}, operation={}", 
                    operationLog.module(), operationLog.value(), e);
        }
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return null;
            }
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                return JwtUtil.getUserId(token);
            }
        } catch (Exception e) {
            log.warn("获取当前用户ID失败", e);
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return null;
            }
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                return JwtUtil.getUsername(token);
            }
        } catch (Exception e) {
            log.warn("获取当前用户名失败", e);
        }
        return null;
    }

    /**
     * 序列化方法参数为JSON（支持敏感字段脱敏）
     * 注意：会过滤掉无法序列化的类型（如 MultipartFile）
     */
    private String serializeParams(Object[] args, String[] sensitiveFields) {
        try {
            if (args == null || args.length == 0) {
                return "[]";
            }

            // 过滤掉无法序列化的参数类型
            Object[] serializableArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) {
                    serializableArgs[i] = null;
                } else if (isSerializable(args[i])) {
                    serializableArgs[i] = args[i];
                } else {
                    // 对于无法序列化的类型，只记录类型名称
                    serializableArgs[i] = "[" + args[i].getClass().getSimpleName() + "]";
                }
            }

            String json = objectMapper.writeValueAsString(serializableArgs);

            // 脱敏处理
            json = maskSensitiveFields(json, sensitiveFields);

            // 限制参数长度（避免过大的参数影响性能）
            if (json.length() > 5000) {
                return json.substring(0, 5000) + "...[truncated]";
            }

            return json;
        } catch (Exception e) {
            log.error("参数序列化失败", e);
            return "[序列化失败]";
        }
    }

    /**
     * 判断对象是否可安全序列化
     * 
     * @param obj 待判断的对象
     * @return true-可序列化; false-不可序列化
     */
    private boolean isSerializable(Object obj) {
        if (obj == null) {
            return true;
        }
        
        String className = obj.getClass().getName();
        
        // 过滤掉Spring的MultipartFile及其实现类
        if (className.contains("MultipartFile")) {
            return false;
        }
        
        // 过滤掉InputStream及其子类
        if (obj instanceof java.io.InputStream) {
            return false;
        }
        
        // 过滤掉HttpServletRequest/Response
        if (className.contains("HttpServlet")) {
            return false;
        }
        
        // 过滤掉ServletRequest相关类
        if (className.contains("ServletRequest") || className.contains("RequestFacade")) {
            return false;
        }
        
        return true;
    }

    /**
     * 敏感字段脱敏
     */
    private String maskSensitiveFields(String json, String[] sensitiveFields) {
        if (json == null || sensitiveFields == null || sensitiveFields.length == 0) {
            return json;
        }

        for (String field : sensitiveFields) {
            // 使用正则替换敏感字段值
            // 匹配格式: "password":"xxx" 或 "password":xxx
            String regex = "\"" + field + "\"\\s*:\\s*\"[^\"]*\"";
            json = json.replaceAll(regex, "\"" + field + "\":\"***\"");

            // 匹配无引号的值
            String regex2 = "\"" + field + "\"\\s*:\\s*[^,}\\]]*";
            json = json.replaceAll(regex2, "\"" + field + "\":\"***\"");
        }

        return json;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp() {
        try {
            HttpServletRequest request = getRequest();
            if (request == null) {
                return "unknown";
            }

            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }

            // 处理多个IP的情况（X-Forwarded-For可能包含多个IP）
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }

            return ip;
        } catch (Exception e) {
            log.warn("获取客户端IP失败", e);
            return "unknown";
        }
    }

    /**
     * 获取当前HTTP请求对象
     */
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}

