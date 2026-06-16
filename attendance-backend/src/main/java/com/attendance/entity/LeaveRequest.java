package com.attendance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("leave_request")
public class LeaveRequest implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String leaveType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String reason;

    private String attachmentUrl;

    private String status;

    private Long approverId;

    private LocalDateTime approveTime;

    private String approveRemark;

    private LocalDateTime createdAt;
}
