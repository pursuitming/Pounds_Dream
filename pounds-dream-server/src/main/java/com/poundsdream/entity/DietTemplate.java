package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_diet_template")
public class DietTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private Integer mealType;

    private Integer totalCalorie;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
