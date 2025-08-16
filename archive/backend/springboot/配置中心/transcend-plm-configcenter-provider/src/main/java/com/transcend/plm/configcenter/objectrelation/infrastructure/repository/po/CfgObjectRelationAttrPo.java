package com.transcend.plm.configcenter.objectrelation.infrastructure.repository.po;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import com.transcend.plm.configcenter.common.pojo.po.BasePoEntity;
import lombok.Data;

/**
 * 对象属性表
 * @TableName cfg_object_relation_attr
 */
@TableName(value ="cfg_object_relation_attr")
@Data
public class CfgObjectRelationAttrPo extends BasePoEntity implements Serializable {
    /**
     * 主键ID
     */
    /*@TableId(type = IdType.AUTO)
    private Integer id;*/

    /**
     * 业务id
     */
    /*private String bid;*/

    /**
     * cfg_object_relation关联对象的业务ID
     */
    private String relationBid;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 内部名称
     */
    private String innerName;

    /**
     * 来源(target:目标对象,relation:关系对象)
     */
    private String sourceModel;

    /**
     * 目标对象bid
     */
    private String sourceModelCode;

    /**
     * 
     */
    @TableField("`explain`")
    private String explain;

    /**
     * 列宽
     */
    private Integer columnWidth;

    /**
     * 是否在列表中显示(具体的关系配置，区分不同源对象)
     */
    private Integer realUseInView;

    /**
     * 关系中排序(具体的关系配置，区分不同源对象)
     */
    private Integer realRelativeSort;

    /**
     * 是否用于默认查询
     */
    private Integer useInQuery;

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
     * 启用标志，0未启用，1启用，2禁用
     */
    /*private Boolean enableFlag;*/

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}