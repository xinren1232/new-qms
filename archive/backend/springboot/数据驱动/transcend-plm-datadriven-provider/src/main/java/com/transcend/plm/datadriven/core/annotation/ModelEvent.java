package com.transcend.plm.datadriven.core.annotation;

import java.lang.annotation.*;

/**
 * 描述 自定义事务注解
 *
 * @author lwb
 * @since 2020/7/8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Inherited
public @interface ModelEvent {


    String value() default "";

    String name() default "";

}
