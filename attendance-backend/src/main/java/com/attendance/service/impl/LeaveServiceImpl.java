package com.attendance.service.impl;

import com.attendance.common.ErrorCode;
import com.attendance.common.exception.BusinessException;
import com.attendance.dto.LeaveApplyDTO;
import com.attendance.entity.LeaveRequest;
import com.attendance.mapper.LeaveRequestMapper;
import com.attendance.service.LeaveService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestMapper leaveRequestMapper;

    public LeaveServiceImpl(LeaveRequestMapper leaveRequestMapper) {
        this.leaveRequestMapper = leaveRequestMapper;
    }

    @Override
    public LeaveRequest applyLeave(Long userId, LeaveApplyDTO dto) {
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "结束时间不能早于开始时间");
        }

        LeaveRequest leave = new LeaveRequest();
        leave.setUserId(userId);
        leave.setLeaveType(dto.getLeaveType());
        leave.setStartTime(dto.getStartTime());
        leave.setEndTime(dto.getEndTime());
        leave.setReason(dto.getReason());
        leave.setAttachmentUrl(dto.getAttachmentUrl());
        leave.setStatus("PENDING");
        leave.setCreatedAt(LocalDateTime.now());

        leaveRequestMapper.insert(leave);
        return leave;
    }

    @Override
    public IPage<LeaveRequest> getLeaveList(Long userId, String status, int pageNum, int pageSize, boolean isAdmin) {
        Page<LeaveRequest> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LeaveRequest> wrapper = new LambdaQueryWrapper<>();

        if (!isAdmin) {
            wrapper.eq(LeaveRequest::getUserId, userId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(LeaveRequest::getStatus, status);
        }

        wrapper.orderByDesc(LeaveRequest::getCreatedAt);
        return leaveRequestMapper.selectPage(page, wrapper);
    }

    @Override
    public LeaveRequest getLeaveById(Long id) {
        LeaveRequest leave = leaveRequestMapper.selectById(id);
        if (leave == null) {
            throw new BusinessException(ErrorCode.LEAVE_NOT_FOUND, "请假申请不存在");
        }
        return leave;
    }

    @Override
    public LeaveRequest approveLeave(Long id, Long approverId, String approveRemark, String status) {
        LeaveRequest leave = leaveRequestMapper.selectById(id);
        if (leave == null) {
            throw new BusinessException(ErrorCode.LEAVE_NOT_FOUND, "请假申请不存在");
        }
        if (!"PENDING".equals(leave.getStatus())) {
            throw new BusinessException(ErrorCode.LEAVE_STATUS_ERROR, "该申请已审批");
        }

        leave.setStatus(status);
        leave.setApproverId(approverId);
        leave.setApproveRemark(approveRemark);
        leave.setApproveTime(LocalDateTime.now());
        leaveRequestMapper.updateById(leave);
        return leave;
    }

    @Override
    public List<LeaveRequest> getUserLeaves(Long userId) {
        LambdaQueryWrapper<LeaveRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LeaveRequest::getUserId, userId)
                .orderByDesc(LeaveRequest::getCreatedAt);
        return leaveRequestMapper.selectList(wrapper);
    }
}
