package com.transcend.plm.datadriven.apm.flow.event.customize.impl;

import com.alibaba.fastjson.JSON;
import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.api.model.QueryWrapper;
import com.transcend.plm.datadriven.apm.flow.event.FlowEventBO;
import com.transcend.plm.datadriven.apm.flow.event.customize.IFlowCustomizeMethod;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.common.tool.CommonConstant;
import com.transcend.plm.datadriven.domain.object.base.ObjectModelDomainService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.transcend.plm.datadriven.apm.constants.ModelCodeProperties.DEMAND_SPECIFIC_MODEL_CODE;

/**
 * @author unknown
 */
@Service("spaceBidUpdateCustomizeService")
public class SpaceBidUpdateCustomizeService implements IFlowCustomizeMethod {
    @Value("${transcend.plm.product.modelCode:A48}")
    private String productModelCode;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Resource
    private ObjectModelDomainService objectModelDomainService;
    @Override
    public void execute(FlowEventBO eventBO) {
        ApmFlowInstanceNode instanceNode = eventBO.getInstanceNode();
       if(instanceNode != null){
           ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(instanceNode.getSpaceAppBid());
           if(apmSpaceApp != null){
               MObject mObject = objectModelDomainService.getByBid(apmSpaceApp.getModelCode(), instanceNode.getInstanceBid());
               if(mObject != null){
                   if(mObject.get("appProducts") != null){
                       List<String> appProducts = new ArrayList<>();
                       String appProductStr = mObject.get("appProducts").toString();
                       if(appProductStr.startsWith(CommonConstant.OPEN_BRACKET)){
                           appProducts = JSON.parseArray(mObject.get("appProducts").toString(), String.class);
                       }else{
                           appProducts.add(appProductStr);
                       }
                       if(CollectionUtils.isNotEmpty(appProducts)){
                           QueryWrapper qo = new QueryWrapper();
                           qo.eq(TranscendModelBaseFields.DELETE_FLAG, 0).and().in(TranscendModelBaseFields.BID, appProducts);
                           List<QueryWrapper> wrappers =  QueryWrapper.buildSqlQo(qo);
                           List<MObject> productObjects = objectModelDomainService.list(productModelCode,wrappers);
                           if(CollectionUtils.isNotEmpty(productObjects)){
                               //查询空间
                              List<String> spaceBids = new ArrayList<>();
                              for(MObject productObject : productObjects){
                                  if(productObject.get("belongSpaceBid") != null){
                                      String spaceBid = productObject.get("belongSpaceBid").toString();
                                      if(!spaceBids.contains(spaceBid)){
                                          spaceBids.add(spaceBid);
                                      }
                                  }
                              }
                              if(spaceBids.size() == 1){
                                  //只有一个空间
                                  ApmSpaceApp apmSpaceAppNew = apmSpaceAppService.getApmSpaceAppBySpaceBidAndModelCode(spaceBids.get(0),DEMAND_SPECIFIC_MODEL_CODE);
                                  if(apmSpaceAppNew != null){
                                      mObject.put(TranscendModelBaseFields.SPACE_BID, apmSpaceAppNew.getSpaceBid());
                                      mObject.put(TranscendModelBaseFields.SPACE_APP_BID, apmSpaceAppNew.getBid());
                                      objectModelDomainService.updateByBid(apmSpaceApp.getModelCode(),instanceNode.getInstanceBid(), mObject);
                                  }
                              }
                           }
                       }
                   }
               }
           }
       }
    }
}
