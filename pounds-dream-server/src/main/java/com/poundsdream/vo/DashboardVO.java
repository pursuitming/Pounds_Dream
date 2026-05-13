package com.poundsdream.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardVO {

    private BigDecimal todayWeight;
    private BigDecimal weightChange;
    private BigDecimal targetWeight;
    private BigDecimal bmi;
    private Integer todayCalorieIntake;
    private Integer todayCalorieBurned;
    private Integer targetCalorie;
    private Integer streakDays;
    private List<WeightRecordVO> weekWeightTrend;
    private Map<String, Integer> todayMeals;

    // 3D Body Avatar 相关字段
    private BigDecimal height;
    private Integer gender;
    private BigDecimal bodyFat;
    private BigDecimal chest;
    private BigDecimal waist;
    private BigDecimal hip;
    private BigDecimal upperArm;
    private BigDecimal thigh;
}
