
package com.transcend.plm.datadriven.apm.space.event;

import com.google.common.eventbus.Subscribe;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.StateFlowTodoDriveAO;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmAppEventService;
import com.transcend.plm.datadriven.apm.task.service.ApmTaskApplicationService;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class StateFlowTodoEventHandlerConsumer implements InitializingBean {

    @Resource
    private ApmAppEventService eventService;

    @Resource
    private ApmTaskApplicationService apmTaskApplicationService;

    @Override
    public void afterPropertiesSet() throws Exception {
        NotifyEventBus.EVENT_BUS.register(this);
    }

    @Subscribe
    public void handleStateFlowEvent(final StateFlowTodoDriveAO stateFlowTodoDriveAO) {
        apmTaskApplicationService.updateStateTask(stateFlowTodoDriveAO.getInstanceBid(), stateFlowTodoDriveAO.getInstanceName(), stateFlowTodoDriveAO.getLifeCycleCode(), stateFlowTodoDriveAO.getPersonResponsible(), stateFlowTodoDriveAO.getIsLastState());
    }
}
