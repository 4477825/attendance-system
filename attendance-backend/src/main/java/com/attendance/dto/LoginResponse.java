package com.attendance.dto;

import com.attendance.entity.User;
import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginResponse {

    private String token;
    private User user;

    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }
}
