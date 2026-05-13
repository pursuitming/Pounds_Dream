package com.poundsdream.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poundsdream.entity.WeightRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WeightRecordMapper extends BaseMapper<WeightRecord> {

    @Select("SELECT * FROM t_weight_record WHERE user_id = #{userId} ORDER BY record_date DESC LIMIT 1")
    WeightRecord selectLatestByUserId(Long userId);
}
