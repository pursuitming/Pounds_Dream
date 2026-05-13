package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_weight_record")
public class WeightRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private BigDecimal weight;

    private BigDecimal bodyFat;

    private BigDecimal bmi;

    private LocalDate recordDate;

    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
