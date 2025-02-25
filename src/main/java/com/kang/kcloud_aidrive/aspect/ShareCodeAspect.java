package com.kang.kcloud_aidrive.aspect;

import com.kang.kcloud_aidrive.annotation.ShareCodeCheck;
import com.kang.kcloud_aidrive.enums.BizCodeEnum;
import com.kang.kcloud_aidrive.exception.BizException;
import com.kang.kcloud_aidrive.util.JsonData;
import com.kang.kcloud_aidrive.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * @author Kai Kang
 * 自定义切面类： 类似于拦截器，在请求到达Controller前执行
 */
@Aspect
@Component
@Slf4j
public class ShareCodeAspect {
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * setter: 设置当前线程的共享ID
     *
     * @param shareId 共享ID
     */
    public static void set(Long shareId) {
        threadLocal.set(shareId);
    }


    /**
     * getter: 获取当前线程绑定的共享ID
     *
     * @return 当前线程绑定的共享ID，若未绑定则返回0
     */
    public static Long get() {
        Long shareId = threadLocal.get();
        if (Objects.isNull(shareId)) {
            return null;
        }
        return shareId;
    }

    /**
     * Step 1：定义 @Pointcut注解表达式：
     * 方式一：@annotation：当执行的方法上拥有指定的注解时生效 - @shareCodeCheck
     * 方式二：execution：一般用于指定方法的执行
     */
    @Pointcut("@annotation(shareCodeCheck)")
    public void pointCutShareCodeCheck(ShareCodeCheck shareCodeCheck) {

    }

    /**
     * Step 2：定义环绕通知, 围绕着方法执行
     *
     * @Around 可以用来在调用一个具体方法前和调用后来完成一些具体的任务。
     * 方式一：单用 @Around("execution(* kang.kcould_aidrive.controller.*.*(..))")可以
     * 方式二：用@Pointcut和@Around联合注解也可以（采用这个）
     */
    @Around("pointCutShareCodeCheck(shareCodeCheck)")
    public Object around(ProceedingJoinPoint joinPoint, ShareCodeCheck shareCodeCheck) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        //令牌形式校验提交
        String requestToken = request.getHeader("share-token");
        if (StringUtils.isBlank(requestToken)) {
            throw new BizException(BizCodeEnum.SHARE_CODE_ILLEGAL);
        }

        Claims claims = JwtUtil.checkShareJWT(requestToken);
        if (claims == null) {
            log.error("AOP Configuration - Share code check failed");
            return JsonData.buildResult(BizCodeEnum.SHARE_CODE_ILLEGAL);
        }
        Long shareId = Long.valueOf(claims.get(JwtUtil.CLAIM_SHARE_KEY) + "");
        set(shareId); // set to ThreadLocal
        log.info("Before Around execution - shareId: {}", shareId);
        Object obj = joinPoint.proceed();
        log.info("After Around execution - shareId: {}", shareId);
        return obj;
    }
}