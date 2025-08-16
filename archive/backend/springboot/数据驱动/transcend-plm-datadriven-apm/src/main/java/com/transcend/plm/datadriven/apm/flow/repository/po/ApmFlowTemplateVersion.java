package com.transcend.plm.datadriven.apm.flow.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程主表
 * @author unknown
 * @TableName amp_flow_template_version
 */
@TableName(value ="apm_flow_template_version")
@Data
public class ApmFlowTemplateVersion implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 业务id
     */
    private String bid;

    /**
     * 流程主表业务id
     */
    private String flowTemplateBid;

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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}