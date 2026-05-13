package com.poundsdream.vo;

import lombok.Data;

import java.util.Map;

@Data
public class PetVO {
    private String petName;
    private String petType;
    private String mood;
    private Integer happiness;
    private Integer petLevel;
    private Integer totalHappiness;
    private Integer evolutionLevel;
    private String evolutionName;
    private String evolutionIcon;
    private Integer daysSinceLastRecord;
    private Integer currentStreak;
    private String message;
    private Map<String, Integer> cooldowns;
}
