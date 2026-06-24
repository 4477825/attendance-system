package com.attendance.controller;

import com.attendance.common.Result;
import com.attendance.entity.User;
import com.attendance.security.JwtTokenProvider;
import com.attendance.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户接口", description = "用户信息管理相关接口")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息")
    public Result<User> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.getUserByUsername(username);
        return Result.success(user);
    }

    @PutMapping("/profile")
    @Operation(summary = "更新用户信息")
    public Result<Void> updateProfile(@RequestBody User user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User existing = userService.getUserByUsername(username);
        user.setId(existing.getId());
        userService.updateProfile(user);
        return Result.successMessage("更新成功");
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtTokenProvider.blacklistToken(token);
        }
        return Result.successMessage("登出成功");
    }

    @GetMapping
    @Operation(summary = "获取所有用户列表")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<java.util.List<User>> listUsers() {
        return Result.success(userService.getAllUsers());
    }

    @PostMapping
    @Operation(summary = "新增用户（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> createUser(@RequestBody User user) {
        return Result.success(userService.createUser(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        userService.updateUser(id, user);
        return Result.successMessage("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户（管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.successMessage("删除成功");
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "启用/禁用用户")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return Result.successMessage("操作成功");
    }
}
