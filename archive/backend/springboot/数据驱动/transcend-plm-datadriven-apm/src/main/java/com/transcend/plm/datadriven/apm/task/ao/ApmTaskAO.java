package com.transcend.plm.datadriven.apm.task.ao;

import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmTaskAO {

    /**
     * 业务id
     */
    private String bid;

    /**
     * 任务类型，目前只有流程任务.flow
     */
    private String taskType;

    /**
     * 任务类型对应的bid,当是流程时，对应apm_flow_instance_node表的bid
     */
    private String bizBid;

    /**
     * 任务处理人
     */
    private List<String> handlers;
}
