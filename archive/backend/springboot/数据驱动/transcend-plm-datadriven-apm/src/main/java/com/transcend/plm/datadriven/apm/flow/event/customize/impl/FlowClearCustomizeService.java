package com.transcend.plm.datadriven.apm.flow.event.customize.impl;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.flow.event.FlowEventBO;
import com.transcend.plm.datadriven.apm.flow.event.customize.IFlowCustomizeMethod;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;
import com.transcend.plm.datadriven.apm.space.repository.service.ApmSpaceAppService;
import com.transcend.plm.datadriven.common.constant.TranscendModelBaseFields;
import com.transcend.plm.datadriven.core.model.ObjectModelStandardI;
import com.transcend.plm.datadriven.core.model.impl.ObjectModelCommonImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author unknown
 * 清除工作流程
 */
@Service("flowClearCustomizeService")
public class FlowClearCustomizeService implements IFlowCustomizeMethod {
    @Resource(type = ObjectModelCommonImpl.class)
    private ObjectModelStandardI objectModelCrudI;

    @Resource
    private ApmSpaceAppService apmSpaceAppService;

    @Override
    public void execute(FlowEventBO eventBO) {
        ApmFlowInstanceNode instanceNode = eventBO.getInstanceNode();
        if (instanceNode != null) {
            ApmSpaceApp apmSpaceApp = apmSpaceAppService.getByBid(instanceNode.getSpaceAppBid());
            if (apmSpaceApp != null) {
                MObject mObject = objectModelCrudI.getByBid(apmSpaceApp.getModelCode(), instanceNode.getInstanceBid());
                if (mObject != null && mObject.get(TranscendModelBaseFields.WORK_ITEM_TYPE) != null) {
                    String tempWorkflowIdStr = "originalWorkflowId";
                    mObject.put(tempWorkflowIdStr, mObject.get(TranscendModelBaseFields.WORK_ITEM_TYPE));
                    mObject.put(TranscendModelBaseFields.WORK_ITEM_TYPE, null);
                    objectModelCrudI.updateByBid(apmSpaceApp.getModelCode(), mObject.getBid(), mObject);
                }
            }
        }
    }
}
