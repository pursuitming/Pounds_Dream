package com.poundsdream.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class HealthProfileVO {

    private BigDecimal bmr;
    private BigDecimal tdee;
    private BigDecimal targetCalorie;
    private BigDecimal carbGram;
    private BigDecimal proteinGram;
    private BigDecimal fatGram;
    private BigDecimal bmi;
    private String bmiCategory;
}
