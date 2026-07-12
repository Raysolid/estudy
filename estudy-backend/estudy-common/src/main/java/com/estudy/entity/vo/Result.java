package com.estudy.entity.vo;

import com.estudy.entity.enums.ResultCode;
import com.estudy.exception.BusinessException;
import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String info;
    private T data;
    private String status;

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";

    public Result() {
    }

    public Result(Integer code, String info, T data, String status) {
        this.code = code;
        this.info = info;
        this.data = data;
        this.status = status;
    }

    public static <T> Result<T> success(T t) {
        return new Result<>(ResultCode.CODE_200.getCode(),
                ResultCode.CODE_200.getMsg(), t, STATUS_SUCCESS);
    }

    /**
     * businessError
     */
    public static <T> Result<T> fail(BusinessException e, T t) {
        return new Result<>(
                e.getCode() == null ? ResultCode.CODE_400.getCode() : e.getCode(),
                e.getMessage(), t, STATUS_ERROR
        );
    }

    /**
     * serverError
     */
    public static <T> Result<T> fail(T t) {
        return new Result<>(ResultCode.CODE_500.getCode(),
                ResultCode.CODE_500.getMsg(), t, STATUS_ERROR);
    }

}
