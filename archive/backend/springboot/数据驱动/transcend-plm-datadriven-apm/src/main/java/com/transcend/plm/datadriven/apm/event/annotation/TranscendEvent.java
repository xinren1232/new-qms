package com.transcend.plm.datadriven.apm.event.annotation;

import com.transcend.plm.datadriven.apm.event.enums.EventHandlerTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author bin.yin
 * @description:
 * @version:
 * @date 2024/06/13 13:49
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface TranscendEvent {
    EventHandlerTypeEnum eventHandlerType();
}
