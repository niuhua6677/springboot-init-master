package com.hgh.springbootinit.common;

/**
 * 返回工具类，用于创建统一的响应数据格式。
 * 提供了成功和失败响应的静态工厂方法。
 *
 * @author hgh
 */
public class ResultUtils {

    /**
     * 创建一个成功响应对象。
     *
     * @param data 泛型数据，可以是任何类型的响应数据
     * @param <T>  响应数据的类型
     * @return 包含成功状态码、数据和默认消息的BaseResponse对象
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 创建一个失败的响应对象，并使用ErrorCode来定义错误状态。
     *
     * @param errorCode 错误代码枚举，包含了错误码和错误信息
     * @return 包含错误状态码和错误信息的BaseResponse对象
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 创建一个失败的响应对象，并手动指定错误码和错误信息。
     *
     * @param code    自定义错误码
     * @param message 自定义错误信息
     * @return 包含指定错误状态码和错误信息的BaseResponse对象
     */
    public static BaseResponse error(int code, String message) {
        return new BaseResponse(code, null, message);
    }

    /**
     * 创建一个失败的响应对象，使用ErrorCode来定义错误状态，并允许覆盖默认的错误信息。
     *
     * @param errorCode 错误代码枚举，包含了错误码
     * @param message   自定义错误信息
     * @return 包含错误状态码和自定义错误信息的BaseResponse对象
     */
    public static BaseResponse error(ErrorCode errorCode, String message) {
        return new BaseResponse(errorCode.getCode(), null, message);
    }
}