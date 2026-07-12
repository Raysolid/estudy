package com.estudy.entity.enums;

import lombok.Getter;

/**
 * 状态码枚举
 */
@Getter
public enum ResultCode {
    CODE_200(200, "请求成功"),
    CODE_400(400, "请求参数错误"),
    CODE_401(401, "未登录或登录超时，请重新登录"),
    CODE_404(404, "请求地址不存在"),
    CODE_409(409, "信息已经存在"),
    CODE_500(500, "服务器返回错误，请联系管理员");

    private final Integer code;

    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
