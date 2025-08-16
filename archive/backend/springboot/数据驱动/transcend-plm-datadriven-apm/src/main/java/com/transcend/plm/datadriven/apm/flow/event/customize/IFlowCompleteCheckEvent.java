package com.transcend.plm.datadriven.apm.flow.event.customize;

import com.transcend.plm.datadriven.api.model.MObject;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.space.repository.po.ApmSpaceApp;

/**
 * @author unknown
 */
public interface IFlowCompleteCheckEvent {
    /**
     * check
     *
     * @param app         app
     * @param currentNode currentNode
     * @param mObject     mObject
     */
    void check(ApmSpaceApp app, ApmFlowInstanceNode currentNode, MObject mObject);
}
