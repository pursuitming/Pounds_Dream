package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_body_measurement")
public class BodyMeasurement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private BigDecimal waist;

    private BigDecimal hip;

    private BigDecimal chest;

    private BigDecimal upperArm;

    private BigDecimal thigh;

    private LocalDate recordDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
