package com.attendance.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    /**
     * 获取部门考勤统计数据
     */
    Map<String, Object> getDeptStatistics(Long deptId, String month);

    /**
     * 获取全员考勤统计
     */
    Map<String, Object> getAllStats(String month);

    /**
     * 获取用户月度考勤详情
     */
    List<Map<String, Object>> getUserMonthlyDetail(Long userId, String month);
}
