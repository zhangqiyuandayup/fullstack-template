package com.campus.erp.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * 为什么需要它：
 *   如果 Service 里 throw 一个异常而没人处理，Spring 会返回 500 白页/默认错误页，
 *   前端拿到的东西不可解析。这个类把所有异常统一转成 {code,message,data} 结构。
 *
 * 两类异常分开处理：
 *   业务异常 BizException  → HTTP 400 + 明确的业务提示（用户能看懂）
 *   其他异常 Exception     → HTTP 500 + 通用提示（不暴露堆栈，避免泄露内部信息）
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBizException(BizException e) {
        // 业务异常是"预料中的失败"，用 warn 级别，不打完整堆栈
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOtherException(Exception e) {
        // 系统异常是"意外的故障"，需要完整堆栈排查；但不把细节返回给前端
        log.error("系统异常", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(9999, "服务器内部错误，请联系管理员"));
    }
}
