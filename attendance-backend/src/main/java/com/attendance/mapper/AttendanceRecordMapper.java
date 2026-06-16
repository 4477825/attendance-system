package com.attendance.mapper;

import com.attendance.entity.AttendanceRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;

@Mapper
public interface AttendanceRecordMapper extends BaseMapper<AttendanceRecord> {

    @Select("SELECT * FROM attendance_record WHERE user_id = #{userId} AND date = #{date} ORDER BY id DESC LIMIT 1")
    AttendanceRecord selectTodayRecord(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT COUNT(*) FROM attendance_record WHERE user_id = #{userId} AND date >= #{startDate}")
    int countAbsentDays(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    @Update("UPDATE attendance_record SET check_out_time = #{checkOutTime}, status = #{status} WHERE id = #{id}")
    int updateCheckOut(@Param("id") Long id, @Param("checkOutTime") java.time.LocalDateTime checkOutTime, @Param("status") String status);
}
