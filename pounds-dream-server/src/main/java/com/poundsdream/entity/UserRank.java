package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_user_rank")
public class UserRank {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer points;

    private Integer rankLevel;

    private String rankName;

    private Integer maxStreak;

    private Integer currentStreak;

    private LocalDate lastCheckDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
