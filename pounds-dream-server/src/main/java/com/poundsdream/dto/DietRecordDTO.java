package com.poundsdream.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DietRecordDTO {

    @NotNull(message = "食物ID不能为空")
    private Long foodId;

    @NotNull(message = "餐次不能为空")
    private Integer mealType;

    @NotNull(message = "食用量不能为空")
    private BigDecimal amount;

    private LocalDate recordDate;
}
