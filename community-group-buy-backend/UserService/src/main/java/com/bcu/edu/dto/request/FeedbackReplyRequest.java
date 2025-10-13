package com.bcu.edu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 反馈回复请求（管理员）
 */
@Data
public class FeedbackReplyRequest {

    @NotNull(message = "反馈ID不能为空")
    private Long feedbackId;

    @NotBlank(message = "回复内容不能为空")
    private String reply;

    @NotNull(message = "处理状态不能为空")
    private Integer status;
}

