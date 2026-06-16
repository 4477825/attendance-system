package com.attendance.controller;

import com.attendance.common.Result;
import com.attendance.entity.Department;
import com.attendance.entity.AttendanceRecord;
import com.attendance.mapper.DepartmentMapper;
import com.attendance.service.AttendanceService;
import com.attendance.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@Tag(name = "统计接口", description = "考勤统计与报表相关接口")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final AttendanceService attendanceService;
    private final DepartmentMapper departmentMapper;

    public StatisticsController(StatisticsService statisticsService,
                                AttendanceService attendanceService,
                                DepartmentMapper departmentMapper) {
        this.statisticsService = statisticsService;
        this.attendanceService = attendanceService;
        this.departmentMapper = departmentMapper;
    }

    @GetMapping("/dept")
    @Operation(summary = "部门考勤统计")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getDeptStatistics(
            @RequestParam Long deptId,
            @RequestParam String month) {
        return Result.success(statisticsService.getDeptStatistics(deptId, month));
    }

    @GetMapping("/all")
    @Operation(summary = "全员考勤统计")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getAllStats(@RequestParam String month) {
        return Result.success(statisticsService.getAllStats(month));
    }

    @GetMapping("/user/monthly/{month}")
    @Operation(summary = "用户月度考勤详情")
    public Result<List<Map<String, Object>>> getUserMonthlyDetail(
            @PathVariable String month,
            @RequestParam Long userId) {
        return Result.success(statisticsService.getUserMonthlyDetail(userId, month));
    }

    @GetMapping("/departments")
    @Operation(summary = "获取部门列表")
    public Result<List<Department>> getDepartments() {
        List<Department> departments = departmentMapper.selectList(null);
        return Result.success(departments);
    }
}
