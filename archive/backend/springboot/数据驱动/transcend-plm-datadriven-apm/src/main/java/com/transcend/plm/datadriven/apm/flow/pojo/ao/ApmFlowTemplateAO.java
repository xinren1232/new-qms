package com.transcend.plm.datadriven.apm.flow.pojo.ao;

import lombok.Data;

import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmFlowTemplateAO {
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 流程模板名称
     */
    private String name;

    /**
     * 所属类型
     */
    private String type;

    /**
     * 空间应用bid
     */
    private String spaceAppBid;

    /**
     * 对象模型编码
     */
    private String modelCode;

    /**
     * 当前版本
     */
    private String version;

    /**
     * 前端页面布局
     */
    private String layout;

    /**
     * 说明
     */
    private String description;

    /**
     * 流程节点信息
     */
    private List<ApmFlowTemplateNodeAO> apmFlowTemplateNodeAOList;

    /**
     * 流程节点连线信息
     */
    private List<ApmFlowNodeLineAO> apmFlowNodeLineAOList;


}
