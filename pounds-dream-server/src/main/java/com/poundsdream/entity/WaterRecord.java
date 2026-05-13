package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_water_record")
public class WaterRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer amount;

    private String drinkType;

    private LocalDateTime recordTime;

    private LocalDate recordDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
