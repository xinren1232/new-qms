package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.apm.event.entity.PageEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractPageEventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.tools.StayDurationHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author qing.chen
 * @date 2025/01/13 14:37
 **/
@Slf4j
@Component
public class BsePageEventHandler extends AbstractPageEventHandler {


    @Override
    public PagedResult<MSpaceAppData> postHandle(PageEventHandlerParam param, PagedResult<MSpaceAppData> result) {
        Optional.ofNullable(result).map(PagedResult::getData).ifPresent(StayDurationHandler::handle);
        return result;
    }

    /**
     * 匹配是否执行
     *
     * @param param 入参
     * @return true:匹配上需要执行; false 匹配不上不需要执行
     */
    @Override
    public boolean isMatch(PageEventHandlerParam param) {
        ApmSpaceApp apmSpaceApp = param.getApmSpaceApp();
        return apmSpaceApp != null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
