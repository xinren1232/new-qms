package com.transcend.plm.datadriven.config.eventbus.posthandler.strategy;

import com.transcend.plm.datadriven.common.pojo.dto.NotifyPostEventBusDto;
import com.transcend.plm.datadriven.config.eventbus.posthandler.constant.PostEventConstants;
import org.springframework.stereotype.Component;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/5/13
 */
@Component(PostEventConstants.CAD_STRATEGY)
public class CadPostStrategy extends DefaultPostStrategy{

    @Override
    public void exec(NotifyPostEventBusDto postEventBusDto) {
        super.buildHandlerChain(postEventBusDto);
        abstractHandler.action(postEventBusDto);
    }

}
