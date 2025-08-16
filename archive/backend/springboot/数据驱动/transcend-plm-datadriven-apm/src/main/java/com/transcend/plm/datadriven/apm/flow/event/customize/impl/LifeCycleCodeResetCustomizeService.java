package com.transcend.plm.datadriven.apm.flow.event.customize.impl;

import com.transcend.plm.datadriven.apm.flow.event.FlowEventBO;
import com.transcend.plm.datadriven.apm.flow.event.customize.IFlowCustomizeMethod;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import com.transcend.plm.datadriven.apm.space.service.IBaseApmSpaceAppDataDrivenService;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author unknown
 * 流程节点重置事件自定义处理,针对的是RR需求重复时完成节点更新需求状态逻辑
 */
@Service("lifeCycleCodeResetCustomizeService")
public class LifeCycleCodeResetCustomizeService implements IFlowCustomizeMethod {

    private static final Logger log = LoggerFactory.getLogger(LifeCycleCodeResetCustomizeService.class);
    @Resource
    private IBaseApmSpaceAppDataDrivenService baseApmSpaceAppDataDrivenService;
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(FlowEventBO eventBO) {
        ApmFlowInstanceNode instanceNode = eventBO.getInstanceNode();
        //查询当前流程实例数据
        MSpaceAppData mSpaceAppData = baseApmSpaceAppDataDrivenService.get(instanceNode.getSpaceAppBid(), instanceNode.getInstanceBid(), false);
        if(mSpaceAppData == null){
            log.info("流程实例数据不存在，流程实例ID：{}", instanceNode.getInstanceBid());
            return;
        }
        if(mSpaceAppData.get("duplicateRequirementNumber")!=null && mSpaceAppData.get("duplicateRequirementNumber").toString().length()>10){
            MSpaceAppData repeatInstance = baseApmSpaceAppDataDrivenService.get(instanceNode.getSpaceAppBid(), mSpaceAppData.get("duplicateRequirementNumber").toString(), false);
            if(repeatInstance == null){
                log.info("重复需求流程实例数据不存在，流程实例ID：{}", mSpaceAppData.get("duplicateRequirementNumber").toString());
                return;
            }
            mSpaceAppData.setLifeCycleCode(repeatInstance.getLifeCycleCode());
            objectModelCrudI.updateByBid(mSpaceAppData.getModelCode(),mSpaceAppData.getBid(), mSpaceAppData);
            if(repeatInstance.get("isItADuplicateRequirement")== null || StringUtils.isBlank(repeatInstance.get("isItADuplicateRequirement").toString())
            || "null".equals(repeatInstance.get("isItADuplicateRequirement").toString())){
                repeatInstance.put("isItADuplicateRequirement","Y");
                objectModelCrudI.updateByBid(repeatInstance.getModelCode(),repeatInstance.getBid(), repeatInstance);
            }
        }
    }
}
