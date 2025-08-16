package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.framework.core.model.api.page.PagedResult;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryCondition;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.apm.event.entity.PageEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.entity.RelationPageEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractPageEventHandler;
import com.transcend.plm.datadriven.apm.event.handler.relation.AbstractRelationPageEventHandler;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证任务点检小结关系页面事件处理器
 * @author qing.chen
 * @date 2025/01/13 14:37
 **/
@Slf4j
@Component
public class CertificationRelationPageEventHandler extends AbstractRelationPageEventHandler {

    /**
     * 认证空间
     */
    @Value("${transcend.application.authSpaceBid:1281300234915565568}")
    private String authSpaceBid;
    /**
     * 认证模板
     */
    @Value("${transcend.application.authTemplateAppBid:1281300396194942976}")
    private String authTemplateAppBid;

    /**
     * 认证任务
     */
    @Value("${transcend.application.authTaskAppBid:1281313877128400896}")
    private String authTaskAppBid;

    @Resource
    private IBaseApmSpaceAppDataDrivenService iBaseApmSpaceAppDataDrivenService;

    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 后置
     *
     * @param param  入参
     * @param result 返回结果
     * @return 返回结果
     */
    @Override
    public PagedResult<MObject> postHandle(RelationPageEventHandlerParam param, PagedResult<MObject> result) {
        List<MObject> dataList = result.getData();
        if (CollectionUtils.isEmpty(dataList)) {
            return super.postHandle(param, result);
        }
        Set<String> templateBids = dataList.stream().map(e -> e.get("templateID") + "").collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(templateBids)) {
            return super.postHandle(param, result);
        }
        QueryCondition queryCondition = new QueryCondition();
        QueryWrapper targetQo = new QueryWrapper();
        targetQo.in("id", templateBids).and().eq("delete_flag", "0");
        List<QueryWrapper> targetQoQueryWrappers = QueryWrapper.buildSqlQo(targetQo);
        queryCondition.setQueries(targetQoQueryWrappers);
        List<MSpaceAppData> templateList = iBaseApmSpaceAppDataDrivenService.list(authTemplateAppBid, queryCondition);
        if (CollectionUtils.isEmpty(templateList)) {
            return super.postHandle(param, result);
        }
        Map<String, Object> templateBidMap = templateList.stream().filter(e -> e.get("isVersionRequired") != null).collect(Collectors.toMap(e -> e.get("id") + "", e -> e.get("isVersionRequired")));
        result.getData().forEach(e -> e.put("isVersionRequired", templateBidMap.get(e.get("templateID")+"")));
        return result;
    }

    /**
     * 匹配是否执行
     *
     * @param param 入参
     * @return true:匹配上需要执行; false 匹配不上不需要执行
     */
    @Override
    public boolean isMatch(RelationPageEventHandlerParam param) {
        ApmSpaceApp apmSpaceApp = param.getApmSpaceApp();
        if (apmSpaceApp == null) {
            return false;
        }
        return authTaskAppBid.equals(apmSpaceApp.getBid());
    }
}
