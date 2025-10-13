package com.bcu.edu.common.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.cn;

/**
 * 安全工具类
 * 提供加密、解密、哈希等安全相关功能
 *
 * @author 耿康瑞
 * @date 2025-10-12
 */
@Slf4j
public class SecurityUtil {

    /**
     * 默认密钥（生产环境应从配置文件读取）
     * 16位密钥用于AES-128加密
     */
    private static final String DEFAULT_AES_KEY = "CommunityGroup16";

    /**
     * 密码加密盐值（生产环境应从配置文件读取）
     */
    private static final String PASSWORD_SALT = "community_group_buy_2025";

    /**
     * SHA256加密（用于密码加密）
     * 
     * @param text 原始文本
     * @return 加密后的十六进制字符串
     */
    public static String sha256(String text) {
        if (text == null) {
            return null;
        }
        return SecureUtil.sha256(text);
    }

    /**
     * 密码加密（SHA256 + 盐值）
     * 
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        if (password == null) {
            return null;
        }
        return sha256(password + PASSWORD_SALT);
    }

    /**
     * 验证密码
     * 
     * @param rawPassword 原始密码
     * @param encryptedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verifyPassword(String rawPassword, String encryptedPassword) {
        if (rawPassword == null || encryptedPassword == null) {
            return false;
        }
        return encryptPassword(rawPassword).equals(encryptedPassword);
    }

    /**
     * MD5加密
     * 
     * @param text 原始文本
     * @return 加密后的十六进制字符串
     */
    public static String md5(String text) {
        if (text == null) {
            return null;
        }
        return SecureUtil.md5(text);
    }

    /**
     * AES加密（用于手机号、支付信息等敏感数据）
     * 
     * @param text 原始文本
     * @return Base64编码后的加密字符串
     */
    public static String aesEncrypt(String text) {
        return aesEncrypt(text, DEFAULT_AES_KEY);
    }

    /**
     * AES加密（别名方法，兼容UserService）
     * 
     * @param text 原始文本
     * @return Base64编码后的加密字符串
     */
    public static String encryptAES(String text) {
        return aesEncrypt(text);
    }

    /**
     * AES加密（指定密钥）
     * 
     * @param text 原始文本
     * @param key 密钥
     * @return Base64编码后的加密字符串
     */
    public static String aesEncrypt(String text, String key) {
        if (text == null) {
            return null;
        }
        try {
            AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = aes.encrypt(text);
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("AES加密失败", e);
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * AES解密
     * 
     * @param encryptedText Base64编码的加密字符串
     * @return 解密后的原始文本
     */
    public static String aesDecrypt(String encryptedText) {
        return aesDecrypt(encryptedText, DEFAULT_AES_KEY);
    }

    /**
     * AES解密（指定密钥）
     * 
     * @param encryptedText Base64编码的加密字符串
     * @param key 密钥
     * @return 解密后的原始文本
     */
    public static String aesDecrypt(String encryptedText, String key) {
        if (encryptedText == null) {
            return null;
        }
        try {
            AES aes = SecureUtil.aes(key.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = aes.decrypt(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密失败", e);
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 生成签名（用于支付等场景）
     * 
     * @param data 待签名数据
     * @param key 签名密钥
     * @return 签名字符串
     */
    public static String sign(String data, String key) {
        if (data == null || key == null) {
            return null;
        }
        return sha256(data + key);
    }

    /**
     * 验证签名
     * 
     * @param data 原始数据
     * @param key 签名密钥
     * @param signature 签名
     * @return 是否验证通过
     */
    public static boolean verifySign(String data, String key, String signature) {
        if (data == null || key == null || signature == null) {
            return false;
        }
        return sign(data, key).equals(signature);
    }

    /**
     * 手机号脱敏显示
     * 
     * @param phone 手机号
     * @return 脱敏后的手机号（例如：138****5678）
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 姓名脱敏显示
     * 
     * @param name 姓名
     * @return 脱敏后的姓名（例如：张*、李**）
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    /**
     * 身份证号脱敏显示
     * 
     * @param idCard 身份证号
     * @return 脱敏后的身份证号（例如：320***********1234）
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 3) + "*".repeat(idCard.length() - 7) + idCard.substring(idCard.length() - 4);
    }
}

