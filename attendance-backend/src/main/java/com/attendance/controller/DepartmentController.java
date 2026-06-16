package com.attendance.controller;

import com.attendance.common.Result;
import com.attendance.entity.Department;
import com.attendance.mapper.DepartmentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@Tag(name = "部门接口", description = "部门管理相关接口")
public class DepartmentController {

    private final DepartmentMapper departmentMapper;

    public DepartmentController(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @GetMapping
    @Operation(summary = "获取部门列表")
    public Result<List<Department>> listDepartments() {
        List<Department> departments = departmentMapper.selectList(null);
        return Result.success(departments);
    }
}
