package com.attendance.service;

import com.attendance.dto.OvertimeApplyDTO;
import com.attendance.entity.OvertimeRecord;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface OvertimeService {

    OvertimeRecord applyOvertime(Long userId, OvertimeApplyDTO dto);

    IPage<OvertimeRecord> getOvertimeList(Long userId, String status, int pageNum, int pageSize, boolean isAdmin);

    OvertimeRecord getOvertimeById(Long id);

    OvertimeRecord approveOvertime(Long id, Long approverId, String approveRemark, String status);

    List<OvertimeRecord> getUserOvertimes(Long userId);
}
