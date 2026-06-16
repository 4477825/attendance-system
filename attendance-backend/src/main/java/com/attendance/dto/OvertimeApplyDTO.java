package com.attendance.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 加班申请DTO
 */
@Data
public class OvertimeApplyDTO {

    @NotNull(message = "加班日期不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private String reason;
}
