package com.kang.kcloud_aidrive.exception;

import com.kang.kcloud_aidrive.util.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * global exception handler
 *
 * @author Kai Kang
 */

// Marks the class as a global exception handler that applies to all controllers in the application.
// It intercepts exceptions thrown by any controller and provides a unified way to handle them.
@ControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public JsonData handler(Exception e) {

        if (e instanceof BizException bizException) {
            log.error("[Business exception]{}", e.getMessage());
            return JsonData.buildCodeAndMsg(bizException.getCode(), bizException.getMsg());
        } else {
            log.error("[System exception]{}", e.getMessage());
            return JsonData.buildError("System exception");
        }

    }

}


