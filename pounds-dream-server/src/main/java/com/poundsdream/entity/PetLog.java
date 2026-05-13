package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_pet_log")
public class PetLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String action;

    private String petMood;

    private Integer happiness;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
