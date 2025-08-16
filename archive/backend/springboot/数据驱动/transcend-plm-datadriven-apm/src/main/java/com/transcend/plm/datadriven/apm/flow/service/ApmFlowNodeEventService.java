package com.transcend.plm.datadriven.apm.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeEvent;

import java.util.List;

/**
 * @author unknown
 */
public interface ApmFlowNodeEventService extends IService<ApmFlowNodeEvent> {

    /**
     * 根据节点标识列表获取ApmFlowNodeEvent列表。
     *
     * @param nodeBids 节点标识列表
     * @return 符合条件的ApmFlowNodeEvent列表
     */
    List<ApmFlowNodeEvent> listByNodeBids(List<String> nodeBids);

    /**
     * 根据节点标识获取相应的ApmFlowNodeEvent列表。
     *
     * @param nodeBid 节点标识
     * @return 符合条件的ApmFlowNodeEvent列表
     */
    List<ApmFlowNodeEvent> listByNodeBid(String nodeBid);

    /**
     * 根据事件分类、节点标识和版本号来获取符合条件的ApmFlowNodeEvent列表。
     *
     * @param eventClassification 事件分类
     * @param nodeBid 节点标识
     * @param version 版本号
     * @return 符合条件的ApmFlowNodeEvent列表
     */
    List<ApmFlowNodeEvent> listByNodeBidAndType(int eventClassification, String nodeBid, String version);
}
