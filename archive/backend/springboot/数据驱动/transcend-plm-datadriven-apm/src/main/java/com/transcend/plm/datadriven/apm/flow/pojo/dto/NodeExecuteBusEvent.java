package com.transcend.plm.datadriven.apm.flow.pojo.dto;

import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import lombok.Builder;
import lombok.Data;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 节点执行事件，用于eventBus
 * @createTime 2023-11-08 14:21:00
 */
@Builder
@Data
public class NodeExecuteBusEvent {
    private Integer eventType;
    private String completeEmpNO;
    private String completeEmpName;
    private ApmFlowInstanceNode instanceNode;
    private MSpaceAppData pageMSpaceAppData;
}
