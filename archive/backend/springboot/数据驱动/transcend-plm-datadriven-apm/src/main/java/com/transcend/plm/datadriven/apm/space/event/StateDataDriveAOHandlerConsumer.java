package com.transcend.plm.datadriven.apm.space.event;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.eventbus.Subscribe;
import com.transcend.plm.datadriven.apm.permission.pojo.ao.StateDataDriveAO;
import com.transcend.plm.datadriven.apm.dto.StateDataDriveDto;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowDriveRelate;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowLineEvent;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowDriveRelateService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author unknown
 */
@Component
public class StateDataDriveAOHandlerConsumer implements InitializingBean {
    @Resource
    private ApmFlowDriveRelateService apmFlowDriveRelateService;

    @Resource
    private IApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;
    @Override
    public void afterPropertiesSet() throws Exception {
        NotifyEventBus.EVENT_BUS.register(this);
    }
    @Subscribe
    public void handleStateDataDriveAO(final StateDataDriveAO stateDataDriveAO){
        ApmFlowLineEvent apmFlowLineEvent = stateDataDriveAO.getApmFlowLineEvent();
        List<StateDataDriveDto> stateDataDriveDtoList = new ArrayList<>();
        List<ApmFlowDriveRelate> apmFlowDriveRelates = apmFlowDriveRelateService.list(Wrappers.<ApmFlowDriveRelate>lambdaQuery().eq(ApmFlowDriveRelate::getEventBid, apmFlowLineEvent.getBid()));
        if(CollectionUtil.isNotEmpty(apmFlowDriveRelates)){
           for(ApmFlowDriveRelate apmFlowDriveRelate:apmFlowDriveRelates){
               List<StateDataDriveDto> stateDataDriveDtos = apmSpaceAppDataDrivenService.updateDriveState(stateDataDriveAO,apmFlowDriveRelate);
               stateDataDriveDtoList.addAll(stateDataDriveDtos);
           }
        }
        if(CollectionUtil.isNotEmpty(stateDataDriveDtoList)){
            for(StateDataDriveDto stateDataDriveDto:stateDataDriveDtoList){
                apmSpaceAppDataDrivenService.updatePartialContent(stateDataDriveDto.getSpaceAppBid(),stateDataDriveDto.getBid(),stateDataDriveDto.getMSpaceAppData());
            }
        }
    }
}
