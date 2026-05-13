package com.poundsdream.enums;

import lombok.Getter;

@Getter
public enum Gender {

    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");

    private final int code;
    private final String desc;

    Gender(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
