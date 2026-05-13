package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_pet")
public class Pet {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String petName;

    private String petType;

    private Integer petLevel;

    private Integer totalHappiness;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
