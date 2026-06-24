package com.attendance.service.impl;

import com.attendance.common.ErrorCode;
import com.attendance.common.exception.BusinessException;
import com.attendance.entity.Department;
import com.attendance.mapper.DepartmentMapper;
import com.attendance.mapper.UserMapper;
import com.attendance.service.DepartmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final UserMapper userMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper, UserMapper userMapper) {
        this.departmentMapper = departmentMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<Department> listDepartments() {
        return departmentMapper.selectList(null);
    }

    @Override
    public Department createDepartment(Department department) {
        departmentMapper.insert(department);
        return department;
    }

    @Override
    public Department updateDepartment(Long id, Department department) {
        Department existing = departmentMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "部门不存在");
        }
        department.setId(id);
        departmentMapper.updateById(department);
        return departmentMapper.selectById(id);
    }

    @Override
    public void deleteDepartment(Long id) {
        Department existing = departmentMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "部门不存在");
        }
        int userCount = userMapper.findByDepartmentId(id).size();
        if (userCount > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该部门下还有 " + userCount + " 名员工，无法删除");
        }
        departmentMapper.deleteById(id);
    }
}
