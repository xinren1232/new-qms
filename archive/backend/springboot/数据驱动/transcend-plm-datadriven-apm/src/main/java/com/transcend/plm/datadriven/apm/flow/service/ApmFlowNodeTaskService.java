package com.transcend.plm.datadriven.apm.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeTask;

import java.util.List;

/**
 * @author unknown
 */
public interface ApmFlowNodeTaskService extends IService<ApmFlowNodeTask> {

    /**
     * 根据节点业务ID列表获取流程节点任务列表。
     *
     * @param nodeBids 节点业务ID列表
     * @return 节点任务列表
     */
    List<ApmFlowNodeTask> listByNodeBids(List<String> nodeBids);

    /**
     * 根据节点业务ID获取流程节点任务列表
     *
     * @param nodeBid 节点业务ID
     * @return 节点任务列表
     */
    List<ApmFlowNodeTask> listByNodeBid(String nodeBid);
}
