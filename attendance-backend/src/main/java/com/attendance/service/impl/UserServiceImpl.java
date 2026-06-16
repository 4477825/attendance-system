package com.attendance.service.impl;

import com.attendance.common.ErrorCode;
import com.attendance.common.exception.BusinessException;
import com.attendance.dto.LoginRequest;
import com.attendance.dto.LoginResponse;
import com.attendance.entity.Department;
import com.attendance.entity.User;
import com.attendance.mapper.DepartmentMapper;
import com.attendance.mapper.UserMapper;
import com.attendance.security.JwtTokenProvider;
import com.attendance.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserServiceImpl(UserMapper userMapper, DepartmentMapper departmentMapper,
                           PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void register(User user) {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS, "用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_EMPLOYEE");
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_WRONG, "密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "用户已被禁用");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getRole());
        return new LoginResponse(token, user);
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public void updateProfile(User user) {
        User existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        if (user.getEmail() != null) existing.setEmail(user.getEmail());
        if (user.getPhone() != null) existing.setPhone(user.getPhone());
        if (user.getRealName() != null) existing.setRealName(user.getRealName());
        if (user.getAvatarUrl() != null) existing.setAvatarUrl(user.getAvatarUrl());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.updateById(existing);
    }

    @Override
    public User getUserProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null && user.getDepartmentId() != null) {
            Department dept = departmentMapper.selectById(user.getDepartmentId());
            if (dept != null) {
                user.setDepartmentName(dept.getName());
            }
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }
}
