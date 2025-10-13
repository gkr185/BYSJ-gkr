package com.bcu.edu.dto.response;

import com.bcu.edu.entity.UserFeedback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 反馈响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

    private Long feedbackId;
    private Long userId;
    private Integer type;
    private String typeName;
    private String content;
    private String images;
    private Integer status;
    private String statusName;
    private String reply;
    private LocalDateTime replyTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 从实体类转换
     */
    public static FeedbackResponse fromEntity(UserFeedback feedback) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getFeedbackId())
                .userId(feedback.getUserId())
                .type(feedback.getType())
                .typeName(getTypeName(feedback.getType()))
                .content(feedback.getContent())
                .images(feedback.getImages())
                .status(feedback.getStatus())
                .statusName(getStatusName(feedback.getStatus()))
                .reply(feedback.getReply())
                .replyTime(feedback.getReplyTime())
                .createTime(feedback.getCreateTime())
                .updateTime(feedback.getUpdateTime())
                .build();
    }

    private static String getTypeName(Integer type) {
        if (type == null) return null;
        return switch (type) {
            case 1 -> "功能问题";
            case 2 -> "商品问题";
            case 3 -> "配送问题";
            case 4 -> "支付问题";
            case 5 -> "其他";
            default -> "未知";
        };
    }

    private static String getStatusName(Integer status) {
        if (status == null) return null;
        return switch (status) {
            case 0 -> "待处理";
            case 1 -> "处理中";
            case 2 -> "已解决";
            case 3 -> "已关闭";
            default -> "未知";
        };
    }
}

