package com.bcu.edu.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * IP地址工具类
 * 用于获取客户端真实IP地址（支持代理）
 */
@Slf4j
public class IpUtil {

    /**
     * 获取客户端真实IP地址
     * 支持通过代理服务器转发的情况
     *
     * @param request HTTP请求对象
     * @return IP地址字符串
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        try {
            // 1. 尝试从X-Forwarded-For获取（适用于经过代理的请求）
            String ip = request.getHeader("X-Forwarded-For");
            if (isValidIp(ip)) {
                // X-Forwarded-For可能包含多个IP，取第一个
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }

            // 2. 尝试从X-Real-IP获取（适用于Nginx代理）
            ip = request.getHeader("X-Real-IP");
            if (isValidIp(ip)) {
                return ip;
            }

            // 3. 尝试从Proxy-Client-IP获取
            ip = request.getHeader("Proxy-Client-IP");
            if (isValidIp(ip)) {
                return ip;
            }

            // 4. 尝试从WL-Proxy-Client-IP获取（WebLogic代理）
            ip = request.getHeader("WL-Proxy-Client-IP");
            if (isValidIp(ip)) {
                return ip;
            }

            // 5. 尝试从HTTP_CLIENT_IP获取
            ip = request.getHeader("HTTP_CLIENT_IP");
            if (isValidIp(ip)) {
                return ip;
            }

            // 6. 尝试从HTTP_X_FORWARDED_FOR获取
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            if (isValidIp(ip)) {
                return ip;
            }

            // 7. 直接从RemoteAddr获取（无代理的情况）
            ip = request.getRemoteAddr();
            if (isValidIp(ip)) {
                // IPv6本地地址转换为IPv4
                if ("0:0:0:0:0:0:0:1".equals(ip)) {
                    return "127.0.0.1";
                }
                return ip;
            }

            return "unknown";
        } catch (Exception e) {
            log.warn("获取客户端IP失败", e);
            return "unknown";
        }
    }

    /**
     * 验证IP地址是否有效
     */
    private static boolean isValidIp(String ip) {
        return ip != null
                && !ip.isEmpty()
                && !"unknown".equalsIgnoreCase(ip);
    }

    /**
     * 判断是否为内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // IPv4内网地址段
        return ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.startsWith("172.16.")
                || ip.startsWith("172.17.")
                || ip.startsWith("172.18.")
                || ip.startsWith("172.19.")
                || ip.startsWith("172.20.")
                || ip.startsWith("172.21.")
                || ip.startsWith("172.22.")
                || ip.startsWith("172.23.")
                || ip.startsWith("172.24.")
                || ip.startsWith("172.25.")
                || ip.startsWith("172.26.")
                || ip.startsWith("172.27.")
                || ip.startsWith("172.28.")
                || ip.startsWith("172.29.")
                || ip.startsWith("172.30.")
                || ip.startsWith("172.31.")
                || ip.startsWith("127.");
    }
}

