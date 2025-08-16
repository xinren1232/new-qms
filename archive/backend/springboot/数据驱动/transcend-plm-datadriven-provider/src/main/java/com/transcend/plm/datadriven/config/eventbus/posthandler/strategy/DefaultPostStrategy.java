package com.transcend.plm.datadriven.config.eventbus.posthandler.strategy;

import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.datadriven.common.pojo.dto.NotifyPostEventBusDto;
import com.transcend.plm.datadriven.config.eventbus.posthandler.strategy.handler.AbstractHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.transcend.plm.datadriven.config.eventbus.posthandler.constant.PostEventConstants.DEFAULT_STRATEGY;

/**
 * @Describe 默认策略类，如果需要针对不同的方法执行不同的逻辑可以在继承类中继续拓展策略模式
 * @Author yuhao.qiu
 * @Date 2024/4/9
 */
@Slf4j
@Component(DEFAULT_STRATEGY)
public abstract class DefaultPostStrategy {

    @Autowired
    protected List<AbstractHandler> handlerList;

    @Autowired
    private CfgObjectFeignClient cfgObjectFeignClient;

    protected AbstractHandler abstractHandler;

    public void exec(NotifyPostEventBusDto postEventBusDto) {
        log.info("执行了ModelCode为{}的后置方法", postEventBusDto.getObject().getModelCode());
    }

    protected void buildHandlerChain(NotifyPostEventBusDto postEventBusDto) {
        // 通过ModelCode拿到各环境统一的对象Bid
        String modelCode = postEventBusDto.getObject().getModelCode();
        CfgObjectVo objVo = cfgObjectFeignClient.getByModelCode(modelCode).getData();
        String currentObjBid = objVo.getBid();
        List<AbstractHandler> handlers = handlerList
                .stream()
                // 过滤无效处理器
                .filter(data -> data.isMatch(currentObjBid))
                // 处理器排序
                .sorted(Comparator.comparingInt(Ordered::getOrder))
                .collect(Collectors.toList());
        // 构造处理器链
        for (int i = 0; i < handlers.size(); i++) {
            if (i == 0) {
                abstractHandler = handlers.get(0);
            } else {
                AbstractHandler currentHandler = handlers.get(i - 1);
                AbstractHandler nextHandler = handlers.get(i);
                currentHandler.setNextHandler(nextHandler);
            }
        }
    }
}
