package com.kang.kcloud_aidrive.annotation;

import java.lang.annotation.*;

/**
 * @author Kai Kang
 * 任何方法需要携带token校验，可以添加该注解， 会自动校验
 * 配合AOP自定义切面类校验规则 - ShareCodeAspect
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ShareCodeCheck {
}
