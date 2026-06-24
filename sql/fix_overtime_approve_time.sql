-- ============================================================
-- Add approve_time column to overtime_record table
-- ============================================================

USE attendance_db;

ALTER TABLE overtime_record
ADD COLUMN approve_time DATETIME COMMENT '审批时间' AFTER approve_remark;
