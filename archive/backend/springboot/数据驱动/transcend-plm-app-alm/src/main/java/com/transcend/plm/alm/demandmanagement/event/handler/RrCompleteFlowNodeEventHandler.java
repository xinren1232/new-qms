package com.transcend.plm.alm.demandmanagement.event.handler;

import com.transcend.plm.configcenter.api.model.view.dto.CfgViewMetaDto;
import com.transcend.plm.configcenter.api.model.view.vo.CfgViewVo;
import com.transcend.plm.datadriven.apm.constants.FlowInstanceNodeProperties;
import com.transcend.plm.datadriven.apm.event.entity.CompleteFlowNodeEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractCompleteFlowNodeEventHandler;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceNodeService;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Describe RR 完成流程节点事件
 * @Author haijun.ren
 * @Date 2024/9/4
 */
@Slf4j
@Component
public class RrCompleteFlowNodeEventHandler extends AbstractCompleteFlowNodeEventHandler {

    private static final String RR_OBJ_BID = "1253640663282315264";

    private static final String NODE_ACTIVE_COMPLETE = "NODE_COMPLETE";

    @Resource
    @Value("#{'${transcend.plm.apm.object.rr.attr.filter:domainList,priority,softwareAndHardwareClassification,hardwareInterfacePersonnel,classificationOfDemandTypes,requirementTypeClassificationSubItems,newFeatureSubItems,distributionTime}'.split(',')}")
    private List<String> filterAttr;


    @Resource
    private FlowInstanceNodeProperties flowInstanceNodeProperties;

    @Resource
    private ApmFlowInstanceNodeService apmFlowInstanceNodeService;

    @Autowired
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;


    @Resource
    private IApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;

    @Override
    public Object postHandle(CompleteFlowNodeEventHandlerParam param, Object result) {
        //判断是否是从退回确认节点到达分析过滤分发或者领域价值评估节点
        String fromNode = param.getNodeBid();
        String spaceAppBid = param.getApmSpaceApp().getBid();
        ApmFlowInstanceNode currentNode = apmFlowInstanceNodeService.getByBid(fromNode);
        if (flowInstanceNodeProperties.getRrBack().equals(currentNode.getWebBid())){
            MSpaceAppData mSpaceAppData = new MSpaceAppData();
            //在到达退回确认节点时，选择为【更新信息重新提交】，配置的字段【是否重启】为【是】，选择为【退回无疑义,不再重新提交】，配置的字段【是否重启】为【否】
            if ("Return".equals(param.getMSpaceAppData().get("returnConfirmation"))) {
                mSpaceAppData.put("isRestart","IS_NO");
            }else if ("updata".equals(param.getMSpaceAppData().get("returnConfirmation"))) {
                mSpaceAppData.put("isRestart","IS_YES");
            }
            //获取当前节点
            List<ApmFlowInstanceNode> apmFlowInstanceNodes = apmFlowInstanceNodeService.listByInstanceBid(param.getBid());
            boolean flag = apmFlowInstanceNodes.stream().anyMatch(v -> v.getNodeState() == 1 && (flowInstanceNodeProperties.getRrAssessment().equals(v.getWebBid()) || flowInstanceNodeProperties.getRrAnalysis().equals(v.getWebBid())));
            if (flag){
                //查询分析过滤分发节点和领域价值评估节点的审批视图属性
                CfgViewVo analysisViewVo = apmSpaceAppConfigManageService.flowNodeViewGet(spaceAppBid, NODE_ACTIVE_COMPLETE, flowInstanceNodeProperties.getRrAnalysis());
                CfgViewVo assessmentViewVo = apmSpaceAppConfigManageService.flowNodeViewGet(spaceAppBid, NODE_ACTIVE_COMPLETE, flowInstanceNodeProperties.getRrAssessment());
                Set<String> attrSet = analysisViewVo.getMetaList().stream().map(CfgViewMetaDto::getName).collect(Collectors.toSet());
                attrSet.addAll(assessmentViewVo.getMetaList().stream().map(CfgViewMetaDto::getName).collect(Collectors.toSet()));
                filterAttr.forEach(attrSet::remove);
                attrSet.forEach(v->mSpaceAppData.put(v, null));
            }
            if (CollectionUtils.isNotEmpty(mSpaceAppData)) {
                // 更新部分属性
                apmSpaceAppDataDrivenService.updatePartialContent(spaceAppBid, param.getBid(), mSpaceAppData,false);
            }
        }
        return result;
    }

    @Override
    public boolean isMatch(CompleteFlowNodeEventHandlerParam param) {
        String objBid = param.getObjBid();
        return RR_OBJ_BID.equals(objBid);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
