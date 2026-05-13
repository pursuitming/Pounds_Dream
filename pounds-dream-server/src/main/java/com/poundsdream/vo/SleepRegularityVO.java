package com.poundsdream.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SleepRegularityVO {

    private Integer score;
    private String avgBedTime;
    private String avgWakeTime;
    private Integer bedTimeStdDev;
    private Integer wakeTimeStdDev;
}
