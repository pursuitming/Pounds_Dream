package com.poundsdream.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private BigDecimal height;
    private BigDecimal targetWeight;
    private Integer targetCalorie;
    private Integer activityLevel;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
}
