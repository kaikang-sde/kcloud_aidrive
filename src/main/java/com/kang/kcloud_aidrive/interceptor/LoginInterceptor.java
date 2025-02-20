package com.kang.kcloud_aidrive.interceptor;

import com.kang.kcloud_aidrive.dto.AccountDTO;
import com.kang.kcloud_aidrive.enums.BizCodeEnum;
import com.kang.kcloud_aidrive.util.CommonUtil;
import com.kang.kcloud_aidrive.util.JsonData;
import com.kang.kcloud_aidrive.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * The LoginInterceptor is a ThreadLocal-based interceptor
 * used to validate user login status and store the logged-in user’s information within the current thread.
 * @author Kai Kang
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    // ThreadLocal is a thread-local variable that is bound to the lifecycle of the current thread.
    // Each thread has its own independent ThreadLocal value, which does not interfere with others.
    public static ThreadLocal<AccountDTO> threadLocal = new ThreadLocal<>();

    // The preHandle method of the interceptor is executed before the Controller method runs.
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Option request is not intercepted
        if (HttpMethod.OPTIONS.name().equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
        }
        // get token from header, if not exist, get token from parameter
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }

        if (StringUtils.isNotBlank(token)) {
            try {
                Claims claims = JwtUtil.checkLoginJWT(token);
                if (claims == null) { // JWT verification failed
                    log.error("JWT verification failed");
                    CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
                    return false;
                }

                Long accountId = Long.valueOf(claims.get("accountId").toString());
                String username = (String) claims.get("username");
                AccountDTO accountDTO = AccountDTO.builder().id(accountId).username(username).build();
                threadLocal.set(accountDTO);
                return true;
            } catch (Exception e) {
                log.error("JWT verification failed", e);
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            }
        }
        // no token
        CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // clean threadLocal, avoid memory leak
        threadLocal.remove();
    }
}
