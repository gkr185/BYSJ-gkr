package com.bcu.edu.service;

import com.bcu.edu.common.enums.ResultCode;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.common.result.PageResult;
import com.bcu.edu.dto.request.FeedbackReplyRequest;
import com.bcu.edu.dto.request.FeedbackRequest;
import com.bcu.edu.dto.response.FeedbackResponse;
import com.bcu.edu.entity.UserFeedback;
import com.bcu.edu.repository.UserFeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 反馈服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final UserFeedbackRepository feedbackRepository;

    /**
     * 提交反馈
     */
    @Transactional
    public FeedbackResponse submitFeedback(Long userId, FeedbackRequest request) {
        UserFeedback feedback = new UserFeedback();
        feedback.setUserId(userId);
        feedback.setType(request.getType());
        feedback.setContent(request.getContent());
        feedback.setImages(request.getImages());
        feedback.setStatus(UserFeedback.Status.PENDING.getCode());

        feedback = feedbackRepository.save(feedback);

        log.info("用户提交反馈: userId={}, feedbackId={}, type={}", userId, feedback.getFeedbackId(), request.getType());

        return FeedbackResponse.fromEntity(feedback);
    }

    /**
     * 获取用户的反馈列表
     */
    public List<FeedbackResponse> getUserFeedbacks(Long userId) {
        List<UserFeedback> feedbacks = feedbackRepository.findByUserId(userId);
        return feedbacks.stream()
                .map(FeedbackResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 分页查询用户反馈
     */
    public PageResult<FeedbackResponse> getUserFeedbacksPage(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<UserFeedback> feedbackPage = feedbackRepository.findByUserId(userId, pageable);

        List<FeedbackResponse> content = feedbackPage.getContent().stream()
                .map(FeedbackResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageResult<>(
                feedbackPage.getTotalElements(),
                (long) feedbackPage.getTotalPages(),
                (long) feedbackPage.getNumber() + 1,
                (long) feedbackPage.getSize(),
                content
        );
    }

    /**
     * 获取反馈详情
     */
    public FeedbackResponse getFeedbackById(Long feedbackId) {
        UserFeedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "反馈不存在"));
        return FeedbackResponse.fromEntity(feedback);
    }

    /**
     * 管理员回复反馈
     */
    @Transactional
    public FeedbackResponse replyFeedback(FeedbackReplyRequest request) {
        UserFeedback feedback = feedbackRepository.findById(request.getFeedbackId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "反馈不存在"));

        feedback.setReply(request.getReply());
        feedback.setStatus(request.getStatus());
        feedback.setReplyTime(LocalDateTime.now());

        feedback = feedbackRepository.save(feedback);

        log.info("管理员回复反馈: feedbackId={}, status={}", feedback.getFeedbackId(), request.getStatus());

        return FeedbackResponse.fromEntity(feedback);
    }

    /**
     * 查询所有反馈（管理员）
     */
    public PageResult<FeedbackResponse> getAllFeedbacks(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<UserFeedback> feedbackPage = feedbackRepository.findAll(pageable);

        List<FeedbackResponse> content = feedbackPage.getContent().stream()
                .map(FeedbackResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageResult<>(
                feedbackPage.getTotalElements(),
                (long) feedbackPage.getTotalPages(),
                (long) feedbackPage.getNumber() + 1,
                (long) feedbackPage.getSize(),
                content
        );
    }

    /**
     * 按状态查询反馈（管理员）
     */
    public PageResult<FeedbackResponse> getFeedbacksByStatus(Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<UserFeedback> feedbackPage = feedbackRepository.findByStatus(status, pageable);

        List<FeedbackResponse> content = feedbackPage.getContent().stream()
                .map(FeedbackResponse::fromEntity)
                .collect(Collectors.toList());

        return new PageResult<>(
                feedbackPage.getTotalElements(),
                (long) feedbackPage.getTotalPages(),
                (long) feedbackPage.getNumber() + 1,
                (long) feedbackPage.getSize(),
                content
        );
    }

    /**
     * 删除反馈
     */
    @Transactional
    public void deleteFeedback(Long feedbackId) {
        if (!feedbackRepository.existsById(feedbackId)) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "反馈不存在");
        }

        feedbackRepository.deleteById(feedbackId);

        log.info("删除反馈: feedbackId={}", feedbackId);
    }
}

