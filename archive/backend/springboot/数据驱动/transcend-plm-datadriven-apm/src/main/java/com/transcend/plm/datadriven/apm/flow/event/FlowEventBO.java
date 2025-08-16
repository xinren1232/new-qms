package com.transcend.plm.datadriven.apm.flow.event;

import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowNodeEvent;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import lombok.Data;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程事件上下文
 * @createTime 2023-10-08 13:43:00
 */
@Data
public class FlowEventBO {
    private ApmFlowInstanceNode instanceNode;
    private ApmFlowNodeEvent event;
    private String completeEmpNO;
    private String completeEmpName;
    private MSpaceAppData pageMSpaceAppData;
}
