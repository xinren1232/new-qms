package com.transcend.plm.datadriven.apm.space.event;

import com.transcend.plm.datadriven.apm.flow.event.IFlowEventHandler;
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
public class AppEventHandlerFactory {
    private AppEventHandlerFactory() {
    }
    private static final Map<String, IFieldUpdateEventHandler> HANDLER_MAP = new HashMap<>(CommonConstant.START_MAP_SIZE);

    static {
        init();
    }
    public static IFieldUpdateEventHandler getHandler(String name) {
        return HANDLER_MAP.get(name);
    }

    private static void init() {
        //从spring容器中获取对应的处理器
        PlmContextHolder.getBeansOfType(IFieldUpdateEventHandler.class).forEach((k, v) -> HANDLER_MAP.put(v.getHandlerName(), v));
    }

}
