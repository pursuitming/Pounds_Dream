package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_menstrual_cycle")
public class MenstrualCycle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer cycleLength;

    private Integer periodLength;

    private String symptoms;

    private String mood;

    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
