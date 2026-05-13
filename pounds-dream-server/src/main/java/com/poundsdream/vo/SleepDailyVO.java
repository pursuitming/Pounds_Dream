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
public class SleepDailyVO {

    private Integer lastDuration;
    private Integer lastQuality;
    private String lastTags;
    private Integer dailyGoal;
    private Integer percentage;
    private Integer score;
    private Integer streak;
    private List<SleepRecordVO> records;
    private List<SleepDayStat> recentDays;
}
