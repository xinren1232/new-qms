package com.transcend.plm.datadriven.config.eventbus.posthandler.strategy.handler.common;

import com.transcend.plm.datadriven.common.pojo.dto.NotifyPostEventBusDto;
import com.transcend.plm.datadriven.config.eventbus.posthandler.strategy.handler.AbstractHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/4/30
 */
@Component
@Slf4j
public class ChainDefaultHandler extends AbstractHandler {

    @Override
    protected void doCurrentAction(NotifyPostEventBusDto postEventBusDto) {
        log.info("执行了ModelCode为{}的后置方法", postEventBusDto.getObject().getModelCode());
    }

    @Override
    public boolean isMatch(String currentExcuteObjBid) {
        return true;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
