package com.attendance.controller;

import com.attendance.common.Result;
import com.attendance.dto.AttendanceQueryDTO;
import com.attendance.entity.AttendanceRecord;
import com.attendance.security.JwtTokenProvider;
import com.attendance.service.AttendanceService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance")
@Tag(name = "考勤接口", description = "打卡签到、考勤记录查询相关接口")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final JwtTokenProvider jwtTokenProvider;

    public AttendanceController(AttendanceService attendanceService, JwtTokenProvider jwtTokenProvider) {
        this.attendanceService = attendanceService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String token = null;
        // Extract token from SecurityContext details or header
        return jwtTokenProvider.getUserIdFromToken("placeholder");
    }

    @PostMapping("/check-in")
    @Operation(summary = "打卡签到")
    public Result<AttendanceRecord> checkIn(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        AttendanceRecord record = attendanceService.checkIn(userId);
        return Result.success(record);
    }

    @PostMapping("/check-out")
    @Operation(summary = "打卡签退")
    public Result<Void> checkOut(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        attendanceService.checkOut(userId);
        return Result.success("签退成功");
    }

    @GetMapping("/records")
    @Operation(summary = "查询考勤记录")
    public Result<IPage<AttendanceRecord>> getRecords(AttendanceQueryDTO queryDTO,
                                                      @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // Admin can query any user, employee only sees their own
        if (queryDTO.getUserId() == null) {
            queryDTO.setUserId(extractUserId(authHeader));
        }
        return Result.success(attendanceService.getRecords(queryDTO));
    }

    @GetMapping("/today")
    @Operation(summary = "获取今日考勤记录")
    public Result<AttendanceRecord> getTodayRecord(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        AttendanceRecord record = attendanceService.getTodayRecord(userId);
        return Result.success(record);
    }

    @GetMapping("/monthly/{userId}")
    @Operation(summary = "获取某月考勤汇总")
    public Result<java.util.Map<String, Object>> getMonthSummary(
            @PathVariable Long userId,
            @RequestParam String month) {
        LocalDate monthDate = LocalDate.parse(month + "-01");
        return Result.success(attendanceService.getMonthSummary(userId, monthDate));
    }

    @GetMapping("/my-monthly/{month}")
    @Operation(summary = "获取本人某月考勤汇总")
    public Result<java.util.Map<String, Object>> getMyMonthSummary(
            @PathVariable String month,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        LocalDate monthDate = LocalDate.parse(month + "-01");
        return Result.success(attendanceService.getMonthSummary(userId, monthDate));
    }

    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtTokenProvider.getUserIdFromToken(authHeader.substring(7));
        }
        throw new RuntimeException("未认证");
    }
}
