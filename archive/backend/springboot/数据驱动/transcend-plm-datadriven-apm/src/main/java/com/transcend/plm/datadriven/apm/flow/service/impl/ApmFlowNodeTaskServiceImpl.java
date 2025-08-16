package com.transcend.plm.datadriven.apm.flow.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeTask;
import com.transcend.plm.datadriven.apm.flow.repository.mapper.ApmFlowNodeTaskMapper;
import com.transcend.plm.datadriven.apm.flow.service.ApmFlowNodeTaskService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author unknown
 */
@Service
public class ApmFlowNodeTaskServiceImpl extends ServiceImpl<ApmFlowNodeTaskMapper, ApmFlowNodeTask>
    implements ApmFlowNodeTaskService {

    @Override
    public List<ApmFlowNodeTask> listByNodeBids(List<String> nodeBids) {
        List<ApmFlowNodeTask> list = list(Wrappers.<ApmFlowNodeTask>lambdaQuery().in(ApmFlowNodeTask::getNodeBid, nodeBids));
        return list;
    }

    @Override
    public List<ApmFlowNodeTask> listByNodeBid(String nodeBid) {
        List<ApmFlowNodeTask> list = list(Wrappers.<ApmFlowNodeTask>lambdaQuery().eq(ApmFlowNodeTask::getNodeBid, nodeBid));
        return list;
    }
}




