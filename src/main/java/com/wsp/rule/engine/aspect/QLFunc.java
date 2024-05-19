package com.wsp.rule.engine.aspect;

import java.lang.annotation.*;

@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QLFunc {

    /**
     * 方法别名
     */
    String alias() default "";


    /**
     * 函数唯一标识key
     * @return
     */
    String key() default "";
}
