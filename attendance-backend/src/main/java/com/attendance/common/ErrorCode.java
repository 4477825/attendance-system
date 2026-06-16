package com.attendance.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    USERNAME_EXISTS(1001, "用户名已存在"),
    PASSWORD_WRONG(1002, "密码错误"),
    USER_NOT_FOUND(1003, "用户不存在"),
    USER_DISABLED(1004, "用户已被禁用"),

    TOKEN_EXPIRED(2001, "Token已过期"),
    TOKEN_INVALID(2002, "Token无效"),

    ATTENDANCE_DUPLICATE(3001, "今日已打卡"),
    ATTENDANCE_NOT_CHECKED_IN(3002, "尚未签到"),

    LEAVE_NOT_FOUND(4001, "请假申请不存在"),
    LEAVE_STATUS_ERROR(4002, "请假状态异常"),

    OVERTIME_NOT_FOUND(5001, "加班记录不存在"),

    FILE_UPLOAD_FAILED(6001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORTED(6002, "不支持的文件类型");

    private final int code;
    private final String message;
}
