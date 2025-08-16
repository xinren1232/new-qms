package com.transcend.plm.datadriven.apm.task.vo;

import com.transcend.plm.datadriven.apm.flow.repository.po.ApmFlowInstanceNode;
import com.transcend.plm.datadriven.apm.space.model.MSpaceAppData;
import lombok.Data;

import java.util.Date;

/**
 * @author unknown
 */
@Data
public class ApmTaskVO {
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 任务类型，目前只有流程任务.flow
     */
    private String taskType;

    /**
     * 所属空间
     */
    private String spaceName;

    /**
     * 所属空间BID
     */
    private String spaceBid;

    /**
     * 所属空间应用BID
     */
    private String spaceAppBid;

    /**
     * 任务类型对应的bid,当是流程时，对应apm_flow_instance_node表的bid
     */
    private String bizBid;

    /**
     * 任务状态，0.未开始，1.进行中，2.完成
     */
    private Integer taskState;

    /**
     * 处理人
     */
    private String taskHandler;

    /**
     * 处理
     */
    private Date handleTime;

    /**
     * 应用图标
     */
    private String iconUrl;

    /**
     * 实例数据
     */
    private MSpaceAppData instance;

    private ApmFlowInstanceNode apmFlowInstanceNode;
}
