package com.attendance.controller;

import com.attendance.common.Result;
import com.attendance.dto.OvertimeApplyDTO;
import com.attendance.entity.OvertimeRecord;
import com.attendance.service.OvertimeService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/overtime")
@Tag(name = "Overtime Management", description = "Overtime registration and approval APIs")
public class OvertimeController {

    private final OvertimeService overtimeService;

    public OvertimeController(OvertimeService overtimeService) {
        this.overtimeService = overtimeService;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return Long.parseLong(auth.getName());
        }
        throw new IllegalStateException("User not authenticated");
    }

    @PostMapping("/apply")
    @Operation(summary = "Submit overtime application")
    public Result<OvertimeRecord> applyOvertime(
            @Valid @RequestBody OvertimeApplyDTO dto) {
        Long userId = getCurrentUserId();
        OvertimeRecord record = overtimeService.applyOvertime(userId, dto);
        return Result.success(record);
    }

    @GetMapping("/list")
    @Operation(summary = "Query overtime records")
    public Result<IPage<OvertimeRecord>> getOvertimeList(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            Page<OvertimeRecord> emptyPage = new Page<>(pageNum, pageSize);
            emptyPage.setRecords(java.util.Collections.emptyList());
            emptyPage.setTotal(0);
            return Result.success((IPage<OvertimeRecord>) emptyPage);
        }
        Long userId = Long.parseLong(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));
        return Result.success(overtimeService.getOvertimeList(userId, status, pageNum, pageSize, isAdmin));
    }

    @GetMapping("/{id}")
    @Operation(summary = "View overtime detail")
    public Result<OvertimeRecord> getOvertimeById(@PathVariable Long id) {
        return Result.success(overtimeService.getOvertimeById(id));
    }

    @PutMapping("/approve/{id}")
    @Operation(summary = "Approve overtime application")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<OvertimeRecord> approveOvertime(
            @PathVariable Long id,
            @RequestParam String approveRemark,
            @RequestParam String status) {
        Long approverId = getCurrentUserId();
        OvertimeRecord record = overtimeService.approveOvertime(id, approverId, approveRemark, status);
        return Result.success(record);
    }
}