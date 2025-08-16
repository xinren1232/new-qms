package com.transcend.plm.datadriven.config.eventbus.posthandler;


import com.google.common.eventbus.Subscribe;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyPostEventBusDto;
import com.transcend.plm.datadriven.config.eventbus.NotifyEventBus;
import com.transcend.plm.datadriven.config.eventbus.posthandler.strategy.DefaultPostStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author unknown
 */
@Slf4j
@Service("notifyEventPostBusService")
public class NotifyEventPostBusService {
    @Resource
    private Map<String, DefaultPostStrategy> postStrategyMap;

    @Resource
    private NotifyPostEventProperties notifyPostEventProperties;

    /**
     * 注册对象订阅抛出事件
     */
    public NotifyEventPostBusService() {
        NotifyEventBus.register(this);
    }

    /**
     * 订阅事件
     *
     * @param postEventBusDto
     */
    @Subscribe
    public void subscribePostEvent(NotifyPostEventBusDto postEventBusDto) {
        String modelCode = postEventBusDto.getObject().getModelCode();
        String strategyName = notifyPostEventProperties.getModelCode2StrategyMap().get(modelCode);
        if (strategyName == null) {
            return;
        }
        DefaultPostStrategy postStrategy = postStrategyMap.get(strategyName);
        if (postStrategy == null) {
            return;
        }
        postStrategy.exec(postEventBusDto);
    }
}