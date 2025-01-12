package com.kang.kcloud_aidrive.exception;

import com.kang.kcloud_aidrive.enums.BizCodeEnum;
import lombok.Data;

/**
 * Custom exception class - 自定义异常类
 *
 * @author Kai Kang
 */
@Data
public class BizException extends RuntimeException {

    private int code;
    private String msg;
    private String detail;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.msg = message;
    }

    public BizException(BizCodeEnum bizCodeEnum) {
        super(bizCodeEnum.getMessage());
        this.code = bizCodeEnum.getCode();
        this.msg = bizCodeEnum.getMessage();
    }

    public BizException(BizCodeEnum bizCodeEnum, Exception e) {
        super(bizCodeEnum.getMessage());
        this.code = bizCodeEnum.getCode();
        this.msg = bizCodeEnum.getMessage();
        this.detail = e.toString();
    }
}