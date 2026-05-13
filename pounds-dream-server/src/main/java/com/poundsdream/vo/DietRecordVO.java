package com.poundsdream.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class DietRecordVO {

    private Long id;
    private Long foodId;
    private String foodName;
    private Integer mealType;
    private String mealTypeName;
    private BigDecimal amount;
    private Integer calorie;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbohydrate;
    private LocalDate recordDate;
    private String riskLevel;
    private List<String> riskTags;
}
