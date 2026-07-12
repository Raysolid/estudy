package com.estudy.exception;

import com.estudy.entity.enums.ResultCode;
import com.estudy.entity.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = Exception.class)
    Object handleException(Exception e, HttpServletRequest request) {
        logger.error("请求错误，请求地址{}，错误信息：", request.getRequestURL(), e);
        Result<Object> ajaxResult = new Result<>();
        if (e instanceof NoHandlerFoundException) {
            // 404
            ajaxResult.setCode(ResultCode.CODE_404.getCode());
            ajaxResult.setInfo(ResultCode.CODE_404.getMsg());
            ajaxResult.setStatus(Result.STATUS_ERROR);
        } else if (e instanceof BusinessException) {
            // 业务错误
            BusinessException biz = (BusinessException) e;
            ajaxResult.setCode(biz.getCode() == null ? ResultCode.CODE_400.getCode() : biz.getCode());
            ajaxResult.setInfo(biz.getMessage());
            ajaxResult.setStatus(Result.STATUS_ERROR);
        } else if (e instanceof BindException|| e instanceof MethodArgumentTypeMismatchException) {
            // 参数类型错误
            ajaxResult.setCode(ResultCode.CODE_400.getCode());
            ajaxResult.setInfo(ResultCode.CODE_400.getMsg());
            ajaxResult.setStatus(Result.STATUS_ERROR);
        } else if (e instanceof DuplicateKeyException) {
            // 主键冲突
            ajaxResult.setCode(ResultCode.CODE_409.getCode());
            ajaxResult.setInfo(ResultCode.CODE_409.getMsg());
            ajaxResult.setStatus(Result.STATUS_ERROR);
        } else {
            // 服务器错误
            ajaxResult.setCode(ResultCode.CODE_500.getCode());
            ajaxResult.setInfo(ResultCode.CODE_500.getMsg());
            ajaxResult.setStatus(Result.STATUS_ERROR);
        }
        return ajaxResult;
    }
}
