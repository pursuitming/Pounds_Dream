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
public class SleepRecordVO {

    private Long id;
    private LocalDateTime bedTime;
    private LocalDateTime wakeTime;
    private Integer duration;
    private Integer quality;
    private String tags;
    private Boolean isNap;
    private String note;
}
