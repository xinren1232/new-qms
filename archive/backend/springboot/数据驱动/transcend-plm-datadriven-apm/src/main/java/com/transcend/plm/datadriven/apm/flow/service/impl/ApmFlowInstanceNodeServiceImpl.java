package com.transcend.plm.datadriven.apm.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.transcend.plm.datadriven.apm.constants.FlowNodeStateConstant;
import com.transcend.plm.datadriven.apm.flow.pojo.event.ApmFlowInstanceNodeChangeEvent;
import com.transcend.plm.datadriven.apm.flow.repository.mapper.ApmFlowInstanceNodeMapper;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowInstanceNodeService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author unknown
 */
@Service
public class ApmFlowInstanceNodeServiceImpl extends ServiceImpl<ApmFlowInstanceNodeMapper, ApmFlowInstanceNode>
        implements ApmFlowInstanceNodeService {

    @Resource
    private ApmFlowInstanceNodeMapper apmFlowInstanceNodeMapper;
    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public ApmFlowInstanceNode getByBid(String nodeBid) {
        Assert.hasText(nodeBid, "nodeBid不能为空");
        return this.getOne(Wrappers.<ApmFlowInstanceNode>lambdaQuery().eq(ApmFlowInstanceNode::getBid, nodeBid));
    }

    @Override
    public List<ApmFlowInstanceNode> listByBids(List<String> nodeBids) {
        if (CollectionUtils.isEmpty(nodeBids)) {
            return null;
        }
        return this.list(Wrappers.<ApmFlowInstanceNode>lambdaQuery().in(ApmFlowInstanceNode::getBid, nodeBids));
    }


    /**
     * 检查流程是否完成
     *
     * @param flowTemplateBid
     * @param instanceBid
     * @return
     */
    @Override
    public boolean checkFlowIsComplete(String flowTemplateBid, String instanceBid) {
        long count = this.count(Wrappers.<ApmFlowInstanceNode>lambdaQuery().eq(ApmFlowInstanceNode::getFlowTemplateBid, flowTemplateBid).eq(ApmFlowInstanceNode::getInstanceBid, instanceBid).eq(ApmFlowInstanceNode::getNodeType, 2).eq(ApmFlowInstanceNode::getNodeState, 2));
        return count > 0;
    }

    @Override
    public String getInstanceNodeBid(String flowTemplateBid, String instanceBid, String nodeDataBid) {
        ApmFlowInstanceNode apmFlowInstanceNode = this.getOne(Wrappers.<ApmFlowInstanceNode>lambdaQuery().eq(ApmFlowInstanceNode::getFlowTemplateBid, flowTemplateBid).eq(ApmFlowInstanceNode::getInstanceBid, instanceBid).eq(ApmFlowInstanceNode::getTemplateNodeDataBid, nodeDataBid));
        if (apmFlowInstanceNode != null) {
            return apmFlowInstanceNode.getBid();
        }
        return null;
    }

    @Override
    public List<ApmFlowInstanceNode> listByInstanceBidAndState(String instanceBid, Integer active) {
        Assert.hasText(instanceBid, "instanceBid不能为空");
        return this.list(Wrappers.<ApmFlowInstanceNode>lambdaQuery().eq(ApmFlowInstanceNode::getInstanceBid, instanceBid)
                .eq(ApmFlowInstanceNode::getNodeState, active));
    }

    @Override
    public boolean deleteByInstanceBid(String instanceBid) {
        Assert.hasText(instanceBid, "instanceBid不能为空");
        return apmFlowInstanceNodeMapper.deleteByInstanceBid(instanceBid);
    }

    @Override
    public List<ApmFlowInstanceNode> listByInstanceBids(List<String> instanceBids) {
        if (CollectionUtils.isEmpty(instanceBids)) {
            return Lists.newArrayList();
        }
        return this.list(Wrappers.<ApmFlowInstanceNode>lambdaQuery().in(ApmFlowInstanceNode::getInstanceBid, instanceBids));
    }

    @Override
    public Boolean deleteByInstanceBids(List<String> instanceBids) {
        if (CollectionUtils.isEmpty(instanceBids)) {
            return false;
        }
        return apmFlowInstanceNodeMapper.deleteByInstanceBids(instanceBids);
    }

    @Override
    public List<ApmFlowInstanceNode> listNodesByInstanceAndNode(List<String> instanceBids, String templateNodeDataBid) {
        if (CollectionUtils.isEmpty(instanceBids)) {
            return Lists.newArrayList();
        }
        return this.list(Wrappers.<ApmFlowInstanceNode>lambdaQuery().in(ApmFlowInstanceNode::getInstanceBid, instanceBids)
                .eq(ApmFlowInstanceNode::getTemplateNodeDataBid, templateNodeDataBid)
        );
    }

    @Override
    public List<ApmFlowInstanceNode> listActiveNodesByInstanceAndNode(List<String> instanceBids, String templateNodeDataBid) {
        if (CollectionUtils.isEmpty(instanceBids)) {
            return Lists.newArrayList();
        }
        return this.list(Wrappers.<ApmFlowInstanceNode>lambdaQuery().in(ApmFlowInstanceNode::getInstanceBid, instanceBids)
                .eq(ApmFlowInstanceNode::getTemplateNodeDataBid, templateNodeDataBid)
                .eq(ApmFlowInstanceNode::getNodeState, FlowNodeStateConstant.ACTIVE)
        );
    }

    @Override
    public List<ApmFlowInstanceNode> listByInstanceBid(String instanceBid) {
        Assert.hasText(instanceBid, "instanceBid不能为空");
        return this.list(Wrappers.<ApmFlowInstanceNode>lambdaQuery().eq(ApmFlowInstanceNode::getInstanceBid, instanceBid));
    }

    @Override
    public List<ApmFlowInstanceNode> listActiveByInstanceBid(String instanceBid, List<String> notInNodeBids) {
        Assert.hasText(instanceBid, "instanceBid不能为空");
        LambdaQueryWrapper<ApmFlowInstanceNode> queryWrapper = Wrappers.<ApmFlowInstanceNode>lambdaQuery().eq(ApmFlowInstanceNode::getInstanceBid, instanceBid)
                .eq(ApmFlowInstanceNode::getNodeState, FlowNodeStateConstant.ACTIVE);
        if (!CollectionUtils.isEmpty(notInNodeBids)) {
            queryWrapper.notIn(ApmFlowInstanceNode::getBid, notInNodeBids);
        }
        return this.list(queryWrapper);
    }

    @Override
    public boolean saveBatch(Collection<ApmFlowInstanceNode> entityList) {
        applicationEventPublisher.publishEvent(new ApmFlowInstanceNodeChangeEvent(entityList));
        return super.saveBatch(entityList);
    }

    @Override
    public boolean updateById(ApmFlowInstanceNode entity) {
        applicationEventPublisher.publishEvent(new ApmFlowInstanceNodeChangeEvent(Collections.singletonList(entity)));
        return super.updateById(entity);
    }

    @Override
    public boolean updateBatchById(Collection<ApmFlowInstanceNode> entityList) {
        applicationEventPublisher.publishEvent(new ApmFlowInstanceNodeChangeEvent(entityList));
        return super.updateBatchById(entityList);
    }
}




