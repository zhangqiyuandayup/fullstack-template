package com.campus.erp.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理器。
 *
 * 为什么需要它：
 *   如果 Service 里 throw 一个异常而没人处理，Spring 会返回 500 白页/默认错误页，
 *   前端拿到的东西不可解析。这个类把所有异常统一转成 {code,message,data} 结构。
 *
 * 处理顺序（Spring 会选【最具体】的那个方法）：
 *   1. BizException                 → 400/401 + 业务提示
 *   2. 请求格式类异常（三小类）      → 400 + 说明"你的请求有问题"
 *   3. 其他 Exception               → 500 + 通用提示
 *
 * 第 2 类必须单独处理：
 *   它们本质是"客户端传错了"，应该返回 400。
 *   如果被第 3 类兜底，就会变成 500，让人误以为服务器坏了，
 *   排查方向也会被带偏。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 业务规则不满足 */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBizException(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());

        // 401 未登录 / 403 已登录但无权限 / 其余业务错误 400
        HttpStatus status;
        if (e.getCode() == 401) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (e.getCode() == 403) {
            status = HttpStatus.FORBIDDEN;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }

        return ResponseEntity
                .status(status)
                .body(Result.fail(e.getCode(), e.getMessage()));
    }

    /**
     * 请求体解析失败：JSON 格式错、字段类型对不上、body 为空等。
     * 例如 PowerShell 里引号没转义，导致发出去的 JSON 是残缺的。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleUnreadableBody(HttpMessageNotReadableException e) {
        log.warn("请求体无法解析: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(4000, "请求体格式不正确，应为合法 JSON"));
    }

    /** 缺少必填的查询参数，例如少了 operatorId */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("缺少参数: {}", e.getParameterName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(4001, "缺少必填参数：" + e.getParameterName()));
    }

    /** 参数类型不对，例如 approvedAmount 传了 abc */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误: name={}, value={}", e.getName(), e.getValue());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(4002, "参数 " + e.getName() + " 的值不合法：" + e.getValue()));
    }

    /** 其余都是意料之外的系统故障 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOtherException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(9999, "服务器内部错误，请联系管理员"));
    }
}
