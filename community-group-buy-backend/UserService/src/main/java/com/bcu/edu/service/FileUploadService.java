package com.bcu.edu.service;

import com.bcu.edu.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 文件上传服务
 * 用于用户头像上传和反馈图片上传
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-11-04
 */
@Slf4j
@Service
public class FileUploadService {
    
    @Value("${server.port:8061}")
    private String serverPort;
    
    @Value("${file.upload.path:E:/E/BYSJ/community-group-buy-backend/uploads/user/}")
    private String uploadPath;
    
    // 用户头像限制：2MB
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024;
    // 反馈图片限制：5MB
    private static final long MAX_FEEDBACK_SIZE = 5 * 1024 * 1024;
    // 支持的图片格式
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");
    
    /**
     * 上传用户头像
     * 
     * @param file 上传的文件
     * @return 文件访问URL
     */
    public String uploadAvatar(MultipartFile file) {
        // 1. 文件校验（头像限制2MB）
        validateFile(file, MAX_AVATAR_SIZE, "头像");
        
        // 2. 生成文件名（avatar前缀）
        String fileName = "avatar_" + generateFileName(file.getOriginalFilename());
        
        // 3. 保存文件
        return saveFile(file, fileName);
    }
    
    /**
     * 上传反馈图片
     * 
     * @param file 上传的文件
     * @return 文件访问URL
     */
    public String uploadFeedbackImage(MultipartFile file) {
        // 1. 文件校验（反馈图片限制5MB）
        validateFile(file, MAX_FEEDBACK_SIZE, "反馈图片");
        
        // 2. 生成文件名（feedback前缀）
        String fileName = "feedback_" + generateFileName(file.getOriginalFilename());
        
        // 3. 保存文件
        return saveFile(file, fileName);
    }
    
    /**
     * 通用文件上传方法
     * 
     * @param file 上传的文件
     * @param type 文件类型（avatar/feedback）
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String type) {
        if ("avatar".equalsIgnoreCase(type)) {
            return uploadAvatar(file);
        } else if ("feedback".equalsIgnoreCase(type)) {
            return uploadFeedbackImage(file);
        } else {
            // 默认使用反馈图片的限制
            validateFile(file, MAX_FEEDBACK_SIZE, "图片");
            String fileName = type + "_" + generateFileName(file.getOriginalFilename());
            return saveFile(file, fileName);
        }
    }
    
    /**
     * 保存文件到磁盘
     */
    private String saveFile(MultipartFile file, String fileName) {
        // 1. 创建上传目录（使用绝对路径）
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                log.info("创建上传目录成功: {}", uploadDir.getAbsolutePath());
            }
        }
        
        // 2. 保存文件
        try {
            File destFile = new File(uploadPath + fileName);
            log.debug("保存文件到: {}", destFile.getAbsolutePath());
            file.transferTo(destFile);
            log.info("文件上传成功: {} -> {}", file.getOriginalFilename(), fileName);
        } catch (IOException e) {
            log.error("文件上传失败: uploadPath={}, fileName={}", uploadPath, fileName, e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
        
        // 3. 返回访问URL
        return "http://localhost:" + serverPort + "/uploads/user/" + fileName;
    }
    
    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file, long maxSize, String fileType) {
        // 文件为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(fileType + "不能为空");
        }
        
        // 文件大小校验
        if (file.getSize() > maxSize) {
            long maxMB = maxSize / (1024 * 1024);
            throw new BusinessException(fileType + "大小不能超过" + maxMB + "MB");
        }
        
        // 文件格式校验
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException("仅支持jpg、jpeg、png、gif、webp格式的图片");
        }
    }
    
    /**
     * 生成文件名：yyyyMMddHHmmss_随机6位数字.扩展名
     */
    private String generateFileName(String originalFilename) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", new Random().nextInt(1000000));
        String extension = getFileExtension(originalFilename);
        return timestamp + "_" + random + extension;
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}

