package com.attendance.controller;

import com.attendance.common.Result;
import com.attendance.dto.LeaveApplyDTO;
import com.attendance.entity.LeaveRequest;
import com.attendance.security.JwtTokenProvider;
import com.attendance.service.LeaveService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave")
@Tag(name = "请假接口", description = "请假申请、审批相关接口")
public class LeaveController {

    private final LeaveService leaveService;
    private final JwtTokenProvider jwtTokenProvider;

    public LeaveController(LeaveService leaveService, JwtTokenProvider jwtTokenProvider) {
        this.leaveService = leaveService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Use the UserDetails from context
        String username = auth.getName();
        // We'll get user id from token
        return null; // handled by @RequestHeader
    }

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        }
        throw new RuntimeException("未认证");
    }

    @PostMapping("/apply")
    @Operation(summary = "提交请假申请")
    public Result<LeaveRequest> applyLeave(
            @RequestBody LeaveApplyDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        LeaveRequest leave = leaveService.applyLeave(userId, dto);
        return Result.success(leave);
    }

    @GetMapping("/list")
    @Operation(summary = "查询请假列表")
    public Result<IPage<LeaveRequest>> getLeaveList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = extractUserId(authHeader);
        // Determine if admin by checking role from token
        String role = jwtTokenProvider.getRoleFromToken(authHeader.substring(7));
        boolean isAdmin = "ROLE_ADMIN".equals(role);

        if (!isAdmin) {
            return Result.success(leaveService.getLeaveList(userId, status, pageNum, pageSize, false));
        }
        // Admin sees all, userId is ignored for listing but we need to call with isAdmin=true
        return Result.success(leaveService.getLeaveList(userId, status, pageNum, pageSize, true));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取请假详情")
    public Result<LeaveRequest> getLeaveById(@PathVariable Long id) {
        return Result.success(leaveService.getLeaveById(id));
    }

    @PutMapping("/approve/{id}")
    @Operation(summary = "审批请假申请")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<LeaveRequest> approveLeave(
            @PathVariable Long id,
            @RequestParam String approveRemark,
            @RequestParam String status,
            @RequestHeader("Authorization") String authHeader) {
        Long approverId = extractUserId(authHeader);
        LeaveRequest leave = leaveService.approveLeave(id, approverId, approveRemark, status);
        return Result.success(leave);
    }
}
