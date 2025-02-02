package com.kang.kcloud_aidrive.exception;

import com.kang.kcloud_aidrive.enums.BizCodeEnum;
import lombok.Data;

/**
 * Custom exception class
 * used to represent business logic-related exceptions in a more structured way
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

    // creating an exception using a predefined BizCodeEnum (error code enum).
    public BizException(BizCodeEnum bizCodeEnum) {
        super(bizCodeEnum.getMessage());
        this.code = bizCodeEnum.getCode();
        this.msg = bizCodeEnum.getMessage();
    }

    // creating an exception using BizCodeEnum and adding details from another exception.
    public BizException(BizCodeEnum bizCodeEnum, Exception e) {
        super(bizCodeEnum.getMessage());
        this.code = bizCodeEnum.getCode();
        this.msg = bizCodeEnum.getMessage();
        this.detail = e.toString();
    }
}