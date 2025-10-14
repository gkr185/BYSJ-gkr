package com.bcu.edu.common.aspect;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.entity.SysOperationLog;
import com.bcu.edu.common.repository.SysOperationLogRepository;
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
 * 拦截带@OperationLog注解的方法，自动记录操作日志到数据库
 */
@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    @Autowired
    private SysOperationLogRepository logRepository;

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
     * 异步保存日志到数据库
     */
    @Async("logExecutor")
    public void saveLogAsync(ProceedingJoinPoint joinPoint, OperationLog operationLog,
                             String resultStatus, String errorMsg, long duration) {
        try {
            // 构建日志对象
            SysOperationLog logEntity = new SysOperationLog();

            // 获取用户信息
            Long userId = getCurrentUserId();
            String username = getCurrentUsername();
            logEntity.setUserId(userId);
            logEntity.setUsername(username != null ? username : "unknown");

            // 设置操作信息
            logEntity.setOperation(operationLog.value());
            logEntity.setModule(operationLog.module());
            logEntity.setMethod(joinPoint.getSignature().toLongString());

            // 序列化参数（支持脱敏）
            if (operationLog.recordParams()) {
                String params = serializeParams(joinPoint.getArgs(), operationLog.sensitiveFields());
                logEntity.setParams(params);
            }

            // 设置执行结果
            logEntity.setResult(resultStatus);
            logEntity.setErrorMsg(errorMsg);
            logEntity.setDuration((int) duration);

            // 获取客户端IP
            logEntity.setIp(getClientIp());

            // 保存到数据库
            logRepository.save(logEntity);
            log.info("操作日志已记录: {}", logEntity.getOperation());
        } catch (Exception e) {
            log.error("异步保存日志失败", e);
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
     */
    private String serializeParams(Object[] args, String[] sensitiveFields) {
        try {
            if (args == null || args.length == 0) {
                return "[]";
            }

            String json = objectMapper.writeValueAsString(args);

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

