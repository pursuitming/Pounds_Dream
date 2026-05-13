package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("t_exercise_type")
public class ExerciseType {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String category;

    private BigDecimal metValue;

    private BigDecimal caloriePerMin;

    private String description;
}
