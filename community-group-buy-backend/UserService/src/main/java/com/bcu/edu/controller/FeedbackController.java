package com.bcu.edu.controller;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.result.PageResult;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.FeedbackReplyRequest;
import com.bcu.edu.dto.request.FeedbackRequest;
import com.bcu.edu.dto.response.FeedbackResponse;
import com.bcu.edu.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 反馈Controller
 */
@Tag(name = "用户反馈", description = "用户反馈提交与管理接口")
@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "提交反馈", description = "用户提交问题反馈")
    @PostMapping("/submit/{userId}")
    public Result<FeedbackResponse> submitFeedback(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Valid @RequestBody FeedbackRequest request) {
        return Result.success(feedbackService.submitFeedback(userId, request));
    }

    @Operation(summary = "获取用户反馈列表", description = "查询指定用户的所有反馈记录")
    @GetMapping("/user/{userId}")
    public Result<List<FeedbackResponse>> getUserFeedbacks(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(feedbackService.getUserFeedbacks(userId));
    }

    @Operation(summary = "分页查询用户反馈", description = "分页查询指定用户的反馈记录")
    @GetMapping("/user/{userId}/page")
    public Result<PageResult<FeedbackResponse>> getUserFeedbacksPage(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return Result.success(feedbackService.getUserFeedbacksPage(userId, page, size));
    }

    @Operation(summary = "获取反馈详情", description = "查询指定反馈的详细信息")
    @GetMapping("/{feedbackId}")
    public Result<FeedbackResponse> getFeedbackById(
            @Parameter(description = "反馈ID") @PathVariable Long feedbackId) {
        return Result.success(feedbackService.getFeedbackById(feedbackId));
    }

    @Operation(summary = "管理员回复反馈", description = "管理员处理并回复用户反馈")
    @OperationLog(value = "回复用户反馈", module = "反馈管理")
    @PostMapping("/reply")
    public Result<FeedbackResponse> replyFeedback(
            @Valid @RequestBody FeedbackReplyRequest request) {
        return Result.success(feedbackService.replyFeedback(request));
    }

    @Operation(summary = "查询所有反馈（管理员）", description = "管理员分页查询所有用户反馈")
    @GetMapping("/all")
    public Result<PageResult<FeedbackResponse>> getAllFeedbacks(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return Result.success(feedbackService.getAllFeedbacks(page, size));
    }

    @Operation(summary = "按状态查询反馈（管理员）", description = "管理员按处理状态分页查询反馈")
    @GetMapping("/status/{status}")
    public Result<PageResult<FeedbackResponse>> getFeedbacksByStatus(
            @Parameter(description = "处理状态（0-待处理；1-处理中；2-已解决；3-已关闭）") @PathVariable Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return Result.success(feedbackService.getFeedbacksByStatus(status, page, size));
    }

    @Operation(summary = "删除反馈", description = "删除指定的反馈记录")
    @OperationLog(value = "删除用户反馈", module = "反馈管理")
    @DeleteMapping("/delete/{feedbackId}")
    public Result<Void> deleteFeedback(
            @Parameter(description = "反馈ID") @PathVariable Long feedbackId) {
        feedbackService.deleteFeedback(feedbackId);
        return Result.success();
    }
}

