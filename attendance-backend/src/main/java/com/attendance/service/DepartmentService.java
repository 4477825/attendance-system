package com.attendance.service;

import com.attendance.entity.Department;

import java.util.List;

public interface DepartmentService {

    List<Department> listDepartments();

    Department createDepartment(Department department);

    Department updateDepartment(Long id, Department department);

    void deleteDepartment(Long id);
}
