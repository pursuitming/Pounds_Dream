package com.poundsdream.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveTemplateDTO {

    @NotBlank(message = "模版名称不能为空")
    private String name;

    @NotNull(message = "餐次不能为空")
    private Integer mealType;
}
