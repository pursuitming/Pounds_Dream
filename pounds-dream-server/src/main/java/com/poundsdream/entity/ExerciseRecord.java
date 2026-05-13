package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_exercise_record")
public class ExerciseRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long exerciseTypeId;

    private Integer duration;

    private Integer calorieBurned;

    private Integer heartRate;

    private LocalDate recordDate;

    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String exerciseName;

    @TableField(exist = false)
    private String exerciseCategory;
}
