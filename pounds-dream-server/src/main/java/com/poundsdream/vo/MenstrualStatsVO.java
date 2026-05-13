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
public class MenstrualStatsVO {

    private Integer avgCycleLength;
    private Integer avgPeriodLength;
    private Integer minCycleLength;
    private Integer maxCycleLength;
    private List<MenstrualCycleVO> recentCycles;
    private MenstrualCycleVO currentCycle;
}
