package com.poundsdream.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WeightDTO {

    @NotNull(message = "体重不能为空")
    private BigDecimal weight;

    private BigDecimal bodyFat;

    private LocalDate recordDate;

    private String note;
}
