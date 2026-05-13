package com.poundsdream.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SleepRecommendationVO {

    private Integer recommendedDuration;
    private Integer exerciseDuration;
    private String exerciseLevel;
    private String reason;
}
