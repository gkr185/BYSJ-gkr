package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.ActivityWithProductResponse;
import com.bcu.edu.entity.GroupBuy;
import com.bcu.edu.service.GroupBuyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 拼团活动管理Controller
 * 
 * <p>管理员接口：
 * <ul>
 *   <li>POST /api/groupbuy/activity - 创建活动</li>
 *   <li>PUT /api/groupbuy/activity/{id} - 更新活动</li>
 *   <li>DELETE /api/groupbuy/activity/{id} - 删除活动</li>
 *   <li>GET /api/groupbuy/activities - 活动列表</li>
 *   <li>GET /api/groupbuy/activity/{id} - 活动详情</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@RestController
@RequestMapping("/api/groupbuy")
@Slf4j
@Tag(name = "拼团活动管理", description = "管理员创建、更新、删除拼团活动")
public class ActivityController {
    
    @Autowired
    private GroupBuyService groupBuyService;
    
    /**
     * 创建拼团活动（管理员）
     */
    @PostMapping("/activity")
    @Operation(summary = "创建拼团活动", description = "管理员创建活动，验证商品存在")
    public Result<GroupBuy> createActivity(@RequestBody GroupBuy activity) {
        GroupBuy created = groupBuyService.createActivity(activity);
        return Result.success(created);
    }
    
    /**
     * 更新活动（管理员）
     */
    @PutMapping("/activity/{id}")
    @Operation(summary = "更新拼团活动", description = "管理员更新活动信息")
    public Result<GroupBuy> updateActivity(
        @Parameter(description = "活动ID") @PathVariable Long id,
        @RequestBody GroupBuy activity) {
        
        GroupBuy updated = groupBuyService.updateActivity(id, activity);
        return Result.success(updated);
    }
    
    /**
     * 删除活动（管理员）
     */
    @DeleteMapping("/activity/{id}")
    @Operation(summary = "删除拼团活动", description = "管理员删除活动")
    public Result<Void> deleteActivity(@Parameter(description = "活动ID") @PathVariable Long id) {
        groupBuyService.deleteActivity(id);
        return Result.success("删除成功");
    }
    
    /**
     * 获取活动列表
     */
    @GetMapping("/activities")
    @Operation(summary = "获取活动列表", description = "查询所有拼团活动")
    public Result<List<GroupBuy>> getActivities() {
        List<GroupBuy> activities = groupBuyService.getActivities();
        return Result.success(activities);
    }
    
    /**
     * 获取进行中的活动
     */
    @GetMapping("/activities/ongoing")
    @Operation(summary = "获取进行中的活动", description = "查询当前有效的拼团活动")
    public Result<List<GroupBuy>> getOngoingActivities() {
        List<GroupBuy> activities = groupBuyService.getOngoingActivities();
        return Result.success(activities);
    }
    
    /**
     * 获取进行中的活动（包含商品信息）
     * 用于团长发起拼团页面
     */
    @GetMapping("/activities/ongoing-with-product")
    @Operation(summary = "获取进行中的活动（含商品信息）", description = "查询当前有效的拼团活动，包含商品名称、图片等详细信息")
    public Result<List<ActivityWithProductResponse>> getOngoingActivitiesWithProduct() {
        List<ActivityWithProductResponse> activities = groupBuyService.getOngoingActivitiesWithProduct();
        return Result.success(activities);
    }
    
    /**
     * 获取活动详情
     */
    @GetMapping("/activity/{id}")
    @Operation(summary = "获取活动详情", description = "查询拼团活动详细信息")
    public Result<GroupBuy> getActivityById(@Parameter(description = "活动ID") @PathVariable Long id) {
        GroupBuy activity = groupBuyService.getActivityById(id);
        return Result.success(activity);
    }
}

