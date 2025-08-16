package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.datadriven.apm.event.entity.UpdateEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractUpdateEventHandler;
import com.transcend.plm.datadriven.common.tool.Assert;
import com.transcend.plm.datadriven.common.wapper.MapWrapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 处理人修改拦截处理器
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/5/7 15:44
 */
@Component
public class ProcessorPersonnelUpdateInterceptHandler extends AbstractUpdateEventHandler {
    /**
     * 处理人字段名称
     */
    private final String PROCESSOR_PERSONNEL_FIELD_NAME = "handler";


    @Override
    public UpdateEventHandlerParam preHandle(UpdateEventHandlerParam param) {
        MapWrapper mapWrapper = new MapWrapper(param.getMSpaceAppData());
        List<Object> list = mapWrapper.getList(Object.class, PROCESSOR_PERSONNEL_FIELD_NAME);
        Assert.notEmpty(list,"请至少保留一个处理人");
        return super.preHandle(param);
    }

    @Override
    public boolean isMatch(UpdateEventHandlerParam param) {
        return Optional.ofNullable(param.getMSpaceAppData())
                .map(map -> map.get(PROCESSOR_PERSONNEL_FIELD_NAME)).isPresent();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
