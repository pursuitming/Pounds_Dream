package com.poundsdream.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenstrualCycleVO {

    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer cycleLength;
    private Integer periodLength;
    private String symptoms;
    private String mood;
    private String note;

    // 预测信息
    private LocalDate nextPredictedDate;
    private Integer daysUntilNext;
    private String currentPhase;
}
