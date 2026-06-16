package com.attendance.service;

import com.attendance.dto.LeaveApplyDTO;
import com.attendance.entity.LeaveRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface LeaveService {

    LeaveRequest applyLeave(Long userId, LeaveApplyDTO dto);

    IPage<LeaveRequest> getLeaveList(Long userId, String status, int pageNum, int pageSize, boolean isAdmin);

    LeaveRequest getLeaveById(Long id);

    LeaveRequest approveLeave(Long id, Long approverId, String approveRemark, String status);

    List<LeaveRequest> getUserLeaves(Long userId);
}
