package com.campus.erp.common;

/**
 * 统一响应格式。
 *
 * 为什么需要它：让前端拿到的永远是同一种结构，
 * 不用为每个接口写不同的解析逻辑。
 *
 * 成功示例：{"code":0,"message":"success","data":[...]}
 * 失败示例：{"code":1000,"message":"可用经费不足","data":null}
 */
public class Result<T> {

    /** 0 表示成功；非 0 为业务错误码 */
    private int code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "success", data);
    }

    public static <T> Result<T> ok() {
        return new Result<>(0, "success", null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
