package com.sun.lifetrack.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;//错误类型标识（带数字码）
    private final String customMessage;//最终展示给用户的消息

    //直接使用ErrorCode中定义的固定消息
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.customMessage = errorCode.getDefaultMessage();
    }

    //使用自定义消息,如规则动态变化、动态信息、多语言/个性化（覆盖默认消息）
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
}