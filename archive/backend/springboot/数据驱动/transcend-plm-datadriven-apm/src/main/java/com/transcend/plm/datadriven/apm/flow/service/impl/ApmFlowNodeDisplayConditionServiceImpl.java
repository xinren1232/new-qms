package com.transcend.plm.datadriven.apm.flow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeDisplayCondition;
import com.transcend.plm.datadriven.apm.flow.repository.mapper.ApmFlowNodeDisplayConditionMapper;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowNodeDisplayConditionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author unknown
 */
@Service
public class ApmFlowNodeDisplayConditionServiceImpl extends ServiceImpl<ApmFlowNodeDisplayConditionMapper, ApmFlowNodeDisplayCondition>
        implements ApmFlowNodeDisplayConditionService {

    @Override
    public List<ApmFlowNodeDisplayCondition> listByNodeBids(List<String> nodeBids) {
        List<ApmFlowNodeDisplayCondition> list = list(Wrappers.<ApmFlowNodeDisplayCondition>lambdaQuery().in(ApmFlowNodeDisplayCondition::getNodeBid, nodeBids));
        return list;
    }

    @Override
    public List<ApmFlowNodeDisplayCondition> listByNodeBid(String nodeBid) {
        List<ApmFlowNodeDisplayCondition> list = list(Wrappers.<ApmFlowNodeDisplayCondition>lambdaQuery().eq(ApmFlowNodeDisplayCondition::getNodeBid, nodeBid));
        return list;
    }
}




