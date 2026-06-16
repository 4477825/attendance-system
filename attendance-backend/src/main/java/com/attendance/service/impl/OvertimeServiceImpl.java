package com.attendance.service.impl;

import com.attendance.common.ErrorCode;
import com.attendance.common.exception.BusinessException;
import com.attendance.dto.OvertimeApplyDTO;
import com.attendance.entity.OvertimeRecord;
import com.attendance.mapper.OvertimeRecordMapper;
import com.attendance.service.OvertimeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
public class OvertimeServiceImpl implements OvertimeService {

    private final OvertimeRecordMapper overtimeRecordMapper;

    public OvertimeServiceImpl(OvertimeRecordMapper overtimeRecordMapper) {
        this.overtimeRecordMapper = overtimeRecordMapper;
    }

    @Override
    public OvertimeRecord applyOvertime(Long userId, OvertimeApplyDTO dto) {
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "结束时间不能早于开始时间");
        }

        Duration duration = Duration.between(dto.getStartTime(), dto.getEndTime());
        double hours = duration.toMinutes() / 60.0;

        OvertimeRecord record = new OvertimeRecord();
        record.setUserId(userId);
        record.setOvertimeDate(dto.getStartTime().toLocalDate());
        record.setStartTime(dto.getStartTime());
        record.setEndTime(dto.getEndTime());
        record.setDurationHours(BigDecimal.valueOf(Math.round(hours * 100.0) / 100.0));
        record.setReason(dto.getReason());
        record.setStatus("PENDING");
        record.setCreatedAt(LocalDateTime.now());

        overtimeRecordMapper.insert(record);
        return record;
    }

    @Override
    public IPage<OvertimeRecord> getOvertimeList(Long userId, String status, int pageNum, int pageSize, boolean isAdmin) {
        Page<OvertimeRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OvertimeRecord> wrapper = new LambdaQueryWrapper<>();

        if (!isAdmin) {
            wrapper.eq(OvertimeRecord::getUserId, userId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(OvertimeRecord::getStatus, status);
        }

        wrapper.orderByDesc(OvertimeRecord::getCreatedAt);
        return overtimeRecordMapper.selectPage(page, wrapper);
    }

    @Override
    public OvertimeRecord getOvertimeById(Long id) {
        OvertimeRecord record = overtimeRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.OVERTIME_NOT_FOUND, "加班记录不存在");
        }
        return record;
    }

    @Override
    public OvertimeRecord approveOvertime(Long id, Long approverId, String approveRemark, String status) {
        OvertimeRecord record = overtimeRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(ErrorCode.OVERTIME_NOT_FOUND, "加班记录不存在");
        }
        if (!"PENDING".equals(record.getStatus())) {
            throw new BusinessException(ErrorCode.LEAVE_STATUS_ERROR, "该申请已审批");
        }

        record.setStatus(status);
        record.setApproverId(approverId);
        overtimeRecordMapper.updateById(record);
        return record;
    }

    @Override
    public List<OvertimeRecord> getUserOvertimes(Long userId) {
        LambdaQueryWrapper<OvertimeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OvertimeRecord::getUserId, userId)
                .orderByDesc(OvertimeRecord::getCreatedAt);
        return overtimeRecordMapper.selectList(wrapper);
    }
}
