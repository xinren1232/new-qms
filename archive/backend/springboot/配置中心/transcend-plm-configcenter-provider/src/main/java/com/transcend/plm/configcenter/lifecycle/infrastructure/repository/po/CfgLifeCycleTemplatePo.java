package com.transcend.plm.configcenter.lifecycle.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName cfg_life_cycle_template
 */
@TableName(value ="cfg_life_cycle_template")
@Data
public class CfgLifeCycleTemplatePo extends BasePoEntity implements Serializable {
    /**
     * 
     */
    /*@TableId(type = IdType.AUTO)
    private Long id;*/

    /**
     * 生命周期模板名称
     */
    private String name;

    /**
     * 当前版本
     */
    private String currentVersion;


    /**
     * 业务id
     */
    /*private String bid;*/

   /* *//**
     * 租户ID
     *//*
    private Long tenantId;

    *//**
     * 创建人
     *//*
    private String createdBy;

    *//**
     * 创建时间
     *//*
    private Date createdTime;

    *//**
     * 更新人
     *//*
    private String updatedBy;

    *//**
     * 更新时间
     *//*
    private Date updatedTime;

    *//**
     * 删除标识
     *//*
    private Boolean deleteFlag;*/

    /**
     * 说明
     */
    private String description;

    /**
     * 是否阶段状态，0否，1是
     */
    private Integer phaseState;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}