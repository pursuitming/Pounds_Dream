package com.poundsdream.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ExerciseRecordDTO {

    @NotNull(message = "运动类型ID不能为空")
    private Long exerciseTypeId;

    @NotNull(message = "运动时长不能为空")
    private Integer duration;

    private Integer heartRate;

    private LocalDate recordDate;

    private String note;
}
