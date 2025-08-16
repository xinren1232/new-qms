package com.transcend.plm.datadriven.apm.flow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeEvent;
import com.transcend.plm.datadriven.apm.flow.repository.mapper.ApmFlowNodeEventMapper;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowNodeEventService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author unknown
 */
@Service
public class ApmFlowNodeEventServiceImpl extends ServiceImpl<ApmFlowNodeEventMapper, ApmFlowNodeEvent>
    implements ApmFlowNodeEventService {

    @Override
    public List<ApmFlowNodeEvent> listByNodeBids(List<String> nodeBids) {
        List<ApmFlowNodeEvent> list = list(Wrappers.<ApmFlowNodeEvent>lambdaQuery().in(ApmFlowNodeEvent::getNodeBid, nodeBids));
        return list;
    }

    @Override
    public List<ApmFlowNodeEvent> listByNodeBid(String nodeBid) {
        List<ApmFlowNodeEvent> list = list(Wrappers.<ApmFlowNodeEvent>lambdaQuery().eq(ApmFlowNodeEvent::getNodeBid, nodeBid));
        return list;
    }

    @Override
    public List<ApmFlowNodeEvent> listByNodeBidAndType(int eventClassification, String nodeBid, String version) {
        List<ApmFlowNodeEvent> list = list(Wrappers.<ApmFlowNodeEvent>lambdaQuery()
                .eq(ApmFlowNodeEvent::getNodeBid, nodeBid)
                .eq(ApmFlowNodeEvent::getVersion, version)
                .eq(ApmFlowNodeEvent::getEventClassification,eventClassification));
        return list;
    }
}




