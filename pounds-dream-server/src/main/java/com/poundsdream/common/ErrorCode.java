package com.poundsdream.common;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户相关 1001-1099
    USER_NOT_FOUND(1001, "用户不存在"),
    USERNAME_EXISTS(1002, "用户名已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    PHONE_EXISTS(1004, "手机号已被注册"),

    // 体重相关 2001-2099
    WEIGHT_ALREADY_RECORDED(2001, "今日已记录体重"),

    // 饮食相关 3001-3099
    FOOD_NOT_FOUND(3001, "食物不存在"),

    // 运动相关 4001-4099
    EXERCISE_TYPE_NOT_FOUND(4001, "运动类型不存在"),

    // 社区相关 5001-5099
    POST_NOT_FOUND(5001, "帖子不存在"),
    COMMENT_NOT_FOUND(5002, "评论不存在"),
    ALREADY_LIKED(5003, "已经点赞过了");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
