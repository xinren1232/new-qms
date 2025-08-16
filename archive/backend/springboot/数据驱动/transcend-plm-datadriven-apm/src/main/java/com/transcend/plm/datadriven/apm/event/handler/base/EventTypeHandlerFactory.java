package com.transcend.plm.datadriven.apm.event.handler.base;

import com.google.common.collect.ArrayListMultimap;
import com.transcend.plm.datadriven.apm.event.entity.base.EventHandlerParam;
import com.transcend.plm.datadriven.apm.event.enums.EventHandlerTypeEnum;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author bin.yin
 * @description:
 * @version:
 * @date 2024/06/13 16:34
 */
@Component
public class EventTypeHandlerFactory {
    private final ArrayListMultimap<Class<?>, EventHandler<EventHandlerParam, Object>> eventHandlerTypeMap = ArrayListMultimap.create();

    @Resource
    private ApplicationContext context;

    @PostConstruct
    public void init() {
        for (EventHandlerTypeEnum eventHandlerTypeEnum : EventHandlerTypeEnum.values()) {
            Map<String, ?> beansOfType = context.getBeansOfType(eventHandlerTypeEnum.getBaseHandlerClass());
            beansOfType.forEach((clazz, value) -> eventHandlerTypeMap.put(eventHandlerTypeEnum.getBaseHandlerClass(), (EventHandler<EventHandlerParam, Object>) value));
        }
    }

    public List<EventHandler<EventHandlerParam, Object>> getEventHandlerTypeMap(Class<?> baseHandlerTypeClass) {
        return eventHandlerTypeMap.get(baseHandlerTypeClass);
    }


}
