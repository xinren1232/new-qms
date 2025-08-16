package com.transcend.plm.datadriven.apm.flow.event.customize.impl;

import com.transcend.plm.configcenter.api.feign.CfgObjectFeignClient;
import com.transcend.plm.configcenter.api.model.object.vo.ObjectModelLifeCycleVO;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.config.TableAttributeDefinition;
import com.transcend.plm.datadriven.api.model.config.TableDefinition;
import com.transcend.plm.datadriven.apm.flow.event.FlowEventBO;
import com.transcend.plm.datadriven.apm.flow.event.customize.IFlowCustomizeMethod;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.apm.space.service.IAppDataService;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.tool.ObjectTools;
import com.transcend.plm.datadriven.domain.object.base.ObjectModelDomainService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author unknown
 */
@Service("addUserDemandCustomizeService")
public class ProductDemandCompleteCustomizeService implements IFlowCustomizeMethod {
    @Value("${transcend.plm.apm.app.userDemandAppBid:USER_DEMAND_SPACE_APP}")
    private String userDemandAppBid;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private ObjectModelDomainService objectModelDomainService;

    @Resource
    private CfgObjectFeignClient cfgObjectClient;

    @Resource
    private IAppDataService appDataService;
    /**
     * @description: 用户需求审批完成时，需要生成一条用户需求管理实例
     * @author: haijun.ren
     * @date: 2024/12/19 17:21
     * @param: null
     * @return:
     **/
    @Override
    public void execute(FlowEventBO eventBO) {
        ApmFlowInstanceNode instanceNode = eventBO.getInstanceNode();
       if(instanceNode != null){
           ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(instanceNode.getSpaceAppBid());
           if(apmSpaceApp != null){
               MObject mObject = objectModelDomainService.getByBid(apmSpaceApp.getModelCode(), instanceNode.getInstanceBid());
               if(mObject != null){
                   //创建用户需求管理需要的参数
                   ApmSpaceApp userDemandApp = apmSpaceAppService.getByBid(userDemandAppBid);
                   TableDefinition table = ObjectTools.fillTableDefinition(userDemandApp.getModelCode());
                   Set<String> attrCods = table.getTableAttributeDefinitions().stream().map(TableAttributeDefinition::getProperty).collect(Collectors.toSet());
                   ObjectModelLifeCycleVO objectModelLifeCycleVO = cfgObjectClient.findObjectLifecycleByModelCode(userDemandApp.getModelCode())
                           .getCheckExceptionData();
                   MObject addMObject = new MObject();
                   mObject.forEach((key, value) -> {
                       if(attrCods.contains(key)){
                           addMObject.put(key, value);
                       }
                   });
                   addMObject.remove(TranscendModelBaseFields.ID);
                   String bid = com.transcend.framework.common.util.SnowflakeIdWorker.nextIdStr();
                   addMObject.put(TranscendModelBaseFields.BID, bid);
                   addMObject.put(TranscendModelBaseFields.DATA_BID, bid);
                   addMObject.put(TranscendModelBaseFields.LIFE_CYCLE_CODE, "DAISHOULI");
                   addMObject.put(TranscendModelBaseFields.MODEL_CODE, userDemandApp.getModelCode());
                   addMObject.put(TranscendModelBaseFields.SPACE_APP_BID, userDemandAppBid);
                   addMObject.put(TranscendModelBaseFields.LC_TEMPL_BID, objectModelLifeCycleVO.getBid());
                   addMObject.put(TranscendModelBaseFields.LC_TEMPL_VERSION, objectModelLifeCycleVO.getLcTemplVersion());
                   addMObject.put(TranscendModelBaseFields.CREATED_BY, mObject.getCreatedBy());
                   addMObject.put(TranscendModelBaseFields.CREATED_TIME, mObject.getCreatedTime());
                   appDataService.add(userDemandApp.getModelCode(), addMObject);
               }
           }
       }
    }
}
