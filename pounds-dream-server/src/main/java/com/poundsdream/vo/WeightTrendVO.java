package com.poundsdream.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class WeightTrendVO {

    private List<WeightRecordVO> records;
    private BigDecimal startWeight;
    private BigDecimal currentWeight;
    private BigDecimal totalChange;
    private BigDecimal averageChange;
}
