package com.attendance.controller;

import com.attendance.common.Result;
import com.attendance.entity.Department;
import com.attendance.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@Tag(name = "部门接口", description = "部门管理相关接口")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @Operation(summary = "获取部门列表")
    public Result<List<Department>> listDepartments() {
        return Result.success(departmentService.listDepartments());
    }

    @PostMapping
    @Operation(summary = "新增部门")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Department> createDepartment(@RequestBody Department department) {
        return Result.success(departmentService.createDepartment(department));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新部门")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Department> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        return Result.success(departmentService.updateDepartment(id, department));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return Result.successMessage("删除成功");
    }
}
