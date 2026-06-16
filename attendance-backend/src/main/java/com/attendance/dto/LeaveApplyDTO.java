package com.attendance.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 请假申请DTO
 */
@Data
public class LeaveApplyDTO {

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @NotBlank(message = "请假类型不能为空")
    private String leaveType;

    private String reason;

    private String attachmentUrl;
}
