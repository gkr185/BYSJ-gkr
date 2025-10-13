package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 用户反馈表
 * 存储用户反馈信息，支持管理员处理与回复
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_feedback", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_create_time", columnList = "create_time")
})
@Comment("用户反馈表")
public class UserFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    @Comment("反馈ID")
    private Long feedbackId;

    @Column(name = "user_id", nullable = false)
    @Comment("用户ID")
    private Long userId;

    @Column(name = "type", nullable = false)
    @Comment("反馈类型（1-功能问题；2-商品问题；3-配送问题；4-支付问题；5-其他）")
    private Integer type;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @Comment("反馈内容")
    private String content;

    @Column(name = "images", columnDefinition = "TEXT")
    @Comment("图片URL（多张用逗号分隔）")
    private String images;

    @Column(name = "status", nullable = false)
    @Comment("处理状态（0-待处理；1-处理中；2-已解决；3-已关闭）")
    private Integer status = 0;

    @Column(name = "reply", columnDefinition = "TEXT")
    @Comment("处理意见（管理员回复）")
    private String reply;

    @Column(name = "reply_time")
    @Comment("处理时间")
    private LocalDateTime replyTime;

    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("反馈提交时间")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    @Comment("更新时间")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    /**
     * 反馈类型枚举
     */
    public enum Type {
        FUNCTION(1, "功能问题"),
        PRODUCT(2, "商品问题"),
        DELIVERY(3, "配送问题"),
        PAYMENT(4, "支付问题"),
        OTHER(5, "其他");

        private final Integer code;
        private final String desc;

        Type(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 反馈状态枚举
     */
    public enum Status {
        PENDING(0, "待处理"),
        PROCESSING(1, "处理中"),
        RESOLVED(2, "已解决"),
        CLOSED(3, "已关闭");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}

