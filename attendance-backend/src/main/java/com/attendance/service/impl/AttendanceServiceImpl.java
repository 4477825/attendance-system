package com.attendance.service.impl;

import com.attendance.common.ErrorCode;
import com.attendance.common.exception.BusinessException;
import com.attendance.dto.AttendanceQueryDTO;
import com.attendance.entity.AttendanceRecord;
import com.attendance.mapper.AttendanceRecordMapper;
import com.attendance.service.AttendanceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordMapper recordMapper;

    public AttendanceServiceImpl(AttendanceRecordMapper recordMapper) {
        this.recordMapper = recordMapper;
    }

    @Override
    public AttendanceRecord checkIn(Long userId) {
        LocalDate today = LocalDate.now();
        AttendanceRecord existing = recordMapper.selectTodayRecord(userId, today);
        if (existing != null && existing.getCheckInTime() != null) {
            throw new BusinessException(ErrorCode.ATTENDANCE_DUPLICATE, "今日已签到，请勿重复打卡");
        }

        AttendanceRecord record;
        if (existing != null) {
            record = existing;
        } else {
            record = new AttendanceRecord();
            record.setUserId(userId);
            record.setDate(today);
        }

        LocalDateTime now = LocalDateTime.now();
        record.setCheckInTime(now);

        // 判断迟到：9:00以后签到算迟到
        LocalTime morningStart = LocalTime.of(9, 0);
        if (now.toLocalTime().isAfter(morningStart)) {
            record.setStatus("LATE");
        } else {
            record.setStatus("NORMAL");
        }

        if (existing == null) {
            recordMapper.insert(record);
        } else {
            recordMapper.updateById(record);
        }

        return record;
    }

    @Override
    public void checkOut(Long userId) {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = recordMapper.selectTodayRecord(userId, today);
        if (record == null || record.getCheckInTime() == null) {
            throw new BusinessException(ErrorCode.ATTENDANCE_NOT_CHECKED_IN, "尚未签到，请先签到");
        }
        if (record.getCheckOutTime() != null) {
            throw new BusinessException(ErrorCode.ATTENDANCE_DUPLICATE, "今日已签退，请勿重复打卡");
        }

        LocalDateTime now = LocalDateTime.now();
        record.setCheckOutTime(now);

        // 计算工作时长
        long minutes = java.time.Duration.between(record.getCheckInTime(), now).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes / 60.0).setScale(2, BigDecimal.ROUND_HALF_UP);
        record.setWorkHours(hours);

        // 判断早退：17:30之前签退算早退
        LocalTime eveningEnd = LocalTime.of(17, 30);
        if (now.toLocalTime().isBefore(eveningEnd) && "NORMAL".equals(record.getStatus())) {
            record.setStatus("EARLY_ABSENTEE");
        }

        recordMapper.updateById(record);
    }

    @Override
    public IPage<AttendanceRecord> getRecords(AttendanceQueryDTO queryDTO) {
        Page<AttendanceRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<AttendanceRecord> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO.getUserId() != null) {
            wrapper.eq(AttendanceRecord::getUserId, queryDTO.getUserId());
        }
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(AttendanceRecord::getDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(AttendanceRecord::getDate, queryDTO.getEndDate());
        }
        if (queryDTO.getStatus() != null && !queryDTO.getStatus().isEmpty()) {
            wrapper.eq(AttendanceRecord::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(AttendanceRecord::getDate);
        return recordMapper.selectPage(page, wrapper);
    }

    @Override
    public AttendanceRecord getTodayRecord(Long userId) {
        return recordMapper.selectTodayRecord(userId, LocalDate.now());
    }

    @Override
    public List<AttendanceRecord> getMonthlyRecords(Long userId, LocalDate month) {
        LocalDate start = month.withDayOfMonth(1);
        LocalDate end = month.withDayOfMonth(month.lengthOfMonth());
        LambdaQueryWrapper<AttendanceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceRecord::getUserId, userId)
                .ge(AttendanceRecord::getDate, start)
                .le(AttendanceRecord::getDate, end)
                .orderByDesc(AttendanceRecord::getDate);
        return recordMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> getMonthSummary(Long userId, LocalDate month) {
        List<AttendanceRecord> records = getMonthlyRecords(userId, month);
        long totalDays = records.size();
        long normalDays = records.stream().filter(r -> "NORMAL".equals(r.getStatus())).count();
        long lateDays = records.stream().filter(r -> "LATE".equals(r.getStatus())).count();
        long absentDays = records.stream().filter(r -> "ABSENT".equals(r.getStatus())).count();
        long earlyDays = records.stream().filter(r -> "EARLY_ABSENTEE".equals(r.getStatus())).count();

        BigDecimal totalHours = records.stream()
                .map(AttendanceRecord::getWorkHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("month", month.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        summary.put("totalDays", totalDays);
        summary.put("normalDays", normalDays);
        summary.put("lateDays", lateDays);
        summary.put("absentDays", absentDays);
        summary.put("earlyDays", earlyDays);
        summary.put("totalHours", totalHours);
        return summary;
    }
}
