package com.poundsdream.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SleepRecordDTO {

    private Long id;

    @NotNull(message = "上床时间不能为空")
    private LocalDateTime bedTime;

    @NotNull(message = "起床时间不能为空")
    private LocalDateTime wakeTime;

    private Integer quality;

    private String tags;

    private Boolean isNap;

    private String note;
}
