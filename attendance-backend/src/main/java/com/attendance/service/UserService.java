package com.attendance.service;

import com.attendance.dto.LoginRequest;
import com.attendance.dto.LoginResponse;
import com.attendance.entity.User;

public interface UserService {

    void register(User user);

    LoginResponse login(LoginRequest request);

    User getUserById(Long userId);

    User getUserByUsername(String username);

    void updateProfile(User user);

    User getUserProfile(Long userId);

    java.util.List<User> getAllUsers();
}
