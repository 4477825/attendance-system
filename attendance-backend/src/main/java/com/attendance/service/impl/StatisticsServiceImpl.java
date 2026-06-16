package com.attendance.service.impl;

import com.attendance.entity.AttendanceRecord;
import com.attendance.entity.LeaveRequest;
import com.attendance.entity.OvertimeRecord;
import com.attendance.entity.User;
import com.attendance.mapper.AttendanceRecordMapper;
import com.attendance.mapper.LeaveRequestMapper;
import com.attendance.mapper.OvertimeRecordMapper;
import com.attendance.mapper.UserMapper;
import com.attendance.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final AttendanceRecordMapper attendanceRecordMapper;
    private final LeaveRequestMapper leaveRequestMapper;
    private final OvertimeRecordMapper overtimeRecordMapper;
    private final UserMapper userMapper;

    public StatisticsServiceImpl(AttendanceRecordMapper attendanceRecordMapper,
                                 LeaveRequestMapper leaveRequestMapper,
                                 OvertimeRecordMapper overtimeRecordMapper,
                                 UserMapper userMapper) {
        this.attendanceRecordMapper = attendanceRecordMapper;
        this.leaveRequestMapper = leaveRequestMapper;
        this.overtimeRecordMapper = overtimeRecordMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Map<String, Object> getDeptStatistics(Long deptId, String monthStr) {
        YearMonth yearMonth = YearMonth.parse(monthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<User> users = userMapper.findByDepartmentId(deptId);
        int totalEmployees = users.size();
        int normalCount = 0;
        int lateCount = 0;
        int absentCount = 0;
        int totalWorkHours = 0;

        for (User user : users) {
            List<AttendanceRecord> records = attendanceRecordMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceRecord>()
                            .eq(AttendanceRecord::getUserId, user.getId())
                            .ge(AttendanceRecord::getDate, start)
                            .le(AttendanceRecord::getDate, end)
            );

            for (AttendanceRecord r : records) {
                if ("NORMAL".equals(r.getStatus())) normalCount++;
                else if ("LATE".equals(r.getStatus())) lateCount++;
                else if ("ABSENT".equals(r.getStatus())) absentCount++;
                if (r.getWorkHours() != null) {
                    totalWorkHours += r.getWorkHours().intValue();
                }
            }
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("departmentId", deptId);
        stats.put("month", monthStr);
        stats.put("totalEmployees", totalEmployees);
        stats.put("normalCount", normalCount);
        stats.put("lateCount", lateCount);
        stats.put("absentCount", absentCount);
        stats.put("totalWorkHours", totalWorkHours);
        return stats;
    }

    @Override
    public Map<String, Object> getAllStats(String monthStr) {
        YearMonth yearMonth = YearMonth.parse(monthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<User> allUsers = userMapper.selectList(null);
        int totalEmployees = allUsers.size();

        List<AttendanceRecord> allRecords = attendanceRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceRecord>()
                        .ge(AttendanceRecord::getDate, start)
                        .le(AttendanceRecord::getDate, end)
        );

        int normalCount = (int) allRecords.stream().filter(r -> "NORMAL".equals(r.getStatus())).count();
        int lateCount = (int) allRecords.stream().filter(r -> "LATE".equals(r.getStatus())).count();
        int absentCount = (int) allRecords.stream().filter(r -> "ABSENT".equals(r.getStatus())).count();
        int earlyCount = (int) allRecords.stream().filter(r -> "EARLY_ABSENTEE".equals(r.getStatus())).count();

        long pendingLeaves = leaveRequestMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<LeaveRequest>()
                        .eq(LeaveRequest::getStatus, "PENDING")
        );

        long approvedOvertimes = overtimeRecordMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OvertimeRecord>()
                        .eq(OvertimeRecord::getStatus, "APPROVED")
                        .ge(OvertimeRecord::getOvertimeDate, start)
                        .le(OvertimeRecord::getOvertimeDate, end)
        );

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("month", monthStr);
        stats.put("totalEmployees", totalEmployees);
        stats.put("normalCount", normalCount);
        stats.put("lateCount", lateCount);
        stats.put("absentCount", absentCount);
        stats.put("earlyCount", earlyCount);
        stats.put("pendingLeaves", pendingLeaves);
        stats.put("approvedOvertimes", approvedOvertimes);
        return stats;
    }

    @Override
    public List<Map<String, Object>> getUserMonthlyDetail(Long userId, String monthStr) {
        YearMonth yearMonth = YearMonth.parse(monthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<AttendanceRecord> records = attendanceRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceRecord>()
                        .eq(AttendanceRecord::getUserId, userId)
                        .ge(AttendanceRecord::getDate, start)
                        .le(AttendanceRecord::getDate, end)
                        .orderByAsc(AttendanceRecord::getDate)
        );

        return records.stream().map(r -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("date", r.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
            map.put("checkInTime", r.getCheckInTime() != null ? r.getCheckInTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "-");
            map.put("checkOutTime", r.getCheckOutTime() != null ? r.getCheckOutTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "-");
            map.put("status", r.getStatus());
            map.put("workHours", r.getWorkHours() != null ? r.getWorkHours().doubleValue() : 0);
            return map;
        }).collect(Collectors.toList());
    }
}
