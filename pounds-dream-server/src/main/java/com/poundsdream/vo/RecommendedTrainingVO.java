package com.poundsdream.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RecommendedTrainingVO {

    private BigDecimal tdee;
    private BigDecimal todayIntake;
    private BigDecimal targetCalorie;
    private BigDecimal calorieToBurn;
    private List<RecommendedExercise> exercises;
    private int totalDuration;
    private String intensity; // LOW, MEDIUM, HIGH

    @Data
    @Builder
    public static class RecommendedExercise {
        private Long exerciseTypeId;
        private String name;
        private String category;
        private int duration; // 分钟
        private BigDecimal calorieBurn;
        private BigDecimal metValue;
        private String description;
        private int priority; // 优先级，1=必选，2=推荐，3=可选
    }
}
