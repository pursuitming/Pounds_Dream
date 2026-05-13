package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_meal_status")
public class MealStatus {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate mealDate;

    private Integer mealType;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime updatedAt;
}
