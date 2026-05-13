package com.poundsdream.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UserProfileDTO {

    private String nickname;

    private Integer gender;

    private LocalDate birthday;

    private BigDecimal height;

    private BigDecimal targetWeight;

    private Integer targetCalorie;

    private Integer activityLevel;

    private String phone;

    private String email;
}
