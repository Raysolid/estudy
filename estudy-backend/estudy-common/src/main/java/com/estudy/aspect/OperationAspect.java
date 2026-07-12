package com.estudy.aspect;

import com.estudy.annotation.GlobalInterceptor;
import com.estudy.component.RedisComponent;
import com.estudy.entity.dto.UserToken;
import com.estudy.entity.enums.ResultCode;
import com.estudy.exception.BusinessException;
import com.estudy.utils.StrUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component("OperationAspect")
public class OperationAspect {

    @Resource
    private RedisComponent redisComponent;

    @Pointcut("@annotation(com.estudy.annotation.GlobalInterceptor)")
    private void pointcut() {
    }

    @Before("pointcut()")
    public void interceptor(JoinPoint point) {
        Object[] args = point.getArgs();
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
        if (interceptor == null) {
            return;
        }
        // 获取token
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("token");
        if (StrUtils.isEmpty(token)) {
            throw new BusinessException(ResultCode.CODE_401);
        }
        // 验证token
        if (interceptor.checkLogin() && interceptor.adminLogin()) {
            adminLogin(token);
        } else {
            checkLogin(token);
        }
    }

    private void checkLogin(String token) {
        UserToken userToken = redisComponent.getUserToken(token);
        if (userToken == null) {
            throw new BusinessException(ResultCode.CODE_401);
        }
    }

    private void adminLogin(String token) {
        String account = redisComponent.getAdminToken(token);
        if (account == null) {
            throw new BusinessException(ResultCode.CODE_401);
        }
    }

}
