package com.attendance.controller;

import com.alibaba.excel.EasyExcel;
import com.attendance.entity.LeaveRequest;
import com.attendance.entity.OvertimeRecord;
import com.attendance.entity.User;
import com.attendance.mapper.LeaveRequestMapper;
import com.attendance.mapper.OvertimeRecordMapper;
import com.attendance.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/export")
@Tag(name = "导出接口", description = "数据导出为Excel")
public class ExportController {

    private final LeaveRequestMapper leaveRequestMapper;
    private final OvertimeRecordMapper overtimeRecordMapper;
    private final UserMapper userMapper;

    public ExportController(LeaveRequestMapper leaveRequestMapper,
                            OvertimeRecordMapper overtimeRecordMapper,
                            UserMapper userMapper) {
        this.leaveRequestMapper = leaveRequestMapper;
        this.overtimeRecordMapper = overtimeRecordMapper;
        this.userMapper = userMapper;
    }

    @GetMapping("/leave")
    @Operation(summary = "导出请假记录")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportLeave(HttpServletResponse response) throws IOException {
        Map<Long, String> userMap = getUserMap();
        List<LeaveRequest> records = leaveRequestMapper.selectList(null);

        List<List<String>> head = List.of(
                List.of("ID"), List.of("申请人"), List.of("类型"),
                List.of("开始时间"), List.of("结束时间"), List.of("事由"),
                List.of("状态"), List.of("审批人"), List.of("审批意见"),
                List.of("审批时间"), List.of("创建时间")
        );

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<List<Object>> data = records.stream().map(r -> {
            List<Object> row = new ArrayList<>();
            row.add(r.getId());
            row.add(userMap.getOrDefault(r.getUserId(), "-"));
            row.add(r.getLeaveType());
            row.add(r.getStartTime() != null ? r.getStartTime().format(dtf) : "-");
            row.add(r.getEndTime() != null ? r.getEndTime().format(dtf) : "-");
            row.add(r.getReason());
            row.add(statusLabel(r.getStatus()));
            row.add(r.getApproverId() != null ? userMap.getOrDefault(r.getApproverId(), "-") : "-");
            row.add(r.getApproveRemark() != null ? r.getApproveRemark() : "-");
            row.add(r.getApproveTime() != null ? r.getApproveTime().format(dtf) : "-");
            row.add(r.getCreatedAt() != null ? r.getCreatedAt().format(dtf) : "-");
            return row;
        }).collect(Collectors.toList());

        writeExcel(response, "请假记录", head, data);
    }

    @GetMapping("/overtime")
    @Operation(summary = "导出加班记录")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportOvertime(HttpServletResponse response) throws IOException {
        Map<Long, String> userMap = getUserMap();
        List<OvertimeRecord> records = overtimeRecordMapper.selectList(null);

        List<List<String>> head = List.of(
                List.of("ID"), List.of("申请人"), List.of("日期"),
                List.of("开始时间"), List.of("结束时间"), List.of("时长(小时)"),
                List.of("事由"), List.of("状态"), List.of("审批人"),
                List.of("审批意见"), List.of("审批时间"), List.of("创建时间")
        );

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<List<Object>> data = records.stream().map(r -> {
            List<Object> row = new ArrayList<>();
            row.add(r.getId());
            row.add(userMap.getOrDefault(r.getUserId(), "-"));
            row.add(r.getOvertimeDate() != null ? r.getOvertimeDate().format(df) : "-");
            row.add(r.getStartTime() != null ? r.getStartTime().format(dtf) : "-");
            row.add(r.getEndTime() != null ? r.getEndTime().format(dtf) : "-");
            row.add(r.getDurationHours() != null ? r.getDurationHours().doubleValue() : 0);
            row.add(r.getReason());
            row.add(statusLabel(r.getStatus()));
            row.add(r.getApproverId() != null ? userMap.getOrDefault(r.getApproverId(), "-") : "-");
            row.add(r.getApproveRemark() != null ? r.getApproveRemark() : "-");
            row.add(r.getApproveTime() != null ? r.getApproveTime().format(dtf) : "-");
            row.add(r.getCreatedAt() != null ? r.getCreatedAt().format(dtf) : "-");
            return row;
        }).collect(Collectors.toList());

        writeExcel(response, "加班记录", head, data);
    }

    private Map<Long, String> getUserMap() {
        return userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId,
                        u -> u.getRealName() != null ? u.getRealName() : u.getUsername(),
                        (a, b) -> a));
    }

    private String statusLabel(String status) {
        if (status == null) return "-";
        switch (status) {
            case "PENDING": return "待审批";
            case "APPROVED": return "已通过";
            case "REJECTED": return "已拒绝";
            default: return status;
        }
    }

    private void writeExcel(HttpServletResponse response, String sheetName,
                            List<List<String>> head, List<List<Object>> data) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(sheetName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream())
                .head(head)
                .sheet(sheetName)
                .doWrite(data);
    }
}
