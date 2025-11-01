package com.bcu.edu.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 安全工具类（加密、签名）
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Component
@Slf4j
public class SecurityUtil {

    @Value("${security.aes.key}")
    private String aesKey;

    @Value("${security.sha256.salt}")
    private String sha256Salt;

    /**
     * AES加密（用于支付回调信息）
     */
    public String aesEncrypt(String content) {
        try {
            AES aes = SecureUtil.aes(aesKey.getBytes(StandardCharsets.UTF_8));
            return aes.encryptHex(content);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new RuntimeException("数据加密失败");
        }
    }

    /**
     * AES解密
     */
    public String aesDecrypt(String encryptedHex) {
        try {
            AES aes = SecureUtil.aes(aesKey.getBytes(StandardCharsets.UTF_8));
            return aes.decryptStr(encryptedHex);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new RuntimeException("数据解密失败");
        }
    }

    /**
     * SHA256签名（用于防篡改）
     */
    public String sha256Sign(String content) {
        return SecureUtil.sha256(content + sha256Salt);
    }

    /**
     * 验证SHA256签名
     */
    public boolean verifySha256Sign(String content, String sign) {
        String computed = sha256Sign(content);
        return computed.equals(sign);
    }
}

