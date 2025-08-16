package com.transcend.plm.datadriven.apm.flow.pojo.event;

import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

/**
 * 流程实例保存事件
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/10 15:20
 */
@Getter
@AllArgsConstructor
public class ApmFlowInstanceNodeChangeEvent {

    /**
     * 变更的实例列表
     */
    private final Collection<ApmFlowInstanceNode> entityList;

}
