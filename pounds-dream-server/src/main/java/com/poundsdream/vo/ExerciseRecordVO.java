package com.poundsdream.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ExerciseRecordVO {

    private Long id;
    private Long exerciseTypeId;
    private String exerciseName;
    private String exerciseCategory;
    private Integer duration;
    private Integer calorieBurned;
    private Integer heartRate;
    private LocalDate recordDate;
    private String note;
}
