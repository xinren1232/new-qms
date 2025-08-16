package com.transcend.plm.datadriven.apm.flow.pojo.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author unknown
 */
@Data
public class ApmFlowTemplateVO {
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
     * 启用标志，0未启用，1启用，2禁用
     */
    private Boolean enableFlag;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * 删除标识
     */
    private Boolean deleteFlag;

    /**
     * 说明
     */
    private String description;

    private List<ApmFlowTemplateNodeVO> apmFlowTemplateNodeVOList;
}
