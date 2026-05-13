package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("t_training_plan_item")
public class TrainingPlanItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long planId;

    private Integer dayOfWeek;

    private Long exerciseTypeId;

    private Integer sets;

    private Integer reps;

    private Integer duration;

    private String note;

    @TableField(exist = false)
    private String exerciseTypeName;

    @TableField(exist = false)
    private String exerciseCategory;
}
