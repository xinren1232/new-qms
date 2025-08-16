package com.transcend.plm.datadriven.apm.extension.event;

import com.google.common.eventbus.Subscribe;
import com.transcend.plm.datadriven.apm.dto.NotifyObjectPartialContentDto;
import com.transcend.plm.datadriven.apm.extension.strategy.ObjectExtensionStrategy;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 对象批量新增消费者
 * @author jinpeng.bai
 * @version v1.0.0
 **/
@Component
@Slf4j
public class UpdatePartialContentEventCustomer {

    @Resource
    private ObjectExtensionStrategy objectExtensionStrategy;

    private UpdatePartialContentEventCustomer() {
        NotifyEventBus.register(this);
    }

    @Subscribe
    public void execute(NotifyObjectPartialContentDto event) {
        log.info("NotifyObjectPartialContentDto execute event:{}", event);
        ObjectExtensionStrategy strategy = objectExtensionStrategy.getStrategy(event.getModelCode());
        if (strategy != null) {
            strategy.updatePartialContent(event);
        }
    }

}
