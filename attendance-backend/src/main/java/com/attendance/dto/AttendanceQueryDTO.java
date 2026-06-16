package com.attendance.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 考勤查询DTO
 */
@Data
public class AttendanceQueryDTO {

    private Long userId;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private int pageNum = 1;

    private int pageSize = 10;
}
