package com.attendance.controller;

import com.attendance.common.Result;
import com.attendance.dto.OvertimeApplyDTO;
import com.attendance.entity.OvertimeRecord;
import com.attendance.security.JwtTokenProvider;
import com.attendance.service.OvertimeService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/overtime")
@Tag(name = "加班接口", description = "加班申请、审批相关接口")
public class OvertimeController {

    private final OvertimeService overtimeService;
    private final JwtTokenProvider jwtTokenProvider;

    public OvertimeController(OvertimeService overtimeService, JwtTokenProvider jwtTokenProvider) {
        this.overtimeService = overtimeService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        }
        throw new RuntimeException("未认证");
    }

    @PostMapping("/apply")
    @Operation(summary = "提交加班申请")
    public Result<OvertimeRecord> applyOvertime(
            @RequestBody OvertimeApplyDTO dto,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        OvertimeRecord record = overtimeService.applyOvertime(userId, dto);
        return Result.success(record);
    }

    @GetMapping("/list")
    @Operation(summary = "查询加班列表")
    public Result<IPage<OvertimeRecord>> getOvertimeList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = extractUserId(authHeader);
        String role = jwtTokenProvider.getRoleFromToken(authHeader.substring(7));
        boolean isAdmin = "ROLE_ADMIN".equals(role);
        return Result.success(overtimeService.getOvertimeList(userId, status, pageNum, pageSize, isAdmin));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取加班详情")
    public Result<OvertimeRecord> getOvertimeById(@PathVariable Long id) {
        return Result.success(overtimeService.getOvertimeById(id));
    }

    @PutMapping("/approve/{id}")
    @Operation(summary = "审批加班申请")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<OvertimeRecord> approveOvertime(
            @PathVariable Long id,
            @RequestParam String approveRemark,
            @RequestParam String status,
            @RequestHeader("Authorization") String authHeader) {
        Long approverId = extractUserId(authHeader);
        OvertimeRecord record = overtimeService.approveOvertime(id, approverId, approveRemark, status);
        return Result.success(record);
    }
}
