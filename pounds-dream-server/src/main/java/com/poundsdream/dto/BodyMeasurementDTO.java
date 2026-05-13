package com.poundsdream.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BodyMeasurementDTO {

    private BigDecimal waist;

    private BigDecimal hip;

    private BigDecimal chest;

    private BigDecimal upperArm;

    private BigDecimal thigh;

    private LocalDate recordDate;
}
