package com.poundsdream.enums;

import lombok.Getter;

@Getter
public enum ActivityLevel {

    SEDENTARY(1, "久坐不动", 1.2),
    LIGHT(2, "轻度活动", 1.375),
    MODERATE(3, "中度活动", 1.55),
    ACTIVE(4, "重度活动", 1.725);

    private final int code;
    private final String desc;
    private final double factor;

    ActivityLevel(int code, String desc, double factor) {
        this.code = code;
        this.desc = desc;
        this.factor = factor;
    }

    public static ActivityLevel fromCode(int code) {
        for (ActivityLevel level : values()) {
            if (level.code == code) {
                return level;
            }
        }
        throw new IllegalArgumentException("无效的活动等级: " + code);
    }
}
