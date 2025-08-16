package com.transcend.plm.datadriven.apm.event.handler.instance;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.apm.event.entity.PageEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.base.EventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transsion.framework.dto.BaseRequest;

/**
 * @Describe 分页查询前后置事件处理器抽象类
 * @Author yuhao.qiu
 * @Date 2024/6/17
 */
public abstract class AbstractPageEventHandler implements EventHandler<PageEventHandlerParam, PagedResult<MSpaceAppData>> {
    @Override
    public PageEventHandlerParam initParam(Object[] args) {
        return PageEventHandlerParam.builder()
                .pageQo((BaseRequest<QueryCondition>) args[1])
                .filterRichText((Boolean) args[2])
                .build()
                .initAppAndObj((String) args[0]);
    }
}