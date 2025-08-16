package com.transcend.plm.datadriven.config.eventbus;

import com.google.common.eventbus.EventBus;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyPreEventBusDto;
import lombok.extern.slf4j.Slf4j;

/**
 * @author bin.yin
 * @description: 同步EventBus
 * @version:
 * @date 2024/04/19 14:20
 */
@Slf4j
public class NotifySyncEventBus {
    public static final EventBus EVENT_BUS = new EventBus();


    /**
     * @param object
     */
    public static void register(Object object){
        EVENT_BUS.register(object);
    }

    /**
     * @param preEventBusDto
     */
    public static void publishPreEvent(NotifyPreEventBusDto preEventBusDto) {
        EVENT_BUS.post(preEventBusDto);
    }
}
