-- ============================================================
-- Fix for overtime_record table: add missing approve_remark column
-- Issue: SELECT query includes approve_remark but column doesn't exist in DB
-- Execute this in MySQL Workbench / IDEA Database tool / command line
-- ============================================================

USE attendance_db;

-- Add the missing approve_remark column if it does not exist
ALTER TABLE overtime_record 
ADD COLUMN approve_remark VARCHAR(255) COMMENT '审批备注' AFTER approver_id;

