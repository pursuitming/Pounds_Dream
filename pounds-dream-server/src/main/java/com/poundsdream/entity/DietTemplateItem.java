package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("t_diet_template_item")
public class DietTemplateItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long templateId;

    private Long foodId;

    private BigDecimal amount;

    @TableField(exist = false)
    private String foodName;

    @TableField(exist = false)
    private Integer calorie;
}
