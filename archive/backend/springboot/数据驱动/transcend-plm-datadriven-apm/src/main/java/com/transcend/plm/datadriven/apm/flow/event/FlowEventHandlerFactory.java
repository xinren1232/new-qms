package com.transcend.plm.datadriven.apm.flow.event;

import com.transcend.plm.datadriven.common.spring.PlmContextHolder;
import com.transcend.plm.datadriven.common.tool.CommonConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程事件处理器工厂
 * @createTime 2023-10-08 15:53:00
 */
public class FlowEventHandlerFactory {
    private FlowEventHandlerFactory() {
    }
    private static final Map<Integer, IFlowEventHandler> HANDLER_MAP = new HashMap<>(CommonConstant.START_MAP_SIZE);

    static {
        init();
    }
    public static IFlowEventHandler getHandler(Integer eventType) {
        return HANDLER_MAP.get(eventType);
    }

    private static void init() {
        //从spring容器中获取对应的处理器
        PlmContextHolder.getBeansOfType(IFlowEventHandler.class).forEach((k, v) -> HANDLER_MAP.put(v.getEventType(), v));
    }

}
