package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_diet_record")
public class DietRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long foodId;

    private Integer mealType;

    private BigDecimal amount;

    private Integer calorie;

    private LocalDate recordDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String foodName;

    @TableField(exist = false)
    private BigDecimal protein;

    @TableField(exist = false)
    private BigDecimal fat;

    @TableField(exist = false)
    private BigDecimal carbohydrate;
}
