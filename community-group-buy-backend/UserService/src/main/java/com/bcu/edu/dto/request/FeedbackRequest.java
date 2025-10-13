package com.bcu.edu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户反馈请求
 */
@Data
public class FeedbackRequest {

    @NotNull(message = "反馈类型不能为空")
    private Integer type;

    @NotBlank(message = "反馈内容不能为空")
    private String content;

    private String images; // 多张图片URL，逗号分隔
}

