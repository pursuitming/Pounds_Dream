package com.poundsdream.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poundsdream.entity.WaterRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface WaterRecordMapper extends BaseMapper<WaterRecord> {

    @Select("SELECT COALESCE(SUM(amount), 0) FROM t_water_record WHERE user_id = #{userId} AND record_date = #{date}")
    int sumAmountByDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT record_date AS recordDate, COALESCE(SUM(amount), 0) AS totalAmount " +
            "FROM t_water_record WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY record_date ORDER BY record_date")
    List<Map<String, Object>> sumAmountByDateRange(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    @Select("SELECT drink_type AS drinkType, COALESCE(SUM(amount), 0) AS totalAmount " +
            "FROM t_water_record WHERE user_id = #{userId} AND record_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY drink_type")
    List<Map<String, Object>> sumAmountByDrinkType(@Param("userId") Long userId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);
}
