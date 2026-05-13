package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("t_food")
public class Food {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String category;

    private Integer calorie;

    private BigDecimal protein;

    private BigDecimal fat;

    private BigDecimal carbohydrate;

    private BigDecimal fiber;

    private BigDecimal gi;

    private String unit;

    private BigDecimal unitWeight;

    private Integer isCustom;

    private Long creatorId;
}
