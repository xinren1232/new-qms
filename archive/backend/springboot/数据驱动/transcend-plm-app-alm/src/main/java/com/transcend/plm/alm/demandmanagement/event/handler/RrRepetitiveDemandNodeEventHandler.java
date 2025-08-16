package com.transcend.plm.alm.demandmanagement.event.handler;

import cn.hutool.core.util.ObjectUtil;
import com.transcend.plm.alm.demandmanagement.constants.DemandManagementConstant;
import com.transcend.plm.alm.demandmanagement.enums.TranscendModel;
import com.transcend.plm.configcenter.api.model.object.vo.CfgObjectVo;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.constants.FlowInstanceNodeProperties;
import com.transcend.plm.datadriven.apm.enums.CommonEnum;
import com.transcend.plm.datadriven.apm.event.entity.CompleteFlowNodeEventHandlerParam;
import com.transcend.plm.datadriven.apm.event.handler.instance.AbstractCompleteFlowNodeEventHandler;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceNodeService;
import com.transcend.plm.datadriven.apm.flow.service.IRuntimeService;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppConfigManageService;
import com.transcend.plm.datadriven.apm.space.service.IApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;


/**
 * @Describe RR  重复需求节点处理
 * @Author haijun.ren
 * @Date 2024/9/4
 */
@Slf4j
@Component
public class RrRepetitiveDemandNodeEventHandler extends AbstractCompleteFlowNodeEventHandler {

    private static final String NODE_ACTIVE_COMPLETE = "NODE_COMPLETE";


    @Resource
    private FlowInstanceNodeProperties flowInstanceNodeProperties;

    @Resource
    private ApmFlowInstanceNodeService apmFlowInstanceNodeService;

    @Autowired
    private IApmSpaceAppConfigManageService apmSpaceAppConfigManageService;


    @Resource
    private IApmSpaceAppDataDrivenService apmSpaceAppDataDrivenService;

    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private IRuntimeService runtimeService;

    @Override
    public Object postHandle(CompleteFlowNodeEventHandlerParam param, Object result) {
        //判断是否是需求分析节点，并且是重复需求
        CfgObjectVo cfgObjectVo = param.getCfgObjectVo();
        MSpaceAppData mSpaceAppData = param.getMSpaceAppData();
        String fromNode = param.getNodeBid();
        ApmFlowInstanceNode currentNode = apmFlowInstanceNodeService.getByBid(fromNode);
        if (flowInstanceNodeProperties.getRrAnalysis().equals(currentNode.getWebBid()) &&
                "redundant".equals(mSpaceAppData.get("returnReason")) &&
                ObjectUtil.isNotEmpty(mSpaceAppData.get(DemandManagementConstant.DEMAND_MANAGEMENT_REPEAT))){
            String duplicateInstanceBid = mSpaceAppData.get(DemandManagementConstant.DEMAND_MANAGEMENT_REPEAT).toString();
            //更新流程状态
            runtimeService.copyNodeState(cfgObjectVo.getModelCode(), duplicateInstanceBid, Collections.singletonList(currentNode.getInstanceBid()));
            MObject mObject = objectModelCrudI.getByBid(cfgObjectVo.getModelCode(), duplicateInstanceBid);
            //更新生命周期
            if(mObject == null){
                log.info("流程实例数据不存在，流程实例ID：{}", duplicateInstanceBid);
                return super.postHandle(param, result);
            }
            MSpaceAppData spaceAppData = new MSpaceAppData();
            spaceAppData.setLifeCycleCode(mObject.getLifeCycleCode());
            objectModelCrudI.updateByBid(cfgObjectVo.getModelCode(),currentNode.getInstanceBid(), spaceAppData);
            //更新被重复需求属性
            if(mObject.get("isItADuplicateRequirement")== null || StringUtils.isBlank(mObject.get("isItADuplicateRequirement").toString())
                    || "null".equals(mObject.get("isItADuplicateRequirement").toString())){
                mObject.put("isItADuplicateRequirement","Y");
                objectModelCrudI.updateByBid(cfgObjectVo.getModelCode(),duplicateInstanceBid, mObject);
            }
        }
        return super.postHandle(param, result);
    }

    @Override
    public boolean isMatch(CompleteFlowNodeEventHandlerParam param) {
        String modelCode = param.getCfgObjectVo().getModelCode();
        return TranscendModel.RR.getCode().equals(modelCode);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
