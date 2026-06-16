package com.attendance.mapper;

import com.attendance.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    @Select("SELECT u.*, d.name AS department_name FROM sys_user u " +
            "LEFT JOIN sys_department d ON u.department_id = d.id " +
            "WHERE u.department_id = #{deptId}")
    List<User> findByDepartmentId(@Param("deptId") Long deptId);
}
