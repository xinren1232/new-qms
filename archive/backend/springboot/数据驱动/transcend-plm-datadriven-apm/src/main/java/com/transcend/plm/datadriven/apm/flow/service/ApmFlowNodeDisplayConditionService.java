package com.transcend.plm.datadriven.apm.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeDisplayCondition;

import java.util.List;

/**
 * @author unknown
 */
public interface ApmFlowNodeDisplayConditionService extends IService<ApmFlowNodeDisplayCondition> {

    /**
     * 根据节点业务id列表查询流程节点可见条件配置表
     *
     * @param nodeBids 节点业务id列表
     * @return 符合条件的流程节点可见条件配置表列表
     */
    List<ApmFlowNodeDisplayCondition> listByNodeBids(List<String> nodeBids);

    /**
     * 根据节点业务id查询流程节点可见条件配置表列表。
     *
     * @param nodeBid 节点业务id
     * @return 符合条件的流程节点可见条件配置表列表
     */
    List<ApmFlowNodeDisplayCondition> listByNodeBid(String nodeBid);

}
