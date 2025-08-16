package com.transcend.plm.datadriven.apm.flow.pojo.vo;

import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowTemplateNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author peng.qin
 * @version 1.0.0
 * @Description 流程实例VO
 * @createTime 2023-10-11 15:12:00
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApmFlowInstanceVO {
    private Object layout;
    private List<ApmFlowInstanceNode> nodes;
    private List<ApmFlowTemplateNode> apmFlowTemplateNodes;
}
