package com.poundsdream.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterRecordVO {

    private Long id;
    private Integer amount;
    private String drinkType;
    private LocalDateTime recordTime;
}
