package com.poundsdream.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterDailyVO {

    private Integer todayAmount;
    private Integer dailyGoal;
    private Integer percentage;
    private Integer remaining;
    private List<WaterRecordVO> records;
    private List<WaterDayStat> recentDays;
}
