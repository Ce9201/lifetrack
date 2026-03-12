package com.sun.lifetrack.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 用户相关错误 40001-40099
    USERNAME_EMPTY(40001, "用户名不能为空"),
    USERNAME_ALREADY_EXISTS(40002, "用户名已被占用"),
    USERNAME_INVALID_LENGTH(40003, "用户名长度须在2-20位之间"),

    PASSWORD_EMPTY(40004, "密码不能为空"),
    PASSWORD_TOO_SHORT(40005, "密码长度至少为6位"),
    PASSWORD_TOO_LONG(40006, "密码长度不能超过20位"),
    PASSWORD_TOO_WEAK(40007, "密码必须包含字母和数字"),

    EMAIL_EMPTY(40008, "邮箱不能为空"),
    EMAIL_INVALID_FORMAT(40009, "邮箱格式不正确"),
    EMAIL_ALREADY_EXISTS(40010, "邮箱已被注册"),

    USERNAME_NOT_FOUND(40011, "用户不存在"),
    PASSWORD_INCORRECT(40012, "密码错误"),
    USERNAME_PASSWORD_EMPTY(40013, "用户名和密码不能为空"),

    // 睡眠记录相关错误 40100-40199
    RECORD_NOT_FOUND(40100, "睡眠记录不存在"),
    RECORD_ALREADY_EXISTS(40101, "该日期已有睡眠记录"),
    RECORD_TIME_OVERLAP(40102, "睡眠记录时间与已有记录重叠"),

    TIME_CANNOT_BE_NULL(40103, "入睡时间和醒来时间不能为空"),
    START_AFTER_END(40104, "入睡时间不能晚于醒来时间"),
    INVALID_DURATION(40105, "睡眠时长无效"),
    DURATION_TOO_LONG(40106, "睡眠时长不能超过18小时"),
    DATE_MISMATCH(40107, "日期必须与入睡日期一致"),

    // 权限错误 40300-40399
    NO_PERMISSION(40300, "无权限操作此记录"),

    // 系统内部错误
    INTERNAL_SERVER_ERROR(50000, "系统内部错误");


    private final int code;//数字错误码（如 40001）
    private final String defaultMessage;//默认的错误描述信息

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}