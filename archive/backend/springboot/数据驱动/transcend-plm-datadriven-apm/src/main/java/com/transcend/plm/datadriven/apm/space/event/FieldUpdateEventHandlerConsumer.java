package com.transcend.plm.datadriven.apm.space.event;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.eventbus.Subscribe;
import com.transcend.plm.datadriven.apm.constants.ApmAppEventTypeConstant;
import com.transcend.plm.datadriven.apm.space.pojo.bo.FieldUpdateEvent;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmAppEvent;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmAppEventService;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 字段更新事件处理
 * @createTime 2023-11-02 10:52:00
 */
@Component
public class FieldUpdateEventHandlerConsumer implements InitializingBean {

    @Resource
    private ApmAppEventService eventService;

    @Override
    public void afterPropertiesSet() throws Exception {
        NotifyEventBus.EVENT_BUS.register(this);
    }

    @Subscribe
    public void handleFieldUpdateEvent(final FieldUpdateEvent fieldUpdateEvent) {
        List<ApmAppEvent> apmAppEvents = eventService.list(Wrappers.<ApmAppEvent>lambdaQuery().eq(ApmAppEvent::getAppBid, fieldUpdateEvent.getSpaceAppBid())
                .eq(ApmAppEvent::getType, ApmAppEventTypeConstant.FIELD_UPDATE_EVENT)
                //enable为1
                .eq(ApmAppEvent::getEnableFlag, 1)
        );
        if (apmAppEvents.isEmpty()) {
            return;
        }
        apmAppEvents.parallelStream().forEach(apmAppEvent -> {
            AppEventHandlerFactory.getHandler(apmAppEvent.getPath()).handleFieldUpdateEvent(fieldUpdateEvent);
        });
    }
}
