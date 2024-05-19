package com.wsp.rule.engine.aspect;


import com.wsp.rule.engine.engine.QLExpress;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@Service
public class QLFuncLoadProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        for (Method method : methods) {
            QLFunc QLAnn = AnnotationUtils.findAnnotation(method, QLFunc.class);
            if (QLAnn != null) {
                String functionId = QLAnn.alias();
                if (StringUtils.isEmpty(functionId)) {
                    functionId = method.getName();
                }
                QLExpress.loadFunction(bean, method, functionId);
            }
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
