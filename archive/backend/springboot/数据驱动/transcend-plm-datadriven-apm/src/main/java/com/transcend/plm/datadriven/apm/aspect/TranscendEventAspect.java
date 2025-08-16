package com.transcend.plm.datadriven.apm.aspect;

import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.apm.event.annotation.TranscendEvent;
import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import com.transcend.plm.datadriven.apm.event.enums.EventHandlerTypeEnum;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.event.handler.base.EventTypeHandlerFactory;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * transcend事件拦截
 * @author bin.yin
 */
@Aspect
@Component
@Slf4j
public class TranscendEventAspect {

    @Resource
    private EventTypeHandlerFactory eventTypeHandlerFactory;

    @Pointcut("@annotation(com.transcend.plm.datadriven.apm.event.annotation.TranscendEvent)")
    public void pointCut() {
    }




    @Around("pointCut()")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object proceed;
        Signature signature = proceedingJoinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature)signature;
        Method method = methodSignature.getMethod();
        TranscendEvent annotation = method.getAnnotation(TranscendEvent.class);
        Object[] args = proceedingJoinPoint.getArgs();
        EventHandlerTypeEnum eventHandlerTypeEnum = annotation.eventHandlerType();
        // 构建事件处理链，获取到同一类型的事件处理器
        List<EventHandler<EventHandlerParam, Object>> eventHandlerList = buildEventHandlerChain(eventHandlerTypeEnum.getBaseHandlerClass());
        // 事件处理链为空 直接执行目标方法
        if (CollectionUtils.isEmpty(eventHandlerList)) {
            proceed = proceedingJoinPoint.proceed();
            return proceed;
        }
        // 构建事件参数
        EventHandlerParam eventHandlerParam = buildParam(eventHandlerList.get(0), args);
        // 匹配需要执行的处理链
        EventHandlerParam finalEventHandlerParam = eventHandlerParam;
        eventHandlerList = eventHandlerList.stream().filter(eventHandler -> eventHandler.isMatch(finalEventHandlerParam)).collect(Collectors.toList());
        // 事件处理链为空 直接执行目标方法
        if (CollectionUtils.isEmpty(eventHandlerList)) {
            proceed = proceedingJoinPoint.proceed();
            return proceed;
        }
        // 执行事件前置
        for (EventHandler<EventHandlerParam, Object> eventHandler : eventHandlerList) {
            eventHandlerParam = eventHandler.preHandle(eventHandlerParam);
        }
        // 执行目标方法
        proceed = proceedingJoinPoint.proceed(args);
        // 执行事件后置
        for (EventHandler<EventHandlerParam, Object> eventHandler : eventHandlerList) {
            proceed = eventHandler.postHandle(eventHandlerParam, proceed);
        }
        return proceed;
    }

    /**
     * 构建事件处理链 排序
     * @param baseHandlerTypeClass 事件处理器类型
     * @return 事件处理器链
     */
    private List<EventHandler<EventHandlerParam, Object>> buildEventHandlerChain(Class<?> baseHandlerTypeClass) {
        List<EventHandler<EventHandlerParam, Object>> eventHandlerList = eventTypeHandlerFactory.getEventHandlerTypeMap(baseHandlerTypeClass);
        return Optional.ofNullable(eventHandlerList)
                .map(e -> e.stream().sorted(Comparator.comparingInt(Ordered::getOrder)).collect(Collectors.toList()))
                .orElse(Lists.newArrayList());
    }

    private EventHandlerParam buildParam(EventHandler<EventHandlerParam, Object> eventHandler, Object[] args) {
        return eventHandler.initParam(args);
    }

}
