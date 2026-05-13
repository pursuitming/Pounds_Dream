package com.poundsdream.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DailyDietVO {

    private Integer totalCalorie;
    private Integer targetCalorie;
    private Integer remainingCalorie;
    private BigDecimal totalProtein;
    private BigDecimal totalFat;
    private BigDecimal totalCarbohydrate;
    private Map<String, List<DietRecordVO>> mealsByType;
}
