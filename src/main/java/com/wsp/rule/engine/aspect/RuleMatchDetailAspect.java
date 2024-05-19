package com.wsp.rule.engine.aspect;


import com.wsp.rule.engine.engine.QLExpress;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class RuleMatchDetailAspect {

    @Autowired
    private QLExpress qlExecutor;

    @Around("@annotation(com.wsp.rule.engine.aspect.QLFunc)")
    public Object ruleValue(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature sign = (MethodSignature) joinPoint.getSignature();
        Method method = sign.getMethod();
        QLFunc rasFunc = method.getAnnotation(QLFunc.class);
        if (null == rasFunc) {
            return joinPoint.proceed();
        }
        Object value = joinPoint.proceed();
        if (StringUtils.isNotEmpty(rasFunc.key())) {
            qlExecutor.setContextValue(rasFunc.key(), value);
        }
        return value;
    }
}