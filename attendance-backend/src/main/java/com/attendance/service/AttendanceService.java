package com.attendance.service;

import com.attendance.dto.AttendanceQueryDTO;
import com.attendance.entity.AttendanceRecord;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.time.LocalDate;

public interface AttendanceService {

    AttendanceRecord checkIn(Long userId);

    void checkOut(Long userId);

    IPage<AttendanceRecord> getRecords(AttendanceQueryDTO queryDTO);

    AttendanceRecord getTodayRecord(Long userId);

    java.util.List<AttendanceRecord> getMonthlyRecords(Long userId, LocalDate month);

    java.util.Map<String, Object> getMonthSummary(Long userId, LocalDate month);
}
