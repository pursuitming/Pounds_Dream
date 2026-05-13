package com.poundsdream.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poundsdream.entity.SleepRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface SleepRecordMapper extends BaseMapper<SleepRecord> {

    @Select("SELECT record_date AS recordDate, COALESCE(SUM(duration), 0) AS totalDuration, " +
            "COALESCE(AVG(quality), 0) AS avgQuality, COUNT(*) AS recordCount " +
            "FROM t_sleep_record WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY record_date ORDER BY record_date")
    List<Map<String, Object>> statByDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Select("SELECT DISTINCT record_date FROM t_sleep_record WHERE user_id = #{userId} " +
            "ORDER BY record_date DESC")
    List<LocalDate> selectDistinctDates(@Param("userId") Long userId);

    @Select("SELECT COALESCE(SUM(duration), 0) FROM t_exercise_record " +
            "WHERE user_id = #{userId} AND record_date = #{date}")
    Integer sumExerciseDurationByDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT bed_time, wake_time FROM t_sleep_record WHERE user_id = #{userId} " +
            "AND record_date BETWEEN #{startDate} AND #{endDate} AND is_nap = 0 " +
            "ORDER BY record_date")
    List<Map<String, Object>> selectBedWakeTimes(@Param("userId") Long userId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
}
