package com.transcend.plm.datadriven.config.eventbus.posthandler.strategy;

import com.transcend.plm.datadriven.common.pojo.dto.NotifyPostEventBusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.transcend.plm.datadriven.config.eventbus.posthandler.constant.PostEventConstants.FILE_STRATEGY;

/**
 * @Describe
 * @Author yuhao.qiu
 * @Date 2024/4/10
 */

@Slf4j
@Component(FILE_STRATEGY)
public class FileObjPostStrategy extends DefaultPostStrategy {

    @Override
    public void exec(NotifyPostEventBusDto postEventBusDto) {
        super.buildHandlerChain(postEventBusDto);
        abstractHandler.action(postEventBusDto);
    }
}
