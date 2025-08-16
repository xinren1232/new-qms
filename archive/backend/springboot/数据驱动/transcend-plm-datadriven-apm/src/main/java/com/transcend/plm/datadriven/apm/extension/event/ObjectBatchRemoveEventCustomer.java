package com.transcend.plm.datadriven.apm.extension.event;

import com.google.common.eventbus.Subscribe;
import com.transcend.plm.datadriven.apm.dto.NotifyRelationBatchRemoveBusDto;
import com.transcend.plm.datadriven.apm.extension.strategy.ObjectExtensionStrategy;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 对象批量新增消费者
 * @author jinpeng.bai
 * @version v1.0.0
 **/
@Service
@Slf4j
public class ObjectBatchRemoveEventCustomer {

    @Resource
    private ObjectExtensionStrategy objectExtensionStrategy;

    private ObjectBatchRemoveEventCustomer() {
        NotifyEventBus.register(this);
    }


    @Subscribe
    public void execute(final NotifyRelationBatchRemoveBusDto event) {
        log.info("NotifyRelationBatchRemoveBusDto execute event:{}", event);
        ObjectExtensionStrategy strategy = objectExtensionStrategy.getStrategy(event.getModelCode());
        if (strategy != null) {
            strategy.batchRemove(event);
        }
    }

}
