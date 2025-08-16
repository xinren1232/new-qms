package com.transcend.plm.datadriven.apm.flow.service;

import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;

import java.util.List;

/**
 * @author unknown
 */
public interface IToDoCenterService {
    /**
     * 推送待办任务数据API
     *
     * @param apmFlowInstanceNode apmFlowInstanceNode
     * @param mSpaceAppData       mSpaceAppData
     * @param empNo               empNo
     * @param content             content
     * @param appLink             appLink
     */
    void pushTodoTaskData(ApmFlowInstanceNode apmFlowInstanceNode, MSpaceAppData mSpaceAppData, List<String> empNo, String content, String appLink);

    /**
     * 推送待办任务状态API
     *
     * @param apmFlowInstanceNode apmFlowInstanceNode
     * @param mSpaceAppData       mSpaceAppData
     * @param empNos              empNos
     * @param content             content
     * @param appLink             appLink
     * @param operate             operate
     * @param state               state
     */
    void pushTodoTaskState(ApmFlowInstanceNode apmFlowInstanceNode, MSpaceAppData mSpaceAppData, List<String> empNos, String content, String appLink, String operate, String state);
}
