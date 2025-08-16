package com.transcend.plm.datadriven.apm.flow.event.customize.impl;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.enums.CommonEnum;
import com.transcend.plm.datadriven.apm.flow.event.FlowEventBO;
import com.transcend.plm.datadriven.apm.flow.event.customize.IFlowCustomizeMethod;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Describe 流程重复需求事件
 * @Author yuanhu.huang
 * @Date 2024/10/8
 */
@Service("repeatedDemandFlowCustomizeService")
public class RepeatedDemandFlowCustomizeService implements IFlowCustomizeMethod {
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    @Override
    public void execute(FlowEventBO eventBO) {
        MSpaceAppData pageMSpaceAppData = eventBO.getPageMSpaceAppData();
        if(pageMSpaceAppData != null && pageMSpaceAppData.get(CommonEnum.REPETITIVE_DEMAND.getCode()) != null){
           String repetitiveDemand = pageMSpaceAppData.get(CommonEnum.REPETITIVE_DEMAND.getCode()).toString();
           if(CommonEnum.Y.getCode().equals(repetitiveDemand)){
               //是重复需求
               String  repetitiveDemandBid = pageMSpaceAppData.get("duplicateRequirementNumber").toString();
               String modelCode = pageMSpaceAppData.get("modelCode").toString();
               MObject mObject = objectModelCrudI.getByBid(modelCode, repetitiveDemandBid,false);
               if(mObject != null){
                   MObject mObjectUpdate = new MObject();
                   mObjectUpdate.setLifeCycleCode(mObject.getLifeCycleCode());
                   objectModelCrudI.updateByBid(modelCode,pageMSpaceAppData.getBid(),mObjectUpdate);
               }
           }
        }
    }
}
