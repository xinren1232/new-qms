package com.transcend.plm.datadriven.apm.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;

import java.util.List;

/**
 * @author unknown
 */
public interface ApmFlowInstanceNodeService extends IService<ApmFlowInstanceNode> {

    /**
     * 根据节点bid获取节点信息
     *
     * @param nodeBid 节点bid
     * @return 节点信息
     */
    ApmFlowInstanceNode getByBid(String nodeBid);

    /**
     * 根据节点bid列表返回节点信息列表。
     *
     * @param nodeBids 节点bid列表
     * @return 节点信息列表
     */
    List<ApmFlowInstanceNode> listByBids(List<String> nodeBids);

    /**
     * 检查流程是否完成。
     *
     * @param flowTemplateBid 流程模板标识
     * @param instanceBid     流程实例标识
     * @return 如果流程已完成，则返回true；否则返回false。
     */
    boolean checkFlowIsComplete(String flowTemplateBid, String instanceBid);

    /**
     * 获取实例节点的标识。
     *
     * @param flowTemplateBid 流程模板标识
     * @param instanceBid     流程实例标识
     * @param nodeDataBid     节点数据标识
     * @return 节点的标识
     */
    String getInstanceNodeBid(String flowTemplateBid, String instanceBid,String nodeDataBid);

    /**
     * 根据流程实例bid获取节点列表
     * @param instanceBid 流程实例bid
     * @return 节点列表
     */
    List<ApmFlowInstanceNode> listByInstanceBid(String instanceBid);

    /**
     * 根据流程实例的标识获取活动的流程实例节点列表。
     *
     * @param instanceBid 流程实例标识
     * @param notInNodeBids 不包含的节点标识列表
     * @return 活动的流程实例节点列表
     */
    List<ApmFlowInstanceNode> listActiveByInstanceBid(String instanceBid,List<String> notInNodeBids);

    /**
     * 根据流程实例的标识和状态获取流程实例节点列表。
     *
     * @param instanceBid 流程实例标识
     * @param active 流程实例节点的状态
     * @return 流程实例节点列表
     */
    List<ApmFlowInstanceNode> listByInstanceBidAndState(String instanceBid, Integer active);

    /**
     * 根据流程实例的标识删除流程实例节点。
     *
     * @param instanceBid 流程实例标识
     * @return 如果成功删除流程实例节点，则返回true；否则返回false。
     */
    boolean deleteByInstanceBid(String instanceBid);

    /**
     * 根据流程实例标识列表获取流程实例节点列表。
     *
     * @param instanceBids 流程实例标识列表
     * @return 流程实例节点列表
     */
    List<ApmFlowInstanceNode> listByInstanceBids(List<String> instanceBids);

    /**
     * 删除给定实例标识列表对应的流程实例节点。
     *
     * @param instanceBids 实例标识列表
     * @return 如果成功删除流程实例节点，则返回true；否则返回false
     */
    Boolean deleteByInstanceBids(List<String> instanceBids);

    /**
     * 根据流程实例标识和模板节点标识列表，获取活动的流程实例节点列表。
     *
     * @param instanceBids       流程实例标识列表
     * @param templateNodeBid    模板节点标识
     * @return 活动的流程实例节点列表
     */
    List<ApmFlowInstanceNode> listActiveNodesByInstanceAndNode(List<String> instanceBids, String templateNodeBid);

    /**
     * 根据流程实例标识和模板节点标识列表，获取流程实例节点列表。
     *
     * @param targetInstanceBids  目标流程实例标识列表
     * @param templateNodeDataBid 模板节点标识
     * @return 匹配的流程实例节点列表
     */
    List<ApmFlowInstanceNode> listNodesByInstanceAndNode(List<String> targetInstanceBids, String templateNodeDataBid);
}
