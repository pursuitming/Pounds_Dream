package com.poundsdream.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class WeightRecordVO {

    private Long id;
    private BigDecimal weight;
    private BigDecimal bodyFat;
    private BigDecimal bmi;
    private LocalDate recordDate;
    private String note;
}
