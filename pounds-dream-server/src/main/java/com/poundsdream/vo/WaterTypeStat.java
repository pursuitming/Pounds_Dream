package com.poundsdream.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterTypeStat {

    private String drinkType;
    private String typeName;
    private Integer totalAmount;
    private Integer percentage;
}
