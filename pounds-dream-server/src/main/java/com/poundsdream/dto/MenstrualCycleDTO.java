package com.poundsdream.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MenstrualCycleDTO {

    private Long id;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    private LocalDate endDate;

    private String symptoms;

    private String mood;

    private String note;
}
