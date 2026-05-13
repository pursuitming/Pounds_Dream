package com.poundsdream.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("t_sleep_record")
public class SleepRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDateTime bedTime;

    private LocalDateTime wakeTime;

    private Integer duration;

    private Integer quality;

    private String tags;

    private Integer isNap;

    private LocalDate recordDate;

    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
