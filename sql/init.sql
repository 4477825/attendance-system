-- 员工考勤管理系统数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS attendance_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE attendance_db;

-- 部门表
DROP TABLE IF EXISTS sys_department;
CREATE TABLE sys_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(100) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT NULL COMMENT '上级部门ID',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_department_parent FOREIGN KEY (parent_id) REFERENCES sys_department(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '加密密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    department_id BIGINT COMMENT '所属部门ID',
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_EMPLOYEE' COMMENT '角色 ROLE_ADMIN/ROLE_EMPLOYEE',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_user_department FOREIGN KEY (department_id) REFERENCES sys_department(id),
    INDEX idx_username (username),
    INDEX idx_department (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 考勤记录表
DROP TABLE IF EXISTS attendance_record;
CREATE TABLE attendance_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    check_in_time DATETIME COMMENT '签到时间',
    check_out_time DATETIME COMMENT '签退时间',
    date DATE NOT NULL COMMENT '考勤日期',
    status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态 NORMAL/LATE/EARLY_ABSENTEE/ABSENT',
    work_hours DECIMAL(5,2) COMMENT '工作时长(小时)',
    remark VARCHAR(255) COMMENT '备注',
    CONSTRAINT fk_record_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    INDEX idx_user_date (user_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录表';

-- 请假申请表
DROP TABLE IF EXISTS leave_request;
CREATE TABLE leave_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '申请人ID',
    leave_type VARCHAR(20) NOT NULL COMMENT '年假/事假/病假/调休',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    reason VARCHAR(500) COMMENT '请假事由',
    attachment_url VARCHAR(500) COMMENT '附件URL',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    approver_id BIGINT COMMENT '审批人ID',
    approve_time DATETIME COMMENT '审批时间',
    approve_remark VARCHAR(255) COMMENT '审批意见',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_leave_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_leave_approver FOREIGN KEY (approver_id) REFERENCES sys_user(id),
    INDEX idx_leave_user (user_id),
    INDEX idx_leave_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- 加班记录表
DROP TABLE IF EXISTS overtime_record;
CREATE TABLE overtime_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    overtime_date DATE NOT NULL COMMENT '加班日期',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    duration_hours DECIMAL(5,2) COMMENT '加班时长',
    reason VARCHAR(500) COMMENT '加班事由',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    approver_id BIGINT COMMENT '审批人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_overtime_user FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT fk_overtime_approver FOREIGN KEY (approver_id) REFERENCES sys_user(id),
    INDEX idx_overtime_user (user_id),
    INDEX idx_overtime_date (overtime_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加班记录表';

-- 插入初始部门数据
INSERT INTO sys_department (name, parent_id, sort_order) VALUES ('总公司', NULL, 0);
INSERT INTO sys_department (name, parent_id, sort_order) VALUES ('技术研发部', 1, 1);
INSERT INTO sys_department (name, parent_id, sort_order) VALUES ('人力资源部', 1, 2);
INSERT INTO sys_department (name, parent_id, sort_order) VALUES ('财务部', 1, 3);
INSERT INTO sys_department (name, parent_id, sort_order) VALUES ('市场部', 1, 4);

-- 插入初始管理员账户 (密码: admin123 经BCrypt加密)
INSERT INTO sys_user (username, password, real_name, email, phone, department_id, role, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@attendance.com', '13800138000', 1, 'ROLE_ADMIN', 1);

-- 插入测试员工账户 (密码: employee123 经BCrypt加密)
INSERT INTO sys_user (username, password, real_name, email, phone, department_id, role, status)
VALUES ('employee', '$2a$10$wNAhtxx0x5WjTGpQNDiWu.0yEILpRRZzYMhSmLg4KFmqwZS5VHBqG', '测试员工', 'employee@attendance.com', '13800138001', 2, 'ROLE_EMPLOYEE', 1);
