package com.poundsdream.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PetLogVO {

    private String action;

    private String actionName;

    private String petMood;

    private Integer happiness;

    private LocalDateTime createdAt;
}
