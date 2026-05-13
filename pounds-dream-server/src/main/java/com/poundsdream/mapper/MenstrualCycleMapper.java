package com.poundsdream.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poundsdream.entity.MenstrualCycle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MenstrualCycleMapper extends BaseMapper<MenstrualCycle> {

    @Select("SELECT * FROM t_menstrual_cycle WHERE user_id = #{userId} ORDER BY start_date DESC")
    List<MenstrualCycle> selectAllByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM t_menstrual_cycle WHERE user_id = #{userId} AND start_date <= #{date} AND (end_date IS NULL OR end_date >= #{date}) LIMIT 1")
    MenstrualCycle selectByDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT * FROM t_menstrual_cycle WHERE user_id = #{userId} ORDER BY start_date DESC LIMIT 1")
    MenstrualCycle selectLatestByUserId(@Param("userId") Long userId);
}
