package com.campus.erp.common;

/**
 * 业务异常。
 *
 * 与系统异常的区别：
 *   业务异常 = 规则不满足（余额不足、状态不对、无权限），是"可以预料的失败"
 *   系统异常 = 代码 bug、数据库断连、空指针，是"意外的故障"
 *
 * 业务异常不应该让 @Transactional 误判，也不需要打印完整堆栈；
 * 它会由全局异常处理器转成 HTTP 400 + 错误消息。
 */
public class BizException extends RuntimeException {

    /** 业务错误码，前端可据此做不同处理 */
    private final int code;

    public BizException(String message) {
        this(1000, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
