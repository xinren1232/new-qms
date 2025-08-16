package com.transcend.plm.datadriven.apm.flow.event.customize.impl;

import com.transcend.plm.datadriven.apm.constants.FlowNodeStateConstant;
import com.transcend.plm.datadriven.apm.constants.TaskTypeConstant;
import com.transcend.plm.datadriven.apm.flow.event.FlowEventBO;
import com.transcend.plm.datadriven.apm.flow.event.customize.IFlowCustomizeMethod;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceNodeService;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceRoleUserService;
import com.transcend.plm.datadriven.apm.task.service.ApmTaskApplicationService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author unknown
 * 流程节点重置事件自定义处理
 */
@Service("flowNodeResetCustomizeService")
public class FlowNodeResetCustomizeService implements IFlowCustomizeMethod {
    @Resource
    private ApmFlowInstanceNodeService apmFlowInstanceNodeService;
    @Resource
    private ApmTaskApplicationService apmTaskApplicationService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execute(FlowEventBO eventBO) {
        ApmFlowInstanceNode instanceNode = eventBO.getInstanceNode();
        if(instanceNode != null){
            List<String> notInBids = new ArrayList<>();
            notInBids.add(instanceNode.getBid());
            List<ApmFlowInstanceNode> apmActiveFlowInstanceNodes = apmFlowInstanceNodeService.listActiveByInstanceBid(instanceNode.getInstanceBid(),notInBids);
            if(CollectionUtils.isNotEmpty(apmActiveFlowInstanceNodes)){
                List<String> apmActiveFlowInstanceNodeBids = apmActiveFlowInstanceNodes.stream().map(ApmFlowInstanceNode::getBid).collect(Collectors.toList());
                //删除任务
                apmTaskApplicationService.deleteByBizBids(TaskTypeConstant.FLOW,apmActiveFlowInstanceNodeBids);
                for(ApmFlowInstanceNode apmActiveFlowInstanceNode : apmActiveFlowInstanceNodes){
                    apmActiveFlowInstanceNode.setNodeState(FlowNodeStateConstant.NOT_START);
                }
                //更新状态
                apmFlowInstanceNodeService.updateBatchById(apmActiveFlowInstanceNodes);
            }
        }
    }
}
