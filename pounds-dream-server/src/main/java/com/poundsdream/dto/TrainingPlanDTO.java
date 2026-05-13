package com.poundsdream.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class TrainingPlanDTO {

    @NotBlank(message = "计划名称不能为空")
    private String name;

    private String description;

    private List<TrainingPlanItemDTO> items;

    @Data
    public static class TrainingPlanItemDTO {
        private Integer dayOfWeek;
        private Long exerciseTypeId;
        private Integer sets;
        private Integer reps;
        private Integer duration;
        private String note;
    }
}
