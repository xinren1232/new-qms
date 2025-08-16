package com.transcend.plm.configcenter.api.model.lifecycle.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class CfgLifeCycleTemplateTransitionLineDto implements Serializable {

    private Long id;

    /**
     * 
     */
    private String bid;

    /**
     * 生命周期模板id
     */
    private String templateBid;

    /**
     * 说明
     */
    private String description;

    /**
     * 版本号
     */
    private String templateVersion;

    /**
     * 角色bid
     */
    private String roleBid;

    /**
     * 开始节点
     */
    private String source;

    /**
     * 结束节点
     */
    private String target;

    /**
     * 存储如坐标等信息
     */
    private String layout;

    /**
     * 
     */
    private String beforeMethod;

    /**
     * 
     */
    private String afterMethod;

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
    private Integer deleteFlag;

    /**
     * 状态（启用标志，0未启用，1启用，2禁用）
     */
    private Integer enableFlag = 1;
    private static final long serialVersionUID = 1L;

}