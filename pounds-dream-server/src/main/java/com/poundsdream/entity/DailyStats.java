package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_daily_stats")
public class DailyStats {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate statDate;

    private Integer totalIntake;

    private Integer totalBurned;

    private Integer targetCalorie;

    private BigDecimal protein;

    private BigDecimal fat;

    private BigDecimal carbohydrate;

    private Integer dietCount;

    private Integer exerciseCount;

    private BigDecimal weight;

    private Integer isGoalMet;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
