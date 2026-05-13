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
public class SleepTrendVO {

    private LocalDate date;
    private Integer totalDuration;
    private Double avgQuality;
}
