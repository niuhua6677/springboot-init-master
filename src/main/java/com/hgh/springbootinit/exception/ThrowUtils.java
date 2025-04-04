package com.hgh.springbootinit.exception;

import com.hgh.springbootinit.common.ErrorCode;

/**
 * 抛异常工具类，用于根据条件抛出异常。
 * 这个工具类可以简化在代码中处理条件判断并抛出异常的逻辑。
 *
 * @author hgh
 */
@SuppressWarnings("unused")
public class ThrowUtils {

    /**
     * 如果条件为真，则抛出给定的运行时异常。
     *
     * @param condition      要检查的条件
     * @param runtimeException 如果条件为真，则抛出此异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 如果条件为真，则根据给定的错误代码创建并抛出BusinessException。
     *
     * @param condition 要检查的条件
     * @param errorCode 错误代码枚举，包含了错误信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 如果条件为真，则根据给定的错误代码和自定义消息创建并抛出BusinessException。
     *
     * @param condition 要检查的条件
     * @param errorCode 错误代码枚举，包含了错误信息
     * @param message   自定义错误消息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
