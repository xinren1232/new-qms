package com.transcend.plm.configcenter.objectview.infrastructure.repository.po;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

/**
 * 视图配置表
 * @TableName cfg_view_config
 */
@TableName(value ="cfg_view_config")
@Data
public class CfgViewConfig extends BasePoEntity implements Serializable {
    /**
     * 主键
     */
    /*@TableId(type = IdType.AUTO)
    private Integer id;*/

    /**
     * 业务id
     */
    /*private String bid;*/

    /**
     * 视图名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 生命周期状态CODE(还有 ALL)
     */
    private String lcStateCode;

    /**
     * 标签集
     */
    private String tags;

    /**
     * 角色类型
     */
    private int roleType;

    /**
     * 优先级
     */
    private int priority;

    /**
     * 启用标志，0未启用，1启用，2禁用
     */
    /*private Boolean enableFlag;*/

    /**
     * 模型code
     */
    private String modelCode;

    /**
     * 租户ID
     */
    /*private Long tenantId;

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
     * 是否被实例化，使用
     */
    private Integer bindingFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}