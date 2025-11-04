package com.bcu.edu.controller;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-11-04
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "文件上传管理", description = "用户头像上传、反馈图片上传")
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    
    /**
     * 上传用户头像
     * 
     * @param file 头像文件（限制2MB）
     * @return 头像访问URL
     */
    @PostMapping("/avatar")
    @Operation(summary = "上传用户头像", description = "上传用户头像，限制2MB，支持jpg/png/gif/webp格式")
    @OperationLog(value = "上传用户头像", module = "文件上传")
    public Result<String> uploadAvatar(
            @Parameter(description = "头像文件", required = true)
            @RequestParam("file") MultipartFile file) {
        
        log.info("上传用户头像: fileName={}, size={}KB", 
            file.getOriginalFilename(), file.getSize() / 1024);
        
        String url = fileUploadService.uploadAvatar(file);
        
        return Result.success("头像上传成功", url);
    }
    
    /**
     * 上传反馈图片
     * 
     * @param file 图片文件（限制5MB）
     * @return 图片访问URL
     */
    @PostMapping("/feedback")
    @Operation(summary = "上传反馈图片", description = "上传反馈图片，限制5MB，支持jpg/png/gif/webp格式")
    @OperationLog(value = "上传反馈图片", module = "文件上传")
    public Result<String> uploadFeedbackImage(
            @Parameter(description = "反馈图片文件", required = true)
            @RequestParam("file") MultipartFile file) {
        
        log.info("上传反馈图片: fileName={}, size={}KB", 
            file.getOriginalFilename(), file.getSize() / 1024);
        
        String url = fileUploadService.uploadFeedbackImage(file);
        
        return Result.success("反馈图片上传成功", url);
    }
    
    /**
     * 通用文件上传接口
     * 
     * @param file 文件
     * @param type 文件类型（avatar/feedback）
     * @return 文件访问URL
     */
    @PostMapping("/file")
    @Operation(summary = "通用文件上传", description = "通用文件上传接口，支持avatar和feedback类型")
    @OperationLog(value = "通用文件上传", module = "文件上传")
    public Result<String> uploadFile(
            @Parameter(description = "文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件类型（avatar/feedback）", required = false)
            @RequestParam(value = "type", defaultValue = "feedback") String type) {
        
        log.info("通用文件上传: fileName={}, size={}KB, type={}", 
            file.getOriginalFilename(), file.getSize() / 1024, type);
        
        String url = fileUploadService.uploadFile(file, type);
        
        return Result.success("文件上传成功", url);
    }
}

