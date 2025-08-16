package com.transcend.plm.datadriven.apm.flow.pojo.event;

import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceRoleUser;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

/**
 * 流程实例角色用户变更事件
 *
 * @author xin.wu2 <xin.wu2@transsion.com>
 * @version 1.0
 * createdAt 2025/4/10 15:22
 */
@Getter
@AllArgsConstructor
public class ApmFlowInstanceRoleUserChangeEvent {

    /**
     * 变更的实例数据
     */
    private Collection<ApmFlowInstanceRoleUser> entityList;
}
