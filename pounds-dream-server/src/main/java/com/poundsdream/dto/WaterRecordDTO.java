package com.poundsdream.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WaterRecordDTO {

    @NotNull(message = "饮水量不能为空")
    @Min(value = 1, message = "饮水量必须大于0")
    private Integer amount;

    private String drinkType;
}
